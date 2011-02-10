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

package org.faktorips.devtools.core.ui.editors.testcasetype;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.contentassist.ContentAssistHandler;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.ui.CompletionUtil;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.TextButtonControl;
import org.faktorips.util.StringUtil;

/**
 * Control to select a association.
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
        ContentAssistHandler.createHandlerForText(getTextControl(),
                CompletionUtil.createContentAssistant(completionProcessor));
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
                    IPolicyCmptTypeAssociation associationResult = (IPolicyCmptTypeAssociation)selectDialog.getResult()[0];
                    setText(associationResult.getName());
                } else {
                    setText(""); //$NON-NLS-1$
                }
            }
        } catch (Exception e) {
            // TODO catch Exception needs to be documented properly or specialized
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    /**
     * Returns all associations of the parentPolicyCmptType which are assoziations or forward
     * compositions
     * 
     * @throws CoreException in case of an error
     */
    protected IPolicyCmptTypeAssociation[] getAssociations() throws CoreException {
        List<IPolicyCmptTypeAssociation> associationsToSelect = new ArrayList<IPolicyCmptTypeAssociation>();
        IPolicyCmptType currPolicyCmptType = parentPolicyCmptType;
        while (currPolicyCmptType != null) {
            IPolicyCmptTypeAssociation[] associations = currPolicyCmptType.getPolicyCmptTypeAssociations();
            for (IPolicyCmptTypeAssociation association : associations) {
                if (association.isAssoziation() || association.isCompositionMasterToDetail()) {
                    associationsToSelect.add(association);
                }
            }
            currPolicyCmptType = (IPolicyCmptType)currPolicyCmptType.findSupertype(currPolicyCmptType.getIpsProject());
        }
        return associationsToSelect.toArray(new IPolicyCmptTypeAssociation[0]);
    }

    public IPolicyCmptTypeAssociation findAssociation() throws CoreException {
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
