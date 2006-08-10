/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.tablecontents;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.ui.controls.TableStructureRefControl;
import org.faktorips.devtools.core.ui.editors.EditDialog;


/**
 * A dialog to choose a tableStructure a tableContent is based on.
 */
public class SetStructureDialog extends EditDialog {
    
    private ITableContents contents;
    private TableStructureRefControl template;
    private String message;
    
    /**
     * Creates a new dialog to choose a tableStructure
     * 
     * @param contents The contents the choosen tableStructure is for.
     * @param parentShell The shell to be used as parent for the dialog
     * @param message The message to be displayed to the user if no error message is set.
     */
    public SetStructureDialog(ITableContents contents, Shell parentShell, String message) {
        super(parentShell, "Choose Table Structure", false);
        this.message = message;
        this.contents = contents;
    }

    /**
     * {@inheritDoc}
     */
    protected Composite createWorkArea(Composite parent) throws CoreException {
        Composite workArea = uiToolkit.createLabelEditColumnComposite(parent);
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        uiToolkit.createFormLabel(workArea, "New Structure:");
        template = new TableStructureRefControl(contents.getIpsProject(), workArea, uiToolkit);
        template.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        template.getTextControl().addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (StringUtils.isEmpty(template.getText())) {
					getButton(OK).setEnabled(false);
					String msg = NLS.bind("Structure {0} does not exist.", template.getText());
					setMessage(msg, IMessageProvider.ERROR);
				} else {
					getButton(OK).setEnabled(true);
					setMessage(message);
				}
			}
		});
        super.setMessage(message);
        
        return workArea;
    }

	/**
	 * {@inheritDoc}
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		super.getButton(OK).setEnabled(false);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void buttonPressed(int buttonId) {
		if (buttonId == OK) {
			contents.setTableStructure(template.getText());
		}
		super.buttonPressed(buttonId);
	}    
}
