/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.valueset;

public class DerivedValueSet<T> extends UnrestrictedValueSet<T> {

    private static final long serialVersionUID = 1L;

    public DerivedValueSet() {
        super(true);
    }

    @Override
    public String toString() {
        return "DerivedValueSet";
    }
}
