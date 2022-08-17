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

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.faktorips.devtools.core.ui.UIToolkit;

/**
 * Page shows the error if an exception was thrown.
 * 
 * @author Joerg Ortmann
 */
public class ErrorPage extends WizardPage implements IHiddenWizardPage {

    private static final String PAGE_ID = "Error"; //$NON-NLS-1$

    private Label details;

    private UIToolkit toolkit;

    public ErrorPage(UIToolkit toolkit) {
        super(PAGE_ID, Messages.ErrorPage_pageTitle, null);
        setDescription(Messages.ErrorPage_pageDescription);
        this.toolkit = toolkit;
        setPageComplete(true);
    }

    @Override
    public void createControl(Composite parent) {
        details = toolkit.createLabel(parent, ""); //$NON-NLS-1$
        setControl(details);
    }

    /**
     * If the current page this page then return false to disable the finish button.
     */
    @Override
    public boolean isPageComplete() {
        return !(getWizard().getContainer().getCurrentPage() == this);
    }

    /**
     * This method returns null to disable the next button.
     */
    @Override
    public IWizardPage getPreviousPage() {
        return null;
    }

    /**
     * Sets the details of the error.
     */
    public void storeErrorDetails(String errorDetails) {
        details.setText(errorDetails);
        details.pack();
    }

    @Override
    public boolean isPageVisible() {
        return false;
    }
}
