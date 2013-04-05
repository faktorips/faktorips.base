/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.versionmanager.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.jar.Attributes;

import org.eclipse.osgi.service.resolver.VersionRange;
import org.eclipse.osgi.util.ManifestElement;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
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
        requireBundles = new LinkedHashMap<String, RequireBundleManifestElement>();
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
                    RequireBundleManifestElement editManifestElement = new RequireBundleManifestElement(manifestElement);
                    requireBundles.put(editManifestElement.getPluginName(), editManifestElement);
                }
            } catch (BundleException be) {
                throw new CoreRuntimeException(be.getMessage());
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
