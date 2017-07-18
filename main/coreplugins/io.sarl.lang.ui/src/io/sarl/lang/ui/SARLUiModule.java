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

package io.sarl.lang.ui;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.name.Names;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.xtend.core.compiler.XtendGenerator;
import org.eclipse.xtext.builder.preferences.BuilderConfigurationBlock;
import org.eclipse.xtext.generator.IGenerator;
import org.eclipse.xtext.generator.IGenerator2;
import org.eclipse.xtext.generator.IOutputConfigurationProvider;
import org.eclipse.xtext.service.SingletonBinding;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.editor.autoedit.AbstractEditStrategy;
import org.eclipse.xtext.validation.IssueSeveritiesProvider;

import io.sarl.lang.bugfixes.pending.bug621.Bug621SARLExtraLanguageValidator;
import io.sarl.lang.generator.extra.ExtraLanguageFeatureNameConverter.FeatureNameConverterRuleReader;
import io.sarl.lang.generator.extra.ExtraLanguageSupportGenerator;
import io.sarl.lang.generator.extra.ExtraLanguageTypeConverter.TypeConverterRuleReader;
import io.sarl.lang.generator.extra.IExtraLanguageGeneratorProvider;
import io.sarl.lang.ui.bugfixes.pending.xtexteclipse282.Issue282BuilderConfigurationBlock;
import io.sarl.lang.ui.generator.extra.ExtensionPointExtraLanguageGeneratorProvider;
import io.sarl.lang.ui.generator.extra.ExtensionPointExtraLanguageOutputConfigurationProvider;
import io.sarl.lang.ui.generator.extra.ExtensionPointExtraLanguageValidatorProvider;
import io.sarl.lang.ui.generator.extra.preferences.PreferenceBasedFeatureNameConverterRuleReader;
import io.sarl.lang.ui.generator.extra.preferences.PreferenceBasedTypeConverterRuleReader;
import io.sarl.lang.ui.validation.UIConfigurableIssueSeveritiesProvider;
import io.sarl.lang.validation.IConfigurableIssueSeveritiesProvider;
import io.sarl.lang.validation.SARLValidator;
import io.sarl.lang.validation.extra.IExtraLanguageValidatorProvider;

/**
 * Use this class to register components to be used within the IDE.
 *
 * <p>DOT NOT ADD BINDINGS IN THIS CLASS. PREFER TO UPDATE THE MWE2 SCRIPT.
 *
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings({"static-method", "javadoc", "checkstyle:javadocmethod"})
public class SARLUiModule extends AbstractSARLUiModule {

	//TODO private static final String AUTOMATIC_PROPOSAL_CHARACTERS = "."; //$NON-NLS-1$

	/** Provider of {@link UIConfigurableIssueSeveritiesProvider}.
	 *
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 * @since 0.5
	 */
	private static class UIConfigurableIssueSeveritiesProviderProvider implements Provider<UIConfigurableIssueSeveritiesProvider> {

		private UIConfigurableIssueSeveritiesProvider severityProvider;

		@Inject
		private Injector injector;

		UIConfigurableIssueSeveritiesProviderProvider() {
			//
		}

		@Override
		public UIConfigurableIssueSeveritiesProvider get() {
			if (this.severityProvider == null) {
				this.severityProvider = new UIConfigurableIssueSeveritiesProvider();
				this.injector.injectMembers(this.severityProvider);
			}
			return this.severityProvider;
		}

	}

	public SARLUiModule(AbstractUIPlugin plugin) {
		super(plugin);
	}

	@Override
	public void configure(Binder binder) {
		super.configure(binder);
		// Configure the automatic auto-completion on specific characters: "." and ":"
		//TODO binder.bind(String.class).annotatedWith(com.google.inject.name.Names.named(
		//  XtextContentAssistProcessor.COMPLETION_AUTO_ACTIVATION_CHARS))
		//	.toInstance(AUTOMATIC_PROPOSAL_CHARACTERS);

		// Configure a system singleton for issue severities provider
		final UIConfigurableIssueSeveritiesProviderProvider provider = new UIConfigurableIssueSeveritiesProviderProvider();
		binder.bind(UIConfigurableIssueSeveritiesProvider.class).toProvider(provider);
		binder.bind(IssueSeveritiesProvider.class).toProvider(provider);
		binder.bind(IConfigurableIssueSeveritiesProvider.class).toProvider(provider);
		// Configure the extra generator/validator provider.
		binder.bind(IGenerator2.class).annotatedWith(Names.named(ExtraLanguageSupportGenerator.MAIN_GENERATOR_NAME))
				.to(XtendGenerator.class);
	}

	public void configureDebugMode(Binder binder) {
		if (Boolean.getBoolean("io.sarl.lang.debug") //$NON-NLS-1$
				|| Boolean.getBoolean("org.eclipse.xtext.xtend.debug")) { //$NON-NLS-1$
			binder.bindConstant().annotatedWith(Names.named(AbstractEditStrategy.DEBUG)).to(true);
		}
		// matches ID of org.eclipse.ui.contexts extension registered in plugin.xml
		binder.bindConstant().annotatedWith(Names.named(XtextEditor.KEY_BINDING_SCOPE))
		.to("io.sarl.lang.ui.scoping.SARLEditorScope"); //$NON-NLS-1$
	}

	/** TODO: Remove when xtext-eclipse/282 is fixed.
	 * {@inheritDoc}
	 */
	@Override
	public Class<? extends BuilderConfigurationBlock> bindBuilderConfigurationBlock() {
		return Issue282BuilderConfigurationBlock.class;
	}

	public Class<? extends IOutputConfigurationProvider> bindIOutputConfigurationProvider() {
		return ExtensionPointExtraLanguageOutputConfigurationProvider.class;
	}

	public Class<? extends IGenerator> bindIGenerator() {
		return ExtraLanguageSupportGenerator.class;
	}

	@SingletonBinding(eager = true)
	public Class<? extends SARLValidator> bindSARLValidator() {
		return Bug621SARLExtraLanguageValidator.class;
	}

	public Class<? extends IExtraLanguageGeneratorProvider> bindIExtraLanguageGeneratorProvider() {
		return ExtensionPointExtraLanguageGeneratorProvider.class;
	}

	public Class<? extends IExtraLanguageValidatorProvider> bindIExtraLanguageValidatorProvider() {
		return ExtensionPointExtraLanguageValidatorProvider.class;
	}

	public Class<? extends TypeConverterRuleReader> bindTypeConverterRuleReader() {
		return PreferenceBasedTypeConverterRuleReader.class;
	}

	public Class<? extends FeatureNameConverterRuleReader> bindFeatureNameConverterRuleReader() {
		return PreferenceBasedFeatureNameConverterRuleReader.class;
	}

}