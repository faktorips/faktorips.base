/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.wizards.productrelease;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.productrelease.ProductReleaseProcessor;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.productrelease.ITargetSystem;

/**
 * The deployment wizard provides the basic ui for deployments of product definition projects. On
 * the first site you have to select a project
 * 
 * @author dirmeier
 */
public class ProductReleaserBuilderWizard extends Wizard {

    private ProductReleaserBuilderWizardPage selectionPage;

    public ProductReleaserBuilderWizard() {
        selectionPage = new ProductReleaserBuilderWizardPage();
        setNeedsProgressMonitor(true);
        setWindowTitle(Messages.ReleaserBuilderWizard_title);
    }

    @Override
    public void addPages() {
        addPage(selectionPage);
    }

    @Override
    public boolean performFinish() {
        IRunnableWithProgress progress = new WorkspaceModifyOperation() {

            @Override
            protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
                    InterruptedException {
                String newVersion = selectionPage.getNewVersion();
                List<ITargetSystem> selectedTargetSystems = selectionPage.getSelectedTargetSystems();
                ProductReleaseProcessor productReleaseProcessor = selectionPage
                        .getReleaseBuilderOpertation();
                if (productReleaseProcessor != null) {
                    productReleaseProcessor.startReleaseBuilder(newVersion, selectedTargetSystems, monitor);
                } else {
                    throw new InterruptedException(Messages.ReleaserBuilderWizard_exception_NotReady);
                }
            }

        };
        try {
            getContainer().run(false, true, progress);
            return true;
        } catch (InvocationTargetException e) {
            selectionPage.setErrorMessage("Invocation Exception: " + e.getTargetException().getMessage()); //$NON-NLS-1$
            selectionPage.setPageComplete(false);
            IpsPlugin.log(e);
            return false;
        } catch (InterruptedException e) {
            selectionPage.setErrorMessage(e.getMessage());
            return false;
        }
    }

    public void setIpsProject(IIpsProject ipsProject) {
        selectionPage.setIpsProject(ipsProject);
    }

}
