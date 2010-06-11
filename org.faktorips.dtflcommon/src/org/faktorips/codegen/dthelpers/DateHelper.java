/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

    /**
     * @param datatype
     */
    public DateHelper(DateDatatype datatype) {
        super(datatype);
    }

    /**
     * Overridden Method.
     * 
     * @see org.faktorips.codegen.dthelpers.AbstractDatatypeHelper#valueOfExpression(java.lang.String)
     */
    @Override
    protected JavaCodeFragment valueOfExpression(String expression) {
        if (StringUtils.isEmpty(expression)) {
            return nullExpression();
        }
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(DateUtil.class);
        fragment.append(".parseIsoDateStringToDate(");
        fragment.append(expression);
        fragment.append(')');
        return fragment;
    }

    /**
     * Overridden Method.
     * 
     * @see org.faktorips.codegen.DatatypeHelper#nullExpression()
     */
    public JavaCodeFragment nullExpression() {
        return new JavaCodeFragment("null");
    }

    /**
     * Overridden Method.
     * 
     * @see org.faktorips.codegen.DatatypeHelper#newInstance(java.lang.String)
     */
    public JavaCodeFragment newInstance(String value) {
        if (value == null) {
            return valueOfExpression(value);
        }
        StringBuffer buf = new StringBuffer();
        buf.append('"').append(value).append('"');
        return valueOfExpression(buf.toString());
    }

    /**
     * Overridden Method.
     * 
     * @see org.faktorips.codegen.DatatypeHelper#getRangeJavaClassName()
     */
    @Override
    public String getRangeJavaClassName(boolean useTypesafeCollections) {
        return Java5ClassNames.DefaultRange_QualifiedName + "<" + Date.class.getName() + ">";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JavaCodeFragment newRangeInstance(JavaCodeFragment lowerBoundExp,
            JavaCodeFragment upperBoundExp,
            JavaCodeFragment stepExp,
            JavaCodeFragment containsNullExp,
            boolean useTypesafeCollections) {
        JavaCodeFragment frag = new JavaCodeFragment();
        frag.appendClassName(getRangeJavaClassName(useTypesafeCollections));
        frag.append(".valueOf(");
        frag.append(lowerBoundExp);
        frag.append(", ");
        frag.append(upperBoundExp);
        frag.append(", ");
        frag.append(stepExp);
        frag.append(", ");
        frag.append(containsNullExp);
        frag.append(")");
        return frag;
    }
}
