/*
 * $Id$
 *
 * Janus platform is an open-source multiagent platform.
 * More details on http://www.janusproject.io
 *
 * Copyright (C) 2014-2015 Sebastian RODRIGUEZ, Nicolas GAUD, Stéphane GALLAND.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.janusproject.tests.bugs;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import io.janusproject.tests.testutils.AbstractJanusRunTest;

import io.sarl.core.AgentSpawned;
import io.sarl.core.DefaultContextInteractions;
import io.sarl.core.Lifecycle;
import io.sarl.lang.SARLVersion;
import io.sarl.lang.annotation.PerceptGuardEvaluator;
import io.sarl.lang.annotation.SarlSpecification;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.BuiltinCapacitiesProvider;
import io.sarl.lang.core.Event;

/**
 * Unit test for the issue #7138: Events being dropped on certain machines.
 * 
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 * @see https://github.com/sarl/sarl/issues/713
 */
@SuppressWarnings("all")
public class Bug713 extends AbstractJanusRunTest {

	static final int NB_EVENTS = 50000;

	static final int NB_EVENTS2 = NB_EVENTS / 6;

//	@Test
//	public void manyEventsToOneAgent() throws Exception {
//		runJanus(ReceiverAgent.class);
//		waitForTheKernel(STANDARD_TIMEOUT);
//		Set<?> num = getResult(Set.class, 0);
//		//System.out.println("EXCHANGED EVENTS: " + num.size());
//		assertEquals(NB_EVENTS, num.size());
//	}

	@Test
	public void manyEventsToSixAgents() throws Exception {
		runJanus(ReceiverAgent2.class);
		waitForTheKernel(STANDARD_TIMEOUT);
		
		Map<UUID, Set<Integer>> events = getResult(Map.class, 0);
		assertEquals(6, events.size());
		for (final Set<Integer> values : events.values()) {
			assertEquals(NB_EVENTS2, values.size());
		}
		
		int num = getResult(Number.class, 1).intValue();
		System.out.println("EXCHANGED EVENTS: " + num);
		assertEquals(NB_EVENTS2, num);
	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	@SarlSpecification(SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING)
	public static class HelloEvent extends Event {

		public HelloEvent() {
			super();
		}

	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	@SarlSpecification(SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING)
	public static class AnswerEvent extends Event {

		public final int value;

		public AnswerEvent(int value) {
			this.value = value;
		}

	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	@SarlSpecification(SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING)
	public static class ReceiverAgent extends TestingAgent {

		private final Set<Integer> counter = new TreeSet<>();

		public ReceiverAgent(BuiltinCapacitiesProvider provider, UUID parentID, UUID agentID) {
			super(provider, parentID, agentID);
		}

		@Override
		protected boolean runAgentTest() {
			//System.out.println("START RECEIVER");
			addResult(this.counter);
			getSkill(Lifecycle.class).spawn(SenderAgent.class);
			return false;
		}

		@PerceptGuardEvaluator
		private void $guardEvaluator$AgentSpawn(final AgentSpawned occurrence, final Collection<Runnable> ___SARLlocal_runnableCollection) {
			assert occurrence != null;
			assert ___SARLlocal_runnableCollection != null;
			___SARLlocal_runnableCollection.add(() -> $behaviorUnit$AgentSpawned$0(occurrence));
		}

		private void $behaviorUnit$AgentSpawned$0(final AgentSpawned occurrence) {
			//System.out.println("SAY HELLO");
			getSkill(DefaultContextInteractions.class).emit(new HelloEvent());
		}

		@PerceptGuardEvaluator
		private void $guardEvaluator$AnswerEvent(final AnswerEvent occurrence, final Collection<Runnable> ___SARLlocal_runnableCollection) {
			assert occurrence != null;
			assert ___SARLlocal_runnableCollection != null;
			___SARLlocal_runnableCollection.add(() -> $behaviorUnit$AnswerEvent$0(occurrence));
		}

		private void $behaviorUnit$AnswerEvent$0(final AnswerEvent occurrence) {
			//System.out.println("RECEIVE ANSWER " + occurrence.value);
			int size;
			synchronized (this) {
				this.counter.add(occurrence.value);
				size = this.counter.size();
			}
			if (size >= NB_EVENTS) {
				getSkill(Lifecycle.class).killMe();
			}
		}

	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	@SarlSpecification(SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING)
	public static class ReceiverAgent2 extends TestingAgent {

		private final Map<UUID, Set<Integer>> counter = new TreeMap<>();
		
		private final AtomicInteger counter2 = new AtomicInteger();

		public ReceiverAgent2(BuiltinCapacitiesProvider provider, UUID parentID, UUID agentID) {
			super(provider, parentID, agentID);
		}

		@Override
		protected boolean runAgentTest() {
			System.out.println("START RECEIVER");
			addResult(this.counter);
			addResult(this.counter2);
			final Lifecycle skill = getSkill(Lifecycle.class);
			for (int i = 0; i < 6; ++i) {
				skill.spawn(SenderAgent.class);
			}
			return false;
		}

		@PerceptGuardEvaluator
		private void $guardEvaluator$AgentSpawn(final AgentSpawned occurrence, final Collection<Runnable> ___SARLlocal_runnableCollection) {
			assert occurrence != null;
			assert ___SARLlocal_runnableCollection != null;
			___SARLlocal_runnableCollection.add(() -> $behaviorUnit$AgentSpawned$0(occurrence));
		}

		private void $behaviorUnit$AgentSpawned$0(final AgentSpawned occurrence) {
			int size;
			synchronized (this) {
				this.counter.put(occurrence.getSource().getUUID(), new TreeSet<>());
				size = this.counter.size();
			}
			System.out.println("PARTNER SPAWN: " + size);
			if (size >= 6) {
				getSkill(DefaultContextInteractions.class).emit(new HelloEvent());
			}
		}

		@PerceptGuardEvaluator
		private void $guardEvaluator$AnswerEvent(final AnswerEvent occurrence, final Collection<Runnable> ___SARLlocal_runnableCollection) {
			assert occurrence != null;
			assert ___SARLlocal_runnableCollection != null;
			___SARLlocal_runnableCollection.add(() -> $behaviorUnit$AnswerEvent$0(occurrence));
		}

		private void $behaviorUnit$AnswerEvent$0(final AnswerEvent occurrence) {
			System.out.println("RECEIVE ANSWER " + occurrence.value);
			int size;
			synchronized (this) {
				Set<Integer> values = this.counter.get(occurrence.getSource().getUUID());
				if (values != null) {
					values.add(occurrence.value);
				}
				size = this.counter2.incrementAndGet();
			}
			if (size >= NB_EVENTS2 * 6) {
				getSkill(Lifecycle.class).killMe();
			}
		}

	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	@SarlSpecification(SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING)
	public static class SenderAgent extends Agent {

		public SenderAgent(BuiltinCapacitiesProvider provider, UUID parentID, UUID agentID) {
			super(provider, parentID, agentID);
		}

		@PerceptGuardEvaluator
		private void $guardEvaluator$HelloEvent(final HelloEvent occurrence, final Collection<Runnable> ___SARLlocal_runnableCollection) {
			assert occurrence != null;
			assert ___SARLlocal_runnableCollection != null;
			___SARLlocal_runnableCollection.add(() -> $behaviorUnit$HelloEvent$0(occurrence));
		}

		private void $behaviorUnit$HelloEvent$0(final HelloEvent occurrence) {
			System.out.println("RECEIVE HELLO");
			sendEvents();
			getSkill(Lifecycle.class).killMe();
		}

		private void sendEvents() {
			final DefaultContextInteractions skill = getSkill(DefaultContextInteractions.class);
			for (int i = 0; i < NB_EVENTS2; ++i) {
				System.out.println("SEND ANSWER " + i);
				skill.emit(new AnswerEvent(i));
			}
		}

	}

}
