/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.datatype.classtypes;

import org.faktorips.datatype.ValueClassNameDatatype;

/**
 * Datatype for <code>Boolean</code>.
 * 
 * @author Jan Ortmann
 */
public class BooleanDatatype extends ValueClassNameDatatype {

    public BooleanDatatype() {
        super(Boolean.class.getSimpleName());
    }

    public BooleanDatatype(String name) {
        super(name);
    }

    @Override
    public Object getValue(String s) {
        if (s == null) {
            return null;
        }
        if ("false".equalsIgnoreCase(s)) { //$NON-NLS-1$
            return Boolean.FALSE;
        }
        if ("true".equalsIgnoreCase(s)) { //$NON-NLS-1$
            return Boolean.TRUE;
        }
        throw new IllegalArgumentException("Can't parse " + s + " to Boolean!"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public boolean supportsCompare() {
        return true;
    }

}
