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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.pctype.ITypeHierarchy;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.TextButtonControl;
import org.faktorips.util.StringUtil;

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
                    IRelation relationResult = (IRelation)selectDialog.getResult()[0];
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
     * 
     * @return
     * @throws CoreException
     */
    protected IRelation[] getRelations() throws CoreException {
        ITypeHierarchy superTypeHierarchy = parentPolicyCmptType.getSupertypeHierarchy();
        IRelation[] relations = superTypeHierarchy.getAllRelations(parentPolicyCmptType);
        return relations;
    }

    public IRelation findRelation() throws CoreException{
        String relation = getText();
        IRelation[] relations = getRelations();
        for (int i = 0; i < relations.length; i++) {
            if (relations[i].getName().equals(relation))
                return relations[i];
        }
        return null;
    }
}
