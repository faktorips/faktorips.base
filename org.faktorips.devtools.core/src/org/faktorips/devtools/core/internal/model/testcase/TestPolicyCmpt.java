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
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.testcase.ITestAttributeValue;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestObject;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmptRelation;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.util.ListElementMover;
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
public class TestPolicyCmpt extends TestObject implements ITestPolicyCmpt {

	/* Tags */
	final static String TAG_NAME = "PolicyCmptTypeObject"; //$NON-NLS-1$
	
	private String testPolicyCmptType = ""; //$NON-NLS-1$
	
	private String productCmpt = ""; //$NON-NLS-1$
	
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
    protected void removePart(IIpsObjectPart part) {
        if (part instanceof TestAttributeValue) {
            testAttributeValues.remove(part);
            return;
        }else if(part instanceof TestPolicyCmptRelation){
            testPolicyCmptRelations.remove(part);
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
	public String getTestPolicyCmptTypeParameter() {
		return testPolicyCmptType;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setTestPolicyCmptTypeParameter(String testPolicyCmptTypeParameter) {
		String oldPolicyCmptType = this.testPolicyCmptType;
		this.testPolicyCmptType = testPolicyCmptTypeParameter;
		valueChanged(oldPolicyCmptType, testPolicyCmptTypeParameter);
	}

	/**
	 * {@inheritDoc}
	 */
	public ITestPolicyCmptTypeParameter findTestPolicyCmptTypeParameter(IIpsProject ipsProject) throws CoreException {
        if (StringUtils.isEmpty(testPolicyCmptType)) {
            return null;
        }
        return ((TestCase)getTestCase()).findTestPolicyCmptTypeParameter(this, ipsProject);
	}

    /**
     * {@inheritDoc}
     */
    public String getTestParameterName() {
        return testPolicyCmptType;
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
	public IProductCmpt findProductCmpt(IIpsProject ipsProject) throws CoreException {
        if (StringUtils.isEmpty(productCmpt)) {
            return null;
        }
        IProductCmpt pc = ipsProject.findProductCmpt(productCmpt);
        return pc;
	}
    
    /**
     * {@inheritDoc}
     */
    public boolean isProductRelevant(){
        return !StringUtils.isEmpty(productCmpt);
    }
	
	/**
	 * {@inheritDoc}
	 */
	public void setName(String newName) {
		String oldName = this.name;
		this.name = newName;
		valueChanged(oldName, newName);
	}

	/**
	 * Returns the top level test case.
	 */
	public ITestCase getTestCase(){
		return ((ITestCase) getRoot().getParent());
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
        name = element.getAttribute(PROPERTY_NAME);
		testPolicyCmptType = element.getAttribute(PROPERTY_POLICYCMPTTYPE);
		productCmpt = element.getAttribute(PROPERTY_PRODUCTCMPT);
	}

    /**
     * {@inheritDoc}
     */
	protected void propertiesToXml(Element element) {
		super.propertiesToXml(element);
		element.setAttribute(PROPERTY_NAME, name);
		element.setAttribute(PROPERTY_POLICYCMPTTYPE, testPolicyCmptType);
		element.setAttribute(PROPERTY_PRODUCTCMPT, productCmpt);
	}
	
    /**
     * {@inheritDoc}
     */
	public Image getImage() {
        if (StringUtils.isNotEmpty(productCmpt)){
            return IpsObjectType.PRODUCT_CMPT.getEnabledImage();
        } else {
            return IpsObjectType.POLICY_CMPT_TYPE.getEnabledImage();
        }
    }

    /**
	 * {@inheritDoc}
	 */
	public ITestAttributeValue newTestAttributeValue() {
		TestAttributeValue a = newTestAttributeValueInternal(getNextPartId());
		objectHasChanged();
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
			if (a.getTestAttribute().equals(name)) {
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
	public ITestPolicyCmptRelation getTestPolicyCmptRelation(String testPolicyCmptType) {
		ArgumentCheck.notNull(testPolicyCmptType);
		for (Iterator it = testPolicyCmptRelations.iterator(); it.hasNext();) {
			ITestPolicyCmptRelation r = (ITestPolicyCmptRelation) it.next();
			if (r.getTestPolicyCmptTypeParameter().equals(testPolicyCmptType)) {
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
			if (element.getTestPolicyCmptTypeParameter().equals(typeParameterName))
				relations.add(element);
		}
		return (ITestPolicyCmptRelation[]) relations.toArray(new ITestPolicyCmptRelation[0]);
	}

	/**
	 * {@inheritDoc}
	 */
	public ITestPolicyCmptRelation newTestPolicyCmptRelation() {
		ITestPolicyCmptRelation r = newTestPcTypeRelationInternal(getNextPartId());
		objectHasChanged();
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
    public ITestObject getRoot(){
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
	public void removeRelation(ITestPolicyCmptRelation relation) {
		int idx = 0;
        int foundIdx = -1;
		for (Iterator iter = testPolicyCmptRelations.iterator(); iter.hasNext();) {
			ITestPolicyCmptRelation element = (ITestPolicyCmptRelation) iter.next();
			if (element == relation){
                foundIdx = idx;
                break;
			}
			idx ++;
		}
		if (foundIdx >= 0){
			testPolicyCmptRelations.remove(foundIdx);
			objectHasChanged();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public ITestPolicyCmptRelation addTestPcTypeRelation(ITestPolicyCmptTypeParameter typeParam, String productCmpt, String targetName) throws CoreException {
		ArgumentCheck.notNull(typeParam);
		
		IPolicyCmptTypeAssociation relation = typeParam.findRelation(typeParam.getIpsProject());
		if (relation == null){
			throw new CoreException(new IpsStatus(NLS.bind(Messages.TestPolicyCmpt_Error_RelationNotFound, typeParam.getRelation())));
		}
		
        ITestPolicyCmptRelation newTestPcTypeRelation = null;
		if (!relation.isAssoziation()){
			// relation is composition
			//   add new relation including a test policy component child
			newTestPcTypeRelation = new TestPolicyCmptRelation(this, getNextPartId());
			newTestPcTypeRelation.setTestPolicyCmptTypeParameter(typeParam.getName());
			
			ITestPolicyCmpt newTestPolicyCmpt = newTestPcTypeRelation.newTargetTestPolicyCmptChild();
			newTestPolicyCmpt.setTestPolicyCmptTypeParameter(typeParam.getName());
			newTestPolicyCmpt.setProductCmpt(StringUtils.isEmpty(productCmpt)?"":productCmpt); //$NON-NLS-1$
			
			// sets the label for the new child test policy component
			String name = ""; //$NON-NLS-1$
			if (StringUtils.isEmpty(productCmpt)){
				name = newTestPolicyCmpt.getTestPolicyCmptTypeParameter();
			}else{
				name = productCmpt;
			}
            name = StringUtil.unqualifiedName(name);
            name = getTestCase().generateUniqueNameForTestPolicyCmpt(newTestPolicyCmpt, name);
			newTestPolicyCmpt.setName(name);
			
            // add all test attribute values as spedified in the test parameter type
			ITestAttribute attributes[] = typeParam.getTestAttributes();
			for (int i = 0; i < attributes.length; i++) {
				ITestAttribute attribute = attributes[i];
				ITestAttributeValue attrValue = newTestPolicyCmpt.newTestAttributeValue();
				attrValue.setTestAttribute(attribute.getName());
			}
            // set the defaults for all attribute values
            newTestPolicyCmpt.updateDefaultTestAttributeValues();

		} else{
			// relation is assoziation
			//   add new assoziation relation (only the target will be set and no child will be created)
			newTestPcTypeRelation = new TestPolicyCmptRelation(this, getNextPartId());
			newTestPcTypeRelation.setTestPolicyCmptTypeParameter(typeParam.getName());
			newTestPcTypeRelation.setTarget(targetName);
		}
		
		// add the new relation at the end of the existing relations, grouped by the relation name
		ITestPolicyCmptRelation prevRelationWithSameName = null;
		for (Iterator iter = testPolicyCmptRelations.iterator(); iter.hasNext();) {
			ITestPolicyCmptRelation currRelation = (ITestPolicyCmptRelation) iter.next();
			if (newTestPcTypeRelation.getTestPolicyCmptTypeParameter().equals(currRelation.getTestPolicyCmptTypeParameter())){
				prevRelationWithSameName = currRelation;
			}
		}
        
		if (prevRelationWithSameName != null){
			int idx = testPolicyCmptRelations.indexOf(prevRelationWithSameName);
			testPolicyCmptRelations.add(idx+1, newTestPcTypeRelation);
		}else{
			testPolicyCmptRelations.add(newTestPcTypeRelation);
		}
        
        fixDifferentChildSortOrder();
        
		objectHasChanged();
		return newTestPcTypeRelation;
	}

	/**
     * {@inheritDoc}
	 */
    public void updateDefaultTestAttributeValues() throws CoreException{
        // add the attributes which are defined in the test case type parameter
        IProductCmptGeneration generation = findProductCmpsCurrentGeneration(getIpsProject());
        ITestAttributeValue[] testAttrValues = getTestAttributeValues();
        for (int i = 0; i < testAttrValues.length; i++) {
            ((TestAttributeValue)testAttrValues[i]).setDefaultTestAttributeValueInternal(generation);
        }
    }

    /**
     * {@inheritDoc}
     */
    public int getIndexOfChildTestPolicyCmpt(ITestPolicyCmpt testPolicyCmpt) throws CoreException {
        Assert.isNotNull(testPolicyCmpt);
        int idx = 0;
        for (Iterator iter = testPolicyCmptRelations.iterator(); iter.hasNext();) {
            ITestPolicyCmptRelation testPolicyCmptRelation = (ITestPolicyCmptRelation)iter.next();
            if (testPolicyCmpt.equals(testPolicyCmptRelation.findTarget())){
                return idx;
            }
            idx ++;
        }
        throw new CoreException(new IpsStatus(Messages.TestPolicyCmpt_Error_MoveNotPossibleBelongsToNoRelation));
    }

    /**
     * {@inheritDoc}
     */
    public int[] moveTestPolicyCmptRelations(int[] indexes, boolean up) {
        ListElementMover mover = new ListElementMover(testPolicyCmptRelations);
        int[] newIdxs = mover.move(indexes, up);
        valueChanged(indexes, newIdxs);
        return newIdxs;
    }

    /**
     * Returns the product components generation depending on the current working date (current working generation).
     * Returns <code>null</code> if the test policy cmpt is not product relevant, or the product cmpt wasn't found.
     */
    public IProductCmptGeneration findProductCmpsCurrentGeneration(IIpsProject ipsProject) throws CoreException {
        if (StringUtils.isEmpty(productCmpt)){
            return null;
        }
        GregorianCalendar workingDate = IpsPlugin.getDefault().getIpsPreferences().getWorkingDate();
        IProductCmpt productCmptObj = ipsProject.findProductCmpt(productCmpt);
        IProductCmptGeneration generation = null;
        if (productCmptObj != null){
            generation = (IProductCmptGeneration)productCmptObj.findGenerationEffectiveOn(workingDate);
        }
        return generation;
    }
	
    /**
     * Fix the sort order of the child test policy cmpt relations in order to the corresponding test
     * policy cmpt type parameter.
     * 
     * @throws CoreException in case of an error
     */
    void fixDifferentChildSortOrder() throws CoreException {
        List oldRelations = testPolicyCmptRelations;
        IIpsProject ipsProject = getIpsProject();
        // fill temp. storage of the relations for a test parameter
        HashMap param2Relations = new HashMap(oldRelations.size());
        for (Iterator iter = oldRelations.iterator(); iter.hasNext();) {
            ITestPolicyCmptRelation testPolicyCmptRelation = (ITestPolicyCmptRelation)iter.next();
            ITestPolicyCmptTypeParameter paramOfRelation = testPolicyCmptRelation.findTestPolicyCmptTypeParameter(ipsProject);
            List relationList = (List)param2Relations.get(paramOfRelation);
            if (relationList == null) {
                relationList = new ArrayList();
            }
            relationList.add(testPolicyCmptRelation);
            param2Relations.put(paramOfRelation, relationList);
        }
        
        // sort the list of relations for each parameter in order of their parameter
        List newChildList = new ArrayList();
        ITestPolicyCmptTypeParameter param = findTestPolicyCmptTypeParameter(getIpsProject());
        if (param == null) {
            throw new RuntimeException("Test parameter not found: " + testPolicyCmptType + "!"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        ITestPolicyCmptTypeParameter[] paramChild = param.getTestPolicyCmptTypeParamChilds();
        // iterate over all relations in the corresponding parameter and add the relation lists to
        // the new whole relation list
        for (int i = 0; i < paramChild.length; i++) {
            // get the list of relations for the parameter
            List relations = (List)param2Relations.get(paramChild[i]);
            if (relations == null)
                // ignore if there are no such kind of relation 
                continue;
            newChildList.addAll(relations);
        }
        testPolicyCmptRelations = newChildList;
        valueChanged(false, true);
    }
    
    /**
     * Fix the sort order of the test attribute values in order to the corresponding test
     * policy cmpt type parameter test attributes.
     * 
     * @throws CoreException in case of an error
     */

    void fixDifferentTestAttrValueSortOrder() throws CoreException {
        List newTestAttrValueList = new ArrayList();
        ITestPolicyCmptTypeParameter param = findTestPolicyCmptTypeParameter(getIpsProject());
        ITestAttribute[] testAttr = param.getTestAttributes();
        for (int i = 0; i < testAttr.length; i++) {
            ITestAttributeValue testAttrValue = getTestAttributeValue(testAttr[i].getName());
            if (testAttrValue == null) {
                throw new CoreException(
                        new IpsStatus(
                                "Couldn't fix the sort order of the test attribute values, because there is a mismatch between test case and its corresponding type!")); //$NON-NLS-1$
            }
            newTestAttrValueList.add(testAttrValue);
        }
        testAttributeValues = newTestAttrValueList;
        valueChanged(false, true);
    }

    /**
     * Returns all test policy cmpt relations.<br>
     * Packageprivate to enable testing only.
     */
    protected ITestPolicyCmptRelation[] getPolicyCmptRelation(){
        return (ITestPolicyCmptRelation[]) testPolicyCmptRelations.toArray(new ITestPolicyCmptRelation[testPolicyCmptRelations.size()]);
    }
    
	/**
	 * {@inheritDoc}
	 */
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);
		// validate if the test case type param exists
		ITestPolicyCmptTypeParameter param = findTestPolicyCmptTypeParameter(ipsProject);
        IProductCmpt productCmptObj = findProductCmpt(ipsProject);
        
		if (param == null){
			String text = NLS.bind(Messages.TestPolicyCmpt_ValidationError_TestCaseTypeParamNotFound, testPolicyCmptType);
			Message msg = new Message(MSGCODE_TEST_CASE_TYPE_PARAM_NOT_FOUND, text, Message.ERROR, this, PROPERTY_POLICYCMPTTYPE); //$NON-NLS-1$
			list.add(msg);
		} else {
			// check if the param defines the requirement for a product component but not product component is specified
			if (param.isRequiresProductCmpt() && StringUtils.isEmpty(getProductCmpt()) ){
				String text = Messages.TestPolicyCmpt_ValidationError_ProductCmptRequired;
				Message msg = new Message(MSGCODE_PRODUCT_CMPT_IS_REQUIRED, text, Message.ERROR, this, PROPERTY_PRODUCTCMPT); //$NON-NLS-1$
				list.add(msg);
			}
			// check if the policy component type exists
			if (param.findPolicyCmptType(ipsProject) == null){
			    String text = NLS.bind(Messages.TestPolicyCmpt_ValidationWarning_PolicyCmptNotExists, param.getPolicyCmptType(), testPolicyCmptType);
			    Message msg = new Message(ITestPolicyCmptTypeParameter.MSGCODE_POLICY_CMPT_TYPE_NOT_EXISTS, text, Message.WARNING, this, PROPERTY_PRODUCTCMPT); //$NON-NLS-1$
			    list.add(msg);
			}
		}
   
		// check if the product component exists
		if (StringUtils.isNotEmpty(productCmpt) &&  productCmptObj == null){
            String text = NLS.bind(Messages.TestPolicyCmpt_ValidationWarning_ProductComponentNotExists, productCmpt, testPolicyCmptType);
		    Message msg = new Message(MSGCODE_PRODUCT_CMPT_NOT_EXISTS, text, Message.ERROR, this, PROPERTY_PRODUCTCMPT); //$NON-NLS-1$
		    list.add(msg);
		}
        
        // check if a product component is not required but the test policy cmpt defines a product cmpt
        if (param != null && StringUtils.isNotEmpty(productCmpt) && !param.isRequiresProductCmpt()){
            String text = NLS.bind(Messages.TestPolicyCmpt_ValidationError_ProductCmptNotRequiredButIsRelatedToProductCmpt, testPolicyCmptType);
            Message msg = new Message(MSGCODE_PRODUCT_COMPONENT_NOT_REQUIRED, text, Message.ERROR, this, PROPERTY_PRODUCTCMPT); //$NON-NLS-1$
            list.add(msg);
        }
        
        // validate the min and max occurence defined in the test policy component type
        // parameter, get all possible relation defined in the parameter and check the min and may
        // instances
        if (param != null) {
            ITestPolicyCmptTypeParameter[] paramForRelations = param.getTestPolicyCmptTypeParamChilds();
            for (int i = 0; i < paramForRelations.length; i++) {
                int currNumberOfInstances = getTestPolicyCmptRelations(paramForRelations[i].getName()).length;

                // check min and max instances
                int minInstances = paramForRelations[i].getMinInstances();
                int maxInstances = paramForRelations[i].getMaxInstances();
                if (currNumberOfInstances < minInstances) {
                    String text = NLS.bind(Messages.TestPolicyCmptRelation_ValidationError_MinimumNotReached,
                            "" + paramForRelations[i].getMinInstances(), paramForRelations[i].getName()); //$NON-NLS-1$
                    Message msg = new Message(MSGCODE_MIN_INSTANCES_NOT_REACHED, text, Message.ERROR, this,
                            ITestPolicyCmptTypeParameter.PROPERTY_MIN_INSTANCES);
                    list.add(msg);
                }

                if (currNumberOfInstances > maxInstances) {
                    String text = NLS.bind(Messages.TestPolicyCmptRelation_ValidationError_MaximumReached,
                            "" + maxInstances, paramForRelations[i].getName()); //$NON-NLS-1$
                    Message msg = new Message(MSGCODE_MAX_INSTANCES_REACHED, text, Message.ERROR, this,
                            ITestPolicyCmptTypeParameter.PROPERTY_MAX_INSTANCES);
                    list.add(msg);
                }
            }
        }
        
        // check correct product cmpt 
         validateAllowedProductCmpt(list, param, productCmptObj, ipsProject);
	}

    private void validateAllowedProductCmpt(MessageList list, ITestPolicyCmptTypeParameter param, 
            IProductCmpt productCmptObj, IIpsProject ipsProject) throws CoreException {
        // abort validation if no product cmpt was found/or specified
        // or if the parameter wasn't found
        if (param == null || productCmptObj == null){
            return;
        }
        
        if (isRoot()){
            IIpsSrcFile[] allowedProductCmpt = param.getAllowedProductCmpt(getIpsProject(), null);
            if (!isInAllowedProductCmpts(productCmptObj, allowedProductCmpt)) {
                String text = NLS.bind(Messages.TestPolicyCmpt_TestPolicyCmpt_ValidationError_ProductCmpNotAllowedRoot, productCmptObj.getName());
                Message msg = new Message(MSGCODE_WRONG_PRODUCT_CMPT_OF_RELATION, text, Message.ERROR, this,
                        ITestPolicyCmpt.PROPERTY_PRODUCTCMPT);
                list.add(msg);
                return;
            }
        }
        
        // this is a child test policy cmpt, check allowed product depending on parent product cmpt
        ITestPolicyCmpt parentPolicyCmpt = getParentPolicyCmpt();
        if (parentPolicyCmpt == null){
            // no validation possible
            return;
        }
        
        // if parent product cmpt not found, add warning
        // check allowed product cmpt by using parent product cmpt
        ITestPolicyCmptTypeParameter parentParameter = param.getParentTestPolicyCmptTypeParam();
        boolean isParentProductRelevant = true;
        if (parentParameter == null || !parentParameter.isRequiresProductCmpt()){
            // error in test case type parameter structure
            // or parent isn't product relevant
            isParentProductRelevant = false;
        }        

        IProductCmpt productCmptOfParent = parentPolicyCmpt.findProductCmpt(ipsProject);
        if (isParentProductRelevant && productCmptOfParent == null){
            String text = NLS.bind(Messages.TestPolicyCmpt_TestPolicyCmpt_ValidationError_ProductCmpCouldNotValidatedParentNotFound, productCmptObj.getName());
            Message msg = new Message(MSGCODE_PARENT_PRODUCT_CMPT_OF_RELATION_NOT_SPECIFIED, text, Message.WARNING, this,
                    ITestPolicyCmpt.PROPERTY_PRODUCTCMPT);
            list.add(msg);
            return;
        }
        
        // check allowed product cmpts
        IIpsSrcFile[] allowedProductCmpt = param.getAllowedProductCmpt(getIpsProject(), productCmptOfParent);
        if (!isInAllowedProductCmpts(productCmptObj, allowedProductCmpt)) {
            String text = NLS.bind(Messages.TestPolicyCmpt_TestPolicyCmpt_ValidationError_ProductCmpNotAllowed, productCmptObj.getName());
            Message msg = new Message(MSGCODE_WRONG_PRODUCT_CMPT_OF_RELATION, text, Message.ERROR, this,
                    ITestPolicyCmpt.PROPERTY_PRODUCTCMPT);
            list.add(msg);
            return;
        }
    }

    private boolean isInAllowedProductCmpts(IProductCmpt productCmptObj, IIpsSrcFile[] allowedProductCmpt) {
        List list = Arrays.asList(allowedProductCmpt);
        return list.contains(productCmptObj.getIpsSrcFile());
    }

    /**
     * {@inheritDoc}
     */
    public IAttribute findProductCmptTypeAttribute(String attribute, IIpsProject ipsProject) throws CoreException {
        if (StringUtils.isEmpty(getProductCmpt())) {
            // no product cmpt is set, therefore no attribute could be searched, 
            // currently an attributes (from sublcasses) could only be searched if an product cmpt was set
            return null;
        } 
        IProductCmpt productCmptObj = findProductCmpt(ipsProject);
        if (productCmptObj == null){
            return null;
        }
        // TODO v2 - Joerg: search attribute using product cmpt type
        IPolicyCmptType pct = productCmptObj.findPolicyCmptType(ipsProject);
        if (pct==null){
            return null;
        }
        return pct.findPolicyCmptTypeAttribute(attribute, ipsProject);
    }
}
