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

package org.faktorips.devtools.core.ui.preferencepages;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * Property page for configuring IPS builder sets. Changes made in this page are persisted in an IPS
 * project`s configuration file (.ipsproject).
 * 
 * @author Roman Grutza
 */
public class BuilderSetPropertyPage extends PropertyPage {

    private IAdaptable element;
    private BuilderSetContainer builderSetContainer;

    @Override
    protected Control createContents(Composite parent) {
        builderSetContainer = new BuilderSetContainer(getIpsProject());
        return builderSetContainer.createContents(parent);
    }

    @Override
    public IAdaptable getElement() {
        return element;
    }

    @Override
    public void setElement(IAdaptable element) {
        this.element = element;
    }

    @Override
    public void setVisible(boolean visible) {
        if (builderSetContainer != null) {
            if (!visible) {
                if (builderSetContainer.hasChangesInDialog()) {
                    String title = Messages.BuilderSetPropertyPage_saveDialog_Title;
                    String message = Messages.BuilderSetPropertyPage_saveDialog_Message;
                    String[] buttonLabels = new String[] { Messages.BuilderSetPropertyPage_saveDialog_Apply,
                            Messages.BuilderSetPropertyPage_saveDialog_Discard,
                            Messages.BuilderSetPropertyPage_saveDialog_ApplyLater };
                    MessageDialog dialog = new MessageDialog(getShell(), title, null, message, MessageDialog.QUESTION,
                            buttonLabels, 0);
                    int res = dialog.open();
                    if (res == 0) {
                        performOk();
                    } else if (res == 1) {
                        // discard
                        builderSetContainer.init(getIpsProject());
                    } else {
                        // apply later
                    }
                }
            } else {
                if (builderSetContainer.hasChangesInDialog() && builderSetContainer.hasChangesInIpsprojectFile()) {
                    builderSetContainer.init(getIpsProject());
                }
            }
        }
        super.setVisible(visible);
    }

    @Override
    public boolean performOk() {
        if (builderSetContainer != null) {
            if (builderSetContainer.hasChangesInDialog()) {
                builderSetContainer.saveToIpsProjectFile();
            }
        }
        return super.performOk();
    }

    @Override
    protected void performDefaults() {
        super.performDefaults();
        if (builderSetContainer != null) {
            builderSetContainer.performDefaults();
        }
    }

    /**
     * Returns the IPS project instance the property page was invoked for
     */
    private IIpsProject getIpsProject() {
        IIpsProject ipsProject = null;
        if (element instanceof IProject) {
            ipsProject = IpsPlugin.getDefault().getIpsModel().getIpsProject((IProject)element);
        } else {
            IJavaElement javaElement = (IJavaElement)element.getAdapter(IJavaElement.class);
            if (javaElement instanceof IJavaProject) {
                IProject project = ((IJavaProject)javaElement).getProject();
                ipsProject = IpsPlugin.getDefault().getIpsModel().getIpsProject(project);
            }
        }
        return ipsProject;
    }

}
