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

package org.faktorips.devtools.core.ui;

/**
 * An interface that marks an user interface component (control, editor, editor page) as being able
 * to tell, if the data shown can be modified by the user or not.
 * 
 * @author Jan Ortmann
 */
public interface IDataChangeableReadAccess {

    /**
     * Returns <code>true</code> if the data shown in this user interface component can be changed,
     * otherwise <code>false</code>.
     */
    public boolean isDataChangeable();

}
