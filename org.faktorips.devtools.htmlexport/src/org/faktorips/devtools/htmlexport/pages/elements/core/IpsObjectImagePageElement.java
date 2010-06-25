package org.faktorips.devtools.htmlexport.pages.elements.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.ImageData;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.testcase.ITestObject;
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
public class IpsObjectImagePageElement extends ImagePageElement {

    /**
     * @param element
     * @param title
     * @param path
     */
    public IpsObjectImagePageElement(IIpsElement element, String title, String path) {
        super(createImageDataByIpsElement(element), title, path);
    }

    /**
     * @param element
     */
    public IpsObjectImagePageElement(IIpsElement element) {
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
            IProductCmpt object = (IProductCmpt)element;
            try {
                IProductCmptType productCmptType = object.getIpsProject().findProductCmptType(
                        object.getProductCmptType());
                if (productCmptType.isUseCustomInstanceIcon()) {
                    return object.getQualifiedName();
                }
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

        return element.getName();
    }
}
