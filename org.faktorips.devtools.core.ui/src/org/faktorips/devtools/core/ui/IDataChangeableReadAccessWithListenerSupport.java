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
 * Extension of the SwitchDataChangeableSupport with additional listener
 * support.
 * 
 * @author Jan Ortmann
 */
public interface IDataChangeableReadAccessWithListenerSupport extends IDataChangeableReadAccess {

    /**
     * Adds the listener.
     */
    public void addDataChangeableStateChangeListener(IDataChangeableStateChangeListener listener);

    /**
     * Removes the listener.
     */
    public void removeDataChangeableStateChangeListener(IDataChangeableStateChangeListener listener);

}
