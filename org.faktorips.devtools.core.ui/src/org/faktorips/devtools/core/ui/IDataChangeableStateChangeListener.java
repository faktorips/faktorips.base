/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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
 * A listener for state changes of the data changeable property.
 * 
 * @see org.faktorips.devtools.core.ui.IDataChangeableReadWriteAccess
 * 
 * @author Jan Ortmann
 */
public interface IDataChangeableStateChangeListener {

    /**
     * Called when the given object's data changeable state has changed.
     */
    public void dataChangeableStateHasChanged(IDataChangeableReadAccess object);
}
