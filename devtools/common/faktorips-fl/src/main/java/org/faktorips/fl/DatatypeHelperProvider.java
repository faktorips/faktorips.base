/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.fl;

import org.faktorips.codegen.BaseDatatypeHelper;
import org.faktorips.codegen.CodeFragment;
import org.faktorips.datatype.Datatype;

/**
 * Provides code generation helpers for the datatypes.
 * 
 * @author Jan Ortmann
 */
public interface DatatypeHelperProvider<T extends CodeFragment> {

    /**
     * Returns the code generation helper for the given datatype or <code>null</code> if either
     * datatype is <code>null</code> or the provide can't provide a helper.
     */
    BaseDatatypeHelper<T> getDatatypeHelper(Datatype datatype);

}
