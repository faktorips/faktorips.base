/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.abstracttest;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.IClassLoaderProviderFactory;
import org.faktorips.devtools.model.IIpsProjectConfigurator;
import org.faktorips.devtools.model.IPreSaveProcessor;
import org.faktorips.devtools.model.IVersionProviderFactory;
import org.faktorips.devtools.model.eclipse.internal.IpsModelExtensionsViaEclipsePlugins;
import org.faktorips.devtools.model.extproperties.IExtensionPropertyDefinition;
import org.faktorips.devtools.model.internal.productcmpt.IDeepCopyOperationFixup;
import org.faktorips.devtools.model.internal.productcmpt.IFormulaCompiler;
import org.faktorips.devtools.model.internal.productcmpt.IImplementationClassProvider;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathContainerType;
import org.faktorips.devtools.model.plainjava.internal.PlainJavaIpsModelExtensions;
import org.faktorips.devtools.model.preferences.IIpsModelPreferences;
import org.faktorips.devtools.model.versionmanager.IIpsFeatureVersionManager;

/**
 * Subclass for testing with hard set values.
 * <p>
 * The {@link TestPlainJavaIpsModelExtensions} class is {@link AutoCloseable}; it replaces the
 * original {@link IpsModelExtensionsViaEclipsePlugins} singleton instance and restores it when
 * {@link #close()} is automatically called in the finish block.
 * <p>
 * Usage:
 *
 * <pre>
 * // the customExtensionRegistry parameter is optional. If omitted, the standard Eclipse extension
 * // registry will be used for all non-overwritten values
 * try (TestIpsModelExtensions testIpsModelExtensions = TestIpsModelExtensions.get()) {
 *     // ... set custom values ...
 *     testIpsModelExtensions.set...
 *     // ... test ...
 * }
 * </pre>
 */
public class TestPlainJavaIpsModelExtensions extends PlainJavaIpsModelExtensions implements TestIpsModelExtensions {

    private PlainJavaIpsModelExtensions original;

    private List<IDeepCopyOperationFixup> deepCopyOperationFixups;

    private IIpsFeatureVersionManager[] featureVersionManagers;

    private Map<String, IVersionProviderFactory> versionProviderFactories;

    private IClassLoaderProviderFactory classLoaderProviderFactory;

    private IIpsModelPreferences modelPreferences;

    private List<IIpsProjectConfigurator> ipsProjectConfigurators;

    private List<IIpsObjectPathContainerType> ipsObjectPathContainerTypes;

    private Map<Class<?>, List<IExtensionPropertyDefinition>> extensionPropertyDefinitions;

    private Map<IpsObjectType, List<IPreSaveProcessor>> preSaveProcessors;

    private Map<String, Datatype> predefinedDatatypes;

    private IFormulaCompiler formulaCompiler;

    private IImplementationClassProvider implementationClassProvider;

    public TestPlainJavaIpsModelExtensions() {
        super();
        original = PlainJavaIpsModelExtensions.get();
        PlainJavaIpsModelExtensions.setInstanceForTest(this);
    }

    @Override
    public void close() {
        PlainJavaIpsModelExtensions.setInstanceForTest(original);
    }

    @Override
    public List<IDeepCopyOperationFixup> getDeepCopyOperationFixups() {
        return deepCopyOperationFixups != null ? deepCopyOperationFixups : original.getDeepCopyOperationFixups();
    }

    /**
     * Sets the deep-copy-operation-fix-ups. This method overwrites all fix-ups registered via
     * extension points.
     */
    @Override
    public void setDeepCopyOperationFixups(List<IDeepCopyOperationFixup> deepCopyOperationFixups) {
        this.deepCopyOperationFixups = deepCopyOperationFixups;
    }

    /**
     * Sets the feature version managers. This method overwrites all feature managers registered via
     * extension points.
     */
    @Override
    public void setFeatureVersionManagers(IIpsFeatureVersionManager... managers) {
        featureVersionManagers = managers;
    }

    @Override
    public IIpsFeatureVersionManager[] getIpsFeatureVersionManagers() {
        return featureVersionManagers != null ? featureVersionManagers : original.getIpsFeatureVersionManagers();
    }

    @Override
    public Map<String, IVersionProviderFactory> getVersionProviderFactories() {
        return versionProviderFactories != null ? versionProviderFactories : original.getVersionProviderFactories();
    }

    /**
     * Sets the version provider factories. This method overwrites all version provider factories
     * registered via extension points.
     */
    @Override
    public void setVersionProviderFactories(Map<String, IVersionProviderFactory> versionProviderFactories) {
        this.versionProviderFactories = versionProviderFactories;
    }

    @Override
    public IClassLoaderProviderFactory getClassLoaderProviderFactory() {
        return classLoaderProviderFactory != null ? classLoaderProviderFactory : super.getClassLoaderProviderFactory();
    }

    /**
     * Sets the ClassLoaderProviderFactory. This method overwrites the ClassLoaderProviderFactory
     * registered via extension points.
     */
    @Override
    public void setClassLoaderProviderFactory(IClassLoaderProviderFactory classLoaderProviderFactory) {
        this.classLoaderProviderFactory = classLoaderProviderFactory;
    }

    @Override
    public List<IIpsProjectConfigurator> getIpsProjectConfigurators() {
        return ipsProjectConfigurators != null ? ipsProjectConfigurators : super.getIpsProjectConfigurators();
    }

    /**
     * Sets the {@link IIpsProjectConfigurator project-configurators}. This method overwrites the
     * configurators registered via extension points.
     *
     * @param ipsProjectConfigurators The passed IPS project-configurators
     */
    @Override
    public void setIpsProjectConfigurators(List<IIpsProjectConfigurator> ipsProjectConfigurators) {
        this.ipsProjectConfigurators = ipsProjectConfigurators;
    }

    @Override
    public IIpsModelPreferences getModelPreferences() {
        return modelPreferences != null ? modelPreferences : super.getModelPreferences();
    }

    /**
     * Sets the IIpsModelPreferences. This method overwrites the IIpsModelPreferences registered via
     * extension points.
     */
    @Override
    public void setModelPreferences(IIpsModelPreferences modelPreferences) {
        this.modelPreferences = modelPreferences;
    }

    /**
     * Sets the IIpsObjectPathContainerTypes. This method overwrites the
     * IIpsObjectPathContainerTypes registered via extension points.
     */
    @Override
    public List<IIpsObjectPathContainerType> getIpsObjectPathContainerTypes() {
        return ipsObjectPathContainerTypes != null ? ipsObjectPathContainerTypes
                : super.getIpsObjectPathContainerTypes();
    }

    @Override
    public void setIpsObjectPathContainerTypes(List<IIpsObjectPathContainerType> ipsObjectPathContainerTypes) {
        this.ipsObjectPathContainerTypes = ipsObjectPathContainerTypes;
    }

    @Override
    public Map<Class<?>, List<IExtensionPropertyDefinition>> getExtensionPropertyDefinitions() {
        return extensionPropertyDefinitions != null ? extensionPropertyDefinitions
                : super.getExtensionPropertyDefinitions();
    }

    @Override
    public void setExtensionPropertyDefinitions(
            Map<Class<?>, List<IExtensionPropertyDefinition>> extensionPropertyDefinitions) {
        this.extensionPropertyDefinitions = extensionPropertyDefinitions;
    }

    @Override
    public List<IPreSaveProcessor> getPreSaveProcessors(IpsObjectType ipsObjectType) {
        return preSaveProcessors != null ? preSaveProcessors.computeIfAbsent(ipsObjectType, super::getPreSaveProcessors)
                : super.getPreSaveProcessors(ipsObjectType);
    }

    @Override
    public void setPreSaveProcessors(Map<IpsObjectType, List<IPreSaveProcessor>> preSaveProcessors) {
        this.preSaveProcessors = preSaveProcessors;
    }

    @Override
    public void setPreSaveProcessor(IpsObjectType ipsObjectType, Consumer<IIpsObject> preSaveProcessor) {
        preSaveProcessors = new HashMap<>(1);
        preSaveProcessors.put(ipsObjectType, List.of(new IPreSaveProcessor() {

            @Override
            public void process(IIpsObject ipsObject) {
                preSaveProcessor.accept(ipsObject);
            }

            @Override
            public IpsObjectType getIpsObjectType() {
                return ipsObjectType;
            }
        }));
    }

    @Override
    public Map<String, Datatype> getPredefinedDatatypes() {
        return predefinedDatatypes != null ? predefinedDatatypes : super.getPredefinedDatatypes();
    }

    @Override
    public void setPredefinedDatatypes(Map<String, Datatype> predefinedDatatypes) {
        this.predefinedDatatypes = predefinedDatatypes;
    }

    @Override
    public void addPredefinedDatatype(Datatype datatype) {
        predefinedDatatypes = new LinkedHashMap<>(getPredefinedDatatypes());
        predefinedDatatypes.put(datatype.getName(), datatype);
        predefinedDatatypes = Collections.unmodifiableMap(predefinedDatatypes);
    }

    @Override
    public IFormulaCompiler getFormulaCompiler() {
        return formulaCompiler != null ? formulaCompiler : super.getFormulaCompiler();
    }

    @Override
    public void setFormulaCompiler(IFormulaCompiler formulaCompiler) {
        this.formulaCompiler = formulaCompiler;
    }

    @Override
    public IImplementationClassProvider getImplementationClassProvider() {
        return implementationClassProvider != null ? implementationClassProvider
                : super.getImplementationClassProvider();
    }

    @Override
    public void setImplementationClassProvider(IImplementationClassProvider implementationClassProvider) {
        this.implementationClassProvider = implementationClassProvider;

    }

}
