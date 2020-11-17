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
import org.faktorips.devtools.model.IVersionProviderFactory;
import org.faktorips.devtools.model.internal.productcmpt.IDeepCopyOperationFixup;
import org.faktorips.devtools.model.plugin.IpsModelExtensionsViaEclipsePlugins;
import org.faktorips.devtools.model.versionmanager.IIpsFeatureVersionManager;

/**
 * Subclass for testing with hard set values.
 * <p>
 * The {@link TestIpsModelExtensionsViaEclipsePlugins} class is {@link AutoCloseable}; it replaces
 * the original {@link IpsModelExtensionsViaEclipsePlugins} singleton instance and restores it when
 * {@link #close()} is automatically called in the finish block.
 * <p>
 * Usage:
 *
 * <pre>
 * // the customExtensionRegistry parameter is optional. If omitted, the standard Eclipse extension
 * // registry will be used for all non-overwritten values
 * try (TestIpsModelExtensionsViaEclipsePlugins testIpsModelExtensions = new TestIpsModelExtensionsViaEclipsePlugins(
 *         customExtensionRegistry)) {
 *     // ... set custom values ...
 *     // ... test ...
 * }
 * </pre>
 */
public class TestIpsModelExtensionsViaEclipsePlugins extends IpsModelExtensionsViaEclipsePlugins
        implements AutoCloseable {

    private IpsModelExtensionsViaEclipsePlugins original;

    private List<IDeepCopyOperationFixup> deepCopyOperationFixups;

    private IIpsFeatureVersionManager[] featureVersionManagers;

    private Map<String, IVersionProviderFactory> versionProviderFactories;

    public TestIpsModelExtensionsViaEclipsePlugins() {
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

}
