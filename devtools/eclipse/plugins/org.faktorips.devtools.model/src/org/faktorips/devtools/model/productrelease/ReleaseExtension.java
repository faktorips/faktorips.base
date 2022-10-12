/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.model.productrelease;

import java.util.regex.Pattern;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.faktorips.devtools.model.plugin.ExtensionPoints;

public class ReleaseExtension {

    public static final String CONFIG_ELEMENT_OPERATION = "operation"; //$NON-NLS-1$

    private static final String ID = "id";

    private static final String VERSION_FORMAT_REGEX = "versionFormatRegex"; //$NON-NLS-1$

    private static final String READABLE_VERSION_FORMAT = "readableVersionFormat"; //$NON-NLS-1$

    private static final String VERSION_MUST_CHANGE = "versionMustChange"; //$NON-NLS-1$

    private final String id;

    private final Pattern versionFormatRegex;

    private final String readableVersionFormat;

    private final boolean versionMustChange;

    private final IConfigurationElement configurationElement;

    public ReleaseExtension(IConfigurationElement configurationElement) {
        this.configurationElement = configurationElement;
        id = configurationElement.getAttribute(ID);
        versionFormatRegex = Pattern.compile(configurationElement.getAttribute(VERSION_FORMAT_REGEX));
        readableVersionFormat = configurationElement.getAttribute(READABLE_VERSION_FORMAT);
        String versionMustChangeAttribute = configurationElement.getAttribute(VERSION_MUST_CHANGE);
        versionMustChange = versionMustChangeAttribute == null || Boolean.parseBoolean(versionMustChangeAttribute);
    }

    public String getId() {
        return id;
    }

    public Pattern getVersionFormatRegex() {
        return versionFormatRegex;
    }

    public String getReadableVersionFormat() {
        return readableVersionFormat;
    }

    public boolean isVersionChangeRequired() {
        return versionMustChange;
    }

    public IReleaseAndDeploymentOperation createReleaseAndDeploymentOperation() {
        return ExtensionPoints.createExecutableExtension((IExtension)configurationElement.getParent(),
                configurationElement, CONFIG_ELEMENT_OPERATION, IReleaseAndDeploymentOperation.class);
    }

}
