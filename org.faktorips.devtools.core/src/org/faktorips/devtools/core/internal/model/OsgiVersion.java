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

import org.faktorips.devtools.core.model.IVersion;
import org.faktorips.util.ArgumentCheck;
import org.osgi.framework.Version;

/**
 * This simple implementation of {@link IVersion} simply takes a string argument and uses it as
 * internal representation of the version.
 */
public class OsgiVersion implements IVersion<OsgiVersion> {

    private Version version;

    public OsgiVersion(String versionString) {
        ArgumentCheck.notNull(versionString);
        version = new Version(versionString);
    }

    @Override
    public String asString() {
        return version.toString();
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

}