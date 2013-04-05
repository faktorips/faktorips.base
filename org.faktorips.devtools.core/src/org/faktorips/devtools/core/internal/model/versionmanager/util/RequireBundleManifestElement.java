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
        this.pluginName = manifestElement.getValue();
    }

    /**
     * Constructs a new RequireBundleManifestElement with plugin Name
     * 
     * @param pluginName Name of Plugin
     * @param versionRange the range for versions
     */
    public RequireBundleManifestElement(String pluginName, VersionRange versionRange) {
        this.pluginName = pluginName;
        this.setVersionRange(versionRange);
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
        StringBuffer buffer = new StringBuffer();
        buffer.append(pluginName);
        buffer.append(appendBundleVersion());
        buffer.append(fillDirective(Constants.VISIBILITY_DIRECTIVE));
        buffer.append(fillDirective(Constants.RESOLUTION_DIRECTIVE));
        return buffer.toString();
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
        final StringBuffer buffer = new StringBuffer();
        buffer.append(SEMICOLON);
        buffer.append(Constants.BUNDLE_VERSION_ATTRIBUTE);
        buffer.append(EQUAL);
        buffer.append(QUOTES);
        buffer.append(versionRange.toString());
        buffer.append(QUOTES);
        return buffer.toString();
    }
}
