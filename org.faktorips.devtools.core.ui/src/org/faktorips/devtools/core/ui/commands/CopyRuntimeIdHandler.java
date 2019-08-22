/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.commands;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.handlers.HandlerUtil;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.ui.util.TypedSelection;

public class CopyRuntimeIdHandler extends IpsAbstractHandler {
    @Override
    public void execute(ExecutionEvent event, IWorkbenchPage activePage, IIpsSrcFile ipsSrcFile)
            throws ExecutionException {
        ISelection selection = HandlerUtil.getCurrentSelectionChecked(event);
        Clipboard clipboard = new Clipboard(HandlerUtil.getActiveShellChecked(event).getDisplay());
        copyRuntimeIdToClipboard(selection, clipboard);
    }

    public void copyRuntimeIdToClipboard(ISelection selection, Clipboard clipboard) {
        StringBuilder runtimeIds = new StringBuilder();
        TypedSelection<IAdaptable> typedSelection = TypedSelection.createAnyCount(IAdaptable.class, selection);

        boolean first = true;
        for (IAdaptable element : typedSelection.getElements()) {
            if (element.getAdapter(IProductCmpt.class) != null) {
                IProductCmpt productComponent = (IProductCmpt)element.getAdapter(IProductCmpt.class);
                if (!first) {
                    runtimeIds.append(System.getProperty("line.separator")); //$NON-NLS-1$
                }
                first = false;
                runtimeIds.append(productComponent.getRuntimeId());
            }
        }

        Object[] ids = new Object[] { runtimeIds.toString() };
        clipboard.setContents(ids, new Transfer[] { TextTransfer.getInstance() });
    }
}
