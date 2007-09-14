/***************************************************************************************************
 *  * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.  *  * Alle Rechte vorbehalten.  *  *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,  * Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der  * Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community)  * genutzt werden, die Bestandteil der Auslieferung ist und auch
 * unter  *   http://www.faktorips.org/legal/cl-v01.html  * eingesehen werden kann.  *  *
 * Mitwirkende:  *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de  *  
 **************************************************************************************************/

package org.faktorips.devtools.core.internal.model.pctype;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.IpsObject;
import org.faktorips.devtools.core.internal.model.TableContentsEnumDatatypeAdapter;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsProjectProperties;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.QualifiedNameType;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IMethod;
import org.faktorips.devtools.core.model.pctype.IParameter;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.pctype.ITypeHierarchy;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.pctype.Parameter;
import org.faktorips.devtools.core.model.pctype.PolicyCmptTypeHierarchyVisitor;
import org.faktorips.devtools.core.model.product.ConfigElementType;
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

    private String unqalifiedProductCmptType = ""; //$NON-NLS-1$

    private String supertype = ""; //$NON-NLS-1$

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
     * {@inheritDoc}
     */
    public String getProductCmptType() {
        IIpsPackageFragment pack = getIpsPackageFragment();
        if (pack.isDefaultPackage()) {
            return unqalifiedProductCmptType;
        } else {
            return pack.getName() + '.' + unqalifiedProductCmptType;
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isConfigurableByProductCmptType() {
        return configurableByProductCmptType;
    }

    /**
     * {@inheritDoc}
     */
    public void setConfigurableByProductCmptType(boolean newValue) {
        boolean oldValue = configurableByProductCmptType;
        configurableByProductCmptType = newValue;
        valueChanged(oldValue, newValue);
    }

    /**
     * {@inheritDoc}
     */
    public IProductCmptType findOldProductCmptType() throws CoreException {
        if (!configurableByProductCmptType) {
            return null;
        }
        if (StringUtils.isEmpty(unqalifiedProductCmptType)) {
            return null;
        }
        return new ProductCmptType(this);
    }


    /**
     * {@inheritDoc}
     */
    public org.faktorips.devtools.core.model.productcmpttype2.IProductCmptType findProductCmptType(IIpsProject ipsProject) throws CoreException {
        return (org.faktorips.devtools.core.model.productcmpttype2.IProductCmptType)ipsProject.findProductCmptType(getProductCmptType());
    }

    public IProductCmptType getProductCmptTypeInternal() throws CoreException {
        return new ProductCmptType(this);
    }
    
    /**
     * {@inheritDoc}
     */
    public String getUnqualifiedProductCmptType() {
        return unqalifiedProductCmptType;
    }

    /**
     * {@inheritDoc}
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
        } else if (partType.equals(IMethod.class)) {
            return newMethod();
        } else if (partType.equals(IRelation.class)) {
            return newRelation();
        } else if (partType.equals(IValidationRule.class)) {
            return newRule();
        }
        throw new IllegalArgumentException("Unknown part type" + partType); //$NON-NLS-1$
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
    public boolean hasSupertype() {
        return StringUtils.isNotEmpty(supertype);
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
     * {@inheritDoc}
     */
    public void setSupertype(String newSupertype) {
        String oldSupertype = supertype;
        supertype = newSupertype;
        valueChanged(oldSupertype, newSupertype);
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isSubtypeOf(IPolicyCmptType supertypeCandidate) throws CoreException {
        if (supertypeCandidate==null) {
            return false;
        }
        IPolicyCmptType supertype = findSupertype();
        if (supertype==null) {
            return false;
        }
        if (supertypeCandidate.equals(supertype)) {
            return true;
        }
        return supertype.isSubtypeOf(supertypeCandidate);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isSubtypeOrSameType(IPolicyCmptType candidate) throws CoreException {
        if (this.equals(candidate)) {
            return true;
        }
        return isSubtypeOf(candidate);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isForceExtensionCompilationUnitGeneration() {
        return forceExtensionCompilationUnitGeneration;
    }

    /**
     * {@inheritDoc}
     */
    public void setForceExtensionCompilationUnitGeneration(boolean flag) {
        boolean oldValue = forceExtensionCompilationUnitGeneration;
        forceExtensionCompilationUnitGeneration = flag;
        valueChanged(oldValue, forceExtensionCompilationUnitGeneration);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isExtensionCompilationUnitGenerated() {
        if (forceExtensionCompilationUnitGeneration) {
            return true;
        }
        if (getNumOfRules() > 0) {
            return true;
        }
        for (Iterator it = methods.iterator(); it.hasNext();) {
            IMethod method = (IMethod)it.next();
            if (!method.isAbstract()) {
                return true;
            }
        }
        for (Iterator it = attributes.iterator(); it.hasNext();) {
            IAttribute attribute = (IAttribute)it.next();
            if (attribute.getAttributeType() == AttributeType.DERIVED_BY_EXPLICIT_METHOD_CALL) {
                if (!attribute.isProductRelevant()) {
                    return true;
                }
            } else if (attribute.getAttributeType() == AttributeType.DERIVED_ON_THE_FLY) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public IIpsElement[] getChildren() {
        int numOfChildren = getNumOfAttributes() + getNumOfMethods() + getNumOfRelations() + getNumOfRules();
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
     * {@inheritDoc}
     */
    public IAttribute[] getAttributes() {
        IAttribute[] a = new IAttribute[attributes.size()];
        attributes.toArray(a);
        return a;
    }

    /**
     * {@inheritDoc}
     */
    public IAttribute getAttribute(String name) {
        for (Iterator it = attributes.iterator(); it.hasNext();) {
            IAttribute a = (IAttribute)it.next();
            if (a.getName().equals(name)) {
                return a;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public IAttribute findAttributeInSupertypeHierarchy(String name) throws CoreException {
        FindAttributeInTypeHierarchyVisitor visitor = new FindAttributeInTypeHierarchyVisitor(name);
        visitor.start(this);
        return visitor.getAttribute();
    }

    /**
     * {@inheritDoc}
     */
    public int getNumOfAttributes() {
        return attributes.size();
    }

    /**
     * {@inheritDoc}
     */
    public IAttribute newAttribute() {
        Attribute a = newAttributeInternal(getNextPartId());
        partWasAdded(a);
        return a;
    }

    /**
     * {@inheritDoc}
     */
    public int[] moveAttributes(int[] indexes, boolean up) {
        ListElementMover mover = new ListElementMover(attributes);
        int[] newIndexes = mover.move(indexes, up);
        partsMoved(getAttributes());
        return newIndexes;
    }

    /*
     * Returns the list holding the attributes as a reference. Package private for use in
     * TypeHierarchy.
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
     * {@inheritDoc}
     */
    public IMethod[] getMethods() {
        IMethod[] m = new IMethod[methods.size()];
        methods.toArray(m);
        return m;
    }

    /*
     * Returns the list holding the methods as a reference. Package private for use in
     * TypeHierarchy.
     */
    List getMethodList() {
        return methods;
    }

    /**
     * {@inheritDoc}
     */
    public IMethod newMethod() {
        Method m = newMethodInternal(getNextPartId());
        partWasAdded(m);
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
     * {@inheritDoc}
     */
    public int getNumOfMethods() {
        return methods.size();
    }

    /**
     * {@inheritDoc}
     */
    public int[] moveMethods(int[] indexes, boolean up) {
        ListElementMover mover = new ListElementMover(methods);
        int[] newIndices = mover.move(indexes, up);
        partsMoved(getMethods());
        return newIndices;
    }

    /**
     * {@inheritDoc}
     * 
     * @throws CoreException
     */
    public boolean isAggregateRoot() throws CoreException {
        IsAggregrateRootVisitor visitor = new IsAggregrateRootVisitor();
        visitor.start(this);
        return visitor.isRoot();
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isDependantType() throws CoreException {
        return !isAggregateRoot();
    }
    
    /**
     * {@inheritDoc}
     */
    public IRelation[] getRelations() {
        IRelation[] r = new IRelation[relations.size()];
        relations.toArray(r);
        return r;
    }
    
    /*
     * Returns the list holding the relations as a reference. Package private for use in
     * TypeHierarchy.
     */
    List getRelationList() {
        return relations;
    }

    /**
     * {@inheritDoc}
     */
    public IRelation getRelation(String name) {
        ArgumentCheck.notNull(name);
        for (Iterator it = relations.iterator(); it.hasNext();) {
            IRelation each = (IRelation)it.next();
            if (name.equals(each.getName())) {
                return each;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public IRelation[] getProductRelevantRelations() {
        ArrayList productRelevantRelations = new ArrayList();
        for (Iterator it = relations.iterator(); it.hasNext();) {
            IRelation relation = (IRelation)it.next();
            if (relation.isProductRelevant()) {
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
        int[] newIndices = mover.move(indexes, up);
        partsMoved(getRelations());
        return newIndices;
    }

    /**
     * {@inheritDoc}
     */
    public IRelation newRelation() {
        Relation r = newRelationInternal(getNextPartId());
        partWasAdded(r);
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
     * {@inheritDoc}
     */
    public IRelation[] findRelationsImplementingContainerRelation(IRelation containerRelation,
            boolean searchSupertypeHierarchy) throws CoreException {
        if (containerRelation == null) {
            return new IRelation[0];
        }
        IRelation[] candidates;
        if (searchSupertypeHierarchy) {
            ITypeHierarchy hierarchy = getSupertypeHierarchy();
            candidates = hierarchy.getAllRelations(this);
        } else {
            candidates = getRelations();
        }
        return findRelationsImplementingContainerRelation(containerRelation, candidates);
    }

    /*
     * candicates must contain relations in supertype hierarchy order
     */
    private IRelation[] findRelationsImplementingContainerRelation(IRelation containerRelation, IRelation[] candidates)
            throws CoreException {
        List result = new ArrayList();
        boolean containerRelationTypeReached = false;
        for (int i = 0; i < candidates.length; i++) {
            if (candidates[i].getPolicyCmptType().equals(containerRelation.getPolicyCmptType())) {
                containerRelationTypeReached = true;
            } else {
                if (containerRelationTypeReached) {
                    break; // do not walk past the container relation's type in the hierarchy
                }
            }
            if (candidates[i].isContainerRelationImplementation(containerRelation)) {
                result.add(candidates[i]);
            }
        }
        return (IRelation[])result.toArray(new IRelation[result.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public IValidationRule[] getRules() {
        IValidationRule[] r = new IValidationRule[rules.size()];
        rules.toArray(r);
        return r;
    }
    
    /*
     * Returns the list holding the rules as a reference. Package private for use in
     * TypeHierarchy.
     */
    List getRulesList() {
        return rules;
    }

    /**
     * {@inheritDoc}
     */
    public IValidationRule newRule() {
        IValidationRule r = newRuleInternal(getNextPartId());
        partWasAdded(r);
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
        int[] newIndices = mover.move(indexes, up);
        partsMoved(getRules());
        return newIndices;
    }

    /**
     * {@inheritDoc}
     */
    public IpsObjectType getIpsObjectType() {
        return IpsObjectType.POLICY_CMPT_TYPE;
    }

    /**
     * {@inheritDoc}
     */
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
        configurableByProductCmptType = Boolean.valueOf(element.getAttribute(PROPERTY_CONFIGURABLE_BY_PRODUCTCMPTTYPE))
                .booleanValue();
        unqalifiedProductCmptType = element.getAttribute(PROPERTY_UNQUALIFIED_PRODUCT_CMPT_TYPE);
        supertype = element.getAttribute(PROPERTY_SUPERTYPE);
        abstractFlag = Boolean.valueOf(element.getAttribute(PROPERTY_ABSTRACT)).booleanValue();
        forceExtensionCompilationUnitGeneration = Boolean.valueOf(
                element.getAttribute(PROPERTY_FORCE_GENERATION_OF_EXTENSION_CU)).booleanValue();
    }

    /**
     * {@inheritDoc}
     */
    protected void reinitPartCollections() {
        attributes.clear();
        methods.clear();
        rules.clear();
        relations.clear();
    }

    /**
     * {@inheritDoc}
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
        throw new RuntimeException("Unknown part type" + part.getClass()); //$NON-NLS-1$
    }
    
    /**
     * {@inheritDoc}
     */
    protected void removePart(IIpsObjectPart part) {
        if (part instanceof IAttribute) {
            attributes.remove(part);
            return;
        } else if (part instanceof IMethod) {
            methods.remove(part);
            return;
        } else if (part instanceof IRelation) {
            relations.remove(part);
            return;
        } else if (part instanceof IValidationRule) {
            rules.remove(part);
            return;
        }
        throw new RuntimeException("Unknown part type" + part.getClass()); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    protected IIpsObjectPart newPart(Element xmlTag, int id) {
        String xmlTagName = xmlTag.getNodeName();
        if (xmlTagName.equals(Attribute.TAG_NAME)) {
            return newAttributeInternal(id);
        } else if (xmlTagName.equals(Relation.TAG_NAME)) {
            return newRelationInternal(id);
        } else if (xmlTagName.equals(Method.TAG_NAME)) {
            return newMethodInternal(id);
        } else if (xmlTagName.equals(ValidationRule.TAG_NAME)) {
            return newRuleInternal(id);
        }
        throw new RuntimeException("Could not create part for tag name" + xmlTagName); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    protected void propertiesToXml(Element newElement) {
        super.propertiesToXml(newElement);
        newElement.setAttribute(PROPERTY_CONFIGURABLE_BY_PRODUCTCMPTTYPE, "" + configurableByProductCmptType); //$NON-NLS-1$
        newElement.setAttribute(PROPERTY_UNQUALIFIED_PRODUCT_CMPT_TYPE, unqalifiedProductCmptType);
        newElement.setAttribute(PROPERTY_SUPERTYPE, supertype);
        newElement.setAttribute(PROPERTY_ABSTRACT, "" + abstractFlag); //$NON-NLS-1$
        newElement
                .setAttribute(PROPERTY_FORCE_GENERATION_OF_EXTENSION_CU, "" + forceExtensionCompilationUnitGeneration); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    protected void validateThis(MessageList list) throws CoreException {
        super.validateThis(list);
        TypeHierarchy supertypeHierarchy = null;
        supertypeHierarchy = TypeHierarchy.getSupertypeHierarchy(this);
        if (supertypeHierarchy.containsCycle()) {
            String msg = "Cycle detected in type hierarchy."; //$NON-NLS-1$
            list.add(new Message(MSGCODE_CYCLE_IN_TYPE_HIERARCHY, msg.toString(), Message.ERROR, this, IPolicyCmptType.PROPERTY_SUPERTYPE));
            return;
        }
        validateSupertypeHierarchy(supertypeHierarchy, list);

        IPolicyCmptType supertypeObj = null;
        if (!supertype.equals("")) { //$NON-NLS-1$
            supertypeObj = (IPolicyCmptType)getIpsProject().findIpsObject(IpsObjectType.POLICY_CMPT_TYPE, supertype);
            if (supertypeObj == null) {
                String text = NLS.bind(Messages.PolicyCmptType_msgSupertypeNotFound, supertype);
                list.add(new Message(MSGCODE_SUPERTYPE_NOT_FOUND, text, Message.ERROR, this,
                        IPolicyCmptType.PROPERTY_SUPERTYPE)); //$NON-NLS-1$
            }
        }

        validateProductSide(list);

        if (!isAbstract()) {
            validateIfAllAbstractMethodsAreImplemented(supertypeHierarchy, list);
            IIpsProjectProperties props = getIpsProject().getProperties();
            if (props.isContainerRelationIsImplementedRuleEnabled()) {
                validateIfAllContainerRelationsAreImplemented(supertypeHierarchy, list);
            }
            IMethod[] methods = getMethods();
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].isAbstract()) {
                    String text = Messages.PolicyCmptType_msgAbstractMissmatch;
                    list.add(new Message(MSGCODE_ABSTRACT_MISSING, text, Message.ERROR, this,
                            IPolicyCmptType.PROPERTY_ABSTRACT)); //$NON-NLS-1$
                }
            }
        }
    }

    private void validateProductSide(MessageList list) throws CoreException {
        if (!isConfigurableByProductCmptType()) {
            return;
        }

        validateNamingConventionsForProductCmptType(list);
        
        if (StringUtils.isEmpty(this.unqalifiedProductCmptType)) {
            String text = Messages.PolicyCmptType_msgNameMissing;
            list.add(new Message(MSGCODE_PRODUCT_CMPT_TYPE_NAME_MISSING, text, Message.ERROR, this,
                    IPolicyCmptType.PROPERTY_UNQUALIFIED_PRODUCT_CMPT_TYPE));
        } else {
            IStatus status = JavaConventions.validateJavaTypeName(unqalifiedProductCmptType);
            if (status.getSeverity() == IStatus.ERROR) {
                String msg = Messages.PolicyCmptType_msgInvalidProductCmptTypeName;
                list.add(new Message(MSGCODE_INVALID_PRODUCT_CMPT_TYPE_NAME, msg, Message.ERROR, this,
                        PROPERTY_UNQUALIFIED_PRODUCT_CMPT_TYPE));
            }
        }
        if (this.unqalifiedProductCmptType.equals(this.getName())) {
            String msg = Messages.PolicyCmptType_msgNameMissmatch;
            list.add(new Message(MSGCODE_PRODUCT_CMPT_TYPE_NAME_MISSMATCH, msg, Message.ERROR, this,
                    IPolicyCmptType.PROPERTY_UNQUALIFIED_PRODUCT_CMPT_TYPE));
        }
        
        IPolicyCmptType superPolicyCmptType = findSupertype();
        if (superPolicyCmptType != null){
            if (! superPolicyCmptType.isConfigurableByProductCmptType()){
                String msg = Messages.PolicyCmptType_msgSuperTypeNotProdRelevantIfProductRelevant;
                list.add(new Message(MSGCODE_SUPERTYPE_NOT_PRODUCT_RELEVANT_IF_THE_TYPE_IS_PRODUCT_RELEVANT, msg, Message.ERROR, this,
                        IPolicyCmptType.PROPERTY_CONFIGURABLE_BY_PRODUCTCMPTTYPE));
            }
        }
    }

    /*
     *  Validate naming conventions for the product cmpt type
     */
    private void validateNamingConventionsForProductCmptType(MessageList list) throws CoreException {
        validateNamingConventions(list, getUnqualifiedProductCmptType(), PROPERTY_UNQUALIFIED_PRODUCT_CMPT_TYPE);
    }

    /**
     * Validation for {@link IPolicyCmptType#MSGCODE_MUST_OVERRIDE_ABSTRACT_METHOD}
     */
    private void validateIfAllAbstractMethodsAreImplemented(ITypeHierarchy hierarchy, MessageList list)
            throws CoreException {
        if (hierarchy.getSupertype(this) == null) {
            return;
        }
        IMethod[] methods = hierarchy.getAllMethods(hierarchy.getSupertype(this));
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].isAbstract()) {
                if (!isAbstractMethodImplemented(this, methods[i], hierarchy)) {
                    String text = NLS.bind(Messages.PolicyCmptType_msgMustOverrideAbstractMethod, methods[i].getName(),
                            methods[i].getPolicyCmptType().getQualifiedName());
                    list.add(new Message(IPolicyCmptType.MSGCODE_MUST_OVERRIDE_ABSTRACT_METHOD, text, Message.ERROR,
                            this));
                }
            }
        }
    }

    /**
     * Returns true if the method is implemented in the indicated type or one of the type's
     * supertypes. Helper method for the one above.
     * 
     * @param pcType The policy component type where the search for the implementation starts.
     * @param method An abstract method of one the type's supertypes.
     * @param hierarchy The supertype hierarchy where the supertype relation is already resolved.
     */
    private boolean isAbstractMethodImplemented(IPolicyCmptType pcType, IMethod method, ITypeHierarchy hierarchy) {
        IMethod match = pcType.getMatchingMethod(method);
        if (match != null && !match.isAbstract()) {
            return true;
        }
        IPolicyCmptType supertype = hierarchy.getSupertype(pcType);
        if (supertype == null) {
            return false;
        }
        return isAbstractMethodImplemented(supertype, method, hierarchy);
    }

    /**
     * Validation for {@link IPolicyCmptType#MSGCODE_MUST_IMPLEMENT_CONTAINER_RELATION}
     */
    private void validateIfAllContainerRelationsAreImplemented(ITypeHierarchy hierarchy, MessageList list)
            throws CoreException {
        IRelation[] relations = hierarchy.getAllRelations(this);
        for (int i = 0; i < relations.length; i++) {
            if (relations[i].isReadOnlyContainer()) {
                if (!isContainerRelationImplemented(relations[i], hierarchy)) {
                    String text = NLS.bind(Messages.PolicyCmptType_msgMustImplementContainerRelation, relations[i]
                            .getName(), relations[i].getPolicyCmptType().getQualifiedName());
                    list.add(new Message(IPolicyCmptType.MSGCODE_MUST_IMPLEMENT_CONTAINER_RELATION, text,
                            Message.ERROR, this, PROPERTY_ABSTRACT));
                }
            }
        }
    }

    /**
     * Returns true if the container relation is implemented in this type or one of it's
     * supertypes. Helper method for the one above.
     * 
     * @param relation A container relation of one the type's supertypes.
     * @param hierarchy The supertype hierarchy where the supertype relation is already resolved.
     * 
     * @throws CoreException
     */
    private boolean isContainerRelationImplemented(IRelation containerRelation, ITypeHierarchy hierarchy)
            throws CoreException {

        IRelation[] candidates = hierarchy.getAllRelations(this);
        return findRelationsImplementingContainerRelation(containerRelation, candidates).length > 0;
    }

    /**
     * Checks if an MSGCODE_SUPERTYPE_NOT_FOUND error in the supertype hirarchy exists. If so, this
     * is reported with a new message with code MSGCODE_INCONSISTENT_TYPE_HIERARCHY in the given
     * list. The message(s) returned by the supertype ar not added.
     */
    private void validateSupertypeHierarchy(TypeHierarchy supertypeHierarchy, MessageList ml) throws CoreException {
        if (supertypeHierarchy == null) {
            return;
        }
        IPolicyCmptType supertype = supertypeHierarchy.getSupertype(this);
        if (supertype == null) {
            return; 
        }
        try {
            MessageList tmpList = supertype.validate();
            for (int i=0; i<tmpList.getNoOfMessages(); i++) {
                Message msg = tmpList.getMessage(i);
                if (msg.getCode().equals(MSGCODE_INCONSISTENT_TYPE_HIERARCHY)
                        || msg.getCode().equals(MSGCODE_SUPERTYPE_NOT_FOUND)
                        || msg.getCode().equals(MSGCODE_PRODUCT_CMPT_TYPE_NAME_MISSING)
                        || msg.getCode().equals(MSGCODE_CYCLE_IN_TYPE_HIERARCHY)) {
                    ml.add(new Message(MSGCODE_INCONSISTENT_TYPE_HIERARCHY,
                            Messages.PolicyCmptType_msgInconsistentTypeHierarchy, Message.ERROR, this, PROPERTY_SUPERTYPE));
                    
                }
            }
        } catch (CoreException ce) {
            throw ce;
        } catch (Exception e) {
            throw new CoreException(new IpsStatus(e));
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isAbstract() {
        return abstractFlag;
    }

    /**
     * {@inheritDoc}
     */
    public void setAbstract(boolean newValue) {
        boolean oldValue = abstractFlag;
        abstractFlag = newValue;
        valueChanged(oldValue, newValue);
    }

    /**
     * {@inheritDoc}
     */
    public ITypeHierarchy getSupertypeHierarchy() throws CoreException {
        return TypeHierarchy.getSupertypeHierarchy(this);
    }

    /**
     * {@inheritDoc}
     */
    public ITypeHierarchy getSubtypeHierarchy() throws CoreException {
        return TypeHierarchy.getSubtypeHierarchy(this);
    }

    /**
     * {@inheritDoc}
     */
    public IMethod[] findOverrideMethodCandidates(boolean onlyAbstractMethods) throws CoreException {
        List candidates = new ArrayList();
        ITypeHierarchy hierarchy = getSupertypeHierarchy();
        IPolicyCmptType[] supertypes = hierarchy.getAllSupertypes(this);
        for (int i = 0; i < supertypes.length; i++) {
            getOverrideCandidates(supertypes[i], onlyAbstractMethods, candidates);
        }
        return (IMethod[])candidates.toArray(new IMethod[candidates.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public IAttribute[] findOverrideAttributeCandidates() throws CoreException {
        IPolicyCmptType supertype = findSupertype();

        if (supertype == null) {
            // no supertype, no candidates :-)
            return new IAttribute[0];
        }

        // for easy finding attributes by name put them in a map with the name as key
        Map toExclude = new HashMap();
        for (Iterator iter = attributes.iterator(); iter.hasNext();) {
            IAttribute attr = (IAttribute)iter.next();
            if (attr.getOverwrites()) {
                toExclude.put(attr.getName(), attr);
            }
        }

        // find all overwrite-candidates
        IAttribute[] candidates = getSupertypeHierarchy().getAllAttributes(supertype);
        List result = new ArrayList();
        for (int i = 0; i < candidates.length; i++) {
            if (!toExclude.containsKey(candidates[i].getName())) {
                result.add(candidates[i]);
            }
        }

        return (IAttribute[])result.toArray(new IAttribute[result.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public QualifiedNameType[] dependsOn() throws CoreException {
        return dependsOn(false);
    }

    /**
     * Returns the <code>QualifiedNameType</code>s of the <code>IpsObject</code>s this
     * <code>IpsObject</code> depends on. This method is used by the interface method dependsOn()
     * and is public because it is used by the <code>ProductCmptType</code>
     * 
     * @param excludeNonProductRelations if true only the Relations that are marked as
     *            productrelevant are considered
     * @throws CoreException delegates rising CoreExceptions
     */
    public QualifiedNameType[] dependsOn(boolean excludeNonProductRelations) throws CoreException {
        Set qualifiedNameTypes = new HashSet();
        new AddQNamesFromTypeHierarchyVisitor(qualifiedNameTypes).start(this);
        addQualifiedNameTypesForRelationTargets(qualifiedNameTypes, excludeNonProductRelations);
        addQualifiedNameTypesForFormulaParameters(qualifiedNameTypes);
        addQualifiedNameTypesForTableBasedEnums(qualifiedNameTypes);
        return (QualifiedNameType[])qualifiedNameTypes.toArray(new QualifiedNameType[qualifiedNameTypes.size()]);
    }

    private void addQualifiedNameTypesForTableBasedEnums(Set qualifedNameTypes) throws CoreException{
        IAttribute[] attributes = getAttributes();
        for (int i = 0; i < attributes.length; i++) {
            Datatype datatype = attributes[i].findDatatype();
            if(datatype instanceof TableContentsEnumDatatypeAdapter){
                TableContentsEnumDatatypeAdapter enumDatatype = (TableContentsEnumDatatypeAdapter)datatype;
                qualifedNameTypes.add(enumDatatype.getTableContents().getQualifiedNameType());
                qualifedNameTypes.add(new QualifiedNameType(enumDatatype.getTableContents().getTableStructure(), IpsObjectType.TABLE_STRUCTURE));
            }
        }
    }
    
    private void addQualifiedNameTypesForFormulaParameters(Set qualifiedNameTypes) throws CoreException {
        IAttribute[] attributes = getAttributes();
        IIpsProject ipsProject = getIpsProject();
        for (int i = 0; i < attributes.length; i++) {
            if (ConfigElementType.FORMULA.equals(attributes[i].getConfigElementType())) {
                Parameter[] parameters = attributes[i].getFormulaParameters();
                for (int j = 0; j < parameters.length; j++) {
                    String datatypeId = parameters[j].getDatatype();
                    Datatype datatype = ipsProject.findDatatype(datatypeId);
                    if (datatype instanceof ValueDatatype) {
                        // no dependency
                    } else if (datatype instanceof IIpsObject) {
                        IIpsObject ipsObject = (IIpsObject)datatype;
                        qualifiedNameTypes.add(ipsObject.getQualifiedNameType());
                    } else {
                        for (int k = 0; k < IpsObjectType.ALL_TYPES.length; k++) {
                            if (IpsObjectType.ALL_TYPES[k].isDatatype()) {
                                qualifiedNameTypes.add(new QualifiedNameType(datatypeId, IpsObjectType.ALL_TYPES[k]));
                            }
                        }
                    }
                }
            }
        }
    }

    private void addQualifiedNameTypesForRelationTargets(Set qualifiedNameTypes, boolean excludeNonProductRelations) throws CoreException {
        IRelation[] relations = getRelations();
        for (int i = 0; i < relations.length; i++) {
            if (excludeNonProductRelations && !relations[i].isProductRelevant()) {
                continue;
            }
            String qualifiedName = relations[i].getTarget();
            qualifiedNameTypes.add(new QualifiedNameType(qualifiedName, IpsObjectType.POLICY_CMPT_TYPE));

            // an additional condition "&& this.isAggregateRoot()" will _not_ be helpfull, because this
            // method is called recursively for the detail and so on. But this detail is not an 
            // aggregate root and the recursion will terminate to early.
            if (relations[i].isCompositionMasterToDetail() 
                    && this.getIpsProject().getIpsArtefactBuilderSet().containsAggregateRootBuilder()) {
                IPolicyCmptType target = getIpsProject().findPolicyCmptType(qualifiedName);
                qualifiedNameTypes.addAll(Arrays.asList(target.dependsOn()));
            }
        }
    }

    /*
     * helper method for getOverrideCandidates
     */
    private void getOverrideCandidates(IPolicyCmptType type, boolean onlyAbstractMethods, List candidates) {
        IMethod[] supertypeMethods = type.getMethods();
        for (int i = 0; i < supertypeMethods.length; i++) {
            if (!hasSameMethod(supertypeMethods[i])) {
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
        for (Iterator it = candidates.iterator(); it.hasNext();) {
            IMethod candidate = (IMethod)it.next();
            if (method.isSame(candidate)) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasSameMethod(IMethod method) {
        return getMatchingMethod(method) != null;
    }

    /**
     * {@inheritDoc}
     */
    public IMethod getMatchingMethod(IMethod method) {
        for (Iterator it = this.methods.iterator(); it.hasNext();) {
            IMethod thisMethod = (IMethod)it.next();
            if (thisMethod.isSame(method)) {
                return thisMethod;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public IMethod[] overrideMethods(IMethod[] methods) {
        IMethod[] newMethods = new IMethod[methods.length];
        for (int i = 0; i < methods.length; i++) {
            IMethod override = newMethod();
            override.setModifier(methods[i].getModifier());
            override.setAbstract(false);
            override.setDatatype(methods[i].getDatatype());
            override.setName(methods[i].getName());
            IParameter[] params = methods[i].getParameters();
            for (int j = 0; j < params.length; j++) {
                IParameter newParam = override.newParameter();
                newParam.setName(params[j].getName());
                newParam.setDatatype(params[j].getDatatype());
            }
            newMethods[i] = override;
        }
        return newMethods;
    }

    /**
     * {@inheritDoc}
     */
    public IAttribute[] overrideAttributes(IAttribute[] attributes) {
        IAttribute[] newAttributes = new IAttribute[attributes.length];
        for (int i = 0; i < attributes.length; i++) {
            IAttribute override = getAttribute(attributes[i].getName());

            if (override == null) {
                override = newAttribute();
                override.setName(attributes[i].getName());
                override.setDefaultValue(attributes[i].getDefaultValue());
                override.setValueSetCopy(attributes[i].getValueSet());
                override.setFormulaParameters(attributes[i].getFormulaParameters());
                override.setDescription(attributes[i].getDescription());
            }
            override.setOverwrites(true);
            newAttributes[i] = override;
        }
        return newAttributes;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isVoid() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isPrimitive() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isValueDatatype() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public int compareTo(Object o) {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public String getJavaClassName() {
        throw new RuntimeException("getJavaClassName is not supported by " + getClass()); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasNullObject() {
        return false;
    }

    private static class IsAggregrateRootVisitor extends PolicyCmptTypeHierarchyVisitor {

        private boolean root = true;
        
        /**
         * {@inheritDoc}
         */
        protected boolean visit(IPolicyCmptType currentType) {
            IRelation[] relations = currentType.getRelations();
            for (int i=0; i<relations.length; i++) {
                IRelation each = relations[i];
                if (each.getRelationType().isCompositionDetailToMaster()) {
                    root = false;
                    return false; // stop the visit, we have the result
                }
            }
            return true;
        }
        
        public boolean isRoot() {
            return root;
        }
        
    }
    
    private class AddQNamesFromTypeHierarchyVisitor extends PolicyCmptTypeHierarchyVisitor {

        private Set qNames;

        public AddQNamesFromTypeHierarchyVisitor(Set names) {
            super();
            qNames = names;
        }

        /**
         * {@inheritDoc}
         */
        protected boolean visit(IPolicyCmptType currentType) {
            if (!currentType.hasSupertype()) {
                return false;
            }
            if (getQualifiedName().equals(currentType.getSupertype())) {
                return false; // don't create a QNameType for this type
            }
            qNames.add(new QualifiedNameType(currentType.getSupertype(), IpsObjectType.POLICY_CMPT_TYPE));
            return true;
        }
        
    }
    
    private static class FindAttributeInTypeHierarchyVisitor extends PolicyCmptTypeHierarchyVisitor {

        private String attributeName;
        private IAttribute attribute = null;
        
        public FindAttributeInTypeHierarchyVisitor(String attributeName) {
            super();
            this.attributeName = attributeName;
        }
        
        public IAttribute getAttribute() {
            return attribute;
        }

        /**
         * {@inheritDoc}
         */
        protected boolean visit(IPolicyCmptType currentType) {
            attribute = currentType.getAttribute(attributeName);
            return attribute==null;
        }
    }    

}