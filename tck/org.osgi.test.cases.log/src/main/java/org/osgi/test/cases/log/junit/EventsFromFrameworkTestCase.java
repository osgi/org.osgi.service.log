/*******************************************************************************
 * Copyright (c) Contributors to the Eclipse Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0 
 *******************************************************************************/
package org.osgi.test.cases.log.junit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.IOException;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.startlevel.FrameworkStartLevel;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogLevel;
import org.osgi.service.log.LogService;
import org.osgi.test.common.wiring.FrameworkWirings;

public class EventsFromFrameworkTestCase extends AbstractLogTestCase {
	private static final String	EVENTS_FRAMEWORK	= "Events.Framework";
	private static final String	EVENTS_BUNDLE		= "Events.Bundle";
	private static final String	EVENTS_SERVICE		= "Events.Service";

	static class EventLogListener extends LogReader {
		private final String			loggerName;

		public EventLogListener(String loggerName) {
			this.loggerName = loggerName;
		}

		@Override
		public void logged(LogEntry entry) {
			if (loggerName.equals(entry.getLoggerName())
					|| entry.getLoggerName().startsWith(loggerName + ".")) {
				synchronized (log) {
					log.add(entry);
					log.notifyAll();
				}
			}
		}

		public LogEntry getEntry(int timeout) {
			synchronized (log) {
				if (log.size() == 0) {
					try {
						log.wait(timeout);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
				LogEntry entry = log.size() == 0 ? null : log.remove(0);
				return entry;
			}
		}

		public void waitForEntry(long timeToWait, String message) {
			synchronized (log) {
				try {
					long startTime = System.currentTimeMillis();
					while (log.size() > 0 && !message.equals(log.get(log.size() - 1).getMessage())
							&& timeToWait > 0) {
						log.wait(timeToWait);
						timeToWait = timeToWait
								- (System.currentTimeMillis() - startTime);
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}

				if (log.size() > 0 && !message.equals(log.get(log.size() - 1).getMessage())) {
					fail(message + " not found in logs");
				}
			}
		}

		public List<LogEntry> getLogs() {
			return log;
		}
	};

	private EventLogListener eventLogListener = null;



	@AfterEach
	public void tearDown() throws Exception {
		if (eventLogListener != null) {
			logReaderService.removeLogListener(eventLogListener);
		}
		super.tearDown();
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testFrameworkEventsLogged()
			throws BundleException, IOException {
		// Only reliable events to test are STARTLEVEL_CHANGED and
		// PACKAGES_REFRESHED
		tb1 = installer.installBundle("tb1.jar", false);
		tb1.start();

		eventLogListener = new EventLogListener(EVENTS_FRAMEWORK);
		logReaderService.addLogListener(eventLogListener);
		FrameworkWirings.refreshBundles(context, tb1);

		Bundle systemBundle = systemBundle();
		String frameworkEventsLoggerName = EVENTS_FRAMEWORK + "."
				+ systemBundle.getSymbolicName();

		LogEntry refreshLogEntry = eventLogListener.getEntry(10000);
		assertThat(refreshLogEntry).as("No entry for refresh.").isNotNull();
		assertEventLog(refreshLogEntry, frameworkEventsLoggerName,
				"FrameworkEvent PACKAGES REFRESHED", systemBundle,
				LogService.LOG_INFO, LogLevel.INFO, null);

		FrameworkStartLevel fwkStartLevel = systemBundle
				.adapt(FrameworkStartLevel.class);

		fwkStartLevel.setStartLevel(fwkStartLevel.getStartLevel() + 1);
		LogEntry startLevelLog1 = eventLogListener.getEntry(10000);

		fwkStartLevel.setStartLevel(fwkStartLevel.getStartLevel() - 1);
		LogEntry startLevelLog2 = eventLogListener.getEntry(10000);

		assertThat(startLevelLog1).as("No entry for start level change 1.").isNotNull();
		assertEventLog(startLevelLog1, frameworkEventsLoggerName,
				"FrameworkEvent STARTLEVEL CHANGED", systemBundle,
				LogService.LOG_INFO, LogLevel.INFO, null);

		assertThat(startLevelLog2).as("No entry for start level change 2.").isNotNull();
		assertEventLog(startLevelLog2, frameworkEventsLoggerName,
				"FrameworkEvent STARTLEVEL CHANGED", systemBundle,
				LogService.LOG_INFO, LogLevel.INFO, null);
	}

	@Test
	public void testBundleEventsLogged() throws BundleException, IOException {
		eventLogListener = new EventLogListener(EVENTS_BUNDLE);
		logReaderService.addLogListener(eventLogListener);

		tb1 = installer.installBundle("tb1.jar", false);
		tb1.start();
		tb1.stop();
		tb1.update(context.getBundle().getEntry("tb1.jar").openStream());
		tb1.start();
		tb1.stop();
		tb1.uninstall();

		List<String> expectedEventTypes = Arrays.asList( //
				"INSTALLED", //
				"RESOLVED", //
				"STARTING", //
				"STARTED", //
				"STOPPING", //
				"STOPPED", //
				"UNRESOLVED", //
				"UPDATED", //
				"RESOLVED", //
				"STARTING", //
				"STARTED", //
				"STOPPING", //
				"STOPPED", //
				"UNRESOLVED", //
				"UNINSTALLED");

		eventLogListener.waitForEntry(10000, "BundleEvent UNINSTALLED");
		List<LogEntry> actualLogs = eventLogListener.getLogs();

		int logsCount = actualLogs.size();
		if (logsCount == expectedEventTypes.size()) {
			assertBundleEventLog(actualLogs, expectedEventTypes,
					tb1.getSymbolicName());
		} else {
			fail("Expected bundle events are not logged: " + actualLogs);
		}
	}

	@Test
	public void testServiceEventsLogged() {
		eventLogListener = new EventLogListener(EVENTS_SERVICE);
		logReaderService.addLogListener(eventLogListener);

		ServiceRegistration<String> testReg = context
				.registerService(String.class, "TestService", null);
		ServiceReference<String> testRef = testReg.getReference();
		Dictionary<String,String> props = new Hashtable<>();
		props.put("test", "value");
		testReg.setProperties(props);
		testReg.unregister();

		assertServiceEventLog("REGISTERED", LogLevel.INFO, testRef);
		assertServiceEventLog("MODIFIED", LogLevel.DEBUG, testRef);
		assertServiceEventLog("UNREGISTERING", LogLevel.INFO, testRef);
	}

	private void assertServiceEventLog(String eventType, LogLevel logLevel,
			ServiceReference<String> ref) {
		LogEntry entry = eventLogListener.getEntry(10000);
		assertEventLog(entry,
				EVENTS_SERVICE + "."
						+ context.getBundle().getSymbolicName(),
				"ServiceEvent " + eventType,
				context.getBundle(), logLevel.ordinal(), logLevel,
				ref);
	}

	@SuppressWarnings("deprecation")
	private void assertBundleEventLog(List<LogEntry> actualLogs,
			List<String> expectedEventTypes, String bsn) {
		for (int i = 0; i < actualLogs.size(); i++) {
			assertEventLog(actualLogs.get(i), EVENTS_BUNDLE + "." + bsn,
					"BundleEvent " + expectedEventTypes.get(i), tb1,
					LogService.LOG_INFO, LogLevel.INFO, null);
		}
	}

	@SuppressWarnings("deprecation")
	private void assertEventLog(LogEntry logEntry, String loggerName,
			String message, Bundle bundle, int level, LogLevel logLevel,
			ServiceReference< ? > reference) {
		assertThat(loggerName).as("Wrong logger name.").isEqualTo(logEntry.getLoggerName());
		assertThat(logEntry.getMessage()).as("Wrong message.").isEqualTo(message);
		assertThat(logEntry.getBundle()).as("Wrong bundle.").isEqualTo(bundle);
		assertThat(logEntry.getLevel()).as("Wrong level.").isEqualTo(level);
		assertThat(logEntry.getLogLevel()).as("Wrong log level.").isEqualTo(logLevel);
		assertThat(logEntry.getServiceReference()).as("Wrong reference.").isEqualTo(reference);
	}

	Bundle systemBundle() {
		return context.getBundle(Constants.SYSTEM_BUNDLE_LOCATION);
	}
}
