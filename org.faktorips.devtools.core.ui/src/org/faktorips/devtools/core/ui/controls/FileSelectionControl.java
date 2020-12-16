/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.faktorips.devtools.core.ui.UIToolkit;

/**
 * 
 * @author Thorsten Waertel
 */
public class FileSelectionControl extends TextButtonControl {

    private final FileDialog dialog;

    /**
     * Create a new control for file selection
     * 
     * @param parent the parent component of this control
     * @param toolkit the ui toolkit
     * @param dialogStyle the style of the dialog, common styles are {@link SWT#SAVE} or
     *            {@link SWT#OPEN}
     */
    public FileSelectionControl(Composite parent, UIToolkit toolkit, int dialogStyle) {
        super(parent, toolkit, Messages.FileSelectionControl_titleBrowse);
        dialog = new FileDialog(getShell(), dialogStyle);
    }

    @Override
    protected void buttonClicked() {
        askForFilename();
    }

    /**
     * Open the file dialog to ask the user to select the file. Returns the selected filename
     */
    protected String askForFilename() {
        getDialog().setFileName(getText());
        String newFile = getDialog().open();
        if (newFile != null) {
            setText(newFile);
        }
        return newFile;
    }

    public String getFile() {
        return getText();
    }

    public void setFile(String file) {
        setText(file);
    }

    /**
     * @return Returns the dialog.
     */
    public FileDialog getDialog() {
        return dialog;
    }
}
