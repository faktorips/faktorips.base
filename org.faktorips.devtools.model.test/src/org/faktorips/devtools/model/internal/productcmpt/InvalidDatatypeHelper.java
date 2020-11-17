package org.faktorips.devtools.model.internal.productcmpt;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.dthelpers.AbstractDatatypeHelper;

public class InvalidDatatypeHelper extends AbstractDatatypeHelper {

    public InvalidDatatypeHelper() {
        super(new InvalidDatatype());
    }

    @Override
    protected JavaCodeFragment valueOfExpression(String expression) {
        return null;
    }

    @Override
    public JavaCodeFragment nullExpression() {
        return null;
    }

    @Override
    public JavaCodeFragment newInstance(String value) {
        return null;
    }

    @Override
    public String getJavaClassName() {
        return null;
    }

}