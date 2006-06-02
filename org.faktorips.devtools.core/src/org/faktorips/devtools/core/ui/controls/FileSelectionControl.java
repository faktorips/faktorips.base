/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;

/**
 * 
 * @author Thorsten Waertel
 */
public class FileSelectionControl extends TextButtonControl {
	
    public FileSelectionControl(
            Composite parent, 
            UIToolkit toolkit) {
        super(parent, toolkit, Messages.FileSelectionControl_titleBrowse);
    }
    
	/**
	 * {@inheritDoc}
	 */
	protected void buttonClicked() {
        try {
    		FileDialog dialog = new FileDialog(getShell());
    		dialog.setFileName(getText());
    		String newFile = dialog.open();
    		if (newFile != null) {
    			setText(newFile);
    		}
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
		
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
