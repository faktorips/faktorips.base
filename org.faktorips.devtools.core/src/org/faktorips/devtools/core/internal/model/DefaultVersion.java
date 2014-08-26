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
import org.faktorips.devtools.core.util.AlphaNumericComparator;
import org.faktorips.util.ArgumentCheck;

/**
 * This simple implementation of {@link IVersion} simply takes a string argument and uses it as
 * internal representation of the version. The compare of two versions is alphanumeric. That means
 * the comparator recognize numeric parts are compares them as numbers.
 * 
 * @see AlphaNumericComparator
 * 
 */
public class DefaultVersion implements IVersion<DefaultVersion> {

    public static final DefaultVersion EMPTY_VERSION = new DefaultVersion("0"); //$NON-NLS-1$

    private final String versionString;

    public DefaultVersion(String versionString) {
        ArgumentCheck.notNull(versionString);
        this.versionString = versionString;
    }

    @Override
    public int compareTo(DefaultVersion o) {
        return new AlphaNumericComparator().compare(versionString, o.versionString);
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
        DefaultVersion other = (DefaultVersion)obj;
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

    @Override
    public boolean isEmptyVersion() {
        return EMPTY_VERSION.equals(this);
    }

    @Override
    public boolean isNotEmptyVersion() {
        return !isEmptyVersion();
    }

}