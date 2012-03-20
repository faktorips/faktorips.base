/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

import javax.xml.transform.TransformerException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.wizard.IWizardPage;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.wizards.productdefinition.FolderAndPackagePage;
import org.faktorips.devtools.core.ui.wizards.productdefinition.NewProductDefinitionWizard;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;

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
public class NewProductCmptWizard extends NewProductDefinitionWizard {

    public static final String ID = "newProductCmptWizard"; //$NON-NLS-1$

    private final TypeSelectionPage typeSelectionPage;
    private final ProductCmptPage productCmptPage;
    private final FolderAndPackagePage folderAndPackagePage;
    private final NewProductCmptValidator validator;

    /**
     * Creating a the new wizard.
     */
    public NewProductCmptWizard() {
        super(new NewProductCmptPMO());
        setWindowTitle(Messages.NewProductCmptWizard_title);
        setDefaultPageImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(
                "wizards/NewProductCmptWizard.png")); //$NON-NLS-1$
        validator = new NewProductCmptValidator(getPmo());
        typeSelectionPage = new TypeSelectionPage(getPmo());
        productCmptPage = new ProductCmptPage(getPmo());
        folderAndPackagePage = new FolderAndPackagePage(getPmo());
    }

    @Override
    protected String getDialogId() {
        return ID;
    }

    @Override
    public NewProductCmptPMO getPmo() {
        return (NewProductCmptPMO)super.getPmo();
    }

    @Override
    public void addPages() {
        addPage(typeSelectionPage);
        addPage(productCmptPage);
        addPage(folderAndPackagePage);
    }

    @Override
    public IWizardPage getPreviousPage(IWizardPage page) {
        if (page == productCmptPage && !getPmo().isFirstPageNeeded()) {
            return null;
        } else {
            return super.getPreviousPage(page);
        }
    }

    @Override
    public IWizardPage getStartingPage() {
        if (!getPmo().isFirstPageNeeded()) {
            return productCmptPage;
        } else {
            return super.getStartingPage();
        }
    }

    @Override
    public boolean canFinish() {
        MessageList messageList = validator.validateAll();
        return super.canFinish() && !messageList.containsErrorMsg();
    }

    @Override
    protected IIpsSrcFile createIpsSrcFile(IProgressMonitor monitor) throws CoreException {
        if (getPmo().isCopyMode()) {
            return copyIpsSrcFile(monitor);
        } else {
            return super.createIpsSrcFile(monitor);
        }
    }

    private IIpsSrcFile copyIpsSrcFile(IProgressMonitor monitor) throws CoreException {
        IIpsPackageFragment targetPackageFragment = getPmo().getIpsPackage();
        String fileName = IpsObjectType.PRODUCT_CMPT.getFileName(getPmo().getName());
        IIpsSrcFile ipsSrcFile = targetPackageFragment.createIpsFile(fileName, getContentsOfIpsObject(getPmo()
                .getCopyProductCmpt()), true, new SubProgressMonitor(monitor, 1));
        return ipsSrcFile;
    }

    private String getContentsOfIpsObject(IIpsObject ipsObject) {
        String encoding = ipsObject.getIpsProject().getXmlFileCharset();
        Element xml = ipsObject.toXml(IpsPlugin.getDefault().getDocumentBuilder().newDocument());
        try {
            return XmlUtil.nodeToString(xml, encoding);
        } catch (TransformerException e) {
            // This is a programming error, re-throw as runtime exception
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void finishIpsSrcFile(IIpsSrcFile ipsSrcFile, IProgressMonitor monitor) throws CoreException {
        IProductCmpt newProductCmpt = (IProductCmpt)ipsSrcFile.getIpsObject();
        newProductCmpt.setProductCmptType(getPmo().getSelectedType().getQualifiedName());
        newProductCmpt.setRuntimeId(getPmo().getRuntimeId());

        if (!getPmo().isCopyMode()) {
            IProductCmptGeneration generation = (IProductCmptGeneration)newProductCmpt.newGeneration();
            generation.setValidFrom(getPmo().getEffectiveDate());
            newProductCmpt.fixAllDifferencesToModel(getPmo().getIpsProject());
        }

        monitor.worked(1);
    }

    @Override
    protected void postProcess(IIpsSrcFile newProductCmpt, IProgressMonitor monitor) {
        monitor.beginTask(null, 2);
        if (getPmo().getAddToProductCmptGeneration() != null
                && IpsUIPlugin.getDefault().isGenerationEditable(getPmo().getAddToProductCmptGeneration())
                && getPmo().getAddToAssociation() != null) {
            IIpsSrcFile srcFile = getPmo().getAddToProductCmptGeneration().getIpsSrcFile();
            if (getPmo().getValidator().validateAddToGeneration().isEmpty()) {
                boolean dirty = srcFile.isDirty();

                IProductCmptLink newLink = getPmo().getAddToProductCmptGeneration().newLink(
                        getPmo().getAddToAssociation());
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
    protected void initDefaults(IIpsPackageFragment selectedPackage, IIpsObject selectedIpsObject) {
        try {
            if (selectedIpsObject == null) {
                initDefaults(selectedPackage, null, null);
            } else if (selectedIpsObject.getIpsObjectType().equals(getPmo().getIpsObjectType())) {
                IProductCmptType cmptType = ((IProductCmpt)selectedIpsObject).findProductCmptType(selectedIpsObject
                        .getIpsProject());
                initDefaults(selectedPackage, cmptType, (IProductCmpt)selectedIpsObject);
            } else if (selectedIpsObject.getIpsObjectType().equals(IpsObjectType.PRODUCT_CMPT_TYPE)) {
                initDefaults(selectedPackage, (IProductCmptType)selectedIpsObject, null);
            } else {
                initDefaults(selectedPackage, null, null);
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
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
        getPmo().initDefaults(defaultPackage, defaultType, defaultProductCmpt);
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
        getPmo().setAddToAssociation(addToproductCmptGen, addToAssociation);
    }

    /**
     * Configures this wizard so that it can be used to copy the provided product component.
     */
    public void setCopyProductCmpt(IProductCmpt productCmptToCopy) {
        getPmo().setCopyProductCmpt(productCmptToCopy);
        setWindowTitle(Messages.NewProductCmptWizard_copyTitle);
    }

}
