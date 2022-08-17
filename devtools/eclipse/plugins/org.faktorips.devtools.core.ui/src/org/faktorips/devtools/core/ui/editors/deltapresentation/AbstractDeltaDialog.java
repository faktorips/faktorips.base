/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.deltapresentation;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.ui.UIToolkit;

/**
 * @author dirmeier
 */
public abstract class AbstractDeltaDialog extends TitleAreaDialog {

    protected UIToolkit toolkit;

    public AbstractDeltaDialog(Shell parentShell) {
        super(parentShell);
        toolkit = new UIToolkit(null);
    }

    @Override
    protected Control createButtonBar(Composite parent) {
        Control buttons = super.createButtonBar(parent);
        super.getButton(OK).setText(Messages.AbstractDeltaDialog_Button_Fix);
        super.getButton(CANCEL).setText(Messages.AbstractDeltaDialog_Button_Ignore);
        return buttons;
    }

    protected void updateDeltaView(Object delta) {
        getTreeViewer().setInput(delta);
        getTreeViewer().refresh();
        getTreeViewer().expandAll();
        getTreeViewer().getControl().redraw();
        getParentShell().layout();
    }

    protected abstract TreeViewer getTreeViewer();
}
