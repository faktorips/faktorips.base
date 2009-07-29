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

package org.faktorips.devtools.core.ui.wizards.ipsimport;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;
import org.faktorips.devtools.core.ui.controls.IpsProjectRefControl;
import org.faktorips.devtools.core.ui.wizards.tableimport.Messages;

/**
 * A wizard page where one can specify an IPS object as the target of an import.
 * 
 * @author Roman Grutza
 */
public abstract class SelectImportTargetPage extends WizardPage implements ValueChangeListener {

    public static final String PAGE_NAME = "SelectImportTargetPage";
    protected IResource selectedResource;
    protected IpsProjectRefControl projectControl;
    protected TextButtonField projectField;
    protected Composite pageControl;

    public SelectImportTargetPage(IStructuredSelection selection, String pageName) throws JavaModelException {
        super(pageName);

        if (selection.getFirstElement() instanceof IResource) {
            selectedResource = (IResource)selection.getFirstElement();
        } else if (selection.getFirstElement() instanceof IJavaElement) {
            selectedResource = ((IJavaElement)selection.getFirstElement()).getCorrespondingResource();
        } else if (selection.getFirstElement() instanceof IIpsElement) {
            selectedResource = ((IIpsElement)selection.getFirstElement()).getEnclosingResource();
        } else {
            selectedResource = null;
        }
        setPageComplete(false);
    }

    public SelectImportTargetPage(String pageName, String title, ImageDescriptor titleImage) {
        super(pageName, title, titleImage);
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
     * Validates the project currently set. Error messages are set on the wizard page if an invalid
     * project is set.
     */
    protected void validateProject() {
        if (projectField.getText().equals("")) {
            setErrorMessage(Messages.SelectContentsPage_msgProjectEmpty);
            return;
        }
        IIpsProject project = getIpsProject();
        if (project == null) {
            setErrorMessage(Messages.SelectContentsPage_msgNonExistingProject);
            return;
        }
        if (!project.exists()) {
            setErrorMessage(Messages.SelectContentsPage_msgNonExistingProject);
            return;
        }
    }

    /**
     * Saves the dialog settings to be able to restore them on future instances of this wizard page.
     */
    public void saveWidgetValues() {
    }
}
