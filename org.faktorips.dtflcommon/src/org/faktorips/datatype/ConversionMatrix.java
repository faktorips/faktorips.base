/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.datatype;

/**
 * A <code>ConversionMatric</code> holds the information if the value of a datatype can be converted
 * into the value of another datatype.
 */
public interface ConversionMatrix {

    /**
     * Returns true if a value of datatype from can be converted into one of datatype to. If
     * datatype from and to are equal, the method returns true.
     * 
     * @throws IllegalArgumentException if either from or to is null.
     */
    public boolean canConvert(Datatype from, Datatype to);

}
