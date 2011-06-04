/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpttype;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPartCollection;
import org.faktorips.devtools.core.internal.model.type.DuplicatePropertyNameValidator;
import org.faktorips.devtools.core.internal.model.type.Method;
import org.faktorips.devtools.core.internal.model.type.Type;
import org.faktorips.devtools.core.model.IDependency;
import org.faktorips.devtools.core.model.IDependencyDetail;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IpsObjectDependency;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpttype.FormulaSignatureFinder;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.productcmpttype.ProductCmptTypeValidations;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;
import org.faktorips.devtools.core.model.type.TypeHierarchyVisitor;
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
    private String instancesIconPath = null;

    private final IpsObjectPartCollection<IProductCmptTypeAttribute> attributes;
    private final IpsObjectPartCollection<ITableStructureUsage> tableStructureUsages;
    private final IpsObjectPartCollection<IProductCmptTypeMethod> methods;
    private final IpsObjectPartCollection<IProductCmptTypeAssociation> associations;

    public ProductCmptType(IIpsSrcFile file) {
        super(file);
        tableStructureUsages = new IpsObjectPartCollection<ITableStructureUsage>(this, TableStructureUsage.class,
                ITableStructureUsage.class, TableStructureUsage.TAG_NAME);
        attributes = new IpsObjectPartCollection<IProductCmptTypeAttribute>(this, ProductCmptTypeAttribute.class,
                IProductCmptTypeAttribute.class, ProductCmptTypeAttribute.TAG_NAME);
        methods = new IpsObjectPartCollection<IProductCmptTypeMethod>(this, ProductCmptTypeMethod.class,
                IProductCmptTypeMethod.class, Method.XML_ELEMENT_NAME);
        associations = new IpsObjectPartCollection<IProductCmptTypeAssociation>(this, ProductCmptTypeAssociation.class,
                IProductCmptTypeAssociation.class, ProductCmptTypeAssociation.TAG_NAME);
    }

    @Override
    protected IpsObjectPartCollection<IProductCmptTypeAttribute> getAttributesPartCollection() {
        return attributes;
    }

    @Override
    protected IpsObjectPartCollection<IProductCmptTypeAssociation> getAssociationPartCollection() {
        return associations;
    }

    @Override
    protected IpsObjectPartCollection<IProductCmptTypeMethod> getMethodPartCollection() {
        return methods;
    }

    @Override
    public IType findSupertype(IIpsProject project) throws CoreException {
        if (!hasSupertype()) {
            return null;
        }
        IProductCmptType supertype = findSuperProductCmptType(project);
        if (supertype != null) {
            return supertype;
        }
        return null;
    }

    @Override
    public IpsObjectType getIpsObjectType() {
        return IpsObjectType.PRODUCT_CMPT_TYPE;
    }

    @Override
    public String getPolicyCmptType() {
        return policyCmptType;
    }

    @Override
    public void setPolicyCmptType(String newType) {
        String oldType = policyCmptType;
        policyCmptType = newType;
        valueChanged(oldType, newType);
    }

    @Override
    public boolean isConfigurationForPolicyCmptType() {
        return configurationForPolicyCmptType;
    }

    @Override
    public void setConfigurationForPolicyCmptType(boolean newValue) {
        boolean oldValue = configurationForPolicyCmptType;
        configurationForPolicyCmptType = newValue;
        if (!newValue && oldValue) {
            policyCmptType = ""; //$NON-NLS-1$
        }
        valueChanged(oldValue, newValue);
    }

    @Override
    public IPolicyCmptType findPolicyCmptType(IIpsProject ipsProject) throws CoreException {
        if (!configurationForPolicyCmptType) {
            return null;
        }
        return ipsProject.findPolicyCmptType(policyCmptType);
    }

    @Override
    public IProductCmptType findSuperProductCmptType(IIpsProject project) throws CoreException {
        return (IProductCmptType)project.findIpsObject(IpsObjectType.PRODUCT_CMPT_TYPE, getSupertype());
    }

    @Override
    public List<IProductCmptProperty> findProductCmptProperties(IIpsProject ipsProject) throws CoreException {
        ProductCmptPropertyCollector collector = new ProductCmptPropertyCollector(null, ipsProject);
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
    public LinkedHashMap<String, IProductCmptProperty> getProductCpmtPropertyMap(ProductCmptPropertyType propertyType,
            IIpsProject ipsProject) throws CoreException {

        ProductCmptPropertyCollector collector = new ProductCmptPropertyCollector(propertyType, ipsProject);
        collector.start(this);
        return collector.getPropertyMap();
    }

    @Override
    public IProductCmptProperty findProductCmptProperty(String propName, IIpsProject ipsProject) throws CoreException {
        for (ProductCmptPropertyType type : ProductCmptPropertyType.values()) {
            IProductCmptProperty prop = findProductCmptProperty(type, propName, ipsProject);
            if (prop != null) {
                return prop;
            }
        }
        return null;
    }

    @Override
    public IProductCmptProperty findProductCmptProperty(ProductCmptPropertyType type,
            String propName,
            IIpsProject ipsProject) throws CoreException {
        if (ProductCmptPropertyType.VALUE == type) {
            return findProductCmptTypeAttribute(propName, ipsProject);
        }
        if (ProductCmptPropertyType.FORMULA == type) {
            return findFormulaSignature(propName, ipsProject);
        }
        if (ProductCmptPropertyType.TABLE_CONTENT_USAGE == type) {
            return findTableStructureUsage(propName, ipsProject);
        }
        IPolicyCmptType policyCmptType = findPolicyCmptType(ipsProject);
        if (policyCmptType == null) {
            return null;
        }
        if (ProductCmptPropertyType.VALIDATION_RULE_CONFIG == type) {
            IValidationRule rule = policyCmptType.findValidationRule(propName, ipsProject);
            if (rule != null && rule.isConfigurableByProductComponent()) {
                return rule;
            }
        }
        if (ProductCmptPropertyType.DEFAULT_VALUE_AND_VALUESET == type) {
            IPolicyCmptTypeAttribute attr = policyCmptType.findPolicyCmptTypeAttribute(propName, ipsProject);
            if (attr != null && attr.isProductRelevant()) {
                return attr;
            }
        }
        return null;

    }

    @Override
    public List<IAssociation> findAllNotDerivedAssociations() throws CoreException {
        NotDerivedAssociationCollector collector = new NotDerivedAssociationCollector(getIpsProject());
        collector.start(this);
        return collector.associations;
    }

    @Override
    public IProductCmptTypeAttribute newProductCmptTypeAttribute() {
        return (IProductCmptTypeAttribute)newAttribute();
    }

    @Override
    public IProductCmptTypeAttribute newProductCmptTypeAttribute(String name) {
        IProductCmptTypeAttribute newAttribute = newProductCmptTypeAttribute();
        newAttribute.setName(name);
        return newAttribute;
    }

    @Override
    public IProductCmptTypeAttribute getProductCmptTypeAttribute(String name) {
        return attributes.getPartByName(name);
    }

    @Override
    public IProductCmptTypeAttribute findProductCmptTypeAttribute(String name, IIpsProject ipsProject)
            throws CoreException {

        return (IProductCmptTypeAttribute)findAttribute(name, ipsProject);
    }

    @Override
    public List<IProductCmptTypeAttribute> getProductCmptTypeAttributes() {
        return attributes.asList();
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        policyCmptType = element.getAttribute(PROPERTY_POLICY_CMPT_TYPE);
        configurationForPolicyCmptType = Boolean.valueOf(
                element.getAttribute(PROPERTY_CONFIGURATION_FOR_POLICY_CMPT_TYPE)).booleanValue();
        instancesIconPath = element.getAttribute(PROPERTY_ICON_FOR_INSTANCES);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_CONFIGURATION_FOR_POLICY_CMPT_TYPE,
                String.valueOf(configurationForPolicyCmptType));
        element.setAttribute(PROPERTY_POLICY_CMPT_TYPE, policyCmptType);

        element.setAttribute(PROPERTY_ICON_FOR_INSTANCES, instancesIconPath);
    }

    @Override
    public IProductCmptTypeAssociation newProductCmptTypeAssociation() {
        return (IProductCmptTypeAssociation)newAssociation();
    }

    @Override
    public ITableStructureUsage findTableStructureUsage(String roleName, IIpsProject project) throws CoreException {
        TableStructureUsageFinder finder = new TableStructureUsageFinder(project, roleName);
        finder.start(this);
        return finder.tsu;
    }

    @Override
    public int getNumOfTableStructureUsages() {
        return tableStructureUsages.size();
    }

    @Override
    public ITableStructureUsage getTableStructureUsage(String roleName) {
        return tableStructureUsages.getPartByName(roleName);
    }

    @Override
    public List<ITableStructureUsage> getTableStructureUsages() {
        return tableStructureUsages.asList();
    }

    @Override
    public int[] moveTableStructureUsage(int[] indexes, boolean up) {
        return tableStructureUsages.moveParts(indexes, up);
    }

    @Override
    public ITableStructureUsage newTableStructureUsage() {
        return tableStructureUsages.newPart();
    }

    @Override
    public List<IProductCmptTypeMethod> getProductCmptTypeMethods() {
        return methods.asList();
    }

    @Override
    public List<IProductCmptTypeAssociation> getProductCmptTypeAssociations() {
        return associations.asList();
    }

    @Override
    public List<IProductCmptTypeMethod> getNonFormulaProductCmptTypeMethods() {
        ArrayList<IProductCmptTypeMethod> result = new ArrayList<IProductCmptTypeMethod>();
        for (IMethod method : methods) {
            if (!((IProductCmptTypeMethod)method).isFormulaSignatureDefinition()) {
                result.add((IProductCmptTypeMethod)method);
            }
        }
        return result;
    }

    @Override
    public IProductCmptTypeMethod newProductCmptTypeMethod() {
        return methods.newPart();
    }

    @Override
    public IProductCmptTypeMethod newFormulaSignature(String formulaName) {
        IProductCmptTypeMethod signature = newProductCmptTypeMethod();
        signature.setFormulaSignatureDefinition(true);
        signature.setFormulaName(formulaName);
        signature.setName(signature.getDefaultMethodName());
        return signature;
    }

    @Override
    public List<IProductCmptTypeMethod> findSignaturesOfOverloadedFormulas(IIpsProject ipsProject) throws CoreException {
        ArrayList<IProductCmptTypeMethod> overloadedMethods = new ArrayList<IProductCmptTypeMethod>();
        for (IMethod method2 : methods) {
            IProductCmptTypeMethod method = (IProductCmptTypeMethod)method2;
            if (method.isFormulaSignatureDefinition() && method.isOverloadsFormula()) {
                IProductCmptTypeMethod overloadedMethod = method.findOverloadedFormulaMethod(ipsProject);
                if (overloadedMethod != null) {
                    overloadedMethods.add(overloadedMethod);
                }
            }
        }
        return overloadedMethods;
    }

    @Override
    public IProductCmptTypeMethod getFormulaSignature(String formulaName) throws CoreException {
        if (StringUtils.isEmpty(formulaName)) {
            return null;
        }
        for (IMethod method2 : methods) {
            IProductCmptTypeMethod method = (IProductCmptTypeMethod)method2;
            if (method.isFormulaSignatureDefinition() && formulaName.equalsIgnoreCase(method.getFormulaName())) {
                return method;
            }
        }
        return null;
    }

    @Override
    public List<IProductCmptTypeMethod> getFormulaSignatures() {

        ArrayList<IProductCmptTypeMethod> result = new ArrayList<IProductCmptTypeMethod>();
        for (IMethod method2 : methods) {
            IProductCmptTypeMethod method = (IProductCmptTypeMethod)method2;
            if (method.isFormulaSignatureDefinition()) {
                result.add(method);
            }
        }
        return result;
    }

    @Override
    public IProductCmptTypeMethod findFormulaSignature(String formulaName, IIpsProject ipsProject) throws CoreException {
        FormulaSignatureFinder finder = new FormulaSignatureFinder(ipsProject, formulaName, true);
        finder.start(this);
        return (IProductCmptTypeMethod)(finder.getMethods().size() != 0 ? finder.getMethods().get(0) : null);
    }

    @Override
    public List<IMethod> findOverrideMethodCandidates(boolean onlyNotImplementedAbstractMethods, IIpsProject ipsProject)
            throws CoreException {

        List<IMethod> candidates = super.findOverrideMethodCandidates(onlyNotImplementedAbstractMethods, ipsProject);
        List<IProductCmptTypeMethod> overloadedMethods = findSignaturesOfOverloadedFormulas(ipsProject);
        ArrayList<IMethod> result = new ArrayList<IMethod>(candidates.size());
        for (IMethod candidate : candidates) {
            if (overloadedMethods.contains(candidate)) {
                continue;
            }
            result.add(candidate);
        }
        return result;
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);
        IProductCmptType supertype = findSuperProductCmptType(ipsProject);
        if (isConfigurationForPolicyCmptType()) {
            validatePolicyCmptTypeReference(supertype, ipsProject, list);
        } else {
            if (supertype != null && supertype.isConfigurationForPolicyCmptType()) {
                String text = Messages.ProductCmptType_TypeMustConfigureAPolicyCmptTypeIfSupertypeDoes;
                list.add(new Message(IProductCmptType.MSGCODE_MUST_HAVE_SAME_VALUE_FOR_CONFIGURES_POLICY_CMPT_TYPE,
                        text, Message.ERROR, this, IProductCmptType.PROPERTY_CONFIGURATION_FOR_POLICY_CMPT_TYPE));
            }
        }
        validateProductCmptTypeAbstractWhenPolicyCmptTypeAbstract(list, ipsProject);
        validateIfAnOverrideOfOverloadedFormulaExists(list, ipsProject);
        list.add(TypeValidations.validateOtherTypeWithSameNameTypeInIpsObjectPath(IpsObjectType.POLICY_CMPT_TYPE,
                getQualifiedName(), ipsProject, this));

        validateIconPath(list, ipsProject);
    }

    // TODO pk: write test case
    private void validateIfAnOverrideOfOverloadedFormulaExists(MessageList msgList, IIpsProject ipsProject)
            throws CoreException {

        ArrayList<IProductCmptTypeMethod> overloadedSupertypeFormulaSignatures = new ArrayList<IProductCmptTypeMethod>();
        List<IProductCmptTypeMethod> formulaSignatures = getFormulaSignatures();
        for (IProductCmptTypeMethod formulaSignature : formulaSignatures) {
            if (formulaSignature.isOverloadsFormula()) {
                IProductCmptTypeMethod method = formulaSignature.findOverloadedFormulaMethod(ipsProject);
                if (method != null) {
                    overloadedSupertypeFormulaSignatures.add(method);
                }
            }
        }

        List<IProductCmptTypeMethod> nonFormulas = getNonFormulaProductCmptTypeMethods();
        for (IProductCmptTypeMethod overloadedMethod : overloadedSupertypeFormulaSignatures) {
            for (IProductCmptTypeMethod nonFormula : nonFormulas) {
                if (nonFormula.isSameSignature(overloadedMethod)) {
                    String text = NLS.bind(Messages.ProductCmptType_msgOverloadedFormulaMethodCannotBeOverridden,
                            overloadedMethod.getFormulaName());
                    msgList.add(new Message(MSGCODE_OVERLOADED_FORMULA_CANNOT_BE_OVERRIDDEN, text, Message.ERROR,
                            nonFormula, IIpsElement.PROPERTY_NAME));
                }
            }
        }
    }

    private void validateIconPath(MessageList msgList, IIpsProject ipsProject) throws CoreException {
        if (isUseCustomInstanceIcon()) {
            InputStream stream = ipsProject.getResourceAsStream(getInstancesIcon());
            if (stream == null) {
                String text = Messages.ProductCmptType_iconFileCannotBeResolved + getInstancesIcon() + "\"."; //$NON-NLS-1$
                msgList.add(new Message(MSGCODE_ICON_PATH_INVALID, text, Message.ERROR, this,
                        PROPERTY_ICON_FOR_INSTANCES));
            } else {
                try {
                    stream.close();
                } catch (IOException e) {
                    throw new CoreException(new IpsStatus(e));
                }
            }
        }
    }

    @Override
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
            msgList.add(ProductCmptTypeValidations.validateProductCmptTypeAbstractWhenPolicyCmptTypeAbstract(
                    policyCmptType.isAbstract(), isAbstract(), this));
        }
    }

    private void validatePolicyCmptTypeReference(IProductCmptType supertype, IIpsProject ipsProject, MessageList list)
            throws CoreException {

        IPolicyCmptType policyCmptTypeObj = findPolicyCmptType(ipsProject);
        if (policyCmptTypeObj == null) {
            String text = NLS.bind(Messages.ProductCmptType_PolicyCmptTypeDoesNotExist, policyCmptType);
            list.add(new Message(MSGCODE_POLICY_CMPT_TYPE_DOES_NOT_EXIST, text, Message.ERROR, this,
                    PROPERTY_POLICY_CMPT_TYPE));
            return;
        }
        if (!policyCmptTypeObj.isConfigurableByProductCmptType()) {
            String text = NLS.bind(Messages.ProductCmptType_notMarkedAsConfigurable, policyCmptType);
            list.add(new Message(MSGCODE_POLICY_CMPT_TYPE_IS_NOT_MARKED_AS_CONFIGURABLE, text, Message.ERROR, this,
                    PROPERTY_POLICY_CMPT_TYPE));
            return;
        }
        if (!isSubtypeOrSameType(policyCmptTypeObj.findProductCmptType(ipsProject), ipsProject)) {
            String text = NLS.bind(Messages.ProductCmptType_policyCmptTypeDoesNotSpecifyThisType, policyCmptType);
            list.add(new Message(MSGCODE_POLICY_CMPT_TYPE_DOES_NOT_SPECIFY_THIS_TYPE, text, Message.ERROR, this,
                    PROPERTY_POLICY_CMPT_TYPE));
            return;
        }
        Message msg = ProductCmptTypeValidations.validateSupertype(this, supertype,
                policyCmptTypeObj.getQualifiedName(), policyCmptTypeObj.getSupertype(), ipsProject);
        if (msg != null) {
            list.add(msg);
        }
    }

    @Override
    protected IDependency[] dependsOn(Map<IDependency, List<IDependencyDetail>> details) throws CoreException {
        Set<IDependency> dependencies = new HashSet<IDependency>();
        if (!StringUtils.isEmpty(getPolicyCmptType())) {
            IDependency dependency = IpsObjectDependency.createReferenceDependency(getQualifiedNameType(),
                    new QualifiedNameType(getPolicyCmptType(), IpsObjectType.POLICY_CMPT_TYPE));
            dependencies.add(dependency);
            addDetails(details, dependency, this, PROPERTY_POLICY_CMPT_TYPE);
        }
        // to force a check is a policy component type exists with the same qualified name
        dependencies.add(IpsObjectDependency.createReferenceDependency(getQualifiedNameType(), new QualifiedNameType(
                getQualifiedName(), IpsObjectType.POLICY_CMPT_TYPE)));
        dependsOn(dependencies, details);
        return dependencies.toArray(new IDependency[dependencies.size()]);
    }

    @Override
    public Collection<IIpsSrcFile> searchProductComponents(boolean includeSubtypes) throws CoreException {
        return searchMetaObjectSrcFiles(includeSubtypes);
    }

    @Override
    public Collection<IIpsSrcFile> searchMetaObjectSrcFiles(boolean includeSubtypes) throws CoreException {
        TreeSet<IIpsSrcFile> result = TreeSetHelper.newIpsSrcFileTreeSet();
        IIpsProject[] searchProjects = getIpsProject().findReferencingProjectLeavesOrSelf();
        for (IIpsProject project : searchProjects) {
            result.addAll(Arrays.asList(project.findAllProductCmptSrcFiles(this, includeSubtypes)));
        }
        return result;
    }

    @Override
    public String getInstancesIcon() {
        return instancesIconPath;
    }

    @Override
    public boolean isUseCustomInstanceIcon() {
        return StringUtils.isNotEmpty(instancesIconPath);
    }

    @Override
    public void setInstancesIcon(String path) {
        String oldPath = instancesIconPath;
        instancesIconPath = path;
        valueChanged(oldPath, instancesIconPath);
    }

    @Override
    public String getCaption(Locale locale) throws CoreException {
        return ResourceBundle.getBundle(Messages.BUNDLE_NAME, locale).getString("ProductCmptType_caption"); //$NON-NLS-1$
    }

    private static class TableStructureUsageFinder extends TypeHierarchyVisitor<IProductCmptType> {

        private String tsuName;
        private ITableStructureUsage tsu = null;

        public TableStructureUsageFinder(IIpsProject project, String tsuName) {
            super(project);
            this.tsuName = tsuName;
        }

        @Override
        protected boolean visit(IProductCmptType currentType) {
            tsu = currentType.getTableStructureUsage(tsuName);
            return tsu == null;
        }

    }

    private static class ProductCmptPropertyCollector extends TypeHierarchyVisitor<IProductCmptType> {

        // if set, indicates the type of properties that are collected
        // if null, all properties are collected
        private ProductCmptPropertyType propertyType;

        private List<IProductCmptTypeAttribute> myAttributes = new ArrayList<IProductCmptTypeAttribute>();
        private List<ITableStructureUsage> myTableStructureUsages = new ArrayList<ITableStructureUsage>();
        private List<IProductCmptTypeMethod> myFormulaSignatures = new ArrayList<IProductCmptTypeMethod>();
        private List<IPolicyCmptTypeAttribute> myPolicyCmptTypeAttributes = new ArrayList<IPolicyCmptTypeAttribute>();
        private List<IValidationRule> myValidationRules = new ArrayList<IValidationRule>();

        private Set<IPolicyCmptType> visitedPolicyCmptTypes = new HashSet<IPolicyCmptType>();

        public ProductCmptPropertyCollector(ProductCmptPropertyType propertyType, IIpsProject ipsProject) {
            super(ipsProject);
            this.propertyType = propertyType;
        }

        @Override
        protected boolean visit(IProductCmptType currentType) throws CoreException {
            ProductCmptType currType = (ProductCmptType)currentType;
            if (propertyType == null || ProductCmptPropertyType.VALUE.equals(propertyType)) {
                myAttributes.addAll(0, currType.attributes.asList());
            }
            if (propertyType == null || ProductCmptPropertyType.TABLE_CONTENT_USAGE.equals(propertyType)) {
                myTableStructureUsages.addAll(0, currType.tableStructureUsages.asList());
            }
            if (propertyType == null || ProductCmptPropertyType.FORMULA.equals(propertyType)) {
                List<IProductCmptTypeMethod> methodsToAdd = new ArrayList<IProductCmptTypeMethod>();
                for (IProductCmptTypeMethod method : currType.methods) {
                    if (method.isFormulaSignatureDefinition()) {
                        methodsToAdd.add(method);
                    }
                }
                myFormulaSignatures.addAll(0, methodsToAdd);
            }
            IPolicyCmptType policyCmptType = currentType.findPolicyCmptType(ipsProject);
            if (policyCmptType == null || visitedPolicyCmptTypes.contains(policyCmptType)) {
                return true;
            }
            if (propertyType == null || ProductCmptPropertyType.DEFAULT_VALUE_AND_VALUESET.equals(propertyType)) {
                visitedPolicyCmptTypes.add(policyCmptType);
                List<IPolicyCmptTypeAttribute> attrsToAdd = new ArrayList<IPolicyCmptTypeAttribute>();
                List<IPolicyCmptTypeAttribute> polAttrs = policyCmptType.getPolicyCmptTypeAttributes();
                for (IPolicyCmptTypeAttribute attr : polAttrs) {
                    if (attr.isProductRelevant() && attr.isChangeable()) {
                        attrsToAdd.add(attr);
                    }
                }
                myPolicyCmptTypeAttributes.addAll(0, attrsToAdd);
            }
            if (propertyType == null || ProductCmptPropertyType.VALIDATION_RULE_CONFIG.equals(propertyType)) {
                visitedPolicyCmptTypes.add(policyCmptType);
                List<IValidationRule> rulesToAdd = new ArrayList<IValidationRule>();
                List<IValidationRule> rules = policyCmptType.getValidationRules();
                for (IValidationRule rule : rules) {
                    if (rule.isConfigurableByProductComponent()) {
                        rulesToAdd.add(rule);
                    }
                }
                myValidationRules.addAll(0, rulesToAdd);
            }
            return true;
        }

        public List<IProductCmptProperty> getProperties() {
            List<IProductCmptProperty> props = new ArrayList<IProductCmptProperty>();
            props.addAll(myAttributes);
            props.addAll(myTableStructureUsages);
            props.addAll(myFormulaSignatures);
            props.addAll(myPolicyCmptTypeAttributes);
            props.addAll(myValidationRules);
            return props;
        }

        public LinkedHashMap<String, IProductCmptProperty> getPropertyMap() {
            LinkedHashMap<String, IProductCmptProperty> propertyMap = new LinkedHashMap<String, IProductCmptProperty>(
                    size());
            add(propertyMap, myAttributes);
            add(propertyMap, myTableStructureUsages);
            add(propertyMap, myFormulaSignatures);
            add(propertyMap, myPolicyCmptTypeAttributes);
            add(propertyMap, myValidationRules);
            return propertyMap;
        }

        private void add(Map<String, IProductCmptProperty> propertyMap,
                List<? extends IProductCmptProperty> propertyList) {
            for (IProductCmptProperty property : propertyList) {
                propertyMap.put(property.getPropertyName(), property);
            }
        }

        private int size() {
            return myAttributes.size() + myTableStructureUsages.size() + myFormulaSignatures.size()
                    + myPolicyCmptTypeAttributes.size() + myValidationRules.size();
        }
    }

    private static class ProductCmptTypeDuplicatePropertyNameValidator extends DuplicatePropertyNameValidator {

        public ProductCmptTypeDuplicatePropertyNameValidator(IIpsProject ipsProject) {
            super(ipsProject);
        }

        @Override
        protected Message createMessage(String propertyName, ObjectProperty[] invalidObjProperties) {
            // test if only formulas are involved
            boolean onlyFormulas = true;
            boolean onlyFormulasInSameType = true;
            IProductCmptType prodType = null;
            for (ObjectProperty invalidObjPropertie : invalidObjProperties) {
                Object obj = invalidObjPropertie.getObject();
                if (!(obj instanceof IProductCmptTypeMethod)) {
                    onlyFormulas = false;
                    onlyFormulasInSameType = false;
                    break;
                } else {
                    if (prodType == null) {
                        prodType = ((IProductCmptTypeMethod)obj).getProductCmptType();
                        onlyFormulasInSameType = true;
                    }
                    if (onlyFormulasInSameType && !prodType.equals(((IProductCmptTypeMethod)obj).getProductCmptType())) {
                        onlyFormulasInSameType = false;
                    }
                }
            }
            if (onlyFormulasInSameType) {
                String text = Messages.ProductCmptType_msgDuplicateFormulasNotAllowedInSameType;
                return new Message(MSGCODE_DUPLICATE_FORMULAS_NOT_ALLOWED_IN_SAME_TYPE, text, Message.ERROR,
                        invalidObjProperties);
            } else if (onlyFormulas) {
                String text = NLS.bind(Messages.ProductCmptType_DuplicateFormulaName, propertyName);
                return new Message(MSGCODE_DUPLICATE_FORMULA_NAME_IN_HIERARCHY, text, Message.ERROR,
                        invalidObjProperties);
            } else {
                String text = NLS.bind(Messages.ProductCmptType_multiplePropertyNames, propertyName);
                return new Message(IType.MSGCODE_DUPLICATE_PROPERTY_NAME, text, Message.ERROR, invalidObjProperties);
            }
        }

        @Override
        protected boolean visit(IType currentType) throws CoreException {
            super.visit(currentType);
            ProductCmptType productCmptType = (ProductCmptType)currentType;
            for (ITableStructureUsage tableStructureUsage : productCmptType.tableStructureUsages) {
                add(tableStructureUsage.getRoleName(), new ObjectProperty(tableStructureUsage,
                        ITableStructureUsage.PROPERTY_ROLENAME));
            }
            for (IProductCmptTypeMethod method : productCmptType.getMethodPartCollection()) {
                if (method.isFormulaSignatureDefinition() && StringUtils.isNotEmpty(method.getFormulaName())
                        && !method.isOverloadsFormula()) {
                    add(method.getFormulaName(), new ObjectProperty(method,
                            IProductCmptTypeMethod.PROPERTY_FORMULA_NAME));
                }
            }
            return true;
        }
    }

    private static class NotDerivedAssociationCollector extends TypeHierarchyVisitor<IProductCmptType> {

        public NotDerivedAssociationCollector(IIpsProject ipsProject) {
            super(ipsProject);
        }

        private List<IAssociation> associations = new ArrayList<IAssociation>();

        @Override
        protected boolean visit(IProductCmptType currentType) throws CoreException {
            List<? extends IAssociation> typeAssociations = currentType.getAssociations();
            int index = 0;
            for (IAssociation association : typeAssociations) {
                /*
                 * To get the associations of the root type of the supertype hierarchy first, put in
                 * the list at first, but with unchanged order for all associations found in one
                 * type ...
                 */
                if (!association.isDerived()) {
                    associations.add(index, association);
                    index++;
                }
            }
            return true;
        }

    }
}
