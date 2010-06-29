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

package org.faktorips.devtools.core.ui.editors.pctype.associationwizard;

import java.util.List;

import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;

/**
 * An instance of this interface can block the wizard to go to the next page.<br>
 * Furthermore this interface could be used to ask for properties, which will be validated and
 * displayed as error text to the page message area.
 * 
 * @author Joerg Ortmann
 */
public interface IBlockedValidationWizardPage extends IWizardPage {

    /**
     * Returns all properties wich are edit by this page.
     */
    public List<String> getProperties();

    /**
     * Sets the error message. If the given error message is <code>null</code> then the currently
     * shown error message will be removed
     * 
     * @see DialogPage#setErrorMessage(String)
     */
    public void setErrorMessage(String errorMessage);

    /**
     * Sets if the page is complete or not. Note that a non complete pages avoids the wizrad to get
     * finished.
     * 
     * @see WizardPage#setPageComplete(boolean)
     */
    public void setPageComplete(boolean complete);
}
