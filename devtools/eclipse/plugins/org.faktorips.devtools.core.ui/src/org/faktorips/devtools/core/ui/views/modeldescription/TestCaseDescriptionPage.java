/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.modeldescription;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.testcase.ITestCase;
import org.faktorips.devtools.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.model.testcasetype.ITestPolicyCmptTypeParameter;

/**
 * A page for presenting the properties of a {@link ITestCaseType} or {@link ITestCase}. This page
 * is connected to a Editor similar to the outline view.
 * 
 * @author Quirin Stoll
 */
public class TestCaseDescriptionPage extends DefaultModelDescriptionPage {

    public TestCaseDescriptionPage(ITestCaseType testCaseType) {
        super();
        setIpsObject(testCaseType);
        setDescriptionData();
    }

    @Override
    protected List<DescriptionItem> createDescriptions() {
        List<DescriptionItem> descriptions = new ArrayList<>();
        ITestPolicyCmptTypeParameter[] testPolicyCmptTypeParameters = getIpsObject().getTestPolicyCmptTypeParameters();
        for (ITestPolicyCmptTypeParameter testPolicyCmptTypeParameter : testPolicyCmptTypeParameters) {
            ITestAttribute[] testAttributes = testPolicyCmptTypeParameter.getTestAttributes();
            String parameterName = testPolicyCmptTypeParameter.getName();
            for (ITestAttribute testAttribute : testAttributes) {
                String desrcItemName = parameterName + " : " + testAttribute.getAttribute(); //$NON-NLS-1$
                String localizedDescription = IIpsModel.get().getMultiLanguageSupport()
                        .getLocalizedDescription(testAttribute);
                descriptions.add(new DescriptionItem(desrcItemName, localizedDescription));
            }
        }
        return descriptions;
    }

    @Override
    public ITestCaseType getIpsObject() {
        return (ITestCaseType)super.getIpsObject();
    }
}
