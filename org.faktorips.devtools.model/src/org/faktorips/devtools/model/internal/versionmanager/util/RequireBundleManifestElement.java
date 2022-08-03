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

import org.apache.commons.lang.StringUtils;
import org.eclipse.osgi.service.resolver.VersionRange;
import org.eclipse.osgi.util.ManifestElement;
import org.faktorips.util.ArgumentCheck;
import org.osgi.framework.Constants;

/**
 * Class extends the {@link ManifestElement} and supplies the String for require-Bundles.
 * 
 * @author frank
 */
public class RequireBundleManifestElement {

    private static final String EQUAL = "="; //$NON-NLS-1$
    private static final String SEMICOLON = ";"; //$NON-NLS-1$
    private static final String QUOTES = "\""; //$NON-NLS-1$
    private static final String COLON_EQUAL = ":="; //$NON-NLS-1$

    private ManifestElement manifestElement;
    private VersionRange versionRange;
    private final String pluginName;

    /**
     * Constructs a new RequireBundleManifestElement with {@link ManifestElement}
     * 
     * @param manifestElement the {@link ManifestElement}
     */
    public RequireBundleManifestElement(ManifestElement manifestElement) {
        ArgumentCheck.notNull(manifestElement);
        this.manifestElement = manifestElement;
        pluginName = manifestElement.getValue();
    }

    /**
     * Constructs a new RequireBundleManifestElement with plugin Name
     * 
     * @param pluginName Name of Plugin
     * @param versionRange the range for versions
     */
    public RequireBundleManifestElement(String pluginName, VersionRange versionRange) {
        this.pluginName = pluginName;
        setVersionRange(versionRange);
    }

    public String getPluginName() {
        return pluginName;
    }

    public void setVersionRange(VersionRange versionRange) {
        ArgumentCheck.notNull(versionRange);
        this.versionRange = versionRange;
    }

    public String getManifestElement() {
        if (versionRange != null) {
            return getChangedManifestElement();
        }
        return getOriginalManifestElement();
    }

    private String getOriginalManifestElement() {
        return manifestElement.toString();
    }

    private String getChangedManifestElement() {
        StringBuilder builder = new StringBuilder();
        builder.append(pluginName);
        builder.append(appendBundleVersion());
        builder.append(fillDirective(Constants.VISIBILITY_DIRECTIVE));
        builder.append(fillDirective(Constants.RESOLUTION_DIRECTIVE));
        return builder.toString();
    }

    private String getDirective(String key) {
        if (manifestElement != null) {
            return manifestElement.getDirective(key);
        } else {
            return null;
        }
    }

    /**
     * Gets the String for the directive
     * 
     * @param key Name of the directive
     * @return String ;visibility:=reexport
     */
    private String getDirectiveString(String key) {
        return SEMICOLON + key + COLON_EQUAL + getDirective(key);
    }

    private boolean hasDirective(String key) {
        return getDirective(key) != null;
    }

    private String fillDirective(String key) {
        if (hasDirective(key)) {
            return getDirectiveString(key);
        }
        return StringUtils.EMPTY;
    }

    /**
     * Gets the bundle-version
     * 
     * @return String ;bundle-version="[3.9.0,3.10.0]"
     */
    private String appendBundleVersion() {
        final StringBuilder builder = new StringBuilder();
        builder.append(SEMICOLON);
        builder.append(Constants.BUNDLE_VERSION_ATTRIBUTE);
        builder.append(EQUAL);
        builder.append(QUOTES);
        builder.append(versionRange.toString());
        builder.append(QUOTES);
        return builder.toString();
    }
}
