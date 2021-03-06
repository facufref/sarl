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

package io.sarl.maven.docs.testing;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import com.google.common.collect.Iterables;
import org.eclipse.jdt.core.Flags;
import org.eclipse.xtext.util.Strings;
import org.eclipse.xtext.xbase.lib.Functions;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.Procedures;
import org.eclipse.xtext.xbase.lib.Pure;

import io.sarl.lang.annotation.DefaultValue;
import io.sarl.lang.annotation.SarlSourceCode;
import io.sarl.lang.annotation.SyntheticMember;
import io.sarl.lang.util.Utils;

/** Functions that are based on Java reflection, for building the documentation.
 *
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 * @since 0.7
 */
public final class ReflectExtensions {

	private ReflectExtensions() {
		//
	}
	
	private static boolean isDeprecated(Method method) {
		return Flags.isDeprecated(method.getModifiers()) || method.getAnnotation(Deprecated.class) != null;
	}

	/** Extract the public methods from the given types.
	 *
	 * @param type the first type to parse.
	 * @param otherTypes the other types to parse.
	 * @return the code.
	 */
	@Pure
	public static String getPublicMethods(Class<?> type, Class<?>... otherTypes) {
		final StringBuilder it = new StringBuilder();
		appendPublicMethods(it, false, IterableExtensions.flatten(
				Arrays.asList(
						Collections.singletonList(type),
						Arrays.asList(otherTypes))));
		return it.toString();
	}

	/** Extract the public methods from the given types.
	 *
	 * @param it the output.
	 * @param indent indicates if the code should be indented.
	 * @param types the types to parse.
	 */
	public static void appendPublicMethods(StringBuilder it, boolean indent, Class<?>... types) {
		appendPublicMethods(it, indent, Arrays.asList(types));
	}

	/** Extract the public methods from the given types.
	 *
	 * @param it the output.
	 * @param indent indicates if the code should be indented.
	 * @param types the types to parse.
	 */
	public static void appendPublicMethods(StringBuilder it, boolean indent, Iterable<? extends Class<?>> types) {
		final List<String> lines = new LinkedList<>();
		for (final Class<?> type : types) {
			for (final Method method : type.getDeclaredMethods()) {
				if (Flags.isPublic(method.getModifiers()) && !Utils.isHiddenMember(method.getName())
						&& !isDeprecated(method) && !method.isSynthetic()
						&& method.getAnnotation(SyntheticMember.class) == null) {
					final StringBuilder line = new StringBuilder();
					if (indent) {
						line.append("\t"); //$NON-NLS-1$
					}
					line.append("def ").append(method.getName()); //$NON-NLS-1$
					if (method.getParameterCount() > 0) {
						line.append("("); //$NON-NLS-1$
						boolean first = true;
						int i = 1;
						for (final Parameter param : method.getParameters()) {
							if (first) {
								first = false;
							} else {
								line.append(", "); //$NON-NLS-1$
							}
							//it.append(param.getName());
							//it.append(" : "); //$NON-NLS-1$
							toType(line, param.getParameterizedType(), method.isVarArgs() && i == method.getParameterCount());
							final String defaultValue = extractDefaultValueString(param);
							if (!Strings.isEmpty(defaultValue)) {
								line.append(" = "); //$NON-NLS-1$
								line.append(defaultValue);
							}
						}
						line.append(")"); //$NON-NLS-1$
						++i;
					}
					if (method.getGenericReturnType() != null && !Objects.equals(method.getGenericReturnType(), Void.class)
							&& !Objects.equals(method.getGenericReturnType(), void.class)) {
						line.append(" : "); //$NON-NLS-1$
						toType(line, method.getGenericReturnType(), false);
					}
					line.append("\n"); //$NON-NLS-1$
					lines.add(line.toString());
				}
			}
		}
		lines.sort(null);
		for (final String line : lines) {
			it.append(line);
		}
	}

	private static String extractDefaultValueString(Parameter parameter) {
		final DefaultValue defaultValueAnnotation = parameter.getAnnotation(DefaultValue.class);
		if (defaultValueAnnotation == null) {
			return null;
		}
		final String fieldId = defaultValueAnnotation.value();
		if (!Strings.isEmpty(fieldId)) {
			final Class<?> container = parameter.getDeclaringExecutable().getDeclaringClass();
			if (container != null) {
				final int index = fieldId.indexOf('#');
				Class<?> target;
				final String fieldName;
				if (index > 0) {
					try {
						final Class<?> type = Class.forName(fieldId.substring(0, index), true, container.getClassLoader());
						target = type;
					} catch (Throwable exception) {
						target = container;
					}
					fieldName = Utils.createNameForHiddenDefaultValueAttribute(fieldId.substring(index + 1));
				} else {
					target = container;
					fieldName = Utils.createNameForHiddenDefaultValueAttribute(fieldId);
				}

				final Field field = Iterables.find(Arrays.asList(target.getDeclaredFields()),
						(it) -> Strings.equal(it.getName(), fieldName),
						null);
				if (field != null) {
					final SarlSourceCode sourceCodeAnnotation = parameter.getAnnotation(SarlSourceCode.class);
					if (sourceCodeAnnotation != null) {
						final String value = sourceCodeAnnotation.value();
						if (!Strings.isEmpty(fieldId)) {
							return value;
						}
					}
				}
			}
		}
		return null;
	}

	/** Extract the type name.
	 *
	 * @param it the output.
	 * @param otype the type to parse.
	 * @param isVarArg indicates if the type is used within a variadic parameter.
	 */
	@SuppressWarnings({"checkstyle:cyclomaticcomplexity", "checkstyle:npathcomplexity"})
	public static void toType(StringBuilder it, Type otype, boolean isVarArg) {
		final Type type;
		if (otype instanceof Class<?>) {
			type = isVarArg ? ((Class<?>) otype).getComponentType() : otype;
		} else {
			type = otype;
		}
		if (type instanceof Class<?>) {
			it.append(((Class<?>) type).getSimpleName());
		} else if (type instanceof ParameterizedType) {
			final ParameterizedType paramType = (ParameterizedType) type;
			final Type ownerType = paramType.getOwnerType();
			final boolean isForFunction = ownerType != null && Functions.class.getName().equals(ownerType.getTypeName());
			final boolean isForProcedure = ownerType != null && Procedures.class.getName().equals(ownerType.getTypeName());
			if (!isForFunction && !isForProcedure) {
				it.append(((Class<?>) paramType.getRawType()).getSimpleName());
				if (paramType.getActualTypeArguments().length > 0) {
					it.append("<"); //$NON-NLS-1$
					boolean first = true;
					for (final Type subtype : paramType.getActualTypeArguments()) {
						if (first) {
							first = false;
						} else {
							it.append(", "); //$NON-NLS-1$
						}
						final StringBuilder it2 = new StringBuilder();
						toType(it2, subtype, false);
						it.append(it2);
					}
					it.append(">"); //$NON-NLS-1$
				}
			} else {
				int nb = paramType.getActualTypeArguments().length;
				if (isForFunction) {
					--nb;
				}
				it.append("("); //$NON-NLS-1$
				for (int i = 0; i < nb; ++i) {
					final Type subtype = paramType.getActualTypeArguments()[i];
					if (i > 0) {
						it.append(", "); //$NON-NLS-1$
					}
					toType(it, subtype, false);
				}
				it.append(") => "); //$NON-NLS-1$
				if (isForFunction) {
					toType(it, paramType.getActualTypeArguments()[nb], false);
				} else {
					it.append("void"); //$NON-NLS-1$
				}
			}
		} else if (type instanceof WildcardType) {
			final Type[] types = ((WildcardType) type).getUpperBounds();
			toType(it, types[0], false);
		} else if (type instanceof GenericArrayType) {
			toType(it, ((GenericArrayType) type).getGenericComponentType(), false);
			it.append("[]"); //$NON-NLS-1$
		} else {
			it.append(Object.class.getSimpleName());
		}
		if (isVarArg) {
			it.append("*"); //$NON-NLS-1$
		}
	}

}
