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

package org.faktorips.devtools.core.model;

/**
 * A listener for changes to modifcation status changes of ips source files.
 *  
 * @author Jan Ortmann
 */
public interface IModificationStatusChangeListener {

    /**
     * Notifies the listener that the modification status of an ips source file has changed.
     * 
     * @param event The event with the detailed information, is never <code>null</code>.
     */
    public void modificationStatusHasChanged(ModificationStatusChangedEvent event);
}
