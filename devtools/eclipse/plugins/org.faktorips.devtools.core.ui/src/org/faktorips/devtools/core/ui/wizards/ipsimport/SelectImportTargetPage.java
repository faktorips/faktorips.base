/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.ipsimport;

import java.util.ArrayList;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.abstraction.Wrappers;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;
import org.faktorips.devtools.core.ui.controls.IpsObjectRefControl;
import org.faktorips.devtools.core.ui.controls.IpsProjectRefControl;
import org.faktorips.devtools.core.ui.wizards.tableimport.Messages;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * A wizard page where one can specify an IPS object as the target of an import.
 * <p>
 * The page consists of two TextButtonFields, the upper one for selecting an IPS project and the
 * lower one for selecting the actual target IPS object (e.g. an Enum Type/Content or an Table
 * Content).
 *
 * @author Roman Grutza
 */
public abstract class SelectImportTargetPage extends WizardPage implements ValueChangeListener {

    public static final String PAGE_NAME = "SelectImportTargetPage"; //$NON-NLS-1$
    protected IResource selectedResource;
    protected IpsProjectRefControl projectControl;
    protected TextButtonField projectField;
    protected Composite pageControl;

    protected IpsObjectRefControl importTargetControl;
    protected TextButtonField importTargetField;

    // true if the input is validated and errors are displayed in the messages area.
    protected boolean validateInput = true;

    /**
     * @param selection The import target type is derived from the given selection if possible.
     * @param pageName The name of the wizard page to create.
     * @throws JavaModelException If the selection does not exist or if an exception occurs while
     *             accessing its corresponding resource
     */
    public SelectImportTargetPage(IStructuredSelection selection, String pageName) throws JavaModelException {
        super(pageName);
        if (selection != null) {
            selectedResource = switch (selection.getFirstElement()) {
                case IResource resource -> resource;
                case IJavaElement javaElement -> javaElement.getCorrespondingResource();
                case IIpsElement ipsElement -> ipsElement.getEnclosingResource().unwrap();
                default -> null;
            };
        }
        setPageComplete(false);
    }

    /**
     * @return The currently set project, or <code>null</code> if not set.
     */
    public IIpsProject getIpsProject() {
        return "".equals(projectField.getText()) ? null //$NON-NLS-1$
                : IIpsModel.get().getIpsProject(projectField.getText());
    }

    /**
     * Sets the given IPS project for this wizard page.
     *
     * @param project A valid IPS project.
     */
    public void setIpsProject(IIpsProject project) {
        projectControl.setIpsProject(project);
    }

    /**
     * Derives the default value for the project from the selected resource.
     *
     * @param selectedResource The resource that was selected in the current selection when the
     *            wizard was opened.
     */
    protected void setDefaults(IResource selectedResource) {
        if (selectedResource == null) {
            return;
        }
        IIpsElement element = IIpsModel.get().getIpsElement(Wrappers.wrap(selectedResource).as(AResource.class));
        if (element != null) {
            setIpsProject(element.getIpsProject());
        }
    }

    /**
     * Validate the page and generate error messages if needed. Can be overridden in subclasses to
     * add specific validation logic.
     */
    public void validatePage() {
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

    /**
     * Validates the project currently set. Error messages are set on the wizard page if an invalid
     * project is set.
     */
    protected void validateProject() {
        if (projectField.getText().equals("")) { //$NON-NLS-1$
            setErrorMessage(Messages.SelectTableContentsPage_msgProjectEmpty);
            return;
        }
        IIpsProject project = getIpsProject();
        if (project == null) {
            setErrorMessage(Messages.SelectTableContentsPage_msgNonExistingProject);
            return;
        }
        if (!project.exists()) {
            setErrorMessage(Messages.SelectTableContentsPage_msgNonExistingProject);
        }
    }

    /**
     * Validates the type into which external data will be imported. Implementors should set an
     * error message on the wizard page if an invalid target type is set.
     */
    protected abstract void validateImportTarget();

    /**
     * Returns the type into which the external data will be imported.
     *
     * @throws IpsException If an exception occurs while searching for the target type.
     */
    public abstract IIpsObject getTargetForImport() throws IpsException;

    /**
     * Saves the dialog settings to be able to restore them on future instances of this wizard page.
     */
    public void saveWidgetValues() {
        // nothing to do
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

    protected void updatePageComplete() {
        if (getErrorMessage() != null) {
            setPageComplete(false);
            return;
        }
        boolean complete = IpsStringUtils.isNotEmpty(projectField.getText())
                && IpsStringUtils.isNotEmpty(importTargetField.getText());
        setPageComplete(complete);
    }

    protected void projectChanged() {
        if ("".equals(projectField.getText())) { //$NON-NLS-1$
            importTargetControl.setIpsProjects();
            return;
        }
        IIpsProject project = IIpsModel.get().getIpsProject(projectField.getText());
        if (project.exists()) {
            importTargetControl.setIpsProjects(project);
            return;
        }
        importTargetControl.setIpsProjects(new ArrayList<>());
    }

    @Override
    public void valueChanged(FieldValueChangedEvent e) {
        if (e.field == projectField) {
            projectChanged();
        }
        if (validateInput) {
            // don't validate during control creating!
            validatePage();
        }
        updatePageComplete();
    }
}
