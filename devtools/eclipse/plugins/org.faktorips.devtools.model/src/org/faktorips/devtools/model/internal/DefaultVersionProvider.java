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

import org.faktorips.devtools.model.IIpsModelExtensions;
import org.faktorips.devtools.model.IVersion;
import org.faktorips.devtools.model.IVersionProvider;
import org.faktorips.devtools.model.internal.ipsproject.properties.IpsProjectProperties;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.ipsproject.IVersionFormat;
import org.faktorips.devtools.model.productrelease.ReleaseExtension;

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
        return IIpsModelExtensions.get()
                .getReleaseExtension(ipsProject)
                .<IVersionFormat> map(ReleaseExtensionVersionFormat::new)
                .orElseGet(OsgiVersionFormat::new);
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

        private final ReleaseExtension releaseExtension;

        private ReleaseExtensionVersionFormat(ReleaseExtension releaseExtension) {
            this.releaseExtension = releaseExtension;
        }

        @Override
        public boolean isCorrectVersionFormat(String version) {
            return releaseExtension.getVersionFormatRegex().matcher(version).matches();
        }

        @Override
        public String getVersionFormat() {
            return releaseExtension.getReadableVersionFormat();
        }

    }

}
