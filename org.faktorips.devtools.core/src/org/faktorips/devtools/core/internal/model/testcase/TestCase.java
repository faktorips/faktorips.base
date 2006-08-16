/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.testcase;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.IpsObject;
import org.faktorips.devtools.core.internal.model.IpsObjectPart;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestObject;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmptRelation;
import org.faktorips.devtools.core.model.testcase.ITestValue;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.ui.editors.testcase.TestCaseHierarchyPath;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
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
	
	/* Containter for input and expected result objects */
	private TestCaseContainer input = new TestCaseContainer(true, this, 0);
	private TestCaseContainer expectedResult = new TestCaseContainer(false, this, 1);

	public TestCase(IIpsSrcFile file) {
		super(file);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public IIpsElement[] getChildren() {
		IIpsElement[] childrenArray = new IIpsElement[2];
		childrenArray[0] = input;
		childrenArray[1] = expectedResult;
		return childrenArray;
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected void reinitPartCollections() {
		this.input = new TestCaseContainer(true, this, 0);
		this.expectedResult = new TestCaseContainer(false, this, 1);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void reAddPart(IIpsObjectPart part) {
		if (part instanceof TestCaseContainer) {
			// decide if this is a input or expected result container.
			if (((TestCaseContainer) part).isInput()){
				input = (TestCaseContainer) part;
			}else{
				expectedResult = (TestCaseContainer) part;
			}
			return;
		}
		throw new RuntimeException("Unknown part type: " + part.getClass()); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	protected IIpsObjectPart newPart(Element xmlTag, int id) {
		String xmlTagName = xmlTag.getNodeName();
		if (TAG_NAME_INPUT.equals(xmlTagName)){
			return new TestCaseContainer(true, this, 0);
		}else if (TAG_NAME_EXPECTED_RESULT.equals(xmlTagName)){
			return new TestCaseContainer(false, this, 1);
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
    public ITestCaseType findTestCaseType() throws CoreException{
    	if (StringUtils.isEmpty(testCaseType))
    		return null;
    	return (ITestCaseType) getIpsProject().findIpsObject(IpsObjectType.TEST_CASE_TYPE, testCaseType);    	
    }
	
	/**
	 * {@inheritDoc}
	 */
	public ITestValue newInputValue() {
		ITestValue param = newValueObjectInternal(true);
		updateSrcFile();
		return param;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public ITestValue newExpectedResultValue() {
		ITestValue param = newValueObjectInternal(false);
		updateSrcFile();
		return param;
	}	

	/**
	 * Creates a new value object as input or expected result.
	 */
	private ITestValue newValueObjectInternal(boolean isInput){
		if (isInput){
			return input.newValueObject();
		}else{
			return expectedResult.newValueObject();
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public ITestPolicyCmpt newInputPolicyCmpt() {
		ITestPolicyCmpt param = newPolicyCmptInternal(true);
		updateSrcFile();
		return param;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public ITestPolicyCmpt newExpectedResultPolicyCmpt() {
		ITestPolicyCmpt param = newPolicyCmptInternal(false);
		updateSrcFile();
		return param;
	}
	
	/**
	 * Creates a new policy component object as input or expected result.
	 */
	private ITestPolicyCmpt newPolicyCmptInternal(boolean isInput){
		if (isInput){
			return input.newPolicyCmptObject();
		}else{
			return expectedResult.newPolicyCmptObject();
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public ITestObject[] getInputObjects() {
		return input.getTestObjects();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public ITestValue[] getInputValues() {
		return input.getTestValueObjects();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public ITestPolicyCmpt[] getInputPolicyCmpt() {
		return input.getTestPolicyCmptObjects();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public ITestObject[] getExpectedResultObjects() {
		return expectedResult.getTestObjects();
	}

	/**
	 * {@inheritDoc}
	 */
	public ITestPolicyCmpt[] getExpectedResultPolicyCmpt() {
		return expectedResult.getTestPolicyCmptObjects();
	}

	/**
	 * {@inheritDoc}
	 */
	public ITestValue[] getExpectedResultValues() {
		return expectedResult.getTestValueObjects();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void removeTestObject(ITestObject testObject) throws CoreException{
		if (testObject.isInputObject()){
			input.remove(testObject);
		}else{
			expectedResult.remove(testObject);
		}
		updateSrcFile();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public ITestPolicyCmpt findExpectedResultPolicyCmpt(String typeName) {
		return expectedResult.findPolicyCmpt(typeName);
	}

	/**
	 * {@inheritDoc}
	 */
	public ITestPolicyCmpt findInputPolicyCmpt(String typeName) {
		return input.findPolicyCmpt(typeName);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public ITestPolicyCmptTypeParameter findTestPolicyCmptTypeParameter(ITestPolicyCmpt testPolicyCmptBase) throws CoreException {
		return findTestPolicyCmptTypeParameter(testPolicyCmptBase, null, testPolicyCmptBase.isInputObject());
	}

	/**
	 * {@inheritDoc}
	 */
	public ITestPolicyCmptTypeParameter findTestPolicyCmptTypeParameter(ITestPolicyCmptRelation relation) throws CoreException {
		return findTestPolicyCmptTypeParameter(null, relation, ((ITestPolicyCmpt)relation.getParent()).isInputObject());
	}

	/**
	 * Returns the corresponing test policy componnet type parameter of the given test policy component
	 * or the given relation. Either the test policy component or the relation must be given, but not
	 * both together.
	 * Returns <code>null</code> if the parameter not found.
	 *
	 * @param testPolicyCmptBase The test policy component which policy component type parameter will be returned.
	 * @param relation The test policy component relation which test relation will be returned
	 * @param isInput Indicates if the given object is a input <code>true</code> or expected result object <code>false</code>
	 *	
	 * @throws CoreException if an error occurs while searching for the object.
	 */
	private ITestPolicyCmptTypeParameter findTestPolicyCmptTypeParameter(ITestPolicyCmpt testPolicyCmptBase,
			ITestPolicyCmptRelation relation, boolean isInput) throws CoreException {
		ArgumentCheck.isTrue(testPolicyCmptBase != null || relation != null);
		ArgumentCheck.isTrue(! (testPolicyCmptBase != null && relation != null));
		
		ITestCaseType foundTestCaseType = findTestCaseType();
		if (foundTestCaseType == null){
			throw new CoreException(new IpsStatus(NLS.bind(Messages.TestCase_Error_TestCaseTypeNotFound, testCaseType)));
		}
		
		TestCaseHierarchyPath hierarchyPath = null;
		if (testPolicyCmptBase != null){
			hierarchyPath  = new TestCaseHierarchyPath(testPolicyCmptBase, false);
		}else if (relation != null){
			hierarchyPath  = new TestCaseHierarchyPath(relation, false);
		}else{
			throw new CoreException(new IpsStatus(Messages.TestCase_Error_NoRelationOrPolicyCmptGiven));
		}
		
		String testPolicyCmptTypeName = hierarchyPath.next();

		ITestPolicyCmptTypeParameter policyCmptTypeParam = isInput ? 
				foundTestCaseType.getInputTestPolicyCmptTypeParameter(testPolicyCmptTypeName) :
				foundTestCaseType.getExpectedResultTestPolicyCmptTypeParameter(testPolicyCmptTypeName); 
		if (policyCmptTypeParam == null){
			return null;
		}
		
		if (! testPolicyCmptTypeName.equals(policyCmptTypeParam.getName())){
			// incosistence between test case and test case type
			return null;
		}

		while (hierarchyPath.hasNext()){
			testPolicyCmptTypeName = hierarchyPath.next();
			policyCmptTypeParam = policyCmptTypeParam.getTestPolicyCmptTypeParamChild(testPolicyCmptTypeName);
			if (policyCmptTypeParam == null || ! testPolicyCmptTypeName.equals(policyCmptTypeParam.getName())){
				// incosistence between test case and test case type
				return null;
			}
		}
		
		return policyCmptTypeParam;
	}
	
	/**
	 * Inner auxialyry class to represent the input or expected result container.
	 */
	private class TestCaseContainer extends IpsObjectPart{	    	
		private boolean isInput = true;

		/** List of child objects */
		private List testObject = new ArrayList();
		
		public TestCaseContainer(boolean isInput, IIpsObject parent, int id){
			super(parent, id);
			this.isInput = isInput;
		}

		/**
		 * Returns <code>true</code> if this containers holds input objects otherwise returns <code>false</code>.
		 */
		public boolean isInput() {
			return isInput;
		}
		
		/**
		 * {@inheritDoc}
		 */
		public IIpsElement[] getChildren() {
			IIpsElement[] childrenArray = new IIpsElement[testObject.size()];
			testObject.toArray(childrenArray);
			return childrenArray;
		}
		
		/**
		 * Return all contained objects.
		 */
		private ITestObject[] getTestObjects() {
			ITestObject[] testParameterArray = new ITestObject[testObject.size()];
			testObject.toArray(testParameterArray);
			return testParameterArray;
		}	
		
		/**
		 * Returns all the test value objects or an empty list if there no such objects.
		 */
		public ITestValue[] getTestValueObjects() {
			List inputObjects = getTestObjects(TestValue.class);
	        ITestValue[] inputObjectsArray = new ITestValue[inputObjects.size()];
			inputObjects.toArray(inputObjectsArray);
			return inputObjectsArray;
		}

		/**
		 * Returns all the test policy component objects or an empty list if there no such objects.
		 */		
		private ITestPolicyCmpt[] getTestPolicyCmptObjects() {
			List inputObjects = getTestObjects(TestPolicyCmpt.class);
	        ITestPolicyCmpt[] inputObjectsArray = new ITestPolicyCmpt[inputObjects.size()];
	        inputObjects.toArray(inputObjectsArray);
			return inputObjectsArray;
		}
		
		/**
		 * Returns the test policy component with the given name.
		 * Returns <code>null</code> if no entry found.
		 */
		private ITestPolicyCmpt getTestPolicyCmptObjectByLabel(String label){
			ITestPolicyCmpt[] policyCmpts = getTestPolicyCmptObjects();
			for (int i = 0; i < policyCmpts.length; i++) {
				ITestPolicyCmpt cmpt = policyCmpts[i];
				if (cmpt.getLabel().equals(label)){
					return cmpt;
				}
			}
			return null;
		}
		
		/**
		 * Returns all the test objects which are kind of the given class.
		 */
		private List getTestObjects(Class objectClass) {
			List objects = new ArrayList();
	        for (Iterator it = testObject.iterator(); it.hasNext();) {
	            ITestObject testObject = (ITestObject)it.next();
	            if (testObject.getClass().equals(objectClass))
	            	objects.add(testObject);
	        }
			return objects;
		}
		
		/**
		 * {@inheritDoc}
		 */
		protected void reinitPartCollections() {
			testObject = new ArrayList();
		}

		/**
		 * {@inheritDoc}
		 */
		protected void reAddPart(IIpsObjectPart part) {
			if (part instanceof ITestObject) {
				testObject.add(part);
				return;
			}
			throw new RuntimeException("Unknown part type: " + part.getClass()); //$NON-NLS-1$
		}
		
		/** 
	     * {@inheritDoc}
	     */
	    public void delete() {
	    }
	    
	    /**
	     * {@inheritDoc}
	     */
	    public boolean isDeleted() {
	    	return false;
	    }    
	    
		/**
		 * Removes the given test parameter object from the parameter list
		 */
		private void remove(ITestObject param) throws CoreException{
			if (param instanceof ITestPolicyCmpt){
				ITestPolicyCmpt testPolicyCmpt = (ITestPolicyCmpt) param;
				if (testPolicyCmpt.isRoot()){
					testObject.remove((TestPolicyCmpt) param);
				}else{
					TestCaseHierarchyPath hierarchyPath = new TestCaseHierarchyPath(testPolicyCmpt, true);
					
					String hierarchyElementName = hierarchyPath.next();
					
					testPolicyCmpt = getTestPolicyCmptByLable(hierarchyElementName);
					if (testPolicyCmpt == null){
						throw new CoreException(new IpsStatus(NLS.bind(Messages.TestCase_Error_TestPolicyCmptNotFound, hierarchyElementName)));
					}

					ITestPolicyCmptRelation relation = null;
					while (hierarchyPath.hasNext()){
						hierarchyElementName = hierarchyPath.next();
						// search over all relations to get the correct child
						// note: there could be more relations with the same name
						ITestPolicyCmptRelation[] relations = testPolicyCmpt.getTestPolicyCmptRelations();
						relation = null;
						String testPolicyCmptType = hierarchyElementName;
						hierarchyElementName = hierarchyPath.next();
						for (int i = 0; i < relations.length; i++) {
							ITestPolicyCmptRelation currRelation = relations[i];
							if (currRelation.getTestPolicyCmptType().equals(testPolicyCmptType)){
								try {
									testPolicyCmpt = currRelation.findTarget();
								} catch (CoreException e) {
									throw new CoreException(new IpsStatus(NLS.bind(Messages.TestCase_Error_SearchingRelationTarget, hierarchyElementName), e));
								}
								if (testPolicyCmpt == null){
									throw new CoreException(new IpsStatus(NLS.bind(Messages.TestCase_Error_SearchingRelationTarget, hierarchyElementName)));
								}								
								if ( testPolicyCmpt.getLabel().equals(hierarchyElementName)){
									relation = currRelation;
									break;
								}
								
							}	
						}
						if (relation == null){
							throw new CoreException(new IpsStatus(NLS.bind(Messages.TestCase_Error_RelationNotFound, testPolicyCmptType, hierarchyElementName)));
						}
					}
					if (testPolicyCmpt != null && relation != null){
						((ITestPolicyCmpt) relation.getParent()).removeRelation(relation);
					}
				}
			}else{
				testObject.remove(param);
			}
		}
		
		private ITestPolicyCmpt getTestPolicyCmptByLable(String lable){
			ITestPolicyCmpt foundObject = null; 
			for (Iterator iter = testObject.iterator(); iter.hasNext();) {
				TestObject element = (TestObject) iter.next();
				if (element instanceof ITestPolicyCmpt){
					if (((ITestPolicyCmpt)element).getLabel().equals(lable)){
						foundObject = (ITestPolicyCmpt) element;
						break;
					}
				}
			}
			return foundObject;
		}
		
	    /** 
	     * {@inheritDoc}
	     */
	    public Image getImage() {
			return null;
	    }
	    
	    /**
	     * Overridden.
	     */
	    protected Element createElement(Document doc) {
	        return isInput ? doc.createElement(TestCase.TAG_NAME_INPUT) :
	        	             doc.createElement(TestCase.TAG_NAME_EXPECTED_RESULT);
	    }

	    /**
	     * {@inheritDoc}
	     */
		protected IIpsObjectPart newPart(Element xmlTag, int id) {
			String xmlTagName = xmlTag.getNodeName();
			if (xmlTagName.equals(TestValue.TAG_NAME)) {
				return newValueObjectInternal(isInput);
			}else if (xmlTagName.equals(TestPolicyCmpt.TAG_NAME)) {
				return newPolicyCmptInternal(isInput);
			}
			throw new RuntimeException("Could not create part for tag name: " + xmlTagName); //$NON-NLS-1$
		}
		
		/**
		 * {@inheritDoc}
		 */
		public IIpsObjectPart newPart(Class partType) {
			throw new IllegalArgumentException("Unknown part type: " + partType); //$NON-NLS-1$
		}	

		/**
		 * Returns a new value object and adds it to the list of children.
		 */
		public ITestValue newValueObject() {
			TestValue newObject = new TestValue(this, getNextPartId());
			newObject.setInputParameter(isInput);
			testObject.add(newObject);
			return newObject;
		}

		/**
		 * Returns a new value object and adds it to the list of children.
		 */
		public ITestPolicyCmpt newPolicyCmptObject() {
			TestPolicyCmpt newObject = new TestPolicyCmpt(this, getNextPartId());
			newObject.setInputParameter(isInput);
			testObject.add(newObject);
			return newObject;
		}

		/**
		 * @param typeName
		 * @return
		 */
		public ITestPolicyCmpt findPolicyCmpt(String typeNamePath) {
			TestCaseHierarchyPath path = new TestCaseHierarchyPath(typeNamePath);
			
			ITestPolicyCmpt pc = null;
			try {
				String currElem = path.next();
				if (path.isFullPath()){
					pc = searchChildTestPolicyCmpt(getTestPolicyCmptObjectByLabel(currElem), path);
				} else {
					ITestPolicyCmpt[] pcs = getTestPolicyCmptObjects();
					for (int i = 0; i < pcs.length; i++) {
						pc = searchChildTestPolicyCmpt(pcs[i], path);
						if (pc != null)
							break;
					}
				}
			} catch (CoreException e) {
			}
			return pc;
		}
	}
	
	private ITestPolicyCmpt searchChildTestPolicyCmpt(ITestPolicyCmpt pc, TestCaseHierarchyPath path) throws CoreException{
		while (pc != null && path.hasNext()){
			String currElem = path.next();

			if (!path.isFullPath() && pc.getLabel().equals(currElem)){
				return pc;
			}
			
			ITestPolicyCmptRelation[] prs;
			if (path.isFullPath()) 
				prs = pc.getTestPolicyCmptRelations(currElem);
			else
				prs = pc.getTestPolicyCmptRelations();
			
			currElem = path.next();
			pc = null;
			for (int i = 0; i < prs.length; i++) {
				ITestPolicyCmptRelation relation = prs[i];
				ITestPolicyCmpt pcTarget = relation.findTarget();
				if (pcTarget == null)
					return null;
				
				if (currElem.equals(pcTarget.getLabel())){
					if (path.isFullPath()){
						pc = pcTarget;
						break;
					}else{
						return pcTarget;
					}
				}
				
				if (!path.isFullPath()){
					 pc = searchChildTestPolicyCmpt(pcTarget, path);
					 if (pc != null)
						 return pc;
				}
			}
		}
		return pc;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String generateUniqueLabelForTestPolicyCmpt(ITestPolicyCmpt newTestPolicyCmpt, String label) {
		String uniqueLabel = label;

		// eval the unique idx of new component
		int idx = 1;
		String newUniqueLabel = uniqueLabel;
		if (newTestPolicyCmpt.isRoot()){
			ITestPolicyCmpt[] testPolicyCmpts = newTestPolicyCmpt.isInputObject()?getInputPolicyCmpt():getExpectedResultPolicyCmpt();
			for (int i = 0; i < testPolicyCmpts.length; i++) {
				ITestPolicyCmpt cmpt = testPolicyCmpts[i];
				if (newUniqueLabel.equals(cmpt.getLabel())){
					idx ++;
					newUniqueLabel = uniqueLabel + " (" + idx + ")"; //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}else{
			ITestPolicyCmpt parent = newTestPolicyCmpt.getParentPolicyCmpt();
			ITestPolicyCmptRelation[] relations = parent.getTestPolicyCmptRelations();
			ArrayList names = new ArrayList();
			for (int i = 0; i < relations.length; i++) {
				ITestPolicyCmptRelation relation = relations[i];
				if (relation.isComposition()){
					try {
						ITestPolicyCmpt child = relation.findTarget();
						names.add(child.getLabel());
					} catch (CoreException e) {
						throw new RuntimeException(e);
					}
				}
			}
			while (names.contains(newUniqueLabel)){
				idx ++;
				newUniqueLabel = uniqueLabel + " (" + idx + ")"; //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		return newUniqueLabel;
	}

	/**
	 * {@inheritDoc}
	 */
	protected void validateThis(MessageList messageList) throws CoreException {
		super.validateThis(messageList);
		ITestCaseType testCaseType = findTestCaseType();
		if (testCaseType == null) {
			String text = NLS.bind(Messages.TestCase_ValidateError_TestCaseTypeNotFound, getTestCaseType());
			Message msg = new Message(MSGCODE_TEST_CASE_TYPE_NOT_FOUND, text, Message.ERROR, this, ITestPolicyCmptTypeParameter.PROPERTY_POLICYCMPTTYPE);
			messageList.add(msg);	
			return;
		}
	}
}
