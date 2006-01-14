package org.faktorips.devtools.core.internal.model.pctype;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.faktorips.devtools.core.internal.model.IpsObject;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.QualifiedNameType;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IMethod;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.pctype.ITypeHierarchy;
import org.faktorips.devtools.core.model.pctype.IValidationRuleDef;
import org.faktorips.devtools.core.model.pctype.Modifier;
import org.faktorips.devtools.core.util.ListElementMover;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;


/**
 * Implementation of IPolicyCmptType.
 * 
 * @author Ortmann
 */
public class PolicyCmptType extends IpsObject implements IPolicyCmptType {
    
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
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsObject#getJavaType(int)
     */
    public IType getJavaType(int kind) throws CoreException {
        return getJavaType(getIpsPackageFragment(), getName(), kind); 
    }
    
    /**
     * Returns the Java type that correspond to the IPS object identified by
     * the IPS package fragmeent and the given name and the given kind.
     * 
     * @throws CoreException if the type can't be determined.
     */
    public final static IType getJavaType(
            IIpsPackageFragment ipsPack, 
            String policyCmptTypeName, 
            int kind) throws CoreException {
        IPackageFragment pack;
        String javaTypeName;
	    ICompilationUnit cu;
        switch (kind) {
        	case JAVA_POLICY_CMPT_IMPLEMENTATION_TYPE:
                pack = ipsPack.getJavaPackageFragment(IIpsPackageFragment.JAVA_PACK_IMPLEMENTATION);
                javaTypeName = StringUtils.capitalise(policyCmptTypeName) + "Impl";
        	    cu = pack.getCompilationUnit(javaTypeName + ".java");
        	    return cu.getType(javaTypeName);
        	case JAVA_POLICY_CMPT_EXTENSTION_IMPLEMENTATION_TYPE:
                pack = ipsPack.getJavaPackageFragment(IIpsPackageFragment.JAVA_PACK_EXTENSION);
                javaTypeName = StringUtils.capitalise(policyCmptTypeName) + "ExtImpl";
        	    cu = pack.getCompilationUnit(javaTypeName + ".java");
        	    return cu.getType(javaTypeName);
        	case JAVA_POLICY_CMPT_PUBLISHED_INTERFACE_TYPE:
                pack = ipsPack.getJavaPackageFragment(IIpsPackageFragment.JAVA_PACK_PUBLISHED_INTERFACE);
                javaTypeName = StringUtils.capitalise(policyCmptTypeName);
        	    cu = pack.getCompilationUnit(javaTypeName + ".java");
        	    return cu.getType(javaTypeName);
        	case JAVA_PRODUCT_CMPT_IMPLEMENTATION_TYPE:
                pack = ipsPack.getJavaPackageFragment(IIpsPackageFragment.JAVA_PACK_IMPLEMENTATION);
                javaTypeName = StringUtils.capitalise(policyCmptTypeName) + "PkImpl";
        	    cu = pack.getCompilationUnit(javaTypeName + ".java");
        	    return cu.getType(javaTypeName);
        	case JAVA_PRODUCT_CMPT_PUBLISHED_INTERFACE_TYPE:
                pack = ipsPack.getJavaPackageFragment(IIpsPackageFragment.JAVA_PACK_PUBLISHED_INTERFACE);
                javaTypeName = StringUtils.capitalise(policyCmptTypeName) + "Pk";
        	    cu = pack.getCompilationUnit(javaTypeName + ".java");
        	    return cu.getType(javaTypeName);
        	default:
        	    throw new IllegalArgumentException("Unkown kind " + kind);
        }
    }
    
    /** 
     * Overridden method.
     * @throws CoreException
     * @see org.faktorips.devtools.core.model.IIpsObject#getAllJavaTypes()
     */
    public IType[] getAllJavaTypes() throws CoreException {
        return getAllJavaTypes(getIpsPackageFragment(), getName());
    }
    
    /**
     * Returns all Java types that correspond to IPS object identified by
     * the package fragment and the name.
     *  
     * Note that none of the returned Java types must exist.
     * @throws CoreException
     */
    public final static IType[] getAllJavaTypes(
            IIpsPackageFragment ipsPack, 
            String policyCmptTypeName) throws CoreException {
        
        IType[] types = new IType[4];
        types[0] = getJavaType(ipsPack, policyCmptTypeName, JAVA_POLICY_CMPT_IMPLEMENTATION_TYPE);
        types[1] = getJavaType(ipsPack, policyCmptTypeName, JAVA_POLICY_CMPT_PUBLISHED_INTERFACE_TYPE);
        types[2] = getJavaType(ipsPack, policyCmptTypeName, JAVA_PRODUCT_CMPT_IMPLEMENTATION_TYPE);
        types[3] = getJavaType(ipsPack, policyCmptTypeName, JAVA_PRODUCT_CMPT_PUBLISHED_INTERFACE_TYPE);
        return types;
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IPolicyCmptType#getSupertype()
     */
    public String getSupertype() {
        return supertype;
    }

    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.model.pctype.IPolicyCmptType#findSupertype()
     */
    public IPolicyCmptType findSupertype() throws CoreException {
        if (StringUtils.isEmpty(supertype)) {
            return null;
        }
        return getIpsProject().findPolicyCmptType(supertype);
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IPolicyCmptType#setSupertype(java.lang.String)
     */
    public void setSupertype(String newSupertype) {
        String oldSupertype = supertype;
        supertype = newSupertype;
        valueChanged(oldSupertype, newSupertype);
    }
    
    /**
     * Overridden IMethod.
     * @see org.faktorips.devtools.core.model.pctype.IPolicyCmptType#isForceExtensionCompilationUnitGeneration()
     */
    public boolean isForceExtensionCompilationUnitGeneration() {
        return forceExtensionCompilationUnitGeneration;
    }

    /**
     * Overridden IMethod.
     * @see org.faktorips.devtools.core.model.pctype.IPolicyCmptType#setForceExtensionCompilationUnitGeneration(boolean)
     */
    public void setForceExtensionCompilationUnitGeneration(boolean flag) {
        boolean oldValue = forceExtensionCompilationUnitGeneration;
        forceExtensionCompilationUnitGeneration = flag;
        valueChanged(oldValue, forceExtensionCompilationUnitGeneration);
    }

    /**
     * Overridden IMethod.
     * @throws CoreException
     * @see org.faktorips.devtools.core.model.pctype.IPolicyCmptType#isExtensionCompilationUnitGenerated()
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
     * Overridden IMethod.
     * 
     * @see org.faktorips.devtools.core.model.pctype.IPolicyCmptType#getJavaImplementationType()
     */
    public IType getJavaImplementationType() throws CoreException {
        if (isExtensionCompilationUnitGenerated()) {
            return getJavaType(JAVA_POLICY_CMPT_EXTENSTION_IMPLEMENTATION_TYPE);
        }
        return getJavaType(JAVA_POLICY_CMPT_IMPLEMENTATION_TYPE);
    }
    
    /**
     * Overridden IMethod.
     * 
     * @see org.faktorips.devtools.core.model.IIpsElement#getChildren()
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
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IPolicyCmptType#getAttributes()
     */
    public IAttribute[] getAttributes() {
        IAttribute[] a = new IAttribute[attributes.size()];
        attributes.toArray(a);
        return a;
    }
    
    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IPolicyCmptType#getAttribute(java.lang.String)
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
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IPolicyCmptType#getNumOfAttributes()
     */
    public int getNumOfAttributes() {
        return attributes.size();
    }
    
    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IPolicyCmptType#newAttribute()
     */
    public IAttribute newAttribute() {
        Attribute a = newAttributeInternal(getNextPartId());
        updateSrcFile();
        return a;
    }
    
    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IPolicyCmptType#moveAttributes(int[], boolean)
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
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IPolicyCmptType#getNumOfMethods()
     */
    public int getNumOfMethods() {
        return methods.size();
    }

    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IPolicyCmptType#moveMethods(int[], boolean)
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
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IPolicyCmptType#getAttributes()
     */
    public IRelation[] getRelations() {
        IRelation[] r = new IRelation[relations.size()];
        relations.toArray(r);
        return r;
    }
    
    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IPolicyCmptType#getRelation(java.lang.String)
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
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.model.pctype.IPolicyCmptType#getProductRelevantRelations()
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
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IPolicyCmptType#getNumOfAttributes()
     */
    public int getNumOfRelations() {
        return relations.size();
    }
    
    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IPolicyCmptType#moveRelations(int[], boolean)
     */
    public int[] moveRelations(int[] indexes, boolean up) {
        ListElementMover mover = new ListElementMover(relations);
        return mover.move(indexes, up);
    }
    
    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IPolicyCmptType#newAttribute()
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
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IPolicyCmptType#getRules()
     */ 
    public IValidationRuleDef[] getRules() {
        IValidationRuleDef[] r = new IValidationRuleDef[rules.size()];
        rules.toArray(r);
        return r;
    }

    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IPolicyCmptType#newRule()
     */ 
    public IValidationRuleDef newRule() {
        IValidationRuleDef r = newRuleInternal(getNextPartId());
        updateSrcFile();
        return r;
    }

    /*
     * Creates a new rule without updating the src file.
     */
    private IValidationRuleDef newRuleInternal(int id) {
        ValidationRuleDef r = new ValidationRuleDef(this, id);
        rules.add(r);
        return r;
    }
    
    /** 
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.pctype.IPolicyCmptType#getNumOfMethods()
     */
    public int getNumOfRules() {
        return rules.size();
    }
    
    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IPolicyCmptType#moveRules(int[], boolean)
     */
    public int[] moveRules(int[] indexes, boolean up) {
        ListElementMover mover = new ListElementMover(rules);
        return mover.move(indexes, up);
    }
    
    /**
     * Removes the method from the type. 
     */
    void removeRule(ValidationRuleDef rule) {
        rules.remove(rule);
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsObject#getIpsObjectType()
     */
    public IpsObjectType getIpsObjectType() {
        return IpsObjectType.POLICY_CMPT_TYPE;
    }

    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObject#initPropertiesFromXml(org.w3c.dom.Element)
     */
    protected void initPropertiesFromXml(Element element) {
        super.initPropertiesFromXml(element);
        supertype = element.getAttribute(PROPERTY_SUPERTYPE);
        abstractFlag = Boolean.valueOf(element.getAttribute(PROPERTY_ABSTRACT)).booleanValue();
        forceExtensionCompilationUnitGeneration = Boolean.valueOf(element.getAttribute(PROPERTY_FORCE_GENERATION_OF_EXTENSION_CU)).booleanValue();
    }

    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObject#reinitPartCollections()
     */
    protected void reinitPartCollections() {
        attributes.clear();
        methods.clear();
        rules.clear();
        relations.clear();
    }

    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObject#reAddPart(org.faktorips.devtools.core.model.IIpsObjectPart)
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
        } else if (part instanceof IValidationRuleDef) {
            rules.add(part);
            return;
        }
        throw new RuntimeException("Unknown part type" + part.getClass());
    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObject#newPart(java.lang.String, int)
     */
    protected IIpsObjectPart newPart(String xmlTagName, int id) {
        if (xmlTagName.equals(Attribute.TAG_NAME)) {
            return newAttributeInternal(id);
        } else if (xmlTagName.equals(Relation.TAG_NAME)) {
            return newRelationInternal(id);
        } else if (xmlTagName.equals(Method.TAG_NAME)) {
            return newMethodInternal(id);
        } else if (xmlTagName.equals(ValidationRuleDef.TAG_NAME)) {
            return newRuleInternal(id);
        }
        throw new RuntimeException("Could not create part for tag name" + xmlTagName);
    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObject#propertiesToXml(org.w3c.dom.Element)
     */
    protected void propertiesToXml(Element newElement) {
        super.propertiesToXml(newElement);
        newElement.setAttribute(PROPERTY_SUPERTYPE, supertype);
        newElement.setAttribute(PROPERTY_ABSTRACT, "" + abstractFlag);
        newElement.setAttribute(PROPERTY_FORCE_GENERATION_OF_EXTENSION_CU, "" + forceExtensionCompilationUnitGeneration);
    }

    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObject#validateThis(org.faktorips.util.message.MessageList)
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
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IPolicyCmptType#isAbstract()
     */
    public boolean isAbstract() {
        return abstractFlag;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IPolicyCmptType#setAbstract(boolean)
     */
    public void setAbstract(boolean newValue) {
        boolean oldValue = abstractFlag;
        abstractFlag = newValue;
        valueChanged(oldValue, newValue);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IPolicyCmptType#getSupertypeHierarchy()
     */
    public ITypeHierarchy getSupertypeHierarchy() throws CoreException {
        return TypeHierarchy.getSupertypeHierarchy(this);
    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.model.pctype.IPolicyCmptType#getSubtypeHierarchy()
     */
    public ITypeHierarchy getSubtypeHierarchy() throws CoreException {
        return TypeHierarchy.getSubtypeHierarchie(this);
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IPolicyCmptType#getOverrideCandidates()
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
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.model.IIpsObject#dependsOn()
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
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IPolicyCmptType#hasSameMethod(org.faktorips.devtools.core.model.pctype.IMethod)
     */
    public boolean hasSameMethod(IMethod method) {
        return getMatchingMethod(method)!=null;
    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.model.pctype.IPolicyCmptType#getMatchingMethod(org.faktorips.devtools.core.model.pctype.IMethod)
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
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IPolicyCmptType#override(org.faktorips.devtools.core.model.pctype.IMethod[])
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
     * Overridden method.
     * @see org.faktorips.datatype.Datatype#isVoid()
     */
    public boolean isVoid() {
        return false;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.datatype.Datatype#isPrimitive()
     */
    public boolean isPrimitive() {
        return false;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.datatype.Datatype#isValueDatatype()
     */
    public boolean isValueDatatype() {
        return false;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.datatype.Datatype#getJavaClassName()
     */
    public String getJavaClassName() {
        try {
            return getJavaType(JAVA_POLICY_CMPT_IMPLEMENTATION_TYPE).getFullyQualifiedName();
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    public String getJavaInterfaceName() {
        try  {
            return getJavaType(JAVA_POLICY_CMPT_PUBLISHED_INTERFACE_TYPE).getFullyQualifiedName();
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }
    
    /** 
     * Overridden method.
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o) {
        return 0;
    }
    
    
}