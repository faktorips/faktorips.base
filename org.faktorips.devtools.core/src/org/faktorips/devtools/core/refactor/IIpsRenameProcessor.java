/*******************************************************************************
 * Copyright © 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 ******************************************************************************/
package org.faktorips.devtools.core.refactor;

/**
 * An <tt>IIpsRenameProcessor</tt> implements a specific Faktor-IPS "Rename" refactoring.
 * 
 * @author Alexander Weickmann
 */
public interface IIpsRenameProcessor extends IIpsRefactoringProcessor {

    /**
     * Sets the new name for the <tt>IIpsElement</tt> to be refactored.
     * 
     * @param newName A new name for the <tt>IIpsElement</tt> to be refactored.
     * 
     * @throws NullPointerException If <tt>newName</tt> is <tt>null</tt>.
     */
    public void setNewName(String newName);

    /** Returns the element's original name. */
    public String getOriginalName();

    /** Returns the element's new name. */
    public String getNewName();

}
