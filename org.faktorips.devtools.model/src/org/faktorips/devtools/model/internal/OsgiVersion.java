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

import org.faktorips.devtools.model.IVersion;
import org.faktorips.util.ArgumentCheck;
import org.osgi.framework.Version;

/**
 * A version in the OSGi version syntax as described by {@link Version}.
 */
public class OsgiVersion implements IVersion<OsgiVersion> {

    public static final OsgiVersion EMPTY_VERSION = new OsgiVersion(Version.emptyVersion);

    private final Version version;

    public OsgiVersion(String versionString) {
        ArgumentCheck.notNull(versionString);
        version = new Version(versionString);
    }

    private OsgiVersion(Version version) {
        this.version = version;
    }

    @Override
    public String asString() {
        return version.toString();
    }

    @Override
    public String getUnqualifiedVersion() {
        if (version.getQualifier().isEmpty()) {
            return version.toString();
        } else {
            String versionString = version.toString();
            int qualifierSep = versionString.lastIndexOf('.');
            return versionString.substring(0, qualifierSep);
        }
    }

    @Override
    public int compareTo(OsgiVersion osgiVersion) {
        return version.compareTo(osgiVersion.version);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof OsgiVersion) {
            OsgiVersion osgiVersion = (OsgiVersion)obj;
            Version otherVersion = osgiVersion.version;
            return this.version.equals(otherVersion);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.version.hashCode();
    }

    @Override
    public String toString() {
        return "OsgiVersion [version=" + version + "]"; //$NON-NLS-1$//$NON-NLS-2$
    }

    @Override
    public boolean isEmptyVersion() {
        return Version.emptyVersion.equals(version);
    }

    @Override
    public boolean isNotEmptyVersion() {
        return !isEmptyVersion();
    }

}