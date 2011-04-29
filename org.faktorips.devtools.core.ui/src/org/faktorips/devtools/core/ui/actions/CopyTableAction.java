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

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

public class CopyTableAction extends IpsAction {

    private final Shell shell;

    public CopyTableAction(Shell shell, ISelectionProvider selectionProvider) {
        super(selectionProvider);
        this.shell = shell;
        setText(Messages.CopyTableAction_title);
        setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("NewTableContentsCopy.gif")); //$NON-NLS-1$
    }

    @Override
    public void run(IStructuredSelection selection) {
        IpsCopyAction copyAction = new IpsCopyAction(selectionProvider, shell);
        copyAction.run();
        IpsPasteAction pasteAction = new IpsPasteAction(selectionProvider, shell);
        pasteAction.run();
    }

}
