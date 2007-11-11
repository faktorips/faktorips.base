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
import org.faktorips.devtools.core.internal.model.TableContentsEnumDatatypeAdapter;
import org.faktorips.devtools.core.internal.model.ValidationUtils;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPartCollection;
import org.faktorips.devtools.core.internal.model.type.Method;
import org.faktorips.devtools.core.internal.model.type.Type;
import org.faktorips.devtools.core.model.Dependency;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.ITypeHierarchy;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.pctype.PolicyCmptTypeHierarchyVisitor;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IMethod;
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

    private IpsObjectPartCollection rules;

    public PolicyCmptType(IIpsSrcFile file) {
        super(file);
        rules = new IpsObjectPartCollection(this, ValidationRule.class, IValidationRule.class, ValidationRule.TAG_NAME);
    }

    /**
     * {@inheritDoc}
     */
    protected IpsObjectPartCollection createCollectionForMethods() {
        return new IpsObjectPartCollection(this, Method.class, IMethod.class, Method.XML_ELEMENT_NAME);
    }

    /**
     * {@inheritDoc}
     */
    protected IpsObjectPartCollection createCollectionForAssociations() {
        return new IpsObjectPartCollection(this, PolicyCmptTypeAssociation.class, IPolicyCmptTypeAssociation.class, PolicyCmptTypeAssociation.TAG_NAME);
    }
    
    /**
     * {@inheritDoc}
     */
    protected IpsObjectPartCollection createCollectionForAttributes() {
        return new IpsObjectPartCollection(this, PolicyCmptTypeAttribute.class, IPolicyCmptTypeAttribute.class, PolicyCmptTypeAttribute.TAG_NAME);
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
            setProductCmptType(""); //$NON-NLS-1$
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
    public boolean isSubtypeOf(IPolicyCmptType supertypeCandidate) throws CoreException {
        if (supertypeCandidate == null) {
            return false;
        }
        IPolicyCmptType supertype = (IPolicyCmptType)findSupertype(supertypeCandidate.getIpsProject());
        if (supertype == null) {
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
            IPolicyCmptTypeAttribute attribute = (IPolicyCmptTypeAttribute)it.next();
            if (attribute.getAttributeType() == AttributeType.DERIVED_BY_EXPLICIT_METHOD_CALL) {
                if (!attribute.isProductRelevant()) {
                    return true;
                }
            }
            else if (attribute.getAttributeType() == AttributeType.DERIVED_ON_THE_FLY) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public IPolicyCmptTypeAttribute[] getPolicyCmptTypeAttributes() {
        IPolicyCmptTypeAttribute[] a = new IPolicyCmptTypeAttribute[attributes.size()];
        attributes.toArray(a);
        return a;
    }

    /**
     * {@inheritDoc}
     */
    public IPolicyCmptTypeAttribute getPolicyCmptTypeAttribute(String name) {
        return (IPolicyCmptTypeAttribute)getAttribute(name);
    }

    /**
     * {@inheritDoc}
     */
    public IPolicyCmptTypeAttribute findAttributeInSupertypeHierarchy(String name) throws CoreException {
        return (IPolicyCmptTypeAttribute)findAttribute(name, getIpsProject());
    }

    /**
     * {@inheritDoc}
     */
    public IPolicyCmptTypeAttribute newPolicyCmptTypeAttribute() {
        return (IPolicyCmptTypeAttribute)newAttribute();
    }

    /*
     * Returns the list holding the attributes as a reference. Package private for use in
     * TypeHierarchy.
     */
    List getAttributeList() {
        return attributes.getBackingList();
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
     * Returns the list holding the associations as a reference. Package private for use in
     * TypeHierarchy.
     */
    List getAssociationList() {
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
     * Returns the list holding the rules as a reference. Package private for use in TypeHierarchy.
     */
    List getRulesList() {
        return rules.getBackingList();
    }

    /**
     * {@inheritDoc}
     */
    public IValidationRule newRule() {
        return (IValidationRule)rules.newPart();
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
        return rules.moveParts(indexes, up);
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
    protected void propertiesToXml(Element newElement) {
        super.propertiesToXml(newElement);
        newElement.setAttribute(PROPERTY_CONFIGURABLE_BY_PRODUCTCMPTTYPE, "" + configurableByProductCmptType); //$NON-NLS-1$
        newElement.setAttribute(PROPERTY_PRODUCT_CMPT_TYPE, productCmptType);
        newElement
                .setAttribute(PROPERTY_FORCE_GENERATION_OF_EXTENSION_CU, "" + forceExtensionCompilationUnitGeneration); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);
        validateProductSide(list, ipsProject);
    }

    private void validateProductSide(MessageList list, IIpsProject ipsProject) throws CoreException {
        if (!isConfigurableByProductCmptType()) {
            return;
        }
        if (StringUtils.isEmpty(this.productCmptType)) {
            String text = Messages.PolicyCmptType_msg_ProductCmptTypeNameMissing;
            list.add(new Message(MSGCODE_PRODUCT_CMPT_TYPE_NAME_MISSING, text, Message.ERROR, this,
                    IPolicyCmptType.PROPERTY_PRODUCT_CMPT_TYPE));
        }
        else {
            IProductCmptType productCmptTypeObj = (IProductCmptType)ValidationUtils.checkIpsObjectReference2(productCmptType, IpsObjectType.PRODUCT_CMPT_TYPE_V2,
                    Messages.PolicyCmptType_productCmptType, this, IPolicyCmptType.PROPERTY_PRODUCT_CMPT_TYPE,
                    IPolicyCmptType.MSGCODE_PRODUCT_CMPT_TYPE_NOT_FOUND, list, ipsProject);
            if (productCmptTypeObj!=null) {
                if (productCmptTypeObj.findPolicyCmptType(ipsProject)!=this) {
                    String text = NLS.bind(Messages.PolicyCmptType_TheTypeDoesNotConfigureThisType, productCmptType);
                    list.add(new Message(IPolicyCmptType.MSGCODE_PRODUCT_CMPT_TYPE_DOES_NOT_CONFIGURE_THIS_TYPE, text, Message.ERROR, this, IPolicyCmptType.PROPERTY_PRODUCT_CMPT_TYPE));
                }
            }
        }
        IPolicyCmptType superPolicyCmptType = (IPolicyCmptType)findSupertype(ipsProject);
        if (superPolicyCmptType != null) {
            if (!superPolicyCmptType.isConfigurableByProductCmptType()) {
                String msg = Messages.PolicyCmptType_msg_IfTheSupertypeIsNotConfigurableTheTypeCanBeConfigurable;
                list.add(new Message(MSGCODE_SUPERTYPE_NOT_PRODUCT_RELEVANT_IF_THE_TYPE_IS_PRODUCT_RELEVANT, msg,
                        Message.ERROR, this, IPolicyCmptType.PROPERTY_CONFIGURABLE_BY_PRODUCTCMPTTYPE));
            }
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
    public IPolicyCmptTypeAttribute[] findOverrideAttributeCandidates(IIpsProject ipsProject) throws CoreException {
        IPolicyCmptType supertype = (IPolicyCmptType)findSupertype(ipsProject);

        if (supertype == null) {
            // no supertype, no candidates :-)
            return new IPolicyCmptTypeAttribute[0];
        }

        // for easy finding attributes by name put them in a map with the name as key
        Map toExclude = new HashMap();
        for (Iterator iter = attributes.iterator(); iter.hasNext();) {
            IPolicyCmptTypeAttribute attr = (IPolicyCmptTypeAttribute)iter.next();
            if (attr.getOverwrites()) {
                toExclude.put(attr.getName(), attr);
            }
        }

        // find all overwrite-candidates
        IPolicyCmptTypeAttribute[] candidates = getSupertypeHierarchy().getAllAttributes(supertype);
        List result = new ArrayList();
        for (int i = 0; i < candidates.length; i++) {
            if (!toExclude.containsKey(candidates[i].getName())) {
                result.add(candidates[i]);
            }
        }

        return (IPolicyCmptTypeAttribute[])result.toArray(new IPolicyCmptTypeAttribute[result.size()]);
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
            dependencies.add(Dependency.createSubtypeDependency(this.getQualifiedNameType(), new QualifiedNameType(
                    getSupertype(), IpsObjectType.POLICY_CMPT_TYPE)));
        }
        addQualifiedNameTypesForRelationTargets(dependencies, excludeNonProductRelations);
        addQualifiedNameTypesForTableBasedEnums(dependencies);
        return (Dependency[])dependencies.toArray(new Dependency[dependencies.size()]);
    }

    private void addQualifiedNameTypesForTableBasedEnums(Set qualifedNameTypes) throws CoreException {
        IPolicyCmptTypeAttribute[] attributes = getPolicyCmptTypeAttributes();
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
            }
            else {
                dependencies.add(Dependency.createReferenceDependency(this.getQualifiedNameType(),
                        new QualifiedNameType(qualifiedName, IpsObjectType.POLICY_CMPT_TYPE)));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public IPolicyCmptTypeAttribute[] overrideAttributes(IPolicyCmptTypeAttribute[] attributes) {
        IPolicyCmptTypeAttribute[] newAttributes = new IPolicyCmptTypeAttribute[attributes.length];
        for (int i = 0; i < attributes.length; i++) {
            IPolicyCmptTypeAttribute override = getPolicyCmptTypeAttribute(attributes[i].getName());

            if (override == null) {
                override = newPolicyCmptTypeAttribute();
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

    private static class IsAggregrateRootVisitor extends PolicyCmptTypeHierarchyVisitor {

        private boolean root = true;

        /**
         * {@inheritDoc}
         */
        protected boolean visit(IPolicyCmptType currentType) {
            IPolicyCmptTypeAssociation[] relations = currentType.getPolicyCmptTypeAssociations();
            for (int i = 0; i < relations.length; i++) {
                IPolicyCmptTypeAssociation each = relations[i];
                if (each.getAssociationType().isCompositionDetailToMaster()) {
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


}