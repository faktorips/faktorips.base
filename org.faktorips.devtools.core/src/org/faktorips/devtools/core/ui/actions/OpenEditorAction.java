/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PartInitException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsSrcFile;

/**
 * Opens a selected product component in an editor.
 * 
 * @author Thorsten Guenther
 * @author Stefan Widmaier
 */
public class OpenEditorAction extends IpsAction {

    public OpenEditorAction(ISelectionProvider selectionProvider) {
        super(selectionProvider);
        super.setText(Messages.OpenEditorAction_name);
        super.setDescription(Messages.OpenEditorAction_description);
        super.setToolTipText(Messages.OpenEditorAction_tooltip);
    }
    
    public void run(IStructuredSelection selection) {
        try {
            IIpsSrcFile file = getIpsSrcFileForSelection(selection);

            if (file != null) {
                IpsPlugin.getDefault().openEditor(file);
            }
            
        } catch (PartInitException e2) {
            IpsPlugin.logAndShowErrorDialog(e2);
        }
    }
}
