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
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.model.testcasetype.TestParameterType;
import org.faktorips.devtools.core.util.ListElementMover;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
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
    public String getDatatype() {
        return getPolicyCmptType();
    }

    /**
     * {@inheritDoc}
     */
    public void setDatatype(String datatype) {
        setPolicyCmptType(datatype);
    }

    /**
     * {@inheritDoc}
     */
    public void setTestParameterType(TestParameterType testParameterType) {
        ArgumentCheck.isTrue(testParameterType.equals(TestParameterType.INPUT)
                || testParameterType.equals(TestParameterType.EXPECTED_RESULT)
                || testParameterType.equals(TestParameterType.COMBINED));
        TestParameterType oldType = this.type;
        this.type = testParameterType;
        valueChanged(oldType, testParameterType);
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
		String oldPolicyCmptType = this.policyCmptType;
        this.policyCmptType = policyCmptType;
        valueChanged(oldPolicyCmptType, policyCmptType);
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
        if (isRoot()){
            return null;
        }
        
        // this is a child parameter therfore a relation should exists
        ITestPolicyCmptTypeParameter parent = (ITestPolicyCmptTypeParameter) getParent();
        IPolicyCmptType pcType = parent.findPolicyCmptType();
        
        while (pcType != null){
            IRelation[] relations = pcType.getRelations();
            for (int i = 0; i < relations.length; i++) {
                if (relations[i].getName().equals(relation)) {
                    return relations[i];
                }
            }
            pcType = pcType.findSupertype();
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
     * If no relation is specified then return the policy cmpt type image or if a product cmpt is required the
     * the product cmpt image. If a relation is specified then return the image which is provided by the relation
     * or if the relation is not found the default "relation.gif" image.
     * 
     * {@inheritDoc}
     */
    public Image getImage() {
        if (StringUtils.isEmpty(relation))
            if (requiresProductCmpt)
                return IpsObjectType.PRODUCT_CMPT.getImage();
            else
                return IpsObjectType.POLICY_CMPT_TYPE.getImage();
        
        if (!isRoot()) {
            try {
                IRelation relation = findRelation();
                if (relation != null)
                    return relation.getImage();
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
        }
        return IpsPlugin.getDefault().getImage("Relation.gif"); //$NON-NLS-1$
    }

    /**
	 * {@inheritDoc}
	 */
	public ITestAttribute newInputTestAttribute() {
		TestAttribute a = newTestAttributeInternal(getNextPartId());
		a.setTestAttributeType(TestParameterType.INPUT);
        objectHasChanged();
		return a;
	}

	/**
	 * {@inheritDoc}
	 */
	public ITestAttribute newExpectedResultTestAttribute() {
	    TestAttribute a = newTestAttributeInternal(getNextPartId());
	    a.setTestAttributeType(TestParameterType.EXPECTED_RESULT);
	    objectHasChanged();
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
		objectHasChanged();
		return p;
	}

	/**
	 * {@inheritDoc}
	 */
	public ITestAttribute getTestAttribute(String attributeName) {
		for (Iterator it = testAttributes.iterator(); it.hasNext();) {
			ITestAttribute a = (ITestAttribute) it.next();
			if (a.getName().equals(attributeName)) {
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

    /**
     * {@inheritDoc}
     */
	public void removeTestPolicyCmptTypeParamChild(TestPolicyCmptTypeParameter testPolicyCmptTypeParamChildName) {
		testPolicyCmptTypeChilds.remove(testPolicyCmptTypeParamChildName);
	}

	/**
     * {@inheritDoc}
	 */
	public void delete() {
        if (!isRoot())
            ((ITestPolicyCmptTypeParameter)getParent()).removeTestPolicyCmptTypeParamChild(this);
            
        super.delete();
    }

    /**
	 * {@inheritDoc}
	 */	
	public ITestParameter getRootParameter() {
        ITestParameter current = this;
		while (!current.isRoot()){
			current = (ITestParameter) current.getParent();
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

    /**
     * {@inheritDoc}
     */
    public boolean isRoot() {
        return (! (getParent() instanceof TestPolicyCmptTypeParameter)); 
    }
    
    /**
     * {@inheritDoc}
     */
    public int[] moveTestAttributes(int[] indexes, boolean up) {
        ListElementMover mover = new ListElementMover(testAttributes);
        int[] newIdxs = mover.move(indexes, up);
        valueChanged(indexes, newIdxs);
        return newIdxs;
    }    
    
    /**
     * {@inheritDoc}
     */
    public int[] moveTestPolicyCmptTypeChild(int[] indexes, boolean up){
        ListElementMover mover = new ListElementMover(testPolicyCmptTypeChilds);
        int[] newIdxs = mover.move(indexes, up);
        valueChanged(indexes, newIdxs);
        return newIdxs;
    }
    
    /**
     * {@inheritDoc}
     */
    protected void validateThis(MessageList list) throws CoreException {
        super.validateThis(list);
        
        // check if the policy component type exists
        IPolicyCmptType policyCmptTypeFound = findPolicyCmptType();
        if (policyCmptTypeFound == null) {
            String text = NLS.bind(Messages.TestPolicyCmptTypeParameter_ValidationError_PolicyCmptTypeNotExists, policyCmptType);
            Message msg = new Message(MSGCODE_POLICY_CMPT_TYPE_NOT_EXISTS, text, Message.ERROR, this,
                    PROPERTY_POLICYCMPTTYPE); //$NON-NLS-1$
            list.add(msg);
        }

        // check min and max instances
        if (minInstances > maxInstances) {
            String text = NLS.bind(Messages.TestPolicyCmptTypeParameter_ValidationError_MinGreaterThanMax, "" + minInstances, "" + maxInstances);  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
            Message msg = new Message(MSGCODE_MIN_INSTANCES_IS_GREATER_THAN_MAX, text, Message.ERROR, this,
                    PROPERTY_MIN_INSTANCES); //$NON-NLS-1$
            list.add(msg);
        }
         if (maxInstances < minInstances) {
            String text = NLS.bind(Messages.TestPolicyCmptTypeParameter_ValidationError_MaxLessThanMin, "" + minInstances, "" + maxInstances);  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
            Message msg = new Message(MSGCODE_MAX_INSTANCES_IS_LESS_THAN_MIN, text, Message.ERROR, this,
                    PROPERTY_MAX_INSTANCES); //$NON-NLS-1$
            list.add(msg);
        }
        
        // check if the type of the parameter matches the type of the parent
        if (!isRoot()){
            TestParameterType parentType = ((ITestPolicyCmptTypeParameter)getParent()).getTestParameterType();
            if (!TestParameterType.isChildTypeMatching(type, parentType)) {
                String text = NLS.bind(Messages.TestPolicyCmptTypeParameter_ValidationError_TypeNotAllowed, type.getName(), parentType
                        .getName());
                Message msg = new Message(MSGCODE_TYPE_DOES_NOT_MATCH_PARENT_TYPE, text, Message.ERROR, this,
                        PROPERTY_TEST_PARAMETER_TYPE);
                list.add(msg);
            }
        }
        
        // check if the relation exists
        //  if the parameter is root, no relation is defined
        if (! isRoot()){
            IRelation relationFound = findRelation();
            if (relationFound == null) {
                String text = NLS.bind(Messages.TestPolicyCmptTypeParameter_ValidationError_RelationNotExists, relation);
                Message msg = new Message(MSGCODE_RELATION_NOT_EXISTS, text, Message.ERROR, this,
                        PROPERTY_RELATION); //$NON-NLS-1$
                list.add(msg);
            } else if (policyCmptTypeFound != null){
                // check if the relation is specified and the policy component type exists
                //   that the policy cmpt type is a possible target of the relation  
                IPolicyCmptType targetOfRelation = relationFound.findTarget();
                if (targetOfRelation == null){
                    String text = NLS.bind(Messages.TestPolicyCmptTypeParameter_ValidationError_TargetOfRelationNotExists, relationFound.getTarget(), relation);
                    Message msg = new Message(MSGCODE_TARGET_OF_RELATION_NOT_EXISTS, text, Message.WARNING, this,
                            PROPERTY_RELATION); //$NON-NLS-1$
                    list.add(msg);
                }else{
                    if (!policyCmptTypeFound.isSubtypeOrSameType(targetOfRelation)){
                        String text = NLS.bind(Messages.TestPolicyCmptTypeParameter_ValidationError_PolicyCmptNotAllowedForRelation, policyCmptType, relation);
                        Message msg = new Message(MSGCODE_WRONG_POLICY_CMPT_TYPE_OF_RELATION, text, Message.ERROR, this,
                                PROPERTY_POLICYCMPTTYPE); //$NON-NLS-1$
                        list.add(msg);
                    }
                }
            }
        } // check relation end
        
        // check if this is a root parameter and the related policy cmpt is abstract, that the required product cmpt flag
        // is true, otherwise it is not possible to select a derived class of the abstract policy cmpt.
        // for none root parameters this check is not necessary, because in this case a dialog will be displayed to select
        // the target of a relation (childs are always defined by using a relation)
        if (isRoot() && policyCmptTypeFound != null){
            if (!isRequiresProductCmpt() && policyCmptTypeFound.isAbstract()){
                String text = NLS.bind(Messages.TestPolicyCmptTypeParameter_ValidationError_MustRequireProdCmptIfRootAndAbstract, policyCmptType);
                Message msg = new Message(MSGCODE_MUST_REQUIRE_PROD_IF_ROOT_AND_ABSTRACT, text, Message.ERROR, this,
                        PROPERTY_REQUIRES_PRODUCTCMT); //$NON-NLS-1$
                list.add(msg);
            }
        }
        
        // check if the requires product flag is only true if the related test policy cmpt is configurable by product cmpt type
        if (requiresProductCmpt && ! policyCmptTypeFound.isConfigurableByProductCmptType() && policyCmptTypeFound != null){
            String text = NLS.bind(Messages.TestPolicyCmptTypeParameter_ValidationError_FlagRequiresIsTrueButPolicyCmptTypeIsNotConfByProduct, policyCmptType);
            Message msg = new Message(MSGCODE_REQUIRES_PROD_BUT_POLICY_CMPT_TYPE_IS_NOT_CONF_BY_PROD, text, Message.ERROR, this,
                    PROPERTY_REQUIRES_PRODUCTCMT); //$NON-NLS-1$
            list.add(msg);
        }
    }
}