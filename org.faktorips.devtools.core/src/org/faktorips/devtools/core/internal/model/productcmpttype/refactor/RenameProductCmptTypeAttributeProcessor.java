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

package org.faktorips.devtools.core.internal.model.productcmpttype.refactor;

import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.refactor.RenameRefactoringProcessor;

/**
 * This is the "Rename Product Component Type Attribute" - refactoring.
 * 
 * @author Alexander Weickmann
 */
public final class RenameProductCmptTypeAttributeProcessor extends RenameRefactoringProcessor {

    /**
     * Creates a <tt>RenameProductCmptTypeAttributeProcessor</tt>.
     * 
     * @param productCmptTypeAttribute The <tt>IProductCmptTypeAttribute</tt> to be refactored.
     */
    public RenameProductCmptTypeAttributeProcessor(IProductCmptTypeAttribute productCmptTypeAttribute) {
        super(productCmptTypeAttribute);
    }

    @Override
    public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException,
            OperationCanceledException {

        RefactoringStatus status = super.checkInitialConditions(pm);
        if (!(getProductCmptTypeAttribute().isValid())) {
            status.addFatalError(NLS.bind(Messages.RenameProductCmptTypeAttributeProcessor_msgAttributeNotValid,
                    getProductCmptTypeAttribute().getName()));
        } else {
            if (!(getProductCmptTypeAttribute().getProductCmptType().isValid())) {
                status.addFatalError(NLS.bind(Messages.RenameProductCmptTypeAttributeProcessor_msgTypeNotValid,
                        getProductCmptTypeAttribute().getProductCmptType().getName()));
            }
        }
        return status;
    }

    @Override
    protected void refactorModel(IProgressMonitor pm) throws CoreException {
        updateProductCmptReferences();
        changeAttributeName();
    }

    /**
     * Updates all references to the <tt>IProductCmptTypeAttribute</tt> in referencing
     * <tt>IProductCmpt</tt>s.
     */
    private void updateProductCmptReferences() throws CoreException {
        Set<IIpsSrcFile> productCmptSrcFiles = findReferencingSourceFiles(IpsObjectType.PRODUCT_CMPT);
        for (IIpsSrcFile ipsSrcFile : productCmptSrcFiles) {
            IProductCmpt productCmpt = (IProductCmpt)ipsSrcFile.getIpsObject();

            // Continue if this product component does not reference this product component type.
            IProductCmptType referencedProductCmptType = productCmpt.findProductCmptType(productCmpt.getIpsProject());
            if (!(referencedProductCmptType.isSubtypeOrSameType(getProductCmptType(), productCmpt.getIpsProject()))) {
                continue;
            }

            for (int i = 0; i < productCmpt.getNumOfGenerations(); i++) {
                IProductCmptGeneration generation = productCmpt.getProductCmptGeneration(i);
                IAttributeValue attributeValue = generation.getAttributeValue(getOriginalElementName());
                if (attributeValue != null) {
                    attributeValue.setAttribute(getNewElementName());
                    addModifiedSrcFile(productCmpt.getIpsSrcFile());
                }
            }
        }
    }

    /**
     * Changes the name of the <tt>IProductCmptTypeAttribute</tt> to be refactored to the new name
     * provided by the user.
     */
    private void changeAttributeName() {
        getProductCmptTypeAttribute().setName(getNewElementName());
        addModifiedSrcFile(getProductCmptTypeAttribute().getIpsSrcFile());
    }

    /** Returns the <tt>IProductCmptTypeAttribute</tt> to be refactored. */
    private IProductCmptTypeAttribute getProductCmptTypeAttribute() {
        return (IProductCmptTypeAttribute)getIpsElement();
    }

    /**
     * Returns the <tt>IProductCmptType</tt> of the <tt>IProductCmptTypeAttribute</tt> to be
     * refactored.
     */
    private IProductCmptType getProductCmptType() {
        return getProductCmptTypeAttribute().getProductCmptType();
    }

    @Override
    public String getIdentifier() {
        return "RenameProductCmptTypeAttribute";
    }

    @Override
    public String getProcessorName() {
        return "Rename Product Component Type Attribute";
    }

    @Override
    public boolean isApplicable() throws CoreException {
        for (Object element : getElements()) {
            if (!(element instanceof IProductCmptTypeAttribute)) {
                return false;
            }
        }
        return true;
    }

}
