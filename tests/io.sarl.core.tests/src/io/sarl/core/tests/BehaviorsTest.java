/*
 * $Id$
 *
 * SARL is an general-purpose agent programming language.
 * More details on http://www.sarl.io
 *
 * Copyright (C) 2014-2016 the original authors or authors.
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
package io.sarl.core.tests;

import static org.junit.Assert.assertEquals;
import io.sarl.lang.core.Behavior;
import io.sarl.lang.core.Capacity;
import io.sarl.lang.core.Event;
import io.sarl.lang.core.EventListener;

import org.junit.Before;
import org.junit.Test;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class BehaviorsTest extends AbstractSarlCoreTest<Capacity> {

	/**
	 */
	@Before
	public void setUp() {
		loadSARL("io.sarl.core.Behaviors", Capacity.class); //$NON-NLS-1$
	}

	/**
	 */
	@Test
	public void memberCount() {
		assertEquals(4, this.type.getDeclaredMethods().length);
	}

	/**
	 */
	@Test
	public void registerBehavior() {
		assertMethod("registerBehavior", Behavior.class, Behavior.class); //$NON-NLS-1$
	}

	/**
	 */
	@Test
	public void unregisterBehavior() {
		assertMethod("unregisterBehavior", Behavior.class, Behavior.class); //$NON-NLS-1$
	}

	/**
	 */
	@Test
	public void wake() {
		assertMethod("wake", void.class, Event.class); //$NON-NLS-1$
	}

	/**
	 */
	@Test
	public void asEventListener() {
		assertMethod("asEventListener", EventListener.class); //$NON-NLS-1$
	}

}
