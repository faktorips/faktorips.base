/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model;

import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProjectProperties;
import org.faktorips.devtools.core.internal.productrelease.ProductReleaseProcessor;
import org.faktorips.devtools.core.model.IVersion;
import org.faktorips.devtools.core.model.IVersionProvider;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.ipsproject.IVersionFormat;

/**
 * The {@link DefaultVersionProvider} is the used when no other {@link IVersionProvider} is
 * configured. It simply reads the version that is specified in the {@link IpsProjectProperties}.
 * The version format could be configured by a extended productReleaseExtension. If no
 * productReleaseExtension is configured, the default version format is
 * "[0-9]+\\.[0-9]+\\.[0-9]+\\.[a-z]+"
 */
public class DefaultVersionProvider implements IVersionProvider<DefaultVersion> {

    private final IIpsProject ipsProject;

    private final IVersionFormat versionFormat;

    public DefaultVersionProvider(IIpsProject ipsProject) {
        this.ipsProject = ipsProject;
        this.versionFormat = createVersionFormat();
    }

    public DefaultVersionProvider(IIpsProject ipsProject, IVersionFormat versionFormat) {
        this.ipsProject = ipsProject;
        this.versionFormat = versionFormat;
    }

    private IVersionFormat createVersionFormat() {
        final IConfigurationElement releaseExtension = ProductReleaseProcessor.getReleaseExtensionElement(ipsProject);
        if (releaseExtension == null) {
            return new DefaultVersionFormat();
        } else {
            return new ReleaseExtensionVersionFormat(releaseExtension);
        }
    }

    @Override
    public IVersion<DefaultVersion> getVersion(String versionAsString) {
        return new DefaultVersion(versionAsString);
    }

    @Override
    public IVersion<DefaultVersion> getProjectlVersion() {
        return getVersion(ipsProject.getReadOnlyProperties().getVersion());
    }

    @Override
    public void setProjectVersion(IVersion<DefaultVersion> version) {
        IIpsProjectProperties properties = ipsProject.getProperties();
        properties.setVersion(version.asString());
        try {
            ipsProject.setProperties(properties);
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    @Override
    public boolean isCorrectVersionFormat(String version) {
        return versionFormat.isCorrectVersionFormat(version);
    }

    @Override
    public String getVersionFormat() {
        return versionFormat.getVersionFormat();
    }

    private static final class DefaultVersionFormat implements IVersionFormat {

        private static final Pattern versionPattern = Pattern.compile("[0-9]+\\.[0-9]+\\.[0-9]+\\.[a-z]+"); //$NON-NLS-1$

        @Override
        public boolean isCorrectVersionFormat(String version) {
            return versionPattern.matcher(version).matches();
        }

        @Override
        public String getVersionFormat() {
            return "for example: 2.1.4.qualifier"; //$NON-NLS-1$
        }
    }

    private static final class ReleaseExtensionVersionFormat implements IVersionFormat {

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
            return Pattern.compile(releaseExtension.getAttribute(ProductReleaseProcessor.VERSION_FORMAT_REGEX));
        }

        private String createVersionFormat(final IConfigurationElement releaseExtension) {
            return releaseExtension.getAttribute(ProductReleaseProcessor.READABLE_VERSION_FORMAT);
        }

    }

}
