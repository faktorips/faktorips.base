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

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.model.testcasetype.ITestParameter;
import org.faktorips.devtools.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.model.testcasetype.TestParameterType;
import org.faktorips.util.StringUtil;

/**
 * Label provider for the test case domain.
 * 
 * @author Joerg Ortmann
 */
public class TestCaseTypeLabelProvider extends DefaultLabelProvider {

    @Override
    public Image getImage(Object element) {
        if (element instanceof ITestParameter) {
            return IpsUIPlugin.getImageHandling().getImage((ITestParameter)element);
        } else if (element instanceof TestCaseTypeTreeRootElement) {
            return ((TestCaseTypeTreeRootElement)element).getImgage();
        }
        return null;
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
        if (element instanceof ITestPolicyCmptTypeParameter) {
            ITestPolicyCmptTypeParameter testPolicyCmptTypeParam = (ITestPolicyCmptTypeParameter)element;

            String targetExtension = testPolicyCmptTypeParam.getPolicyCmptType() == null ? "" //$NON-NLS-1$
                    : StringUtil.unqualifiedName(testPolicyCmptTypeParam.getPolicyCmptType());

            if (StringUtils.isNotEmpty(targetExtension) && !targetExtension.equals(testPolicyCmptTypeParam.getName())) {
                targetExtension = " : " + targetExtension; //$NON-NLS-1$
            } else {
                // no association or association is equal test param name
                targetExtension = ""; //$NON-NLS-1$
            }
            String productExt = testPolicyCmptTypeParam.isRequiresProductCmpt() ? " (P)" : ""; //$NON-NLS-1$ //$NON-NLS-2$

            return testPolicyCmptTypeParam.getName() + targetExtension
                    + getTypeExtension(testPolicyCmptTypeParam.getTestParameterType()) + productExt;
        } else if (element instanceof ITestParameter) {
            ITestParameter testParam = (ITestParameter)element;
            return testParam.getName() + getTypeExtension(testParam.getTestParameterType());
        } else if (element instanceof ITestAttribute) {
            String text = super.getText(element);
            ITestAttribute testAttribute = (ITestAttribute)element;
            String extension = ""; //$NON-NLS-1$
            if (StringUtils.isNotEmpty(testAttribute.getAttribute())
                    && !testAttribute.getAttribute().equals(testAttribute.getName())) {
                extension = " : " + testAttribute.getAttribute(); //$NON-NLS-1$
            }
            return text + extension + getTypeExtension(testAttribute.getTestAttributeType());
        } else if (element instanceof TestCaseTypeTreeRootElement) {
            return ((TestCaseTypeTreeRootElement)element).getText();
        }

        return Messages.TestCaseTypeLabelProvider_Undefined;
    }

    /**
     * Returns the type extension of the given type, format: " - typeName"
     */
    private String getTypeExtension(TestParameterType type) {
        if (type == null) {
            return StringUtils.EMPTY;
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
