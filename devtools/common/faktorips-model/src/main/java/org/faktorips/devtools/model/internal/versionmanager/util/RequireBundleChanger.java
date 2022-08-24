/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.versionmanager.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.jar.Attributes;

import org.eclipse.osgi.service.resolver.VersionRange;
import org.eclipse.osgi.util.ManifestElement;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;

/**
 * Class to change the versionRange from specific bundle of the Require-Bundles at the manifest-file
 * 
 * @author frank
 */
public class RequireBundleChanger {

    private static final String COMMA = ","; //$NON-NLS-1$
    private final Attributes attributes;
    private final StringBuffer buffer;
    private final Map<String, RequireBundleManifestElement> requireBundles;

    public RequireBundleChanger(Attributes attributes) {
        this.attributes = attributes;
        buffer = new StringBuffer();
        requireBundles = new LinkedHashMap<>();
    }

    /**
     * Finds all requireBundles in Manifest and change the one that needs to refresh.
     * 
     * @param plugin Name of the plugin to change
     * @param versionRange range with version
     */
    public void changePluginDependency(String plugin, VersionRange versionRange) {

        final String requireBundle = attributes.getValue(Constants.REQUIRE_BUNDLE);
        if (requireBundle != null) {
            try {
                ManifestElement[] elements = ManifestElement.parseHeader(Constants.REQUIRE_BUNDLE, requireBundle);
                for (ManifestElement manifestElement : elements) {
                    RequireBundleManifestElement editManifestElement = new RequireBundleManifestElement(
                            manifestElement);
                    requireBundles.put(editManifestElement.getPluginName(), editManifestElement);
                }
            } catch (BundleException be) {
                throw new IpsException(be.getMessage());
            }
        }

        modifyMap(plugin, versionRange);
        // set all the dependencies in requireBundles
        attributes.putValue(Constants.REQUIRE_BUNDLE, getRequireDependencies());
    }

    private void modifyMap(String plugin, VersionRange versionRange) {
        if (requireBundles.containsKey(plugin)) {
            // plugin found, set the versionRange
            RequireBundleManifestElement requireBundleManifestElement = requireBundles.get(plugin);
            requireBundleManifestElement.setVersionRange(versionRange);
        } else {
            // plugin not found, create new one
            requireBundles.put(plugin, new RequireBundleManifestElement(plugin, versionRange));
        }
    }

    private String getRequireDependencies() {
        for (RequireBundleManifestElement reElement : requireBundles.values()) {
            appendSeparator();
            append(reElement.getManifestElement());
        }
        return buffer.toString();
    }

    private void append(String appendString) {
        buffer.append(appendString);
    }

    private void appendSeparator() {
        if (buffer.length() > 0) {
            buffer.append(COMMA);
        }
    }
}
