/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
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
	 * Called before the method <code>IIpsObjectPart.delete()</code> is called.
	 * Note that no veto is possible.
	 * 
	 * @param part The part that will be deleted.
	 */
	public void aboutToDelete(IIpsObjectPart part);
	
	/**
	 * Called after the part was deleted.
	 * 
	 * @param part The deleted part.
	 */
	public void deleted(IIpsObjectPart part);
}
