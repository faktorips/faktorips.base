/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

    /**
     * The style of the file dialog. Common styles are {@link SWT#SAVE} or {@link SWT#OPEN}
     */
    private int dialogStyle;

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
        this.dialogStyle = dialogStyle;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void buttonClicked() {
        askForFilename();
    }

    /**
     * Open the file dialog to ask the user to select the file. Returns the selected filename
     */
    protected String askForFilename() {
        FileDialog dialog = new FileDialog(getShell(), dialogStyle);
        dialog.setFileName(getText());
        String newFile = dialog.open();
        if (newFile != null) {
            setText(newFile);
        }
        return newFile;
    }

    /**
     * @return Returns the file.
     */
    public String getFile() {
        return getText();
    }

    /**
     * @param file The file to set.
     */
    public void setFile(String file) {
        setText(file);
    }
}
