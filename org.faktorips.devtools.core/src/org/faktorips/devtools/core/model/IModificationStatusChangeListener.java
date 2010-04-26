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
