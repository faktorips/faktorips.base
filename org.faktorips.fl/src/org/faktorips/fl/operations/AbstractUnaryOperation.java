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

package org.faktorips.fl.operations;

import org.faktorips.codegen.CodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.UnaryOperation;

/**
 * Abstract implementation of {@link UnaryOperation}.
 * 
 * @param <T> a {@link CodeFragment} implementation for a specific target language
 */
public abstract class AbstractUnaryOperation<T extends CodeFragment> implements UnaryOperation<T> {

    private Datatype datatype;
    private String operator;

    /**
     * Creates a new unary operation for the indicated operator and {@link Datatype data type}.
     */
    public AbstractUnaryOperation(Datatype datatype, String operator) {
        this.datatype = datatype;
        this.operator = operator;
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.UnaryOperation#getDatatype()
     */
    public Datatype getDatatype() {
        return datatype;
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.UnaryOperation#getOperator()
     */
    public String getOperator() {
        return operator;
    }

}
