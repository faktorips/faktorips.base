/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.codegen.dthelpers;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.ValueDatatype;

/**
 * A helper class for lists of values. There is no data type for lists though. The data type
 * specified in the constructor is the data type of the elements in the list.
 * {@link #valueOfExpression(String)} expects a list as expression and returns code that creates a
 * copy of that list. {@link #newInstance(String)} returns code that creates an empty list. The
 * given value will be ignored.
 * 
 * @author Stefan Widmaier
 */
public class ListOfValueDatatypeHelper extends AbstractDatatypeHelper {

    public ListOfValueDatatypeHelper(ValueDatatype elementDatatype) {
        super(elementDatatype);
    }

    /**
     * Returns the {@link ValueDatatype} of the elements in the list.
     */
    public ValueDatatype getElementDatatype() {
        return (ValueDatatype)getDatatype();
    }

    /**
     * Expects a list as expression and returns code that creates a copy of that list.
     * 
     * {@inheritDoc}
     */
    @Override
    protected JavaCodeFragment valueOfExpression(String expression) {
        JavaCodeFragmentBuilder builder = new JavaCodeFragmentBuilder();
        builder.append("new "); //$NON-NLS-1$
        builder.appendClassName(ArrayList.class);
        builder.appendGenerics(getElementJavaClassName());
        builder.appendParameters(new String[] { expression });
        return builder.getFragment();
    }

    public String getElementJavaClassName() {
        if (getElementDatatype().isPrimitive()) {
            return getElementDatatype().getWrapperType().getJavaClassName();
        } else {
            return getElementDatatype().getJavaClassName();
        }
    }

    /**
     * Returns code that creates a list where the given expression is used as a constructor
     * argument.
     * 
     * {@inheritDoc}
     */
    public JavaCodeFragment newInstance(String expression) {
        return valueOfExpression(expression);
    }

    /**
     * Returns code for a list-variable or -argument declarations. e.g. List &lt
     * ElementJavaClassName &gt
     */
    public JavaCodeFragment getDeclarationJavaTypeFragment() {
        JavaCodeFragmentBuilder builder = new JavaCodeFragmentBuilder();
        builder.appendClassName(List.class);
        builder.appendGenerics(getElementJavaClassName());
        return builder.getFragment();
    }
}
