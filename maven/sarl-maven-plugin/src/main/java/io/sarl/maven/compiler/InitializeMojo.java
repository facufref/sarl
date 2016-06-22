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

package io.sarl.maven.compiler;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.arakhne.afc.vmutil.locale.Locale;

/** Initialization mojo for compiling SARL.
 *
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@Mojo(name = "initialize", defaultPhase = LifecyclePhase.INITIALIZE, requiresDependencyResolution = ResolutionScope.COMPILE)
public class InitializeMojo extends AbstractSarlMojo {

	@Override
	protected void executeMojo() throws MojoExecutionException, MojoFailureException {
		for (File f : new File[] {getInput(), getOutput()}) {
			String absPath = f.getAbsolutePath();
			getLog().debug(Locale.getString(InitializeMojo.class, "ADD_SOURCE_FOLDERS", absPath)); //$NON-NLS-1$
			this.mavenHelper.getSession().getCurrentProject().addCompileSourceRoot(absPath);
		}
		for (File f : new File[] {getTestInput(), getTestOutput()}) {
			String absPath = f.getAbsolutePath();
			getLog().debug(Locale.getString(InitializeMojo.class, "ADD_TEST_SOURCE_FOLDERS", absPath)); //$NON-NLS-1$
			this.mavenHelper.getSession().getCurrentProject().addTestCompileSourceRoot(absPath);
		}
	}

}
