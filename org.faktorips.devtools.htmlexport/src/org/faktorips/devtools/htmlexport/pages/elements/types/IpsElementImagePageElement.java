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
import org.faktorips.devtools.core.model.testcase.ITestValue;
import org.faktorips.devtools.core.model.testcasetype.ITestParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestRuleParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestValueParameter;
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

    public IpsElementImagePageElement(IIpsElement element, String title, String path) {
        super(createImageDataByIpsElement(element), title, path);
    }

    public IpsElementImagePageElement(IIpsElement element) throws CoreException {
        super(createImageDataByIpsElement(element), element.getName(), getIpsElementImageName(element));
    }

    private static ImageData createImageDataByIpsElement(IIpsElement element) {
        return IpsUIPlugin.getImageHandling().getImage(element, true).getImageData();
    }

    private static String getIpsElementImageName(IIpsElement element) throws CoreException {
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
            return createImageNameByTestObject((ITestObject)element);
        }

        if (element instanceof ITestParameter) {
            return createImageNameByTestParameter((ITestParameter)element);
        }

        if (element instanceof IProductCmptTypeAssociation) {
            IProductCmptTypeAssociation assoc = (IProductCmptTypeAssociation)element;
            if (assoc.isAssoziation()) {
                return "association"; //$NON-NLS-1$
            }
            return "aggregation"; //$NON-NLS-1$
        }

        return element.getName();
    }

    private static String createImageNameByTestParameter(ITestParameter element) {
        if (element instanceof ITestRuleParameter) {
            return "testruleparameter"; //$NON-NLS-1$
        }
        if (element instanceof ITestValueParameter) {
            return "datatype"; //$NON-NLS-1$
        }
        if (element instanceof ITestPolicyCmptTypeParameter) {
            ITestPolicyCmptTypeParameter cmptTypeParameter = (ITestPolicyCmptTypeParameter)element;
            if (cmptTypeParameter.isRequiresProductCmpt()) {
                return IpsObjectType.PRODUCT_CMPT.getFileExtension();
            }
            return IpsObjectType.PRODUCT_CMPT_TYPE.getFileExtension();
        }
        throw new RuntimeException("Unknown Type of ITestParameter"); //$NON-NLS-1$
    }

    private static String createImageNameByTestObject(ITestObject element) {
        if (element instanceof ITestRule) {
            return "testrule"; //$NON-NLS-1$
        }
        if (element instanceof ITestPolicyCmpt) {
            ITestPolicyCmpt testPolicyCmpt = (ITestPolicyCmpt)element;
            if (testPolicyCmpt.isProductRelevant()) {
                return "testpolicycmptproductrelevant"; //$NON-NLS-1$
            }
            return "testpolicycmpt"; //$NON-NLS-1$
        }
        if (element instanceof ITestValue) {
            return "testvalue"; //$NON-NLS-1$
        }
        throw new RuntimeException("Unknown Type of ITestValue"); //$NON-NLS-1$
    }

    private static String createImageNameByIpsSrcFile(IIpsElement element) throws CoreException {
        IIpsSrcFile srcFile = (IIpsSrcFile)element;
        if (srcFile.getIpsObjectType() != IpsObjectType.PRODUCT_CMPT) {
            return srcFile.getIpsObjectType().getFileExtension();
        }
        IProductCmpt ipsObject = (IProductCmpt)srcFile.getIpsObject();
        return createImageNameByProductCmpt(ipsObject);
    }

    private static String createImageNameByProductCmpt(IProductCmpt object) throws CoreException {
        IProductCmptType productCmptType = object.getIpsProject().findProductCmptType(object.getProductCmptType());
        return getProductCmptImageNameByProductCmptType(productCmptType);
    }

    private static String getProductCmptImageNameByProductCmptType(IProductCmptType productCmptType)
            throws CoreException {
        if (productCmptType.isUseCustomInstanceIcon()) {
            return productCmptType.getQualifiedName();
        }

        if (productCmptType.hasSupertype()) {
            return getProductCmptImageNameByProductCmptType(productCmptType.findSuperProductCmptType(productCmptType
                    .getIpsProject()));
        }

        return IpsObjectType.PRODUCT_CMPT.getFileExtension();
    }
}
