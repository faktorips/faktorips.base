/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.testcase.deltapresentation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.editors.testcase.TestCaseContentProvider;
import org.faktorips.devtools.core.ui.editors.testcase.TestCaseTypeAssociation;
import org.faktorips.devtools.model.testcase.ITestAttributeValue;
import org.faktorips.devtools.model.testcase.ITestCase;
import org.faktorips.devtools.model.testcase.ITestCaseTestCaseTypeDelta;
import org.faktorips.devtools.model.testcase.ITestObject;
import org.faktorips.devtools.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.model.testcase.ITestPolicyCmptLink;
import org.faktorips.devtools.model.testcasetype.ITestAttribute;

/**
 * Content provider to represent differnces between test case and test case type.
 * 
 * @see org.faktorips.devtools.core.ui.editors.testcase.deltapresentation.TestCaseDeltaType for all
 *          provided types of delta objects.
 * 
 * @author Joerg Ortmann
 */
public class TestCaseDeltaContentProvider implements ITreeContentProvider {

    /** The base content provider for the test case tree structure */
    private TestCaseContentProvider testCaseContentProvider;

    /** Contains the delta which will be displayed. */
    private ITestCaseTestCaseTypeDelta in;

    // Contains the list from the delta from the test case side for faster search
    private List<ITestPolicyCmptLink> missingTestPolicyCmptLinks = new ArrayList<>();
    private List<ITestObject> missingTestObjects = new ArrayList<>();
    private List<ITestAttribute> missingTestAttributes = new ArrayList<>();
    private List<ITestAttributeValue> missingTestAttributeValues = new ArrayList<>();

    public TestCaseDeltaContentProvider(ITestCase testCase) {
        testCaseContentProvider = new TestCaseContentProvider(TestCaseContentProvider.COMBINED, testCase);
    }

    /**
     * Enables the filter for the delta viewer.
     */
    public void enableTestCaseDeltaViewerFilter(TreeViewer viewer) {
        viewer.addFilter(new MissingParamFilter());
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        if (newInput instanceof ITestCaseTestCaseTypeDelta) {
            in = (ITestCaseTestCaseTypeDelta)newInput;
            missingTestPolicyCmptLinks.clear();
            missingTestObjects.clear();
            missingTestAttributes.clear();
            missingTestAttributeValues.clear();
            missingTestPolicyCmptLinks.addAll(Arrays.asList(in.getTestPolicyCmptLinkWithMissingTypeParam()));
            missingTestObjects.addAll(Arrays.asList(in.getTestPolicyCmptsWithMissingTypeParam()));
            missingTestObjects.addAll(Arrays.asList(in.getTestValuesWithMissingTestValueParam()));
            missingTestObjects.addAll(Arrays.asList(in.getTestRulesWithMissingTestValueParam()));
            missingTestAttributes.addAll(Arrays.asList(in.getTestAttributesWithMissingTestAttributeValue()));
            missingTestAttributeValues.addAll(Arrays.asList(in.getTestAttributeValuesWithMissingTestAttribute()));
        }
    }

    @Override
    public void dispose() {
        // Nothing to do
    }

    /**
     * Retruns the 4 elements containing the aspects for the delta description.
     */
    // CSOFF: CyclomaticComplexity
    @Override
    public Object[] getElements(Object inputElement) {
        if (!(inputElement instanceof ITestCaseTestCaseTypeDelta)) {
            return new Object[0];
        }

        inputChanged(null, in, inputElement);

        ArrayList<TestCaseDeltaType> result = new ArrayList<>();
        boolean doNotShowDifferentSortOrder = false;

        if (in.getTestPolicyCmptTypeParametersWithMissingTestPolicyCmpt().length > 0
                || in.getTestValueParametersWithMissingTestValue().length > 0) {
            result.add(TestCaseDeltaType.MISSING_ROOT_TEST_OBJECT);
            doNotShowDifferentSortOrder = true;
        }

        if (in.getTestPolicyCmptsWithMissingTypeParam().length > 0
                || in.getTestPolicyCmptLinkWithMissingTypeParam().length > 0
                || in.getTestValuesWithMissingTestValueParam().length > 0
                || in.getTestRulesWithMissingTestValueParam().length > 0) {
            result.add(TestCaseDeltaType.MISSING_TEST_PARAM);
            doNotShowDifferentSortOrder = true;
        }

        if (in.getTestAttributesWithMissingTestAttributeValue().length > 0) {
            result.add(TestCaseDeltaType.MISSING_TEST_ATTRIBUTE_VALUE);
            doNotShowDifferentSortOrder = true;
        }

        if (in.getTestAttributeValuesWithMissingTestAttribute().length > 0) {
            result.add(TestCaseDeltaType.MISSING_TEST_ATTRIBUTE);
            doNotShowDifferentSortOrder = true;
        }

        // show the different sort order only if no other differents exists,
        // otherwise the order will be implicit fixed
        if (!doNotShowDifferentSortOrder && in.isDifferentTestParameterOrder()) {
            result.add(TestCaseDeltaType.DIFFERENT_SORT_ORDER);
            doNotShowDifferentSortOrder = true;
        }

        return result.toArray();
    }
    // CSON: CyclomaticComplexity

    @Override
    public boolean hasChildren(Object element) {
        Object[] children = getChildren(element);
        if (children == null) {
            return false;
        }
        return children.length > 0;
    }

    @Override
    public Object getParent(Object element) {
        return null;
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        ArrayList<Object> result = new ArrayList<>();

        if (parentElement == TestCaseDeltaType.MISSING_ROOT_TEST_OBJECT) {
            result.addAll(Arrays.asList(in.getTestValueParametersWithMissingTestValue()));
            result.addAll(Arrays.asList(in.getTestPolicyCmptTypeParametersWithMissingTestPolicyCmpt()));
        } else if (parentElement == TestCaseDeltaType.MISSING_TEST_PARAM) {
            return TestCaseDeltaWrapperObject.createWrapperObjects(testCaseContentProvider,
                    TestCaseDeltaType.MISSING_TEST_PARAM, null, testCaseContentProvider.getElements());
        } else if (parentElement == TestCaseDeltaType.MISSING_TEST_ATTRIBUTE) {
            return TestCaseDeltaWrapperObject.createWrapperObjects(testCaseContentProvider,
                    TestCaseDeltaType.MISSING_TEST_ATTRIBUTE, null, testCaseContentProvider.getElements());
        } else if (parentElement == TestCaseDeltaType.MISSING_TEST_ATTRIBUTE_VALUE) {
            return TestCaseDeltaWrapperObject.createWrapperObjects(testCaseContentProvider,
                    TestCaseDeltaType.MISSING_TEST_ATTRIBUTE_VALUE, null, testCaseContentProvider.getElements());
        } else if (parentElement instanceof TestCaseDeltaWrapperObject) {
            TestCaseDeltaWrapperObject wrapperObject = (TestCaseDeltaWrapperObject)parentElement;
            // create new child wrapper objects and return these children, the childs represents the
            // same content tree like the test case
            result.addAll(Arrays.asList(wrapperObject.getChildren(wrapperObject)));
            // add additional the test attributes or test attribute values if existing for the test
            // policy cmpt
            addMissingTestAttributesOrTestAttributesValue(wrapperObject, result);
        }

        return result.toArray();
    }

    /**
     * Adds test attributes or test attribute values to the given result list.
     */
    private void addMissingTestAttributesOrTestAttributesValue(TestCaseDeltaWrapperObject wrapperObject,
            ArrayList<Object> result) {

        if (wrapperObject.isHasToBeDeletedTestAttributes()) {
            for (ITestAttribute testAttr : missingTestAttributes) {
                ITestPolicyCmpt[] testPolicyCmptsWithMissingTestAttr = in
                        .getTestPolicyCmptForMissingTestAttribute(testAttr);
                if (Arrays.asList(testPolicyCmptsWithMissingTestAttr).contains(wrapperObject.getBaseObject())) {
                    result.add(testAttr);
                }
            }
        } else if (wrapperObject.isHasNewTestAttributes()) {
            for (ITestAttributeValue testAttrValue : missingTestAttributeValues) {
                if (testAttrValue.getParent().equals(wrapperObject.getBaseObject())) {
                    result.add(testAttrValue);
                }
            }
        }
    }

    /**
     * Inner class of filter implementation. Filters all objects which will be deleted or the parent
     * should be deleted. Or if test attribute should be deleted or added.
     */
    private class MissingParamFilter extends ViewerFilter {

        @Override
        public Object[] filter(Viewer viewer, Object parent, Object[] elements) {
            int size = elements.length;
            ArrayList<Object> out = new ArrayList<>(size);
            for (int i = 0; i < size; ++i) {
                Object element = elements[i];
                if (select(viewer, parent, element)) {
                    out.add(element);
                }
            }
            return out.toArray();
        }

        /**
         * The filter is always active.
         */
        @Override
        public boolean isFilterProperty(Object element, String property) {
            return true;
        }

        // CSOFF: CyclomaticComplexity
        @Override
        public boolean select(Viewer viewer, Object parentElement, Object element) {
            if (element instanceof TestCaseDeltaWrapperObject) {
                // tree content only the objects which are in the missing list will be displayed
                try {
                    TestCaseDeltaWrapperObject wrapperObject = (TestCaseDeltaWrapperObject)element;

                    // check if the current object should be deleted
                    if (checkVisibility(wrapperObject.getBaseObject(), wrapperObject.getDeltaType())
                            && wrapperObject.getDeltaType() == TestCaseDeltaType.MISSING_TEST_PARAM) {
                        wrapperObject.setWillBeDeleted(true);
                    }

                    if (checkVisibility(wrapperObject.getBaseObject(), wrapperObject.getDeltaType())
                            && wrapperObject.getDeltaType() == TestCaseDeltaType.MISSING_TEST_ATTRIBUTE) {
                        wrapperObject.setHasNewTestAttributes(true);
                    }

                    if (checkVisibility(wrapperObject.getBaseObject(), wrapperObject.getDeltaType())
                            && wrapperObject.getDeltaType() == TestCaseDeltaType.MISSING_TEST_ATTRIBUTE_VALUE) {
                        wrapperObject.setHasToBeDeletedTestAttributes(true);
                    }

                    // checks the child object, if a parent should be deleted then show the childs
                    // check if a child object is visible, if true show the parent object
                    if ((wrapperObject.isWillBeDeleted()
                            && wrapperObject.getDeltaType() == TestCaseDeltaType.MISSING_TEST_PARAM)
                            || isFilterFor(wrapperObject.getBaseObject(), wrapperObject.getDeltaType())) {
                        return true;
                    }

                    return false;
                } catch (IpsException e) {
                    IpsPlugin.logAndShowErrorDialog(e);
                    return false;
                }
            } else {
                // all other object are always visible
                return true;
            }
        }
        // CSON: CyclomaticComplexity

        /**
         * Returns <code>true</code> if the object itself or a child of the object is visible for
         * the given delta type aspect.
         */
        // CSOFF: CyclomaticComplexity
        private boolean isFilterFor(Object object, TestCaseDeltaType deltaType) {
            // the object is visible
            if (checkVisibility(object, deltaType)) {
                return true;
            }
            // search the childs
            if (object instanceof ITestPolicyCmpt) {
                ITestPolicyCmptLink[] testPolicyCmptLinks = ((ITestPolicyCmpt)object).getTestPolicyCmptLinks();
                for (ITestPolicyCmptLink testPolicyCmptLink : testPolicyCmptLinks) {
                    if (checkVisibility(testPolicyCmptLink, deltaType)) {
                        return true;
                    }
                    if (testPolicyCmptLink.isComposition()) {
                        if (isFilterFor(testPolicyCmptLink.findTarget(), deltaType)) {
                            return true;
                        }
                    }
                }
            } else if (object instanceof TestCaseTypeAssociation) {
                TestCaseTypeAssociation dummyAssociation = (TestCaseTypeAssociation)object;
                ITestPolicyCmpt parentTestPolicyCmpt = dummyAssociation.getParentTestPolicyCmpt();
                if (parentTestPolicyCmpt == null) {
                    Object[] children = testCaseContentProvider.getChildren(dummyAssociation);
                    if (children.length == 0) {
                        return false;
                    }
                    if (children.length > 1) {
                        throw new RuntimeException(
                                "Invalid test case content. More than one cildren directly under the root node!"); //$NON-NLS-1$
                    }
                    return isFilterFor(children[0], deltaType);
                }
                ITestPolicyCmptLink[] testLinks = parentTestPolicyCmpt.getTestPolicyCmptLinks(dummyAssociation
                        .getName());
                for (ITestPolicyCmptLink testLink : testLinks) {
                    if (checkVisibility(testLink, deltaType) || isFilterFor(testLink.findTarget(), deltaType)) {
                        return true;
                    }
                }
            } else if (object instanceof ITestPolicyCmptLink) {
                if (checkVisibility((ITestPolicyCmptLink)object, deltaType)) {
                    return true;
                }
            }
            return false;
        }
        // CSON: CyclomaticComplexity

        /**
         * Checks the visibility for the given object and delta type.
         */
        private boolean checkVisibility(Object object, TestCaseDeltaType deltaType) {
            if (object instanceof ITestPolicyCmptLink) {
                return checkVisibility((ITestPolicyCmptLink)object, deltaType);
            } else if (object instanceof ITestPolicyCmpt) {
                return checkVisibility((ITestPolicyCmpt)object, deltaType);
            } else if (object instanceof ITestObject) {
                return checkVisibility((ITestObject)object, deltaType);
            }
            return false;
        }

        private boolean checkVisibility(ITestPolicyCmpt testPolicyCmpt, TestCaseDeltaType deltaType) {
            if (deltaType == TestCaseDeltaType.MISSING_TEST_ATTRIBUTE_VALUE) {
                return isTestAttributeInList(testPolicyCmpt);
            } else if (deltaType == TestCaseDeltaType.MISSING_TEST_ATTRIBUTE) {
                return isTestAttributeValueInList(testPolicyCmpt);
            } else if (deltaType == TestCaseDeltaType.MISSING_TEST_PARAM) {
                return missingTestObjects.contains(testPolicyCmpt);
            }
            return false;
        }

        private boolean checkVisibility(ITestPolicyCmptLink link, TestCaseDeltaType deltaType) {
            if (deltaType == TestCaseDeltaType.MISSING_TEST_PARAM) {
                return missingTestPolicyCmptLinks.contains(link);
            }
            return false;
        }

        private boolean checkVisibility(ITestObject testObject, TestCaseDeltaType deltaType) {
            if (deltaType == TestCaseDeltaType.MISSING_TEST_PARAM) {
                return missingTestObjects.contains(testObject);
            }
            return false;
        }

        /**
         * Check if the given test policy cmpt has missing test attributes values (to be deleted)
         */
        private boolean isTestAttributeValueInList(ITestPolicyCmpt cmpt) {
            ITestAttributeValue[] testAttrValue = cmpt.getTestAttributeValues();
            for (ITestAttributeValue element : testAttrValue) {
                if (missingTestAttributeValues.contains(element)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Check if the given test policy cmpt has missing test attributes (to be added)
         */
        private boolean isTestAttributeInList(ITestPolicyCmpt cmpt) {
            for (ITestAttribute iTestAttribute : missingTestAttributes) {
                ITestPolicyCmpt[] testPolicyCmptsWithMissingTestAttr = in
                        .getTestPolicyCmptForMissingTestAttribute(iTestAttribute);
                if (Arrays.asList(testPolicyCmptsWithMissingTestAttr).contains(cmpt)) {
                    return true;
                }
            }
            return false;
        }
    }
}
