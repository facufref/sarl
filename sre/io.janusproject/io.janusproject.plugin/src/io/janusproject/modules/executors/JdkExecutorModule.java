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

package io.janusproject.modules.executors;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Provider;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import org.eclipse.xtext.xbase.lib.IntegerRange;

import io.janusproject.JanusConfig;
import io.janusproject.kernel.services.jdk.executors.JdkExecutorService;
import io.janusproject.kernel.services.jdk.executors.JdkRejectedExecutionHandler;
import io.janusproject.kernel.services.jdk.executors.JdkUncaughtExceptionHandler;
import io.janusproject.services.executor.ExecutorService;

/**
 * Configure the module for the {@code ExecutorService} based on the JDF.
 *
 * @author $Author: srodriguez$
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class JdkExecutorModule extends AbstractModule {

	@Override
	protected void configure() {
		// Thread catchers
		bind(UncaughtExceptionHandler.class).to(JdkUncaughtExceptionHandler.class).in(Singleton.class);
		bind(RejectedExecutionHandler.class).to(JdkRejectedExecutionHandler.class).in(Singleton.class);

		// Bind the background objects
		//bind(ThreadFactory.class).to(JdkThreadFactory.class).in(Singleton.class);
		//bind(java.util.concurrent.ExecutorService.class).to(JdkThreadPoolExecutor.class).in(Singleton.class);
		//bind(ScheduledExecutorService.class).to(JdkScheduledThreadPoolExecutor.class).in(Singleton.class);
		bind(java.util.concurrent.ExecutorService.class).toProvider(ExecutorProvider.class).in(Singleton.class);
		bind(ScheduledExecutorService.class).toProvider(ScheduledExecutorProvider.class).in(Singleton.class);
		bind(ThreadFactory.class).toProvider(ThreadFactoryProvider.class);

		// Bind the service
		bind(ExecutorService.class).to(JdkExecutorService.class).in(Singleton.class);
	}

	/** Abstract implementation for a provider of a low-level executor service.
	 *
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 * @since 2.0.6.0
	 */
	public static class ThreadFactoryProvider implements Provider<ThreadFactory> {

		private ThreadFactory defaultFactory;

		private int priority = -1;

		/** Replies the default thread factory.
		 *
		 * @return the default thread factory.
		 */
		protected ThreadFactory getDefaultThreadFactory() {
			if (this.defaultFactory == null) {
				this.defaultFactory = Executors.defaultThreadFactory();
			}
			return this.defaultFactory;
		}

		/** Change the default thread factory to be used by this provider.
		 *
		 * @param factory the default factory.
		 */
		public void setDefaultThreadFactory(ThreadFactory factory) {
			this.defaultFactory = factory;
		}

		/** Replies the default thread priority.
		 *
		 * @return the default thread priority.
		 */
		protected int getThreadPriority() {
			if (this.priority < 0) {
				this.priority = JanusConfig.getSystemPropertyAsThreadPriority(
						JanusConfig.MIN_NUMBER_OF_THREADS_IN_EXECUTOR_NAME);
			}
			return this.priority;
		}

		@Override
		public ThreadFactory get() {
			return (runnable) -> {
				final Thread thread = getDefaultThreadFactory().newThread(runnable);
				thread.setDaemon(true);
				thread.setPriority(getThreadPriority());
				return thread;
			};
		}

	}

	/** Abstract implementation for a provider of a low-level executor service.
	 *
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 * @since 2.0.6.0
	 */
	public static class AbstractExecutorProvider {

		private RejectedExecutionHandler rejectedExecutionHandler;

		private ThreadFactory threadFactory;

		private long keepAliveDuration = -1;

		private int minPoolSize = -1;

		private int maxPoolSize = -1;

		private Boolean rejectedTaskTracking;

		/** Change the handler for rejected executions.
		 *
		 * @param handler the handler.
		 */
		@com.google.inject.Inject(optional = true)
		public void setRejectedExecutionHandler(RejectedExecutionHandler handler) {
			this.rejectedExecutionHandler = handler;
		}

		/** Replies the handler for rejected executions.
		 *
		 * @return the handler.
		 */
		public RejectedExecutionHandler getRejectedExecutionHandler() {
			return this.rejectedExecutionHandler;
		}

		/** Change the thread factory.
		 *
		 * @param factory the thread factory.
		 */
		@Inject
		public void setThreadFactory(ThreadFactory factory) {
			this.threadFactory = factory;
		}

		/** Replies the thread factory.
		 *
		 * @return the thread factory.
		 */
		public ThreadFactory getThreadFactory() {
			return this.threadFactory;
		}

		/** Replies the duration for keeping alive a idle thread.
		 *
		 * @return the duration.
		 */
		protected long getKeepAliveDuration() {
			if (this.keepAliveDuration < 0) {
				this.keepAliveDuration = JanusConfig.getSystemPropertyAsInteger(JanusConfig.THREAD_KEEP_ALIVE_DURATION_NAME,
						JanusConfig.THREAD_KEEP_ALIVE_DURATION_VALUE);
			}
			return this.keepAliveDuration;
		}

		/** Replies the min and max numbers of threads in the pool.
		 *
		 * @return the min and max numbers.
		 */
		protected IntegerRange getPoolSize() {
			if (this.minPoolSize < 0 || this.maxPoolSize < 0) {
				final int min = JanusConfig.getSystemPropertyAsInteger(JanusConfig.MIN_NUMBER_OF_THREADS_IN_EXECUTOR_NAME,
						JanusConfig.MIN_NUMBER_OF_THREADS_IN_EXECUTOR_VALUE);
				final int max = JanusConfig.getSystemPropertyAsInteger(JanusConfig.MAX_NUMBER_OF_THREADS_IN_EXECUTOR_NAME,
						JanusConfig.MAX_NUMBER_OF_THREADS_IN_EXECUTOR_VALUE);
				this.minPoolSize = Math.max(0, Math.min(min, max));
				this.maxPoolSize = Math.max(1, Math.max(min, max));
			}
			return new IntegerRange(this.minPoolSize, this.maxPoolSize);
		}

		/** Track the rejected tasks if the configuration flag is set.
		 *
		 * @param executor the executor to configure.
		 */
		protected void configureRejectedTasksTracking(ThreadPoolExecutor executor) {
			if (this.rejectedTaskTracking == null) {
				final boolean logRejected = JanusConfig.getSystemPropertyAsBoolean(JanusConfig.REJECTED_TASK_TRACKING_NAME,
						JanusConfig.REJECTED_TASK_TRACKING_VALUE);
				this.rejectedTaskTracking = Boolean.valueOf(logRejected && getRejectedExecutionHandler() != null);
			}
			if (this.rejectedTaskTracking.booleanValue()) {
				executor.setRejectedExecutionHandler(getRejectedExecutionHandler());
			}
		}

	}

	/** Provider of a low-level executor service.
	 *
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	public static class ExecutorProvider extends AbstractExecutorProvider
			implements Provider<java.util.concurrent.ExecutorService> {

		@Override
		public java.util.concurrent.ExecutorService get() {
			final IntegerRange poolSize = getPoolSize();
			final long keepAliveDuration = getKeepAliveDuration();
			final ThreadPoolExecutor executor = new ThreadPoolExecutor(
					poolSize.getStart(), poolSize.getEnd(),
					keepAliveDuration, TimeUnit.SECONDS,
					new SynchronousQueue<Runnable>(),
					getThreadFactory());
			executor.allowCoreThreadTimeOut(keepAliveDuration > 0);
			configureRejectedTasksTracking(executor);
			return executor;
		}

	}

	/** Provider of a low-level scheduled executor service.
	 *
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	public static class ScheduledExecutorProvider extends AbstractExecutorProvider
			implements Provider<ScheduledExecutorService> {

		@Override
		public ScheduledExecutorService get() {
			final IntegerRange poolSize = getPoolSize();
			final long keepAliveDuration = getKeepAliveDuration();
			final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(
					poolSize.getStart(),
				    getThreadFactory());
			executor.setMaximumPoolSize(poolSize.getEnd());
			executor.setKeepAliveTime(keepAliveDuration, TimeUnit.SECONDS);
			executor.allowCoreThreadTimeOut(keepAliveDuration > 0);
			configureRejectedTasksTracking(executor);
			return executor;
		}

	}

}
