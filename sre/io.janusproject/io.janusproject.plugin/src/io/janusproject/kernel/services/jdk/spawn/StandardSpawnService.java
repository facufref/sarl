/*
 * $Id$
 *
 * SARL is an general-purpose agent programming language.
 * More details on http://www.sarl.io
 *
 * Copyright (C) 2014-2017 the original authors or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.janusproject.kernel.services.jdk.spawn;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Service;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import io.janusproject.kernel.bic.BuiltinCapacityUtil;
import io.janusproject.services.AbstractDependentService;
import io.janusproject.services.contextspace.ContextSpaceService;
import io.janusproject.services.executor.ExecutorService;
import io.janusproject.services.logging.LogService;
import io.janusproject.services.spawn.KernelAgentSpawnListener;
import io.janusproject.services.spawn.SpawnService;
import io.janusproject.services.spawn.SpawnServiceListener;
import io.janusproject.util.ListenerCollection;

import io.sarl.core.AgentKilled;
import io.sarl.core.AgentSpawned;
import io.sarl.core.Logging;
import io.sarl.lang.core.Address;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.AgentContext;
import io.sarl.lang.core.BuiltinCapacitiesProvider;
import io.sarl.lang.core.EventSpace;
import io.sarl.lang.core.SREutils;
import io.sarl.lang.util.SynchronizedIterable;
import io.sarl.lang.util.SynchronizedSet;
import io.sarl.sarlspecification.SarlSpecificationChecker;
import io.sarl.util.Collections3;

/**
 * Implementation of a spawning service that is based on the other services of the Janus platform.
 *
 * @author $Author: srodriguez$
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@Singleton
public class StandardSpawnService extends AbstractDependentService implements SpawnService {

	/** Maximum number of agents to be launch by a single thread.
	 */
	private static final int CREATION_POOL_SIZE = 128;

	private final ListenerCollection<?> globalListeners = new ListenerCollection<>();

	// TODO The use of two maps is slowly the platform
	private final Map<UUID, ListenerCollection<SpawnServiceListener>> agentLifecycleListeners = new TreeMap<>();

	// TODO The use of two maps is slowly the platform
	private final Map<UUID, Agent> agents = new TreeMap<>();

	private final Injector injector;

	private final SarlSpecificationChecker sarlSpecificationChecker;

	@Inject
	private ExecutorService executor;

	@Inject
	private LogService logger;

	@Inject
	private BuiltinCapacitiesProvider builtinCapacityProvider;

	/**
	 * Constructs the service with the given (injected) injector.
	 *
	 * @param injector
	 *            the injector that should be used by this service for creating the agents.
	 * @param sarlSpecificationChecker the tool for checking the validity of the SARL specification supported by
	 *      the agents to launch.
	 */
	@Inject
	public StandardSpawnService(Injector injector, SarlSpecificationChecker sarlSpecificationChecker) {
		this.injector = injector;
		this.sarlSpecificationChecker = sarlSpecificationChecker;
	}

	/** Replies the mutex for synchronizing on agent repository.
	 *
	 * @return the mutex.
	 */
	protected final Object getAgentRepositoryMutex() {
		return this.agents;
	}

	/** Replies the mutex for synchronizing on agent-lifecycle listeners.
	 *
	 * @return the mutex.
	 */
	protected final Object getAgentLifecycleListenerMutex() {
		return this.agentLifecycleListeners;
	}

	@Override
	public final Class<? extends Service> getServiceType() {
		return SpawnService.class;
	}

	@Override
	public Collection<Class<? extends Service>> getServiceDependencies() {
		return Arrays.<Class<? extends Service>>asList(ContextSpaceService.class);
	}

	private void ensureSarlSpecificationVersion(Class<? extends Agent> agentClazz) {
		if (!this.sarlSpecificationChecker.isValidSarlElement(agentClazz)) {
			throw new InvalidSarlSpecificationException(agentClazz);
		}
	}

	@Override
	public List<UUID> spawn(int nbAgents, UUID spawningAgent, AgentContext parent, UUID agentID,
			Class<? extends Agent> agentClazz, Object... params) {
		if (isRunning() && nbAgents > 0) {
			try {
				// Check if the version of the SARL agent class is compatible.
				ensureSarlSpecificationVersion(agentClazz);
				// Create the shared injector that is also able to create the agent instance.
				final JustInTimeAgentInjectionModule agentInjectionModule = new JustInTimeAgentInjectionModule(
						agentClazz, parent.getID(), agentID);
				final Injector agentInjector = this.injector.createChildInjector(agentInjectionModule);
				// Create the list of the spawned agents during this function execution
				final List<Agent> agents = new ArrayList<>(nbAgents);
				// Create the block of code for creating a single agent
				final Runnable agentCreator = () -> {
					final Agent agent = agentInjector.getInstance(Agent.class);
					assert agent != null;
					// Create the builtin capacities / skill installation will be done later in the life cycle.
					StandardSpawnService.this.builtinCapacityProvider.builtinCapacities(agent, (capacity, skill) -> {
						try {
							SREutils.createSkillMapping(agent, capacity, skill);
						} catch (Exception e) {
							throw new Error(Messages.StandardSpawnService_5, e);
						}
					});
					// Add the agent in the system
					synchronized (this.agents) {
						this.agents.put(agent.getID(), agent);
					}
					synchronized (agents) {
						agents.add(agent);
					}
					fireAgentSpawnedInAgent(spawningAgent, parent, agent, params);
				};
				// Create a single agent with a sequential call; or multiple agents in parallel
				if (nbAgents > 1) {
					this.executor.executeMultipleTimesInParallelAndWaitForTermination(
							agentCreator, nbAgents, CREATION_POOL_SIZE);
				} else {
					agentCreator.run();
				}
				// Fire the general spawning event
				fireAgentSpawnedOutsideAgent(spawningAgent, parent, agentClazz, agents, params);
				return Collections.unmodifiableList(Lists.transform(agents, (it) -> it.getID()));
			} catch (Throwable e) {
				throw new CannotSpawnException(agentClazz, e);
			}
		}
		throw new SpawnDisabledException(parent.getID(), agentClazz);
	}

	/** Notify the listeners about the agents' spawning.
	 *
	 * @param spawningAgent the spawning agent.
	 * @param context the context in which the agents were spawned.
	 * @param agentClazz the type of the spwnaed agents.
	 * @param agents the spawned agents.
	 * @param initializationParameters the initialization parameters.
	 */
	protected void fireAgentSpawnedOutsideAgent(UUID spawningAgent, AgentContext context, Class<? extends Agent> agentClazz, List<Agent> agents,
			Object... initializationParameters) {
		// Notify the listeners on the spawn events (not restricted to a single agent)
		for (final SpawnServiceListener l : this.globalListeners.getListeners(SpawnServiceListener.class)) {
			l.agentSpawned(spawningAgent, context, agents, initializationParameters);
		}

		// Send the event in the default space.
		final EventSpace defSpace = context.getDefaultSpace();
		assert defSpace != null : "A context does not contain a default space"; //$NON-NLS-1$
		final Address source = new Address(defSpace.getSpaceID(),
				spawningAgent == null ? context.getID() : spawningAgent);
		assert source != null;
		final AgentSpawned event = new AgentSpawned(source, agentClazz.getName(),
				Collections2.transform(agents, (it) -> it.getID()));
		defSpace.emit(
				// No need to give an event source because it is explicitly set above.
				null,
				event);
	}

	/** Notify the agent's listeners about its spawning.
	 *
	 * @param spawningAgent the spawning agent.
	 * @param context the context in which the agent was spawned.
	 * @param agent the spawned agent.
	 * @param initializationParameters the initialization parameters.
	 */
	protected void fireAgentSpawnedInAgent(UUID spawningAgent, AgentContext context, Agent agent, Object... initializationParameters) {
		// Notify the listeners on the lifecycle events on
		// the just spawned agent.
		// Usually, only BICs and the AgentLifeCycleSupport in
		// io.janusproject.kernel.bic.StandardBuiltinCapacitiesProvider
		// is invoked.
		final ListenerCollection<SpawnServiceListener> list;
		synchronized (this.agentLifecycleListeners) {
			list = this.agentLifecycleListeners.get(agent.getID());
		}
		if (list != null) {
			final List<Agent> singleton = Collections.singletonList(agent);
			for (final SpawnServiceListener l : list.getListeners(SpawnServiceListener.class)) {
				l.agentSpawned(spawningAgent, context, singleton, initializationParameters);
			}
		}
	}

	@Override
	public boolean killAgent(UUID agentID) {
		final boolean error = !isRunning();

		// We should check if it is possible to kill the agent BEFORE killing it.
		final boolean foundAgent;
		boolean isLast = false;
		Agent killAgent = null;
		final String warningMessage;
		synchronized (getAgentRepositoryMutex()) {
			final Agent agent = this.agents.get(agentID);
			foundAgent = agent != null;
			if (foundAgent) {
				if (canKillAgent(agent)) {
					this.agents.remove(agentID);
					isLast = this.agents.isEmpty();
					killAgent = agent;
					warningMessage = null;
				} else {
					warningMessage = Messages.StandardSpawnService_7;
				}
			} else {
				warningMessage = Messages.StandardSpawnService_8;
			}
		}

		if (warningMessage == null) {
			assert killAgent != null;

			fireAgentDestroyed(killAgent);

			if (isLast) {
				fireKernelAgentDestroy();
			}
			if (error) {
				throw new SpawnServiceStopException(agentID);
			}
			return true;
		}

		if (killAgent != null) {
			try {
				final Logging skill = SREutils.getInternalSkill(killAgent, Logging.class);
				skill.warning(warningMessage);
			} catch (Exception e) {
				throw new Error(Messages.StandardSpawnService_9, e);
			}
		} else {
			this.logger.warning(warningMessage);
		}

		return false;
	}

	/**
	 * Replies the registered agents.
	 *
	 * @return the registered agents.
	 */
	public SynchronizedSet<UUID> getAgents() {
		final Object mutex = getAgentRepositoryMutex();
		synchronized (mutex) {
			return Collections3.synchronizedSet(this.agents.keySet(), mutex);
		}
	}

	/**
	 * Replies the registered agent.
	 *
	 * @param id
	 *            is the identifier of the agent.
	 * @return the registered agent, or <code>null</code>.
	 */
	Agent getAgent(UUID id) {
		assert id != null;
		synchronized (getAgentRepositoryMutex()) {
			return this.agents.get(id);
		}
	}

	@Override
	public void addKernelAgentSpawnListener(KernelAgentSpawnListener listener) {
		this.globalListeners.add(KernelAgentSpawnListener.class, listener);
	}

	@Override
	public void removeKernelAgentSpawnListener(KernelAgentSpawnListener listener) {
		this.globalListeners.remove(KernelAgentSpawnListener.class, listener);
	}

	/**
	 * Notifies the listeners about the kernel agent creation.
	 */
	protected void fireKernelAgentSpawn() {
		for (final KernelAgentSpawnListener l : this.globalListeners.getListeners(KernelAgentSpawnListener.class)) {
			l.kernelAgentSpawn();
		}
	}

	/**
	 * Notifies the listeners about the kernel agent destruction.
	 */
	protected void fireKernelAgentDestroy() {
		for (final KernelAgentSpawnListener l : this.globalListeners.getListeners(KernelAgentSpawnListener.class)) {
			l.kernelAgentDestroy();
		}
	}

	@Override
	public void addSpawnServiceListener(UUID id, SpawnServiceListener agentLifecycleListener) {
		synchronized (getAgentLifecycleListenerMutex()) {
			ListenerCollection<SpawnServiceListener> listeners = this.agentLifecycleListeners.get(id);
			if (listeners == null) {
				listeners = new ListenerCollection<>();
				this.agentLifecycleListeners.put(id, listeners);
			}
			listeners.add(SpawnServiceListener.class, agentLifecycleListener);
		}
	}

	@Override
	public void addSpawnServiceListener(SpawnServiceListener agentLifecycleListener) {
		this.globalListeners.add(SpawnServiceListener.class, agentLifecycleListener);
	}

	@Override
	public void removeSpawnServiceListener(UUID id, SpawnServiceListener agentLifecycleListener) {
		synchronized (getAgentLifecycleListenerMutex()) {
			final ListenerCollection<SpawnServiceListener> listeners = this.agentLifecycleListeners.get(id);
			if (listeners != null) {
				listeners.remove(SpawnServiceListener.class, agentLifecycleListener);
				if (listeners.isEmpty()) {
					this.agentLifecycleListeners.remove(id);
				}
			}
		}
	}

	@Override
	public void removeSpawnServiceListener(SpawnServiceListener agentLifecycleListener) {
		this.globalListeners.remove(SpawnServiceListener.class, agentLifecycleListener);
	}

	/**
	 * Replies if the given agent can be killed.
	 *
	 * @param agent
	 *            - agent to test.
	 * @return <code>true</code> if the given agent can be killed, otherwise <code>false</code>.
	 */
	@SuppressWarnings("static-method")
	public boolean canKillAgent(Agent agent) {
		try {
			final AgentContext ac = BuiltinCapacityUtil.getContextIn(agent);
			if (ac != null) {
				final SynchronizedSet<UUID> participants = ac.getDefaultSpace().getParticipants();
				if (participants != null) {
					synchronized (participants.mutex()) {
						if (participants.size() > 1 || (participants.size() == 1
								&& !participants.contains(agent.getID()))) {
							return false;
						}
					}
				}
			}
			return true;
		} catch (Throwable exception) {
			return false;
		}
	}

	/**
	 * Notifies the listeners about the agent destruction.
	 *
	 * @param agent
	 *            - the destroyed agent.
	 */
	protected void fireAgentDestroyed(Agent agent) {
		final ListenerCollection<SpawnServiceListener> list;
		synchronized (getAgentLifecycleListenerMutex()) {
			list = this.agentLifecycleListeners.get(agent.getID());
		}
		final SpawnServiceListener[] ilisteners;
		if (list != null) {
			ilisteners = list.getListeners(SpawnServiceListener.class);
		} else {
			ilisteners = null;
		}
		final SpawnServiceListener[] ilisteners2 = this.globalListeners.getListeners(SpawnServiceListener.class);

		try {
			final SynchronizedIterable<AgentContext> sc = BuiltinCapacityUtil.getContextsOf(agent);
			synchronized (sc.mutex()) {
				for (final AgentContext context : sc) {
					final EventSpace defSpace = context.getDefaultSpace();
					defSpace.emit(
							// No need to give an event source because it is explicitly set below.
							null,
							new AgentKilled(defSpace.getAddress(agent.getID()), agent.getID(), agent.getClass().getName()));
				}
			}
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		if (ilisteners != null) {
			for (final SpawnServiceListener l : ilisteners) {
				l.agentDestroy(agent);
			}
		}
		for (final SpawnServiceListener l : ilisteners2) {
			l.agentDestroy(agent);
		}
	}

	@Override
	protected void doStart() {
		// Assume that when the service is starting, the kernel agent is up.
		fireKernelAgentSpawn();
		notifyStarted();
	}

	@Override
	protected void doStop() {
		synchronized (getAgentLifecycleListenerMutex()) {
			this.agentLifecycleListeners.clear();
		}
		notifyStopped();
	}

	/**
	 * This exception is thrown when the spawning service of agents is disabled.
	 *
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	public static class SpawnDisabledException extends RuntimeException {

		private static final long serialVersionUID = -380402400888610762L;

		/**
		 * @param parentID
		 *            - the identifier of the parent entity that is creating the agent.
		 * @param agentClazz
		 *            - the type of the agent to spawn.
		 */
		public SpawnDisabledException(UUID parentID, Class<? extends Agent> agentClazz) {
			super(MessageFormat.format(Messages.StandardSpawnService_0, parentID, agentClazz));
		}

	}

	/**
	 * This exception is thrown when the spawning service is not running when the killing function on an agent is called.
	 *
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	public static class SpawnServiceStopException extends RuntimeException {

		private static final long serialVersionUID = 8104012713598435249L;

		/**
		 * @param agentID
		 *            - the identifier of the agent.
		 */
		public SpawnServiceStopException(UUID agentID) {
			super(MessageFormat.format(Messages.StandardSpawnService_1, agentID));
		}

	}

	/**
	 * This exception is thrown when the agent to spawn is not generated according to a valid SARL specification version.
	 *
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	public static class InvalidSarlSpecificationException extends RuntimeException {

		private static final long serialVersionUID = -3194494637438344108L;

		/**
		 * @param agentType
		 *            the invalid type of agent.
		 */
		public InvalidSarlSpecificationException(Class<? extends Agent> agentType) {
			super(MessageFormat.format(Messages.StandardSpawnService_2, agentType.getName()));
		}

	}

	/**
	 * This exception is thrown when an agent cannot be spawned.
	 *
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	public static class CannotSpawnException extends RuntimeException {

		private static final long serialVersionUID = -380402400888610762L;

		/**
		 * @param agentClazz
		 *            - the type of the agent to spawn.
		 * @param cause
		 *            - the cause of the exception.
		 */
		public CannotSpawnException(Class<? extends Agent> agentClazz, Throwable cause) {
			super(MessageFormat.format(Messages.StandardSpawnService_3, agentClazz,
					(cause == null) ? null : cause.getLocalizedMessage()), cause);
		}

	}

	/**
	 * An injection module that is able to inject the parent ID and agent ID when creating an agent.
	 *
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	private static class JustInTimeAgentInjectionModule extends AbstractModule implements Provider<Agent> {

		private final Class<? extends Agent> agentType;

		private final Constructor<? extends Agent> constructor1;

		private final Constructor<? extends Agent> constructor2;

		private final UUID parentID;

		private final UUID agentID;

		JustInTimeAgentInjectionModule(Class<? extends Agent> agentType, UUID parentID, UUID agentID) {
			assert agentType != null;
			assert parentID != null;
			this.agentType = agentType;
			this.parentID = parentID;
			this.agentID = (agentID == null) ? UUID.randomUUID() : agentID;
			Constructor<? extends Agent> cons;
			Exception e1 = null;
			try {
				cons = this.agentType.getConstructor(UUID.class, UUID.class);
			} catch (NoSuchMethodException | SecurityException | IllegalArgumentException exception) {
				cons = null;
				e1 = exception;
			}
			this.constructor1 = cons;
			Exception e2 = null;
			try {
				cons = this.agentType.getConstructor(BuiltinCapacitiesProvider.class, UUID.class, UUID.class);
			} catch (NoSuchMethodException | SecurityException | IllegalArgumentException exception) {
				cons = null;
				e2 = exception;
			}
			this.constructor2 = cons;
			if (this.constructor1 == null && this.constructor2 == null) {
				throw new CannotSpawnException(this.agentType, e1 == null ? e2 : e1);
			}
		}

		@Override
		public void configure() {
			bind(Agent.class).toProvider(this);
		}

		@Override
		public Agent get() {
			assert this.constructor1 != null || this.constructor2 != null;
			try {
				if (this.constructor1 != null) {
					return this.constructor1.newInstance(this.parentID, this.agentID);
				}
				return this.constructor2.newInstance(null, this.parentID, this.agentID);
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException exception) {
				throw new CannotSpawnException(this.agentType, exception);
			}
		}

	}

}
