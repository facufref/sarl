/*
 * $Id$
 *
 * File is automatically generated by the Xtext language generator.
 * Do not change it.
 *
 * SARL is an general-purpose agent programming language.
 * More details on http://www.sarl.io
 *
 * Copyright 2014-2016 the original authors and authors.
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
package io.sarl.lang.codebuilder.appenders;

import io.sarl.lang.codebuilder.builders.IBehaviorUnitBuilder;
import io.sarl.lang.codebuilder.builders.IBlockExpressionBuilder;
import io.sarl.lang.codebuilder.builders.IExpressionBuilder;
import io.sarl.lang.sarl.SarlBehaviorUnit;
import java.io.IOException;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtend.core.xtend.XtendTypeDeclaration;
import org.eclipse.xtext.xbase.compiler.ISourceAppender;
import org.eclipse.xtext.xbase.lib.Pure;

/** Source appender of a Sarl BehaviorUnit.
 */
@SuppressWarnings("all")
public class BehaviorUnitSourceAppender extends AbstractSourceAppender implements IBehaviorUnitBuilder {

	private final IBehaviorUnitBuilder builder;

	public BehaviorUnitSourceAppender(IBehaviorUnitBuilder builder) {
		this.builder = builder;
	}

	public void build(ISourceAppender appender) throws IOException {
		build(this.builder.getSarlBehaviorUnit(), appender);
	}

	/** Initialize the Ecore element.
	 * @param container - the container of the BehaviorUnit.
	 * @param name - the type of the BehaviorUnit.
	 */
	public void eInit(XtendTypeDeclaration container, String name) {
		this.builder.eInit(container, name);
	}

	/** Replies the generated element.
	 */
	@Pure
	public SarlBehaviorUnit getSarlBehaviorUnit() {
		return this.builder.getSarlBehaviorUnit();
	}

	/** Replies the resource.
	 */
	@Pure
	public Resource eResource() {
		return getSarlBehaviorUnit().eResource();
	}

	/** Change the documentation of the element.
	 *
	 * <p>The documentation will be displayed just before the element.
	 *
	 * @param doc the documentation.
	 */
	public void setDocumentation(String doc) {
		this.builder.setDocumentation(doc);
	}

	/** Change the guard.
	 * @param value - the value of the guard. It may be <code>null</code>.
	 */
	@Pure
	public IExpressionBuilder getGuard() {
		return this.builder.getGuard();
	}

	/** Create the block of code.
	 * @return the block builder.
	 */
	public IBlockExpressionBuilder getExpression() {
		return this.builder.getExpression();
	}

	/** Add an annotation.
	 * @param type the qualified name of the annotation
	 */
	public void addAnnotation(String type) {
		this.builder.addAnnotation(type);
	}

}
