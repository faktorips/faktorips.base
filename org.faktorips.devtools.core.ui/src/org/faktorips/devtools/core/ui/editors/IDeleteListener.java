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

package org.faktorips.devtools.core.ui.editors;

import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;

/**
 * Interface for listeners which want to be notified on ips object part deletion.
 * 
 * @author Thorsten Guenther
 */
public interface IDeleteListener {

    /**
     * Called before the method <code>IIpsObjectPart.delete()</code> is called. The method have to
     * return true otherwise the deletion will be interrupted
     * 
     * @param part The part that will be deleted.
     * @return true to continue deletion or false to interrupt
     */
    public boolean aboutToDelete(IIpsObjectPart part);

    /**
     * Called after the part was deleted.
     * 
     * @param part The deleted part.
     */
    public void deleted(IIpsObjectPart part);

}
