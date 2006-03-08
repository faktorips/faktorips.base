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

import org.faktorips.devtools.core.model.IIpsObjectPart;

/**
 * Interface for listeners which want to be notified before an ips object part ist deleted.
 * After all listeners are notified, the deletion will took place, no veto is supported.
 * 
 * @author Thorsten Guenther
 */
public interface IDeleteListener {

	/**
	 * Called before the method <code>IIpsObjectPart.delete()</code> is called.
	 * 
	 * @param part The part that will be deleted.
	 */
	public void aboutToDelete(IIpsObjectPart part);
}
