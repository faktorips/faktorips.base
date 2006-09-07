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

import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsSrcFile;

/**
 * Action for opening objects in the corresponding editor. <p>
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
        // ignores IFiles even if the underlying object is an IpsSrcFile
		IIpsSrcFile[] srcFiles= getIpsSrcFilesForSelection(selection);
        for (int i = 0; i < srcFiles.length; i++) {
            IpsPlugin.getDefault().openEditor(srcFiles[i]);
        }
        for (Iterator iter= selection.iterator(); iter.hasNext();) {
            Object selected= iter.next();
            if(selected instanceof IFile){
                IpsPlugin.getDefault().openEditor((IFile) selected);
            }
		}
    }
}
