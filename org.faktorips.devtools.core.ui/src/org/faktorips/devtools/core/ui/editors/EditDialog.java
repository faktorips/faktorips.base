/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.faktorips.devtools.core.ui.IDataChangeableReadWriteAccess;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;

/**
 * @author Jan Ortmann
 */
public abstract class EditDialog extends TitleAreaDialog implements IDataChangeableReadWriteAccess {

    private final UIToolkit toolkit = new UIToolkit(null);

    private String windowTitle;

    private boolean tabFolderUsed;

    private boolean dataChangeable = true;

    public EditDialog(Shell shell, String windowTitle) {
        this(shell, windowTitle, false);
    }

    public EditDialog(Shell shell, String windowTitle, boolean useTabFolder) {
        super(shell);
        setShellStyle(getShellStyle() | SWT.MAX | SWT.RESIZE);
        this.windowTitle = windowTitle;
        tabFolderUsed = useTabFolder;
    }

    @Override
    public boolean isDataChangeable() {
        return dataChangeable;
    }

    @Override
    public void setDataChangeable(boolean changeable) {
        if (dataChangeable == changeable) {
            return;
        }
        dataChangeable = changeable;
        setDataChangeableThis(changeable);
    }

    /**
     * Template method that may be extended by sub classes. The default implementation sets the UI
     * toolkit's changeable state.
     * 
     * @param changeable Flag indicating the changeable state.
     */
    protected void setDataChangeableThis(boolean changeable) {
        toolkit.setDataChangeable(getDialogArea(), dataChangeable);
    }

    public void setWindowTitle(String newTitle) {
        windowTitle = newTitle;
        if (getShell() != null) {
            getShell().setText(newTitle);
        }
    }

    @Override
    protected Control createContents(Composite parent) {
        Control control = super.createContents(parent);
        setDataChangeableThis(dataChangeable);
        return control;
    }

    @Override
    protected final Control createDialogArea(Composite parent) {
        getShell().setText(windowTitle);
        Composite composite = (Composite)super.createDialogArea(parent);
        updateTitleInTitleArea();
        composite.setLayout(new GridLayout(1, false));
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        Composite panel;

        if (tabFolderUsed) {
            panel = new TabFolder(composite, SWT.TOP);
        } else {
            panel = new Composite(composite, SWT.NONE);
            panel.setLayoutData(new GridData(GridData.FILL_BOTH));
            GridLayout layout = new GridLayout(1, false);
            layout.marginHeight = 10;
            layout.marginWidth = 10;
            panel.setLayout(layout);
        }
        Composite workArea = createWorkArea(panel);
        if (workArea.getLayoutData() == null) {
            workArea.setLayoutData(new GridData(GridData.FILL_BOTH));
        }
        return composite;
    }

    /**
     * This method must create and return the UI composite containing the actual contents of the
     * dialog.
     * 
     * @param parent The parent UI composite.
     */
    protected abstract Composite createWorkArea(Composite parent);

    /**
     * Allows sub classes to change the title area's content during dialog creation.
     */
    protected void updateTitleInTitleArea() {
        // Empty default implementation
    }

    /**
     * Creates a top level composite for a tab item with standardized margins and a grid layout with
     * the given number of columns.
     */
    protected final Composite createTabItemComposite(TabFolder folder, int numOfColumns, boolean equalSize) {
        Composite c = toolkit.createGridComposite(folder, numOfColumns, equalSize, true);
        ((GridLayout)c.getLayout()).marginHeight = 12;
        return c;
    }

    protected void showValidationResult(MessageList result) {
        Message msg = result.getFirstMessage(Message.ERROR);
        if (msg == null) {
            setErrorMessage(null);
        } else {
            setErrorMessage(msg.getText());
        }
    }

    protected boolean isTabFolderUsed() {
        return tabFolderUsed;
    }

    protected final UIToolkit getToolkit() {
        return toolkit;
    }

}
