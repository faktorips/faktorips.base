/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.enumimport;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.abstraction.Wrappers;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controls.EnumRefControl;
import org.faktorips.devtools.core.ui.wizards.ipsimport.SelectImportTargetPage;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.enums.IEnumContent;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.enums.IEnumValueContainer;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.runtime.Severity;

/**
 * A wizard page to select an existing Enum Type or Content.
 * 
 * @author Thorsten Waertel
 * @author Roman Grutza
 */
public class SelectEnumPage extends SelectImportTargetPage {

    public SelectEnumPage(IStructuredSelection selection) throws JavaModelException {
        super(selection, Messages.SelectEnumPage_title);
    }

    @Override
    public void createControl(Composite parent) {
        UIToolkit toolkit = new UIToolkit(null);
        validateInput = false;
        setTitle(Messages.SelectEnumPage_title);

        pageControl = new Composite(parent, SWT.NONE);
        pageControl.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout pageLayout = new GridLayout(1, false);
        pageLayout.verticalSpacing = 20;
        pageControl.setLayout(pageLayout);
        setControl(pageControl);

        Composite locationComposite = toolkit.createLabelEditColumnComposite(pageControl);
        toolkit.createFormLabel(locationComposite, Messages.SelectEnumPage_locationLabel);
        projectControl = toolkit.createIpsProjectRefControl(locationComposite);
        projectField = new TextButtonField(projectControl);
        projectField.addChangeListener(this);

        Label line = new Label(pageControl, SWT.SEPARATOR | SWT.HORIZONTAL);
        line.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        createEnumImportControls(toolkit);

        setDefaults(selectedResource);

        validateInput = true;
    }

    private void createEnumImportControls(UIToolkit toolkit) {
        Composite lowerComposite = toolkit.createLabelEditColumnComposite(pageControl);
        toolkit.createFormLabel(lowerComposite, Messages.SelectEnumPage_targetTypeLabel);
        importTargetControl = toolkit.createEnumRefControl(null, lowerComposite, true, false);
        importTargetField = new TextButtonField(importTargetControl);
        importTargetField.addChangeListener(this);
    }

    @Override
    public IIpsObject getTargetForImport() {
        return ((EnumRefControl)importTargetControl).findEnum();
    }

    @Override
    protected void setDefaults(IResource selectedResource) {
        super.setDefaults(selectedResource);
        if (selectedResource == null) {
            setTargetForImport(null);
            return;
        }
        IIpsElement element = IIpsModel.get().getIpsElement(Wrappers.wrap(selectedResource).as(AResource.class));
        if (element instanceof IIpsSrcFile src) {
            setTargetForImport(src.getIpsObject());
        }
        if (element == null) {
            setTargetForImport(null);
        }
    }

    private void setTargetForImport(IIpsObject ipsObject) {
        if (ipsObject != null && ipsObject instanceof IEnumValueContainer valueContainer) {
            setEnum(valueContainer);
        }
    }

    @Override
    protected void validateImportTarget() {
        if (importTargetControl.getText().length() == 0) {
            setErrorMessage(Messages.SelectEnumPage_msgEnumEmpty);
            return;
        }
        IEnumValueContainer enumValueContainer = (IEnumValueContainer)getTargetForImport();
        if (!validateEnumValueContainerExists(enumValueContainer)) {
            setErrorMessage(Messages.SelectEnumPage_msgMissingContent);
            return;
        }
        if (enumValueContainer instanceof IEnumType enumType) {
            if (enumType.isAbstract()) {
                setErrorMessage(Messages.SelectEnumPage_msgAbstractEnumType);
                return;
            }
        }
        if (enumValueContainer instanceof IEnumContent) {
            IEnumType enumType = enumValueContainer.findEnumType(enumValueContainer.getIpsProject());
            if (enumType.validate(enumType.getIpsProject()).getNoOfMessages(Severity.ERROR) > 0) {
                setErrorMessage(Messages.SelectEnumPage_msgEnumTypeNotValid);
                return;
            }
        }

        if (enumValueContainer.validate(enumValueContainer.getIpsProject()).getNoOfMessages(Severity.ERROR) > 0) {
            setMessage(Messages.SelectEnumPage_msgEnumNotValid, WARNING);
        }
    }

    private boolean validateEnumValueContainerExists(IEnumValueContainer enumValueContainer) {
        return enumValueContainer != null && enumValueContainer.exists();
    }

    private void setEnum(IEnumValueContainer enumValueContainer) {
        if (enumValueContainer == null) {
            setIpsProject(null);
            importTargetControl.updateSelection(null);
            return;
        }
        setIpsProject(enumValueContainer.getIpsProject());
        importTargetControl.updateSelection(enumValueContainer.getQualifiedNameType());
    }

}
