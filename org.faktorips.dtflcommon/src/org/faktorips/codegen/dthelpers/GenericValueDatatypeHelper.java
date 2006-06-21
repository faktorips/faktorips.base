/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.codegen.dthelpers;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.GenericValueDatatype;

public class GenericValueDatatypeHelper extends AbstractDatatypeHelper {

    public GenericValueDatatypeHelper(GenericValueDatatype datatype) {
        super(datatype);
    }
    
    private GenericValueDatatype getGenericValueDatatype() {
        return (GenericValueDatatype)getDatatype();
    }

    protected JavaCodeFragment valueOfExpression(String expression) {
        JavaCodeFragment code = new JavaCodeFragment();
        code.appendClassName(getGenericValueDatatype().getJavaClassName());
        code.append('.');
        code.append(getGenericValueDatatype().getValueOfMethodName());
        code.append('(');
        code.append(expression);
        code.append(')');
        return code;
    }

    public JavaCodeFragment nullExpression() {
        GenericValueDatatype datatype = getGenericValueDatatype();
        JavaCodeFragment code = new JavaCodeFragment();
        if (!datatype.hasNullObject()) {
            code.append("null");
            return code;
        }
        code.appendClassName(datatype.getJavaClassName());
        code.append('.');
        code.append(datatype.getValueOfMethodName());
        code.append('(');
        code.appendQuoted(datatype.getNullObjectId());
        code.append(')');
        return code;
    }

    public JavaCodeFragment newInstance(String value) {
        if (value==null) {
            return nullExpression();
        }
        return valueOfExpression('"' + value + '"');
    }

}
