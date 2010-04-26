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

package org.faktorips.devtools.core.ui;

/**
 * An interface that is implemented by user interface components that allow to switch whether the
 * data shown is changeable or not.
 * 
 * @author Jan Ortmann
 */
public interface IDataChangeableReadWriteAccess extends IDataChangeableReadAccess {

    /**
     * Sets if the data shown in this user interface component can be changed ot not.
     */
    public void setDataChangeable(boolean changeable);

}
