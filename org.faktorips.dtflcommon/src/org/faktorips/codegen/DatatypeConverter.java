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
    }

    /**
     * {@inheritDoc}
     */
    public boolean canConvert(Datatype from, Datatype to) {
        return false;
    }

}
