/*
 * Copyright (C) 2014-2017 the original authors or authors.
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
 */
package io.sarl.lang.tests.general.compilation.general;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import io.sarl.lang.SARLVersion;
import io.sarl.lang.sarl.SarlAction;
import io.sarl.lang.sarl.SarlAgent;
import io.sarl.lang.sarl.SarlPackage;
import io.sarl.lang.sarl.SarlScript;
import io.sarl.lang.validation.IssueCodes;
import io.sarl.tests.api.AbstractSarlTest;

import org.eclipse.xtext.serializer.ISerializer;
import org.eclipse.xtext.xbase.XbasePackage;
import org.eclipse.xtext.xbase.annotations.xAnnotations.XAnnotationsPackage;
import org.eclipse.xtext.xbase.testing.CompilationTestHelper;
import org.eclipse.xtext.xtype.XtypePackage;
import org.junit.Test;

import com.google.common.base.Strings;
import com.google.inject.Inject;

/**
 * @author $Author: sgalland$
 * @version $Name$ $Revision$ $Date$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings("all")
public class CloneFunctionTest extends AbstractSarlTest {

	@Inject
	private CompilationTestHelper compiler;

	@Test
	public void noClone_noInheritance_noGeneric() throws Exception {
		String source = multilineString(
				"class C1 implements Cloneable {",
				"}"
				);
		final String expected = multilineString(
				"import io.sarl.lang.annotation.SarlElementType;",
				"import io.sarl.lang.annotation.SarlSpecification;",
				"import io.sarl.lang.annotation.SyntheticMember;",
				"import org.eclipse.xtext.xbase.lib.Pure;",
				"",
				"@SarlSpecification(\"" + SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING + "\")",
				"@SarlElementType(" + SarlPackage.SARL_CLASS + ")",
				"@SuppressWarnings(\"all\")",
				"public class C1 implements Cloneable {",
				"  @Override",
				"  @Pure",
				"  @SyntheticMember",
				"  public C1 clone() {",
				"    try {",
				"      return (C1) super.clone();",
				"    } catch (Throwable exception) {",
				"      throw new Error(exception);",
				"    }",
				"  }",
				"  ",
				"  @SyntheticMember",
				"  public C1() {",
				"    super();",
				"  }",
				"}",
				"");
		this.compiler.assertCompilesTo(source, expected);
	}

	@Test
	public void clone_noInheritance_noGeneric() throws Exception {
		String source = multilineString(
				"class C1 implements Cloneable {",
				"  def clone : C1 { new C1 }",
				"}"
				);
		final String expected = multilineString(
				"import io.sarl.lang.annotation.SarlElementType;",
				"import io.sarl.lang.annotation.SarlSpecification;",
				"import io.sarl.lang.annotation.SyntheticMember;",
				"import org.eclipse.xtext.xbase.lib.Pure;",
				"",
				"@SarlSpecification(\"" + SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING + "\")",
				"@SarlElementType(" + SarlPackage.SARL_CLASS + ")",
				"@SuppressWarnings(\"all\")",
				"public class C1 implements Cloneable {",
				"  @Pure",
				"  public C1 clone() {",
				"    return new C1();",
				"  }",
				"  ",
				"  @SyntheticMember",
				"  public C1() {",
				"    super();",
				"  }",
				"}",
				"");
		this.compiler.assertCompilesTo(source, expected);
	}

	@Test
	public void noClone_inheritance_noGeneric() throws Exception {
		String source = multilineString(
				"class C0 implements Cloneable {",
				"  def clone : C0 { new C0 }",
				"}",
				"class C1 extends C0 {",
				"}"
				);
		final String expected = multilineString(
				"import io.sarl.lang.annotation.SarlElementType;",
				"import io.sarl.lang.annotation.SarlSpecification;",
				"import io.sarl.lang.annotation.SyntheticMember;",
				"import org.eclipse.xtext.xbase.lib.Pure;",
				"",
				"@SarlSpecification(\"" + SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING + "\")",
				"@SarlElementType(" + SarlPackage.SARL_CLASS + ")",
				"@SuppressWarnings(\"all\")",
				"public class C1 extends C0 {",
				"  @Override",
				"  @Pure",
				"  @SyntheticMember",
				"  public C1 clone() {",
				"    try {",
				"      return (C1) super.clone();",
				"    } catch (Throwable exception) {",
				"      throw new Error(exception);",
				"    }",
				"  }",
				"  ",
				"  @SyntheticMember",
				"  public C1() {",
				"    super();",
				"  }",
				"}",
				"");
		this.compiler.compile(source, (it) -> {
			assertEquals(expected, it.getGeneratedCode("C1"));
		});
	}

	@Test
	public void clone_inheritance_noGeneric() throws Exception {
		String source = multilineString(
				"class C0 implements Cloneable {",
				"  def clone : C0 { new C0 }",
				"}",
				"class C1 extends C0 {",
				"  def clone : C1 { new C1 }",
				"}"
				);
		final String expected = multilineString(
				"import io.sarl.lang.annotation.SarlElementType;",
				"import io.sarl.lang.annotation.SarlSpecification;",
				"import io.sarl.lang.annotation.SyntheticMember;",
				"import org.eclipse.xtext.xbase.lib.Pure;",
				"",
				"@SarlSpecification(\"" + SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING + "\")",
				"@SarlElementType(" + SarlPackage.SARL_CLASS + ")",
				"@SuppressWarnings(\"all\")",
				"public class C1 extends C0 {",
				"  @Pure",
				"  public C1 clone() {",
				"    return new C1();",
				"  }",
				"  ",
				"  @SyntheticMember",
				"  public C1() {",
				"    super();",
				"  }",
				"}",
				"");
		this.compiler.compile(source, (it) -> {
			assertEquals(expected, it.getGeneratedCode("C1"));
		});
	}

	@Test
	public void noClone_noInheritance_generic() throws Exception {
		String source = multilineString(
				"class C1<T extends Number> implements Cloneable {",
				"}"
				);
		final String expected = multilineString(
				"import io.sarl.lang.annotation.SarlElementType;",
				"import io.sarl.lang.annotation.SarlSpecification;",
				"import io.sarl.lang.annotation.SyntheticMember;",
				"import org.eclipse.xtext.xbase.lib.Pure;",
				"",
				"@SarlSpecification(\"" + SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING + "\")",
				"@SarlElementType(" + SarlPackage.SARL_CLASS + ")",
				"@SuppressWarnings(\"all\")",
				"public class C1<T extends Number> implements Cloneable {",
				"  @Override",
				"  @Pure",
				"  @SyntheticMember",
				"  public C1<T> clone() {",
				"    try {",
				"      return (C1<T>) super.clone();",
				"    } catch (Throwable exception) {",
				"      throw new Error(exception);",
				"    }",
				"  }",
				"  ",
				"  @SyntheticMember",
				"  public C1() {",
				"    super();",
				"  }",
				"}",
				"");
		this.compiler.assertCompilesTo(source, expected);
	}

	@Test
	public void clone_noInheritance_generic() throws Exception {
		String source = multilineString(
				"class C1<T extends Number> implements Cloneable {",
				"  def clone : C1<T> { new C1 }",
				"}"
				);
		final String expected = multilineString(
				"import io.sarl.lang.annotation.SarlElementType;",
				"import io.sarl.lang.annotation.SarlSpecification;",
				"import io.sarl.lang.annotation.SyntheticMember;",
				"import org.eclipse.xtext.xbase.lib.Pure;",
				"",
				"@SarlSpecification(\"" + SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING + "\")",
				"@SarlElementType(" + SarlPackage.SARL_CLASS + ")",
				"@SuppressWarnings(\"all\")",
				"public class C1<T extends Number> implements Cloneable {",
				"  @Pure",
				"  public C1<T> clone() {",
				"    return new C1<T>();",
				"  }",
				"  ",
				"  @SyntheticMember",
				"  public C1() {",
				"    super();",
				"  }",
				"}",
				"");
		this.compiler.assertCompilesTo(source, expected);
	}

	@Test
	public void noClone_inheritance_generic() throws Exception {
		String source = multilineString(
				"class C0 implements Cloneable {",
				"  def clone : C0 { new C0 }",
				"}",
				"class C1<T extends Number> extends C0 {",
				"}"
				);
		final String expected = multilineString(
				"import io.sarl.lang.annotation.SarlElementType;",
				"import io.sarl.lang.annotation.SarlSpecification;",
				"import io.sarl.lang.annotation.SyntheticMember;",
				"import org.eclipse.xtext.xbase.lib.Pure;",
				"",
				"@SarlSpecification(\"" + SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING + "\")",
				"@SarlElementType(" + SarlPackage.SARL_CLASS + ")",
				"@SuppressWarnings(\"all\")",
				"public class C1<T extends Number> extends C0 {",
				"  @Override",
				"  @Pure",
				"  @SyntheticMember",
				"  public C1<T> clone() {",
				"    try {",
				"      return (C1<T>) super.clone();",
				"    } catch (Throwable exception) {",
				"      throw new Error(exception);",
				"    }",
				"  }",
				"  ",
				"  @SyntheticMember",
				"  public C1() {",
				"    super();",
				"  }",
				"}",
				"");
		this.compiler.compile(source, (it) -> {
			assertEquals(expected, it.getGeneratedCode("C1"));
		});
	}

	@Test
	public void clone_inheritance_generic() throws Exception {
		String source = multilineString(
				"class C0 implements Cloneable {",
				"  def clone : C0 { new C0 }",
				"}",
				"class C1<T extends Number> extends C0 {",
				"  def clone : C1<T> { new C1 }",
				"}"
				);
		final String expected = multilineString(
				"import io.sarl.lang.annotation.SarlElementType;",
				"import io.sarl.lang.annotation.SarlSpecification;",
				"import io.sarl.lang.annotation.SyntheticMember;",
				"import org.eclipse.xtext.xbase.lib.Pure;",
				"",
				"@SarlSpecification(\"" + SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING + "\")",
				"@SarlElementType(" + SarlPackage.SARL_CLASS + ")",
				"@SuppressWarnings(\"all\")",
				"public class C1<T extends Number> extends C0 {",
				"  @Pure",
				"  public C1<T> clone() {",
				"    return new C1<T>();",
				"  }",
				"  ",
				"  @SyntheticMember",
				"  public C1() {",
				"    super();",
				"  }",
				"}",
				"");
		this.compiler.compile(source, (it) -> {
			assertEquals(expected, it.getGeneratedCode("C1"));
		});
	}

	@Test
	public void finalInheritedClone() throws Exception {
		String source = multilineString(
				"class C0 implements Cloneable {",
				"  final def clone : C0 { new C0 }",
				"}",
				"class C1 extends C0 {",
				"}"
				);
		final String expected = multilineString(
				"import io.sarl.lang.annotation.SarlElementType;",
				"import io.sarl.lang.annotation.SarlSpecification;",
				"import io.sarl.lang.annotation.SyntheticMember;",
				"",
				"@SarlSpecification(\"" + SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING + "\")",
				"@SarlElementType(" + SarlPackage.SARL_CLASS + ")",
				"@SuppressWarnings(\"all\")",
				"public class C1 extends C0 {",
				"  @SyntheticMember",
				"  public C1() {",
				"    super();",
				"  }",
				"}",
				"");
		this.compiler.compile(source, (it) -> {
			assertEquals(expected, it.getGeneratedCode("C1"));
		});
	}

}
