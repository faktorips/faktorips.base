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

package org.faktorips.devtools.core.ui.wizards.productcmpt;

import java.lang.reflect.InvocationTargetException;
import java.util.GregorianCalendar;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.WorkbenchRunnableAdapter;
import org.faktorips.util.message.MessageList;

/**
 * Wizard to create a new product component.
 * <p>
 * This wizard is used to create new product components. Normally you start with the fist page by
 * selecting an abstract product component type from a list. The list is created in context to the
 * selected project.
 * <p>
 * This wizard was completely rewritten in version 3.6
 * 
 * @author dirmeier
 * 
 */
public class NewProductCmptWizard extends Wizard implements IWorkbenchWizard {

    private final NewProductCmptPMO newProductCmptPMO;
    private final TypeSelectionPage typeSelectionPage;
    private final ProductCmptPage productCmptPage;
    private FolderAndPackagePage folderAndPackagePage;
    private NewProdutCmptValidator validator;

    /**
     * Creating a the new wizard.
     */
    public NewProductCmptWizard() {
        super();
        setWindowTitle("Create new product component");
        setDefaultPageImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(
                "wizards/NewProductCmptWizard.png")); //$NON-NLS-1$
        newProductCmptPMO = new NewProductCmptPMO(IpsPlugin.getDefault().getIpsPreferences().getWorkingDate());
        validator = new NewProdutCmptValidator(newProductCmptPMO);
        typeSelectionPage = new TypeSelectionPage(newProductCmptPMO);
        productCmptPage = new ProductCmptPage(newProductCmptPMO);
        folderAndPackagePage = new FolderAndPackagePage(newProductCmptPMO);
    }

    @Override
    public void addPages() {
        addPage(typeSelectionPage);
        addPage(productCmptPage);
        addPage(folderAndPackagePage);
    }

    @Override
    public boolean canFinish() {
        MessageList messageList = validator.validateAll();
        return super.canFinish() && !messageList.containsErrorMsg();
    }

    @Override
    public boolean performFinish() {
        IWorkspaceRunnable op = new IWorkspaceRunnable() {
            @Override
            public void run(IProgressMonitor monitor) throws CoreException, OperationCanceledException {
                monitor.beginTask("Create product component", 3);
                IIpsSrcFile ipsSrcFile = newProductCmptPMO.getIpsPackage().createIpsFile(IpsObjectType.PRODUCT_CMPT,
                        newProductCmptPMO.getFullName(), true, new SubProgressMonitor(monitor, 1));

                IProductCmpt newProductCmpt = (IProductCmpt)ipsSrcFile.getIpsObject();
                newProductCmpt.setProductCmptType(newProductCmptPMO.getSelectedType().getQualifiedName());

                GregorianCalendar date = newProductCmptPMO.getWorkingDate();
                newProductCmpt.setRuntimeId(newProductCmptPMO.getRuntimeId());
                IProductCmptGeneration generation = (IProductCmptGeneration)newProductCmpt.newGeneration();
                generation.setValidFrom(date);
                newProductCmpt.fixAllDifferencesToModel(newProductCmptPMO.getIpsProject());
                monitor.worked(1);

                ipsSrcFile.save(true, new SubProgressMonitor(monitor, 1));
                monitor.done();
            }
        };
        try {
            ISchedulingRule rule = null;
            Job job = Job.getJobManager().currentJob();
            if (job != null) {
                rule = job.getRule();
            }
            IRunnableWithProgress runnable = null;
            if (rule != null) {
                runnable = new WorkbenchRunnableAdapter(op, rule);
            } else {
                runnable = new WorkbenchRunnableAdapter(op, ResourcesPlugin.getWorkspace().getRoot());
            }
            getContainer().run(false, true, runnable);
        } catch (InvocationTargetException e) {
            IpsPlugin.logAndShowErrorDialog(e);
            return false;
        } catch (InterruptedException e) {
            return false;
        }
        IIpsSrcFile srcFile = newProductCmptPMO.getIpsPackage().getIpsSrcFile(
                IpsObjectType.PRODUCT_CMPT.getFileName(newProductCmptPMO.getQualifiedName()));
        IpsUIPlugin.getDefault().openEditor(srcFile);
        return true;
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        Object element = selection.getFirstElement();
        if (element instanceof IAdaptable) {
            IAdaptable adaptableObject = (IAdaptable)element;
            IResource resource = (IResource)adaptableObject.getAdapter(IResource.class);
            if (resource != null) {
                newProductCmptPMO.initDefaults(resource);
            }
        }
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
