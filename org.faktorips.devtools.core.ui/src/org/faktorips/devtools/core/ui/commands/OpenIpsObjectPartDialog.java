/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;
import org.faktorips.devtools.core.ui.editors.productcmpt.link.LinkEditDialog;
import org.faktorips.devtools.core.ui.util.TypedSelection;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFileMemento;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;

/**
 * This command handler is indented to open the correct property dialog for any
 * {@link IIpsObjectPart}. If the dialog for your part is missing feel free to append.
 * 
 * @author dirmeier
 */
public class OpenIpsObjectPartDialog extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection currentSelection = HandlerUtil.getCurrentSelection(event);

        TypedSelection<IAdaptable> typedSelection = new TypedSelection<>(IAdaptable.class, currentSelection);
        if (typedSelection.isValid()) {
            IAdaptable firstElement = typedSelection.getFirstElement();

            IProductCmptLink link = IpsObjectPartTester.castOrAdaptToPart(firstElement,
                    IProductCmptLink.class);
            if (link != null) {
                openLinkEditDialog(link, event);
            }
        }
        return null;
    }

    private void openLinkEditDialog(IProductCmptLink link, ExecutionEvent event) {
        Shell activeShell = HandlerUtil.getActiveShell(event);
        try {
            IIpsSrcFile file = link.getIpsObject().getIpsSrcFile();
            IIpsSrcFileMemento memento = file.newMemento();
            LinkEditDialog dialog = new LinkEditDialog(link, activeShell);
            dialog.setDataChangeable(isEditorEditable(link, event));
            int rc = dialog.open();
            if (rc == Window.CANCEL) {
                file.setMemento(memento);
            }
        } catch (IpsException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    private boolean isEditorEditable(IIpsObjectPart ipsObjectPart, ExecutionEvent event) {
        IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);
        if (activeEditor instanceof IpsObjectEditor) {
            IpsObjectEditor ipsObjectEditor = (IpsObjectEditor)activeEditor;
            return ipsObjectEditor.isDataChangeable()
                    && ipsObjectEditor.getIpsSrcFile().equals(ipsObjectPart.getIpsSrcFile());
        }
        return false;
    }

}
