/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.pctype.associationwizard;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.faktorips.devtools.core.ui.UIToolkit;

public class ErrorPage extends WizardPage implements IHiddenWizardPage{
    private static final String PAGE_ID = "Error"; //$NON-NLS-1$
    
    private Label details;

    private UIToolkit toolkit;
    
    public ErrorPage(UIToolkit toolkit) {
        super(PAGE_ID, "Error", null);
        setDescription("An internal error occured. See the log file for details.");
        this.toolkit = toolkit;
        setPageComplete(true);
    }

    /**
     * {@inheritDoc}
     */
    public void createControl(Composite parent) {
        details = toolkit.createLabel(parent, ""); //$NON-NLS-1$
        setControl(details);
    }

    /**
     * If the current page this page then return false to disable the finish button.
     * {@inheritDoc}
     */
    public boolean isPageComplete() {
        return ! (getWizard().getContainer().getCurrentPage() == this);
    }

    /**
     * This method returns null to disable the next button.
     * {@inheritDoc}
     */
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

    public boolean isPageVisible() {
        return false;
    }
}
