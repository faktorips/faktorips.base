/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.codegen.conversion;

import org.faktorips.codegen.SingleConversionCg;
import org.faktorips.datatype.Datatype;


/**
 * A ConversionGenerator that ...
 */
public abstract class AbstractSingleConversionCg implements
        SingleConversionCg {

    private Datatype from;
    private Datatype to;
    
    /**
     * Creates a new ConversionGenerator that converts from Datatype from to 
     * Datatype to.
     */
    public AbstractSingleConversionCg(Datatype from, Datatype to) {
        this.from = from;
        this.to = to;
    }

    /**
     * Overridden method.
     * @see org.faktorips.codegen.SingleConversionCg#getFrom()
     */
    public Datatype getFrom() {
        return from;
    }

    /**
     * Overridden method.
     * @see org.faktorips.codegen.SingleConversionCg#getTo()
     */
    public Datatype getTo() {
        return to;
    }
    
}
