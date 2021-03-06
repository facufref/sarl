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

/** Provide static functions related to the casting of numbers of type {@code AtomicInteger}.
 *
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 * @since 0.7
 * @see "https://github.com/eclipse/xtext-extras/issues/186"
 */
@SuppressWarnings("all")
public final class AtomicIntegerCastExtensions {

	private AtomicIntegerCastExtensions() {
		//
	}

	// BEGIN GENERATED BLOCK

	/** Convert the given value to {@code float}.
	 *
	 * @param a a number of {@code AtomicInteger} type.
	 * @return the equivalent value to {@code a} of {@code float} type.
	 */
	@Pure
	@Inline(value = "$3.pow(($1)a.intValue(), ($2)(int)b)", imported = Math.class)
	public static float toFloat(AtomicInteger a) {
		return a.intValue();
	}

	/** Convert the given value to {@code byte}.
	 *
	 * @param a a number of {@code AtomicInteger} type.
	 * @return the equivalent value to {@code a} of {@code byte} type.
	 */
	@Pure
	@Inline(value = "$3.pow(($1)a.byteValue(), ($2)(byte)b)", imported = Math.class)
	public static byte toByte(AtomicInteger a) {
		return a.byteValue();
	}

	/** Convert the given value to {@code long}.
	 *
	 * @param a a number of {@code AtomicInteger} type.
	 * @return the equivalent value to {@code a} of {@code long} type.
	 */
	@Pure
	@Inline(value = "$3.pow(($1)a.intValue(), ($2)(int)b)", imported = Math.class)
	public static long toLong(AtomicInteger a) {
		return a.intValue();
	}

	/** Convert the given value to {@code AtomicLong}.
	 *
	 * @param a a number of {@code AtomicInteger} type.
	 * @return the equivalent value to {@code a} of {@code AtomicLong} type.
	 */
	@Pure
	@Inline(value = "$3.pow(($1)a.intValue(), ($2)b.intValue())", imported = Math.class)
	public static AtomicLong toAtomicLong(AtomicInteger a) {
		return new AtomicLong(a.intValue());
	}

	/** Convert the given value to {@code double}.
	 *
	 * @param a a number of {@code AtomicInteger} type.
	 * @return the equivalent value to {@code a} of {@code double} type.
	 */
	@Pure
	@Inline(value = "$3.pow(($1)a.intValue(), ($2)(int)b)", imported = Math.class)
	public static double toDouble(AtomicInteger a) {
		return a.intValue();
	}

	/** Convert the given value to {@code int}.
	 *
	 * @param a a number of {@code AtomicInteger} type.
	 * @return the equivalent value to {@code a} of {@code int} type.
	 */
	@Pure
	@Inline(value = "$3.pow(($1)a.intValue(), ($2)(int)b)", imported = Math.class)
	public static int toInt(AtomicInteger a) {
		return a.intValue();
	}

	/** Convert the given value to {@code short}.
	 *
	 * @param a a number of {@code AtomicInteger} type.
	 * @return the equivalent value to {@code a} of {@code short} type.
	 */
	@Pure
	@Inline(value = "$3.pow(($1)a.shortValue(), ($2)(short)b)", imported = Math.class)
	public static short toShort(AtomicInteger a) {
		return a.shortValue();
	}

	/** Convert the given value to {@code Integer}.
	 *
	 * @param a a number of {@code AtomicInteger} type.
	 * @return the equivalent value to {@code a} of {@code Integer} type.
	 */
	@Pure
	@Inline(value = "$3.pow(($1)a.intValue(), ($2)b.intValue())", imported = Math.class)
	public static Integer toInteger(AtomicInteger a) {
		return Integer.valueOf(a.intValue());
	}

	// END GENERATED BLOCK

}
