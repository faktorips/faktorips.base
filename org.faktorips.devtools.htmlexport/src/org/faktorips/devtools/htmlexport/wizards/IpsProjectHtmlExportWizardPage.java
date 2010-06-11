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

package org.faktorips.devtools.htmlexport.wizards;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.WizardDataTransferPage;
import org.faktorips.devtools.core.ui.controller.fields.ComboField;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;

public class IpsProjectHtmlExportWizardPage extends WizardDataTransferPage implements ValueChangeListener,
        ModifyListener, ICheckStateListener {

    private static final String PAGE_NAME = "IpsProjectHtmlExportWizardPage";

    private IStructuredSelection selection;

    // private Checkbox includeReferencedProjects;
    private Combo destinationNamesCombo;

    protected IpsProjectHtmlExportWizardPage(IStructuredSelection selection) {
        super(PAGE_NAME);
        this.selection = selection;
        setTitle("TITEL");
        setDescription("BESCHREIBUNG");

        setPageComplete(false);
    }

    @Override
    protected boolean allowNewContainerName() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void handleEvent(Event event) {
        // TODO Auto-generated method stub

    }

    @Override
    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, true));

        createDestinationGroup(composite);

        setControl(composite);
    }

    private void createDestinationGroup(Composite parent) {
        // destination specification group
        Composite destinationSelectionGroup = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        destinationSelectionGroup.setLayout(layout);
        destinationSelectionGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL
                | GridData.VERTICAL_ALIGN_FILL));

        new Label(destinationSelectionGroup, SWT.NONE).setText("ZIEL");

        // destination name entry field
        destinationNamesCombo = new Combo(destinationSelectionGroup, SWT.SINGLE | SWT.BORDER);
        ComboField destinationNameComboField = new ComboField(destinationNamesCombo);
        destinationNameComboField.addChangeListener(this);
        destinationNameComboField.setText(getDefaultDestinationDirectory());
        destinationNamesCombo.addModifyListener(this);
        destinationNamesCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        // destination browse button
        Button destinationBrowseButton = new Button(destinationSelectionGroup, SWT.PUSH);
        destinationBrowseButton.setText("BROWSE");
        destinationBrowseButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        destinationBrowseButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleDestinationBrowseButtonPressed();
            }
        });
    }

    /**
     * Open an appropriate destination browser
     */
    protected void handleDestinationBrowseButtonPressed() {
        DirectoryDialog directoryDialog = new DirectoryDialog(getContainer().getShell());
        directoryDialog.setText("DIALOG TEXT");
        directoryDialog.setFilterPath(((IProject)selection.getFirstElement()).getLocation() + File.separator + "html");

        String selectedDirectoryName = directoryDialog.open();
        if (selectedDirectoryName != null) {
            destinationNamesCombo.setText(selectedDirectoryName);
        }
    }

    @Override
    public void valueChanged(FieldValueChangedEvent e) {
        canFinish();
    }

    @Override
    public void modifyText(ModifyEvent e) {
        canFinish();
    }

    @Override
    public void checkStateChanged(CheckStateChangedEvent event) {
        canFinish();
    }

    public String getDestinationDirectory() {
        return destinationNamesCombo.getText().trim();
    }

    private String getDefaultDestinationDirectory() {
        IProject firstElement = (IProject)selection.getFirstElement();
        return firstElement.getFullPath().toOSString() + File.separator + "html";

    }

    private void canFinish() {
        if (StringUtils.isNotBlank(getDestinationDirectory())) {
            setPageComplete(true);
            return;
        }
        setPageComplete(false);
    }
}
