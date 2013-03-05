/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.InternationalStringDatatype;

public class InternationalStringDatatypeHelper extends AbstractDatatypeHelper {

    public InternationalStringDatatypeHelper() {
        super(new InternationalStringDatatype());
    }

    @Override
    public Datatype getDatatype() {
        return super.getDatatype();
    }

    @Override
    public void setDatatype(Datatype datatype) {
        // do nothing
    }

    public JavaCodeFragment newInstance(String value) {
        return valueOfExpression(value);
    }

    /**
     * {@inheritDoc}
     * <p>
     * In the implementation of {@link InternationalStringDatatypeHelper} we ignore the checkForNull
     * because InternationalString variables should never be null at all.
     */
    @Override
    public JavaCodeFragment newInstanceFromExpression(String expression, boolean checkForNull) {
        return valueOfExpression(expression);
    }

    @Override
    protected JavaCodeFragment valueOfExpression(String valueExpression) {
        JavaCodeFragment javaCodeFragment = new JavaCodeFragment(valueExpression);
        return javaCodeFragment;
    }

    /**
     * {@inheritDoc}
     * <p>
     * For InternationalString we do not use the string representation! We simply return the field
     * name because every use of the international string should handle the type directly. This is a
     * big difference to all other datatypes but is necessary to read and write international
     * strings in a proper format.
     */
    @Override
    public JavaCodeFragment getToStringExpression(String fieldName) {
        return new JavaCodeFragment(fieldName);
    }

}
