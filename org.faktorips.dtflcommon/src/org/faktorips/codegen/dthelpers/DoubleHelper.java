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

import org.apache.commons.lang.StringUtils;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.classtypes.DoubleDatatype;
import org.faktorips.valueset.DoubleRange;

/**
 * 
 * @author Jan Ortmann
 */
public class DoubleHelper extends AbstractDatatypeHelper {

    /**
     * Constructs a new helper.
     */
    public DoubleHelper() {
        super();
    }

    /**
     * @param datatype
     */
    public DoubleHelper(DoubleDatatype datatype) {
        super(datatype);
    }

    /**
     * Overridden Method.
     *
     * @see org.faktorips.codegen.dthelpers.AbstractDatatypeHelper#valueOfExpression(java.lang.String)
     */
    protected JavaCodeFragment valueOfExpression(String expression) {
        return newInstance(expression);
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
        if (StringUtils.isEmpty(value)) {
        	return nullExpression();
        }
        JavaCodeFragment fragment = new JavaCodeFragment();        
        fragment.append("new ");
        fragment.appendClassName(Double.class);
        fragment.append('(');
        fragment.append(value);
        fragment.append(')');
        return fragment;
    }

    /**
     * {@inheritDoc}
     */
    public String getRangeJavaClassName() {
        return DoubleRange.class.getName();
    }
    
    /**
     * {@inheritDoc}
     */
    public JavaCodeFragment newRangeInstance(JavaCodeFragment lowerBoundExp, JavaCodeFragment upperBoundExp, JavaCodeFragment stepExp, JavaCodeFragment containsNullExp) {
        JavaCodeFragment frag = new JavaCodeFragment();
        frag.appendClassName(getRangeJavaClassName());
        frag.append(".valueOf(");
        frag.append(lowerBoundExp);
        frag.append(", ");
        frag.append(upperBoundExp);
        frag.append(", ");
        frag.append(containsNullExp);
        frag.append(")");
        return frag;
    }

}
