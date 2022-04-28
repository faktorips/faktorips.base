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

import org.faktorips.devtools.model.builder.IPersistenceProvider;

public abstract class AbstractPersistenceProvider implements IPersistenceProvider {

    public abstract String getPackagePrefix();

    @Override
    public String getQualifiedName(PersistenceClass persistenceClass) {
        return getPackagePrefix() + persistenceClass.toString();
    }

}
