/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFileMemento;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;
import org.faktorips.devtools.core.ui.editors.productcmpt.LinkEditDialog;
import org.faktorips.devtools.core.ui.util.TypedSelection;

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
        TypedSelection<IIpsObjectPart> typedSelection = new TypedSelection<IIpsObjectPart>(IIpsObjectPart.class,
                currentSelection);
        if (typedSelection.isValid()) {
            IIpsObjectPart ipsObjectPart = typedSelection.getFirstElement();
            if (ipsObjectPart instanceof IProductCmptLink) {
                IProductCmptLink productCmptLink = (IProductCmptLink)ipsObjectPart;
                openLinkEditDialog(productCmptLink, event);
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
        } catch (CoreException e) {
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
