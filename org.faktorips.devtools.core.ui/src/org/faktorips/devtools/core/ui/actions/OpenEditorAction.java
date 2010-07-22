/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
import org.faktorips.devtools.core.internal.model.ipsobject.ArchiveIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.editors.productcmpt.ProductCmptEditor;
import org.faktorips.devtools.core.ui.editors.productcmpt.ProductCmptEditorInput;

/**
 * Action for opening objects in the corresponding editor.
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

    @Override
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
    public IEditorPart openEditor(IStructuredSelection selection) {
        IStructuredSelection relevantSelection = openProductCmptEditorBySelectedGeneration(selection);

        // ignores IFiles even if the underlying object is an IpsSrcFile
        IIpsSrcFile[] srcFiles = getIpsSrcFilesForSelection(relevantSelection);
        IEditorPart result = null;
        for (IIpsSrcFile srcFile : srcFiles) {
            result = IpsUIPlugin.getDefault().openEditor(srcFile);
        }
        for (Iterator<Object> iter = getSelectionIterator(relevantSelection); iter.hasNext();) {
            Object selected = iter.next();
            if (selected instanceof IFile) {
                result = IpsUIPlugin.getDefault().openEditor((IFile)selected);
            }
        }
        return result;
    }

    /**
     * Open product components via selected generations. Returns a new structured selection with all
     * elements which wasn't open using this method.
     */
    private IStructuredSelection openProductCmptEditorBySelectedGeneration(IStructuredSelection selection) {
        List<Object> newSelection = new ArrayList<Object>();
        for (Iterator<Object> iter = getSelectionIterator(selection); iter.hasNext();) {
            Object object = iter.next();
            if (object instanceof IProductCmptGeneration) {
                IProductCmptGeneration generation = (IProductCmptGeneration)object;
                IIpsSrcFile ipsSrcFile = generation.getIpsObject().getIpsSrcFile();
                IEditorPart part = null;
                if (!(ipsSrcFile instanceof ArchiveIpsSrcFile)) {
                    /*
                     * open the editor skipping the generation mismatch dialog. If the ipsSrcFile is
                     * used instead of the productCmptEditorInput, we cannot decide if the dialog
                     * should be shown or ignored
                     */
                    part = IpsUIPlugin.getDefault().openEditor(ProductCmptEditorInput.createWithGeneration(generation));
                } else {
                    /*
                     * open editor directly the generation mismatch dialog will never be displayed,
                     * because objects within archives are always be opened in read-only mode
                     */
                    part = IpsUIPlugin.getDefault().openEditor(ipsSrcFile);
                }
                if (part instanceof ProductCmptEditor) {
                    ((ProductCmptEditor)part).showGenerationEffectiveOn(generation.getValidFrom());
                }
            } else {
                newSelection.add(object);
            }
        }
        return new StructuredSelection(newSelection);
    }

}
