/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.abstraction.eclipse;

import org.faktorips.devtools.abstraction.AVersion;
import org.osgi.framework.Version;

public class OsgiVersions {

    private OsgiVersions() {
        // util
    }

    /**
     * Converts the given OSGi-{@link Version} to {@link AVersion}.
     */
    public static final AVersion toAVersion(Version osgiVersion) {
        return AVersion.parse(osgiVersion.toString());
    }
}
