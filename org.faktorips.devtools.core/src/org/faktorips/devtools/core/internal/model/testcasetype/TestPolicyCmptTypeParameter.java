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

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test policy component type parameter class. 
 * Defines a test policy component type parameter for a specific test case type.
 * 
 * @author Joerg Ortmann
 */
public class TestPolicyCmptTypeParameter extends TestParameter implements
		ITestPolicyCmptTypeParameter {

	/** Tags */
	final static String TAG_NAME = "PolicyCmptTypeParameter"; //$NON-NLS-1$
	
	private String policyCmptType = ""; //$NON-NLS-1$
	
	private String relation = ""; //$NON-NLS-1$
	
	private boolean requiresProductCmpt = false;
	
	private List testAttributes = new ArrayList(0);
	
	private List testPolicyCmptTypeChilds = new ArrayList(0);
	
	private int minInstances = 0;
	
	private int maxInstances = 1;
	
	public TestPolicyCmptTypeParameter(IIpsObject parent, int id) {
		super(parent, id);
	}

	public TestPolicyCmptTypeParameter(IIpsObjectPart parent, int id) {
		super(parent, id);
	}

	/**
	 * {@inheritDoc}
	 */
	public IIpsElement[] getChildren() {
		int numOfChildren = testAttributes.size() + testPolicyCmptTypeChilds.size();
		IIpsElement[] childrenArray = new IIpsElement[numOfChildren];
		List childrenList = new ArrayList(numOfChildren);
		childrenList.addAll(testAttributes);
		childrenList.addAll(testPolicyCmptTypeChilds);
		childrenList.toArray(childrenArray);
		return childrenArray;
	}

	/**
	 * {@inheritDoc}
	 */
	protected void reinitPartCollections() {
		this.testAttributes = new ArrayList();
		this.testPolicyCmptTypeChilds = new ArrayList();
	}

	/**
	 * {@inheritDoc}
	 */
	protected void reAddPart(IIpsObjectPart part) {
		if (part instanceof TestAttribute) {
			testAttributes.add(part);
			return;
		}else if(part instanceof TestPolicyCmptTypeParameter){
			testPolicyCmptTypeChilds.add(part);
			return;
		}
		throw new RuntimeException("Unknown part type" + part.getClass()); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	protected IIpsObjectPart newPart(Element xmlTag, int id) {
		String xmlTagName = xmlTag.getNodeName();
		if (xmlTagName.equals(TestAttribute.TAG_NAME)) {
			return newTestAttributeInternal(id);
		} else if (xmlTagName.equals(TAG_NAME)) {
			return newTestPolicyCmptTypeParamChildInternal(id);
		}
		throw new RuntimeException("Could not create part for tag name: " + xmlTagName); //$NON-NLS-1$
	}	
	
	/**
	 * {@inheritDoc}
	 */
	public String getPolicyCmptType() {
		return policyCmptType;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setPolicyCmptType(String policyCmptType) {
		this.policyCmptType = policyCmptType;
	}

	/**
	 * {@inheritDoc}
	 */
	public IPolicyCmptType findPolicyCmptType() throws CoreException {
        if (StringUtils.isEmpty(policyCmptType)) {
            return null;
        }
		return getIpsProject().findPolicyCmptType(policyCmptType);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getRelation() {
		return relation;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setRelation(String relation) {
		String oldRelation = this.relation;
		this.relation = relation;
		valueChanged(oldRelation, relation);
	}

	/**
	 * {@inheritDoc}
	 */
	public IRelation findRelation() throws CoreException {
        if (StringUtils.isEmpty(relation)) {
            return null;
        }
        // if this is a root parameter then the relation field is not used
        if (isRootParameter())
        	return null;
        
        // this is a child parameter therfore a relation should exists
        ITestPolicyCmptTypeParameter parent = (ITestPolicyCmptTypeParameter) getParent();
        IPolicyCmptType policyCmptType = parent.findPolicyCmptType();
        if (policyCmptType != null){
        	return policyCmptType.getRelation(relation);
        }
        return null;
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
		policyCmptType = element.getAttribute(PROPERTY_POLICYCMPTTYPE);
		relation = element.getAttribute(PROPERTY_RELATION);
		String needsProductCmptAttr = element.getAttribute(PROPERTY_REQUIRES_PRODUCTCMT);
		if (StringUtils.isNotEmpty(needsProductCmptAttr)){
			requiresProductCmpt = needsProductCmptAttr.equalsIgnoreCase("yes")  ? true : //$NON-NLS-1$
							   needsProductCmptAttr.equalsIgnoreCase("true") ? true : //$NON-NLS-1$
							   needsProductCmptAttr.equalsIgnoreCase("1")    ? true : false; //$NON-NLS-1$
		}else{
			requiresProductCmpt = false;
		}
		try {
			minInstances = Integer.parseInt(element.getAttribute(PROPERTY_MIN_INSTANCES));
		} catch (NumberFormatException e) {
			minInstances = 0;
		}
		try {
			maxInstances = Integer.parseInt(element.getAttribute(PROPERTY_MAX_INSTANCES));
		} catch (NumberFormatException e) {
			maxInstances = 0;
		}		
	}

    /**
     * {@inheritDoc}
     */
	protected void propertiesToXml(Element element) {
		super.propertiesToXml(element);
		element.setAttribute(PROPERTY_POLICYCMPTTYPE, policyCmptType);
		element.setAttribute(PROPERTY_RELATION, relation);
		element.setAttribute(PROPERTY_REQUIRES_PRODUCTCMT, requiresProductCmpt ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
		element.setAttribute(PROPERTY_MIN_INSTANCES, "" + minInstances); //$NON-NLS-1$
		element.setAttribute(PROPERTY_MAX_INSTANCES, "" + maxInstances); //$NON-NLS-1$
	}
	
	/**
	 * {@inheritDoc}
	 */
	public ITestAttribute newTestAttribute() {
		TestAttribute a = newTestAttributeInternal(getNextPartId());
		updateSrcFile();
		return a;
	}

	/**
	 * Creates a new test attribute without updating the src file.
	 */
	private TestAttribute newTestAttributeInternal(int id) {
		TestAttribute a = new TestAttribute(this, id);
		testAttributes.add(a);
		return a;
	}

	/**
	 * {@inheritDoc}
	 */
	public ITestPolicyCmptTypeParameter newTestPolicyCmptTypeParamChild() {
		ITestPolicyCmptTypeParameter p = newTestPolicyCmptTypeParamChildInternal(getNextPartId());
		updateSrcFile();
		return p;
	}

	/**
	 * {@inheritDoc}
	 */
	public ITestAttribute getTestAttribute(String attribute) {
		for (Iterator it = testAttributes.iterator(); it.hasNext();) {
			ITestAttribute a = (ITestAttribute) it.next();
			if (a.getAttribute().equals(attribute)) {
				return a;
			}
		}
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public ITestAttribute[] getTestAttributes() {
		ITestAttribute[] a = new ITestAttribute[testAttributes.size()];
		testAttributes.toArray(a);
		return a;
	}

	/**
	 * Removes the attribute from the type. 
	 */
	void removeTestAttribute(TestAttribute attribute) {
		testAttributes.remove(attribute);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public TestPolicyCmptTypeParameter getTestPolicyCmptTypeChild(String name) {
		for (Iterator it = testPolicyCmptTypeChilds.iterator(); it.hasNext();) {
			TestPolicyCmptTypeParameter p = (TestPolicyCmptTypeParameter) it.next();
			if (p.getName().equals(name)) {
				return p;
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public ITestPolicyCmptTypeParameter[] getTestPolicyCmptTypeParamChilds() {
		TestPolicyCmptTypeParameter[] p = new TestPolicyCmptTypeParameter[testPolicyCmptTypeChilds.size()];
		testPolicyCmptTypeChilds.toArray(p);
		return p;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public ITestPolicyCmptTypeParameter getTestPolicyCmptTypeParamChild(String name){
		for (Iterator it = testPolicyCmptTypeChilds.iterator(); it.hasNext();) {
			ITestPolicyCmptTypeParameter p = (ITestPolicyCmptTypeParameter) it.next();
			if (p.getName().equals(name)) {
				return p;
			}
		}
		return null;
	}	

	/**
	 * Creates a new test policy component type param child without updating the src file.
	 */
	private TestPolicyCmptTypeParameter newTestPolicyCmptTypeParamChildInternal(int id) {
		TestPolicyCmptTypeParameter p = new TestPolicyCmptTypeParameter(this, id);
		testPolicyCmptTypeChilds.add(p);
		return p;
	}
	
	public void removeTestPolicyCmptTypeParamChild(TestPolicyCmptTypeParameter testPolicyCmptTypeParamChildName) {
		testPolicyCmptTypeChilds.remove(testPolicyCmptTypeParamChildName);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isRootParameter(){
		return (! (getParent() instanceof TestPolicyCmptTypeParameter)); 
	}

	/**
	 * {@inheritDoc}
	 */	
	public ITestPolicyCmptTypeParameter getRootParameter() {
		ITestPolicyCmptTypeParameter current = this;
		while (!current.isRootParameter()){
			current = (ITestPolicyCmptTypeParameter) current.getParent();
		}
		return current;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isRequiresProductCmpt() {
		return requiresProductCmpt;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setRequiresProductCmpt(boolean newNeedsProductCmpt) {
		boolean oldRequiresProductCmpt = this.requiresProductCmpt;
		this.requiresProductCmpt = newNeedsProductCmpt;
		valueChanged(oldRequiresProductCmpt, newNeedsProductCmpt);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public int getMinInstances() {
		return minInstances;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setMinInstances(int minInstances) {
		int oldMinInstances = this.minInstances;
		this.minInstances = minInstances;
		valueChanged(oldMinInstances, minInstances);
	}

	/**
	 * {@inheritDoc}
	 */
	public int getMaxInstances() {
		return maxInstances;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setMaxInstances(int maxInstances) {
		int oldMaxInstances = this.maxInstances;
		this.maxInstances = maxInstances;
		valueChanged(oldMaxInstances, maxInstances);
	}
}
