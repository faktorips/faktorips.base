/***************************************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html eingesehen werden kann.
 * 
 * Mitwirkende:  Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de  
 **************************************************************************************************/

package org.faktorips.devtools.core.model;

import org.eclipse.core.runtime.CoreException;

/**
 * 
 * @author Daniel Hohenberger
 */
public interface IFixDifferencesToModelSupport {

    /**
     * Returns <code>true</code> if this element contains structural differences to its
     * corresponding model element.
     * 
     * @throws CoreException
     */
    public boolean containsDifferenceToModel() throws CoreException;

    /**
     * Fixes all differences between this element and its corresponding model element.
     * The element should be saved before this method returns.
     * 
     * @throws CoreException
     */
    public void fixAllDifferencesToModel() throws CoreException;

}
