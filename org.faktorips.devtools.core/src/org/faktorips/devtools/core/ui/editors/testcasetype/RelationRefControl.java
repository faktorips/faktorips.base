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
 * Control to select a relation.
 * 
 * @author Joerg Ortmann
 */
public class RelationRefControl extends TextButtonControl {
    private String dialogTitle;
    private String dialogMessage;
    private IPolicyCmptType parentPolicyCmptType;
    
    public RelationRefControl(
            Composite parent, 
            UIToolkit toolkit,
            IPolicyCmptType parentPolicyCmptType) {
        super(parent, toolkit, Messages.RelationRefControl_Button_Browse);
        this.dialogTitle = Messages.RelationRefControl_Title;
        this.dialogMessage = Messages.RelationRefControl_Description;
        this.parentPolicyCmptType = parentPolicyCmptType;
        
        RelationCompletionProcessor completionProcessor = new RelationCompletionProcessor(parentPolicyCmptType, true);
        completionProcessor.setComputeProposalForEmptyPrefix(true);
        ContentAssistHandler.createHandlerForText(text, CompletionUtil.createContentAssistant(completionProcessor));
    }

    /**
     * {@inheritDoc}
     */
    protected void buttonClicked() {
        try {
            ElementListSelectionDialog selectDialog = new ElementListSelectionDialog(getShell(), new DefaultLabelProvider());
            selectDialog.setTitle(dialogTitle);
            selectDialog.setMessage(dialogMessage);
            selectDialog.setElements(getRelations());
            selectDialog.setFilter(StringUtil.unqualifiedName(super.getText()));
            if (selectDialog.open()==Window.OK) {
                if (selectDialog.getResult().length>0) {
                    IPolicyCmptTypeAssociation relationResult = (IPolicyCmptTypeAssociation)selectDialog.getResult()[0];
                    setText(relationResult.getName());
                } else {
                    setText(""); //$NON-NLS-1$
                }
            }
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }
    
    /**
     * Returns all relations of the parentPolicyCmptType which are assoziations or forward compositions
     * 
     * @throws CoreException in case of an error
     */
    protected IPolicyCmptTypeAssociation[] getRelations() throws CoreException {
        List relationsToSelect = new ArrayList();
        IPolicyCmptType currPolicyCmptType = parentPolicyCmptType;
        while (currPolicyCmptType != null){
            IPolicyCmptTypeAssociation[] relations = currPolicyCmptType.getPolicyCmptTypeAssociations();
            for (int i = 0; i < relations.length; i++) {
                if (relations[i].isAssoziation() || relations[i].isCompositionMasterToDetail()){
                    relationsToSelect.add(relations[i]);
                }
            }
            currPolicyCmptType = (IPolicyCmptType)currPolicyCmptType.findSupertype(currPolicyCmptType.getIpsProject());
        }
        return (IPolicyCmptTypeAssociation[]) relationsToSelect.toArray(new IPolicyCmptTypeAssociation[0]);
    }

    public IPolicyCmptTypeAssociation findRelation() throws CoreException{
        String relation = getText();
        IPolicyCmptTypeAssociation[] relations = getRelations();
        for (int i = 0; i < relations.length; i++) {
            if (relations[i].getName().equals(relation))
                return relations[i];
        }
        return null;
    }
}
