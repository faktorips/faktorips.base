/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpttype;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.enums.EnumValue;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPartCollection;
import org.faktorips.devtools.core.internal.model.type.DuplicatePropertyNameValidator;
import org.faktorips.devtools.core.internal.model.type.Type;
import org.faktorips.devtools.core.model.IDependency;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IpsObjectDependency;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.FormulaSignatureFinder;
import org.faktorips.devtools.core.model.productcmpttype.IProdDefProperty;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.productcmpttype.ProdDefPropertyType;
import org.faktorips.devtools.core.model.productcmpttype.ProductCmptTypeHierarchyVisitor;
import org.faktorips.devtools.core.model.productcmpttype.ProductCmptTypeValidations;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.type.TypeValidations;
import org.faktorips.devtools.core.util.TreeSetHelper;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;
import org.w3c.dom.Element;

/**
 * Implementation of IProductCmptType.
 * 
 * @author Jan Ortmann
 */
public class ProductCmptType extends Type implements IProductCmptType {

    private boolean configurationForPolicyCmptType = true;
    private String policyCmptType = ""; //$NON-NLS-1$
    
    private IpsObjectPartCollection tableStructureUsages = new IpsObjectPartCollection(this, TableStructureUsage.class, ITableStructureUsage.class, "TableStructureUsage"); //$NON-NLS-1$
    
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
        return new IpsObjectPartCollection(this, ProductCmptTypeMethod.class, IProductCmptTypeMethod.class, "Method"); //$NON-NLS-1$
    }
    
    /**
     * {@inheritDoc}
     */
    protected IpsObjectPartCollection createCollectionForAssociations() {
        return new IpsObjectPartCollection(this, ProductCmptTypeAssociation.class, IProductCmptTypeAssociation.class, "Association"); //$NON-NLS-1$
    }
    
    protected Iterator getIteratorForTableStructureUsages() {
        return tableStructureUsages.iterator();
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
        return IpsObjectType.PRODUCT_CMPT_TYPE;
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
        return configurationForPolicyCmptType;
    }
    
    /**
     * {@inheritDoc}
     */
    public void setConfigurationForPolicyCmptType(boolean newValue) {
        boolean oldValue = configurationForPolicyCmptType;
        configurationForPolicyCmptType = newValue;
        if (!newValue && oldValue) {
            policyCmptType = ""; //$NON-NLS-1$
        }
        valueChanged(oldValue, newValue);
    }

    /**
     * {@inheritDoc}
     */
    public IPolicyCmptType findPolicyCmptType(IIpsProject ipsProject) throws CoreException {
        if (!configurationForPolicyCmptType) {
            return null;
        }
        return ipsProject.findPolicyCmptType(policyCmptType);
    }
    
    /**
     * {@inheritDoc}
     */
    public IProductCmptType findSuperProductCmptType(IIpsProject project) throws CoreException {
        return (IProductCmptType)project.findIpsObject(IpsObjectType.PRODUCT_CMPT_TYPE, getSupertype());
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
     * Returns a map containing the property names as keys and the properties as values. This method
     * searches the supertype hierarchy.
     * <p>
     * Note this is a model internal method, it is not part of the published interface.
     * 
     * @param propertyType The type of properties that should be included in the map.
     *            <code>null</code> indicates that all properties should be included in the map.
     */
    public LinkedHashMap getProdDefPropertiesMap(ProdDefPropertyType propertyType, IIpsProject ipsProject)
            throws CoreException {
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
            throw new RuntimeException("Unknown type " + type); //$NON-NLS-1$
        }
        IPolicyCmptType policyCmptType = findPolicyCmptType(ipsProject);
        if(policyCmptType == null){
            return null;
        }
        IPolicyCmptTypeAttribute attr = policyCmptType.findPolicyCmptTypeAttribute(propName, ipsProject);
        if (attr!=null && attr.isProductRelevant()) {
            return attr;
        }
        return null;
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
        configurationForPolicyCmptType = Boolean.valueOf(element.getAttribute(PROPERTY_CONFIGURATION_FOR_POLICY_CMPT_TYPE)).booleanValue();
    }

    /**
     * {@inheritDoc}
     */
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_CONFIGURATION_FOR_POLICY_CMPT_TYPE, "" + configurationForPolicyCmptType); //$NON-NLS-1$
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
    public IProductCmptTypeAssociation[] getProductCmptTypeAssociations() {
        return (IProductCmptTypeAssociation[])associations.toArray(new IProductCmptTypeAssociation[associations.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public IProductCmptTypeMethod[] getNonFormulaProductCmptTypeMethods() {

        ArrayList result = new ArrayList();
        for (Iterator it = methods.iterator(); it.hasNext();) {
            IProductCmptTypeMethod method = (IProductCmptTypeMethod)it.next();
            if(!method.isFormulaSignatureDefinition()){
                result.add(method);
            }
        }
        return (IProductCmptTypeMethod[])result.toArray(new IProductCmptTypeMethod[result.size()]);
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
        signature.setName(signature.getDefaultMethodName());
        return signature;
    }

    /**
     * {@inheritDoc}
     */
    public IProductCmptTypeMethod[] findSignaturesOfOverloadedFormulas(IIpsProject ipsProject) throws CoreException{
        ArrayList overloadedMethods = new ArrayList();
        for (Iterator it=methods.iterator(); it.hasNext(); ) {
            IProductCmptTypeMethod method = (IProductCmptTypeMethod)it.next();
            if (method.isFormulaSignatureDefinition() && method.isOverloadsFormula()) {
                IProductCmptTypeMethod overloadedMethod = method.findOverloadedFormulaMethod(ipsProject);
                if(overloadedMethod != null){
                    overloadedMethods.add(overloadedMethod);
                }
            }
        }
        return (IProductCmptTypeMethod[])overloadedMethods.toArray(new IProductCmptTypeMethod[overloadedMethods.size()]);
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

    public IProductCmptTypeMethod[] getFormulaSignatures() {

        ArrayList result = new ArrayList();
        for (Iterator it=methods.iterator(); it.hasNext(); ) {
            IProductCmptTypeMethod method = (IProductCmptTypeMethod)it.next();
            if (method.isFormulaSignatureDefinition()) {
                result.add(method);
            }
        }
        return (IProductCmptTypeMethod[])result.toArray(new IProductCmptTypeMethod[result.size()]);
    }
    
    /**
     * {@inheritDoc}
     */
    public IProductCmptTypeMethod findFormulaSignature(String formulaName, IIpsProject ipsProject) throws CoreException {
        FormulaSignatureFinder finder = new FormulaSignatureFinder(ipsProject, formulaName, true);
        finder.start(this);
        return (IProductCmptTypeMethod)(finder.getMethods().size() != 0 ? finder.getMethods().get(0) : null);  
    }

    /**
     * {@inheritDoc}
     */
    public IMethod[] findOverrideMethodCandidates(boolean onlyNotImplementedAbstractMethods, IIpsProject ipsProject) throws CoreException {
        IMethod[] candidates = super.findOverrideMethodCandidates(onlyNotImplementedAbstractMethods, ipsProject);
        List overloadedMethods = Arrays.asList(findSignaturesOfOverloadedFormulas(ipsProject));
        ArrayList result = new ArrayList(candidates.length);
        for (int i = 0; i < candidates.length; i++) {
            if(overloadedMethods.contains(candidates[i])){
                continue;
            }
            result.add(candidates[i]);
        }
        return (IMethod[])result.toArray(new IMethod[result.size()]);
    }

    
    /**
     * {@inheritDoc}
     */
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);
        IProductCmptType supertype = findSuperProductCmptType(ipsProject);
        if (isConfigurationForPolicyCmptType()) {
            validatePolicyCmptTypeReference(supertype, ipsProject, list);
        } else {
            if (supertype!=null && supertype.isConfigurationForPolicyCmptType()) {
                String text = Messages.ProductCmptType_TypeMustConfigureAPolicyCmptTypeIfSupertypeDoes;
                list.add(new Message(IProductCmptType.MSGCODE_MUST_HAVE_SAME_VALUE_FOR_CONFIGURES_POLICY_CMPT_TYPE, text, Message.ERROR, this, IProductCmptType.PROPERTY_CONFIGURATION_FOR_POLICY_CMPT_TYPE));
            }
        }
        validateProductCmptTypeAbstractWhenPolicyCmptTypeAbstract(list, ipsProject);
        validateIfAnOverrideOfOverloadedFormulaExists(list, ipsProject);
        list.add(TypeValidations.validateOtherTypeWithSameNameTypeInIpsObjectPath(IpsObjectType.POLICY_CMPT_TYPE, getQualifiedName(), ipsProject, this));
    }

    //TODO pk: write test case
    private void validateIfAnOverrideOfOverloadedFormulaExists(MessageList msgList, IIpsProject ipsProject) throws CoreException{
        ArrayList overloadedSupertypeFormulaSignatures = new ArrayList();
        IProductCmptTypeMethod[] formulaSignatures = getFormulaSignatures();
        for (int i = 0; i < formulaSignatures.length; i++) {
            if(formulaSignatures[i].isOverloadsFormula()){
                IProductCmptTypeMethod method = formulaSignatures[i].findOverloadedFormulaMethod(ipsProject);
                if(method != null){
                    overloadedSupertypeFormulaSignatures.add(method);
                }
            }
        }
        
        IProductCmptTypeMethod[] nonFormulas = getNonFormulaProductCmptTypeMethods();
        for (Iterator it = overloadedSupertypeFormulaSignatures.iterator(); it.hasNext();) {
            IProductCmptTypeMethod overloadedMethod = (IProductCmptTypeMethod)it.next();
            for (int i = 0; i < nonFormulas.length; i++) {
                if(nonFormulas[i].isSameSignature(overloadedMethod)){
                    String text = NLS.bind(Messages.ProductCmptType_msgOverloadedFormulaMethodCannotBeOverridden, overloadedMethod.getFormulaName());
                    msgList.add(new Message(MSGCODE_OVERLOADED_FORMULA_CANNOT_BE_OVERRIDDEN, text, Message.ERROR, nonFormulas[i], IProductCmptTypeMethod.PROPERTY_NAME));
                }
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    protected DuplicatePropertyNameValidator createDuplicatePropertyNameValidator(IIpsProject ipsProject) {
        return new ProductCmptTypeDuplicatePropertyNameValidator(ipsProject);
    }

    private void validateProductCmptTypeAbstractWhenPolicyCmptTypeAbstract(MessageList msgList, IIpsProject ipsProject)
            throws CoreException {
        if (StringUtils.isEmpty(getPolicyCmptType())) {
            return;
        }
        IPolicyCmptType policyCmptType = findPolicyCmptType(ipsProject);
        if (policyCmptType != null) {
            msgList.add(ProductCmptTypeValidations.validateProductCmptTypeAbstractWhenPolicyCmptTypeAbstract(policyCmptType
                    .isAbstract(), isAbstract(), this));
        }
    }
    
    private void validatePolicyCmptTypeReference(IProductCmptType supertype, IIpsProject ipsProject, MessageList list) throws CoreException {
        IPolicyCmptType policyCmptTypeObj = findPolicyCmptType(ipsProject);
        if (policyCmptTypeObj==null) {
            String text = NLS.bind(Messages.ProductCmptType_PolicyCmptTypeDoesNotExist, policyCmptType);
            list.add(new Message(MSGCODE_POLICY_CMPT_TYPE_DOES_NOT_EXIST, text, Message.ERROR, this, PROPERTY_POLICY_CMPT_TYPE));
            return;
        }
        if (!policyCmptTypeObj.isConfigurableByProductCmptType()) {
            String text = NLS.bind(Messages.ProductCmptType_notMarkedAsConfigurable, policyCmptType);
            list.add(new Message(MSGCODE_POLICY_CMPT_TYPE_IS_NOT_MARKED_AS_CONFIGURABLE, text, Message.ERROR, this, PROPERTY_POLICY_CMPT_TYPE));
            return;
        }
        if (!this.isSubtypeOrSameType(policyCmptTypeObj.findProductCmptType(ipsProject), ipsProject)) {
            String text = NLS.bind(Messages.ProductCmptType_policyCmptTypeDoesNotSpecifyThisType, policyCmptType);
            list.add(new Message(MSGCODE_POLICY_CMPT_TYPE_DOES_NOT_SPECIFY_THIS_TYPE, text, Message.ERROR, this, PROPERTY_POLICY_CMPT_TYPE));
            return;
        }
        if (supertype==null) {
            return;
        }
        IPolicyCmptType policyCmptTypeOfSupertype = supertype.findPolicyCmptType(ipsProject);
        if (policyCmptTypeObj!=policyCmptTypeOfSupertype && policyCmptTypeObj.findSupertype(ipsProject)!=policyCmptTypeOfSupertype) {
            String text = Messages.ProductCmptType_InconsistentTypeHierarchies;
            list.add(new Message(MSGCODE_HIERARCHY_MISMATCH, text, Message.ERROR, this, new String[]{PROPERTY_SUPERTYPE, PROPERTY_POLICY_CMPT_TYPE}));
            return;
        }
    }
  
    /**
     * {@inheritDoc}
     */
    public IDependency[] dependsOn() throws CoreException {
        Set dependencies = new HashSet();
        if(!StringUtils.isEmpty(getPolicyCmptType())){
            dependencies.add(IpsObjectDependency.createReferenceDependency(getQualifiedNameType(), new QualifiedNameType(getPolicyCmptType(), IpsObjectType.POLICY_CMPT_TYPE)));
        }
//      to force a check is a policy component type exists with the same qualified name
        dependencies.add(IpsObjectDependency.createReferenceDependency(getQualifiedNameType(), new QualifiedNameType(getQualifiedName(), IpsObjectType.POLICY_CMPT_TYPE)));
        dependsOn(dependencies);
        return (IDependency[])dependencies.toArray(new IDependency[dependencies.size()]);
    }
    

    private static class TableStructureUsageFinder extends ProductCmptTypeHierarchyVisitor {

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

    
    private static class ProdDefPropertyCollector extends ProductCmptTypeHierarchyVisitor {

        // if set, indicates the type of properties that are collected
        // if null, all properties are collected
        private ProdDefPropertyType propertyType;
        
        private List myAttributes = new ArrayList();
        private List myTableStructureUsages = new ArrayList();
        private List myFormulaSignatures = new ArrayList();
        private List myPolicyCmptTypeAttributes = new ArrayList();
        
        private Set visitedPolicyCmptTypes = new HashSet();
        
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
                IPolicyCmptType policyCmptType = currentType.findPolicyCmptType(ipsProject);
                if (policyCmptType==null || visitedPolicyCmptTypes.contains(policyCmptType)) {
                    return true;
                }
                visitedPolicyCmptTypes.add(policyCmptType);
                IPolicyCmptTypeAttribute[] polAttr = policyCmptType.getPolicyCmptTypeAttributes();
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

        public LinkedHashMap getPropertyMap() {
            LinkedHashMap propertyMap = new LinkedHashMap(size());
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

    
    private static class ProductCmptTypeDuplicatePropertyNameValidator extends DuplicatePropertyNameValidator {

        public ProductCmptTypeDuplicatePropertyNameValidator(IIpsProject ipsProject) {
            super(ipsProject);
        }

        protected Message createMessage(String propertyName, ObjectProperty[] invalidObjProperties){
            //test if only formulas are involved
            boolean onlyFormulas = true;
            boolean onlyFormulasInSameType = true;
            IProductCmptType prodType = null;
            for (int i = 0; i < invalidObjProperties.length; i++) {
                Object obj = invalidObjProperties[i].getObject();
                if(!(obj instanceof IProductCmptTypeMethod)){
                    onlyFormulas = false;
                    onlyFormulasInSameType = false;
                    break;
                } else {
                    if(prodType == null){
                        prodType = ((IProductCmptTypeMethod)obj).getProductCmptType();
                        onlyFormulasInSameType = true;
                    }
                    if(onlyFormulasInSameType && !prodType.equals(((IProductCmptTypeMethod)obj).getProductCmptType())){
                        onlyFormulasInSameType = false;
                    }
                }
            }
            if(onlyFormulasInSameType){
                String text = Messages.ProductCmptType_msgDuplicateFormulasNotAllowedInSameType;
                return new Message(MSGCODE_DUPLICATE_FORMULAS_NOT_ALLOWED_IN_SAME_TYPE, text, Message.ERROR, invalidObjProperties);
            }
            else if(onlyFormulas){
                String text = NLS.bind(Messages.ProductCmptType_DuplicateFormulaName, propertyName);
                return new Message(MSGCODE_DUPLICATE_FORMULA_NAME_IN_HIERARCHY, text, Message.ERROR, invalidObjProperties);
            } else {
                String text = NLS.bind(Messages.ProductCmptType_multiplePropertyNames, propertyName);
                return new Message(IType.MSGCODE_DUPLICATE_PROPERTY_NAME, text, Message.ERROR, invalidObjProperties);
            }
        }

        /**
         * {@inheritDoc}
         */
        protected boolean visit(IType currentType) throws CoreException {
            super.visit(currentType);
            ProductCmptType productCmptType = (ProductCmptType)currentType;
            for (Iterator it=productCmptType.getIteratorForTableStructureUsages(); it.hasNext(); ) {
                ITableStructureUsage tsu = (ITableStructureUsage)it.next();
                add(tsu.getRoleName(), new ObjectProperty(tsu, ITableStructureUsage.PROPERTY_ROLENAME));
            }
            for (Iterator it = productCmptType.getIteratorForMethods(); it.hasNext();) {
                IProductCmptTypeMethod method = (IProductCmptTypeMethod)it.next();
                if (method.isFormulaSignatureDefinition() && 
                        StringUtils.isNotEmpty(method.getFormulaName()) && 
                        !method.isOverloadsFormula()) {
                    add(method.getFormulaName(), new ObjectProperty(method, IProductCmptTypeMethod.PROPERTY_FORMULA_NAME));
                }
            }
            return true;
        }        
    }


	/* (non-Javadoc)
	 * @see org.faktorips.devtools.core.model.IIpsMetaClass#findAllMetaObjects(org.faktorips.devtools.core.model.ipsproject.IIpsProject, boolean)
	 */
    /**
     * {@inheritDoc}
     */
	public IIpsSrcFile[] findAllMetaObjectSrcFiles(IIpsProject ipsProject,
			boolean includeSubtypes) throws CoreException {
		TreeSet<IIpsSrcFile> result = TreeSetHelper.newIpsSrcFileTreeSet();
		IIpsProject[] searchProjects = ipsProject.getReferencingProjectLeavesOrSelf();
		for (IIpsProject project : searchProjects) {
			result.addAll(Arrays.asList(project.findAllProductCmptSrcFiles(this, includeSubtypes)));
		}
		return result.toArray(new IIpsSrcFile[result.size()]);
	}
}
