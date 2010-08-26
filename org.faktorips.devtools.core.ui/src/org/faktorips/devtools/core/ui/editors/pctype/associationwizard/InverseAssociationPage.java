/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.pctype.associationwizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.UIToolkit;

/**
 * Page to choose on of the following options:
 * <ul>
 * <li>create a new inverse association
 * <li>use an existing inverse association
 * <li>create or use no inverse association
 * </ul>
 * 
 * Note that the first option is only visible for association type master to detail. The creation of
 * a detail to master and a master to detail as inverse in one step is not supported.
 * 
 * @author Joerg Ortmann
 */
public class InverseAssociationPage extends WizardPage {

    private NewPcTypeAssociationWizard wizard;
    private UIToolkit toolkit;

    private Button newInverseAssociation;
    private Button useExistingAssociation;
    private Button noInverseAssociation;
    private Button prevSelection;

    /**
     * stores if this page shows the detail to master state means only use existing association is
     * visible
     */
    private boolean detailToMasterState = false;

    public InverseAssociationPage(NewPcTypeAssociationWizard wizard, UIToolkit toolkit) {
        super(Messages.InverseAssociationPage_pageName, Messages.InverseAssociationPage_pageTitle, null);
        setDescription(Messages.InverseAssociationPage_pageDescription);
        this.wizard = wizard;
        this.toolkit = toolkit;

        setPageComplete(true);
    }

    @Override
    public void createControl(Composite parent) {
        Composite pageComposite = wizard.createPageComposite(parent);

        InverseAssociationSelectionListener listener = new InverseAssociationSelectionListener();

        newInverseAssociation = toolkit.createRadioButton(pageComposite,
                Messages.InverseAssociationPage_labelNewInverseAssociation);
        newInverseAssociation.addSelectionListener(listener);

        toolkit.createVerticalSpacer(pageComposite, 1);

        useExistingAssociation = toolkit.createRadioButton(pageComposite,
                Messages.InverseAssociationPage_labelUseExistiongAssociation);
        useExistingAssociation.addSelectionListener(listener);
        toolkit.createVerticalSpacer(pageComposite, 1);

        noInverseAssociation = toolkit.createRadioButton(pageComposite,
                Messages.InverseAssociationPage_labelNoInverseAssociation);
        noInverseAssociation.addSelectionListener(listener);

        // set the default selection: no inverse
        newInverseAssociation.setSelection(true);
        prevSelection = noInverseAssociation;

        setControl(pageComposite);
    }

    /**
     * Listener for the radio buttons.
     */
    private class InverseAssociationSelectionListener implements SelectionListener {
        @Override
        public void widgetSelected(SelectionEvent e) {
            if (prevSelection != e.getSource()) {
                prevSelection = (Button)e.getSource();
                if (e.getSource() == useExistingAssociation) {
                    wizard
                            .setInverseAssociationManipulation(NewPcTypeAssociationWizard.USE_EXISTING_INVERSE_ASSOCIATION);
                } else if (e.getSource() == newInverseAssociation) {
                    wizard.setInverseAssociationManipulation(NewPcTypeAssociationWizard.NEW_INVERSE_ASSOCIATION);
                } else if (e.getSource() == noInverseAssociation) {
                    wizard.setInverseAssociationManipulation(NewPcTypeAssociationWizard.NONE_INVERSE_ASSOCIATION);
                }

                wizard.pageHasChanged();
            }
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
            widgetSelected(e);
        }
    }

    public void setVisibleStateForDetailToMasterAssociation(boolean detailToMasterAssociation) {
        newInverseAssociation.setVisible(!detailToMasterAssociation);
        noInverseAssociation.setVisible(!detailToMasterAssociation);
        if (detailToMasterAssociation) {
            if (detailToMasterState) {
                return;
            }
            detailToMasterState = true;
            wizard.setInverseAssociationManipulation(NewPcTypeAssociationWizard.USE_EXISTING_INVERSE_ASSOCIATION);
            newInverseAssociation.setSelection(false);
            useExistingAssociation.setSelection(true);
            noInverseAssociation.setSelection(false);
            // wizard.handleInverseAssociationSelectionState();
        }
    }
}
