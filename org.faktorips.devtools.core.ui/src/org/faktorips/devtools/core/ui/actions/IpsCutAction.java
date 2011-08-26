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

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.ui.commands.IpsCutHandler;

/**
 * An action to cut IpsObjectPartContainer-objects out of the model into the clipboard.
 * 
 * @author Thorsten Guenther
 */
public class IpsCutAction extends IpsAction {

    private Clipboard clipboard;
    private final IpsCutHandler cutHandler = new IpsCutHandler();

    public IpsCutAction(ISelectionProvider selectionProvider, Shell shell) {
        super(selectionProvider);
        clipboard = new Clipboard(shell.getDisplay());
    }

    @Override
    public void run(IStructuredSelection selection) {
        cutHandler.cutToClipboard(selection, clipboard);
    }
}
