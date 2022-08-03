/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.testcase;

import org.faktorips.devtools.model.testcase.ITestCase;
import org.faktorips.devtools.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.model.testcase.ITestPolicyCmptLink;

/**
 * Class to evaluate and navigate a hierarchy path for test case or test case types.
 * 
 * @author Joerg Ortmann
 */
public class TestCaseHierarchyPath {

    /** Separator between each hierarchy element. */
    public static final String SEPARATOR = "//"; //$NON-NLS-1$

    /** Separator between the path and the offset index. */
    public static final String OFFSET_SEPARATOR = "#"; //$NON-NLS-1$

    /** Contains the complete hierarchy path. */
    private String hierarchyPath = ""; //$NON-NLS-1$

    public TestCaseHierarchyPath(String hierarchyPath) {
        this.hierarchyPath = hierarchyPath;
    }

    /**
     * Creates a test case hierarchy path for a given test policy component.
     */
    public TestCaseHierarchyPath(ITestPolicyCmpt currTestPolicyCmpt) {
        hierarchyPath = evalHierarchyPathForTestCase(currTestPolicyCmpt, ""); //$NON-NLS-1$
    }

    /**
     * Creates a test case or test case type hierarchy path for a given test policy component.
     * 
     * @param currTestPolicyCmpt The test policy component for which the path will be created.
     * @param evalForTestCase <code>true</code> if the hierarchy path will be evaluated for a test
     *            case <code>false</code> if the hierarchy path will be evaluated for a test case
     *            type.
     */
    public TestCaseHierarchyPath(ITestPolicyCmpt currTestPolicyCmpt, boolean evalForTestCase) {
        if (evalForTestCase) {
            hierarchyPath = evalHierarchyPathForTestCase(currTestPolicyCmpt, ""); //$NON-NLS-1$
        } else {
            hierarchyPath = evalHierarchyPathForTestCaseType(currTestPolicyCmpt, ""); //$NON-NLS-1$
        }
    }

    /**
     * Creates a test case hierarchy path for a given test policy component link.
     * 
     * @param link The test policy component link for which the path will be created.
     * @param evalforTestCase <code>true</code> if the hierarchy path will be evaluated for a test
     *            case <code>false</code> if the hierarchy path will be evaluated for a test case
     *            type.
     */
    public TestCaseHierarchyPath(ITestPolicyCmptLink link, boolean evalforTestCase) {
        String linkPath = link.getTestPolicyCmptTypeParameter();
        if (evalforTestCase) {
            hierarchyPath = evalHierarchyPathForTestCase((ITestPolicyCmpt)link.getParent(), linkPath);
        } else {
            hierarchyPath = evalHierarchyPathForTestCaseType((ITestPolicyCmpt)link.getParent(), linkPath);
        }
    }

    /**
     * Removes the folder information from the beginning.
     */
    public static String unqualifiedName(String hierarchyPath) {
        int index = hierarchyPath.lastIndexOf(SEPARATOR);
        if (index == -1) {
            return hierarchyPath;
        }
        return hierarchyPath.substring(index + SEPARATOR.length());
    }

    /**
     * Evaluate the test policy component type parameter path of the given test policy component.
     */
    public static String evalTestPolicyCmptParamPath(ITestPolicyCmpt testPolicyCmpt) {
        String pathWithOffset = ""; //$NON-NLS-1$
        ITestPolicyCmpt policyCmpt = testPolicyCmpt;

        while (!policyCmpt.isRoot()) {
            int offset = 0;
            ITestPolicyCmpt parent = policyCmpt.getParentTestPolicyCmpt();
            ITestPolicyCmptLink[] links = parent.getTestPolicyCmptLinks();
            for (ITestPolicyCmptLink link : links) {
                if (link.findTarget().equals(policyCmpt)) {
                    break;
                }
                // Check for same parameter and increment offset if necessary.
                if (link.getTestPolicyCmptTypeParameter().equals(policyCmpt.getTestPolicyCmptTypeParameter())) {
                    offset++;
                }
            }
            pathWithOffset = policyCmpt.getTestPolicyCmptTypeParameter() + OFFSET_SEPARATOR + offset
                    + (pathWithOffset.length() > 0 ? "." + pathWithOffset : ""); //$NON-NLS-1$ //$NON-NLS-2$
            policyCmpt = parent;
        }

        /*
         * Get the offset of the test policy component by searching test policy component with the
         * same test policy component type parameter.
         */
        ITestCase testCase = policyCmpt.getTestCase();
        ITestPolicyCmpt[] tpcs = testCase.getTestPolicyCmpts();
        int offset = 0;
        for (ITestPolicyCmpt tpc : tpcs) {
            if (policyCmpt.equals(tpc)) {
                break;
            }
            // Check for same parameter and increment offset if necessary.
            if (policyCmpt.getTestPolicyCmptTypeParameter().equals(tpc.getTestPolicyCmptTypeParameter())) {
                offset++;
            }
        }
        return policyCmpt.getTestPolicyCmptTypeParameter() + OFFSET_SEPARATOR + offset
                + (pathWithOffset.length() > 0 ? "." + pathWithOffset : "");
    }

    /**
     * Returns the hierarchy path.
     */
    public String getHierarchyPath() {
        return hierarchyPath;
    }

    /**
     * Returns <code>true</code> if there is a next path element.
     */
    public boolean hasNext() {
        return hierarchyPath.length() > 0;
    }

    /**
     * Returns the current path element and sets the navigation pointer one element forward.
     */
    public String next() {
        String next = ""; //$NON-NLS-1$

        if (hierarchyPath.indexOf(SEPARATOR) >= 0) {
            next = hierarchyPath.substring(0, hierarchyPath.indexOf(SEPARATOR));
            hierarchyPath = hierarchyPath.substring(hierarchyPath.indexOf(SEPARATOR) + SEPARATOR.length());
        } else {
            next = hierarchyPath;
            hierarchyPath = ""; //$NON-NLS-1$
        }
        return next;
    }

    /**
     * Returns the string representation of this object.
     */
    @Override
    public String toString() {
        return hierarchyPath;
    }

    /**
     * Returns the count of path elements.
     */
    public int count() {
        int count = 0;
        TestCaseHierarchyPath tempHierarchyPath = new TestCaseHierarchyPath(hierarchyPath);
        if (!(hierarchyPath.length() > 0)) {
            return 0;
        }
        while (tempHierarchyPath.hasNext()) {
            tempHierarchyPath.next();
            count++;
        }
        return count;
    }

    /**
     * Returns the folder name for a given hierarchy path.
     */
    public static String getFolderName(String hierarchyPath) {
        int index = hierarchyPath.lastIndexOf(SEPARATOR);
        if (index == -1) {
            return ""; //$NON-NLS-1$
        }
        return hierarchyPath.substring(0, index);
    }

    private String evalHierarchyPathForTestCaseType(ITestPolicyCmpt currTestPolicyCmpt, String hierarchyPath) {
        String path = hierarchyPath;
        ITestPolicyCmpt policyCmpt = currTestPolicyCmpt;
        while (!policyCmpt.isRoot()) {
            if (path.length() > 0) {
                path = SEPARATOR + path;
            }
            ITestPolicyCmptLink testPcTypeLink = (ITestPolicyCmptLink)policyCmpt.getParent();
            path = testPcTypeLink.getTestPolicyCmptTypeParameter() + path;
            policyCmpt = (ITestPolicyCmpt)testPcTypeLink.getParent();
        }
        return policyCmpt.getTestPolicyCmptTypeParameter()
                + (path.length() > 0 ? SEPARATOR + path : "");
    }

    private String evalHierarchyPathForTestCase(ITestPolicyCmpt currTestPolicyCmpt, String hierarchyPath) {
        String path = hierarchyPath;
        ITestPolicyCmpt policyCmpt = currTestPolicyCmpt;
        while (!policyCmpt.isRoot()) {
            if (path.length() > 0) {
                path = SEPARATOR + path;
            }
            path = SEPARATOR + policyCmpt.getName() + path;
            ITestPolicyCmptLink testPcTypeLink = (ITestPolicyCmptLink)policyCmpt.getParent();
            path = testPcTypeLink.getTestPolicyCmptTypeParameter() + path;
            policyCmpt = (ITestPolicyCmpt)testPcTypeLink.getParent();
        }
        return policyCmpt.getName() + (path.length() > 0 ? SEPARATOR + path : "");
    }

}
