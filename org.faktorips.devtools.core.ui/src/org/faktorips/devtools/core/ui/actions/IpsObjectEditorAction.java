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

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;

/**
 * Base class for actions that are used for IpsObjectEditors. It considers the data changable state
 * of the IpsObjectEditor respectively the active IpsObjectEditorPage when calculating the enabled state
 * if this action.
 * 
 * @author Peter Erzberger
 */
public abstract class IpsObjectEditorAction extends IpsAction {

    private IpsObjectEditor editor;
    
    public IpsObjectEditorAction(IpsObjectEditor editor) {
        super(editor.getSelectionProviderDispatcher());
        this.editor = editor;
    }    

    /**
     * {@inheritDoc}
     */
    protected boolean computeEnabledProperty(IStructuredSelection selection) {
        if(!editor.isDataChangeable().booleanValue()){
            return false;
        }
        if(!editor.getActiveIpsObjectEditorPage().isDataChangeable()){
            return false;
        }
        return true;
    }
}
