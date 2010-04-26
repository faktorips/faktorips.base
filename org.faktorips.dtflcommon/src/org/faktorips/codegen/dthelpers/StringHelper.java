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

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.classtypes.StringDatatype;

/**
 *  
 */
public class StringHelper extends AbstractDatatypeHelper {

    /**
     * Constructs a new helper for the string datatype.
     */
    public StringHelper() {
        super();
    }

    /**
     * Constructs a new helper for the given string datatype.
     * 
     * @throws IllegalArgumentException if datatype is <code>null</code>.
     */
    public StringHelper(StringDatatype datatype) {
        super(datatype);
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.codegen.DatatypeHelper#newInstance(java.lang.String)
     */
    public JavaCodeFragment newInstance(String value) {
        if (value == null) {
            return nullExpression();
        }
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendQuoted(StringEscapeUtils.escapeJava(value));
        return fragment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JavaCodeFragment newInstanceFromExpression(String expression) {
        return valueOfExpression(expression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JavaCodeFragment valueOfExpression(String expression) {
        if (StringUtils.isEmpty(expression)) {
            return nullExpression();
        }
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.append(expression);
        return fragment;
    }

    /**
     * {@inheritDoc}
     */
    public JavaCodeFragment nullExpression() {
        return new JavaCodeFragment("null");
    }

}
