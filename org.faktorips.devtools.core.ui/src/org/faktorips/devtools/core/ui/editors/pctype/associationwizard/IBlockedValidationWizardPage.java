/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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
    List<String> getProperties();

    /**
     * Sets the error message. If the given error message is <code>null</code> then the currently
     * shown error message will be removed
     * 
     * @see DialogPage#setErrorMessage(String)
     */
    void setErrorMessage(String errorMessage);

    /**
     * Sets if the page is complete or not. Note that a non complete pages avoids the wizrad to get
     * finished.
     * 
     * @see WizardPage#setPageComplete(boolean)
     */
    void setPageComplete(boolean complete);
}
