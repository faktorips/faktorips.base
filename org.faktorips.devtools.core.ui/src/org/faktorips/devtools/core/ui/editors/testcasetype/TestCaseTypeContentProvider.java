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

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.faktorips.devtools.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.model.testcasetype.ITestPolicyCmptTypeParameter;

/**
 * Content provider for the test case domain.
 * 
 * @author Joerg Ortmann
 */
public class TestCaseTypeContentProvider implements ITreeContentProvider {

    private static Object[] EMPTY_ARRAY = new Object[0];

    private TestCaseTypeTreeRootElement rootElement;

    public TestCaseTypeContentProvider(ITestCaseType testCaseType) {
        this.rootElement = new TestCaseTypeTreeRootElement(testCaseType);
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof TestCaseTypeTreeRootElement) {
            ITestCaseType testCaseType = ((TestCaseTypeTreeRootElement)parentElement).getTestCaseType();
            return testCaseType.getTestParameters();
        } else if (parentElement instanceof ITestPolicyCmptTypeParameter) {
            ITestPolicyCmptTypeParameter testPolicyCmptTypeParam = (ITestPolicyCmptTypeParameter)parentElement;
            return testPolicyCmptTypeParam.getTestPolicyCmptTypeParamChilds();
        }
        return EMPTY_ARRAY;
    }

    @Override
    public Object getParent(Object element) {
        // only the objects above have parents, in other case no parent necessary
        return null;
    }

    @Override
    public boolean hasChildren(Object element) {
        Object[] children = getChildren(element);
        if (children == null) {
            return false;
        }
        return children.length > 0;
    }

    @Override
    public Object[] getElements(Object inputElement) {
        return new Object[] { rootElement };
    }

    @Override
    public void dispose() {
        // Nothing to do
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // nothing to do
    }

}
