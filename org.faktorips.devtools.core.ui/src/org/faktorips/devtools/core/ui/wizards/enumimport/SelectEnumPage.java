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
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.enums.IEnumValueContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controls.EnumRefControl;
import org.faktorips.devtools.core.ui.controls.IpsObjectRefControl;
import org.faktorips.devtools.core.ui.wizards.ipsimport.SelectImportTargetPage;
import org.faktorips.util.message.Message;

/**
 * A wizard page to select enum types or contents.
 * 
 * @author Thorsten Waertel
 * @author Roman Grutza
 */
public class SelectEnumPage extends SelectImportTargetPage {

    private IpsObjectRefControl enumControl;
    private TextButtonField enumField;

    // true if the input is validated and errors are displayed in the messages area.
    protected boolean validateInput = true;

    /**
     * @param pageName
     * @param selection
     * @throws JavaModelException
     */
    public SelectEnumPage(IStructuredSelection selection) throws JavaModelException {
        super(selection, Messages.SelectEnumPage_title);
    }

    /**
     * {@inheritDoc}
     */
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
        enumControl = toolkit.createEnumRefControl(null, lowerComposite, true, true);
        enumField = new TextButtonField(enumControl);
        enumField.addChangeListener(this);
    }

    /**
     * {@inheritDoc}
     */
    protected void setDefaults(IResource selectedResource) {
        super.setDefaults(selectedResource);
        try {
            if (selectedResource == null) {
                setImportTarget(null);
                return;
            }
            IIpsElement element = IpsPlugin.getDefault().getIpsModel().getIpsElement(selectedResource);
            if (element instanceof IIpsSrcFile) {
                IIpsSrcFile src = (IIpsSrcFile)element;
                setImportTarget(src.getIpsObject());
            }
            if (element == null) {
                setImportTarget(null);
            }
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
    }

    private void setImportTarget(IIpsObject ipsObject) throws CoreException {
        if (ipsObject != null && ipsObject instanceof IEnumValueContainer) {
            IEnumValueContainer valueContainer = (IEnumValueContainer)ipsObject;
            setEnum(valueContainer);
        }
    }

    private void setEnum(IEnumValueContainer enumValueContainer) {
        if (enumValueContainer == null) {
            enumControl.setText(""); //$NON-NLS-1$
            setIpsProject(null);
            return;
        }
        enumControl.setText(enumValueContainer.getQualifiedName());
        setIpsProject(enumValueContainer.getIpsProject());
    }

    public IEnumValueContainer getEnum() throws CoreException {
        return ((EnumRefControl)enumControl).findEnum();
    }

    protected void contentsChanged() {

    }

    protected void projectChanged() {
        if ("".equals(projectField.getText())) { //$NON-NLS-1$
            enumControl.setIpsProject(null);
            return;
        }
        IIpsProject project = IpsPlugin.getDefault().getIpsModel().getIpsProject(projectField.getText());
        if (project.exists()) {
            enumControl.setIpsProject(project);
            return;
        }
        enumControl.setIpsProject(null);
    }

    public void valueChanged(FieldValueChangedEvent e) {
        if (e.field == projectField) {
            projectChanged();
        }
        if (e.field == enumField) {
            contentsChanged();
        }
        if (validateInput) { // don't validate during control creating!
            validatePage();
        }
        updatePageComplete();
    }

    /**
     * Validates the page and generates error messages if needed. Can be overridden in subclasses to
     * add specific validation logic.s
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
        if (enumControl.getText().length() == 0) {
            setErrorMessage(Messages.SelectEnumPage_msgEnumEmpty);
            return;
        }
        try {
            IEnumValueContainer enumValueContainer = getEnum();
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
            }
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    protected void updatePageComplete() {
        if (getErrorMessage() != null) {
            setPageComplete(false);
            return;
        }
        boolean complete = !"".equals(projectField.getText()) //$NON-NLS-1$
                && !"".equals(enumField.getText()); //$NON-NLS-1$
        setPageComplete(complete);
    }
}
