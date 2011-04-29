/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

import org.faktorips.datatype.Datatype;
import org.faktorips.fl.UnaryOperation;

/**
 *
 */
public abstract class AbstractUnaryOperation implements UnaryOperation {

    private Datatype datatype;
    private String operator;

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
