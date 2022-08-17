/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.persistence;

/**
 * Persistence provider for standard generic Jakarta Persistence 3.0 support
 */
public class Jakarta3_0PersistenceProvider extends Jakarta2_2PersistenceProvider {

    public static final String PACKAGE_PREFIX_JAKARTA_PERSISTENCE = "jakarta.persistence.";

    @Override
    public String getPackagePrefix() {
        return PACKAGE_PREFIX_JAKARTA_PERSISTENCE;
    }

}
