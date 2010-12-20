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
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.productrelease.ProductReleaseProcessor;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.productrelease.ITargetSystem;
import org.faktorips.devtools.core.productrelease.ObservableProgressMessages;

/**
 * The deployment wizard provides the basic ui for deployments of product definition projects. On
 * the first site you have to select a project
 * 
 * @author dirmeier
 */
public class ProductReleaserBuilderWizard extends Wizard {

    private ProductReleaserBuilderWizardPage selectionPage;

    private StatusPage statusPage;

    private ObservableProgressMessages observableProgressMessages;

    private boolean finished;

    public ProductReleaserBuilderWizard() {
        observableProgressMessages = new ObservableProgressMessages();
        selectionPage = new ProductReleaserBuilderWizardPage(observableProgressMessages);
        statusPage = new StatusPage();
        observableProgressMessages.addObserver(statusPage);
        setNeedsProgressMonitor(true);
        setWindowTitle(Messages.ReleaserBuilderWizard_title);
    }

    @Override
    public void addPages() {
        addPage(selectionPage);
    }

    @Override
    public IWizardPage getNextPage(IWizardPage page) {
        return super.getNextPage(page);
    }

    @Override
    public boolean canFinish() {
        return !finished && super.canFinish();
    }

    @Override
    public boolean performFinish() {
        addPage(statusPage);
        getContainer().showPage(statusPage);
        runProgress();
        return false;
    }

    private boolean runProgress() {
        Operation operation = new Operation(selectionPage.getNewVersion(), selectionPage.getSelectedTargetSystems(),
                selectionPage.getProductReleaseProcessor());
        try {
            finished = true;
            getContainer().run(true, false, operation);
            setFinishStatus(!operation.returnState);
            return false;
        } catch (InvocationTargetException e) {
            observableProgressMessages.error("Invocation Exception: " + e.getTargetException().getMessage()); //$NON-NLS-1$
            selectionPage.setPageComplete(false);
            IpsPlugin.log(e);
            return false;
        } catch (InterruptedException e) {
            observableProgressMessages.error(e.getMessage());
            return false;
        } finally {
            getContainer().updateButtons();
        }
    }

    private void setFinishStatus(boolean errorWhileFinished) {
        if (errorWhileFinished) {
            observableProgressMessages.error(Messages.ProductReleaserBuilderWizard_ccomplete_error);
        } else {
            observableProgressMessages.info(Messages.ProductReleaserBuilderWizard_complete_success);
        }
    }

    public void setIpsProject(IIpsProject ipsProject) {
        selectionPage.setIpsProject(ipsProject);
    }

    static class Operation extends WorkspaceModifyOperation {

        private final String newVersion;
        private final List<ITargetSystem> selectedTargetSystems;
        private final ProductReleaseProcessor productReleaseProcessor;

        private boolean returnState;

        public Operation(String newVersion, List<ITargetSystem> selectedTargetSystems,
                ProductReleaseProcessor productReleaseProcessor) {
            this.newVersion = newVersion;
            this.selectedTargetSystems = selectedTargetSystems;
            this.productReleaseProcessor = productReleaseProcessor;
        }

        @Override
        protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
                InterruptedException {
            if (productReleaseProcessor != null) {
                returnState = productReleaseProcessor.startReleaseBuilder(newVersion, selectedTargetSystems, monitor);
            } else {
                throw new InterruptedException(Messages.ReleaserBuilderWizard_exception_NotReady);
            }

        }

    }

}
