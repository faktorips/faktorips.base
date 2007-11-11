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

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.Validatable;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmptRelation;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * Helper class to represent a dummy relation depending on the test case type relation (test policy
 * component type parameter) and a concrete test policy component which will be the parent of all
 * concrete relations inside the test case. The concret relations are based on the test case type
 * relation.<br>
 * The parent test policy component could be used to add new relations to this test policy component
 * inside the test case (based on the test case type relation).<br>
 * Example: this class R contains the relation type RT and the test policy component A1. By using
 * this class a new test relation RT1 of type RT could be added to the policy component A.
 * 
 * @author Joerg Ortmann
 */
public class TestCaseTypeRelation implements Validatable {

	/** Contains the type definition of the relation */
	private ITestPolicyCmptTypeParameter testPolicyCmptTypeParameter;
	
	/** Contains the parent inside the test case model of the test relation type parameter */
	private ITestPolicyCmpt parentTestPolicyCmpt;
	
    /**
     * Constructor for testing purposes.
     */
    protected TestCaseTypeRelation() {
    }
    
	public TestCaseTypeRelation(ITestPolicyCmptTypeParameter testPolicyCmptTypeParameter,
            ITestPolicyCmpt parentTestPolicyCmpt) {
        this.testPolicyCmptTypeParameter = testPolicyCmptTypeParameter;
        this.parentTestPolicyCmpt = parentTestPolicyCmpt;
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
		return parentTestPolicyCmpt;
	}
	
	/**
	 * Sets the test policy component (concrete instance inside the test case model) 
	 * which is the parent of the test relation type.
	 */
	public void setParentTestPolicyCmpt(ITestPolicyCmpt testPolicyCmpt) {
		this.parentTestPolicyCmpt = testPolicyCmpt;
	}
	
	/**
	 * Returns the name of the test relation type parameter (test policy component type parameter).
	 */
	public String getName(){
        if (testPolicyCmptTypeParameter == null)
            return ""; //$NON-NLS-1$
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
	 * @param ipsProject TODO
	 */
	public IPolicyCmptTypeAssociation findRelation(IIpsProject ipsProject) throws CoreException {
		return testPolicyCmptTypeParameter.findRelation(ipsProject);
	}

	/**
	 * Returns <code>true</code> if the test relation type requires a product component.
	 */
	public boolean isRequiresProductCmpt() {
		return testPolicyCmptTypeParameter.isRequiresProductCmpt();
	}
	
    /**
	 * Returns <code>true</code> if the test relation type is an input parameter.
	 */
	public boolean isInput() {
	    return testPolicyCmptTypeParameter.isInputParameter();
	}

    /**
	 * Returns <code>true</code> if the test relation type is an expected result parameter.
	 */
	public boolean isExpectedResult() {
	    return testPolicyCmptTypeParameter.isExpextedResultParameter();
	}

	/**
	 * Returns the name of the policy component type which is related by the test relation parameter.
	 * @param ipsProject TODO
	 */
	public String getPolicyCmptTypeTarget(IIpsProject ipsProject) throws CoreException {
		return findRelation(ipsProject).getTarget();
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
	public MessageList validate(IIpsProject ipsProject) throws CoreException {
		MessageList messageList = new MessageList();
		validate(messageList, ipsProject);
		return messageList;
	}

	/**
     * Validate the test policy cmpt relation parameters. And validate the min and max instances of
     * the test policy cmpt type param by validating the parent test policy cmpt
     */
	private void validate(MessageList list, IIpsProject ipsProject) throws CoreException {
		if (parentTestPolicyCmpt == null){
			return;
		}
		
		// delegate the validation to the corresponding test policy component relation
        //   validate all relations with the same name, because this relations are grouped to one
        //   element
		ITestPolicyCmptRelation[] relations = parentTestPolicyCmpt.getTestPolicyCmptRelations();
		HashMap messages = new HashMap();
        for (int i = 0; i < relations.length; i++) {
		    ITestPolicyCmptRelation testPolicyCmptRelation = null;
			if (relations[i].getTestPolicyCmptTypeParameter().equals(getName())){
                testPolicyCmptRelation = relations[i];
                MessageList msgList = testPolicyCmptRelation.validate(ipsProject);
                // add only unique messages
                for (Iterator iter = msgList.iterator(); iter.hasNext();){
                    Message msg = (Message)iter.next();
                    messages.put(msg.getCode(), msg);
                }
			}
		}
        
        // get the validation messages of the number of instances from the parent test policy cmpt,
        // thus it could be displayed on the relation symbol
        MessageList ml = parentTestPolicyCmpt.validate(null);
        
        MessageList mlMin = ml.getMessagesFor(parentTestPolicyCmpt, ITestPolicyCmptTypeParameter.PROPERTY_MIN_INSTANCES);
        for (Iterator iter = mlMin.iterator(); iter.hasNext();) {
            Message msg = (Message)iter.next();
            if (ifMessageRelevant(msg)){
                messages.put(msg.getCode(), msg);
            }
        }
        MessageList mlMax = ml.getMessagesFor(parentTestPolicyCmpt, ITestPolicyCmptTypeParameter.PROPERTY_MAX_INSTANCES);
        for (Iterator iter = mlMax.iterator(); iter.hasNext();) {
            Message msg = (Message)iter.next();
            if (ifMessageRelevant(msg)){
                messages.put(msg.getCode(), msg);
            }
        }
        
        // add the unique test relation messages to the list of messages
        for (Iterator iter = messages.values().iterator(); iter.hasNext();) {
            list.add((Message)iter.next());
        }
	}

    /*
     * Returns true if the message is relevant for this relation.
     */
    private boolean ifMessageRelevant(Message msg) {
        return (msg.getText().indexOf("\"" + getName()+ "\"")>=0); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
