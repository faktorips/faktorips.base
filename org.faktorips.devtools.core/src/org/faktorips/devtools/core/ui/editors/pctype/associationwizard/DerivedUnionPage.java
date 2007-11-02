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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;

public class DerivedUnionPage extends WizardPage implements IHiddenWizardPage {
    private NewPcTypeAssociationWizard wizard;
    private IPolicyCmptTypeAssociation association;
    private UIToolkit toolkit;
    private Combo derivedUnionCandidatesCombo;

    protected DerivedUnionPage(NewPcTypeAssociationWizard wizard, IPolicyCmptTypeAssociation association, UIToolkit toolkit, BindingContext bindingContext) {
        super("DerivedUnionPage", "Select derived union association", null);
        super.setDescription("Select the derived union association if this associoation should be a subset of a derived union association");
        this.wizard = wizard;
        this.association = association;
        this.toolkit = toolkit;
        
        setPageComplete(true);
    }
    
    public void createControl(Composite parent) {
        Composite workArea = toolkit.createLabelEditColumnComposite(parent);
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        toolkit.createFormLabel(workArea, "Derived union");
        derivedUnionCandidatesCombo = toolkit.createCombo(workArea);
        
        setControl(workArea);
    }

    void fillComboWithDerivedUnionCandidates(IAssociation[] candidates){
        String[] candidateNames = new String[candidates.length+1];
        candidateNames[0] = "";
        for (int i = 0; i < candidates.length; i++) {
            candidateNames[i+1] = candidates[i].getName();
        }
        derivedUnionCandidatesCombo.setItems(candidateNames);
        if (candidates.length >= 1){
            derivedUnionCandidatesCombo.select(1);
        }
    }
    
    /**
     * @return <code>true</code> if at least one derived union candidates exists and the
     *         association is no derived union association and the target exits.
     */
    public boolean isPageVisible(){
        IIpsProject ipsProject = association.getIpsProject();
        try {
            if (association.findTarget(ipsProject) != null
                && association.findDerivedUnionCandidates(ipsProject).length > 0
                && !association.isDerivedUnion()) {
                return true;
            }
        } catch (CoreException e) {
            wizard.showAndLogError(e);
        }
        return false;
    }
}
