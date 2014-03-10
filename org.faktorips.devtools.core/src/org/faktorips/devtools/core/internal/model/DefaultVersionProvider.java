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

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.DefaultVersionProvider.Version;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProjectProperties;
import org.faktorips.devtools.core.model.IVersion;
import org.faktorips.devtools.core.model.IVersionProvider;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.ipsproject.IVersionFormat;
import org.faktorips.util.ArgumentCheck;

/**
 * 
 * The {@link DefaultVersionProvider} is the used when no other {@link IVersionProvider} is
 * configured. It simply reads the version that is specified in the {@link IpsProjectProperties}.
 * The version format could be configured by the a extends productReleaseExtension.
 */
public class DefaultVersionProvider implements IVersionProvider<Version> {

    private final IIpsProject ipsProject;

    public DefaultVersionProvider(IIpsProject ipsProject) {
        this.ipsProject = ipsProject;
    }

    @Override
    public IVersion<Version> getVersion(String versionAsString) {
        return new Version(versionAsString);
    }

    @Override
    public IVersion<Version> getProjectlVersion() {
        return getVersion(ipsProject.getReadOnlyProperties().getVersion());
    }

    @Override
    public void setProjectVersion(IVersion<Version> version) {
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
        IVersionFormat versionFormat = getVersionFormatFromProject();
        if (versionFormat != null) {
            return versionFormat.isCorrectVersionFormat(version);
        } else {
            return true;
        }
    }

    @Override
    public String getVersionFormat() {
        IVersionFormat versionFormat = getVersionFormatFromProject();
        if (versionFormat != null) {
            return versionFormat.getVersionFormat();
        } else {
            return StringUtils.EMPTY;
        }
    }

    private IVersionFormat getVersionFormatFromProject() {
        try {
            return ipsProject.getVersionFormat();
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    /**
     * This simple implementation of {@link IVersion} simply takes a string argument and uses it as
     * internal representation of the version. The compare of two versions are only alphabetically.
     * Hence the version 3.0 is greater than 13.0!
     * 
     */
    public static class Version implements IVersion<Version> {

        private String versionString;

        public Version(String versionString) {
            ArgumentCheck.notNull(versionString);
            this.versionString = versionString;
        }

        @Override
        public int compareTo(Version o) {
            return versionString.compareTo(o.versionString);
        }

        @Override
        public String asString() {
            return versionString;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((versionString == null) ? 0 : versionString.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Version other = (Version)obj;
            if (versionString == null) {
                if (other.versionString != null) {
                    return false;
                }
            } else if (!versionString.equals(other.versionString)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "Version [" + versionString + "]"; //$NON-NLS-1$ //$NON-NLS-2$
        }

    }

}
