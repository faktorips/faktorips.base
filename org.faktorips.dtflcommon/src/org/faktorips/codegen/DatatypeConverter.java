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

package org.faktorips.codegen;

import org.faktorips.datatype.ConversionMatrix;
import org.faktorips.datatype.Datatype;

/**
 *
 */
public class DatatypeConverter implements ConversionMatrix {

    /**
     * 
     */
    public DatatypeConverter() {
        super();
        // TODO Auto-generated constructor stub
    }

    /** 
     * Overridden method.
     * @see org.faktorips.datatype.ConversionMatrix#canConvert(org.faktorips.datatype.Datatype, org.faktorips.datatype.Datatype)
     */
    public boolean canConvert(Datatype from, Datatype to) {
        // TODO Auto-generated method stub
        return false;
    }

}
