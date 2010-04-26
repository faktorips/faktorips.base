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
import org.faktorips.datatype.classtypes.LongDatatype;
import org.faktorips.valueset.LongRange;

/**
 * DatatypeHelper for datatype Long.
 */
public class LongHelper extends AbstractDatatypeHelper {

    /**
     * Constructs a new helper.
     */
    public LongHelper() {
        super();
    }

    /**
     * Constructs a new helper for the given integer datatype.
     * 
     * @throws IllegalArgumentException if datatype is <code>null</code>.
     */
    public LongHelper(LongDatatype datatype) {
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
        fragment.appendClassName(Long.class);
        fragment.append('(');
        fragment.append(value);
        fragment.append(')');
        return fragment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    @Override
    public String getRangeJavaClassName(boolean useTypesafeCollections) {
        return LongRange.class.getName();
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
