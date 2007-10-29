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
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.pctype.relationwizard.Messages;

public class InverseRelationPage extends WizardPage {

    private NewPcTypeAssociationWizard wizard;
    private UIToolkit toolkit;

    private Button newReverseRelation;
    private Button useExistingRelation;
    private Button noReverseRelation;
    private Button prevSelection;

    public InverseRelationPage(NewPcTypeAssociationWizard wizard, UIToolkit toolkit) {
        super("InverseRelationPage", "Inverse relation", null);
        setDescription("Define inverse relation");
        this.wizard = wizard;
        this.toolkit = toolkit;
    }

    public void createControl(Composite parent) {
        Composite c = toolkit.createGridComposite(parent, 1, false, true);
        ((GridLayout)c.getLayout()).marginHeight = 12;
        
        InverseRelationSelectionListener listener = new InverseRelationSelectionListener();

        newReverseRelation = toolkit.createRadioButton(c, Messages.NewPcTypeRelationWizard_reverseRelation_labelNewReverseRelation);
        newReverseRelation.addSelectionListener(listener);
        
        toolkit.createVerticalSpacer(c, 1);

        useExistingRelation = toolkit.createRadioButton(c, Messages.NewPcTypeRelationWizard_reverseRelation_labelUseExistingRelation);
        useExistingRelation.addSelectionListener(listener);
        
        toolkit.createVerticalSpacer(c, 1);
        
        noReverseRelation = toolkit.createRadioButton(c, Messages.NewPcTypeRelationWizard_reverseRelation_labelNoReverseRelation);
        noReverseRelation.addSelectionListener(listener);
        
        // set the default selection
        newReverseRelation.setSelection(true);
        
        prevSelection = newReverseRelation;
        
        setControl(c);
    }

    /**
     * Listener for the radio buttons.
     */
    private class InverseRelationSelectionListener implements SelectionListener {
        public void widgetSelected(SelectionEvent e) {
            // if no reverse relation is selected then disable next wizard page
            // otherwise enable next wizard page
            if (prevSelection != e.getSource()){
                prevSelection = (Button) e.getSource();
                if (e.getSource() == useExistingRelation) {
                    wizard.setExistingReverseRelation();
                }else if(e.getSource() == newReverseRelation){
                    wizard.setNewReverseRelation();
                }else if(e.getSource() == noReverseRelation){
                    wizard.setNoneReverseRelation();
                }
                
                // informs the property page of the reverse relation about the change
                wizard.inverseRelationOperationHasChanged();
            }
        }
        public void widgetDefaultSelected(SelectionEvent e) {
            widgetSelected(e);
        }
    }
}
