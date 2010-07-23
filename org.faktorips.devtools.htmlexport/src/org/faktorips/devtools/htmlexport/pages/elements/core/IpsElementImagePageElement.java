/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.htmlexport.pages.elements.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.ImageData;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.testcase.ITestObject;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestRule;
import org.faktorips.devtools.core.model.testcasetype.ITestParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestRuleParameter;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

/**
 * A {@link PageElement} representing an image for an {@link IIpsElement}.
 * 
 * @author dicker
 * 
 */
public class IpsElementImagePageElement extends ImagePageElement {

    /**
     * @param element
     * @param title
     * @param path
     */
    public IpsElementImagePageElement(IIpsElement element, String title, String path) {
        super(createImageDataByIpsElement(element), title, path);
    }

    /**
     * @param element
     */
    public IpsElementImagePageElement(IIpsElement element) {
        this(element, element.getName(), getIpsElementImageName(element));
    }

    private static ImageData createImageDataByIpsElement(IIpsElement element) {
        return IpsUIPlugin.getImageHandling().getImage(element, true).getImageData();
    }

    private static String getIpsElementImageName(IIpsElement element) {
        if (element instanceof IIpsPackageFragment) {
            return "ipspackage"; //$NON-NLS-1$
        }

        if (element instanceof IProductCmpt) {
            return getProductCmptImageName((IProductCmpt)element);
        }

        if (element instanceof IIpsSrcFile) {
            IIpsSrcFile srcFile = (IIpsSrcFile)element;
            if (srcFile.getIpsObjectType() != IpsObjectType.PRODUCT_CMPT) {
                return srcFile.getIpsObjectType().getFileExtension();
            }
            try {
                IProductCmpt ipsObject = (IProductCmpt)srcFile.getIpsObject();
                return getProductCmptImageName(ipsObject);
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
        }

        if (element instanceof IIpsObject) {
            IIpsObject object = (IIpsObject)element;
            return object.getIpsObjectType().getFileExtension();
        }

        if (element instanceof ITestObject) {
            if (element instanceof ITestRule) {
                return "testrule"; //$NON-NLS-1$
            }
            if (element instanceof ITestPolicyCmpt) {
                return "testpolicycmpt"; //$NON-NLS-1$
            }
            ITestObject testObject = (ITestObject)element;
            try {
                return testObject.findTestParameter(element.getIpsProject()).getDatatype();
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
        }

        if (element instanceof ITestParameter) {
            if (element instanceof ITestRuleParameter) {
                return "testruleparameter"; //$NON-NLS-1$
            }
            ITestParameter testParameter = (ITestParameter)element;
            return testParameter.getDatatype();
        }

        if (element instanceof ProductCmptTypeAssociation) {
            ProductCmptTypeAssociation association = (ProductCmptTypeAssociation)element;
            return getProductCmptImageNameByProductCmptType(association.getProductCmptType());
        }

        return element.getName();
    }

    private static String getProductCmptImageName(IProductCmpt object) {
        try {
            IProductCmptType productCmptType = object.getIpsProject().findProductCmptType(object.getProductCmptType());
            return getProductCmptImageNameByProductCmptType(productCmptType);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getProductCmptImageNameByProductCmptType(IProductCmptType productCmptType) {
        if (productCmptType.isUseCustomInstanceIcon()) {
            return productCmptType.getQualifiedName();
        }
        return IpsObjectType.PRODUCT_CMPT_TYPE.getFileExtension();
    }
}
