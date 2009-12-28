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

package org.faktorips.devtools.core.ui.wizards.refactor;

import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.refactor.LocationDescriptor;

/**
 * The <tt>RenamePage</tt> provides a text field that allows the user to type a new name for
 * <tt>IIpsElement</tt> to rename.
 * 
 * @author Alexander Weickmann
 */
class RenamePage extends IpsRenameMovePage {

    /**
     * Creates the <tt>RenamePage</tt>.
     * 
     * @param ipsElement The <tt>IIpsElement</tt> to be renamed.
     */
    RenamePage(IIpsElement ipsElement) {
        super(ipsElement, "RenamePage");
    }

    @Override
    protected void setPromptMessage() {
        setMessage(NLS.bind(Messages.RenamePage_message, getIpsElementName(), getIpsElement().getName()));
    }

    @Override
    protected LocationDescriptor getTargetLocationFromUserInput() {
        return new LocationDescriptor(getOriginalLocation().getIpsPackageFragment(), getUserInputNewName());
    }

}