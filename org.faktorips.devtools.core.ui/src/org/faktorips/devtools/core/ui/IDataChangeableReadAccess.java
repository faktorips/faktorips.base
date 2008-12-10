/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

/**
 * An interface that marks an user interface component (control, editor, editor page)
 * as being able to tell, if the data shown can be modified by the user or not.
 * 
 * @author Jan Ortmann
 */
public interface IDataChangeableReadAccess {

    /**
     * Returns <code>true</code> if the data shown in this user interface component can
     * be changed, otherwise <code>false</code>.
     */
    public boolean isDataChangeable();
    

}
