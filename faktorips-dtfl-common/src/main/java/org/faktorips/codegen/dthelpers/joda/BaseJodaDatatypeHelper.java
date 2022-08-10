/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.codegen.dthelpers.joda;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.dthelpers.AbstractTimeHelper;
import org.faktorips.datatype.Datatype;
import org.faktorips.values.ObjectUtil;

/**
 * Base class for Joda-Time {@link DatatypeHelper} implementations
 */
public class BaseJodaDatatypeHelper extends AbstractTimeHelper {

    public static final String ORG_FAKTORIPS_UTIL_JODA_UTIL = "org.faktorips.valuetypes.joda.util.JodaUtil"; //$NON-NLS-1$
    protected static final String ORG_JODA_TIME_FORMAT_ISO_DATE_TIME_FORMAT = "org.joda.time.format.ISODateTimeFormat"; //$NON-NLS-1$
    private final String parseMethod;
    private final String className;

    public BaseJodaDatatypeHelper(Datatype datatype, String className, String parseMethod) {
        super(datatype);
        this.className = className;
        this.parseMethod = parseMethod;
    }

    public BaseJodaDatatypeHelper(String className, String parseMethod) {
        this.className = className;
        this.parseMethod = parseMethod;
    }

    @Override
    public String getJavaClassName() {
        return className;
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

    @Override
    public JavaCodeFragment getToStringExpression(String fieldName) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(ObjectUtil.class).append(".isNull(").append(fieldName) //$NON-NLS-1$
                .append(")").append(" ? null : ") //$NON-NLS-1$ //$NON-NLS-2$
                .append(fieldName).append(".toString("); //$NON-NLS-1$
        appendToStringParameter(fragment);
        fragment.append(")"); //$NON-NLS-1$
        return fragment;
    }

    /**
     * Allows subclasses to add parameters to the toString() method.
     * 
     * @param fragment the fragment to append to
     */
    protected void appendToStringParameter(JavaCodeFragment fragment) {
        // default: no parameter
    }

    @Override
    public JavaCodeFragment nullExpression() {
        return new JavaCodeFragment("null"); //$NON-NLS-1$
    }

}
