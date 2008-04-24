/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.IPreferencePageContainer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.wizards.objectpath.IpsObjectPathContainer;

/**
 * <p>Property page for configuring the IPS object path.</p>
 * <p>This class is derived from JDT's BuildPathsPropertyPage.</p>
 * 
 * @see <div>org.eclipse.jdt.internal.ui.preferences.BuildPathsPropertyPage</div>
 * @author Roman Grutza
 */
public class IpsObjectPathPropertyPage extends PropertyPage {

    private static final String PAGE_SETTINGS = "IpsObjectPathPropertyPage"; //$NON-NLS-1$
    private static final String INDEX = "pageIndex"; //$NON-NLS-1$

    private IpsObjectPathContainer objectPathsBlock;

    /**
     * {@inheritDoc}
     */
    protected Control createContents(Composite parent) {
        // ensure the page has no special buttons
        noDefaultAndApplyButton();

        IIpsProject ipsProject = getIpsProject();
        if (ipsProject == null) {
            return null;
        }

        Control result;
        if (!ipsProject.getProject().isOpen()) {
            result = createForClosedProject(parent);
        } else {
            try {
                result = createForIpsProject(parent, ipsProject);
            } catch (CoreException e) {
                IpsPlugin.logAndShowErrorDialog(e);
                return null;
            }
        }
        Dialog.applyDialogFont(result);
        return result;
    }

    /*
     * Create property page for closed projects.
     */
    private Control createForClosedProject(Composite parent) {
        Label label = new Label(parent, SWT.LEFT);
        label.setText(Messages.IpsObjectPathsPropertyPage_closed_project_message);
        objectPathsBlock = null;
        setValid(true);
        return label;
    }


    private Control createForIpsProject(Composite parent, IIpsProject ipsProject) throws CoreException {
        IWorkbenchPreferenceContainer pageContainer = null;
        IPreferencePageContainer container = getContainer();
        if (container instanceof IWorkbenchPreferenceContainer) {
            pageContainer = (IWorkbenchPreferenceContainer)container;
        }

        objectPathsBlock = new IpsObjectPathContainer(getSettings().getInt(INDEX), pageContainer);
        objectPathsBlock.init(ipsProject, null);
        return objectPathsBlock.createControl(parent);
    }

    /*
     * Determine IPS project instance for which the property page has to be created 
     */
    private IIpsProject getIpsProject() {
        IProject project = getProject();
        IIpsProject ipsProject = null;

        if (project != null) {
            ipsProject = IpsPlugin.getDefault().getIpsModel().getIpsProject(project);
        }
        return ipsProject;
    }

    private IProject getProject() {
        IAdaptable adaptable = getElement();
        IProject project = null;
        
        if (adaptable instanceof IProject) {
            project = (IProject) adaptable;
        }
        else {
            IJavaElement elem = (IJavaElement)adaptable.getAdapter(IJavaElement.class);
            if (elem instanceof IJavaProject) {
                project = ((IJavaProject)elem).getProject();
            }
        }
        return project;
    }

    private IDialogSettings getSettings() {
        IDialogSettings dialogSettings = IpsPlugin.getDefault().getDialogSettings();
        IDialogSettings pageSettings = dialogSettings.getSection(PAGE_SETTINGS);
        if (pageSettings == null) {
            pageSettings = dialogSettings.addNewSection(PAGE_SETTINGS);
            pageSettings.put(INDEX, 0);
        }
        return pageSettings;
    }

    /**
     * {@inheritDoc}
     */
    public boolean performOk() {

        boolean result = objectPathsBlock.finish();
        return result;
    }

}
