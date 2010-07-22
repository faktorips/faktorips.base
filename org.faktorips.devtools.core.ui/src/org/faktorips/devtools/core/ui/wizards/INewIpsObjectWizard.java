/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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
     * Returns the type of the object created by the wizard.
     */
    public IpsObjectType getIpsObjectType();
}
