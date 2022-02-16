/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.elements.types;

import java.util.concurrent.CompletableFuture;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ImagePageElement;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.decorators.IIpsDecorators;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.testcase.ITestObject;
import org.faktorips.devtools.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.model.testcase.ITestRule;
import org.faktorips.devtools.model.testcase.ITestValue;
import org.faktorips.devtools.model.testcasetype.ITestParameter;
import org.faktorips.devtools.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.model.testcasetype.ITestRuleParameter;
import org.faktorips.devtools.model.testcasetype.ITestValueParameter;

/**
 * A {@link IPageElement} representing an image for an {@link IIpsElement}.
 * 
 * @author dicker
 * 
 */
public class IpsElementImagePageElement extends ImagePageElement {

    public IpsElementImagePageElement(IIpsElement element, String title, String path, DocumentationContext context) {
        super(createImageDataByIpsElement(element), title, path, context);
    }

    public IpsElementImagePageElement(IIpsElement element, DocumentationContext context) {
        super(createImageDataByIpsElement(element), element.getName(), getIpsElementImageName(element), context);
    }

    private static ImageData createImageDataByIpsElement(IIpsElement element) {
        if (Display.getCurrent() == null) {
            return IIpsDecorators.getImageHandling()
                    .getImage(element, true)
                    .getImageData();
        } else {
            CompletableFuture<ImageData> futureImageData = new CompletableFuture<>();
            Display.getDefault()
                    .syncExec(() -> futureImageData
                            .complete(IIpsDecorators.getImageHandling()
                                    .getImage(element, true)
                                    .getImageData()));
            return futureImageData.join();
        }
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

    private static String createImageNameByIpsSrcFile(IIpsElement element) {
        IIpsSrcFile srcFile = (IIpsSrcFile)element;
        if (srcFile.getIpsObjectType() != IpsObjectType.PRODUCT_CMPT) {
            return srcFile.getIpsObjectType().getFileExtension();
        }
        IProductCmpt ipsObject = (IProductCmpt)srcFile.getIpsObject();
        return createImageNameByProductCmpt(ipsObject);
    }

    private static String createImageNameByProductCmpt(IProductCmpt object) {
        IProductCmptType productCmptType = object.getIpsProject().findProductCmptType(object.getProductCmptType());
        return getProductCmptImageNameByProductCmptType(productCmptType);
    }

    private static String getProductCmptImageNameByProductCmptType(IProductCmptType productCmptType)
            {
        if (productCmptType.isUseCustomInstanceIcon()) {
            return productCmptType.getQualifiedName();
        }

        if (productCmptType.hasExistingSupertype(productCmptType.getIpsProject())) {
            return getProductCmptImageNameByProductCmptType(
                    productCmptType.findSuperProductCmptType(productCmptType.getIpsProject()));
        }

        return IpsObjectType.PRODUCT_CMPT.getFileExtension();
    }
}
