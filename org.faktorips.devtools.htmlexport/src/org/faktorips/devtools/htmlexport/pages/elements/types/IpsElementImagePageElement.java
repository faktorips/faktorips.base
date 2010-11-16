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

package org.faktorips.devtools.htmlexport.pages.elements.types;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.ImageData;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.testcase.ITestObject;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestRule;
import org.faktorips.devtools.core.model.testcasetype.ITestParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestRuleParameter;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.htmlexport.pages.elements.core.ImagePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;

/**
 * A {@link PageElement} representing an image for an {@link IIpsElement}.
 * 
 * @author dicker
 * 
 */
public class IpsElementImagePageElement extends ImagePageElement {

    public IpsElementImagePageElement(IIpsElement element) {
        super(createImageDataByIpsElement(element), element.getName(), getIpsElementImageName(element));
    }

    private static ImageData createImageDataByIpsElement(IIpsElement element) {
        return IpsUIPlugin.getImageHandling().getImage(element, true).getImageData();
    }

    private static String getIpsElementImageName(IIpsElement element) {
        if (element instanceof IIpsPackageFragment) {
            return "ipspackage"; //$NON-NLS-1$
        }

        if (element instanceof IIpsSrcFile) {
            return createImageNameByIpsSrcFile(element);
        }

        if (element instanceof IIpsObject) {
            return createImageNameByIpsSrcFile(((IIpsObject)element).getIpsSrcFile());
        }

        if (element instanceof ITestObject) {
            return createImageNameByTestObject(element);
        }

        if (element instanceof ITestParameter) {
            return createImageNameByTestParameter(element);
        }

        if (element instanceof IProductCmptTypeAssociation) {
            return getProductCmptImageNameByProductCmptType(((IProductCmptTypeAssociation)element).getProductCmptType())
                    + "assoc"; //$NON-NLS-1$
        }

        return element.getName();
    }

    private static String createImageNameByTestParameter(IIpsElement element) {
        if (element instanceof ITestRuleParameter) {
            return "testruleparameter"; //$NON-NLS-1$
        }
        ITestParameter testParameter = (ITestParameter)element;
        return testParameter.getDatatype();
    }

    private static String createImageNameByTestObject(IIpsElement element) {
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

    private static String createImageNameByIpsSrcFile(IIpsElement element) {
        IIpsSrcFile srcFile = (IIpsSrcFile)element;
        if (srcFile.getIpsObjectType() != IpsObjectType.PRODUCT_CMPT) {
            return srcFile.getIpsObjectType().getFileExtension();
        }
        try {
            IProductCmpt ipsObject = (IProductCmpt)srcFile.getIpsObject();
            return createImageNameByProductCmpt(ipsObject);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    private static String createImageNameByProductCmpt(IProductCmpt object) {
        try {
            IProductCmptType productCmptType = object.getIpsProject().findProductCmptType(object.getProductCmptType());

            String productCmptImageNameByProductCmptType = getProductCmptImageNameByProductCmptType(productCmptType);

            // System.out.println(object.getQualifiedName() + "\t" +
            // productCmptType.getQualifiedName() + "\t" + productCmptType.isUseCustomInstanceIcon()
            // + "\t" + productCmptImageNameByProductCmptType);
            return productCmptImageNameByProductCmptType;
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getProductCmptImageNameByProductCmptType(IProductCmptType productCmptType) {
        if (productCmptType.isUseCustomInstanceIcon()) {
            return productCmptType.getQualifiedName();
        }

        if (productCmptType.hasSupertype()) {
            try {
                return getProductCmptImageNameByProductCmptType(productCmptType
                        .findSuperProductCmptType(productCmptType.getIpsProject()));
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
        }

        return IpsObjectType.PRODUCT_CMPT.getFileExtension();
    }
}
