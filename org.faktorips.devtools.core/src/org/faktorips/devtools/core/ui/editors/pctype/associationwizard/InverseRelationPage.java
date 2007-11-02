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
        
        setPageComplete(true);
    }

    public void createControl(Composite parent) {
        Composite c = toolkit.createGridComposite(parent, 1, false, true);
        ((GridLayout)c.getLayout()).marginHeight = 12;
        
        InverseRelationSelectionListener listener = new InverseRelationSelectionListener();

        noReverseRelation = toolkit.createRadioButton(c, "No inverse relation");
        noReverseRelation.addSelectionListener(listener);
        
        toolkit.createVerticalSpacer(c, 1);

        useExistingRelation = toolkit.createRadioButton(c, "Use existing relation as inverse");
        useExistingRelation.addSelectionListener(listener);
        
        toolkit.createVerticalSpacer(c, 1);

        newReverseRelation = toolkit.createRadioButton(c, "New inverse relation");
        newReverseRelation.addSelectionListener(listener);
        
        // set the default selection: no inverse
        noReverseRelation.setSelection(true);
        prevSelection = noReverseRelation;
        
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
                    wizard.setInverseAssociationManipulation(NewPcTypeAssociationWizard.USE_EXISTING_REVERSE_RELATION);
                }else if(e.getSource() == newReverseRelation){
                    wizard.setInverseAssociationManipulation(NewPcTypeAssociationWizard.NEW_REVERSE_RELATION);
                }else if(e.getSource() == noReverseRelation){
                    wizard.setInverseAssociationManipulation(NewPcTypeAssociationWizard.NONE_REVERSE_RELATION);
                }
                
                wizard.pageHasChanged();
            }
        }
        public void widgetDefaultSelected(SelectionEvent e) {
            widgetSelected(e);
        }
    }
}
