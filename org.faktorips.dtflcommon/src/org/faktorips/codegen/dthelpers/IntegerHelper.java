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
import org.faktorips.datatype.classtypes.IntegerDatatype;
import org.faktorips.valueset.IntegerRange;


/**
 * DatatypeHelper for datatype Integer. 
 */
public class IntegerHelper extends AbstractDatatypeHelper {

    /**
     * Constructs a new helper.
     */
    public IntegerHelper() {
        super();
    }

    /**
     * Constructs a new helper for the given integer datatype.
     * 
     * @throws IllegalArgumentException if datatype is <code>null</code>.
     */
    public IntegerHelper(IntegerDatatype datatype) {
        super(datatype);
    }
    
    /**
     * {@inheritDoc}
     */
    public JavaCodeFragment newInstance(String value) {
        if (StringUtils.isEmpty(value)) {
        	return nullExpression();
        }
        JavaCodeFragment fragment = new JavaCodeFragment();        
        fragment.append("new ");
        fragment.appendClassName(Integer.class);
        fragment.append('(');
        fragment.append(value);
        fragment.append(')');
        return fragment;
    }

    /**
     * {@inheritDoc}
     */
	protected JavaCodeFragment valueOfExpression(String expression) {
        return newInstance(expression);
    }

    /**
     * {@inheritDoc}
     */
	public JavaCodeFragment nullExpression() {
		return new JavaCodeFragment("null");
	}

    /**
     * {@inheritDoc}
     */
    public String getRangeJavaClassName() {
        return IntegerRange.class.getName();
    }
    
    /**
     * {@inheritDoc}
     */
    public JavaCodeFragment newRangeInstance(String lowerBound, String upperBound, String step, String containsNull) {
        JavaCodeFragment frag = new JavaCodeFragment();
        frag.appendClassName(getRangeJavaClassName());
        frag.append(".valueOf(");
        frag.append(lowerBound);
        frag.append(", ");
        frag.append(upperBound);
        frag.append(", ");
        frag.append(step);
        frag.append(", ");
        frag.append(containsNull);
        frag.append(")");
        return frag;
    }

}
