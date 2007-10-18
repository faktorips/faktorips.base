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

package org.faktorips.devtools.core.internal.model.productcmpttype;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.IpsObjectPartCollection;
import org.faktorips.devtools.core.internal.model.type.Type;
import org.faktorips.devtools.core.model.Dependency;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.QualifiedNameType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProdDefProperty;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.productcmpttype.ProdDefPropertyType;
import org.faktorips.devtools.core.model.productcmpttype.ProductCmptTypeHierarchyVisitor;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.values.EnumValue;
import org.w3c.dom.Element;

/**
 * Implementation of IProductCmptType.
 * 
 * @author Jan Ortmann
 */
public class ProductCmptType extends Type implements IProductCmptType {

    private String policyCmptType = "";
    
    private IpsObjectPartCollection tableStructureUsages = new IpsObjectPartCollection(this, TableStructureUsage.class, ITableStructureUsage.class, "TableStructureUsage");
    
    public ProductCmptType(IIpsSrcFile file) {
        super(file);
    }

    /**
     * {@inheritDoc}
     */
    protected IpsObjectPartCollection createCollectionForAttributes() {
        return new IpsObjectPartCollection(this, ProductCmptTypeAttribute.class, IProductCmptTypeAttribute.class, ProductCmptTypeAttribute.TAG_NAME);
    }

    /**
     * {@inheritDoc}
     */
    protected IpsObjectPartCollection createCollectionForMethods() {
        return new IpsObjectPartCollection(this, ProductCmptTypeMethod.class, IProductCmptTypeMethod.class, "Method");
    }
    
    /**
     * {@inheritDoc}
     */
    protected IpsObjectPartCollection createCollectionForAssociations() {
        return new IpsObjectPartCollection(this, ProductCmptTypeAssociation.class, IProductCmptTypeAssociation.class, "Association");
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
        return null;  
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
    public IProdDefProperty[] findProdDefProperties(IIpsProject ipsProject) throws CoreException {
        ProdDefPropertyCollector collector = new ProdDefPropertyCollector(null, ipsProject);
        collector.start(this);
        return collector.getProperties();
    }
    
    /**
     * Returns a  map containing the property names as keys and the properties as values.
     * This method searched the supertype hierarchy.
     * <p>
     * Note this is a model internal method, it is not part of the published interface.
     * 
     * @param propertyType The type of properties that should be included in the map. <code>null</code> indicates
     *                     that all properties should be included in the map.
     */
    public Map getProdDefPropertiesMap(ProdDefPropertyType propertyType, IIpsProject ipsProject) throws CoreException {
        ProdDefPropertyCollector collector = new ProdDefPropertyCollector(propertyType, ipsProject);
        collector.start(this);
        return collector.getPropertyMap();
    }
    

    /**
     * {@inheritDoc}
     */
    public IProdDefProperty findProdDefProperty(String propName, IIpsProject ipsProject) throws CoreException {
        EnumValue[] types = ProdDefPropertyType.enumType.getValues();
        for (int i = 0; i < types.length; i++) {
            IProdDefProperty prop = findProdDefProperty((ProdDefPropertyType)types[i], propName, ipsProject);
            if (prop!=null) {
                return prop;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public IProdDefProperty findProdDefProperty(ProdDefPropertyType type, String propName, IIpsProject ipsProject) throws CoreException {
        if (ProdDefPropertyType.VALUE==type) {
            return findProductCmptTypeAttribute(propName, ipsProject);
        }
        if (ProdDefPropertyType.FORMULA==type) {
            return findFormulaSignature(propName, ipsProject);
        }
        if (ProdDefPropertyType.TABLE_CONTENT_USAGE==type) {
            return findTableStructureUsage(propName, ipsProject);
        }
        if (ProdDefPropertyType.DEFAULT_VALUE_AND_VALUESET!=type) {
            throw new RuntimeException("Unknown type " + type);
        }
        IPolicyCmptType policyCmptType = findPolicyCmptType(true, ipsProject);
        return policyCmptType.findAttributeInSupertypeHierarchy(propName);
    }

    /**
     * {@inheritDoc}
     */
    public IProductCmptTypeAttribute newProductCmptTypeAttribute() {
        return (IProductCmptTypeAttribute)newAttribute();
    }
    
    /**
     * {@inheritDoc}
     */
    public IProductCmptTypeAttribute newProductCmptTypeAttribute(String name) {
        IProductCmptTypeAttribute newAttribute = newProductCmptTypeAttribute();
        newAttribute.setName(name);
        return newAttribute;
    }

    /**
     * {@inheritDoc}
     */
    public IProductCmptTypeAttribute getProductCmptTypeAttribute(String name) {
        return (IProductCmptTypeAttribute)attributes.getPartByName(name);
    }
    
    /**
     * {@inheritDoc}
     */
    public IProductCmptTypeAttribute findProductCmptTypeAttribute(String name, IIpsProject ipsProject) throws CoreException {
        return (IProductCmptTypeAttribute)findAttribute(name, ipsProject);
    }

    /**
     * {@inheritDoc}
     */
    public IProductCmptTypeAttribute[] getProductCmptTypeAttributes() {
        return (IProductCmptTypeAttribute[])attributes.toArray(new IProductCmptTypeAttribute[attributes.size()]);
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
    public IProductCmptTypeAssociation newProductCmptTypeAssociation() {
        return (IProductCmptTypeAssociation)newAssociation();
    }

    /**
     * {@inheritDoc}
     */
    public ITableStructureUsage findTableStructureUsage(String roleName, IIpsProject project) throws CoreException {
        TableStructureUsageFinder finder = new TableStructureUsageFinder(project, roleName);
        finder.start(this);
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
    public IProductCmptTypeMethod newFormulaSignature(String formulaName) {
        IProductCmptTypeMethod signature = newProductCmptTypeMethod();
        signature.setFormulaSignatureDefinition(true);
        signature.setFormulaName(formulaName);
        return signature;
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
    public IProductCmptTypeMethod findFormulaSignature(String formulaName, IIpsProject ipsProject) throws CoreException {
        FormulaSignatureFinder finder = new FormulaSignatureFinder(ipsProject, formulaName);
        finder.start(this);
        return finder.method;
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
    
    public Dependency[] dependsOn() throws CoreException {
        Set dependencies = new HashSet();
        dependencies.clear();
        super.dependsOn(dependencies);
        if (hasSupertype()) {
            dependencies.add(Dependency.createSubtypeDependency(this.getQualifiedNameType(), new QualifiedNameType(getSupertype(),
                    IpsObjectType.PRODUCT_CMPT_TYPE_V2)));
        }
        addQualifiedNameTypesForRelationTargets(dependencies);
        return (Dependency[])dependencies.toArray(new Dependency[dependencies.size()]);
    }

    private void addQualifiedNameTypesForRelationTargets(Set dependencies) throws CoreException {
        IAssociation[] associations = getAssociations();
        for (int i = 0; i < associations.length; i++) {
            String qualifiedName = associations[i].getTarget();
            dependencies.add(Dependency.createReferenceDependency(this.getQualifiedNameType(), new QualifiedNameType(qualifiedName,
                    IpsObjectType.PRODUCT_CMPT_TYPE_V2)));
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

    class ProdDefPropertyCollector extends ProductCmptTypeHierarchyVisitor {

        // if set, indicates the type of properties that are collected
        // if null, all properties are collected
        private ProdDefPropertyType propertyType;
        
        private List myAttributes = new ArrayList();
        private List myTableStructureUsages = new ArrayList();
        private List myFormulaSignatures = new ArrayList();
        private List myPolicyCmptTypeAttributes = new ArrayList();
        
        public ProdDefPropertyCollector(ProdDefPropertyType propertyType, IIpsProject ipsProject) {
            super(ipsProject);
            this.propertyType = propertyType;
        }

        /**
         * {@inheritDoc}
         */
        protected boolean visit(IProductCmptType currentType) throws CoreException {
            ProductCmptType currType = (ProductCmptType)currentType;
            if (propertyType==null || ProdDefPropertyType.VALUE.equals(propertyType)) {
                addInReverseOrder(currType.attributes, myAttributes);
            }
            if (propertyType==null || ProdDefPropertyType.TABLE_CONTENT_USAGE.equals(propertyType)) {
                addInReverseOrder(currType.tableStructureUsages, myTableStructureUsages);
            }
            if (propertyType==null || ProdDefPropertyType.FORMULA.equals(propertyType)) {
                for (int i=currType.methods.size()-1; i>=0; i--) {
                    IProductCmptTypeMethod method = (IProductCmptTypeMethod)currType.methods.getPart(i);
                    if (method.isFormulaSignatureDefinition()) {
                        myFormulaSignatures.add(method);
                    }
                }
            }
            if (propertyType==null || ProdDefPropertyType.DEFAULT_VALUE_AND_VALUESET.equals(propertyType)) {
                IPolicyCmptType policyCmptType = currentType.findPolicyCmptType(false, ipsProject);
                if (policyCmptType==null) {
                    return true;
                }
                org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute[] polAttr = policyCmptType.getPolicyCmptTypeAttributes();
                for (int i = polAttr.length-1; i>=0; i--) {
                    if (polAttr[i].isProductRelevant() && polAttr[i].isChangeable()) {
                        myPolicyCmptTypeAttributes.add(polAttr[i]);
                    }
                }
            }
            return true;
        }
        
        private void addInReverseOrder(IpsObjectPartCollection source, Collection target) {
            int size = source.size();
            for (int i = size-1; i>=0; i--) {
                target.add(source.getPart(i));
            }
        }
        
        public IProdDefProperty[] getProperties() {
            int size = size();
            IProdDefProperty[] props = new IProdDefProperty[size];
            int counter = 0;
            for (int i=myAttributes.size()-1; i>=0; i--) {
                props[counter++] = (IProdDefProperty)myAttributes.get(i);
            }
            for (int i=myTableStructureUsages.size()-1; i>=0; i--) {
                props[counter++] = (IProdDefProperty)myTableStructureUsages.get(i);
            }
            for (int i=myFormulaSignatures.size()-1; i>=0; i--) {
                props[counter++] = (IProdDefProperty)myFormulaSignatures.get(i);
            }
            for (int i=myPolicyCmptTypeAttributes.size()-1; i>=0; i--) {
                props[counter++] = (IProdDefProperty)myPolicyCmptTypeAttributes.get(i);
            }
            return props;
        }

        public Map getPropertyMap() {
            Map propertyMap = new HashMap(size());
            add(propertyMap, myAttributes);
            add(propertyMap, myTableStructureUsages);
            add(propertyMap, myFormulaSignatures);
            add(propertyMap, myPolicyCmptTypeAttributes);
            return propertyMap;
        }
        
        private void add(Map propertyMap, List propertyList) {
            for (Iterator it = propertyList.iterator(); it.hasNext();) {
                IProdDefProperty property = (IProdDefProperty)it.next();
                propertyMap.put(property.getPropertyName(), property);
            }
        }
        
        private int size() {
            return myAttributes.size() + myTableStructureUsages.size() + myFormulaSignatures.size() + myPolicyCmptTypeAttributes.size();
        }
        
    }

}
