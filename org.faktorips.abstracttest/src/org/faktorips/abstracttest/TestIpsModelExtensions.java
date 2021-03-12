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

import org.eclipse.core.runtime.Platform;
import org.faktorips.devtools.model.IClassLoaderProviderFactory;
import org.faktorips.devtools.model.IVersionProviderFactory;
import org.faktorips.devtools.model.internal.productcmpt.IDeepCopyOperationFixup;
import org.faktorips.devtools.model.plugin.IpsModelExtensionsViaEclipsePlugins;
import org.faktorips.devtools.model.preferences.IIpsModelPreferences;
import org.faktorips.devtools.model.versionmanager.IIpsFeatureVersionManager;

/**
 * Subclass for testing with hard set values.
 * <p>
 * The {@link TestIpsModelExtensions} class is {@link AutoCloseable}; it replaces the original
 * {@link IpsModelExtensionsViaEclipsePlugins} singleton instance and restores it when
 * {@link #close()} is automatically called in the finish block.
 * <p>
 * Usage:
 *
 * <pre>
 * // the customExtensionRegistry parameter is optional. If omitted, the standard Eclipse extension
 * // registry will be used for all non-overwritten values
 * try (TestIpsModelExtensions testIpsModelExtensions = new TestIpsModelExtensions()) {
 *     // ... set custom values ...
 *     testIpsModelExtensions.set...
 *     // ... test ...
 * }
 * </pre>
 */
public class TestIpsModelExtensions extends IpsModelExtensionsViaEclipsePlugins implements AutoCloseable {

    private IpsModelExtensionsViaEclipsePlugins original;

    private List<IDeepCopyOperationFixup> deepCopyOperationFixups;

    private IIpsFeatureVersionManager[] featureVersionManagers;

    private Map<String, IVersionProviderFactory> versionProviderFactories;

    private IClassLoaderProviderFactory classLoaderProviderFactory;

    private IIpsModelPreferences modelPreferences;

    public TestIpsModelExtensions() {
        super(Platform.getExtensionRegistry());
        original = IpsModelExtensionsViaEclipsePlugins.get();
        IpsModelExtensionsViaEclipsePlugins.setInstanceForTest(this);
    }

    @Override
    public void close() {
        IpsModelExtensionsViaEclipsePlugins.setInstanceForTest(original);
    }

    @Override
    public List<IDeepCopyOperationFixup> getDeepCopyOperationFixups() {
        return deepCopyOperationFixups != null ? deepCopyOperationFixups : original.getDeepCopyOperationFixups();
    }

    /**
     * Sets the deep-copy-operation-fix-ups. This method overwrites all fix-ups registered via
     * extension points.
     */
    public void setDeepCopyOperationFixups(List<IDeepCopyOperationFixup> deepCopyOperationFixups) {
        this.deepCopyOperationFixups = deepCopyOperationFixups;
    }

    /**
     * Sets the feature version managers. This method overwrites all feature managers registered via
     * extension points.
     */
    public void setFeatureVersionManagers(IIpsFeatureVersionManager... managers) {
        this.featureVersionManagers = managers;
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
    public void setClassLoaderProviderFactory(IClassLoaderProviderFactory classLoaderProviderFactory) {
        this.classLoaderProviderFactory = classLoaderProviderFactory;
    }

    @Override
    public IIpsModelPreferences getModelPreferences() {
        return modelPreferences != null ? modelPreferences : super.getModelPreferences();
    }

    /**
     * Sets the IIpsModelPreferences. This method overwrites the IIpsModelPreferences registered via
     * extension points.
     */
    public void setModelPreferences(IIpsModelPreferences modelPreferences) {
        this.modelPreferences = modelPreferences;
    }

    public TestIpsModelExtensions with(IIpsModelPreferences modelPreferences) {
        setModelPreferences(modelPreferences);
        return this;
    }

    public static TestIpsModelExtensions using(IIpsModelPreferences modelPreferences) {
        TestIpsModelExtensions ipsModelExtensions = new TestIpsModelExtensions();
        ipsModelExtensions.setModelPreferences(modelPreferences);
        return ipsModelExtensions;
    }

}
