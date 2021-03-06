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

// THIS FILE IS AUTO-GENERATED. DO NOT CHANGE MANUALLY

package io.sarl.lang.scoping.numbers;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.xtext.xbase.lib.Inline;
import org.eclipse.xtext.xbase.lib.Pure;

/** Provide static functions related to the casting of numbers of type {@code long}.
 *
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 * @since 0.7
 * @see "https://github.com/eclipse/xtext-extras/issues/186"
 */
@SuppressWarnings("all")
public final class PrimitiveLongCastExtensions {

	private PrimitiveLongCastExtensions() {
		//
	}

	// BEGIN GENERATED BLOCK

	/** Convert the given value to {@code AtomicInteger}.
	 *
	 * @param a a number of {@code long} type.
	 * @return the equivalent value to {@code a} of {@code AtomicInteger} type.
	 */
	@Pure
	@Inline(value = "$3.pow(($1)(int)a, ($2)b.intValue())", imported = Math.class)
	public static AtomicInteger toAtomicInteger(long a) {
		return new AtomicInteger((int)a);
	}

	/** Convert the given value to {@code AtomicLong}.
	 *
	 * @param a a number of {@code long} type.
	 * @return the equivalent value to {@code a} of {@code AtomicLong} type.
	 */
	@Pure
	@Inline(value = "$3.pow(($1)(long)a, ($2)b.longValue())", imported = Math.class)
	public static AtomicLong toAtomicLong(long a) {
		return new AtomicLong((long)a);
	}

	// END GENERATED BLOCK

}
