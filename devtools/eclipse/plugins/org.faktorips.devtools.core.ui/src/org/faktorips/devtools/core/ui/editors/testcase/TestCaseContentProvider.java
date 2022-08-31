/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.testcase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.internal.testcase.TestCaseHierarchyPath;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.testcase.ITestCase;
import org.faktorips.devtools.model.testcase.ITestObject;
import org.faktorips.devtools.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.model.testcase.ITestPolicyCmptLink;
import org.faktorips.devtools.model.testcase.ITestValue;
import org.faktorips.devtools.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.model.testcasetype.ITestParameter;
import org.faktorips.devtools.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.model.testcasetype.ITestRuleParameter;
import org.faktorips.devtools.model.testcasetype.ITestValueParameter;
import org.faktorips.util.ArgumentCheck;

/**
 * Content provider for the test case domain.
 * 
 * @author Joerg Ortmann
 */
public class TestCaseContentProvider implements ITreeContentProvider {

    /**
     * Defines the type of the content which will be currently provided: the input objects, the
     * expected result objects, or both could be provided
     */
    public static final int COMBINED = 0;
    public static final int INPUT = 1;
    public static final int EXPECTED_RESULT = 2;

    private static final Object[] EMPTY_ARRAY = {};

    private int contentType = COMBINED;
    /** Contains the test case for which the content will be provided */
    private ITestCase testCase;

    /** Indicates if the structure should be displayed without association layer */
    private boolean withoutAssociations = false;

    /**
     * Cache containing the dummy objects, to display the association and rules. This kind of
     * objects are only used in the user interface to adapt the model objects to the correct content
     * in the tree view
     */
    private HashMap<String, IDummyTestCaseObject> dummyObjects = new HashMap<>();

    /** ips project used to search */
    private IIpsProject ipsProject;

    public TestCaseContentProvider(int contentType, ITestCase testCase) {
        ArgumentCheck.notNull(testCase);
        this.contentType = contentType;
        this.testCase = testCase;
        ipsProject = testCase.getIpsProject();
    }

    /**
     * Returns the test case.
     */
    public ITestCase getTestCase() {
        return testCase;
    }

    /**
     * Set the test case to be displayed.
     */
    public void setTestCase(ITestCase testCase) {
        this.testCase = testCase;
    }

    /**
     * Returns <code>true</code> if the content will be provided without the association layer. If
     * the complete structure will be displayed (with associations) then <code>false</code> will be
     * returned.
     */
    public boolean isWithoutAssociations() {
        return withoutAssociations;
    }

    /**
     * Set if the association layer will be shown <code>false</code> or if the association should be
     * hidden <code>true</code>.
     */
    public void setWithoutAssociations(boolean withoutAssociations) {
        this.withoutAssociations = withoutAssociations;
    }

    /**
     * Returns the int value for the corresponding type, input or expected result.
     */
    public int getContentType() {
        return contentType;
    }

    /**
     * Returns the corresponding test policy component objects.<br>
     * Input, expected result or both objects.<br>
     * Rerurns <code>null</code> if this content provider has an unknown type.
     */
    public ITestPolicyCmpt[] getTestPolicyCmpts() {
        if (isInput()) {
            return testCase.getInputTestPolicyCmpts();
        } else if (isExpectedResult()) {
            return testCase.getExpectedResultTestPolicyCmpts();
        } else if (isCombined()) {
            return testCase.getTestPolicyCmpts();
        }
        return null;
    }

    /**
     * Returns the corresponding test value objects.<br>
     * Input or expected result objects.<br>
     * Rerurns <code>null</code> if this content provider has an unknown type.
     */
    public ITestValue[] getTestValues() {
        if (isCombined()) {
            return testCase.getTestValues();
        } else if (isExpectedResult()) {
            return testCase.getExpectedResultTestValues();
        } else if (isInput()) {
            return testCase.getInputTestValues();
        }
        return null;
    }

    /**
     * Returns the all test objects.<br>
     * Input or expected result objects.<br>
     * Rerurns <code>null</code> if this content provider has no test objects.
     */
    public ITestObject[] getTestObjects() {
        if (isCombined()) {
            return testCase.getTestObjects();
        } else if (isExpectedResult()) {
            return testCase.getExpectedResultTestObjects();
        } else if (isInput()) {
            return testCase.getInputTestObjects();
        }
        return null;
    }

    /**
     * Returns <code>true</code> if this content provider provides the input objetcs of the test
     * case.
     */
    public boolean isInput() {
        return contentType == INPUT || contentType == COMBINED;
    }

    /**
     * Returns <code>true</code> if this content provider provides the expected result objects of
     * the test case.
     */
    public boolean isExpectedResult() {
        return contentType == EXPECTED_RESULT || contentType == COMBINED;
    }

    /**
     * Returns <code>true</code> if this content provider provides the expected result and input
     * objects of the test case.
     */
    public boolean isCombined() {
        return contentType == COMBINED;
    }

    /**
     * Sets the content type of the content provider.
     */
    public void setContentType(int contentType) {
        this.contentType = contentType;
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof ITestPolicyCmpt) {
            return getChildsForTestPolicyCmpt((ITestPolicyCmpt)parentElement);
        } else if (parentElement instanceof ITestPolicyCmptLink) {
            return getChildsForTestPolicyCmptAssociation((ITestPolicyCmptLink)parentElement);
        } else if (parentElement instanceof TestCaseTypeAssociation) {
            return getChildsForTestCaseTypeAssociation((TestCaseTypeAssociation)parentElement);
        } else if (parentElement instanceof TestCaseTypeRule) {
            return testCase.getTestRule(((TestCaseTypeRule)parentElement).getName());
        }
        return EMPTY_ARRAY;
    }

    @Override
    public Object getParent(Object element) {
        if (element instanceof ITestPolicyCmpt) {
            return ((ITestPolicyCmpt)element).getParent();
        } else if (element instanceof ITestPolicyCmptLink) {
            return ((ITestPolicyCmptLink)element).getParent();
        } else if (element instanceof TestCaseTypeAssociation) {
            return ((TestCaseTypeAssociation)element).getParentTestPolicyCmpt();
        }
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

    /**
     * Returns the content of the test case this provider belongs to.
     */
    public Object[] getElements() {
        return getElements(testCase);
    }

    @Override
    public Object[] getElements(Object inputElement) {
        List<ITestObject> elements = new ArrayList<>();
        if (inputElement instanceof ITestCase) {
            addElementsFor((ITestCase)inputElement, elements);
        }

        ITestCaseType testCaseType = null;
        try {
            testCaseType = testCase.findTestCaseType(ipsProject);
            // CSOFF: Empty Statement
        } catch (IpsException e) {
            // ignore exception while retrieving the test rule parameter
        }
        // CSON: Empty Statement
        if (testCaseType == null) {
            // if the test case type is missing then show the unsorted list of all existing test
            // objects
            return elements.toArray(new Object[elements.size()]);
        }

        return sortListAndAddDummyElements(elements, testCaseType);
    }

    /**
     * returns the ordered list, the ordered list depends on the test case type, because the test
     * rule objects displayed as group and the root test policy cmpt type node is a dummy node
     * depending on the root parameter in the type
     */
    private Object[] sortListAndAddDummyElements(List<ITestObject> elements, ITestCaseType testCaseType) {
        // create helper map
        List<Object> resultList = new ArrayList<>();
        HashMap<String, List<ITestObject>> name2elements = new HashMap<>();
        for (ITestObject element : elements) {
            List<ITestObject> existingElements = name2elements.computeIfAbsent(element.getTestParameterName(),
                    $ -> new ArrayList<>(1));
            existingElements.add(element);
        }

        // sort and add dummy nodes for root policy cmpt type params and rules
        ITestParameter[] params = testCaseType.getTestParameters();

        for (ITestParameter param : params) {
            List<ITestObject> testObjects = name2elements.get(param.getName());
            name2elements.remove(param.getName());

            addToResultList(resultList, param, testObjects);
        }

        // add all elements which are not in the test parameter on the end
        // -> invalid test objects
        for (List<ITestObject> elementsWithNoParams : name2elements.values()) {
            resultList.addAll(elementsWithNoParams);
        }

        return resultList.toArray(new Object[resultList.size()]);
    }

    private void addToResultList(List<Object> resultList, ITestParameter param, List<ITestObject> testObjects) {
        if (param instanceof ITestPolicyCmptTypeParameter) {
            // dummy root node for all test policy cmpt type parameter
            if (parameterMatchesType(param)) {
                resultList.add(getDummyObject(param, null));
            }
        } else if (param instanceof ITestRuleParameter) {
            if (isCombined() || isExpectedResult()) {
                // test rule objects are not visible if the input filter is chosen
                resultList.add(getDummyObject(param, null));
            }
        } else if (testObjects != null && param instanceof ITestValueParameter) {
            if (parameterMatchesType(param)) {
                resultList.addAll(testObjects);
            }
        }
    }

    private void addElementsFor(ITestCase testCase, List<ITestObject> elements) {
        if (isCombined()) {
            // return input and expected result objects
            elements.addAll(Arrays.asList(testCase.getTestObjects()));
        } else if (isExpectedResult()) {
            // return expected result objects
            elements.addAll(Arrays.asList(testCase.getExpectedResultTestObjects()));
        } else if (isInput()) {
            // return input objects
            elements.addAll(Arrays.asList(testCase.getInputTestObjects()));
        }
    }

    @Override
    public void dispose() {
        // Nothing to do
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // nothing to do
    }

    /**
     * Returns all test policy component objects which are provided by this provider.
     */
    public ITestPolicyCmpt[] getPolicyCmpts() {
        if (isCombined()) {
            return testCase.getTestPolicyCmpts();
        } else if (isInput()) {
            return testCase.getInputTestPolicyCmpts();
        } else if (isExpectedResult()) {
            return testCase.getExpectedResultTestPolicyCmpts();
        } else {
            return new ITestPolicyCmpt[0];
        }
    }

    /**
     * Finds the test policy component by the given path.
     */
    public ITestPolicyCmpt findPolicyCmpt(String path) {
        return testCase.findTestPolicyCmpt(path);
    }

    /**
     * Returns all child of the given test case type association parameter (dummy association based
     * on the test case type definition)
     */
    private Object[] getChildsForTestCaseTypeAssociation(TestCaseTypeAssociation dummyAssociation) {
        // show instances of this test policy component type parameter

        // the result objects type could be ITestPolicyCmpt or ITestPolicyCpmtLink in case of
        // an association
        ArrayList<IIpsObjectPart> childs = new ArrayList<>();

        ITestPolicyCmpt parent = dummyAssociation.getParentTestPolicyCmpt();
        if (parent != null) {
            ITestPolicyCmptLink[] associations = parent.getTestPolicyCmptLinks(dummyAssociation.getName());
            for (ITestPolicyCmptLink association : associations) {
                if (association.isComposition()) {
                    if (isExpectedResultOrInput(association)) {
                        childs.add(association.findTarget());
                    }
                } else {
                    childs.add(association);
                }
            }
        } else {
            ITestPolicyCmpt[] testPolicyCmpts = testCase.getTestPolicyCmpts();
            for (ITestPolicyCmpt testPolicyCmpt : testPolicyCmpts) {
                if (dummyAssociation.getName().equals(testPolicyCmpt.getTestParameterName())) {
                    childs.add(testPolicyCmpt);
                }
            }
        }
        return childs.toArray(new IIpsElement[0]);
    }

    private boolean isExpectedResultOrInput(ITestPolicyCmptLink association) {
        return (isExpectedResult() && association.findTarget().isExpectedResult())
                || (isInput() && association.findTarget().isInput());
    }

    /**
     * Returns all child of the given test case association.
     */
    private Object[] getChildsForTestPolicyCmptAssociation(ITestPolicyCmptLink testPcAssociation) {
        if (testPcAssociation.isAssociation()) {
            return EMPTY_ARRAY;
        } else {
            ITestPolicyCmpt[] childs = new ITestPolicyCmpt[1];
            childs[0] = testPcAssociation.findTarget();
            return childs;
        }
    }

    /**
     * Returns childs of the test policy component.
     */
    private Object[] getChildsForTestPolicyCmpt(ITestPolicyCmpt testPolicyCmpt) {
        ITestPolicyCmptLink[] links = testPolicyCmpt.getTestPolicyCmptLinks();
        if (withoutAssociations) {
            return getChildsWithoutDummyAssociationLayer(links);
        } else {
            return getChildsWithAssociationLayer(testPolicyCmpt);
        }
    }

    private Object[] getChildsWithAssociationLayer(ITestPolicyCmpt testPolicyCmpt) {
        // group child's using the test policy component type

        // the result objects type could be IDummyTestCaseObject or ITestPolicyCpmtLink in case of
        // an association
        ArrayList<Object> childs = new ArrayList<>();
        ArrayList<String> childNames = new ArrayList<>();
        try {
            // get all child's from the test case type definition
            ITestPolicyCmptTypeParameter typeParam = testPolicyCmpt.findTestPolicyCmptTypeParameter(ipsProject);
            if (typeParam != null) {
                ITestPolicyCmptTypeParameter[] children = typeParam.getTestPolicyCmptTypeParamChilds();
                for (ITestPolicyCmptTypeParameter parameter : children) {
                    if (parameterMatchesType(parameter)) {
                        childs.add(getDummyObject(parameter, testPolicyCmpt));
                    }
                    childNames.add(parameter.getName());
                }
            }
            // add links which are not added by the test case parameter
            // association with missing test case type parameter
            ITestPolicyCmptLink[] linksInTestCase = testPolicyCmpt.getTestPolicyCmptLinks();
            for (ITestPolicyCmptLink element : linksInTestCase) {
                ITestPolicyCmptLink link = element;
                if (!childNames.contains(link.getTestPolicyCmptTypeParameter())) {
                    childs.add(link);
                }
            }
            return childs.toArray(new Object[0]);
        } catch (IpsException e) {
            /*
             * ignore model error, the model consitence between the test case type and the test case
             * will be check when openening the editor, therefore it will be ignored is here
             */
            return EMPTY_ARRAY;
        }
    }

    /**
     * Return child's without association layer.
     */
    private Object[] getChildsWithoutDummyAssociationLayer(ITestPolicyCmptLink[] links) {
        // the result objects type could be ITestPolicyCmpt or ITestPolicyCpmtLink in case of an
        // association
        List<IIpsObjectPart> childTestPolicyCmpt = new ArrayList<>(links.length);
        for (ITestPolicyCmptLink link : links) {
            if (link.isComposition()) {
                ITestPolicyCmpt target = link.findTarget();
                if (target != null) {
                    if ((isInput() && target.isInput()) || (isExpectedResult() && target.isExpectedResult())) {
                        childTestPolicyCmpt.add(target);
                    }
                }
            } else {
                // the link is an association will be added
                childTestPolicyCmpt.add(link);
            }
        }
        return childTestPolicyCmpt.toArray(new IIpsElement[0]);
    }

    /**
     * Returns a cached dummy object. To adapt the model object to the corresponding object which
     * will be displayed in the user interface.
     */
    IDummyTestCaseObject getDummyObject(ITestParameter parameter, ITestObject testObject) {
        String id = getIdFor(parameter, testObject);
        IDummyTestCaseObject dummyObject = dummyObjects.get(id);
        if (dummyObject == null) {
            if (testObject instanceof ITestPolicyCmpt) {
                dummyObject = new TestCaseTypeAssociation((ITestPolicyCmptTypeParameter)parameter,
                        (ITestPolicyCmpt)testObject);
                dummyObjects.put(id, dummyObject);
            } else if (parameter instanceof ITestRuleParameter) {
                dummyObject = new TestCaseTypeRule(testCase, (ITestRuleParameter)parameter);
                dummyObjects.put(id, dummyObject);
            } else if (testObject == null) {
                dummyObject = new TestCaseTypeAssociation((ITestPolicyCmptTypeParameter)parameter, null);
                dummyObjects.put(id, dummyObject);
            }

        }
        return dummyObject;
    }

    private String getIdFor(ITestParameter parameter, ITestObject testObject) {
        String id = ""; //$NON-NLS-1$
        if (testObject instanceof ITestPolicyCmpt) {
            id = parameter.getName() + "#" + new TestCaseHierarchyPath((ITestPolicyCmpt)testObject).toString(); //$NON-NLS-1$
        } else {
            id = parameter.getName() + (testObject == null ? "" : "#" + testObject.getName()); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return id;
    }

    /**
     * Returns <code>true</code> if the given parameter matches the current type which the content
     * provider provides.
     */
    private boolean parameterMatchesType(ITestParameter parameter) {
        return (isExpectedResult() && parameter.isExpextedResultOrCombinedParameter())
                || (isInput() && parameter.isInputOrCombinedParameter());
    }

    /**
     * Clears the dummy objects for the given test policy component and all dummy objects of the
     * child's from the cache. The dummy objects are only displayed in the gui. If an object are
     * deleted then the dummy objects must be deleted too.
     */
    public void clearChildDummyObjectsInCache(ITestPolicyCmpt testPolicyCmpt) {
        String id = ""; //$NON-NLS-1$
        try {
            id = getIdFor(testPolicyCmpt.findTestParameter(ipsProject), testPolicyCmpt);
        } catch (IpsException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
        String objectId = StringUtils.substringAfter(id, "#"); //$NON-NLS-1$
        List<String> objectsToRemove = new ArrayList<>();
        for (String currId : dummyObjects.keySet()) {
            String currObjectId = currId.indexOf("#") == -1 ? "none" : StringUtils.substringAfter(currId, "#"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            if (currObjectId.startsWith(objectId)) {
                objectsToRemove.add(currId);
            }
        }
        for (String string : objectsToRemove) {
            dummyObjects.remove(string);

        }
    }
}
