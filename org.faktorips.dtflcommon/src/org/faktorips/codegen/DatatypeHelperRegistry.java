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
