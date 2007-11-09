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

package org.faktorips.devtools.core.ui.controls;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.binding.ButtonTextBinding;
import org.faktorips.devtools.core.ui.binding.IpsObjectPartPmo;
import org.faktorips.devtools.core.util.QNameUtil;

/**
 * Group composite to edit the associations qualification.
 * 
 * @author Joerg Ortmann
 */
public class AssociationQualificationGroup extends Composite {

    private PmoAssociation pmoAssociation;

    public AssociationQualificationGroup(UIToolkit uiToolkit, BindingContext bindingContext, Composite parent, IAssociation association) {
        super(parent, SWT.NONE);
        
        GridLayout layout = new GridLayout(1, false);
        layout.marginTop = 0;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        this.setLayout(layout);
        
        GridData gridData = new GridData(SWT.FILL, SWT.NONE, true, false);
        this.setLayoutData(gridData);
        
        pmoAssociation = new PmoAssociation((IPolicyCmptTypeAssociation)association);
        
        Group groupQualification = uiToolkit.createGroup(this, "Qualification");
        ((GridData)groupQualification.getLayoutData()).grabExcessVerticalSpace = false;
        createQualificationControls(uiToolkit, bindingContext, groupQualification, association);
    }

    /**
     * Bind a label (as note) to inform if the association is [not] constrained by product structure.
     */
    public void bindLabelAboutConstrainedByProductStructure(Label note, BindingContext bindingContext) {
        note.setText(pmoAssociation.getConstrainedNote());
        bindingContext.bindContent(note, pmoAssociation, PmoAssociation.PROPERTY_CONSTRAINED_NOTE);
    }  
    
    private void createQualificationControls(UIToolkit uiToolkit, BindingContext bindingContext, Composite parent, IAssociation association) {
        Composite workArea = uiToolkit.createGridComposite(parent, 1, true, true);
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        Checkbox qualifiedCheckbox = uiToolkit.createCheckbox(workArea);
        bindingContext.bindContent(qualifiedCheckbox, association, IAssociation.PROPERTY_QUALIFIED);
        bindingContext.bindEnabled(qualifiedCheckbox, pmoAssociation, PmoAssociation.PROPERTY_QUALIFICATION_POSSIBLE);
        Label note = uiToolkit.createFormLabel(workArea, StringUtils.rightPad("", 120)); //$NON-NLS-1$
        bindingContext.bindContent(note, pmoAssociation, PmoAssociation.PROPERTY_QUALIFICATION_NOTE);
        bindingContext.add(new ButtonTextBinding(qualifiedCheckbox, pmoAssociation, PmoAssociation.PROPERTY_QUALIFICATION_LABEL));
    }
    
    public class PmoAssociation extends IpsObjectPartPmo {
        public final static String PROPERTY_QUALIFICATION_LABEL = "qualificationLabel"; //$NON-NLS-1$
        public final static String PROPERTY_QUALIFICATION_NOTE = "qualificationNote"; //$NON-NLS-1$
        public final static String PROPERTY_QUALIFICATION_POSSIBLE = "qualificationPossible"; //$NON-NLS-1$
        public final static String PROPERTY_CONSTRAINED_NOTE = "constrainedNote"; //$NON-NLS-1$
        
        private IIpsProject ipsProject;
        private IPolicyCmptTypeAssociation association;

        public PmoAssociation(IPolicyCmptTypeAssociation association) {
            super(association);
            
            this.association = association;
            this.ipsProject = association.getIpsProject();
        }
        
        public String getQualificationLabel() {
            String label = "This association is qualified";
            try {
                String productCmptType = QNameUtil.getUnqualifiedName(association.findQualifierCandidate(ipsProject));
                if (StringUtils.isNotEmpty(productCmptType)) {
                    label = label + NLS.bind(" by type \"{0}\"", productCmptType);
                }
            }
            catch (CoreException e) {
                IpsPlugin.log(e);
            }
            return StringUtils.rightPad(label, 80);            
        }

        public String getQualificationNote() {
            String note = "Note: ";
            if (!association.isCompositionMasterToDetail()) {
                note = note + "Qualification is only applicable for compositions (master to detail).";
            } else {
                try {
                    if (!association.isQualificationPossible(ipsProject)) {
                        note = note + "Qualification is only applicable, if the target type is configurable by a product.";
                    } else {
                        note = note + "For qualified associations multiplicty is defined per qualified instance.";
                    }
                }
                catch (CoreException e) {
                    IpsPlugin.log(e);
                }
            }
            return StringUtils.rightPad(note, 90);
        }
        
        public boolean isQualificationPossible() {
            try {
                return association.isQualificationPossible(ipsProject);
            }
            catch (CoreException e) {
                IpsPlugin.log(e);
                return false;
            }
        }

        public String getConstrainedNote() {
            try {
                if (association.isCompositionDetailToMaster()) {
                    return StringUtils.rightPad("", 120) + StringUtils.rightPad("\n", 120) + StringUtils.right("\n", 120); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                }
                IProductCmptTypeAssociation matchingAss = association.findMatchingProductCmptTypeAssociation(ipsProject);
                if (matchingAss!=null) {
                    String type = matchingAss.getProductCmptType().getName();
                    return NLS.bind("Note: This association is constrained by product structure. The matching \nassociation in type \"{0}\" is \"{1}\" (rolename).", type, matchingAss.getTargetRoleSingular()) 
                            + StringUtils.rightPad("\n", 120);  //$NON-NLS-1$
                } else {
                    String note = "Note: This association is not constrained by product structure."; 
                    IProductCmptType sourceProductType = association.getPolicyCmptType().findProductCmptType(ipsProject);
                    IPolicyCmptType targetType = association.findTargetPolicyCmptType(ipsProject);
                    if (sourceProductType!=null && targetType!=null) {
                        IProductCmptType targetProductType = targetType.findProductCmptType(ipsProject);
                        if (targetProductType!=null) {
                            return note + NLS.bind("\nTo constrain the association by product structure, create an association between the \nproduct component types ''{0}'' and ''{1}''.", sourceProductType.getName(), targetProductType.getName());
                        }
                    }
                    return note + StringUtils.rightPad("\n", 120) + StringUtils.rightPad("\n", 120) ; //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
            catch (CoreException e) {
                IpsPlugin.log(e);
                return ""; //$NON-NLS-1$
            }
        }
    }

}
