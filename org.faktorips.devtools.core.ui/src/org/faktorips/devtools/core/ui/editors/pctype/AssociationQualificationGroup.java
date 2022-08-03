/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.pctype;

import org.apache.commons.lang.StringUtils;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.binding.ButtonTextBinding;
import org.faktorips.devtools.core.ui.binding.IpsObjectPartPmo;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.devtools.model.util.QNameUtil;

/**
 * Group composite to edit the associations qualification.
 * 
 * @author Joerg Ortmann
 */
public class AssociationQualificationGroup extends Composite {

    private final PmoAssociation pmoAssociation;

    public AssociationQualificationGroup(UIToolkit uiToolkit, BindingContext bindingContext, Composite parent,
            IAssociation association) {

        super(parent, SWT.NONE);

        GridLayout layout = new GridLayout(1, false);
        layout.marginTop = 0;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        setLayout(layout);

        GridData gridData = new GridData(SWT.FILL, SWT.NONE, true, false);
        setLayoutData(gridData);

        pmoAssociation = new PmoAssociation((IPolicyCmptTypeAssociation)association);

        Group groupQualification = uiToolkit.createGroup(this,
                Messages.AssociationQualificationGroup_groupQualification);
        ((GridData)groupQualification.getLayoutData()).grabExcessVerticalSpace = false;
        createQualificationControls(uiToolkit, bindingContext, groupQualification, association);
    }

    /**
     * Bind a text (as note) to inform if the association is [not] constrained by product structure.
     */
    public void bindLabelAboutConstrainedByProductStructure(Text note, BindingContext bindingContext) {
        note.setText(pmoAssociation.getConstrainedNote());
        bindingContext.bindContent(note, pmoAssociation, PmoAssociation.PROPERTY_CONSTRAINED_NOTE);
    }

    /**
     * Creates the note text to inform if the association is [not] constrained by product structure.
     */
    public static Text createConstrainedNote(UIToolkit uiToolkit, Composite parent) {
        uiToolkit.createHorizonzalLine(parent);

        Text noteAboutProductStructureConstrained = new Text(parent, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
        noteAboutProductStructureConstrained.setEditable(false);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.heightHint = 60;
        gridData.widthHint = UIToolkit.DEFAULT_WIDTH;
        noteAboutProductStructureConstrained.setLayoutData(gridData);
        return noteAboutProductStructureConstrained;
    }

    private void createQualificationControls(UIToolkit uiToolkit,
            BindingContext bindingContext,
            Composite parent,
            IAssociation association) {

        Composite workArea = uiToolkit.createGridComposite(parent, 1, true, true);
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH));

        Checkbox qualifiedCheckbox = uiToolkit.createCheckbox(workArea);
        bindingContext.bindContent(qualifiedCheckbox, association, IAssociation.PROPERTY_QUALIFIED);
        bindingContext.bindEnabled(qualifiedCheckbox, pmoAssociation, PmoAssociation.PROPERTY_QUALIFICATION_POSSIBLE);
        Label note = uiToolkit.createFormLabel(workArea, StringUtils.rightPad("", 120)); //$NON-NLS-1$
        bindingContext.bindContent(note, pmoAssociation, PmoAssociation.PROPERTY_QUALIFICATION_NOTE);
        bindingContext.add(
                new ButtonTextBinding(qualifiedCheckbox, pmoAssociation, PmoAssociation.PROPERTY_QUALIFICATION_LABEL));

        GridData gridData = new GridData();
        GC gc = new GC(getShell());
        gridData.widthHint = getMaxWidth(gc, gridData.minimumWidth, pmoAssociation.getQualificationNote(false, false));
        gridData.widthHint = getMaxWidth(gc, gridData.minimumWidth, pmoAssociation.getQualificationNote(false, true));
        gridData.widthHint = getMaxWidth(gc, gridData.minimumWidth, pmoAssociation.getQualificationNote(true, false));
        gridData.widthHint = getMaxWidth(gc, gridData.minimumWidth, pmoAssociation.getQualificationNote(true, true));
        note.setLayoutData(gridData);
    }

    private int getMaxWidth(GC gc, int maxWidth, String text) {
        Point e = gc.textExtent(text, SWT.DRAW_DELIMITER | SWT.DRAW_TAB);
        return Math.max(maxWidth, e.x);
    }

    public class PmoAssociation extends IpsObjectPartPmo {

        public static final String PROPERTY_QUALIFICATION_LABEL = "qualificationLabel"; //$NON-NLS-1$
        public static final String PROPERTY_QUALIFICATION_NOTE = "qualificationNote"; //$NON-NLS-1$
        public static final String PROPERTY_QUALIFICATION_POSSIBLE = "qualificationPossible"; //$NON-NLS-1$
        public static final String PROPERTY_CONSTRAINED_NOTE = "constrainedNote"; //$NON-NLS-1$

        private IIpsProject ipsProject;
        private IPolicyCmptTypeAssociation association;

        public PmoAssociation(IPolicyCmptTypeAssociation association) {
            super(association);
            this.association = association;
            ipsProject = association.getIpsProject();
            addDisposeListener($ -> dispose());
        }

        public String getQualificationLabel() {
            String label = Messages.AssociationQualificationGroup_labelIsQualified;
            try {
                String productCmptType = QNameUtil.getUnqualifiedName(association.findQualifierCandidate(ipsProject));
                if (StringUtils.isNotEmpty(productCmptType)) {
                    label = label
                            + NLS.bind(Messages.AssociationQualificationGroup_labelIsQualifiedByType, productCmptType);
                }
            } catch (IpsException e) {
                IpsPlugin.log(e);
            }
            return StringUtils.rightPad(label, 80);
        }

        public String getQualificationNote() {
            try {
                return getQualificationNote(association.isCompositionMasterToDetail(),
                        association.isQualificationPossible(ipsProject));

            } catch (IpsException e) {
                IpsPlugin.log(e);
            }
            return ""; //$NON-NLS-1$
        }

        public String getQualificationNote(boolean compositeDetailToMaster, boolean qualificationPossible) {
            String note = Messages.AssociationQualificationGroup_labelNote;
            if (!compositeDetailToMaster) {
                note = note + Messages.AssociationQualificationGroup_labelNoteQualificationOnlyMasterDetail;
            } else {
                if (!qualificationPossible) {
                    note = note + Messages.AssociationQualificationGroup_labelNoteQualificationOnlyTargetConfByProduct;
                } else {
                    note = note + Messages.AssociationQualificationGroup_noteQualifiedMultiplictyPerQualifiedInstance;
                }
            }
            return StringUtils.rightPad(note, 90);
        }

        public boolean isQualificationPossible() {
            try {
                return association.isQualificationPossible(ipsProject);
            } catch (IpsException e) {
                IpsPlugin.log(e);
                return false;
            }
        }

        public String getConstrainedNote() {
            if (association.isCompositionDetailToMaster()) {
                return StringUtils.rightPad("", 120) + StringUtils.rightPad("\n", 120) + StringUtils.right("\n", 120); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
            IProductCmptTypeAssociation matchingAss = association.findMatchingProductCmptTypeAssociation(ipsProject);
            if (matchingAss != null) {
                String type = matchingAss.getProductCmptType().getName();
                return NLS.bind(Messages.AssociationQualificationGroup_noteIsConstrained, type,
                        matchingAss.getTargetRoleSingular()) + StringUtils.rightPad("\n", 120); //$NON-NLS-1$
            } else {
                String note = Messages.AssociationQualificationGroup_noteIsNotConstrained;
                IProductCmptType sourceProductType = association.getPolicyCmptType().findProductCmptType(ipsProject);
                IPolicyCmptType targetType = association.findTargetPolicyCmptType(ipsProject);
                if (sourceProductType != null && targetType != null) {
                    IProductCmptType targetProductType = targetType.findProductCmptType(ipsProject);
                    if (targetProductType != null) {
                        return note + NLS.bind(Messages.AssociationQualificationGroup_noteContrainHowTo,
                                sourceProductType.getName(), targetProductType.getName());
                    }
                }
                return note + StringUtils.rightPad("\n", 120) + StringUtils.rightPad("\n", 120); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
    }

}
