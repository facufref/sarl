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

package io.sarl.lang.scoping.batch;

import java.util.List;

import com.google.inject.Singleton;
import org.eclipse.xtext.xbase.scoping.batch.ImplicitlyImportedFeatures;


/** Provider of the implicitly imported features in the SARL language.
 *
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@Singleton
public class SARLImplicitlyImportedFeatures extends ImplicitlyImportedFeatures {

	/** Construct the provider.
	 */
	public SARLImplicitlyImportedFeatures() {
		super();
	}

	@Override
	protected List<Class<?>> getExtensionClasses() {
		List<Class<?>> xtextList = super.getExtensionClasses();
		// Insert at the beginning for ensuring the SARL extension is selected before any Xtext extension.
		xtextList.add(0, SARLMapExtensions.class);
		xtextList.add(0, SARLTimeExtensions.class);
		return xtextList;
	}

}

