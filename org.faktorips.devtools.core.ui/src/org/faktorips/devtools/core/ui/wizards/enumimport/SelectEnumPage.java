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

package org.faktorips.devtools.core.ui.wizards.enumimport;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValueContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controls.EnumRefControl;
import org.faktorips.devtools.core.ui.wizards.ipsimport.SelectImportTargetPage;
import org.faktorips.util.message.Message;

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
        importTargetControl = toolkit.createEnumRefControl(null, lowerComposite, true, true);
        importTargetField = new TextButtonField(importTargetControl);
        importTargetField.addChangeListener(this);
    }

    @Override
    public IIpsObject getTargetForImport() throws CoreException {
        // Return the Enum which currently holds the values if an IEnumType and an IEnumContent with
        // the same full qualified name exist
        final IEnumValueContainer enum1 = ((EnumRefControl)importTargetControl).findEnum(false);
        final IEnumValueContainer enum2 = ((EnumRefControl)importTargetControl).findEnum(true);

        if (enum1 == enum2) {
            return enum1;
        }

        if (enum1.isCapableOfContainingValues()) {
            return enum1;
        } else {
            return enum2;
        }
    }

    @Override
    protected void setDefaults(IResource selectedResource) {
        super.setDefaults(selectedResource);
        try {
            if (selectedResource == null) {
                setTargetForImport(null);
                return;
            }
            IIpsElement element = IpsPlugin.getDefault().getIpsModel().getIpsElement(selectedResource);
            if (element instanceof IIpsSrcFile) {
                IIpsSrcFile src = (IIpsSrcFile)element;
                setTargetForImport(src.getIpsObject());
            }
            if (element == null) {
                setTargetForImport(null);
            }
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
    }

    private void setTargetForImport(IIpsObject ipsObject) throws CoreException {
        if (ipsObject != null && ipsObject instanceof IEnumValueContainer) {
            IEnumValueContainer valueContainer = (IEnumValueContainer)ipsObject;
            setEnum(valueContainer);
        }
    }

    @Override
    protected void validateImportTarget() {
        if (importTargetControl.getText().length() == 0) {
            setErrorMessage(Messages.SelectEnumPage_msgEnumEmpty);
            return;
        }
        try {
            IEnumValueContainer enumValueContainer = (IEnumValueContainer)getTargetForImport();
            if (enumValueContainer == null) {
                setErrorMessage(Messages.SelectEnumPage_msgMissingContent);
                return;
            }
            if (!enumValueContainer.exists()) {
                setErrorMessage(Messages.SelectEnumPage_msgMissingContent);
                return;
            }
            if (enumValueContainer.validate(enumValueContainer.getIpsProject()).getNoOfMessages(Message.ERROR) > 0) {
                setErrorMessage(Messages.SelectEnumPage_msgEnumNotValid);
                return;
            }
            if (enumValueContainer instanceof IEnumType) {
                IEnumType enumType = (IEnumType)enumValueContainer;
                if (enumType.isAbstract()) {
                    setErrorMessage(Messages.SelectEnumPage_msgAbstractEnumType);
                    return;
                }
                if (!(enumType.isContainingValues())) {
                    setErrorMessage(Messages.SelectEnumPage_msgEnumTypeNotContainingValues);
                    return;
                }
            }
            if (enumValueContainer instanceof IEnumContent) {
                IEnumType enumType = enumValueContainer.findEnumType(enumValueContainer.getIpsProject());
                if (enumType.validate(enumType.getIpsProject()).getNoOfMessages(Message.ERROR) > 0) {
                    setErrorMessage(Messages.SelectEnumPage_msgEnumTypeNotValid);
                }
            }
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    private void setEnum(IEnumValueContainer enumValueContainer) {
        if (enumValueContainer == null) {
            importTargetControl.setText("");
            setIpsProject(null);
            return;
        }
        importTargetControl.setText(enumValueContainer.getQualifiedName());
        setIpsProject(enumValueContainer.getIpsProject());
    }

}
