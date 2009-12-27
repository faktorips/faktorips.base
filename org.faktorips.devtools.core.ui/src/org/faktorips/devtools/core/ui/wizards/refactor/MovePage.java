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
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.IIpsElement;

/**
 * The one-and-only input page a Faktor-IPS move refactoring needs.
 * 
 * @author Alexander Weickmann
 */
public class MovePage extends IpsRefactoringUserInputPage {

    /**
     * Creates the <tt>MovePage</tt>.
     * 
     * @param ipsElement The <tt>IIpsElement</tt> to be moved.
     */
    MovePage(IIpsElement ipsElement) {
        super(ipsElement, "MovePage");
    }

    @Override
    protected void setPromptMessage() {
        setMessage(NLS.bind(Messages.MovePage_message, getIpsElementName(), getIpsElement().getName()));
    }

    public void createControl(Composite parent) {
        // TODO AW: Finish this.
    }

}
