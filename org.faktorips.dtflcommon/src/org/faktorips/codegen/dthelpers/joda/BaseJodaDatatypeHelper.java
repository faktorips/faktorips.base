package org.faktorips.codegen.dthelpers.joda;

import org.apache.commons.lang.StringUtils;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.dthelpers.AbstractDatatypeHelper;

/**
 * Base class for Joda-Time {@link DatatypeHelper} implementations
 */
public class BaseJodaDatatypeHelper extends AbstractDatatypeHelper {

    public static final String ORG_FAKTORIPS_UTIL_JODA_UTIL = "org.faktorips.util.JodaUtil"; //$NON-NLS-1$
    private String parseMethod;

    public BaseJodaDatatypeHelper(String parseMethod) {
        this.parseMethod = parseMethod;
    }

    @Override
    protected JavaCodeFragment valueOfExpression(String expression) {
        JavaCodeFragment code = new JavaCodeFragment();
        code.appendClassName(ORG_FAKTORIPS_UTIL_JODA_UTIL);
        code.append('.');
        code.append(parseMethod);
        code.append('(');
        code.append(expression);
        code.append(')');
        return code;
    }

    public JavaCodeFragment newInstance(String value) {
        if (StringUtils.isEmpty(value)) {
            return nullExpression();
        }
        return valueOfExpression(value);
    }

    @Override
    public JavaCodeFragment nullExpression() {
        return new JavaCodeFragment("null"); //$NON-NLS-1$
    }

}
