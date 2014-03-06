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

/**
 * This class implements {@link IVersion} in order to represent a Version adequately.
 */
public class Version implements IVersion {
    private String versionAsString;

    public Version(String versionAsString) {
        this.versionAsString = versionAsString;
    }

    @Override
    public String asString() {
        return versionAsString;
    }
}
