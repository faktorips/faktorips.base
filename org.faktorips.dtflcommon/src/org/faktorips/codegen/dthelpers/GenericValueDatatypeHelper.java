/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.codegen.dthelpers;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.GenericValueDatatype;

/**
 * {@link DatatypeHelper} for {@link GenericValueDatatype}.
 */
public class GenericValueDatatypeHelper extends AbstractDatatypeHelper {

    public GenericValueDatatypeHelper(GenericValueDatatype datatype) {
        super(datatype);
    }

    @Override
    public GenericValueDatatype getDatatype() {
        return (GenericValueDatatype)super.getDatatype();
    }

    @Override
    public String getJavaClassName() {
        return getDatatype().getJavaClassName();
    }

    @Override
    protected JavaCodeFragment valueOfExpression(String expression) {
        JavaCodeFragment code = new JavaCodeFragment();
        code.appendClassName(getDatatype().getJavaClassName());
        code.append('.');
        code.append(getDatatype().getValueOfMethodName());
        code.append('(');
        code.append(expression);
        code.append(')');
        return code;
    }

    @Override
    public JavaCodeFragment nullExpression() {
        GenericValueDatatype datatype = getDatatype();
        JavaCodeFragment code = new JavaCodeFragment();
        if (!datatype.hasNullObject()) {
            code.append("null"); //$NON-NLS-1$
            return code;
        }
        code.appendClassName(datatype.getJavaClassName());
        code.append('.');
        code.append(datatype.getValueOfMethodName());
        code.append('(');
        if (datatype.getNullObjectId() == null) {
            code.append("null"); //$NON-NLS-1$
        } else {
            code.appendQuoted(datatype.getNullObjectId());
        }
        code.append(')');
        return code;
    }

    @Override
    public JavaCodeFragment newInstance(String value) {
        if (value == null) {
            return nullExpression();
        }
        return valueOfExpression('"' + value + '"');
    }

    @Override
    public JavaCodeFragment getToStringExpression(String fieldName) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.append(fieldName);
        fragment.append("==null?null:"); //$NON-NLS-1$
        fragment.append(fieldName);
        fragment.append("."); //$NON-NLS-1$
        fragment.append(getDatatype().getToStringMethodName());
        fragment.append("()"); //$NON-NLS-1$
        return fragment;
    }

}
