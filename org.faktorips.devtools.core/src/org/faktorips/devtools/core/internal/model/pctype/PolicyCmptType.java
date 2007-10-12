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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.internal.model.Dependency;
import org.faktorips.devtools.core.internal.model.IpsObjectPartCollection;
import org.faktorips.devtools.core.internal.model.TableContentsEnumDatatypeAdapter;
import org.faktorips.devtools.core.internal.model.ValidationUtils;
import org.faktorips.devtools.core.internal.model.type.Method;
import org.faktorips.devtools.core.internal.model.type.Type;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsProjectProperties;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.QualifiedNameType;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.ITypeHierarchy;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.pctype.PolicyCmptTypeHierarchyVisitor;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.type.TypeHierarchyVisitor;
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
public class PolicyCmptType extends Type implements IPolicyCmptType {

    private boolean configurableByProductCmptType = false;

    private String productCmptType = ""; //$NON-NLS-1$

    private boolean forceExtensionCompilationUnitGeneration = false;

    private List attributes = new ArrayList(0);

    private List rules = new ArrayList(0);

    public PolicyCmptType(IIpsSrcFile file) {
        super(file);
    }

    /**
     * {@inheritDoc}
     */
    protected IpsObjectPartCollection createCollectionForMethods() {
        return new IpsObjectPartCollection(this, Method.class, Method.XML_ELEMENT_NAME);
    }
    
    /**
     * {@inheritDoc}
     */
    protected IpsObjectPartCollection createCollectionForAssociations() {
        return new IpsObjectPartCollection(this, PolicyCmptTypeAssociation.class, PolicyCmptTypeAssociation.TAG_NAME);
    }
    
    /**
     * {@inheritDoc}
     */
    public String getProductCmptType() {
        return productCmptType;
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
        if (!configurableByProductCmptType) {
            setProductCmptType("");
        }
        valueChanged(oldValue, newValue);
    }

    /**
     * {@inheritDoc}
     */
    public IProductCmptType findProductCmptType(IIpsProject ipsProject) throws CoreException {
        return ipsProject.findProductCmptType(getProductCmptType());
    }

    /**
     * {@inheritDoc}
     */
    public void setProductCmptType(String newName) {
        ArgumentCheck.notNull(newName);
        String oldName = productCmptType;
        productCmptType = newName;
        valueChanged(oldName, newName);
    }

    /**
     * {@inheritDoc}
     */
    public IIpsObjectPart newPart(Class partType) {
        if (partType.equals(IAttribute.class)) {
            return newAttribute();
        } else if (partType.equals(IMethod.class)) {
            return newMethod();
        } else if (partType.equals(IPolicyCmptTypeAssociation.class)) {
            return newPolicyCmptTypeAssociation();
        } else if (partType.equals(IValidationRule.class)) {
            return newRule();
        }
        throw new IllegalArgumentException("Unknown part type" + partType); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public IPolicyCmptType findSupertype() throws CoreException {
        return getIpsProject().findPolicyCmptType(getSupertype());
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
        int numOfChildren = getNumOfAttributes() + getNumOfMethods() + getNumOfAssociations() + getNumOfRules();
        IIpsElement[] childrenArray = new IIpsElement[numOfChildren];
        List childrenList = new ArrayList(numOfChildren);
        childrenList.addAll(attributes);
        childrenList.addAll(methods.getBackingList());
        childrenList.addAll(associations.getBackingList());
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

    /*
     * Returns the list holding the methods as a reference. Package private for use in
     * TypeHierarchy.
     */
    List getMethodList() {
        return methods.getBackingList();
    }

    /**
     * {@inheritDoc}
     */
    public IMethod newMethod() {
        return (IMethod)methods.newPart();
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
        return methods.moveParts(indexes, up);
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
    public IPolicyCmptTypeAssociation[] getPolicyCmptTypeAssociations() {
        IPolicyCmptTypeAssociation[] r = new IPolicyCmptTypeAssociation[associations.size()];
        associations.toArray(r);
        return r;
    }
    
    /*
     * Returns the list holding the relations as a reference. Package private for use in
     * TypeHierarchy.
     */
    List getRelationList() {
        return associations.getBackingList();
    }

    /**
     * {@inheritDoc}
     */
    public IPolicyCmptTypeAssociation getRelation(String name) {
        return (IPolicyCmptTypeAssociation)getAssociation(name);
    }

    /**
     * {@inheritDoc}
     */
    public IPolicyCmptTypeAssociation newPolicyCmptTypeAssociation() {
        return (IPolicyCmptTypeAssociation)newAssociation();
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
        productCmptType = element.getAttribute(PROPERTY_PRODUCT_CMPT_TYPE);
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
        associations.clear();
    }

    /**
     * {@inheritDoc}
     */
    protected void reAddPart(IIpsObjectPart part) {
        if (part instanceof IAttribute) {
            attributes.add(part);
            return;
        } else if (part instanceof IMethod) {
            methods.readdPart(part);
            return;
        } else if (part instanceof IPolicyCmptTypeAssociation) {
            associations.readdPart(part);
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
            methods.removePart(part);
            return;
        } else if (part instanceof IPolicyCmptTypeAssociation) {
            associations.removePart(part);
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
        } else if (xmlTagName.equals(PolicyCmptTypeAssociation.TAG_NAME)) {
            return associations.newPart(xmlTag, id);
        } else if (xmlTagName.equals(Method.XML_ELEMENT_NAME)) {
            return methods.newPart(xmlTag, id);
        } else if (xmlTagName.equals(ValidationRule.TAG_NAME)) {
            return newRuleInternal(id);
        }
        throw new RuntimeException("Could not create part for tag name " + xmlTagName); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    protected void propertiesToXml(Element newElement) {
        super.propertiesToXml(newElement);
        newElement.setAttribute(PROPERTY_CONFIGURABLE_BY_PRODUCTCMPTTYPE, "" + configurableByProductCmptType); //$NON-NLS-1$
        newElement.setAttribute(PROPERTY_PRODUCT_CMPT_TYPE, productCmptType);
        newElement.setAttribute(PROPERTY_FORCE_GENERATION_OF_EXTENSION_CU, "" + forceExtensionCompilationUnitGeneration); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    protected void validateThis(MessageList list) throws CoreException {
        super.validateThis(list);
        IIpsProject ipsProject = getIpsProject();

        validateProductSide(list);

        if (!isAbstract()) {
            validateIfAllAbstractMethodsAreImplemented(getIpsProject(), list);
            IIpsProjectProperties props = getIpsProject().getProperties();
            if (props.isContainerRelationIsImplementedRuleEnabled()) {
                DerivedUnionsSpecifiedValidator validator = new DerivedUnionsSpecifiedValidator(list, ipsProject);
                validator.start(this);
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
        if (StringUtils.isEmpty(this.productCmptType)) {
            String text = Messages.PolicyCmptType_msgNameMissing;
            list.add(new Message(MSGCODE_PRODUCT_CMPT_TYPE_NAME_MISSING, text, Message.ERROR, this,
                    IPolicyCmptType.PROPERTY_PRODUCT_CMPT_TYPE));
        } else {
            if (!ValidationUtils.checkIpsObjectReference(productCmptType, IpsObjectType.PRODUCT_CMPT_TYPE_V2, "Product component type", this, IPolicyCmptType.PROPERTY_PRODUCT_CMPT_TYPE, IPolicyCmptType.MSGCODE_PRODUCT_CMPT_TYPE_NOT_FOUND, list)) {
                return;
            }
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

    /**
     * Validation for {@link IPolicyCmptType#MSGCODE_MUST_OVERRIDE_ABSTRACT_METHOD}
     */
    private void validateIfAllAbstractMethodsAreImplemented(IIpsProject ipsProject, MessageList list)
            throws CoreException {
        
        IMethod[] methods = findOverrideMethodCandidates(true, ipsProject);
        for (int i = 0; i < methods.length; i++) {
            String text = NLS.bind(Messages.PolicyCmptType_msgMustOverrideAbstractMethod, methods[i].getName(),
                    methods[i].getType().getQualifiedName());
            list.add(new Message(IPolicyCmptType.MSGCODE_MUST_OVERRIDE_ABSTRACT_METHOD, text, Message.ERROR, this));
        }
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
    public Dependency[] dependsOn() throws CoreException {
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
    public Dependency[] dependsOn(boolean excludeNonProductRelations) throws CoreException {
        Set dependencies = new HashSet();
        if (hasSupertype()) {
            dependencies.add(Dependency.createSubtypeDependency(this.getQualifiedNameType(), new QualifiedNameType(getSupertype(),
                    IpsObjectType.POLICY_CMPT_TYPE)));
        }
        addQualifiedNameTypesForRelationTargets(dependencies, excludeNonProductRelations);
        addQualifiedNameTypesForTableBasedEnums(dependencies);
        return (Dependency[])dependencies.toArray(new Dependency[dependencies.size()]);
}

    private void addQualifiedNameTypesForTableBasedEnums(Set qualifedNameTypes) throws CoreException {
        IAttribute[] attributes = getAttributes();
        for (int i = 0; i < attributes.length; i++) {
            Datatype datatype = attributes[i].findDatatype();
            if (datatype instanceof TableContentsEnumDatatypeAdapter) {
                TableContentsEnumDatatypeAdapter enumDatatype = (TableContentsEnumDatatypeAdapter)datatype;
                qualifedNameTypes.add(Dependency.createReferenceDependency(this.getQualifiedNameType(), enumDatatype
                        .getTableContents().getQualifiedNameType()));
                qualifedNameTypes.add(Dependency.createReferenceDependency(this.getQualifiedNameType(),
                        new QualifiedNameType(enumDatatype.getTableContents().getTableStructure(),
                                IpsObjectType.TABLE_STRUCTURE)));
            }
        }
    }
    
    private void addQualifiedNameTypesForRelationTargets(Set dependencies, boolean excludeNonProductRelations)
            throws CoreException {
        IPolicyCmptTypeAssociation[] relations = getPolicyCmptTypeAssociations();
        for (int i = 0; i < relations.length; i++) {
            if (excludeNonProductRelations && !relations[i].isProductRelevant()) {
                continue;
            }
            String qualifiedName = relations[i].getTarget();
            // an additional condition "&& this.isAggregateRoot()" will _not_ be helpfull, because
            // this
            // method is called recursively for the detail and so on. But this detail is not an
            // aggregate root and the recursion will terminate to early.
            if (relations[i].isCompositionMasterToDetail()) {
                dependencies.add(Dependency.createCompostionMasterDetailDependency(this.getQualifiedNameType(),
                        new QualifiedNameType(qualifiedName, IpsObjectType.POLICY_CMPT_TYPE)));
            } else {
                dependencies.add(Dependency.createReferenceDependency(this.getQualifiedNameType(),
                        new QualifiedNameType(qualifiedName, IpsObjectType.POLICY_CMPT_TYPE)));
            }
        }
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
            IPolicyCmptTypeAssociation[] relations = currentType.getPolicyCmptTypeAssociations();
            for (int i=0; i<relations.length; i++) {
                IPolicyCmptTypeAssociation each = relations[i];
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
    
    private class DerivedUnionsSpecifiedValidator extends TypeHierarchyVisitor {

        private MessageList msgList;
        private List candidateSubsets = new ArrayList(0);
        
        
        public DerivedUnionsSpecifiedValidator(MessageList msgList, IIpsProject ipsProject) {
            super(ipsProject);
            this.msgList = msgList;
        }

        /**
         * {@inheritDoc}
         */
        protected boolean visit(IType currentType) throws CoreException {
            IAssociation[] associations = currentType.getAssociations();
            for (int i = 0; i < associations.length; i++) {
                candidateSubsets.add(associations[i]);
            }
            for (int i = 0; i < associations.length; i++) {
                if (associations[i].isDerivedUnion()) {
                    if (!isSubsetted(associations[i])) {
                        String text = NLS.bind(Messages.PolicyCmptType_msgMustImplementContainerRelation, associations[i].getName(), 
                                associations[i].getType().getQualifiedName());
                        msgList.add(new Message(IPolicyCmptType.MSGCODE_MUST_IMPLEMENT_CONTAINER_RELATION, text, Message.ERROR, this, IType.PROPERTY_ABSTRACT));
                        
                    }
                }
            }
            return true;
        }
        
        private boolean isSubsetted(IAssociation derivedUnion) throws CoreException {
            for (Iterator it = candidateSubsets.iterator(); it.hasNext();) {
                IAssociation candidate = (IAssociation)it.next();
                if (derivedUnion==candidate.findSubsettedDerivedUnion(ipsProject)) {
                    return true;
                }
            }
            return false;
        }
        
        
    }

}