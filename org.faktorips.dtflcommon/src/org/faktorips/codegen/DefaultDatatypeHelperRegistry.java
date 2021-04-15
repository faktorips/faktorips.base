/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.codegen;

import java.util.HashMap;
import java.util.Map;

import org.faktorips.datatype.Datatype;

/**
 * A default DatatypeHelperRegistry.
 */
public class DefaultDatatypeHelperRegistry implements DatatypeHelperRegistry {

    private Map<Datatype, DatatypeHelper> helpers = new HashMap<>(20);

    /**
     * Returns a new empty registry.
     */
    public static final DatatypeHelperRegistry newEmptyRegistry() {
        return new DefaultDatatypeHelperRegistry();
    }

    @Override
    public DatatypeHelper getHelper(Datatype datatype) {
        return helpers.get(datatype);
    }

    @Override
    public void register(DatatypeHelper helper) {
        helpers.put(helper.getDatatype(), helper);
    }

}
