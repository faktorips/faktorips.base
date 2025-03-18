/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.productcmpt;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.ui.wizards.productdefinition.FolderAndPackagePage;
import org.faktorips.devtools.core.ui.wizards.productdefinition.NewProductDefinitionOperation;
import org.faktorips.devtools.core.ui.wizards.productdefinition.NewProductDefinitionPMO;
import org.faktorips.devtools.core.ui.wizards.productdefinition.NewProductDefinitionWizard;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;

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
public abstract class NewProductWizard extends NewProductDefinitionWizard {

    private final TypeSelectionPage typeSelectionPage;
    private final ProductCmptPage productCmptPage;
    private final FolderAndPackagePage folderAndPackagePage;

    /**
     * Creating a the new wizard.
     */
    public NewProductWizard(NewProductCmptPMO pmo) {
        super(pmo);
        setWindowTitle(NLS.bind(Messages.NewProductCmptWizard_title, getPmo().getIpsObjectType().getDisplayName()));
        typeSelectionPage = new TypeSelectionPage(getPmo());
        productCmptPage = new ProductCmptPage(getPmo());
        folderAndPackagePage = new FolderAndPackagePage(getPmo());
    }

    @Override
    public NewProductCmptPMO getPmo() {
        return (NewProductCmptPMO)super.getPmo();
    }

    @Override
    protected NewProductDefinitionOperation<? extends NewProductDefinitionPMO> getOperation() {
        if (getPmo().isCopyMode()) {
            return new CopyProductCmptOperation(getPmo());
        } else if (getPmo().isAddToMode()) {
            return new AddNewProductCmptOperation(getPmo());
        } else {
            return new NewProductCmptOperation(getPmo());
        }
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
    protected void initDefaults(IIpsPackageFragment selectedPackage, IIpsObject selectedIpsObject) {
        switch (selectedIpsObject) {
            case null -> initDefaults(selectedPackage, null, null);
            case IProductCmpt productCmpt -> initDefaults(selectedPackage,
                    productCmpt.findProductCmptType(productCmpt.getIpsProject()), productCmpt);
            case IProductCmptType productCmptType -> initDefaults(selectedPackage, productCmptType, null);
            default -> initDefaults(selectedPackage, null, null);
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
        setWindowTitle(NLS.bind(Messages.NewProductCmptWizard_copyTitle, getPmo().getIpsObjectType().getDisplayName()));
    }

}
