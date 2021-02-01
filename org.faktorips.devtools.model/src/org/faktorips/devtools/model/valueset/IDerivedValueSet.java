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
 * Represents a value set that is implemented in code. At design time, it behaves mostly like a
 * {@link IUnrestrictedValueSet} including null.
 * 
 * @see ValueDatatype
 * 
 */
public interface IDerivedValueSet extends IValueSet {

    // No specific methods so far.
}
