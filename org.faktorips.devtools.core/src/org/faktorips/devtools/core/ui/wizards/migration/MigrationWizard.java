/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.migration;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsProject;


/**
 * Supports migration for an IpsProject
 * 
 * @author Thorsten Guenther
 */
public class MigrationWizard extends Wizard implements IWorkbenchWizard {
    private ProjectSelectionPage projectSelectionPage;

    public MigrationWizard() {
        setWindowTitle("Migrate projects");
        setDefaultPageImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("wizards/MigrationWizard.png"));
    }
    
    public void addPages() {
        super.addPages();
        projectSelectionPage = new ProjectSelectionPage();
        super.addPage(projectSelectionPage);
        super.addPage(new MigrationPage(projectSelectionPage));
    }

    /**
     * {@inheritDoc}
     */
    public boolean performFinish() {
        try {
            IIpsProject[] projects = projectSelectionPage.getProjects();
            for (int i = 0; i < projects.length; i++) {
                getContainer().run(false, true, IpsPlugin.getDefault().getMigrationOperation(projects[i]));
            }
        }
        catch (InvocationTargetException e) {
            IpsPlugin.log(e);
        }
        catch (InterruptedException e) {
            IpsPlugin.log(e);
        }
        catch (CoreException e) {
            IpsPlugin.log(e);
        }
        return true;
    }


    /**
     * {@inheritDoc}
     */
    public void init(IWorkbench workbench, IStructuredSelection selection) {
    }
    
}
