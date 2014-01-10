/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.codegen;

import java.util.HashMap;
import java.util.Map;

import org.faktorips.datatype.Datatype;

/**
 * A default DatatypeHelperRegistry.
 */
public class DefaultDatatypeHelperRegistry implements DatatypeHelperRegistry {

    private Map<Datatype, DatatypeHelper> helpers = new HashMap<Datatype, DatatypeHelper>(20);

    /**
     * Returns a new empty registry.
     */
    public final static DatatypeHelperRegistry newEmptyRegistry() {
        return new DefaultDatatypeHelperRegistry();
    }

    /**
     * {@inheritDoc}
     */
    public DatatypeHelper getHelper(Datatype datatype) {
        return helpers.get(datatype);
    }

    /**
     * {@inheritDoc}
     */
    public void register(DatatypeHelper helper) {
        helpers.put(helper.getDatatype(), helper);
    }

}
