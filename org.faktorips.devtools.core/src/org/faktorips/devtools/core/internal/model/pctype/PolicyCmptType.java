package org.faktorips.devtools.core.internal.model.pctype;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.faktorips.devtools.core.internal.model.IpsObject;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.QualifiedNameType;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IMethod;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.pctype.ITypeHierarchy;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.pctype.Modifier;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.util.ListElementMover;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;


/**
 * Implementation of IPolicyCmptType.
 * 
 * @author Jan Ortmann
 */
public class PolicyCmptType extends IpsObject implements IPolicyCmptType {
    
	private boolean configurableByProductCmptType = false;
	private String unqalifiedProductCmptType = "";
    private String supertype = "";
    private boolean abstractFlag = false;
    private boolean forceExtensionCompilationUnitGeneration = false;
    private List attributes = new ArrayList(0);
    private List methods = new ArrayList(0);
    private List relations = new ArrayList(0);
    private List rules = new ArrayList(0);

    public PolicyCmptType(IIpsSrcFile file) {
        super(file);
    }
    
    /**
     * Constructor for testing purposes.
     */
    PolicyCmptType() {
    }
    
    /**
     * Overridden.
     */
    public String getProductCmptType() {
		return getIpsPackageFragment().getName() + '.' + unqalifiedProductCmptType;
	}
    
	/**
	 * Overridden.
	 */
	public boolean isConfigurableByProductCmptType() {
		return configurableByProductCmptType;
	}

	/**
	 * Overridden.
	 */
	public void setConfigurableByProductCmptType(boolean newValue) {
		boolean oldValue = configurableByProductCmptType;
		configurableByProductCmptType = newValue;
		valueChanged(oldValue, newValue);
	}

	/**
	 * Overridden.
	 */
	public IProductCmptType findProductCmptType() throws CoreException {
		if (!configurableByProductCmptType) {
			return null;
		}
		if (StringUtils.isEmpty(unqalifiedProductCmptType)) {
			return null;
		}
		return new ProductCmptType(this);
	}

	/**
	 * Overridden.
	 */
	public String getUnqualifiedProductCmptType() {
		return unqalifiedProductCmptType;
	}

	/**
	 * Overridden.
	 */
	public void setUnqualifiedProductCmptType(String newUnqualifiedName) {
		ArgumentCheck.notNull(newUnqualifiedName);
        String oldName = unqalifiedProductCmptType;
        unqalifiedProductCmptType = newUnqualifiedName;
        valueChanged(oldName, newUnqualifiedName);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public IIpsObjectPart newPart(Class partType) {
		if (partType.equals(IAttribute.class)) {
			return newAttribute();
		}
		else if (partType.equals(IMethod.class)) {
			return newMethod();
		}
		else if (partType.equals(IRelation.class)) {
			return newRelation();
		}
		else if (partType.equals(IValidationRule.class)) {
			return newRule();
		}
		throw new IllegalArgumentException("Unknown part type" + partType);
	}

    /** 
     * {@inheritDoc}
     */
    public String getSupertype() {
        return supertype;
    }

    /**
     * {@inheritDoc}
     */
    public IPolicyCmptType findSupertype() throws CoreException {
        if (StringUtils.isEmpty(supertype)) {
            return null;
        }
        return getIpsProject().findPolicyCmptType(supertype);
    }
    
    /** 
     * Overridden.
     */
    public void setSupertype(String newSupertype) {
        String oldSupertype = supertype;
        supertype = newSupertype;
        valueChanged(oldSupertype, newSupertype);
    }
    
    /**
     * Overridden.
     */
    public boolean isForceExtensionCompilationUnitGeneration() {
        return forceExtensionCompilationUnitGeneration;
    }

    /**
     * Overridden.
     */
    public void setForceExtensionCompilationUnitGeneration(boolean flag) {
        boolean oldValue = forceExtensionCompilationUnitGeneration;
        forceExtensionCompilationUnitGeneration = flag;
        valueChanged(oldValue, forceExtensionCompilationUnitGeneration);
    }

    /**
     * Overridden.
     */
    public boolean isExtensionCompilationUnitGenerated() {
        if (forceExtensionCompilationUnitGeneration) {
            return true;
        }
        if (getNumOfRules()>0) {
            return true;
        }
        for (Iterator it=methods.iterator(); it.hasNext(); ) {
            IMethod method = (IMethod)it.next();
            if (!method.isAbstract()) {
                return true;
            }
        }
        for (Iterator it=attributes.iterator(); it.hasNext(); ) {
            IAttribute attribute = (IAttribute)it.next();
            if (attribute.getAttributeType()==AttributeType.COMPUTED) {
                if (!attribute.isProductRelevant()) {
                    return true;
                }
            } else if(attribute.getAttributeType()==AttributeType.DERIVED) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Overridden.
     */
    public IIpsElement[] getChildren() {
        int numOfChildren = getNumOfAttributes()
        	+ getNumOfMethods()
        	+ getNumOfRelations()
        	+ getNumOfRules();
        IIpsElement[] childrenArray = new IIpsElement[numOfChildren];
        List childrenList = new ArrayList(numOfChildren);
        childrenList.addAll(attributes);
        childrenList.addAll(methods);
        childrenList.addAll(relations);
        childrenList.addAll(rules);
        childrenList.toArray(childrenArray);
        return childrenArray;
    }

    /** 
     * Overridden.
     */
    public IAttribute[] getAttributes() {
        IAttribute[] a = new IAttribute[attributes.size()];
        attributes.toArray(a);
        return a;
    }
    
    /**
     * Overridden.
     */
    public IAttribute getAttribute(String name) {
        for (Iterator it=attributes.iterator(); it.hasNext();) {
            IAttribute a = (IAttribute)it.next();
            if (a.getName().equals(name)) {
                return a;
            }
        }
        return null;
    }
    
    /**
     * Overridden.
     */
    public int getNumOfAttributes() {
        return attributes.size();
    }
    
    /**
     * Overridden.
     */
    public IAttribute newAttribute() {
        Attribute a = newAttributeInternal(getNextPartId());
        updateSrcFile();
        return a;
    }
    
    /**
     * Overridden.
     */
    public int[] moveAttributes(int[] indexes, boolean up) {
        ListElementMover mover = new ListElementMover(attributes);
        return mover.move(indexes, up);
    }
    
    /**
     * Returns the list holding the attributes as a reference. 
     */
    List getAttributeList() {
        return attributes;
    }
    
    /*
     * Creates a new attribute without updating the src file.
     */
    private Attribute newAttributeInternal(int id) {
        Attribute a = new Attribute(this, id);
        attributes.add(a);
        return a;
    }
    
    /**
     * Removes the attribute from the type. 
     */
    void removeAttribute(Attribute attribute) {
        attributes.remove(attribute);
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IPolicyCmptType#getMethods()
     */
    public IMethod[] getMethods() {
        IMethod[] m = new IMethod[methods.size()];
        methods.toArray(m);
        return m;
    }
    
    /**
     * Returns the list holding the methods as a reference. 
     */
    List getMethodList() {
        return methods;
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IPolicyCmptType#newMethod()
     */
    public IMethod newMethod() {
        IMethod m = newMethodInternal(getNextPartId());
        updateSrcFile();
        return m;
    }

    /*
     * Creates a new attribute without updating the src file.
     */
    private Method newMethodInternal(int id) {
        Method m = new Method(this, id);
        methods.add(m);
        return m;
    }
    
    /** 
     * Overridden.
     */
    public int getNumOfMethods() {
        return methods.size();
    }

    /**
     * Overridden.
     */
    public int[] moveMethods(int[] indexes, boolean up) {
        ListElementMover mover = new ListElementMover(methods);
        return mover.move(indexes, up);
    }
    
    /**
     * Removes the method from the type. 
     */
    void removeMethod(Method method) {
        methods.remove(method);
    }
    
    /** 
     * Overridden.
     */
    public IRelation[] getRelations() {
        IRelation[] r = new IRelation[relations.size()];
        relations.toArray(r);
        return r;
    }
    
    /**
     * Overridden.
     */
    public IRelation getRelation(String name) {
        ArgumentCheck.notNull(name);
        for (Iterator it=relations.iterator(); it.hasNext();) {
            IRelation each = (IRelation)it.next();
            if (name.equals(each.getName())) {
                return each;
            }
        }
        return null;
    }
    
    /**
     * Overridden.
     */
    //TODO test case
    public IRelation[] getProductRelevantRelations(){
        ArrayList productRelevantRelations = new ArrayList();
        for (Iterator it = relations.iterator(); it.hasNext();) {
            IRelation relation = (IRelation)it.next();
            if(relation.isProductRelevant()){
                productRelevantRelations.add(relation);
            }
        }
        return (IRelation[])productRelevantRelations.toArray(new IRelation[productRelevantRelations.size()]);
    }
    
    /**
     * {@inheritDoc}
     */
    public int getNumOfRelations() {
        return relations.size();
    }
    
    /**
     * {@inheritDoc}
     */
    public int[] moveRelations(int[] indexes, boolean up) {
        ListElementMover mover = new ListElementMover(relations);
        return mover.move(indexes, up);
    }
    
    /**
     * {@inheritDoc}
     */
    public IRelation newRelation() {
        Relation r = newRelationInternal(getNextPartId());
        updateSrcFile();
        return r;
    }
    
    /*
     * Creates a new attribute without updating the src file.
     */
    private Relation newRelationInternal(int id) {
        Relation r = new Relation(this, id);
        relations.add(r);
        return r;
    }
    
    /**
     * Removes the attribute from the type. 
     */
    void removeRelation(Relation relation) {
        relations.remove(relation);
    }
    
    /**
     * {@inheritDoc}
     */
    public IValidationRule[] getRules() {
        IValidationRule[] r = new IValidationRule[rules.size()];
        rules.toArray(r);
        return r;
    }

    /**
     * {@inheritDoc}
     */
    public IValidationRule newRule() {
        IValidationRule r = newRuleInternal(getNextPartId());
        updateSrcFile();
        return r;
    }

    /*
     * Creates a new rule without updating the src file.
     */
    private IValidationRule newRuleInternal(int id) {
        ValidationRule r = new ValidationRule(this, id);
        rules.add(r);
        return r;
    }
    
    /** 
     * {@inheritDoc}
     */
    public int getNumOfRules() {
        return rules.size();
    }
    
    /**
     * {@inheritDoc}
     */
    public int[] moveRules(int[] indexes, boolean up) {
        ListElementMover mover = new ListElementMover(rules);
        return mover.move(indexes, up);
    }
    
    /**
     * Removes the method from the type. 
     */
    void removeRule(ValidationRule rule) {
        rules.remove(rule);
    }
    
    /** 
     * Overridden.
     */
    public IpsObjectType getIpsObjectType() {
        return IpsObjectType.POLICY_CMPT_TYPE;
    }

    /**
     * Overridden.
     */
    protected void initPropertiesFromXml(Element element, int id) {
        super.initPropertiesFromXml(element, id);
        configurableByProductCmptType = Boolean.valueOf(element.getAttribute(PROPERTY_CONFIGURABLE_BY_PRODUCTCMPTTYPE)).booleanValue();
        unqalifiedProductCmptType = element.getAttribute(PROPERTY_UNQUALIFIED_PRODUCT_CMPT_TYPE);
        supertype = element.getAttribute(PROPERTY_SUPERTYPE);
        abstractFlag = Boolean.valueOf(element.getAttribute(PROPERTY_ABSTRACT)).booleanValue();
        forceExtensionCompilationUnitGeneration = Boolean.valueOf(element.getAttribute(PROPERTY_FORCE_GENERATION_OF_EXTENSION_CU)).booleanValue();
    }

    /**
     * Overridden.
     */
    protected void reinitPartCollections() {
        attributes.clear();
        methods.clear();
        rules.clear();
        relations.clear();
    }

    /**
     * Overridden.
     */
    protected void reAddPart(IIpsObjectPart part) {
        if (part instanceof IAttribute) {
            attributes.add(part);
            return;
        } else if (part instanceof IMethod) {
            methods.add(part);
            return;
        } else if (part instanceof IRelation) {
            relations.add(part);
            return;
        } else if (part instanceof IValidationRule) {
            rules.add(part);
            return;
        }
        throw new RuntimeException("Unknown part type" + part.getClass());
    }
    
    /**
     * {@inheritDoc}
     */
    protected IIpsObjectPart newPart(String xmlTagName, int id) {
        if (xmlTagName.equals(Attribute.TAG_NAME)) {
            return newAttributeInternal(id);
        } else if (xmlTagName.equals(Relation.TAG_NAME)) {
            return newRelationInternal(id);
        } else if (xmlTagName.equals(Method.TAG_NAME)) {
            return newMethodInternal(id);
        } else if (xmlTagName.equals(ValidationRule.TAG_NAME)) {
            return newRuleInternal(id);
        }
        throw new RuntimeException("Could not create part for tag name" + xmlTagName);
    }
    
    /**
     * Overridden.
     */
    protected void propertiesToXml(Element newElement) {
        super.propertiesToXml(newElement);
        newElement.setAttribute(PROPERTY_CONFIGURABLE_BY_PRODUCTCMPTTYPE, "" + configurableByProductCmptType);
        newElement.setAttribute(PROPERTY_UNQUALIFIED_PRODUCT_CMPT_TYPE, unqalifiedProductCmptType);
        newElement.setAttribute(PROPERTY_SUPERTYPE, supertype);
        newElement.setAttribute(PROPERTY_ABSTRACT, "" + abstractFlag);
        newElement.setAttribute(PROPERTY_FORCE_GENERATION_OF_EXTENSION_CU, "" + forceExtensionCompilationUnitGeneration);
    }

    /**
     * Overridden.
     */
    protected void validateThis(MessageList list) throws CoreException {
        IPolicyCmptType supertypeObj = null;
        if (!supertype.equals("")) {
            supertypeObj = (IPolicyCmptType)getIpsProject().findIpsObject(IpsObjectType.POLICY_CMPT_TYPE, supertype);
            if (supertypeObj==null) {
                String text = "The supertype " + supertype + " does not exists!";
                list.add(new Message("", text, Message.ERROR, this, IPolicyCmptType.PROPERTY_SUPERTYPE));
            }
        }
        if (isConfigurableByProductCmptType() && StringUtils.isEmpty(this.unqalifiedProductCmptType)) {
            String text = "The product component type name is missing.";
            list.add(new Message("", text, Message.ERROR, this, IPolicyCmptType.PROPERTY_UNQUALIFIED_PRODUCT_CMPT_TYPE));
        }
        if (!isAbstract()) {
            validateIfAllAbstractMethodsAreImplemented(list);
            IMethod[] methods = getMethods();
            for (int i=0; i<methods.length; i++) {
                if (methods[i].isAbstract()) {
                    String text = "Only an abstract type can have abstract methods.";
                    list.add(new Message("", text, Message.ERROR, this, IPolicyCmptType.PROPERTY_ABSTRACT));
                }
            }
        }
    }
    
    private void validateIfAllAbstractMethodsAreImplemented(MessageList list) throws CoreException {
        ITypeHierarchy hierarchy = getSupertypeHierarchy();
        if (hierarchy.getSupertype(this)==null) {
            return;
        }
        IMethod[] methods = hierarchy.getAllMethods(hierarchy.getSupertype(this));
        for (int i=0; i<methods.length; i++) {
            if (methods[i].isAbstract()) {
                if (!isAbstractMethodImplemented(this, methods[i], hierarchy)) {
                    String text = "Must override the abstract method " + methods[i].getName() + " defined in " 
                		+ methods[i].getPolicyCmptType().getQualifiedName();
                    list.add(new Message(IPolicyCmptType.MSGCODE_MUST_OVERRIDE_ABSTRACT_METHOD, text, Message.ERROR, this));
                }
            }
        }
    }
    
    /**
     * Returns true if the method is implemented in the indicated type or one of the type's supertypes.
     * 
     * @param pcType The policy component type where the search for the implementation starts.
     * @param method An abstract method of one the type's supertypes.
     * @param hierarchy The supertype hierarchy where the supertype relation is already resolved.
     */
    private boolean isAbstractMethodImplemented(IPolicyCmptType pcType, IMethod method, ITypeHierarchy hierarchy) {
        IMethod match = pcType.getMatchingMethod(method);
        if (match!=null && !match.isAbstract()) {
            return true;
        }
        IPolicyCmptType supertype = hierarchy.getSupertype(pcType);
        if (supertype==null) {
            return false;
        }
        return isAbstractMethodImplemented(supertype, method, hierarchy);
    }
    
    /** 
     * Overridden.
     */
    public boolean isAbstract() {
        return abstractFlag;
    }

    /** 
     * Overridden.
     */
    public void setAbstract(boolean newValue) {
        boolean oldValue = abstractFlag;
        abstractFlag = newValue;
        valueChanged(oldValue, newValue);
    }

    /** 
     * Overridden.
     */
    public ITypeHierarchy getSupertypeHierarchy() throws CoreException {
        return TypeHierarchy.getSupertypeHierarchy(this);
    }
    
    /**
     * Overridden.
     */
    public ITypeHierarchy getSubtypeHierarchy() throws CoreException {
        return TypeHierarchy.getSubtypeHierarchie(this);
    }
    
    /** 
     * Overridden.
     */
    public IMethod[] findOverrideCandidates(boolean onlyAbstractMethods) throws CoreException {
        List candidates = new ArrayList();
        ITypeHierarchy hierarchy = getSupertypeHierarchy();
        IPolicyCmptType[] supertypes = hierarchy.getAllSupertypes(this);
        for (int i=0; i<supertypes.length; i++) {
            getOverrideCandidates(supertypes[i], onlyAbstractMethods, candidates);
        }
        return (IMethod[])candidates.toArray(new IMethod[candidates.size()]);
    }
    
    /**
     * Overridden.
     */
    public QualifiedNameType[] dependsOn() throws CoreException {
        ArrayList qualifiedNameTypes = new ArrayList();
        qualifiedNameTypes.add(new QualifiedNameType(getSupertype(), IpsObjectType.POLICY_CMPT_TYPE));
        addQualifiedNameTypesForRelationTargets(this, qualifiedNameTypes);
        return (QualifiedNameType[])qualifiedNameTypes.toArray(new QualifiedNameType[qualifiedNameTypes.size()]);
    }

    private void addQualifiedNameTypesForRelationTargets(IPolicyCmptType policyCmptType, List qualifiedNameTypes){
        IRelation[] relations = policyCmptType.getRelations();
        for (int i = 0; i < relations.length; i++) {
            String qualifiedName = relations[i].getTarget();
            qualifiedNameTypes.add(new QualifiedNameType(qualifiedName, IpsObjectType.POLICY_CMPT_TYPE));
        }
    }
    
    /*
     * helper method for getOverrideCandidates
     */
    private void getOverrideCandidates(IPolicyCmptType type, boolean onlyAbstractMethods, List candidates) {
        IMethod[] supertypeMethods = type.getMethods();
        for (int i=0; i<supertypeMethods.length; i++) {
            if (supertypeMethods[i].getModifier()!=Modifier.PRIVATE 
                && !hasSameMethod(supertypeMethods[i])) {
                if (!onlyAbstractMethods || supertypeMethods[i].isAbstract()) {
                    // candidate found, but it might be already in the list
                    if (!sameMethodAlreadyInCandidateList(supertypeMethods[i], candidates)) {
                        candidates.add(supertypeMethods[i]);    
                    }
                }
            }
        }
    }
    
    /*
     * helper method for getOverrideCandidates
     */
    private boolean sameMethodAlreadyInCandidateList(IMethod method, List candidates) {
        for (Iterator it=candidates.iterator(); it.hasNext(); ) {
            IMethod candidate = (IMethod)it.next();
            if (method.isSame(candidate)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Overridden.
     */
    public boolean hasSameMethod(IMethod method) {
        return getMatchingMethod(method)!=null;
    }
    
    /**
     * Overridden.
     */
    public IMethod getMatchingMethod(IMethod method) {
        for (Iterator it=this.methods.iterator(); it.hasNext(); ) {
            IMethod thisMethod = (IMethod)it.next();
            if (thisMethod.isSame(method)) {
                return thisMethod;
            }
        }
        return null;
    }
    
    /** 
     * Overridden.
     */
    public IMethod[] override(IMethod[] methods) {
        IMethod[] newMethods = new IMethod[methods.length];
        for (int i=0; i<methods.length; i++) {
            IMethod override = newMethod();
            override.setModifier(methods[i].getModifier());
            override.setAbstract(false);
            override.setDatatype(methods[i].getDatatype());
            override.setName(methods[i].getName());
            override.setParameters(methods[i].getParameters());
            newMethods[i] = override;
        }
        return newMethods;
    }
    
    /** 
     * Overridden.
     */
    public boolean isVoid() {
        return false;
    }

    /** 
     * Overridden.
     */
    public boolean isPrimitive() {
        return false;
    }

    /** 
     * Overridden.
     */
    public boolean isValueDatatype() {
        return false;
    }

    /** 
     * Overridden.
     */
    public int compareTo(Object o) {
        return 0;
    }

	/**
	 * {@inheritDoc}
	 */
	public IType getJavaImplementationType() throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getJavaClassName() {
		throw new RuntimeException("getJavaClassName is not supported by " + getClass());
	}
    
    
}