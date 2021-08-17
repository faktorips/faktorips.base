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

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.handlers.HandlerUtil;
import org.faktorips.devtools.core.ui.util.TypedSelection;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;

public class AbstractCopyRuntimeIdHandler<T extends IIpsObject> extends IpsAbstractHandler {

    private final Function<IAdaptable, Optional<T>> adapt;
    private final Function<T, String> getRuntimeId;

    public AbstractCopyRuntimeIdHandler(Function<IAdaptable, Optional<T>> adapt, Function<T, String> getRuntimeId) {
        super();
        this.adapt = adapt;
        this.getRuntimeId = getRuntimeId;
    }

    @Override
    public void execute(ExecutionEvent event, IWorkbenchPage activePage, IIpsSrcFile ipsSrcFile)
            throws ExecutionException {
        ISelection selection = HandlerUtil.getCurrentSelectionChecked(event);
        Clipboard clipboard = new Clipboard(HandlerUtil.getActiveShellChecked(event).getDisplay());
        copyRuntimeIdToClipboard(selection, clipboard);
    }

    public void copyRuntimeIdToClipboard(ISelection selection, Clipboard clipboard) {
        TypedSelection<IAdaptable> typedSelection = TypedSelection.createAnyCount(IAdaptable.class, selection);
        String runtimeIds = typedSelection.getElements().stream()
                .map(adapt)
                .flatMap(Optional::stream)
                .map(getRuntimeId)
                .collect(Collectors.joining(System.lineSeparator()));
        Object[] ids = new Object[] { runtimeIds };
        clipboard.setContents(ids, new Transfer[] { TextTransfer.getInstance() });
    }

}
