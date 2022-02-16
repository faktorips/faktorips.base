/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.testcasetype;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.CompletionUtil;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.TextButtonControl;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.util.StringUtil;

/**
 * Control to select a association. Only "normal" associations (no aggregations) as well as
 * master-to-detail compositions can be selected with this control. Derived unions can not be
 * selected to prevent their use in test cases. See FIPS-594.
 * 
 * @author Joerg Ortmann
 */
public class AssociationRefControl extends TextButtonControl {

    private String dialogTitle;
    private String dialogMessage;
    private IPolicyCmptType parentPolicyCmptType;

    public AssociationRefControl(Composite parent, UIToolkit toolkit, IPolicyCmptType parentPolicyCmptType) {
        super(parent, toolkit, Messages.AssociationRefControl_Button_Browse);
        this.dialogTitle = Messages.AssociationRefControl_Title;
        this.dialogMessage = Messages.AssociationRefControl_Description;
        this.parentPolicyCmptType = parentPolicyCmptType;

        AssociationCompletionProcessor completionProcessor = new AssociationCompletionProcessor(parentPolicyCmptType,
                true);
        completionProcessor.setComputeProposalForEmptyPrefix(true);
        CompletionUtil.createHandlerForText(getTextControl(), completionProcessor);
    }

    @Override
    protected void buttonClicked() {
        try {
            ElementListSelectionDialog selectDialog = new ElementListSelectionDialog(getShell(),
                    new DefaultLabelProvider());
            selectDialog.setTitle(dialogTitle);
            selectDialog.setMessage(dialogMessage);
            selectDialog.setElements(getAssociations());
            selectDialog.setFilter(StringUtil.unqualifiedName(super.getText()));
            if (selectDialog.open() == Window.OK) {
                if (selectDialog.getResult().length > 0) {
                    IPolicyCmptTypeAssociation associationResult = (IPolicyCmptTypeAssociation)selectDialog
                            .getResult()[0];
                    setText(associationResult.getName());
                } else {
                    setText(""); //$NON-NLS-1$
                }
            }
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    /**
     * Returns all associations of the parentPolicyCmptType which are assoziations or forward
     * compositions
     * 
     * @throws IpsException in case of an error
     */
    protected IPolicyCmptTypeAssociation[] getAssociations() {
        List<IPolicyCmptTypeAssociation> associationsToSelect = new ArrayList<>();
        IPolicyCmptType currPolicyCmptType = parentPolicyCmptType;
        while (currPolicyCmptType != null) {
            List<IPolicyCmptTypeAssociation> associations = currPolicyCmptType.getPolicyCmptTypeAssociations();
            for (IPolicyCmptTypeAssociation association : associations) {
                if (isRelevantAssociation(association)) {
                    associationsToSelect.add(association);
                }
            }
            currPolicyCmptType = (IPolicyCmptType)currPolicyCmptType.findSupertype(currPolicyCmptType.getIpsProject());
        }
        return associationsToSelect.toArray(new IPolicyCmptTypeAssociation[0]);
    }

    /**
     * As in FIPS-594 no derive unions should be supported by test case types.
     */
    protected boolean isRelevantAssociation(IPolicyCmptTypeAssociation association) {
        return (association.isAssoziation() || association.isCompositionMasterToDetail())
                && !association.isDerivedUnion();
    }

    public IPolicyCmptTypeAssociation findAssociation() {
        String association = getText();
        IPolicyCmptTypeAssociation[] associations = getAssociations();
        for (IPolicyCmptTypeAssociation association2 : associations) {
            if (association2.getName().equals(association)) {
                return association2;
            }
        }
        return null;
    }
}
