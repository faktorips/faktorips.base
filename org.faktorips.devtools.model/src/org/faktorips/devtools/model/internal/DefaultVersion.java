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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.faktorips.devtools.model.IVersion;
import org.faktorips.devtools.model.util.AlphaNumericComparator;

/**
 * This simple implementation of {@link IVersion} simply takes a string argument and uses it as
 * internal representation of the version. The comparison of two versions is alphanumeric. That
 * means the comparator recognises numeric parts and compares them as numbers.
 * <p>
 * The version might have an optional qualifier part that is described as the part after the first
 * non-numeric character (except '.' if that is followed by another numeric character).
 * 
 * @see AlphaNumericComparator
 * 
 */
public class DefaultVersion implements IVersion<DefaultVersion> {

    public static final DefaultVersion EMPTY_VERSION = new DefaultVersion(null);

    private static final String DEFAULT_VERSION_STRING = "0"; //$NON-NLS-1$
    private static final Pattern QUALIFIER_PATTERN = Pattern.compile("\\d*(\\.\\d+)*"); //$NON-NLS-1$

    private final String versionString;

    public DefaultVersion(String versionString) {
        if (versionString == null || versionString.isEmpty()) {
            this.versionString = DEFAULT_VERSION_STRING;
        } else {
            this.versionString = versionString;
        }
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
    public String getUnqualifiedVersion() {
        Matcher matcher = QUALIFIER_PATTERN.matcher(versionString);
        if (matcher.find()) {
            String unqualified = matcher.group();
            return unqualified.isEmpty() ? EMPTY_VERSION.asString() : unqualified;
        } else {
            return EMPTY_VERSION.asString();
        }
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
        return this.equals(EMPTY_VERSION);
    }

    @Override
    public boolean isNotEmptyVersion() {
        return !isEmptyVersion();
    }

}