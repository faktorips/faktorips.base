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

import org.apache.commons.lang.StringUtils;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.classtypes.DecimalDatatype;
import org.faktorips.values.Decimal;
import org.faktorips.valueset.DecimalRange;

/**
 * DatatypeHelper for datatype Decimal.
 */
public class DecimalHelper extends AbstractDatatypeHelper {

    /**
     * Constructs a new helper.
     */
    public DecimalHelper() {
        super();
    }

    /**
     * Constructs a new helper for the given decimal datatype.
     * 
     * @throws IllegalArgumentException if datatype is <code>null</code>.
     */
    public DecimalHelper(DecimalDatatype datatype) {
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
        fragment.appendClassName(Decimal.class);
        fragment.append(".valueOf(");
        fragment.appendQuoted(value);
        fragment.append(')');
        return fragment;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.faktorips.codegen.dthelpers.AbstractDatatypeHelper#valueOfExpression(java.lang.String)
     */
    @Override
    protected JavaCodeFragment valueOfExpression(String expression) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(Decimal.class);
        fragment.append(".valueOf(");
        fragment.append(expression);
        fragment.append(')');
        return fragment;
    }

    /**
     * Methode der Oberklasse wird ueberschrieben, weil bei diesem Datentyp valueOf-Methode selbst
     * Null-Expression zurueckgeben kann
     */
    @Override
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
        fragment.appendClassName(Decimal.class);
        fragment.append(".NULL");
        return fragment;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.faktorips.codegen.DatatypeHelper#getRangeJavaClassName()
     */
    @Override
    public String getRangeJavaClassName(boolean useTypesafeCollections) {
        return DecimalRange.class.getName();
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
