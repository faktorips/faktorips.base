/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.testcasetype;

import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.model.testcasetype.ITestParameter;
import org.faktorips.devtools.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.model.testcasetype.TestParameterType;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.util.StringUtil;

/**
 * Label provider for the test case domain.
 *
 * @author Joerg Ortmann
 */
public class TestCaseTypeLabelProvider extends DefaultLabelProvider {

    @Override
    public Image getImage(Object element) {
        return switch (element) {
            case ITestParameter testParameter -> IpsUIPlugin.getImageHandling().getImage(testParameter);
            case TestCaseTypeTreeRootElement rootElement -> rootElement.getImgage();
            default -> null;
        };
    }

    /**
     * Returns the displayed text of the test value parameter or test policy cmpt type param.<br>
     * If the element is a test value parameter then return the name of the param.<br>
     * If the element is a test policy cmpt type param return the name of the param and if a
     * association is specified and the target name is not equal the param name return "name :
     * association".
     */
    @Override
    public String getText(Object element) {
        return switch (element) {
            case ITestPolicyCmptTypeParameter testPolicyCmptTypeParam -> getText(testPolicyCmptTypeParam);
            case ITestParameter testParam -> testParam.getName() + getTypeExtension(testParam.getTestParameterType());
            case ITestAttribute testAttribute -> getText(element, testAttribute);
            case TestCaseTypeTreeRootElement rootElement -> rootElement.getText();
            default -> Messages.TestCaseTypeLabelProvider_Undefined;
        };
    }

    private String getText(Object element, ITestAttribute testAttribute) {
        String text = super.getText(element);
        String extension = ""; //$NON-NLS-1$
        if (IpsStringUtils.isNotEmpty(testAttribute.getAttribute())
                && !testAttribute.getAttribute().equals(testAttribute.getName())) {
            extension = " : " + testAttribute.getAttribute(); //$NON-NLS-1$
        }
        return text + extension + getTypeExtension(testAttribute.getTestAttributeType());
    }

    private String getText(ITestPolicyCmptTypeParameter testPolicyCmptTypeParam) {
        String targetExtension = testPolicyCmptTypeParam.getPolicyCmptType() == null
                ? "" //$NON-NLS-1$
                : StringUtil.unqualifiedName(testPolicyCmptTypeParam.getPolicyCmptType());

        if (IpsStringUtils.isNotEmpty(targetExtension)
                && !targetExtension.equals(testPolicyCmptTypeParam.getName())) {
            targetExtension = " : " + targetExtension; //$NON-NLS-1$
        } else {
            // no association or association is equal test param name
            targetExtension = ""; //$NON-NLS-1$
        }
        String productExt = testPolicyCmptTypeParam.isRequiresProductCmpt() ? " (P)" : ""; //$NON-NLS-1$ //$NON-NLS-2$

        return testPolicyCmptTypeParam.getName() + targetExtension
                + getTypeExtension(testPolicyCmptTypeParam.getTestParameterType()) + productExt;
    }

    /**
     * Returns the type extension of the given type, format: " - typeName"
     */
    private String getTypeExtension(TestParameterType type) {
        if (type == null) {
            return IpsStringUtils.EMPTY;
        }
        return " - " + type.getName(); //$NON-NLS-1$
    }

    @Override
    public void dispose() {
        // Nothing to do
    }

    @Override
    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

}
