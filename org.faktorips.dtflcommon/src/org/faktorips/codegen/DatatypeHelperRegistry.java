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

import org.faktorips.datatype.Datatype;

/**
 * A registry for DatatypeHelper.
 */
public interface DatatypeHelperRegistry {

    /**
     * Returns the helper for the indicated datatype. Returns null if no helper is registered for
     * the datatype.
     */
    public DatatypeHelper getHelper(Datatype datatype);

    /**
     * Registers the datatype helper.
     * 
     * @throws IllegalArgumentException if helper is null.
     */
    public void register(DatatypeHelper helper);
}
