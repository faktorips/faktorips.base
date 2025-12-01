/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.datatype;

/**
 * Datatype for the primitive <code>boolean</code>.
 */
public class PrimitiveBooleanDatatype extends AbstractPrimitiveDatatype {

    @Override
    public String getName() {
        return "boolean"; //$NON-NLS-1$
    }

    @Override
    public String getQualifiedName() {
        return "boolean"; //$NON-NLS-1$
    }

    @Override
    public String getDefaultValue() {
        return Boolean.FALSE.toString();
    }

    @Override
    public ValueDatatype getWrapperType() {
        return Datatype.BOOLEAN;
    }

    @Override
    public Object getValue(String value) {
        if ("true".equals(value)) {
            return Boolean.TRUE;
        } else if ("false".equals(value)) {
            return Boolean.FALSE;
        } else {
            throw new IllegalArgumentException("Invalid boolean value: " + value);
        }
    }

    @Override
    public boolean supportsCompare() {
        return false;
    }

}
