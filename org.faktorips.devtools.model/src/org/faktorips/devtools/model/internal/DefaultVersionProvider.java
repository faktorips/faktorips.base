/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal;

import java.util.regex.Pattern;

import org.eclipse.core.runtime.IConfigurationElement;
import org.faktorips.devtools.abstraction.Abstractions;
import org.faktorips.devtools.model.IVersion;
import org.faktorips.devtools.model.IVersionProvider;
import org.faktorips.devtools.model.internal.ipsproject.properties.IpsProjectProperties;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.ipsproject.IVersionFormat;
import org.faktorips.devtools.model.plugin.ExtensionPoints;

/**
 * The {@link DefaultVersionProvider} is the used when no other {@link IVersionProvider} is
 * configured. It simply reads the version that is specified in the {@link IpsProjectProperties}.
 * The version format could be configured by an extended product release extension. If no product
 * release extension is configured, the default version format is the {@link OsgiVersionFormat}.
 */
public class DefaultVersionProvider implements IVersionProvider<DefaultVersion> {

    private final IIpsProject ipsProject;

    private final IVersionFormat versionFormat;

    public DefaultVersionProvider(IIpsProject ipsProject) {
        this.ipsProject = ipsProject;
        versionFormat = createVersionFormat();
    }

    public DefaultVersionProvider(IIpsProject ipsProject, IVersionFormat versionFormat) {
        this.ipsProject = ipsProject;
        this.versionFormat = versionFormat;
    }

    private IVersionFormat createVersionFormat() {
        final IConfigurationElement releaseExtension = Abstractions.isEclipseRunning()
                ? ExtensionPoints.getReleaseExtensionElement(ipsProject)
                : null;
        if (releaseExtension == null) {
            return new OsgiVersionFormat();
        } else {
            return new ReleaseExtensionVersionFormat(releaseExtension);
        }
    }

    @Override
    public IVersion<DefaultVersion> getVersion(String versionAsString) {
        if (isCorrectVersionFormat(versionAsString)) {
            return new DefaultVersion(versionAsString);
        } else {
            throw new IllegalArgumentException(("No valid version: " + versionAsString)); //$NON-NLS-1$
        }
    }

    @Override
    public IVersion<DefaultVersion> getProjectVersion() {
        String projectVersion = ipsProject.getReadOnlyProperties().getVersion();
        if (projectVersion != null && isCorrectVersionFormat(projectVersion)) {
            return getVersion(projectVersion);
        }
        return DefaultVersion.EMPTY_VERSION;
    }

    @Override
    public void setProjectVersion(IVersion<DefaultVersion> version) {
        IIpsProjectProperties properties = ipsProject.getProperties();
        properties.setVersion(version.asString());
        ipsProject.setProperties(properties);
    }

    @Override
    public boolean isCorrectVersionFormat(String version) {
        return versionFormat.isCorrectVersionFormat(version);
    }

    @Override
    public String getVersionFormat() {
        return versionFormat.getVersionFormat();
    }

    public static final class ReleaseExtensionVersionFormat implements IVersionFormat {

        public static final String VERSION_FORMAT_REGEX = "versionFormatRegex"; //$NON-NLS-1$

        public static final String READABLE_VERSION_FORMAT = "readableVersionFormat"; //$NON-NLS-1$

        private final IConfigurationElement releaseExtension;

        private final Pattern versionPattern;

        private ReleaseExtensionVersionFormat(IConfigurationElement releaseExtension) {
            this.releaseExtension = releaseExtension;
            versionPattern = createVersionPattern(releaseExtension);
        }

        @Override
        public boolean isCorrectVersionFormat(String version) {
            return versionPattern.matcher(version).matches();
        }

        @Override
        public String getVersionFormat() {
            return createVersionFormat(releaseExtension);
        }

        private Pattern createVersionPattern(final IConfigurationElement releaseExtension) {
            return Pattern.compile(releaseExtension.getAttribute(VERSION_FORMAT_REGEX));
        }

        private String createVersionFormat(final IConfigurationElement releaseExtension) {
            return releaseExtension.getAttribute(READABLE_VERSION_FORMAT);
        }

    }

}
