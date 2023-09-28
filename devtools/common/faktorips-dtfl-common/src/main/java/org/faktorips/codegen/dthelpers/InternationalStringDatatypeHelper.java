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

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.InternationalStringDatatype;
import org.faktorips.values.DefaultInternationalString;
import org.faktorips.values.InternationalString;

public class InternationalStringDatatypeHelper extends AbstractDatatypeHelper {

    private boolean useInterface;

    public InternationalStringDatatypeHelper(boolean useInterface) {
        super(new InternationalStringDatatype());
        this.useInterface = useInterface;
    }

    @Override
    public Datatype getDatatype() {
        return super.getDatatype();
    }

    @Override
    public String getJavaClassName() {
        if (useInterface) {
            return InternationalString.class.getName();
        } else {
            return DefaultInternationalString.class.getName();
        }
    }

    @Override
    public void setDatatype(Datatype datatype) {
        // do nothing
    }

    @Override
    public JavaCodeFragment newInstance(String value) {
        return valueOfExpression(value);
    }

    /**
     * {@inheritDoc}
     * <p>
     * In the implementation of {@link InternationalStringDatatypeHelper} we ignore the checkForNull
     * because international string variables should never be null at all.
     */
    @Override
    public JavaCodeFragment newInstanceFromExpression(String expression, boolean checkForNull) {
        return valueOfExpression(expression);
    }

    @Override
    protected JavaCodeFragment valueOfExpression(String valueExpression) {
        if (valueExpression == null) {
            return new JavaCodeFragment().appendClassName(DefaultInternationalString.class).append(".EMPTY");
        }
        return new JavaCodeFragment(valueExpression);
    }

    /**
     * {@inheritDoc}
     * <p>
     * For international string we do not use the string representation! We simply return the field
     * name because every use of the international string should handle the type directly. This is a
     * big difference to all other datatypes but is necessary to read and write international
     * strings in a proper format.
     */
    @Override
    public JavaCodeFragment getToStringExpression(String fieldName) {
        return new JavaCodeFragment(fieldName);
    }

}
