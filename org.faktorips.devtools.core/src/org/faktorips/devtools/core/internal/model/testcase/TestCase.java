/***************************************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) dürfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1
 * (vor Gründung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn GmbH - initial API and implementation
 * 
 **************************************************************************************************/

package org.faktorips.devtools.core.internal.model.testcase;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.IpsObject;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestCaseTestCaseTypeDelta;
import org.faktorips.devtools.core.model.testcase.ITestObject;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmptRelation;
import org.faktorips.devtools.core.model.testcase.ITestValue;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.model.testcasetype.TestParameterRole;
import org.faktorips.devtools.core.ui.editors.testcase.TestCaseHierarchyPath;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;

/**
 * Test case class. Defines a concrete test case based on a test case type definition.
 * 
 * @author Joerg Ortmann
 */
public class TestCase extends IpsObject implements ITestCase {

    /* Tags */
    static final String TAG_NAME_INPUT = "Input"; //$NON-NLS-1$
    static final String TAG_NAME_EXPECTED_RESULT = "ExpectedResult"; //$NON-NLS-1$

    /* Name of corresponding test case type */
    private String testCaseType = ""; //$NON-NLS-1$

    /* Children */
    private List testObjects = new ArrayList();

    public TestCase(IIpsSrcFile file) {
        super(file);
    }

    /**
     * {@inheritDoc}
     */
    public IIpsElement[] getChildren() {
        return (IIpsElement[])testObjects.toArray(new IIpsElement[0]);
    }

    /**
     * {@inheritDoc}
     */
    protected void reinitPartCollections() {
        this.testObjects = new ArrayList();
    }

    /**
     * {@inheritDoc}
     */
    protected void reAddPart(IIpsObjectPart part) {
        if (part instanceof ITestObject) {
            testObjects.add(part);
            return;
        }
        throw new RuntimeException("Unknown part type" + part.getClass()); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    protected IIpsObjectPart newPart(Element xmlTag, int id) {
        String xmlTagName = xmlTag.getNodeName();
        if (TestPolicyCmpt.TAG_NAME.equals(xmlTagName)) {
            return newTestPolicyCmptInternal(id);
        } else if (TestValue.TAG_NAME.equals(xmlTagName)) {
            return newTestValueInternal(id);
        }
        throw new RuntimeException("Could not create part for tag name: " + xmlTagName); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public IpsObjectType getIpsObjectType() {
        return IpsObjectType.TEST_CASE;
    }

    /**
     * {@inheritDoc}
     */
    public IIpsObjectPart newPart(Class partType) {
        throw new IllegalArgumentException("Unknown part type: " + partType); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
        testCaseType = element.getAttribute(PROPERTY_TEST_CASE_TYPE);
    }

    /**
     * {@inheritDoc}
     */
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_TEST_CASE_TYPE, testCaseType);
    }

    /**
     * {@inheritDoc}
     */
    public String getTestCaseType() {
        return testCaseType;
    }

    /**
     * {@inheritDoc}
     */
    public void setTestCaseType(String testCaseType) {
        String oldTestCaseType = this.testCaseType;
        this.testCaseType = testCaseType;
        valueChanged(oldTestCaseType, testCaseType);
    }

    /**
     * {@inheritDoc}
     */
    public ITestCaseType findTestCaseType() throws CoreException {
        if (StringUtils.isEmpty(testCaseType))
            return null;
        return (ITestCaseType)getIpsProject().findIpsObject(IpsObjectType.TEST_CASE_TYPE, testCaseType);
    }

    /**
     * {@inheritDoc}
     */
    public ITestCaseTestCaseTypeDelta computeDeltaToTestCaseType() throws CoreException {
        ITestCaseType testCaseType = findTestCaseType();
        if (testCaseType != null) {
            return new TestCaseTestCaseTypeDelta(this, testCaseType);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void fixDifferences(ITestCaseTestCaseTypeDelta delta) throws CoreException {
    }

    /**
     * {@inheritDoc}
     */
    public ITestValue newTestValue() {
        ITestValue v = newTestValueInternal(getNextPartId());
        updateSrcFile();
        return v;
    }

    /**
     * {@inheritDoc}
     */
    public ITestPolicyCmpt newTestPolicyCmpt() {
        ITestPolicyCmpt p = newTestPolicyCmptInternal(getNextPartId());
        updateSrcFile();
        return p;
    }

    //
    // Getters for test objects
    //
    
    /**
     * {@inheritDoc}
     */
    public ITestPolicyCmpt[] getTestPolicyCmpts() {
        return (ITestPolicyCmpt[])getTestObjects(null, TestPolicyCmpt.class, null).toArray(
                new ITestPolicyCmpt[0]);
    }
    
    /**
     * {@inheritDoc}
     */
    public ITestValue[] getTestValues() {
        return (ITestValue[])getTestObjects(null, TestValue.class, null).toArray(new ITestValue[0]);
    }
    
    //
    // Getters for input objects
    //

    /**
     * {@inheritDoc}
     */
    public ITestValue[] getInputTestValues() {
        return (ITestValue[])getTestObjects(TestParameterRole.INPUT, TestValue.class, null).toArray(new ITestValue[0]);
    }

    /**
     * {@inheritDoc}
     */
    public ITestPolicyCmpt[] getInputTestPolicyCmpts() {
        return (ITestPolicyCmpt[])getTestObjects(TestParameterRole.INPUT, TestPolicyCmpt.class, null).toArray(
                new ITestPolicyCmpt[0]);
    }

    //
    // Getters for expected result objects
    //    

    /**
     * {@inheritDoc}
     */
    public ITestValue[] getExpectedResultTestValues() {
        return (ITestValue[])getTestObjects(TestParameterRole.EXPECTED_RESULT, TestValue.class, null).toArray(
                new ITestValue[0]);
    }

    /**
     * {@inheritDoc}
     */
    public ITestPolicyCmpt[] getExpectedResultTestPolicyCmpts() {
        return (ITestPolicyCmpt[])getTestObjects(TestParameterRole.EXPECTED_RESULT, TestPolicyCmpt.class, null)
                .toArray(new ITestPolicyCmpt[0]);
    }

    /**
     * {@inheritDoc}
     */
    public void removeTestObject(ITestObject testObject) throws CoreException {
        if (testObject.isRoot())
            testObjects.remove(testObject);
        else
            remove(testObject);
        
        updateSrcFile();
    }

    //
    // Finder methods to search inside the complete test case structure.
    //

    /**
     * {@inheritDoc}
     */
    public ITestPolicyCmptTypeParameter findTestPolicyCmptTypeParameter(ITestPolicyCmpt testPolicyCmpt)
            throws CoreException {
        return findTestPolicyCmptTypeParameter(testPolicyCmpt, null);
    }

     /**
     * {@inheritDoc}
     */
     public ITestPolicyCmptTypeParameter findTestPolicyCmptTypeParameter(ITestPolicyCmptRelation relation) throws CoreException {
         return findTestPolicyCmptTypeParameter(null, relation);
     }

    /**
     * Returns the corresponing test policy componnet type parameter of the given test policy
     * component or the given relation. Either the test policy component or the relation must be
     * given, but not both together. Returns <code>null</code> if the parameter not found.
     * 
     * @param testPolicyCmptBase The test policy component which policy component type parameter
     *            will be returned.
     * @param relation The test policy component relation which test relation will be returned
     * 
     * @throws CoreException if an error occurs while searching for the object.
     */
    private ITestPolicyCmptTypeParameter findTestPolicyCmptTypeParameter(ITestPolicyCmpt testPolicyCmptBase,
            ITestPolicyCmptRelation relation) throws CoreException {
        ArgumentCheck.isTrue(testPolicyCmptBase != null || relation != null);
        ArgumentCheck.isTrue(!(testPolicyCmptBase != null && relation != null));

        ITestCaseType testCaseType = findTestCaseType();
        if (testCaseType == null) {
            throw new CoreException(new IpsStatus(NLS.bind(Messages.TestCase_Error_TestCaseTypeNotFound, testCaseType)));
        }

        // Create a helper path obejct to search the given string path
        TestCaseHierarchyPath hierarchyPath = null;
        if (testPolicyCmptBase != null) {
            hierarchyPath = new TestCaseHierarchyPath(testPolicyCmptBase, false);
        } else if (relation != null) {
            hierarchyPath = new TestCaseHierarchyPath(relation, false);
        } else {
            throw new CoreException(new IpsStatus(Messages.TestCase_Error_NoRelationOrPolicyCmptGiven));
        }

        // find the root test policy component parameter type
        String testPolicyCmptTypeName = hierarchyPath.next();
        ITestParameter testParam = testCaseType.getTestParameterByName(testPolicyCmptTypeName);
        if (testParam == null) {
            return null;
        }

        // heck the found object 
        if (! (testParam instanceof ITestPolicyCmptTypeParameter)) {
            throw new CoreException(
                    new IpsStatus(NLS.bind(Messages.TestCase_Error_WrongInstanceParam, testPolicyCmptTypeName, testParam.getClass().getName())));
        }
        if (!testPolicyCmptTypeName.equals(testParam.getName())) {
            // incosistence between test case and test case type
            return null;
        }

        // now search the given path until the test policy component is found
        ITestPolicyCmptTypeParameter policyCmptTypeParam = (ITestPolicyCmptTypeParameter) testParam;
        while (hierarchyPath.hasNext()) {
            testPolicyCmptTypeName = hierarchyPath.next();
            policyCmptTypeParam = policyCmptTypeParam.getTestPolicyCmptTypeParamChild(testPolicyCmptTypeName);
            if (policyCmptTypeParam == null || !testPolicyCmptTypeName.equals(policyCmptTypeParam.getName())) {
                // incosistence between test case and test case type
                return null;
            }
        }

        return policyCmptTypeParam;
    }

     /**
      * Removes the given test parameter object from the parameter list
      */
    private void remove(ITestObject testObject) throws CoreException {
        if (testObject instanceof ITestPolicyCmpt) {
            ITestPolicyCmpt testPolicyCmpt = (ITestPolicyCmpt) testObject;
            if (testPolicyCmpt.isRoot()) {
                removeTestObject(testObject);
            } else {
                TestCaseHierarchyPath hierarchyPath = new TestCaseHierarchyPath(testPolicyCmpt);
                testPolicyCmpt = findTestPolicyCmpt(hierarchyPath.toString());
                if (testPolicyCmpt == null) {
                    throw new CoreException(new IpsStatus(NLS.bind(Messages.TestCase_Error_TestPolicyCmptNotFound,
                            hierarchyPath.toString())));
                }

                ITestPolicyCmptRelation relation = (ITestPolicyCmptRelation) testPolicyCmpt.getParent();
                if (relation != null) {
                    ((ITestPolicyCmpt)relation.getParent()).removeRelation(relation);
                }
            }
        } else {
            removeTestObject(testObject);
        }
    }

    /**
     * {@inheritDoc}
     */
    public ITestPolicyCmpt findTestPolicyCmpt(String testPolicyCmptPath) throws CoreException {
         TestCaseHierarchyPath path = new TestCaseHierarchyPath(testPolicyCmptPath);
         ITestPolicyCmpt pc = null;
         String currElem = path.next();

         List testPoliyCmpts = getTestObjects(null, TestPolicyCmpt.class, currElem);
         if (testPoliyCmpts.size() == 1){
             assertInstanceOfTestPolicyCmpt(currElem, (ITestObject) testPoliyCmpts.get(0));
             pc = searchChildTestPolicyCmpt((ITestPolicyCmpt) testPoliyCmpts.get(0), path);
         } else if (testPoliyCmpts.size() == 0) {
             return null;
         } else {
             throw new CoreException(new IpsStatus(NLS.bind(
                     Messages.TestCase_Error_MoreThanOneObject, currElem)));
         }
         return pc;
    }

    /*
     * Assert the correct instance of ITestPolicyCmpt for the given testObject.
     * @throws CoreException if the check fails.
     */
    private void assertInstanceOfTestPolicyCmpt(String currElem, ITestObject testObject) throws CoreException {
        if (! ( testObject instanceof ITestPolicyCmpt))
             throw new CoreException(
                     new IpsStatus(NLS.bind(Messages.TestCase_Error_WrongInstanceTestPolicyCmpt, currElem, testObject.getClass().getName())));
    }
    
    /*
     * Search the test policy component by the given path.
     */
     private ITestPolicyCmpt searchChildTestPolicyCmpt(ITestPolicyCmpt pc, TestCaseHierarchyPath path)
            throws CoreException {
        String searchedPath = path.toString();
        while (pc != null && path.hasNext()) {
            boolean found = false;
            String currElem = path.next();

            ITestPolicyCmptRelation[] prs;
            prs = pc.getTestPolicyCmptRelations(currElem);

            currElem = path.next();
            pc = null;
            for (int i = 0; i < prs.length; i++) {
                ITestPolicyCmptRelation relation = prs[i];
                ITestPolicyCmpt pcTarget = relation.findTarget();
                if (pcTarget == null)
                    return null;

                if (currElem.equals(pcTarget.getName())) {                     if (found){                         // exception more than one element found with the given path                         throw new CoreException(new IpsStatus(NLS.bind(                                 Messages.TestCase_Error_MoreThanOneObject, searchedPath)));                     }                     found = true;                                             pc = pcTarget;                }
            }
        }
        return pc;
    }

     /**
      * {@inheritDoc}
      */
    public String generateUniqueNameForTestPolicyCmpt(ITestPolicyCmpt newTestPolicyCmpt, String name) {
        String uniqueLabel = name;

        // eval the unique idx of new component
        int idx = 1;
        String newUniqueLabel = uniqueLabel;
        if (newTestPolicyCmpt.isRoot()) {
            ITestPolicyCmpt[] testPolicyCmpts = getTestPolicyCmpts();
            for (int i = 0; i < testPolicyCmpts.length; i++) {
                ITestPolicyCmpt cmpt = testPolicyCmpts[i];
                if (newUniqueLabel.equals(cmpt.getName()) && ! cmpt.equals(newTestPolicyCmpt) ) {
                    idx++;
                    newUniqueLabel = uniqueLabel + " (" + idx + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
        } else {
            ITestPolicyCmpt parent = newTestPolicyCmpt.getParentPolicyCmpt();
            ITestPolicyCmptRelation[] relations = parent.getTestPolicyCmptRelations();
            ArrayList names = new ArrayList();
            for (int i = 0; i < relations.length; i++) {
                ITestPolicyCmptRelation relation = relations[i];
                if (relation.isComposition()) {
                    try {
                        ITestPolicyCmpt child = relation.findTarget();
                        if (! child.equals(newTestPolicyCmpt)){
                            names.add(child.getName());
                        }
                    } catch (CoreException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            while (names.contains(newUniqueLabel)) {
                idx++;
                newUniqueLabel = uniqueLabel + " (" + idx + ")"; //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        return newUniqueLabel;
    }

    /*
     * Creates a new test policy component without updating the src file.
     */
    private ITestPolicyCmpt newTestPolicyCmptInternal(int id) {
        ITestPolicyCmpt p = new TestPolicyCmpt(this, id);
        testObjects.add(p);
        return p;
    }

    /*
     * Creates a new test value without updating the src file.
     */
    private ITestValue newTestValueInternal(int id) {
        ITestValue v = new TestValue(this, id);
        testObjects.add(v);
        return v;
    }

    /*
     * Returns the test objects which matches the given role, is instance of the given class and
     * matches the given name. The particular object aspect will only check if the particular field
     * is not <code>null</code>. For instance if all parameter are <code>null</code> then all
     * parameters are returned.
     */
    private List getTestObjects(TestParameterRole role, Class parameterClass, String name) {
        List result = new ArrayList(testObjects.size());
        for (Iterator iter = testObjects.iterator(); iter.hasNext();) {
            TestObject testObject = (TestObject)iter.next();
            boolean addParameter = true;
            
            if (role != null && ! isRoleOrDefault(testObject.getTestParameterName(), role, TestObject.DEFAULT_ROLE)){
                addParameter = false;
                continue;
            }
            
            if (parameterClass != null && !testObject.getClass().equals(parameterClass)) {
                addParameter = false;
                continue;
            }
            if (name != null && !name.equals(testObject.getName())) {
                addParameter = false;
                continue;
            }
            if (addParameter)
                result.add(testObject);
        }
        return result;
    }

    /**
     * Returns <code>true</code> if the given role is the role of the corresponding test
     * parameter. If the test parameter couldn't determined return <code>true</code> if the given
     * role is the default role otherwise <code>false</code>.<br>
     * Return <code>false</code> if an error occurs.<br>
     * (Packageprivate helper method.)
     */
    boolean isRoleOrDefault(String testParameterName, TestParameterRole role, TestParameterRole defaultRole) {
            try {
                ITestCaseType testCaseType = findTestCaseType();
                if (testCaseType == null)
                    return role.equals(defaultRole);

                ITestParameter testParameter = testCaseType.getTestParameterByName(testParameterName);
                if (testParameter == null)
                    return role.equals(defaultRole);

                return isRoleOrDefault(testParameter, role, defaultRole);
            } catch (Exception e) {
                // ignore exceptions
            }
            return false;
    }

    /**
     * Returns <code>true</code> if the given role is the role of the corresponding test
     * parameter.<br>
     * Return <code>false</code> if an error occurs.<br>
     * (Packageprivate helper method.)
     */
    boolean isRoleOrDefault(ITestParameter testParameter, TestParameterRole role, TestParameterRole defaultRole) {
        try {
            // compare the paramters role and return if the role matches the given role
            if (testParameter.isInputParameter() && role.equals(TestParameterRole.INPUT)) {
                return true;
            }
            if (testParameter.isExpextedResultParameter() && role.equals(TestParameterRole.EXPECTED_RESULT)) {
                return true;
            }
            if (testParameter.isCombinedParameter() && role.equals(TestParameterRole.COMBINED)) {
                return true;
            }
        } catch (Exception e) {
            // ignore exceptions
        }
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    protected void validateThis(MessageList messageList) throws CoreException {
        super.validateThis(messageList);
        ITestCaseType testCaseType = findTestCaseType();
        if (testCaseType == null) {
            String text = NLS.bind(Messages.TestCase_ValidateError_TestCaseTypeNotFound, getTestCaseType());
            Message msg = new Message(MSGCODE_TEST_CASE_TYPE_NOT_FOUND, text, Message.ERROR, this,
                    ITestPolicyCmptTypeParameter.PROPERTY_POLICYCMPTTYPE);
            messageList.add(msg);
            return;
        }
    }
}
