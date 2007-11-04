/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) d�rfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung � Version 0.1 (vor Gr�ndung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards;

import org.eclipse.ui.INewWizard;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;

/**
 * Interface for new ips object creating wizards.
 * 
 * @author Joerg Ortmann
 */
public interface INewIpsObjectWizard extends INewWizard {
    
    /**
     * Returns the ips object type of the to be created ips object.
     */
    public IpsObjectType getIpsObjectType();
}
