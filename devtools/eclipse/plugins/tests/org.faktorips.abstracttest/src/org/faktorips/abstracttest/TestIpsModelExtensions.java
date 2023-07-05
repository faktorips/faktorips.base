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

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.abstraction.Abstractions;
import org.faktorips.devtools.model.IClassLoaderProviderFactory;
import org.faktorips.devtools.model.IIpsModelExtensions;
import org.faktorips.devtools.model.IIpsProjectConfigurator;
import org.faktorips.devtools.model.IPreSaveProcessor;
import org.faktorips.devtools.model.IVersionProviderFactory;
import org.faktorips.devtools.model.extproperties.IExtensionPropertyDefinition;
import org.faktorips.devtools.model.internal.productcmpt.IDeepCopyOperationFixup;
import org.faktorips.devtools.model.internal.productcmpt.IFormulaCompiler;
import org.faktorips.devtools.model.internal.productcmpt.IImplementationClassProvider;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathContainerType;
import org.faktorips.devtools.model.preferences.IIpsModelPreferences;
import org.faktorips.devtools.model.versionmanager.IIpsFeatureVersionManager;

public interface TestIpsModelExtensions extends IIpsModelExtensions, AutoCloseable {

    static TestIpsModelExtensions get() {
        if (Abstractions.isEclipseRunning()) {
            return new TestEclipseIpsModelExtensions();
        } else {
            return new TestPlainJavaIpsModelExtensions();
        }
    }

    static TestIpsModelExtensions using(IIpsModelPreferences modelPreferences) {
        TestIpsModelExtensions ipsModelExtensions = get();
        ipsModelExtensions.setModelPreferences(modelPreferences);
        return ipsModelExtensions;
    }

    @Override
    void close();

    /**
     * Sets the deep-copy-operation-fix-ups. This method overwrites all fix-ups registered via
     * extension points.
     */
    void setDeepCopyOperationFixups(List<IDeepCopyOperationFixup> deepCopyOperationFixups);

    /**
     * Sets the feature version managers. This method overwrites all feature managers registered via
     * extension points.
     */
    void setFeatureVersionManagers(IIpsFeatureVersionManager... managers);

    /**
     * Sets the version provider factories. This method overwrites all version provider factories
     * registered via extension points.
     */
    void setVersionProviderFactories(Map<String, IVersionProviderFactory> versionProviderFactories);

    /**
     * Sets the ClassLoaderProviderFactory. This method overwrites the ClassLoaderProviderFactory
     * registered via extension points.
     */
    void setClassLoaderProviderFactory(IClassLoaderProviderFactory classLoaderProviderFactory);

    /**
     * Sets the {@link IIpsProjectConfigurator project-configurators}. This method overwrites the
     * configurators registered via extension points.
     *
     * @param ipsProjectConfigurators The passed IPS project-configurators
     */
    void setIpsProjectConfigurators(List<IIpsProjectConfigurator> ipsProjectConfigurators);

    /**
     * Sets the IIpsModelPreferences. This method overwrites the IIpsModelPreferences registered via
     * extension points.
     */
    void setModelPreferences(IIpsModelPreferences modelPreferences);

    void setIpsObjectPathContainerTypes(List<IIpsObjectPathContainerType> ipsObjectPathContainerTypes);

    void setExtensionPropertyDefinitions(
            Map<Class<?>, List<IExtensionPropertyDefinition>> extensionPropertyDefinitions);

    void setPreSaveProcessors(Map<IpsObjectType, List<IPreSaveProcessor>> preSaveProcessors);

    void setPreSaveProcessor(IpsObjectType ipsObjectType, Consumer<IIpsObject> preSaveProcessor);

    default TestIpsModelExtensions with(IIpsModelPreferences modelPreferences) {
        setModelPreferences(modelPreferences);
        return this;
    }

    void setPredefinedDatatypes(Map<String, Datatype> predefinedDatatypes);

    void addPredefinedDatatype(Datatype datatype);

    void setFormulaCompiler(IFormulaCompiler formulaCompiler);

    void setImplementationClassProvider(IImplementationClassProvider implementationClassProvider);

}
