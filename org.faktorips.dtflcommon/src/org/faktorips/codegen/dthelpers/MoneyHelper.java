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
import org.faktorips.datatype.classtypes.MoneyDatatype;
import org.faktorips.values.Money;
import org.faktorips.valueset.MoneyRange;

/**
 * DatatypeHelper for datatype Money.
 */
public class MoneyHelper extends AbstractDatatypeHelper {

    /**
     * Constructs a new helper.
     */
    public MoneyHelper() {
        super();
    }

    /**
     * Constructs a new helper for the given money datatype.
     * 
     * @throws IllegalArgumentException if datatype is <code>null</code>.
     */
    public MoneyHelper(MoneyDatatype datatype) {
        super(datatype);
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.codegen.DatatypeHelper#newInstance(java.lang.String)
     */
    public JavaCodeFragment newInstance(String value) {
        if (StringUtils.isEmpty(value)) {
            return nullExpression();
        }
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(Money.class);
        fragment.append(".valueOf(");
        fragment.appendQuoted(value);
        fragment.append(')');
        return fragment;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.faktorips.codegen.dthelpers.AbstractDatatypeHelper#valueOfExpression(java.lang.String)
     */
    protected JavaCodeFragment valueOfExpression(String expression) {
        if (StringUtils.isEmpty(expression)) {
            return nullExpression();
        }
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(Money.class);
        fragment.append(".valueOf(");
        fragment.append(expression);
        fragment.append(')');
        return fragment;
    }

    /**
     * Methode der Oberklasse wird ueberschrieben, weil bei diesem Datentyp valueOf-Methode selbst
     * Null-Expression zurueckgeben kann
     */
    public JavaCodeFragment newInstanceFromExpression(String expression) {
        return valueOfExpression(expression);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.faktorips.codegen.DatatypeHelper#nullExpression()
     */
    public JavaCodeFragment nullExpression() {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(Money.class);
        fragment.append(".NULL");
        return fragment;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.faktorips.codegen.DatatypeHelper#getRangeJavaClassName()
     */
    public String getRangeJavaClassName(boolean useTypesafeCollections) {
        if (useTypesafeCollections) {
            return Java5ClassNames.MoneyRange_QualifiedName;
        } else {
            return MoneyRange.class.getName();
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
