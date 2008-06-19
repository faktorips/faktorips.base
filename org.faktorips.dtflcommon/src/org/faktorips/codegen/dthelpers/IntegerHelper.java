/***************************************************************************************************
 *  * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.  *  * Alle Rechte vorbehalten.  *  *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,  * Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der  * Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community)  * genutzt werden, die Bestandteil der Auslieferung ist und auch
 * unter  *   http://www.faktorips.org/legal/cl-v01.html  * eingesehen werden kann.  *  *
 * Mitwirkende:  *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de  *  
 **************************************************************************************************/

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
        value = value.trim();
        if (value.charAt(0)!='0') {
            return valueOfExpression(value);
        }
        // if value starts with a leading zero, we must generate Integer.valueOf("08") as
        // new Integer(08) won't compile (try it out!)
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(Integer.class);
        fragment.append(".valueOf(");
        fragment.appendQuoted(value);
        fragment.append(')');
        return fragment;
    }

    /**
     * {@inheritDoc}
     */
    protected JavaCodeFragment valueOfExpression(String expression) {
        if (StringUtils.isEmpty(expression)) {
            return nullExpression();
        }
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.append("new ");
        fragment.appendClassName(Integer.class);
        fragment.append('(');
        fragment.append(expression);
        fragment.append(')');
        return fragment;
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
    public String getRangeJavaClassName(boolean useTypesafeCollections) {
        if (useTypesafeCollections) {
            return Java5ClassNames.IntegerRange_QualifiedName;
        } else {
            return IntegerRange.class.getName();
        }
    }

    /**
     * {@inheritDoc}
     */
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
