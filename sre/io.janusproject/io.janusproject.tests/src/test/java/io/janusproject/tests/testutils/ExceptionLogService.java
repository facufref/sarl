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
package io.janusproject.tests.testutils;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.google.common.util.concurrent.AbstractService;
import com.google.common.util.concurrent.Service;

import io.janusproject.services.logging.LogService;

/**
 * This class provides an implementation of the {@link LogService} that throws an exception when logging an error.
 *
 * <p>This service is thread-safe.
 *
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class ExceptionLogService extends AbstractService implements LogService {

	private final List<Object> results;
	private final Logger logger;

	/**
	 * @param results the results of the run.
	 */
	public ExceptionLogService(List<Object> results) {
		this.results = results;
		this.logger = Logger.getLogger(ExceptionLogService.class.getName());
	}

	/** Replies the mutex to be used for being synchronized on the results.
	 *
	 * @return the mutex.
	 */
	protected Object getResultMutex() {
		return this;
	}

	@Override
	public Class<? extends Service> getServiceType() {
		return LogService.class;
	}

	@Override
	public Collection<Class<? extends Service>> getServiceDependencies() {
		return Collections.emptyList();
	}

	@Override
	public Collection<Class<? extends Service>> getServiceWeakDependencies() {
		return Collections.emptyList();
	}

	@Override
	public void info(String message, Object... params) {
		//
	}

	@Override
	public void fineInfo(String message, Object... params) {
		//
	}

	@Override
	public void finerInfo(String message, Object... params) {
		//
	}

	@Override
	public void debug(String message, Object... params) {
		//
	}

	@Override
	public void warning(String message, Object... params) {
		//
	}

	@Override
	public void warning(Throwable exception) {
		//
	}

	@Override
	public void error(String message, Object... params) {
		final Exception ex = new LoggedException(MessageFormat.format(message, params));
		error(ex);
	}

	@Override
	public void error(Throwable exception) {
		synchronized(getResultMutex()) {
			this.results.add(exception);
		}
		this.logger.log(Level.SEVERE, exception.getMessage(), exception);
	}

	@Override
	public void log(LogRecord record) {
		if (record.getLevel() == Level.SEVERE) {
			final Exception ex = new LoggedException(record.getMessage(), record.getThrown());
			synchronized(getResultMutex()) {
				this.results.add(ex);
			}
			this.logger.log(Level.SEVERE, ex.getMessage(), ex);
		}
	}

	@Override
	public void log(Level level, String message, Object... params) {
		if (level == Level.SEVERE) {
			final Exception ex = new LoggedException(message);
			synchronized(getResultMutex()) {
				this.results.add(ex);
			}
			this.logger.log(Level.SEVERE, ex.getMessage(), ex);
		}
	}

	@Override
	public Logger getLogger() {
		return this.logger;
	}

	@Override
	public void setLogger(Logger logger) {
		//
	}

	@Override
	public void setFilter(Filter filter) {
		//
	}

	@Override
	public Filter getFilter() {
		return null;
	}

	@Override
	public boolean isLoggeable(Level level) {
		return level.intValue() <= Level.SEVERE.intValue();
	}

	@Override
	public Level getLevel() {
		return Level.SEVERE;
	}

	@Override
	public void setLevel(Level level) {
		//
	}

	@Override
	protected void doStart() {
		this.logger.setLevel(Level.SEVERE);
		notifyStarted();
	}

	@Override
	protected void doStop() {
		//this.logger.setLevel(Level.OFF);
		notifyStopped();
	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	public static class LoggedException extends RuntimeException {

		private static final long serialVersionUID = 7747342758657910404L;

		/**
		 * @param message the error message.
		 */
		public LoggedException(String message) {
			super(message);
		}

		/**
		 * @param cause the cause of the error..
		 */
		public LoggedException(Throwable cause) {
			super(cause);
		}

		/**
		 * @param message the error message.
		 * @param cause the cause of the error..
		 */
		public LoggedException(String message, Throwable cause) {
			super(message, cause);
		}

	}

}
