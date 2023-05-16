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
import java.util.Optional;
import java.util.function.Supplier;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.abstraction.AVersion;
import org.faktorips.devtools.model.abstractions.WorkspaceAbstractions;
import org.faktorips.devtools.model.builder.IDependencyGraphPersistenceManager;
import org.faktorips.devtools.model.extproperties.IExtensionPropertyDefinition;
import org.faktorips.devtools.model.fl.IFlIdentifierFilterExtension;
import org.faktorips.devtools.model.fl.IdentifierFilter;
import org.faktorips.devtools.model.internal.DefaultVersionProvider;
import org.faktorips.devtools.model.internal.IpsObjectPathContainerFactory;
import org.faktorips.devtools.model.internal.productcmpt.DeepCopyOperation;
import org.faktorips.devtools.model.internal.productcmpt.IDeepCopyOperationFixup;
import org.faktorips.devtools.model.internal.productcmpt.IFormulaCompiler;
import org.faktorips.devtools.model.ipsobject.ICustomValidation;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathContainerType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.plugin.IIpsWorkspaceInteractions;
import org.faktorips.devtools.model.preferences.IIpsModelPreferences;
import org.faktorips.devtools.model.productcmpt.IProductCmptNamingStrategyFactory;
import org.faktorips.devtools.model.productrelease.ReleaseExtension;
import org.faktorips.devtools.model.util.IpsProjectConfigurators;
import org.faktorips.devtools.model.util.SortorderSet;
import org.faktorips.devtools.model.versionmanager.IIpsFeatureVersionManager;
import org.faktorips.devtools.model.versionmanager.IIpsProjectMigrationOperationFactory;

/**
 * Extensions to the Faktor-IPS model.
 */
public interface IIpsModelExtensions {

    static IIpsModelExtensions get() {
        return WorkspaceAbstractions.getIpsModelExtensions();
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
     *
     * @deprecated since 21.12.
     */
    @Deprecated(since = "21.12")
    org.faktorips.devtools.model.ipsproject.IIpsLoggingFrameworkConnector[] getIpsLoggingFrameworkConnectors();

    /**
     * Returns the <code>IIpsLoggingFrameworkConnector</code> for the provided id. If no
     * <code>IIpsLoggingFrameworkConnector</code> with the provided id is found <code>null</code>
     * will be returned.
     *
     * @deprecated since 21.12.
     */
    @Deprecated(since = "21.12")
    default org.faktorips.devtools.model.ipsproject.IIpsLoggingFrameworkConnector getIpsLoggingFrameworkConnector(
            String id) {
        org.faktorips.devtools.model.ipsproject.IIpsLoggingFrameworkConnector[] builders = IIpsModelExtensions.get()
                .getIpsLoggingFrameworkConnectors();
        for (org.faktorips.devtools.model.ipsproject.IIpsLoggingFrameworkConnector builder : builders) {
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
     *             target version of the operation
     */
    Map<AVersion, IIpsProjectMigrationOperationFactory> getRegisteredMigrationOperations(String contributorName);

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
     * @see IpsProjectConfigurators#applicableTo(org.faktorips.devtools.abstraction.AJavaProject)
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
     * Returns the {@link IFormulaCompiler}.
     *
     * @since 23.6
     */
    IFormulaCompiler getFormulaCompiler();

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
     * Returns the actions that should be executed before saving an {@link IIpsObject} of the given
     * {@link IpsObjectType}.
     *
     * @since 21.12
     */
    List<IPreSaveProcessor> getPreSaveProcessors(IpsObjectType ipsObjectType);

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
     * Returns all registered {@link IIpsObjectPathContainerType IIpsObjectPathContainerTypes} to be
     * used by the {@link IpsObjectPathContainerFactory}.
     */
    List<IIpsObjectPathContainerType> getIpsObjectPathContainerTypes();

    /**
     * Returns all registered {@link Datatype Datatypes} mapped by their qualified names.
     */
    Map<String, Datatype> getPredefinedDatatypes();

    /**
     * Returns all registered {@link IProductCmptNamingStrategyFactory product component naming
     * strategy factories}.
     *
     * @since 22.12
     */
    List<IProductCmptNamingStrategyFactory> getProductCmptNamingStrategyFactories();

    /**
     * Returns all registered {@link ICustomValidation custom validations}.
     *
     * @since 22.12
     */
    List<ICustomValidation<?>> getCustomValidations();

    /**
     * Returns all registered additional {@link IpsObjectType IPS object types} (beyond those
     * defined in {@link IpsObjectType} itself).
     *
     * @since 22.12
     */
    List<IpsObjectType> getAdditionalIpsObjectTypes();

    /**
     * Returns all registered {@link ReleaseExtension ReleaseExtensions}.
     *
     * @since 22.12
     */
    List<ReleaseExtension> getReleaseExtensions();

    /**
     * Returns a registry for looking up helpers for data types.
     *
     * @since 23.1
     */
    Supplier<Map<Datatype, DatatypeHelper>> getDatatypeHelperRegistry();

    /**
     * Returns the {@link ReleaseExtension} registered for the given {@link IIpsProject}, if one is
     * configured, otherwise {@link Optional#empty()}.
     *
     * @param ipsProject the Faktor-IPS project for which the release extension is requested
     *
     * @since 22.12
     */
    default Optional<ReleaseExtension> getReleaseExtension(IIpsProject ipsProject) {
        String releaseExtensionId = ipsProject == null ? null
                : ipsProject.getReadOnlyProperties().getReleaseExtensionId();
        return releaseExtensionId == null ? Optional.empty()
                : getReleaseExtensions().stream()
                        .filter(e -> e.getId().equals(releaseExtensionId))
                        .findFirst();
    }

}
