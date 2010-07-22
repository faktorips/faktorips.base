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

    public JavaCodeFragment newInstance(String value) {
        if (StringUtils.isEmpty(value)) {
            return nullExpression();
        }
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(Money.class);
        fragment.append(".valueOf("); //$NON-NLS-1$
        fragment.appendQuoted(value);
        fragment.append(')');
        return fragment;
    }

    @Override
    protected JavaCodeFragment valueOfExpression(String expression) {
        if (StringUtils.isEmpty(expression)) {
            return nullExpression();
        }
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(Money.class);
        fragment.append(".valueOf("); //$NON-NLS-1$
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

    public JavaCodeFragment nullExpression() {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(Money.class);
        fragment.append(".NULL"); //$NON-NLS-1$
        return fragment;
    }

    @Override
    public String getRangeJavaClassName(boolean useTypesafeCollections) {
        return MoneyRange.class.getName();
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
