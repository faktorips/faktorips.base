/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model;

import java.util.List;
import java.util.Map;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.model.builder.IDependencyGraphPersistenceManager;
import org.faktorips.devtools.model.extproperties.IExtensionPropertyDefinition;
import org.faktorips.devtools.model.fl.IFlIdentifierFilterExtension;
import org.faktorips.devtools.model.fl.IdentifierFilter;
import org.faktorips.devtools.model.internal.DefaultVersionProvider;
import org.faktorips.devtools.model.internal.IpsObjectPathContainerFactory;
import org.faktorips.devtools.model.internal.productcmpt.DeepCopyOperation;
import org.faktorips.devtools.model.internal.productcmpt.IDeepCopyOperationFixup;
import org.faktorips.devtools.model.ipsproject.IIpsLoggingFrameworkConnector;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathContainerType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.plugin.IIpsWorkspaceInteractions;
import org.faktorips.devtools.model.plugin.IpsModelExtensionsViaEclipsePlugins;
import org.faktorips.devtools.model.preferences.IIpsModelPreferences;
import org.faktorips.devtools.model.util.IpsProjectConfigurators;
import org.faktorips.devtools.model.util.SortorderSet;
import org.faktorips.devtools.model.versionmanager.IIpsFeatureVersionManager;
import org.faktorips.devtools.model.versionmanager.IIpsProjectMigrationOperationFactory;
import org.osgi.framework.Version;

/**
 * Extensions to the Faktor-IPS model.
 */
public interface IIpsModelExtensions {

    static IIpsModelExtensions get() {
        return IpsModelExtensionsViaEclipsePlugins.get();
    }

    /**
     * Returns the {@link IFunctionResolverFactory IFunctionResolverFactories} that are registered
     * at the according extension-point.
     */
    SortorderSet<IFunctionResolverFactory<JavaCodeFragment>> getFlFunctionResolverFactories();

    /**
     * Returns all installed IPS feature version managers.
     */
    IIpsFeatureVersionManager[] getIpsFeatureVersionManagers();

    /**
     * Returns the manager for the feature with the given id or <code>null</code> if no manager was
     * found.
     * 
     * @param featureId The id of the feature the manager has to be returned.
     */
    default IIpsFeatureVersionManager getIpsFeatureVersionManager(String featureId) {
        for (IIpsFeatureVersionManager manager : getIpsFeatureVersionManagers()) {
            if (manager.getFeatureId().equals(featureId)) {
                return manager;
            }
        }
        return null;
    }

    /**
     * Returns the <code>IIpsLoggingFrameworkConnector</code> that are registered at the according
     * extension-point.
     */
    IIpsLoggingFrameworkConnector[] getIpsLoggingFrameworkConnectors();

    /**
     * Returns the <code>IIpsLoggingFrameworkConnector</code> for the provided id. If no
     * <code>IIpsLoggingFrameworkConnector</code> with the provided id is found <code>null</code>
     * will be returned.
     */
    default IIpsLoggingFrameworkConnector getIpsLoggingFrameworkConnector(String id) {
        IIpsLoggingFrameworkConnector[] builders = IIpsModelExtensions.get().getIpsLoggingFrameworkConnectors();
        for (IIpsLoggingFrameworkConnector builder : builders) {
            if (id.equals(builder.getId())) {
                return builder;
            }
        }
        return null;
    }

    /**
     * Get all registered migration operations for a specified contributor name. The contributor
     * name is the symbolic name of the bundle that provides the registered migration operations.
     * 
     * @param contributorName The name of the contributor which provides the requested migration
     *            operations
     * 
     * @return A map containing all registered migration operations. The key of the map is the
     *         target version of the operation
     */
    Map<Version, IIpsProjectMigrationOperationFactory> getRegisteredMigrationOperations(String contributorName);

    /**
     * Returns user preferences.
     */
    IIpsModelPreferences getModelPreferences();

    /**
     * Returns ways to interact with an IDE's workspace.
     */
    IIpsWorkspaceInteractions getWorkspaceInteractions();

    /**
     * Returns the available configurators for adding the IPS nature to a project.
     *
     * @see IpsProjectConfigurators#applicableTo(org.eclipse.jdt.core.IJavaProject)
     */
    List<IIpsProjectConfigurator> getIpsProjectConfigurators();

    /**
     * Returns the {@link IdentifierFilter} who calls the extension points
     * {@link IFlIdentifierFilterExtension}
     */
    IdentifierFilter getIdentifierFilter();

    /**
     * Returns the persistence manager for the dependency graphs.
     */
    IDependencyGraphPersistenceManager getDependencyGraphPersistenceManager();

    /**
     * Returns a factory that can create {@link IClassLoaderProvider IClassLoaderProviders} for
     * {@link IIpsProject IIpsProjects}.
     */
    IClassLoaderProviderFactory getClassLoaderProviderFactory();

    /**
     * Returns fix-ups that should be applied after a {@link DeepCopyOperation}.
     */
    List<IDeepCopyOperationFixup> getDeepCopyOperationFixups();

    /**
     * Returns a map of {@link IExtensionPropertyDefinition extension property definitions} by the
     * classes they extend.
     */
    Map<Class<?>, List<IExtensionPropertyDefinition>> getExtensionPropertyDefinitions();

    /**
     * Returns a map of {@link IVersionProviderFactory version provider factories} by their IDs.
     */
    Map<String, IVersionProviderFactory> getVersionProviderFactories();

    /**
     * Returns the {@link IVersionProvider} configured in the given {@link IIpsProject project}'s
     * {@link IIpsProjectProperties#getVersionProviderId() versionProviderID property} or a
     * {@link DefaultVersionProvider} if none is configured.
     */
    default IVersionProvider<?> getVersionProvider(IIpsProject ipsProject) {
        String versionProviderId = ipsProject.getReadOnlyProperties().getVersionProviderId();
        return getVersionProviderFactories()
                .getOrDefault(versionProviderId, DefaultVersionProvider::new)
                .createVersionProvider(ipsProject);
    }

    /**
     * Returns a all registered {@link IIpsObjectPathContainerType IIpsObjectPathContainerTypes} to
     * be used by the {@link IpsObjectPathContainerFactory}.
     */
    List<IIpsObjectPathContainerType> getIpsObjectPathContainerTypes();

}