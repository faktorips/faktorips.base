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
import org.faktorips.datatype.classtypes.BooleanDatatype;

public class BooleanHelper extends AbstractDatatypeHelper {

    /**
     * Constructs a new helper.
     */
    public BooleanHelper() {
        super();
    }

    /**
     * Constructs a new helper for the given boolean datatype.
     * 
     * @throws IllegalArgumentException if datatype is <code>null</code>.
     */
    public BooleanHelper(BooleanDatatype datatype) {
        super(datatype);
    }

    public JavaCodeFragment newInstance(String value) {
        if (StringUtils.isEmpty(value)) {
            return nullExpression();
        }
        Boolean booleanValue = Boolean.valueOf(value);
        if (booleanValue.booleanValue()) {
            return new JavaCodeFragment("Boolean.TRUE"); //$NON-NLS-1$
        } else {
            return new JavaCodeFragment("Boolean.FALSE"); //$NON-NLS-1$
        }
    }

    @Override
    protected JavaCodeFragment valueOfExpression(String expression) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(Boolean.class);
        fragment.append(".valueOf("); //$NON-NLS-1$
        fragment.append(expression);
        fragment.append(')');
        return fragment;
    }

    public JavaCodeFragment nullExpression() {
        return new JavaCodeFragment("null"); //$NON-NLS-1$
    }

}
