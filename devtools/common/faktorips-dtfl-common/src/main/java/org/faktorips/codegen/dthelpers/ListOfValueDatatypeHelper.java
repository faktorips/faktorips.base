/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.codegen.dthelpers;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.codegen.PrimitiveDatatypeHelper;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ListOfTypeDatatype;
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

    private final DatatypeHelper elementDatatypeHelper;

    public ListOfValueDatatypeHelper(DatatypeHelper elementDatatypeHelper) {
        super(new ListOfTypeDatatype(elementDatatypeHelper.getDatatype()));
        if (elementDatatypeHelper instanceof PrimitiveDatatypeHelper primitiveDatatypeHelper) {
            this.elementDatatypeHelper = primitiveDatatypeHelper.getWrapperTypeHelper();
        } else {
            this.elementDatatypeHelper = elementDatatypeHelper;
        }
    }

    @Override
    public ListOfTypeDatatype getDatatype() {
        return (ListOfTypeDatatype)super.getDatatype();
    }

    /**
     * Returns the {@link ValueDatatype} of the elements in the list.
     */
    public Datatype getBasicDatatype() {
        return getDatatype().getBasicDatatype();
    }

    public String getBasicJavaClassName() {
        return elementDatatypeHelper.getJavaClassName();
    }

    @Override
    public String getJavaClassName() {
        StringBuilder sb = new StringBuilder(List.class.getName());
        sb.append('<');
        appendBasicJavaName(sb);
        sb.append('>');
        return sb.toString();
    }

    private void appendBasicJavaName(StringBuilder sb) {
        sb.append(elementDatatypeHelper.getJavaClassName());
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
        builder.appendGenerics(""); //$NON-NLS-1$
        builder.appendParameters(new String[] { expression });
        return builder.getFragment();
    }

    /**
     * Returns code that creates a list where the given expression is used as a constructor
     * argument.
     * 
     * {@inheritDoc}
     */
    @Override
    public JavaCodeFragment newInstance(String expression) {
        return valueOfExpression(expression);
    }

    @Override
    public JavaCodeFragment referenceOrSafeCopyIfNeccessary(String expression) {
        return newInstance(expression);
    }

    /**
     * Returns code for a list-variable or -argument declarations. e.g.
     * {@code List<ElementJavaClassName>}.
     */
    public JavaCodeFragment getDeclarationJavaTypeFragment() {
        JavaCodeFragmentBuilder builder = new JavaCodeFragmentBuilder();
        builder.appendClassName(List.class);
        builder.appendGenerics(getBasicJavaClassName());
        return builder.getFragment();
    }

}
