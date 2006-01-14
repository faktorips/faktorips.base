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
        code.appendClassName(getGenericValueDatatype().getAdaptedClass());
        code.append('.');
        code.append(getGenericValueDatatype().getValueOfMethodName());
        code.append('(');
        code.append(expression);
        code.append(')');
        return code;
    }

    public JavaCodeFragment nullExpression() {
        JavaCodeFragment code = new JavaCodeFragment();
        String nullValueId = getGenericValueDatatype().getSpecialNullValue();
        if (nullValueId==null) {
            code.append("null");
            return code;
        }
        code.appendClassName(getGenericValueDatatype().getAdaptedClass());
        code.append('.');
        code.append(getGenericValueDatatype().getValueOfMethodName());
        code.append('(');
        code.appendQuoted(nullValueId);
        code.append(')');
        return code;
    }

    public JavaCodeFragment newInstance(String value) {
        return valueOfExpression('"' + value + '"');
    }

}
