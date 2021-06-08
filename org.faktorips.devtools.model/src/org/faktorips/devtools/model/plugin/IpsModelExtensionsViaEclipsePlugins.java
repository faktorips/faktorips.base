/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.plugin;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.model.IClassLoaderProviderFactory;
import org.faktorips.devtools.model.IFunctionResolverFactory;
import org.faktorips.devtools.model.IIpsModelExtensions;
import org.faktorips.devtools.model.IIpsProjectConfigurator;
import org.faktorips.devtools.model.IVersionProviderFactory;
import org.faktorips.devtools.model.builder.DependencyGraphPersistenceManager;
import org.faktorips.devtools.model.extproperties.IExtensionPropertyDefinition;
import org.faktorips.devtools.model.fl.IdentifierFilter;
import org.faktorips.devtools.model.internal.productcmpt.IDeepCopyOperationFixup;
import org.faktorips.devtools.model.ipsproject.IIpsLoggingFrameworkConnector;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathContainerType;
import org.faktorips.devtools.model.plugin.extensions.ClassLoaderProviderFactoryExtension;
import org.faktorips.devtools.model.plugin.extensions.DeepCopyOperationFixupExtensions;
import org.faktorips.devtools.model.plugin.extensions.ExtensionPropertyDefinitionExtensions;
import org.faktorips.devtools.model.plugin.extensions.FeatureVersionManagerExtensions;
import org.faktorips.devtools.model.plugin.extensions.FunctionResolverFactoryExtensions;
import org.faktorips.devtools.model.plugin.extensions.IdentifierFilterExtensions;
import org.faktorips.devtools.model.plugin.extensions.IpsObjectPathContainerTypesExtensions;
import org.faktorips.devtools.model.plugin.extensions.IpsProjectConfigurerExtension;
import org.faktorips.devtools.model.plugin.extensions.IpsWorkspaceInteractionsExtension;
import org.faktorips.devtools.model.plugin.extensions.LoggingFrameworkConnectorExtensions;
import org.faktorips.devtools.model.plugin.extensions.MigrationOperationExtensions;
import org.faktorips.devtools.model.plugin.extensions.ModelPreferencesExtension;
import org.faktorips.devtools.model.plugin.extensions.VersionProviderFactoryExtensions;
import org.faktorips.devtools.model.preferences.IIpsModelPreferences;
import org.faktorips.devtools.model.util.SortorderSet;
import org.faktorips.devtools.model.versionmanager.IIpsFeatureVersionManager;
import org.faktorips.devtools.model.versionmanager.IIpsProjectMigrationOperationFactory;
import org.osgi.framework.Version;

public class IpsModelExtensionsViaEclipsePlugins implements IIpsModelExtensions {

    private static /* final */ IpsModelExtensionsViaEclipsePlugins instance = new IpsModelExtensionsViaEclipsePlugins();

    private final Supplier<IIpsModelPreferences> modelPreferences;

    private final Supplier<IIpsWorkspaceInteractions> ipsWorkspaceInteractions;

    private final Supplier<IClassLoaderProviderFactory> classLoaderProviderFactory;

    private final Supplier<Map<Version, IIpsProjectMigrationOperationFactory>> registeredMigrationOperations;

    private final Supplier<SortorderSet<IFunctionResolverFactory<JavaCodeFragment>>> flFunctionResolvers;

    private final Supplier<List<IIpsLoggingFrameworkConnector>> loggingFrameworkConnectors;

    private final Supplier<List<IIpsFeatureVersionManager>> featureVersionManagers;

    private final Supplier<List<IIpsProjectConfigurator>> ipsProjectConfigurators;

    private final Supplier<IdentifierFilter> identifierFilter;

    private final Supplier<List<IDeepCopyOperationFixup>> deepCopyOperationFixups;

    private final Supplier<Map<Class<?>, List<IExtensionPropertyDefinition>>> extensionPropertyDefinitions;

    private final Supplier<Map<String, IVersionProviderFactory>> versionProviderFactories;

    private final Supplier<List<IIpsObjectPathContainerType>> ipsObjectPathContainerTypes;

    private IpsModelExtensionsViaEclipsePlugins() {
        this(Platform.getExtensionRegistry());
    }

    /**
     * <em><strong>For testing with a custom {@link IExtensionRegistry} only.</strong></em>
     *
     * @see IpsModelExtensionsViaEclipsePlugins#get IpsModelExtensionsViaEclipsePlugins#get for the
     *      singleton instance initialized from the Eclipse {@link Platform}.
     */
    protected IpsModelExtensionsViaEclipsePlugins(IExtensionRegistry extensionRegistry) {
        ExtensionPoints extensionPoints = new ExtensionPoints(extensionRegistry, IpsModelActivator.PLUGIN_ID);
        modelPreferences = new ModelPreferencesExtension(extensionPoints);
        ipsWorkspaceInteractions = new IpsWorkspaceInteractionsExtension(extensionPoints);
        ipsProjectConfigurators = new IpsProjectConfigurerExtension(extensionPoints);
        classLoaderProviderFactory = new ClassLoaderProviderFactoryExtension(extensionPoints);
        registeredMigrationOperations = new MigrationOperationExtensions(extensionPoints);
        flFunctionResolvers = new FunctionResolverFactoryExtensions(extensionPoints);
        loggingFrameworkConnectors = new LoggingFrameworkConnectorExtensions(extensionPoints);
        featureVersionManagers = new FeatureVersionManagerExtensions(extensionPoints);
        identifierFilter = new IdentifierFilterExtensions(extensionPoints);
        deepCopyOperationFixups = new DeepCopyOperationFixupExtensions(extensionPoints);
        extensionPropertyDefinitions = new ExtensionPropertyDefinitionExtensions(extensionPoints);
        versionProviderFactories = new VersionProviderFactoryExtensions(extensionPoints);
        ipsObjectPathContainerTypes = new IpsObjectPathContainerTypesExtensions(extensionPoints);
    }

    /**
     * Returns the singleton instance initialized from the Eclipse {@link Platform}.
     */
    public static IpsModelExtensionsViaEclipsePlugins get() {
        return instance;
    }

    /**
     * <em><strong>For testing with a custom {@link IExtensionRegistry} only.</strong></em>
     * 
     * @param testInstance an IpsModelExtensionsViaEclipsePlugins with test data
     */
    protected static void setInstanceForTest(IpsModelExtensionsViaEclipsePlugins testInstance) {
        instance = testInstance;
    }

    @Override
    public SortorderSet<IFunctionResolverFactory<JavaCodeFragment>> getFlFunctionResolverFactories() {
        return flFunctionResolvers.get();
    }

    @Override
    public IIpsFeatureVersionManager[] getIpsFeatureVersionManagers() {
        return featureVersionManagers.get().toArray(new IIpsFeatureVersionManager[0]);
    }

    @Override
    public IIpsLoggingFrameworkConnector[] getIpsLoggingFrameworkConnectors() {
        return loggingFrameworkConnectors.get().toArray(new IIpsLoggingFrameworkConnector[0]);
    }

    @Override
    public Map<Version, IIpsProjectMigrationOperationFactory> getRegisteredMigrationOperations(String contributorName) {
        // FIXME filter by contributor
        // if (!contributorName.equals(configElement.getContributor().getName())) {
        return registeredMigrationOperations.get();
    }

    @Override
    public IIpsModelPreferences getModelPreferences() {
        return modelPreferences.get();
    }

    @Override
    public IIpsWorkspaceInteractions getWorkspaceInteractions() {
        return ipsWorkspaceInteractions.get();
    }

    @Override
    public IdentifierFilter getIdentifierFilter() {
        return identifierFilter.get();
    }

    @Override
    public DependencyGraphPersistenceManager getDependencyGraphPersistenceManager() {
        return IpsModelActivator.get().getDependencyGraphPersistenceManager();
    }

    @Override
    public IClassLoaderProviderFactory getClassLoaderProviderFactory() {
        return classLoaderProviderFactory.get();
    }

    @Override
    public List<IDeepCopyOperationFixup> getDeepCopyOperationFixups() {
        return deepCopyOperationFixups.get();
    }

    @Override
    public Map<Class<?>, List<IExtensionPropertyDefinition>> getExtensionPropertyDefinitions() {
        return extensionPropertyDefinitions.get();
    }

    @Override
    public Map<String, IVersionProviderFactory> getVersionProviderFactories() {
        return versionProviderFactories.get();
    }

    @Override
    public List<IIpsProjectConfigurator> getIpsProjectConfigurators() {
        return ipsProjectConfigurators.get();
    }

    @Override
    public List<IIpsObjectPathContainerType> getIpsObjectPathContainerTypes() {
        return ipsObjectPathContainerTypes.get();
    }

}
