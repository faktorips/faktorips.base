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

package org.faktorips.devtools.core.internal.model.testcasetype;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.internal.model.IpsObject;
import org.faktorips.devtools.core.internal.model.IpsObjectPart;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestValueParameter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test case type class. Definition of a test case type.
 * 
 * @author Joerg Ortmann
 */
public class TestCaseType extends IpsObject implements ITestCaseType {
	
	/* Tags */
	static final String TAG_NAME_INPUT = "Input"; //$NON-NLS-1$
	static final String TAG_NAME_EXPECTED_RESULT = "ExpectedResult"; //$NON-NLS-1$
	
	/* Containter for input and expected result objects */
	private TestCaseTypeContainer input = new TestCaseTypeContainer(true, this, 0);
	private TestCaseTypeContainer expectedResult = new TestCaseTypeContainer(false, this, 1);
	
	public TestCaseType(IIpsSrcFile file) {
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
		this.input = new TestCaseTypeContainer(true, this, 0);
		this.expectedResult = new TestCaseTypeContainer(false, this, 1);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void reAddPart(IIpsObjectPart part) {
		if (part instanceof TestCaseTypeContainer) {
			if (((TestCaseTypeContainer) part).isInput){
				input = (TestCaseTypeContainer) part;
			}else{
				expectedResult = (TestCaseTypeContainer) part;
			}
			return;
		}
		throw new RuntimeException("Unknown part type" + part.getClass()); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	protected IIpsObjectPart newPart(Element xmlTag, int id) {
		String xmlTagName = xmlTag.getNodeName();
		if (TAG_NAME_INPUT.equals(xmlTagName)){
			return new TestCaseTypeContainer(true, this, 0);
		}else if (TAG_NAME_EXPECTED_RESULT.equals(xmlTagName)){
			return new TestCaseTypeContainer(false, this, 1);
		}
		
		throw new RuntimeException("Could not create part for tag name: " + xmlTagName); //$NON-NLS-1$
	}
	
	/**
	 * {@inheritDoc}
	 */
	public IpsObjectType getIpsObjectType() {
		return IpsObjectType.TEST_CASE_TYPE;
	}

	/**
	 * {@inheritDoc}
	 */
	public IIpsObjectPart newPart(Class partType) {
		throw new IllegalArgumentException("Unknown part type" + partType); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	public ITestValueParameter newInputValueParameter() {
		ITestValueParameter param = newValueParameterInternal(true);
		updateSrcFile();
		return param;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public ITestValueParameter newExpectedResultValueParameter() {
		ITestValueParameter param = newValueParameterInternal(false);
		updateSrcFile();
		return param;
	}
	
	/**
	 * Creates a new value parameter as input or expected result.
	 */	
	private ITestValueParameter newValueParameterInternal(boolean isInput){
		if (isInput){
			return input.newValueParameter();
		}else{
			return expectedResult.newValueParameter();
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public ITestPolicyCmptTypeParameter newInputPolicyCmptTypeParameter() {
		ITestPolicyCmptTypeParameter param = newPolicyCmptParameterInternal(true);
		updateSrcFile();
		return param;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public ITestPolicyCmptTypeParameter newExpectedResultPolicyCmptParameter() {
		ITestPolicyCmptTypeParameter param = newPolicyCmptParameterInternal(false);
		updateSrcFile();
		return param;
	}
	
	/**
	 * Creates a new policy component parameter as input or expected result.
	 */	
	private ITestPolicyCmptTypeParameter newPolicyCmptParameterInternal(boolean isInput){
		if (isInput){
			return input.newPolicyCmptParameter();
		}else{
			return expectedResult.newPolicyCmptParameter();
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public ITestParameter[] getInputParameters() {
		return input.getTestParameter();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public ITestValueParameter[] getInputTestValueParameters() {
		return input.getTestValueParameters();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public ITestPolicyCmptTypeParameter[] getInputTestPolicyCmptTypeParameters() {
		return input.getTestPolicyCmptTypeParameters();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public ITestValueParameter getInputTestValueParameter(String inputTestValueParameter){
		return input.getTestValueParameter(inputTestValueParameter);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public ITestPolicyCmptTypeParameter getInputTestPolicyCmptTypeParameter(String inputTestPolicyCmptTypeParameter){
		return input.getTestPolicyCmptTypeParameter(inputTestPolicyCmptTypeParameter);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public ITestParameter[] getExpectedResultParameter() {
		return expectedResult.getTestParameter();
	}

	/**
	 * {@inheritDoc}
	 */
	public ITestValueParameter[] getExpectedResultTestValueParameters() {
		return expectedResult.getTestValueParameters();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public ITestPolicyCmptTypeParameter[] getExpectedResultTestPolicyCmptTypeParameters() {
		return expectedResult.getTestPolicyCmptTypeParameters();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public ITestValueParameter getExpectedResultTestValueParameter(String expResultTestValueParameter) {
		return expectedResult.getTestValueParameter(expResultTestValueParameter);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public ITestPolicyCmptTypeParameter getExpectedResultTestPolicyCmptTypeParameter(String expResultTestPolicyCmptTypeParameter) {
		return expectedResult.getTestPolicyCmptTypeParameter(expResultTestPolicyCmptTypeParameter);
	}	

	/**
	 * Removes the test parameter from the type. 
	 */
	void removeTestParameter(TestParameter param) {
		if (param.isInputParameter()){
			input.remove(param);
		}else{
			expectedResult.remove(param);
		}
	}

	/**
	 * Inner class to represent the input or expected result container.
	 */
	private class TestCaseTypeContainer extends IpsObjectPart{	    	
		private boolean isInput = true;

		private List testParameter = new ArrayList();
		
		public TestCaseTypeContainer(boolean isInput, IIpsObject parent, int id){
			super(parent, id);
			this.isInput = isInput;
		}

		public boolean isInput() {
			return isInput;
		}
		
		public IIpsElement[] getChildren() {
			IIpsElement[] childrenArray = new IIpsElement[testParameter.size()];
			testParameter.toArray(childrenArray);
			return childrenArray;
		}	
		
		public ITestParameter[] getTestParameter() {
			ITestParameter[] testParameterArray = new ITestParameter[testParameter.size()];
			testParameter.toArray(testParameterArray);
			return testParameterArray;
		}

		/**
		 * Returns all the test parameter which are kind of the given class.
		 */
		private List getTestParameters(Class parameterClass) {
			List parameter = new ArrayList();
	        for (Iterator it = testParameter.iterator(); it.hasNext();) {
	        	ITestParameter testParameter = (ITestParameter)it.next();
	            if (testParameter.getClass().equals(parameterClass))
	            	parameter.add(testParameter);
	        }
			return parameter;
		}
		
		public ITestPolicyCmptTypeParameter getTestPolicyCmptTypeParameter(String inputTestPolicyCmptTypeParameter) {
			ITestPolicyCmptTypeParameter testPolicyCmptTypeParameter = null;
			for (Iterator it = testParameter.iterator(); it.hasNext();) {
				ITestParameter testParameter = (ITestParameter) it.next();
				if (testParameter instanceof ITestPolicyCmptTypeParameter && inputTestPolicyCmptTypeParameter.equals(testParameter.getName())){
					testPolicyCmptTypeParameter = (ITestPolicyCmptTypeParameter) testParameter;
					break;
				}
			}
			return testPolicyCmptTypeParameter;
		}

		public ITestValueParameter getTestValueParameter(String inputTestValueParameter) {
			ITestValueParameter testValueParameterFound = null;
			for (Iterator it = testParameter.iterator(); it.hasNext();) {
				ITestParameter testParameter = (ITestParameter) it.next();
				if (testParameter instanceof ITestValueParameter && inputTestValueParameter.equals(testParameter.getName())){
					testValueParameterFound = (ITestValueParameter) testParameter;
					break;
				}
			}
			return testValueParameterFound;
		}
		
		public ITestPolicyCmptTypeParameter[] getTestPolicyCmptTypeParameters() {
			List inputObjects = getTestParameters(TestPolicyCmptTypeParameter.class);
			return (ITestPolicyCmptTypeParameter[]) inputObjects.toArray(new ITestPolicyCmptTypeParameter[0]);
		}
		
		public ITestValueParameter[] getTestValueParameters() {
			List inputObjects = getTestParameters(TestValueParameter.class);
			return (ITestValueParameter[]) inputObjects.toArray(new ITestValueParameter[0]);
		}
		
		/**
		 * {@inheritDoc}
		 */
		protected void reinitPartCollections() {
			testParameter = new ArrayList();
		}

		/**
		 * {@inheritDoc}
		 */
		protected void reAddPart(IIpsObjectPart part) {
			if (part instanceof ITestParameter) {
				testParameter.add(part);
				return;
			}
			throw new RuntimeException("Unknown part type" + part.getClass()); //$NON-NLS-1$
		}
		
		public void addTestParameter(TestParameter param){
			testParameter.add(param);
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
		public void remove(TestParameter param) {
			testParameter.remove(param);
		}
		
	    /** 
	     * {@inheritDoc}
	     */
	    public Image getImage() {
			return null;
	    }
	    
	    /**
	     * {@inheritDoc}
	     */
	    protected Element createElement(Document doc) {
	        return isInput ? doc.createElement(TestCaseType.TAG_NAME_INPUT) :
	        	             doc.createElement(TestCaseType.TAG_NAME_EXPECTED_RESULT);
	    }

	    /**
	     * {@inheritDoc}
	     */
		protected IIpsObjectPart newPart(Element xmlTag, int id) {
			String xmlTagName = xmlTag.getNodeName();
			if (xmlTagName.equals(TestValueParameter.TAG_NAME)) {
				return newValueParameterInternal(isInput);
			}else if (xmlTagName.equals(TestPolicyCmptTypeParameter.TAG_NAME)) {
				return newPolicyCmptParameterInternal(isInput);
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
		 * Creates a new value parameter without updating the src file.
		 */
		public ITestValueParameter newValueParameter() {
			TestValueParameter param = new TestValueParameter(this, getNextPartId());
			param.setInputParameter(isInput);
			testParameter.add(param);
			return param;
		}
		
		/**
		 * {@inheritDoc}
		 */
		public ITestPolicyCmptTypeParameter newPolicyCmptParameter() {
			TestPolicyCmptTypeParameter param = new TestPolicyCmptTypeParameter(this, getNextPartId());
			param.setInputParameter(isInput);
			testParameter.add(param);
			return param;
		}	
	}	
}
