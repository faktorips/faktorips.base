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

package org.faktorips.devtools.core.ui.editors.testcase;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.Validatable;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmptRelation;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * Class to represent a dummy relation depending on the test case type relation (test policy component type parameter)
 * and a concrete test policy component which is the parent of this test relation type.
 * 
 * @author Joerg Ortmann
 */
public class TestCaseTypeRelation implements Validatable{

	/** Contains the type definition of the relation */
	private ITestPolicyCmptTypeParameter testPolicyCmptTypeParameter;
	
	/** Contains the parent inside the test case model of the test relation type parameter */
	private ITestPolicyCmpt testPolicyCmpt;
	
    /**
     * Constructor for testing purposes.
     */
    protected TestCaseTypeRelation() {
    }
    
	public TestCaseTypeRelation(ITestPolicyCmptTypeParameter testPolicyCmptTypeParameter) {
		this.testPolicyCmptTypeParameter = testPolicyCmptTypeParameter;
	}
	
	/**
	 * Returns the test relation type parameter.
	 */
	public ITestPolicyCmptTypeParameter getTestPolicyCmptTypeParam(){
		return testPolicyCmptTypeParameter;
	}

	/**
	 * Returns the test policy component (concrete instance inside the test case model) 
	 * which is the parent of the test relation type.
	 */
	public ITestPolicyCmpt getParentTestPolicyCmpt() {
		return testPolicyCmpt;
	}
	
	/**
	 * Sets the test policy component (concrete instance inside the test case model) 
	 * which is the parent of the test relation type.
	 */
	public void setTestPolicyCmpt(ITestPolicyCmpt testPolicyCmpt) {
		this.testPolicyCmpt = testPolicyCmpt;
	}
	
	/**
	 * Returns the name of the test relation type parameter (test policy component type parameter).
	 */
	public String getName(){
		return testPolicyCmptTypeParameter.getName();
	}

	/**
	 * Returns the relation name which is related by the test relation type.
	 */
	public String getRelation() {
		return testPolicyCmptTypeParameter.getRelation();
	}
	
	/**
	 * Returns the relation object which is related by the test relation type.
	 */
	public IRelation findRelation() throws CoreException {
		return testPolicyCmptTypeParameter.findRelation();
	}

	/**
	 * Returns <code>true</code> if the test relation type requires a product component.
	 */
	public boolean isRequiresProductCmpt() {
		return testPolicyCmptTypeParameter.isRequiresProductCmpt();
	}

	/**
	 * Returns the name of the policy component type which is related by the test relation parameter.
	 */
	public String getPolicyCmptTypeTarget() throws CoreException {
		return findRelation().getTarget();
	}
	
	/**
	 * Returns all test attributes which are defined inside the test relation type (test policy component type parameter).
	 */
	public ITestAttribute[] getTestAttributes() {
		return testPolicyCmptTypeParameter.getTestAttributes();
	}
	
	//
	// Methods for validation interface 
	//
	
	/**
	 * {@inheritDoc}
	 */
	public int getValidationResultSeverity() throws CoreException {
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isValid() throws CoreException {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public MessageList validate() throws CoreException {
		MessageList messageList = new MessageList();
		validate(messageList);
		return messageList;
	}

	/**
	 * Validate the test relation parameter.
	 * Check if the relation which is related by this test relation exists 
	 * and validate min and max instances of this type
	 */
	private void validate(MessageList list) throws CoreException {
		MessageList msgList = testPolicyCmptTypeParameter.validate();
		list.add(msgList);
		
		if (testPolicyCmpt == null){
			return;
		}
		
		// first count relataions of this kind in the test case
		int count = 0;
		ITestPolicyCmptRelation[] relations = testPolicyCmpt.getTestPcTypeRelations();
		for (int i = 0; i < relations.length; i++) {
			if (relations[i].getTestPolicyCmptType().equalsIgnoreCase(getName())){
				count++;
			}
		}
		
		IRelation modelRelation = null;
		modelRelation = findRelation();
		if (modelRelation == null){
			String text = "The model relation \"" + testPolicyCmptTypeParameter.getRelation() + "\" for this test case relation doesn't exists.";
			Message msg = new Message("4711", text, Message.ERROR, this, ITestPolicyCmptTypeParameter.PROPERTY_POLICYCMPTTYPE);
			list.add(msg);		
		}

		if (count < testPolicyCmptTypeParameter.getMinInstances()){
			String text = "The mininum of " + testPolicyCmptTypeParameter.getMinInstances() + " is not reached.";
			Message msg = new Message("4711", text, Message.ERROR, this, ITestPolicyCmptTypeParameter.PROPERTY_POLICYCMPTTYPE);
			list.add(msg);
		}
		
		if (count > testPolicyCmptTypeParameter.getMaxInstances()){
			String text = "The maximum of " + testPolicyCmptTypeParameter.getMaxInstances() + " is reached.";
			Message msg = new Message("4711", text, Message.ERROR, this, ITestPolicyCmptTypeParameter.PROPERTY_POLICYCMPTTYPE);
			list.add(msg);			
		}
	}
}
