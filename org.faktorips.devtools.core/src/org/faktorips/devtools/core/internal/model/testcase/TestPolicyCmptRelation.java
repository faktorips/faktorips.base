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

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.internal.model.IpsObjectPart;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmptRelation;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test policy component relation. Defines a relation for a policy component
 * class within a test case defination.
 * 
 * @author Joerg Ortmann
 */
public class TestPolicyCmptRelation extends IpsObjectPart implements
		ITestPolicyCmptRelation {

	public static final String TAG_NAME = "Relation";

	private String testPolicyCmptType = "";

	private String target = "";

	private ITestPolicyCmpt targetChild;

	private boolean deleted = false;

	private boolean isTransient = false;

	public TestPolicyCmptRelation(IIpsObject parent, int id) {
		super(parent, id);
	}

	public TestPolicyCmptRelation(IIpsObjectPart parent, int id) {
		super(parent, id);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getTestPolicyCmptType() {
		return testPolicyCmptType;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setTestPolicyCmptType(String newPolicyCmptType) {
		String oldPolicyCmptType = this.testPolicyCmptType;
		this.testPolicyCmptType = newPolicyCmptType;
		if (!isTransient)
			valueChanged(oldPolicyCmptType, newPolicyCmptType);
	}

	/**
	 * {@inheritDoc}
	 */
	public ITestPolicyCmptTypeParameter findTestPolicyCmptType()
			throws CoreException {
		if (StringUtils.isEmpty(testPolicyCmptType)) {
			return null;
		}
		return getTestCase().findTestPolicyCmptTypeParameter(this);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getTarget() {
		return target;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setTarget(String target) {
		this.target = target;
	}

	/**
	 * {@inheritDoc}
	 */
	public ITestPolicyCmpt findTarget() throws CoreException {
		if (targetChild != null) {
			return targetChild;
		}
		if (StringUtils.isEmpty(target)) {
			return null;
		}
		
		// the target is an association, search for the target in the test case
		if (isInput()) {
			return getTestCase().findInputPolicyCmpt(target);
		} else {
			return getTestCase().findExpectedResultPolicyCmpt(target);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	protected Element createElement(Document doc) {
		return doc.createElement(TAG_NAME);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void initPropertiesFromXml(Element element, Integer id) {
		super.initPropertiesFromXml(element, id);
		testPolicyCmptType = element.getAttribute(PROPERTY_POLICYCMPTTYPE);
		target = element.getAttribute(PROPERTY_TARGET);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void propertiesToXml(Element element) {
		super.propertiesToXml(element);
		element.setAttribute(PROPERTY_POLICYCMPTTYPE, testPolicyCmptType);
		element.setAttribute(PROPERTY_TARGET, target);
	}

	/**
	 * {@inheritDoc}
	 */
	public void delete() {
		((TestPolicyCmpt) getIpsObject()).removeTestPcTypeRelation(this);
		if (!isTransient)
			updateSrcFile();
		deleted = true;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isDeleted() {
		return deleted;
	}

	/**
	 * {@inheritDoc}
	 */
	public Image getImage() {
		return null;
	}

	/**
	 * This object has no parts.
	 */
	public IIpsObjectPart newPart(Class partType) {
		throw new IllegalArgumentException("Unknown part type" + partType); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	public ITestPolicyCmpt newTargetTestPolicyCmptChild() {
		ITestPolicyCmpt param = newTargetTestPolicyCmptChildInternal(getNextPartId());
		return param;
	}

	/**
	 * Creates a new test policy component type parameter as target for this
	 * relation without updating the src file.
	 */
	private ITestPolicyCmpt newTargetTestPolicyCmptChildInternal(int id) {
		TestPolicyCmpt param = new TestPolicyCmpt(this, id);
		targetChild = param;
		return param;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isComposition() {
		return targetChild != null;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isAccociation() {
		return targetChild == null;
	}

	/**
	 * {@inheritDoc}
	 */
	public IIpsElement[] getChildren() {
		IIpsElement[] childrenArray = null;
		if (targetChild != null) {
			childrenArray = new IIpsElement[1];
			childrenArray[0] = targetChild;
		} else {
			childrenArray = new IIpsElement[0];
		}
		return childrenArray;
	}

	/**
	 * {@inheritDoc}
	 */
	protected void reinitPartCollections() {
		targetChild = null;
	}

	/**
	 * {@inheritDoc}
	 */
	protected void reAddPart(IIpsObjectPart part) {
		if (part instanceof TestPolicyCmpt) {
			targetChild = (TestPolicyCmpt) part;
			return;
		}
		throw new RuntimeException("Unknown part type: " + part.getClass()); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	protected IIpsObjectPart newPart(Element xmlTag, int id) {
		String xmlTagName = xmlTag.getNodeName();
		if (xmlTagName.equals(TestPolicyCmpt.TAG_NAME)) {
			return newTargetTestPolicyCmptChildInternal(id);
		}
		throw new RuntimeException(
				"Could not create part for tag name: " + xmlTagName); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	public ITestCase getTestCase() {
		return ((ITestPolicyCmpt) getParent()).getTestCase();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setTransient(boolean isTransient) {
		this.isTransient = isTransient;
	}

	/**
	 * {@inheritDoc}
	 */
	protected void validate(MessageList list) throws CoreException {
		super.validate(list);

		// validate the count of relation with the min and max occurens
		ITestPolicyCmptTypeParameter param = null;
		try {
			param = getTestCase().findTestPolicyCmptTypeParameter(this);
		} catch (CoreException e) {
			// ignore exception, the param will be used to indicate errors
		}
		
		if (param == null){
			String text = "The test case type definition for this relation doesn't exists.";
			Message msg = new Message("4711", text, Message.ERROR, this, PROPERTY_POLICYCMPTTYPE);
			list.add(msg);	
		}
	}

	/**
	 * Returns <code>true</code> if this relation belongs to a test policy
	 * component which is an input object of the test case, otherwise if it is
	 * an expected result object return <code>false</code>
	 */
	private boolean isInput() {
		return ((ITestPolicyCmpt) getParent()).isInputObject();
	}
}
