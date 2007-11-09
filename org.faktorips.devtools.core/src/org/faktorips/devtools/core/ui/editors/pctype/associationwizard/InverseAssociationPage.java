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
 * @author Joerg Ortmann
 */
public class InverseAssociationPage extends WizardPage {

    private NewPcTypeAssociationWizard wizard;
    private UIToolkit toolkit;

    private Button newInverseAssociation;
    private Button useExistingAssociation;
    private Button noInverseAssociation;
    private Button prevSelection;

    public InverseAssociationPage(NewPcTypeAssociationWizard wizard, UIToolkit toolkit) {
        super("InverseAssociationPage", "Inverse association", null);
        setDescription("Define inverse association");
        this.wizard = wizard;
        this.toolkit = toolkit;
        
        setPageComplete(true);
    }

    public void createControl(Composite parent) {
        Composite pageComposite = wizard.createPageComposite(parent);
        
        InverseAssociationSelectionListener listener = new InverseAssociationSelectionListener();

        newInverseAssociation = toolkit.createRadioButton(pageComposite, "New inverse association");
        newInverseAssociation.addSelectionListener(listener);
        
        toolkit.createVerticalSpacer(pageComposite, 1);

        useExistingAssociation = toolkit.createRadioButton(pageComposite, "Use existing association as inverse");
        useExistingAssociation.addSelectionListener(listener);
        
        toolkit.createVerticalSpacer(pageComposite, 1);

        noInverseAssociation = toolkit.createRadioButton(pageComposite, "No inverse association");
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
        public void widgetSelected(SelectionEvent e) {
            if (prevSelection != e.getSource()){
                prevSelection = (Button) e.getSource();
                if (e.getSource() == useExistingAssociation) {
                    wizard.setInverseAssociationManipulation(NewPcTypeAssociationWizard.USE_EXISTING_INVERSE_ASSOCIATION);
                }else if(e.getSource() == newInverseAssociation){
                    wizard.setInverseAssociationManipulation(NewPcTypeAssociationWizard.NEW_INVERSE_ASSOCIATION);
                }else if(e.getSource() == noInverseAssociation){
                    wizard.setInverseAssociationManipulation(NewPcTypeAssociationWizard.NONE_INVERSE_ASSOCIATION);
                }
                
                wizard.pageHasChanged();
            }
        }
        public void widgetDefaultSelected(SelectionEvent e) {
            widgetSelected(e);
        }
    }
}
