/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.valueset;

import org.faktorips.datatype.ValueDatatype;

/**
 * Represents a value set that does not restrict the values defined by a value data type. A special
 * case is the <code>null</code> value. Although the value set is unrestricted, it has the ability
 * to include or exclude <code>null</code> explicitly. That is necessary because of the special
 * meaning of <code>null</code> in the business context. By creating an unrestricted value set
 * excluding <code>null</code> it is possible to force the user to select a concrete value.
 * 
 * @see ValueDatatype
 * 
 */
public interface IUnrestrictedValueSet extends IValueSet {

    // No specific methods so far.
}
