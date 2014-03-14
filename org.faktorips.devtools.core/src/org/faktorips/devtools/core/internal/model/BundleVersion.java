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
 * internal representation of the version.
 */
public class BundleVersion implements IVersion<BundleVersion> {

    private final String versionString;

    public BundleVersion(String versionString) {
        ArgumentCheck.notNull(versionString);
        this.versionString = versionString;
    }

    @Override
    public String asString() {
        return versionString;
    }

    @Override
    public int compareTo(BundleVersion version) {
        return new AlphaNumericComparator().compare(versionString, version.versionString);
    }
}