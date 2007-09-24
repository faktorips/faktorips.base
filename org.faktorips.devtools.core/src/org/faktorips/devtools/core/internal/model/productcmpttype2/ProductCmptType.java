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

package org.faktorips.devtools.core.internal.model.productcmpttype2;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.IpsObjectPartCollection;
import org.faktorips.devtools.core.internal.model.type.Type;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.QualifiedNameType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype2.IAttribute;
import org.faktorips.devtools.core.model.productcmpttype2.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype2.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype2.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.productcmpttype2.ITableStructureUsage;
import org.faktorips.devtools.core.model.productcmpttype2.ProductCmptTypeHierarchyVisitor;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;

/**
 * Implementation of IProductCmptType.
 * 
 * @author Jan Ortmann
 */
public class ProductCmptType extends Type implements IProductCmptType {

    private String policyCmptType = "";
    
    private IpsObjectPartCollection attributes = new IpsObjectPartCollection(this, Attribute.class, "Attribute");
    private IpsObjectPartCollection associations = new IpsObjectPartCollection(this, ProductCmptTypeAssociation.class, "Association");
    private IpsObjectPartCollection tableStructureUsages = new IpsObjectPartCollection(this, TableStructureUsage.class, "TableStructureUsage");
    
    public ProductCmptType(IIpsSrcFile file) {
        super(file);
    }

    /**
     * {@inheritDoc}
     */
    protected IpsObjectPartCollection createCollectionForMethods() {
        return new IpsObjectPartCollection(this, ProductCmptTypeMethod.class, "Method");
    }

    /**
     * {@inheritDoc}
     */
    public IType findSupertype(IIpsProject project) throws CoreException {
        if (!hasSupertype()) {
            return null;
        }
        IProductCmptType supertype = findSuperProductCmptType(project);
        if (supertype!=null) {
            return supertype;
        }
        return null; // TODO hier muessen auch policy component types gefunden werden! 
    }


    /**
     * {@inheritDoc}
     */
    public IpsObjectType getIpsObjectType() {
        return IpsObjectType.PRODUCT_CMPT_TYPE_V2;
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
    public void setPolicyCmptType(String newType) {
        String oldType = policyCmptType;
        policyCmptType = newType;
        valueChanged(oldType, newType);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isConfigurationForPolicyCmptType() {
        return !StringUtils.isEmpty(policyCmptType);
    }

    /**
     * {@inheritDoc}
     */
    public IPolicyCmptType findPolicyCmptType(boolean searchSupertypeHierarchy, IIpsProject project) throws CoreException {
        if (!searchSupertypeHierarchy) {
            return project.findPolicyCmptType(policyCmptType);
        }
        PolicyCmptTypeFinder finder = new PolicyCmptTypeFinder(project);
        finder.start(this);
        return finder.policyCmptType;
    }
    
    /**
     * {@inheritDoc}
     */
    public IProductCmptType findSuperProductCmptType(IIpsProject project) throws CoreException {
        return (IProductCmptType)project.findIpsObject(IpsObjectType.PRODUCT_CMPT_TYPE_V2, getSupertype());
    }

    /**
     * {@inheritDoc}
     */
    public IAttribute newAttribute() {
        return (IAttribute)attributes.newPart();
    }
    
    /**
     * {@inheritDoc}
     */
    public IAttribute getAttribute(String name) {
        return (IAttribute)attributes.getPartByName(name);
    }

    /**
     * {@inheritDoc}
     */
    public IAttribute[] getAttributes() {
        return (IAttribute[])attributes.toArray(new IAttribute[attributes.size()]);
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
    public int[] moveAttributes(int[] indexes, boolean up) {
        return attributes.moveParts(indexes, up);
    }

    /**
     * {@inheritDoc}
     */
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
        policyCmptType = element.getAttribute(PROPERTY_POLICY_CMPT_TYPE);
    }

    /**
     * {@inheritDoc}
     */
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_POLICY_CMPT_TYPE, policyCmptType);
    }

    /**
     * {@inheritDoc}
     */
    public IProductCmptTypeAssociation newAssociation() {
        return (IProductCmptTypeAssociation)associations.newPart();
    }

    /**
     * {@inheritDoc}
     */
    public int getNumOfAssociations() {
        return associations.size();
    }

    /**
     * {@inheritDoc}
     */
    public IProductCmptTypeAssociation getAssociation(String name) {
        return (IProductCmptTypeAssociation)associations.getPartByName(name);
    }
    
    /**
     * {@inheritDoc}
     */
    public IProductCmptTypeAssociation findAssociationInSupertypeHierarchy(String name, boolean includeSelf, IIpsProject project) throws CoreException {
        RelationFinder finder = new RelationFinder(project, name);
        finder.start( includeSelf ? this : findSuperProductCmptType(project));
        return finder.relation;
    }

    /**
     * {@inheritDoc}
     */
    public IProductCmptTypeAssociation[] getAssociations() {
        return (IProductCmptTypeAssociation[])associations.toArray(new IProductCmptTypeAssociation[associations.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public int[] moveAssociations(int[] indexes, boolean up) {
        return associations.moveParts(indexes, up);
    }
    
    /**
     * {@inheritDoc}
     */
    public ITableStructureUsage findTableStructureUsageInSupertypeHierarchy(String roleName, boolean includeSelf, IIpsProject project) throws CoreException {
        TableStructureUsageFinder finder = new TableStructureUsageFinder(project, roleName);
        finder.start( includeSelf ? this : findSuperProductCmptType(project));
        return finder.tsu;
    }

    /**
     * {@inheritDoc}
     */
    public int getNumOfTableStructureUsages() {
        return tableStructureUsages.size();
    }

    /**
     * {@inheritDoc}
     */
    public ITableStructureUsage getTableStructureUsage(String roleName) {
        return (ITableStructureUsage)tableStructureUsages.getPartByName(roleName);
    }

    /**
     * {@inheritDoc}
     */
    public ITableStructureUsage[] getTableStructureUsages() {
        return (ITableStructureUsage[])tableStructureUsages.toArray(new ITableStructureUsage[tableStructureUsages.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public int[] moveTableStructureUsage(int[] indexes, boolean up) {
        return tableStructureUsages.moveParts(indexes, up);
    }

    /**
     * {@inheritDoc}
     */
    public ITableStructureUsage newTableStructureUsage() {
        return (ITableStructureUsage)tableStructureUsages.newPart();    
    }

    /**
     * {@inheritDoc}
     */
    public IProductCmptTypeMethod[] getProductCmptTypeMethods() {
        return (IProductCmptTypeMethod[])methods.toArray(new IProductCmptTypeMethod[methods.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public IProductCmptTypeMethod newProductCmptTypeMethod() {
        return (IProductCmptTypeMethod)methods.newPart();
    }
    
    /**
     * {@inheritDoc}
     */
    public IProductCmptTypeMethod getFormulaSignature(String formulaName) throws CoreException {
        if (StringUtils.isEmpty(formulaName)) {
            return null;
        }
        for (Iterator it=methods.iterator(); it.hasNext(); ) {
            IProductCmptTypeMethod method = (IProductCmptTypeMethod)it.next();
            if (method.isFormulaSignatureDefinition() && formulaName.equalsIgnoreCase(method.getFormulaName())) {
                return method;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public IProductCmptTypeMethod findFormulaSignature(String formulaName, boolean searchTypeHierarchy, IIpsProject ipsProject) throws CoreException {
        if (searchTypeHierarchy) {
            FormulaSignatureFinder finder = new FormulaSignatureFinder(ipsProject, formulaName);
            finder.start(this);
            return finder.method;
        }
        return getFormulaSignature(formulaName);
    }

    /**
     * {@inheritDoc}
     */
    protected void validateThis(MessageList list) throws CoreException {
        super.validateThis(list);
        if (isConfigurationForPolicyCmptType()) {
            validatePolicyCmptTypeReference(getIpsProject(), list);
        }
    }
    
    private void validatePolicyCmptTypeReference(IIpsProject ipsProject, MessageList list) throws CoreException {
        IPolicyCmptType typeObj = findPolicyCmptType(false, ipsProject);
        if (typeObj==null) {
            String text = "The policy component type " + policyCmptType + " does not exist.";
            list.add(new Message(MSGCODE_POLICY_CMPT_TYPE_DOES_NOT_EXIST, text, Message.ERROR, this, PROPERTY_POLICY_CMPT_TYPE));
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public QualifiedNameType[] dependsOn() throws CoreException {
        Set qualifiedNameTypes = new HashSet();
        super.dependsOn(qualifiedNameTypes);
        if (hasSupertype()) {
            qualifiedNameTypes.add(new QualifiedNameType(getSupertype(), IpsObjectType.PRODUCT_CMPT_TYPE_V2));
        }
        addQualifiedNameTypesForRelationTargets(qualifiedNameTypes);
        return (QualifiedNameType[])qualifiedNameTypes.toArray(new QualifiedNameType[qualifiedNameTypes.size()]);
    }

    private void addQualifiedNameTypesForRelationTargets(Set qualifiedNameTypes) throws CoreException {
        IProductCmptTypeAssociation[] relations = getAssociations();
        for (int i = 0; i < relations.length; i++) {
            String qualifiedName = relations[i].getTarget();
            qualifiedNameTypes.add(new QualifiedNameType(qualifiedName, IpsObjectType.PRODUCT_CMPT_TYPE_V2));
        }
    }


    class RelationFinder extends ProductCmptTypeHierarchyVisitor {

        private String relationName;
        private IProductCmptTypeAssociation relation = null;
        
        public RelationFinder(IIpsProject project, String relationName) {
            super(project);
            this.relationName = relationName;
        }

        /**
         * {@inheritDoc}
         */
        protected boolean visit(IProductCmptType currentType) {
            relation = currentType.getAssociation(relationName);
            return relation==null;
        }
        
    }

    class TableStructureUsageFinder extends ProductCmptTypeHierarchyVisitor {

        private String tsuName;
        private ITableStructureUsage tsu = null;
        
        public TableStructureUsageFinder(IIpsProject project, String tsuName) {
            super(project);
            this.tsuName = tsuName;
        }

        /**
         * {@inheritDoc}
         */
        protected boolean visit(IProductCmptType currentType) {
            tsu = currentType.getTableStructureUsage(tsuName);
            return tsu==null;
        }
        
    }

    class PolicyCmptTypeFinder extends ProductCmptTypeHierarchyVisitor {

        private IPolicyCmptType policyCmptType = null;
        
        public PolicyCmptTypeFinder(IIpsProject ipsProject) {
            super(ipsProject);
        }

        /**
         * {@inheritDoc}
         */
        protected boolean visit(IProductCmptType currentType) throws CoreException {
            policyCmptType = ipsProject.findPolicyCmptType(currentType.getPolicyCmptType());
            return !currentType.isConfigurationForPolicyCmptType();
        }
        
    }
    
    class FormulaSignatureFinder extends ProductCmptTypeHierarchyVisitor {

        private String formulaName;
        private IProductCmptTypeMethod method;
        
        public FormulaSignatureFinder(IIpsProject ipsProject, String formulaName) {
            super(ipsProject);
            this.formulaName = formulaName;
        }

        /**
         * {@inheritDoc}
         */
        protected boolean visit(IProductCmptType currentType) throws CoreException {
            method = currentType.getFormulaSignature(formulaName);
            return method==null;
        }
        
    }
}
