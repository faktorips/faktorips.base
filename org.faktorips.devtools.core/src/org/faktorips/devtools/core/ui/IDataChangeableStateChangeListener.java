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
 * A listener for state changes of the data changeable property.
 * 
 * @see org.faktorips.devtools.core.ui.ISwitchDataChangeableSupport
 * 
 * @author Jan Ortmann
 */
public interface IDataChangeableStateChangeListener {

    /**
     * Called when the given object's data changeable state has changed.
     */
    public void dataChangeableStateHasChanged(ISwitchDataChangeableWithListenerSupport object);
}
