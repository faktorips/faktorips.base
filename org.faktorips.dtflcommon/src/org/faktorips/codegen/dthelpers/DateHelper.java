/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.codegen.dthelpers;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.classtypes.DateDatatype;
import org.faktorips.values.DateUtil;

/**
 * 
 * @author Peter Erzberger
 */
public class DateHelper extends AbstractDatatypeHelper {

    /**
     * Constructs a new helper.
     */
    public DateHelper() {
        super();
    }

    public DateHelper(DateDatatype datatype) {
        super(datatype);
    }

    @Override
    protected JavaCodeFragment valueOfExpression(String expression) {
        if (StringUtils.isEmpty(expression)) {
            return nullExpression();
        }
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(DateUtil.class);
        fragment.append(".parseIsoDateStringToDate("); //$NON-NLS-1$
        fragment.append(expression);
        fragment.append(')');
        return fragment;
    }

    public JavaCodeFragment nullExpression() {
        return new JavaCodeFragment("null"); //$NON-NLS-1$
    }

    public JavaCodeFragment newInstance(String value) {
        if (value == null) {
            return valueOfExpression(value);
        }
        StringBuffer buf = new StringBuffer();
        buf.append('"').append(value).append('"');
        return valueOfExpression(buf.toString());
    }

    @Override
    public String getRangeJavaClassName(boolean useTypesafeCollections) {
        return Java5ClassNames.DefaultRange_QualifiedName + "<" + Date.class.getName() + ">"; //$NON-NLS-1$//$NON-NLS-2$
    }

    @Override
    public JavaCodeFragment newRangeInstance(JavaCodeFragment lowerBoundExp,
            JavaCodeFragment upperBoundExp,
            JavaCodeFragment stepExp,
            JavaCodeFragment containsNullExp,
            boolean useTypesafeCollections) {

        JavaCodeFragment frag = new JavaCodeFragment();
        frag.appendClassName(getRangeJavaClassName(useTypesafeCollections));
        frag.append(".valueOf("); //$NON-NLS-1$
        frag.append(lowerBoundExp);
        frag.append(", "); //$NON-NLS-1$
        frag.append(upperBoundExp);
        frag.append(", "); //$NON-NLS-1$
        frag.append(stepExp);
        frag.append(", "); //$NON-NLS-1$
        frag.append(containsNullExp);
        frag.append(")"); //$NON-NLS-1$
        return frag;
    }

}
