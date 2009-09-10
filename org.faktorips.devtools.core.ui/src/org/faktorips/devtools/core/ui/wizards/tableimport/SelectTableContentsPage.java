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

package org.faktorips.devtools.core.ui.wizards.tableimport;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controls.TableContentsRefControl;
import org.faktorips.devtools.core.ui.wizards.ipsimport.IpsObjectImportWizard;
import org.faktorips.devtools.core.ui.wizards.ipsimport.SelectImportTargetPage;
import org.faktorips.util.message.Message;

public class SelectTableContentsPage extends SelectImportTargetPage {

    private TableContentsRefControl contentsControl;
    private TextButtonField contentsField;
    private boolean validateInput;

    public SelectTableContentsPage(IStructuredSelection selection) throws JavaModelException {
        this(selection, "Select table contents");
    }

    public SelectTableContentsPage(IStructuredSelection selection, String pageName) throws JavaModelException {
        super(selection, pageName);
    }

    public void createControl(Composite parent) {
        UIToolkit toolkit = new UIToolkit(null);
        validateInput = false;
        setTitle(Messages.SelectContentsPage_title);

        pageControl = new Composite(parent, SWT.NONE);
        pageControl.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout pageLayout = new GridLayout(1, false);
        pageLayout.verticalSpacing = 20;
        pageControl.setLayout(pageLayout);
        setControl(pageControl);

        Composite locationComposite = toolkit.createLabelEditColumnComposite(pageControl);
        toolkit.createFormLabel(locationComposite, Messages.SelectContentsPage_labelProject);
        projectControl = toolkit.createIpsProjectRefControl(locationComposite);
        projectField = new TextButtonField(projectControl);
        projectField.addChangeListener(this);

        Label line = new Label(pageControl, SWT.SEPARATOR | SWT.HORIZONTAL);
        line.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        createTableImportControls(toolkit);

        setDefaults(selectedResource);

        validateInput = true;

    }

    private void createTableImportControls(UIToolkit toolkit) {
        Composite lowerComposite = toolkit.createLabelEditColumnComposite(pageControl);
        toolkit.createFormLabel(lowerComposite, Messages.SelectContentsPage_labelContents);
        contentsControl = toolkit.createTableContentsRefControl(null, lowerComposite);
        contentsField = new TextButtonField(contentsControl);
        contentsField.addChangeListener(this);
    }

    private void setImportDestination(IIpsObject ipsObject) throws CoreException {
        if (ipsObject != null && ipsObject.getIpsObjectType() == IpsObjectType.TABLE_CONTENTS) {
            ITableContents contents = (ITableContents)ipsObject;
            setTableContents(contents);
        }
    }

    public ITableContents getTableContents() throws CoreException {
        return (contentsControl).findTableContents();
    }

    @Override
    protected void setDefaults(IResource selectedResource) {
        super.setDefaults(selectedResource);
        try {
            if (selectedResource == null) {
                setImportDestination(null);
                return;
            }
            IIpsElement element = IpsPlugin.getDefault().getIpsModel().getIpsElement(selectedResource);
            if (element instanceof IIpsSrcFile) {
                IIpsSrcFile src = (IIpsSrcFile)element;
                setImportDestination(src.getIpsObject());
            }
            if (element == null) {
                setImportDestination(null);
            }
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
    }

    /**
     * Validates the page and generates error messages if needed. Can be overridden in subclasses to
     * add specific validation logic.
     */
    protected void validatePage() {
        setMessage("", IMessageProvider.NONE); //$NON-NLS-1$
        setErrorMessage(null);
        validateProject();
        if (getErrorMessage() != null) {
            return;
        }
        validateImportTarget();
        if (getErrorMessage() != null) {
            return;
        }
        updatePageComplete();
    }

    protected void validateImportTarget() {
        if (contentsControl.getText().length() == 0) {
            setErrorMessage(Messages.SelectContentsPage_msgContentsEmpty);
            return;
        }
        try {
            ITableContents tableContents = getTableContents();
            if (tableContents == null) {
                setErrorMessage(NLS.bind(Messages.SelectContentsPage_msgMissingContent, contentsControl.getText()));
                return;
            }
            if (!tableContents.exists()) {
                setErrorMessage(NLS.bind(Messages.SelectContentsPage_msgMissingContent, contentsControl.getText()));
                return;
            }
            if (tableContents.validate(tableContents.getIpsProject()).getNoOfMessages(Message.ERROR) > 0) {
                setErrorMessage(Messages.SelectContentsPage_msgContentsNotValid);
                return;
            }
            ITableStructure structure = tableContents.findTableStructure(tableContents.getIpsProject());
            if (structure.validate(structure.getIpsProject()).getNoOfMessages(Message.ERROR) > 0) {
                setErrorMessage(Messages.SelectContentsPage_msgStructureNotValid);
            }
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }

    }

    private void setTableContents(ITableContents contents) {
        if (contents == null) {
            contentsControl.setText(""); //$NON-NLS-1$
            setIpsProject(null);
            return;
        }
        contentsControl.setText(contents.getQualifiedName());
        setIpsProject(contents.getIpsProject());
    }

    protected void updatePageComplete() {
        if (getErrorMessage() != null) {
            setPageComplete(false);
            return;
        }
        boolean complete = !"".equals(projectField.getText()) //$NON-NLS-1$
                && !"".equals(contentsField.getText()); //$NON-NLS-1$
        setPageComplete(complete);
    }

    public void valueChanged(FieldValueChangedEvent e) {
        if (e.field == projectField) {
            projectChanged();
        }
        if (validateInput) { // don't validate during control creating!
            validatePage();
        }
        updatePageComplete();
    }

    private void projectChanged() {
        contentsControl.setIpsProject(getIpsProject());
    }

    @Override
    public boolean canFlipToNextPage() {
        IWizard wizard = getWizard();
        if (wizard instanceof IpsObjectImportWizard) {
            if (((IpsObjectImportWizard)wizard).isExcelTableFormatSelected()) {
                // do not show the configuration/preview page for excel
                return false;
            }
        }

        return super.canFlipToNextPage();
    }

}
