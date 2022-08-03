/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.tableimport;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.abstraction.Wrappers;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controls.TableContentsRefControl;
import org.faktorips.devtools.core.ui.wizards.ipsimport.SelectImportTargetPage;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.runtime.Message;

/**
 * A wizard page to select an existing Table Content.
 * 
 * @author Roman Grutza
 */
public class SelectTableContentsPage extends SelectImportTargetPage {

    public SelectTableContentsPage(IStructuredSelection selection) throws JavaModelException {
        super(selection, Messages.SelectTableContentsPage_title);
    }

    @Override
    public void createControl(Composite parent) {
        UIToolkit toolkit = new UIToolkit(null);
        validateInput = false;
        setTitle(Messages.SelectTableContentsPage_title);

        pageControl = new Composite(parent, SWT.NONE);
        pageControl.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout pageLayout = new GridLayout(1, false);
        pageLayout.verticalSpacing = 20;
        pageControl.setLayout(pageLayout);
        setControl(pageControl);

        Composite locationComposite = toolkit.createLabelEditColumnComposite(pageControl);
        toolkit.createFormLabel(locationComposite, Messages.SelectTableContentsPage_labelProject);
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
        toolkit.createFormLabel(lowerComposite, Messages.SelectTableContentsPage_labelContents);
        importTargetControl = toolkit.createTableContentsRefControl(null, lowerComposite);
        importTargetField = new TextButtonField(importTargetControl);
        importTargetField.addChangeListener(this);
    }

    @Override
    public IIpsObject getTargetForImport() {
        return ((TableContentsRefControl)importTargetControl).findTableContents();
    }

    @Override
    protected void setDefaults(IResource selectedResource) {
        super.setDefaults(selectedResource);
        if (selectedResource == null) {
            setTargetForImport(null);
            return;
        }
        IIpsElement element = IIpsModel.get().getIpsElement(Wrappers.wrap(selectedResource).as(AResource.class));
        if (element instanceof IIpsSrcFile) {
            IIpsSrcFile src = (IIpsSrcFile)element;
            setTargetForImport(src.getIpsObject());
        }
        if (element == null) {
            setTargetForImport(null);
        }
    }

    private void setTargetForImport(IIpsObject ipsObject) {
        if (ipsObject != null && ipsObject.getIpsObjectType() == IpsObjectType.TABLE_CONTENTS) {
            ITableContents contents = (ITableContents)ipsObject;
            setTableContents(contents);
        }
    }

    @Override
    protected void validateImportTarget() {
        if (importTargetControl.getText().length() == 0) {
            setErrorMessage(Messages.SelectTableContentsPage_msgContentsEmpty);
            return;
        }
        ITableContents tableContents = (ITableContents)getTargetForImport();
        if ((tableContents == null) || !tableContents.exists()) {
            setErrorMessage(NLS.bind(Messages.SelectTableContentsPage_msgMissingContent,
                    importTargetControl.getText()));
            return;
        }
        ITableStructure structure = tableContents.findTableStructure(tableContents.getIpsProject());
        if (structure.validate(structure.getIpsProject()).getNoOfMessages(Message.ERROR) > 0) {
            setErrorMessage(Messages.SelectTableContentsPage_msgStructureNotValid);
        }

    }

    private void setTableContents(ITableContents contents) {
        if (contents == null) {
            importTargetControl.setText(""); //$NON-NLS-1$
            setIpsProject(null);
            return;
        }
        importTargetControl.setText(contents.getQualifiedName());
        setIpsProject(contents.getIpsProject());
    }
}
