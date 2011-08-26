/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.ui.commands.IpsCopyHandler;

/**
 * Copy of objects controlled by FaktorIps. This action activates/deactivates itself according to
 * the current selection.
 * 
 * @author Thorsten Guenther
 * @author Stefan Widmaier
 */
public class IpsCopyAction extends IpsAction implements ISelectionChangedListener {

    private Clipboard clipboard;
    private final IpsCopyHandler copyHandler = new IpsCopyHandler();

    public IpsCopyAction(ISelectionProvider selectionProvider, Shell shell) {
        super(selectionProvider);
        clipboard = new Clipboard(shell.getDisplay());
        selectionProvider.addSelectionChangedListener(this);
    }

    @Override
    public void run(IStructuredSelection selection) {
        copyHandler.copyToClipboard(selection, clipboard);
    }

    /**
     * Disabled this action if no copyable IpsElement is selected.
     */
    @Override
    public void selectionChanged(SelectionChangedEvent event) {
        if (event.getSelection() instanceof IStructuredSelection) {
            IStructuredSelection selection = (IStructuredSelection)event.getSelection();
            Object[] objects = selection.toArray();
            boolean enabled = true;
            for (Object object : objects) {
                if (object instanceof IIpsObjectPart) {
                    enabled = false;
                }
            }
            setEnabled(enabled);
        } else {
            setEnabled(false);
        }
    }

}
