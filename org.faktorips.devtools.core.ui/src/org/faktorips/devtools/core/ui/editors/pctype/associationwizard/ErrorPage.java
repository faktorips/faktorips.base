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

    /**
     * {@inheritDoc}
     */
    public void createControl(Composite parent) {
        details = toolkit.createLabel(parent, ""); //$NON-NLS-1$
        setControl(details);
    }

    /**
     * If the current page this page then return false to disable the finish button. {@inheritDoc}
     */
    @Override
    public boolean isPageComplete() {
        return !(getWizard().getContainer().getCurrentPage() == this);
    }

    /**
     * This method returns null to disable the next button. {@inheritDoc}
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

    /**
     * {@inheritDoc}
     */
    public boolean isPageVisible() {
        return false;
    }
}
