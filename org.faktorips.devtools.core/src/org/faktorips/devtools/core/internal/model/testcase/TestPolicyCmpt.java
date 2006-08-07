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
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.testcase.ITestAttributeValue;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmptRelation;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.StringUtil;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test policy component. Defines a test policy component within a test case definition.
 * 
 * @author Joerg Ortmann
 */
public class TestPolicyCmpt extends TestObject implements
		ITestPolicyCmpt {

	/* Tags */
	final static String TAG_NAME = "PolicyCmptTypeObject";
	
	private String testPolicyCmptType = "";
	
	private String productCmpt = "";
	
	private String label = "";
	
	private List testAttributeValues = new ArrayList(0);
	
	private List testPolicyCmptRelations = new ArrayList(0);
	
	public TestPolicyCmpt(IIpsObject parent, int id) {
		super(parent, id);
	}

	public TestPolicyCmpt(IIpsObjectPart parent, int id) {
		super(parent, id);
	}

	/**
	 * {@inheritDoc}
	 */
	public IIpsElement[] getChildren() {
		int numOfChildren = testAttributeValues.size() + testPolicyCmptRelations.size();
		IIpsElement[] childrenArray = new IIpsElement[numOfChildren];
		List childrenList = new ArrayList(numOfChildren);
		childrenList.addAll(testAttributeValues);
		childrenList.addAll(testPolicyCmptRelations);
		childrenList.toArray(childrenArray);
		return childrenArray;
	}

	/**
	 * {@inheritDoc}
	 */
	protected void reinitPartCollections() {
		this.testAttributeValues = new ArrayList();
		this.testPolicyCmptRelations = new ArrayList();
	}

	/**
	 * {@inheritDoc}
	 */
	protected void reAddPart(IIpsObjectPart part) {
		if (part instanceof TestAttributeValue) {
			testAttributeValues.add(part);
			return;
		}else if(part instanceof TestPolicyCmptRelation){
			testPolicyCmptRelations.add(part);
			return;
		}
		throw new RuntimeException("Unknown part type" + part.getClass()); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	protected IIpsObjectPart newPart(Element xmlTag, int id) {
		String xmlTagName = xmlTag.getNodeName();
		if (xmlTagName.equals(TestAttributeValue.TAG_NAME)) {
			return newTestAttributeValueInternal(id);
		} else if (xmlTagName.equals(TestPolicyCmptRelation.TAG_NAME)) {
			return newTestPcTypeRelationInternal(id);
		}
		throw new RuntimeException("Could not create part for tag name: " + xmlTagName); //$NON-NLS-1$
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
		valueChanged(oldPolicyCmptType, newPolicyCmptType);
	}

	/**
	 * {@inheritDoc}
	 */
	public ITestPolicyCmptTypeParameter findTestPolicyCmptType() throws CoreException {
        if (StringUtils.isEmpty(testPolicyCmptType)) {
            return null;
        }
        return getTestCase().findTestPolicyCmptTypeParameter(this);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setProductCmpt(String newProductCmpt) {
		String oldTestProductCmpt = this.productCmpt;
		this.productCmpt = newProductCmpt;
		valueChanged(oldTestProductCmpt, newProductCmpt);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getProductCmpt() {
		return productCmpt;
	}

	/**
	 * {@inheritDoc}
	 */
	public IProductCmpt findProductCmpt() throws CoreException {
        if (StringUtils.isEmpty(productCmpt)) {
            return null;
        }
		return getIpsProject().findProductCmpt(productCmpt);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setLabel(String newLabel) {
		String oldLabel = this.label;
		this.label = newLabel;
		valueChanged(oldLabel, newLabel);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getLabel() {
		return label;
	}
	
	/**
	 * Returns the top level test case.
	 */
	public ITestCase getTestCase(){
		return ((ITestCase) getRoot().getParent().getParent());
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
		productCmpt = element.getAttribute(PROPERTY_PRODUCTCMPT);
		label = element.getAttribute(PROPERTY_LABEL);
	}

    /**
     * {@inheritDoc}
     */
	protected void propertiesToXml(Element element) {
		super.propertiesToXml(element);
		element.setAttribute(PROPERTY_POLICYCMPTTYPE, testPolicyCmptType);
		element.setAttribute(PROPERTY_PRODUCTCMPT, productCmpt);
		element.setAttribute(PROPERTY_LABEL, label);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public ITestAttributeValue newTestAttributeValue() {
		TestAttributeValue a = newTestAttributeValueInternal(getNextPartId());
		updateSrcFile();
		return a;
	}

	/**
	 * Creates a new test attribute without updating the src file.
	 */
	private TestAttributeValue newTestAttributeValueInternal(int id) {
		TestAttributeValue a = new TestAttributeValue(this, id);
		testAttributeValues.add(a);
		return a;
	}

	/**
	 * {@inheritDoc}
	 */
	public ITestAttributeValue getTestAttributeValue(String name) {
		for (Iterator it = testAttributeValues.iterator(); it.hasNext();) {
			ITestAttributeValue a = (ITestAttributeValue) it.next();
			if (a.getName().equals(name)) {
				return a;
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public ITestAttributeValue[] getTestAttributeValues() {
		ITestAttributeValue[] a = new ITestAttributeValue[testAttributeValues.size()];
		testAttributeValues.toArray(a);
		return a;
	}

	/**
	 * Removes the attribute from the type. 
	 */
	void removeTestAttributeValue(TestAttributeValue attribute) {
		testAttributeValues.remove(attribute);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public ITestPolicyCmptRelation getTestPcTypeRelation(String testPolicyCmptType) {
		ArgumentCheck.notNull(testPolicyCmptType);
		for (Iterator it = testPolicyCmptRelations.iterator(); it.hasNext();) {
			ITestPolicyCmptRelation r = (ITestPolicyCmptRelation) it.next();
			if (r.getTestPolicyCmptType().equals(testPolicyCmptType)) {
				return r;
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public ITestPolicyCmptRelation[] getTestPolicyCmptRelations() {
		ITestPolicyCmptRelation[] r = new ITestPolicyCmptRelation[testPolicyCmptRelations.size()];
		testPolicyCmptRelations.toArray(r);
		return r;
	}

	/**
	 * {@inheritDoc}
	 */
	public ITestPolicyCmptRelation[] getTestPolicyCmptRelations(String typeParameterName) {
		ArrayList relations = new ArrayList();
		for (Iterator iter = testPolicyCmptRelations.iterator(); iter.hasNext();) {
			ITestPolicyCmptRelation element = (ITestPolicyCmptRelation) iter.next();
			if (element.getTestPolicyCmptType().equals(typeParameterName))
				relations.add(element);
		}
		return (ITestPolicyCmptRelation[]) relations.toArray(new ITestPolicyCmptRelation[0]);
	}

	/**
	 * {@inheritDoc}
	 */
	public ITestPolicyCmptRelation newTestPolicyCmptRelation() {
		ITestPolicyCmptRelation r = newTestPcTypeRelationInternal(getNextPartId());
		updateSrcFile();
		return r;
	}
	
	/**
	 * Creates a new test relation without updating the src file.
	 */
	private TestPolicyCmptRelation newTestPcTypeRelationInternal(int id) {
		TestPolicyCmptRelation r = new TestPolicyCmptRelation(this, id);
		testPolicyCmptRelations.add(r);
		return r;
	}
	
	/**
	 * Removes the relation from the type. 
	 */
	void removeTestPcTypeRelation(TestPolicyCmptRelation relation) {
		testPolicyCmptRelations.remove(relation);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean isRoot(){
		return (! (getParent() instanceof TestPolicyCmptRelation)); 
	}

	/**
	 * {@inheritDoc}
	 */
    public ITestPolicyCmpt getRoot(){
    	ITestPolicyCmpt testPolicyCmpt = this;
    	while (!testPolicyCmpt.isRoot()) {
			testPolicyCmpt = testPolicyCmpt.getParentPolicyCmpt();
		}
		return testPolicyCmpt;
    }

    /**
     * {@inheritDoc}
     */
	public ITestPolicyCmpt getParentPolicyCmpt() {
		if (isRoot()){
			return null;
		}
		ITestPolicyCmptRelation testPcTypeRelation = (ITestPolicyCmptRelation) getParent();
		return  (ITestPolicyCmpt) testPcTypeRelation.getParent();
	}
	
    /**
     * {@inheritDoc}
     */	
	public void removeRelation(ITestPolicyCmptRelation relation){
		boolean found = false;
		int idx = 0;
		for (Iterator iter = testPolicyCmptRelations.iterator(); iter.hasNext();) {
			ITestPolicyCmptRelation element = (ITestPolicyCmptRelation) iter.next();
			if (element == relation){
				found = true;
				break;
			}
			idx ++;
		}
		if (found){
			testPolicyCmptRelations.remove(idx);
			updateSrcFile();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public ITestPolicyCmptRelation addTestPcTypeRelation(ITestPolicyCmptTypeParameter typeParam, String productCmpt, String targetName) throws CoreException {
		ArgumentCheck.notNull(typeParam);
		
		IRelation relation = typeParam.findRelation();
		if (relation == null){
			throw new CoreException(new IpsStatus(NLS.bind("Realtion not found \"{0}\".", typeParam.getRelation())));
		}
		
		ITestPolicyCmptRelation newTestPcTypeRelation = null;
		if (!relation.isAssoziation()){
			// relation is composition
			//   add new relation including a test policy component child
			newTestPcTypeRelation = new TestPolicyCmptRelation(this, getNextPartId());
			newTestPcTypeRelation.setTestPolicyCmptType(typeParam.getName());
			
			ITestPolicyCmpt newTestPolicyCmpt = newTestPcTypeRelation.newTargetTestPolicyCmptChild();
			newTestPolicyCmpt.setTestPolicyCmptType(typeParam.getName());
			newTestPolicyCmpt.setProductCmpt(productCmpt);
			
			// sets the label for the new child test policy component
			String label = "";
			if (StringUtils.isEmpty(productCmpt)){
				label = newTestPolicyCmpt.getTestPolicyCmptType();
			}else{
				label = productCmpt;
			}
			label = StringUtil.unqualifiedName(label);
			label = generateUniqueLabelOfTestPolicyCmpt(newTestPolicyCmpt, label);
			newTestPolicyCmpt.setLabel(label);
			
			// add the attributes which are defined in the test case type parameter
			ITestAttribute attributes[] = typeParam.getTestAttributes();
			for (int i = 0; i < attributes.length; i++) {
				ITestAttribute attribute = attributes[i];
				ITestAttributeValue attrValue = newTestPolicyCmpt.newTestAttributeValue();
				attrValue.setTestAttribute(attribute.getAttribute());
			}
		} else{
			// relation is assoziation
			//   add new assoziation relation (only the target will be set and no child will be created)
			newTestPcTypeRelation = new TestPolicyCmptRelation(this, getNextPartId());
			newTestPcTypeRelation.setTestPolicyCmptType(typeParam.getName());
			newTestPcTypeRelation.setTarget(targetName);
		}
		
		// add the new relation at the end of the existing relations, grouped by the relation name
		ITestPolicyCmptRelation prevRelationWithSameName = null;
		for (Iterator iter = testPolicyCmptRelations.iterator(); iter.hasNext();) {
			ITestPolicyCmptRelation currRelation = (ITestPolicyCmptRelation) iter.next();
			if (newTestPcTypeRelation.getTestPolicyCmptType().equals(currRelation.getTestPolicyCmptType())){
				prevRelationWithSameName = currRelation;
			}
		}
		if (prevRelationWithSameName != null){
			int idx = testPolicyCmptRelations.indexOf(prevRelationWithSameName);
			testPolicyCmptRelations.add(idx+1, newTestPcTypeRelation);
		}else{
			testPolicyCmptRelations.add(newTestPcTypeRelation);
		}
		updateSrcFile();
		return newTestPcTypeRelation;
	}
	
	/**
	 * Generate and set a unique label for the given test policy component.
	 */
	private String generateUniqueLabelOfTestPolicyCmpt(ITestPolicyCmpt newTestPolicyCmpt, String label) {
		String uniqueLabel = label;

		// eval the unique idx of new component
		int idx = 1;
		String newUniqueLabel = uniqueLabel;
		if (newTestPolicyCmpt.isRoot()){
			ITestPolicyCmpt[] testPolicyCmpts = getTestCase().getInputPolicyCmpt();
			for (int i = 0; i < testPolicyCmpts.length; i++) {
				ITestPolicyCmpt cmpt = testPolicyCmpts[i];
				if (newUniqueLabel.equals(cmpt.getLabel())){
					idx ++;
					newUniqueLabel = uniqueLabel + " (" + idx + ")";
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
				newUniqueLabel = uniqueLabel + " (" + idx + ")";
			}
		}
		return newUniqueLabel;
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected void validateThis(MessageList list) throws CoreException {
		super.validateThis(list);
		// validate if the test case type param exists
		ITestPolicyCmptTypeParameter param = null;
		try {
			param = getTestCase().findTestPolicyCmptTypeParameter(this);
		} catch (CoreException e) {
			//	ignore exception, the param will be used to indicate errors
		}
		
		if (param == null){
			String text = "The test case type definition for this policy component doesn't exists.";
			Message msg = new Message("4711", text, Message.ERROR, this, PROPERTY_POLICYCMPTTYPE);
			list.add(msg);	
		}
		
		for (Iterator iter = testAttributeValues.iterator(); iter.hasNext();) {
			MessageList msgAttributeValueValidation;
			ITestAttributeValue attrValue = (ITestAttributeValue) iter.next();
			msgAttributeValueValidation = attrValue.validate();
			list.add(msgAttributeValueValidation);
		}
	}	
}
