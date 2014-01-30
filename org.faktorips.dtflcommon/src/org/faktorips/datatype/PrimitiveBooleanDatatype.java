/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.datatype;

/**
 * Datatype for the primitive <code>boolean</code>.
 */
public class PrimitiveBooleanDatatype extends AbstractPrimitiveDatatype {

    public String getName() {
        return "boolean"; //$NON-NLS-1$
    }

    public String getQualifiedName() {
        return "boolean"; //$NON-NLS-1$
    }

    public String getDefaultValue() {
        return Boolean.FALSE.toString();
    }

    public ValueDatatype getWrapperType() {
        return Datatype.BOOLEAN;
    }

    public String getJavaClassName() {
        return "boolean"; //$NON-NLS-1$
    }

    @Override
    public Object getValue(String value) {
        return Boolean.valueOf(value);
    }

    public boolean supportsCompare() {
        return false;
    }

}
