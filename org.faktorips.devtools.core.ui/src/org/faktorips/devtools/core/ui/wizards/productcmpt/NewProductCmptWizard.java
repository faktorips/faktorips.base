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

import org.eclipse.core.resources.IProject;
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
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
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
public class NewProductCmptWizard extends Wizard implements INewWizard {

    public static final String ID = "newProductCmptWizard"; //$NON-NLS-1$

    private final NewProductCmptPMO pmo;
    private final TypeSelectionPage typeSelectionPage;
    private final ProductCmptPage productCmptPage;
    private FolderAndPackagePage folderAndPackagePage;
    private NewProdutCmptValidator validator;
    private boolean skipFirstPage;

    /**
     * Creating a the new wizard.
     */
    public NewProductCmptWizard() {
        super();
        setWindowTitle(Messages.NewProductCmptWizard_title);
        setDefaultPageImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(
                "wizards/NewProductCmptWizard.png")); //$NON-NLS-1$
        pmo = new NewProductCmptPMO(IpsPlugin.getDefault().getIpsPreferences().getWorkingDate());
        loadDialogSettings();
        validator = new NewProdutCmptValidator(pmo);
        typeSelectionPage = new TypeSelectionPage(pmo);
        productCmptPage = new ProductCmptPage(pmo);
        folderAndPackagePage = new FolderAndPackagePage(pmo);
    }

    @Override
    public void addPages() {
        addPage(typeSelectionPage);
        addPage(productCmptPage);
        addPage(folderAndPackagePage);
    }

    @Override
    public IWizardPage getStartingPage() {
        if (skipFirstPage) {
            return getPages()[1];
        } else {
            return getPages()[0];
        }
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
                monitor.beginTask(Messages.NewProductCmptWizard_title, 4);
                IIpsSrcFile ipsSrcFile = createIpsSrcFile(monitor);
                finishIpsSrcFile(ipsSrcFile, monitor);
                ipsSrcFile.save(true, new SubProgressMonitor(monitor, 1));
                addToProductCmpt(ipsSrcFile, new SubProgressMonitor(monitor, 1));
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
        IIpsSrcFile srcFile = pmo.getIpsPackage().getIpsSrcFile(getIpsObjectType().getFileName(pmo.getFullName()));
        if (pmo.isOpenEditor()) {
            IpsUIPlugin.getDefault().openEditor(srcFile);
        }
        safeDialogSettings();
        return true;
    }

    protected void finishIpsSrcFile(IIpsSrcFile ipsSrcFile, IProgressMonitor monitor) throws CoreException {
        IProductCmpt newProductCmpt = (IProductCmpt)ipsSrcFile.getIpsObject();
        newProductCmpt.setProductCmptType(pmo.getSelectedType().getQualifiedName());

        GregorianCalendar date = pmo.getWorkingDate();
        newProductCmpt.setRuntimeId(pmo.getRuntimeId());
        IProductCmptGeneration generation = (IProductCmptGeneration)newProductCmpt.newGeneration();
        generation.setValidFrom(date);
        newProductCmpt.fixAllDifferencesToModel(pmo.getIpsProject());
        monitor.worked(1);
    }

    protected IIpsSrcFile createIpsSrcFile(IProgressMonitor monitor) throws CoreException {
        IIpsSrcFile ipsSrcFile = pmo.getIpsPackage().createIpsFile(getIpsObjectType(), pmo.getFullName(), true,
                new SubProgressMonitor(monitor, 1));
        return ipsSrcFile;
    }

    protected void addToProductCmpt(IIpsSrcFile newProductCmpt, IProgressMonitor monitor) {
        monitor.beginTask(null, 2);
        if (pmo.getAddToProductCmptGeneration() != null && pmo.getAddToAssociation() != null) {
            IIpsSrcFile srcFile = pmo.getAddToProductCmptGeneration().getIpsSrcFile();
            if (pmo.getValidator().validateAddToGeneration().isEmpty()) {
                boolean dirty = srcFile.isDirty();

                IProductCmptLink newLink = pmo.getAddToProductCmptGeneration().newLink(pmo.getAddToAssociation());
                newLink.setTarget(newProductCmpt.getQualifiedNameType().getName());
                monitor.worked(1);
                if (!dirty) {
                    try {
                        srcFile.save(true, new SubProgressMonitor(monitor, 1));
                    } catch (CoreException e) {
                        throw new CoreRuntimeException(e);
                    }
                }
            }
        }
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        Object element = selection.getFirstElement();
        if (element instanceof IAdaptable) {
            IAdaptable adaptableObject = (IAdaptable)element;
            try {
                IIpsElement ipsElement = (IIpsElement)adaptableObject.getAdapter(IIpsElement.class);
                if (ipsElement instanceof IIpsPackageFragmentRoot) {
                    IIpsPackageFragmentRoot ipsPackageRoot = (IIpsPackageFragmentRoot)ipsElement;
                    initDefaults(ipsPackageRoot.getDefaultIpsPackageFragment(), null, null);
                } else if (ipsElement instanceof IIpsPackageFragment) {
                    IIpsPackageFragment packageFragment = (IIpsPackageFragment)ipsElement;
                    initDefaults(packageFragment, null, null);
                } else if (ipsElement instanceof IIpsSrcFile
                        && ((IIpsSrcFile)ipsElement).getIpsObjectType().equals(IpsObjectType.PRODUCT_CMPT)) {
                    IIpsSrcFile ipsSrcFile = (IIpsSrcFile)ipsElement;
                    IProductCmpt defaultProductCmpt = (IProductCmpt)((IIpsSrcFile)ipsElement).getIpsObject();
                    IProductCmptType cmptType = defaultProductCmpt.findProductCmptType(ipsSrcFile.getIpsProject());
                    initDefaults(ipsSrcFile.getIpsPackageFragment(), cmptType, defaultProductCmpt);
                } else {
                    IResource resource = (IResource)adaptableObject.getAdapter(IResource.class);
                    if (resource != null) {
                        IProject project = resource.getProject();
                        IIpsProject ipsProject = IpsPlugin.getDefault().getIpsModel().getIpsProject(project);
                        IIpsPackageFragmentRoot ipsPackageFragmentRoot = ipsProject.getSourceIpsPackageFragmentRoots()[0];
                        initDefaults(ipsPackageFragmentRoot.getDefaultIpsPackageFragment(), null, null);
                    }
                }
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
        }
    }

    /**
     * Setting the defaults for the new product component wizard.
     * <p>
     * The default package set the project, the package root and the package fragment. The default
     * package should not be null, if it is null, the method does nothing. The default type may be
     * null, if not null it is used to specify the base type as well as the current selected type,
     * if it is not abstract. The default product component may also be null. It does not change the
     * default type but is used to fill default kind id and version id.
     * 
     * @param defaultPackage Used for default project, package root and package fragment, should not
     *            be null.
     * @param defaultType The type to initialize the selected base type and selected concrete type
     * @param defaultProductCmpt a product component to initialize the version id and kind id - does
     *            not set the default type!
     */
    public void initDefaults(IIpsPackageFragment defaultPackage,
            IProductCmptType defaultType,
            IProductCmpt defaultProductCmpt) {
        pmo.initDefaults(defaultPackage, defaultType, defaultProductCmpt);
        skipFirstPage = defaultType != null;

    }

    /**
     * Setting a product component generation and an association to which the newly created product
     * component should be added when wizard is finished.
     * <p>
     * This method overwrites the selected base type with the target type of the given association.
     * It uses exactly the target type of the association also it may not be in the list of
     * available base type. With this behavior the list of selectable concrete types contains
     * exactly the types that could be selected for this association. If the target type of the
     * association is not abstract it is also used as default selected type.
     * 
     * @param addToproductCmptGen The generation you want to add the new product component to
     * @param addToAssociation the association in which context the new product component is added
     * @see #initDefaults(IIpsPackageFragment, IProductCmptType, IProductCmpt)
     */
    public void setAddToAssociation(IProductCmptGeneration addToproductCmptGen,
            IProductCmptTypeAssociation addToAssociation) {
        pmo.setAddToAssociation(addToproductCmptGen, addToAssociation);
    }

    private void loadDialogSettings() {
        IDialogSettings section = IpsUIPlugin.getDefault().getDialogSettings().getSection(ID);
        if (section != null) {
            boolean openEditor = section.getBoolean(NewProductCmptPMO.PROPERTY_OPEN_EDITOR);
            pmo.setOpenEditor(openEditor);
        }
    }

    private void safeDialogSettings() {
        IDialogSettings section = IpsUIPlugin.getDefault().getDialogSettings().addNewSection(ID);
        section.put(NewProductCmptPMO.PROPERTY_OPEN_EDITOR, pmo.isOpenEditor());
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    protected IpsObjectType getIpsObjectType() {
        return IpsObjectType.PRODUCT_CMPT;
    }

}
