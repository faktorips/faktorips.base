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
 * Persistence provider for EclipseLink 3.0 support
 */
public class EclipseLink3_0PersistenceProvider extends EclipseLink25PersistenceProvider {

    @Override
    public String getPackagePrefix() {
        return Jakarta3_0PersistenceProvider.PACKAGE_PREFIX_JAKARTA_PERSISTENCE;
    }

}
