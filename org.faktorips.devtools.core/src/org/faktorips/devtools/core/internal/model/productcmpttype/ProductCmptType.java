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
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpttype.FormulaSignatureFinder;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory;
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
    private final IpsObjectPartCollection<IProductCmptCategory> categories;

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
        categories = new IpsObjectPartCollection<IProductCmptCategory>(this, ProductCmptCategory.class,
                IProductCmptCategory.class, ProductCmptCategory.XML_TAG_NAME);
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
        if (ProductCmptPropertyType.PRODUCT_CMPT_TYPE_ATTRIBUTE == type) {
            return findProductCmptTypeAttribute(propName, ipsProject);
        }
        if (ProductCmptPropertyType.FORMULA_SIGNATURE_DEFINITION == type) {
            return findFormulaSignature(propName, ipsProject);
        }
        if (ProductCmptPropertyType.TABLE_STRUCTURE_USAGE == type) {
            return findTableStructureUsage(propName, ipsProject);
        }
        IPolicyCmptType policyCmptType = findPolicyCmptType(ipsProject);
        if (policyCmptType == null) {
            return null;
        }
        if (ProductCmptPropertyType.VALIDATION_RULE == type) {
            IValidationRule rule = policyCmptType.findValidationRule(propName, ipsProject);
            if (rule != null && rule.isConfigurableByProductComponent()) {
                return rule;
            }
        }
        if (ProductCmptPropertyType.POLICY_CMPT_TYPE_ATTRIBUTE == type) {
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
    public IProductCmptTypeMethod getFormulaSignature(String formulaName) {
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
        validateDefaultCategoryForFormulaSignatureDefinition(list, ipsProject);
        validateDefaultCategoryForPolicyCmptTypeAttribute(list, ipsProject);
        validateDefaultCategoryForProductCmptTypeAttribute(list, ipsProject);
        validateDefaultCategoryForTableStructureUsages(list, ipsProject);
        validateDefaultCategoryForValidationRules(list, ipsProject);
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

    private void validateDefaultCategoryForFormulaSignatureDefinition(MessageList list, IIpsProject ipsProject)
            throws CoreException {

        if (findDefaultCategoryForFormulaSignatureDefinitions(ipsProject) == null) {
            String text = NLS.bind(Messages.ProductCmptCategory_NoDefaultForFormulaSignatureDefinitions, getName());
            Message msg = new Message(MSGCODE_NO_DEFAULT_FOR_FORMULA_SIGNATURE_DEFINITIONS, text, Message.ERROR,
                    ProductCmptType.this);
            list.add(msg);
        }
    }

    private void validateDefaultCategoryForPolicyCmptTypeAttribute(MessageList list, IIpsProject ipsProject)
            throws CoreException {

        if (findDefaultCategoryForPolicyCmptTypeAttributes(ipsProject) == null) {
            String text = NLS.bind(Messages.ProductCmptCategory_NoDefaultForPolicyCmptTypeAttributes, getName());
            Message msg = new Message(MSGCODE_NO_DEFAULT_FOR_POLICY_CMPT_TYPE_ATTRIBUTES, text, Message.ERROR,
                    ProductCmptType.this);
            list.add(msg);
        }
    }

    private void validateDefaultCategoryForProductCmptTypeAttribute(MessageList list, IIpsProject ipsProject)
            throws CoreException {

        if (findDefaultCategoryForProductCmptTypeAttributes(ipsProject) == null) {
            String text = NLS.bind(Messages.ProductCmptCategory_NoDefaultForProductCmptTypeAttributes, getName());
            Message msg = new Message(MSGCODE_NO_DEFAULT_FOR_PRODUCT_CMPT_TYPE_ATTRIBUTES, text, Message.ERROR,
                    ProductCmptType.this);
            list.add(msg);
        }
    }

    private void validateDefaultCategoryForTableStructureUsages(MessageList list, IIpsProject ipsProject)
            throws CoreException {

        if (findDefaultCategoryForTableStructureUsages(ipsProject) == null) {
            String text = NLS.bind(Messages.ProductCmptCategory_NoDefaultForTableStructureUsages, getName());
            Message msg = new Message(MSGCODE_NO_DEFAULT_FOR_TABLE_STRUCTURE_USAGES, text, Message.ERROR,
                    ProductCmptType.this);
            list.add(msg);
        }
    }

    private void validateDefaultCategoryForValidationRules(MessageList list, IIpsProject ipsProject)
            throws CoreException {

        if (findDefaultCategoryForValidationRules(ipsProject) == null) {
            String text = NLS.bind(Messages.ProductCmptCategory_NoDefaultForValidationRules, getName());
            Message msg = new Message(MSGCODE_NO_DEFAULT_FOR_VALIDATION_RULES, text, Message.ERROR,
                    ProductCmptType.this);
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

        /*
         * Adding dependency for explicitly specified matching associations for differing policy and
         * product structure. @see FIPS-563
         */
        for (IProductCmptTypeAssociation association : getProductCmptTypeAssociations()) {
            if (association.constrainsPolicyCmptTypeAssociation(getIpsProject())) {
                IPolicyCmptTypeAssociation matchingPolicyCmptTypeAssociations = association
                        .findMatchingPolicyCmptTypeAssociation(getIpsProject());
                if (!matchingPolicyCmptTypeAssociations.getPolicyCmptType().isConfigurableByProductCmptType()) {
                    IpsObjectDependency dependency = IpsObjectDependency.createReferenceDependency(
                            getQualifiedNameType(), matchingPolicyCmptTypeAssociations.getPolicyCmptType()
                                    .getQualifiedNameType());
                    dependencies.add(dependency);
                }
            }
        }

        super.dependsOn(dependencies, details);

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
        return Messages.ProductCmptType_caption;
    }

    @Override
    public IProductCmptCategory newProductCmptCategory() {
        return categories.newPart();
    }

    @Override
    public IProductCmptCategory newProductCmptCategory(String name) {
        IProductCmptCategory category = newProductCmptCategory();
        category.setName(name);
        return category;
    }

    @Override
    public List<IProductCmptCategory> getProductCmptCategories() {
        List<IProductCmptCategory> notInheritedCategories = new ArrayList<IProductCmptCategory>(categories.size());
        for (IProductCmptCategory category : categories) {
            if (!category.isInherited()) {
                notInheritedCategories.add(category);
            }
        }
        return notInheritedCategories;
    }

    @Override
    public List<IProductCmptCategory> getProductCmptCategoriesIncludeSupertypeCopies() {
        return Collections.unmodifiableList(categories.asList());
    }

    @Override
    public List<IProductCmptCategory> findAllProductCmptCategories(IIpsProject ipsProject) throws CoreException {
        // Collect all categories from the supertype hierarchy
        final Map<IProductCmptType, List<IProductCmptCategory>> typesToCategories = new LinkedHashMap<IProductCmptType, List<IProductCmptCategory>>();
        TypeHierarchyVisitor<IProductCmptType> visitor = new TypeHierarchyVisitor<IProductCmptType>(ipsProject) {
            @Override
            protected boolean visit(IProductCmptType currentType) throws CoreException {
                typesToCategories.put(currentType, currentType.getProductCmptCategories());
                return true;
            }
        };
        visitor.start(this);
        // Sort so that categories that are farther up in the hierarchy are listed at the top
        List<IProductCmptCategory> sortedCategories = new ArrayList<IProductCmptCategory>();
        for (int i = visitor.getVisited().size() - 1; i >= 0; i--) {
            IType type = visitor.getVisited().get(i);
            sortedCategories.addAll(typesToCategories.get(type));
        }
        return Collections.unmodifiableList(sortedCategories);
    }

    @Override
    public IProductCmptCategory getProductCmptCategory(String name) {
        IProductCmptCategory category = getProductCmptCategoryIncludeSupertypeCopies(name);
        if (category != null && !category.isInherited()) {
            return category;
        }
        return null;
    }

    @Override
    public IProductCmptCategory getProductCmptCategoryIncludeSupertypeCopies(String name) {
        for (IProductCmptCategory category : categories) {
            if (category.getName().equals(name)) {
                return category;
            }
        }
        return null;
    }

    @Override
    public boolean existsPersistedProductCmptPropertyReference(IProductCmptProperty property) {
        for (IProductCmptCategory category : categories) {
            if (category.isReferencedAndPersistedProductCmptProperty(property)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public IProductCmptCategory findDefaultCategoryForFormulaSignatureDefinitions(IIpsProject ipsProject)
            throws CoreException {

        DefaultCategoryFinder defaultCategoryFinder = new DefaultCategoryFinder(ipsProject) {
            @Override
            protected boolean isDefault(IProductCmptCategory category) {
                return category.isDefaultForFormulaSignatureDefinitions();
            }
        };
        defaultCategoryFinder.start(this);
        return defaultCategoryFinder.defaultCategory;
    }

    @Override
    public IProductCmptCategory findDefaultCategoryForPolicyCmptTypeAttributes(IIpsProject ipsProject)
            throws CoreException {

        DefaultCategoryFinder defaultCategoryFinder = new DefaultCategoryFinder(ipsProject) {
            @Override
            protected boolean isDefault(IProductCmptCategory category) {
                return category.isDefaultForPolicyCmptTypeAttributes();
            }
        };
        defaultCategoryFinder.start(this);
        return defaultCategoryFinder.defaultCategory;
    }

    @Override
    public IProductCmptCategory findDefaultCategoryForProductCmptTypeAttributes(IIpsProject ipsProject)
            throws CoreException {

        DefaultCategoryFinder defaultCategoryFinder = new DefaultCategoryFinder(ipsProject) {
            @Override
            protected boolean isDefault(IProductCmptCategory category) {
                return category.isDefaultForProductCmptTypeAttributes();
            }
        };
        defaultCategoryFinder.start(this);
        return defaultCategoryFinder.defaultCategory;
    }

    @Override
    public IProductCmptCategory findDefaultCategoryForTableStructureUsages(IIpsProject ipsProject) throws CoreException {
        DefaultCategoryFinder defaultCategoryFinder = new DefaultCategoryFinder(ipsProject) {
            @Override
            protected boolean isDefault(IProductCmptCategory category) {
                return category.isDefaultForTableStructureUsages();
            }
        };
        defaultCategoryFinder.start(this);
        return defaultCategoryFinder.defaultCategory;
    }

    @Override
    public IProductCmptCategory findDefaultCategoryForValidationRules(IIpsProject ipsProject) throws CoreException {
        DefaultCategoryFinder defaultCategoryFinder = new DefaultCategoryFinder(ipsProject) {
            @Override
            protected boolean isDefault(IProductCmptCategory category) {
                return category.isDefaultForValidationRules();
            }
        };
        defaultCategoryFinder.start(this);
        return defaultCategoryFinder.defaultCategory;
    }

    @Override
    public IProductCmptCategory findProductCmptCategory(final String name, IIpsProject ipsProject) throws CoreException {
        class ProductCmptCategoryFinder extends TypeHierarchyVisitor<IProductCmptType> {

            private final String categoryName;

            private IProductCmptCategory category;

            private ProductCmptCategoryFinder(IIpsProject ipsProject, String categoryName) {
                super(ipsProject);
                this.categoryName = categoryName;
            }

            @Override
            protected boolean visit(IProductCmptType currentType) throws CoreException {
                if (currentType.getProductCmptCategory(categoryName) != null
                        && !currentType.getProductCmptCategory(categoryName).isInherited()) {
                    category = currentType.getProductCmptCategory(categoryName);
                    return false;
                }
                return true;
            }

        }

        ProductCmptCategoryFinder visitor = new ProductCmptCategoryFinder(ipsProject, name);
        visitor.start(this);

        return visitor.category;
    }

    @Override
    public int[] moveProductCmptCategories(int[] indexes, boolean up) {
        return categories.moveParts(indexes, up);
    }

    private abstract class DefaultCategoryFinder extends TypeHierarchyVisitor<IProductCmptType> {

        private IProductCmptCategory defaultCategory;

        protected DefaultCategoryFinder(IIpsProject ipsProject) {
            super(ipsProject);
        }

        @Override
        protected boolean visit(IProductCmptType currentType) throws CoreException {
            for (IProductCmptCategory category : currentType.getProductCmptCategories()) {
                if (isDefault(category)) {
                    defaultCategory = category;
                    return false;
                }
            }
            return true;
        }

        protected abstract boolean isDefault(IProductCmptCategory category);

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
            if (propertyType == null || ProductCmptPropertyType.PRODUCT_CMPT_TYPE_ATTRIBUTE.equals(propertyType)) {
                myAttributes.addAll(0, currType.attributes.asList());
            }
            if (propertyType == null || ProductCmptPropertyType.TABLE_STRUCTURE_USAGE.equals(propertyType)) {
                myTableStructureUsages.addAll(0, currType.tableStructureUsages.asList());
            }
            if (propertyType == null || ProductCmptPropertyType.FORMULA_SIGNATURE_DEFINITION.equals(propertyType)) {
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
            if (propertyType == null || ProductCmptPropertyType.POLICY_CMPT_TYPE_ATTRIBUTE.equals(propertyType)) {
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
            if (propertyType == null || ProductCmptPropertyType.VALIDATION_RULE.equals(propertyType)) {
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
