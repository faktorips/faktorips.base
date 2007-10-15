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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.ui.editors.productcmpt.ProductCmptEditor;
import org.faktorips.devtools.core.ui.editors.productcmpt.ProductCmptEditorInput;

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
    
    /**
     * {@inheritDoc}
     */
    public void run(IStructuredSelection selection) {
        openEditor(selection);
    }
    
    public IEditorPart openEditor() {
        ISelection selection = selectionProvider.getSelection();
        if (selection != null) {
            if (selection instanceof IStructuredSelection) {
                return openEditor((IStructuredSelection)selection);
            } else {
                throw new RuntimeException(Messages.IpsAction_msgUnsupportedSelection + selection.getClass().getName());
            }
        }
        return null;
    }
    
    /**
     * Opens all corresponding editor for the given selection. Returns the editor input of the last
     * opened editor or <code>null</code> if no editor was opened.
     */
    public IEditorPart openEditor(IStructuredSelection selection){
        IStructuredSelection relevantSelection = openProductCmptEditorBySelectedGeneration(selection);
        
        // ignores IFiles even if the underlying object is an IpsSrcFile
        IIpsSrcFile[] srcFiles= getIpsSrcFilesForSelection(relevantSelection);
        for (int i = 0; i < srcFiles.length; i++) {
            return IpsPlugin.getDefault().openEditor(srcFiles[i]);
        }
        for (Iterator iter= relevantSelection.iterator(); iter.hasNext();) {
            Object selected= iter.next();
            if(selected instanceof IFile){
                return IpsPlugin.getDefault().openEditor((IFile) selected);
            }
        }
        return null;
    }
    
    /*
     * Open product componts via selected generations. Returns a new structured selection with all
     * elements which wasn't open using this method.
     */
    private IStructuredSelection openProductCmptEditorBySelectedGeneration(IStructuredSelection selection) {
        List newSelection = new ArrayList();
        for (Iterator iter = selection.iterator(); iter.hasNext();) {
            Object object = iter.next();
            if (object instanceof IProductCmptGeneration){
                IProductCmptGeneration generation = (IProductCmptGeneration) object;
                IEditorPart part = IpsPlugin.getDefault().openEditor(ProductCmptEditorInput.createWithGeneration(generation));
                if (part instanceof ProductCmptEditor){
                    ((ProductCmptEditor)part).showGenerationEffectiveOn(generation.getValidFrom());
                }
            } else {
                newSelection.add(object);
            }
        }
        return new StructuredSelection(newSelection);
    }
}
