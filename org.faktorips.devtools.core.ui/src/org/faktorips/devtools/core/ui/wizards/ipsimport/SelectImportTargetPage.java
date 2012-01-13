/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.ipsimport;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;
import org.faktorips.devtools.core.ui.controls.IpsObjectRefControl;
import org.faktorips.devtools.core.ui.controls.IpsProjectRefControl;
import org.faktorips.devtools.core.ui.wizards.tableimport.Messages;

/**
 * A wizard page where one can specify an IPS object as the target of an import.
 * <p/>
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
            if (selection.getFirstElement() instanceof IResource) {
                selectedResource = (IResource)selection.getFirstElement();
            } else if (selection.getFirstElement() instanceof IJavaElement) {
                selectedResource = ((IJavaElement)selection.getFirstElement()).getCorrespondingResource();
            } else if (selection.getFirstElement() instanceof IIpsElement) {
                selectedResource = ((IIpsElement)selection.getFirstElement()).getEnclosingResource();
            } else {
                selectedResource = null;
            }
        }
        setPageComplete(false);
    }

    /**
     * @return The currently set project, or <code>null</code> if not set.
     */
    public IIpsProject getIpsProject() {
        return "".equals(projectField.getText()) ? null : //$NON-NLS-1$
                IpsPlugin.getDefault().getIpsModel().getIpsProject(projectField.getText());
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
        IIpsElement element = IpsPlugin.getDefault().getIpsModel().getIpsElement(selectedResource);
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
            return;
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
     * @throws CoreException If an exception occurs while searching for the target type.
     */
    public abstract IIpsObject getTargetForImport() throws CoreException;

    /**
     * Saves the dialog settings to be able to restore them on future instances of this wizard page.
     */
    public void saveWidgetValues() {

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
        boolean complete = !"".equals(projectField.getText()) //$NON-NLS-1$
                && !"".equals(importTargetField.getText()); //$NON-NLS-1$
        setPageComplete(complete);
    }

    protected void projectChanged() {
        if ("".equals(projectField.getText())) { //$NON-NLS-1$
            importTargetControl.setIpsProject(null);
            return;
        }
        IIpsProject project = IpsPlugin.getDefault().getIpsModel().getIpsProject(projectField.getText());
        if (project.exists()) {
            importTargetControl.setIpsProject(project);
            return;
        }
        importTargetControl.setIpsProject(null);
    }

    @Override
    public void valueChanged(FieldValueChangedEvent e) {
        if (e.field == projectField) {
            projectChanged();
        }
        if (validateInput) { // don't validate during control creating!
            validatePage();
        }
        updatePageComplete();
    }
}
