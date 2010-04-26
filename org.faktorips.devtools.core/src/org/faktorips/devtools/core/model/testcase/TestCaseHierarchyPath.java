/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.model.testcase;

/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung (Version 0.1
 * (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn GmbH - initial API and implementation
 * 
 *******************************************************************************/

import org.eclipse.core.runtime.CoreException;

/**
 * Class to evalulate and navigate a hierarchy path for test case or test case types.
 * 
 * @author Joerg Ortmann
 */
public class TestCaseHierarchyPath {
    // Separator between each hierarchy element
    public static final String SEPARATOR = "//"; //$NON-NLS-1$

    // Separator between the path and the offset index
    public static final String OFFSET_SEPARATOR = "#"; //$NON-NLS-1$

    // Contains the complete hierarchy path
    private String hierarchyPath = ""; //$NON-NLS-1$

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
    public static String evalTestPolicyCmptParamPath(ITestPolicyCmpt testPolicyCmpt) throws CoreException {
        String pathWithOffset = ""; //$NON-NLS-1$

        while (!testPolicyCmpt.isRoot()) {
            int offset = 0;
            ITestPolicyCmpt parent = testPolicyCmpt.getParentTestPolicyCmpt();
            ITestPolicyCmptLink[] links = parent.getTestPolicyCmptLinks();
            for (ITestPolicyCmptLink link : links) {
                if (link.findTarget().equals(testPolicyCmpt)) {
                    break;
                }
                // check for same parameter and increment offset if necessary
                if (link.getTestPolicyCmptTypeParameter().equals(testPolicyCmpt.getTestPolicyCmptTypeParameter())) {
                    offset++;
                }
            }
            pathWithOffset = testPolicyCmpt.getTestPolicyCmptTypeParameter() + OFFSET_SEPARATOR + offset
                    + (pathWithOffset.length() > 0 ? "." + pathWithOffset : ""); //$NON-NLS-1$ //$NON-NLS-2$
            testPolicyCmpt = parent;
        }

        // get the offset of the test policy cmpt
        // by searching test policy cmpt with the same test policy cmpt type param
        ITestCase testCase = testPolicyCmpt.getTestCase();
        ITestPolicyCmpt[] tpcs = testCase.getTestPolicyCmpts();
        int offset = 0;
        for (ITestPolicyCmpt tpc : tpcs) {
            if (testPolicyCmpt.equals(tpc)) {
                break;
            }
            // check for same parameter and increment offset if necessary
            if (testPolicyCmpt.getTestPolicyCmptTypeParameter().equals(tpc)) {
                offset++;
            }
        }
        pathWithOffset = testPolicyCmpt.getTestPolicyCmptTypeParameter() + OFFSET_SEPARATOR + offset
                + (pathWithOffset.length() > 0 ? "." + pathWithOffset : ""); //$NON-NLS-1$ //$NON-NLS-2$
        return pathWithOffset;
    }

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
     * @param currTestPolicyCmpt The test policy component link for which the path will be created.
     * @param evalForTestCase <code>true</code> if the hierarchy path will be evaluated for a test
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
            return next;
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
        while (!currTestPolicyCmpt.isRoot()) {
            if (hierarchyPath.length() > 0) {
                hierarchyPath = SEPARATOR + hierarchyPath;
            }
            ITestPolicyCmptLink testPcTypeLink = (ITestPolicyCmptLink)currTestPolicyCmpt.getParent();
            hierarchyPath = testPcTypeLink.getTestPolicyCmptTypeParameter() + hierarchyPath;
            currTestPolicyCmpt = (ITestPolicyCmpt)testPcTypeLink.getParent();
        }
        hierarchyPath = currTestPolicyCmpt.getTestPolicyCmptTypeParameter()
                + (hierarchyPath.length() > 0 ? SEPARATOR + hierarchyPath : ""); //$NON-NLS-1$
        return hierarchyPath;
    }

    private String evalHierarchyPathForTestCase(ITestPolicyCmpt currTestPolicyCmpt, String hierarchyPath) {
        while (!currTestPolicyCmpt.isRoot()) {
            if (hierarchyPath.length() > 0) {
                hierarchyPath = SEPARATOR + hierarchyPath;
            }
            hierarchyPath = SEPARATOR + currTestPolicyCmpt.getName() + hierarchyPath;
            ITestPolicyCmptLink testPcTypeLink = (ITestPolicyCmptLink)currTestPolicyCmpt.getParent();
            hierarchyPath = testPcTypeLink.getTestPolicyCmptTypeParameter() + hierarchyPath;
            currTestPolicyCmpt = (ITestPolicyCmpt)testPcTypeLink.getParent();
        }
        hierarchyPath = currTestPolicyCmpt.getName() + (hierarchyPath.length() > 0 ? SEPARATOR + hierarchyPath : ""); //$NON-NLS-1$
        return hierarchyPath;
    }
}
