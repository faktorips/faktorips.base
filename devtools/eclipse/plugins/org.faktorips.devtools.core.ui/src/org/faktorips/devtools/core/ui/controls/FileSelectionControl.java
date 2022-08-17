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

    /**
     * Sets a filter to the dialog for just showing the files with the required format. The length
     * of the array of both names and extensions has to be equal.
     * 
     * @param filterNames the descriptions of the formats
     * @param filterExtensions the extensions of the formats
     */
    public void setDialogFilterExtensions(String[] filterNames, String[] filterExtensions) {
        if (filterNames.length == filterExtensions.length) {
            dialog.setFilterExtensions(filterExtensions);
            dialog.setFilterNames(filterNames);
            dialog.setFilterIndex(0);
        } else {
            throw new IllegalArgumentException(
                    "The number of the allowed file extensions has to be equal to the number of the extension names. " //$NON-NLS-1$
                            + "Number of names: " + filterNames.length //$NON-NLS-1$
                            + ". Number of extensions: " + filterExtensions.length + "."); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /**
     * Clears the extensions filter of the dialog.
     */
    public void clearDialogFilterExtensions() {
        dialog.setFilterNames(null);
        dialog.setFilterExtensions(null);
    }
}
