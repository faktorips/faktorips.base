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
import java.util.Comparator;
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
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPartCollection;
import org.faktorips.devtools.core.internal.model.type.DuplicatePropertyNameValidator;
import org.faktorips.devtools.core.internal.model.type.Method;
import org.faktorips.devtools.core.internal.model.type.Type;
import org.faktorips.devtools.core.model.IDependency;
import org.faktorips.devtools.core.model.IDependencyDetail;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IpsObjectDependency;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpttype.FormulaSignatureFinder;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory.Position;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptPropertyReference;
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
import org.faktorips.devtools.core.util.IElementMover;
import org.faktorips.devtools.core.util.SubListElementMover;
import org.faktorips.devtools.core.util.TreeSetHelper;
import org.faktorips.util.ArgumentCheck;
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

    private boolean layerSupertype = false;
    private boolean configurationForPolicyCmptType = true;
    private String policyCmptType = ""; //$NON-NLS-1$
    private String instancesIconPath = null;

    private final IpsObjectPartCollection<IProductCmptTypeAttribute> attributes;
    private final IpsObjectPartCollection<ITableStructureUsage> tableStructureUsages;
    private final IpsObjectPartCollection<IProductCmptTypeMethod> methods;
    private final IpsObjectPartCollection<IProductCmptTypeAssociation> associations;
    private final IpsObjectPartCollection<IProductCmptCategory> categories;
    private final IpsObjectPartCollection<IProductCmptPropertyReference> propertyReferences;

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
                IProductCmptCategory.class, IProductCmptCategory.XML_TAG_NAME);
        propertyReferences = new IpsObjectPartCollection<IProductCmptPropertyReference>(this,
                ProductCmptPropertyReference.class, IProductCmptPropertyReference.class,
                IProductCmptPropertyReference.XML_TAG_NAME);
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
    public IProductCmptType findSupertype(IIpsProject project) throws CoreException {
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
    public void setLayerSupertype(boolean layerSupertype) {
        boolean oldValue = this.layerSupertype;
        this.layerSupertype = layerSupertype;
        valueChanged(oldValue, layerSupertype, PROPERTY_LAYER_SUPERTYPE);
    }

    @Override
    public boolean isLayerSupertype() {
        return layerSupertype;
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

    /**
     * Returns the {@link IProductCmptProperty} corresponding to the provided
     * {@link IProductCmptPropertyReference} or null no such property is found.
     * 
     * @param reference the {@link IProductCmptPropertyReference} to search the corresponding
     *            {@link IProductCmptProperty} for
     * @param ipsProject the {@link IIpsProject} whose {@link IIpsObjectPath} is used for the search
     * 
     * @throws CoreException If an error occurs during the search
     */
    IProductCmptProperty findProductCmptProperty(IProductCmptPropertyReference reference, IIpsProject ipsProject)
            throws CoreException {

        for (IProductCmptProperty property : findProductCmptProperties(false, ipsProject)) {
            if (reference.isReferencingProperty(property)) {
                return property;
            }
        }
        return null;
    }

    @Override
    public List<IProductCmptProperty> findProductCmptProperties(IIpsProject ipsProject) throws CoreException {
        return findProductCmptProperties(true, ipsProject);
    }

    @Override
    public List<IProductCmptProperty> findProductCmptProperties(boolean searchSupertypeHierarchy, IIpsProject ipsProject)
            throws CoreException {

        ProductCmptPropertyCollector collector = new ProductCmptPropertyCollector(null, searchSupertypeHierarchy,
                ipsProject);
        collector.start(this);
        return collector.getProperties();
    }

    @Override
    public IProductCmptProperty findProductCmptProperty(String propertyName, IIpsProject ipsProject)
            throws CoreException {
        for (ProductCmptPropertyType type : ProductCmptPropertyType.values()) {
            IProductCmptProperty property = findProductCmptProperty(type, propertyName, ipsProject);
            if (property != null) {
                return property;
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

    /**
     * Returns a map containing the property names as keys and the properties as values. This method
     * searches the supertype hierarchy.
     * <p>
     * Note this is a model internal method, it is not part of the published interface.
     * 
     * @param propertyType The type of properties that should be included in the map.
     *            <code>null</code> indicates that all properties should be included in the map.
     */
    public Map<String, IProductCmptProperty> findProductCmptPropertyMap(ProductCmptPropertyType propertyType,
            IIpsProject ipsProject) throws CoreException {

        ProductCmptPropertyCollector collector = new ProductCmptPropertyCollector(propertyType, true, ipsProject);
        collector.start(this);
        return collector.getPropertyMap();
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
        layerSupertype = Boolean.valueOf(element.getAttribute(PROPERTY_LAYER_SUPERTYPE)).booleanValue();
        instancesIconPath = element.getAttribute(PROPERTY_ICON_FOR_INSTANCES);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_CONFIGURATION_FOR_POLICY_CMPT_TYPE,
                String.valueOf(configurationForPolicyCmptType));
        element.setAttribute(PROPERTY_LAYER_SUPERTYPE, String.valueOf(layerSupertype));
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
        validateLayerSupertype(list, ipsProject);
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

    private void validateLayerSupertype(MessageList list, IIpsProject ipsProject) throws CoreException {
        if (isLayerSupertype() && hasSupertype()) {
            IProductCmptType supertype = findSupertype(ipsProject);
            if (supertype != null && !supertype.isLayerSupertype()) {
                String text = NLS.bind(Messages.ProductCmptType_error_supertypeNotMarkedAsLayerSupertype,
                        supertype.getName());
                list.add(new Message(MSGCODE_SUPERTYPE_NOT_MARKED_AS_LAYER_SUPERTYPE, text, Message.ERROR, this,
                        PROPERTY_LAYER_SUPERTYPE));
            }
        }
    }

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
            list.newError(MSGCODE_NO_DEFAULT_CATEGORY_FOR_FORMULA_SIGNATURE_DEFINITIONS, text, ProductCmptType.this,
                    null);
        }
    }

    private void validateDefaultCategoryForPolicyCmptTypeAttribute(MessageList list, IIpsProject ipsProject)
            throws CoreException {

        if (findDefaultCategoryForPolicyCmptTypeAttributes(ipsProject) == null) {
            String text = NLS.bind(Messages.ProductCmptCategory_NoDefaultForPolicyCmptTypeAttributes, getName());
            list.newError(MSGCODE_NO_DEFAULT_CATEGORY_FOR_POLICY_CMPT_TYPE_ATTRIBUTES, text, ProductCmptType.this, null);
        }
    }

    private void validateDefaultCategoryForProductCmptTypeAttribute(MessageList list, IIpsProject ipsProject)
            throws CoreException {

        if (findDefaultCategoryForProductCmptTypeAttributes(ipsProject) == null) {
            String text = NLS.bind(Messages.ProductCmptCategory_NoDefaultForProductCmptTypeAttributes, getName());
            list.newError(MSGCODE_NO_DEFAULT_CATEGORY_FOR_PRODUCT_CMPT_TYPE_ATTRIBUTES, text, ProductCmptType.this,
                    null);
        }
    }

    private void validateDefaultCategoryForTableStructureUsages(MessageList list, IIpsProject ipsProject)
            throws CoreException {

        if (findDefaultCategoryForTableStructureUsages(ipsProject) == null) {
            String text = NLS.bind(Messages.ProductCmptCategory_NoDefaultForTableStructureUsages, getName());
            list.newError(MSGCODE_NO_DEFAULT_CATEGORY_FOR_TABLE_STRUCTURE_USAGES, text, ProductCmptType.this, null);
        }
    }

    private void validateDefaultCategoryForValidationRules(MessageList list, IIpsProject ipsProject)
            throws CoreException {

        if (findDefaultCategoryForValidationRules(ipsProject) == null) {
            String text = NLS.bind(Messages.ProductCmptCategory_NoDefaultForValidationRules, getName());
            list.newError(MSGCODE_NO_DEFAULT_CATEGORY_FOR_VALIDATION_RULES, text, ProductCmptType.this, null);
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
    public IProductCmptCategory newCategory() {
        return categories.newPart();
    }

    @Override
    public IProductCmptCategory newCategory(String name) {
        IProductCmptCategory category = newCategory();
        category.setName(name);
        return category;
    }

    @Override
    public IProductCmptCategory newCategory(String name, Position position) {
        IProductCmptCategory category = newCategory(name);
        category.setPosition(position);
        return category;
    }

    @Override
    public List<IProductCmptCategory> getCategories() {
        return Collections.unmodifiableList(categories.asList());
    }

    @Override
    public List<IProductCmptCategory> getCategories(Position position) {
        List<IProductCmptCategory> positionCategories = new ArrayList<IProductCmptCategory>();
        for (IProductCmptCategory category : categories) {
            if (position.equals(category.getPosition())) {
                positionCategories.add(category);
            }
        }
        return positionCategories;
    }

    @Override
    public List<IProductCmptCategory> findCategories(IIpsProject ipsProject) throws CoreException {
        // Collect all categories from the supertype hierarchy
        final Map<IProductCmptType, List<IProductCmptCategory>> typesToOriginalCategories = new LinkedHashMap<IProductCmptType, List<IProductCmptCategory>>();
        TypeHierarchyVisitor<IProductCmptType> visitor = new TypeHierarchyVisitor<IProductCmptType>(ipsProject) {
            @Override
            protected boolean visit(IProductCmptType currentType) throws CoreException {
                typesToOriginalCategories.put(currentType, currentType.getCategories());
                return true;
            }
        };
        visitor.start(this);

        // Sort so categories originating from farther up in the hierarchy are listed at the top
        List<IProductCmptCategory> sortedCategories = new ArrayList<IProductCmptCategory>();
        for (int i = visitor.getVisited().size() - 1; i >= 0; i--) {
            IType type = visitor.getVisited().get(i);
            sortedCategories.addAll(typesToOriginalCategories.get(type));
        }
        return sortedCategories;
    }

    @Override
    public IProductCmptCategory getCategory(String name) {
        for (IProductCmptCategory category : categories) {
            if (name.equals(category.getName())) {
                return category;
            }
        }
        return null;
    }

    @Override
    public IProductCmptCategory getFirstCategory(Position position) {
        for (IProductCmptCategory category : categories) {
            if (position.equals(category.getPosition())) {
                return category;
            }
        }
        return null;
    }

    @Override
    public IProductCmptCategory getLastCategory(Position position) {
        for (int i = categories.size() - 1; i >= 0; i--) {
            if (position.equals(categories.getPart(i).getPosition())) {
                return categories.getPart(i);
            }
        }
        return null;
    }

    @Override
    public boolean isFirstCategory(IProductCmptCategory category) {
        return category.equals(getFirstCategory(category.getPosition()));
    }

    @Override
    public boolean isLastCategory(IProductCmptCategory category) {
        return category.equals(getLastCategory(category.getPosition()));
    }

    @Override
    public boolean isDefining(IProductCmptCategory category) {
        return this.equals(category.getParent());
    }

    @Override
    public boolean hasCategory(String name) {
        return getCategory(name) != null;
    }

    @Override
    public boolean findHasCategory(String name, IIpsProject ipsProject) throws CoreException {
        return findCategory(name, ipsProject) != null;
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
    public IProductCmptCategory findCategory(final String name, IIpsProject ipsProject) throws CoreException {
        class ProductCmptCategoryFinder extends TypeHierarchyVisitor<IProductCmptType> {

            private final String categoryName;

            private IProductCmptCategory category;

            private ProductCmptCategoryFinder(IIpsProject ipsProject, String categoryName) {
                super(ipsProject);
                this.categoryName = categoryName;
            }

            @Override
            protected boolean visit(IProductCmptType currentType) throws CoreException {
                if (currentType.getCategory(categoryName) != null) {
                    category = currentType.getCategory(categoryName);
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
    public boolean moveCategories(List<IProductCmptCategory> categories, boolean up) {
        // Check that all categories to be moved belong to this type
        for (IProductCmptCategory category : categories) {
            ArgumentCheck.equals(this, category.getProductCmptType());
        }

        // Split the categories to be moved according to position
        List<IProductCmptCategory> leftCategories = new ArrayList<IProductCmptCategory>();
        List<IProductCmptCategory> rightCategories = new ArrayList<IProductCmptCategory>();
        for (IProductCmptCategory category : categories) {
            List<IProductCmptCategory> targetList = category.isAtLeftPosition() ? leftCategories : rightCategories;
            targetList.add(category);
        }

        boolean leftMoved = moveCategories(leftCategories, getCategories(Position.LEFT), up);
        boolean rightMoved = moveCategories(rightCategories, getCategories(Position.RIGHT), up);
        if (leftMoved || rightMoved) {
            partsMoved(this.categories.getParts());
            return true;
        }
        return false;
    }

    private boolean moveCategories(List<IProductCmptCategory> categories,
            List<IProductCmptCategory> contextCategories,
            boolean up) {

        int[] indices = new int[categories.size()];
        for (int i = 0; i < categories.size(); i++) {
            indices[i] = contextCategories.indexOf(categories.get(i));
        }

        SubListElementMover<IProductCmptCategory> mover = new SubListElementMover<IProductCmptCategory>(
                this.categories.getBackingList(), contextCategories);
        int[] newIndices = mover.move(indices, up);
        return !Arrays.equals(indices, newIndices);
    }

    /**
     * Moves {@link IProductCmptPropertyReference}s up or down within this type.
     * <p>
     * The move operation is logically performed according to the provided context list.
     * <p>
     * <strong>Example:</strong><br>
     * <ol>
     * <li>property1 (in context list)<br>
     * <li>property2 (not in context list)<br>
     * <li>property3 (in context list)
     * </ol>
     * Moving property3 up results in:<br>
     * <br>
     * <ol>
     * <li>property3 (in context list)<br>
     * <li>property2 (not in context list)<br>
     * <li>property1 (in context list)
     * </ol>
     * <p>
     * The indices array identifies the properties to be moved within the context list. Therefore,
     * the indices must be valid with respect to the context list.
     * <p>
     * Returns the new indices within the context list.
     * 
     * @param movedIndices the indices identifying the {@link IProductCmptProperty}s of the context
     *            list to be moved
     * @param contextProperties only {@link IProductCmptPropertyReference}s corresponding to these
     *            {@link IProductCmptProperty}s are swapped with each other. This is necessary to be
     *            able to change the ordering of properties that belong to the same category without
     *            interference from properties belonging to other categories. To achieve this,
     *            clients must provide all move-enabled properties assigned to the category in
     *            question
     * @param up flag indicating whether to move up or down
     * 
     * @return true the new indices within the context list
     * 
     * @throws CoreException If an error occurs during the move
     */
    int[] moveProductCmptPropertyReferences(int[] movedIndices, List<IProductCmptProperty> contextProperties, boolean up)
            throws CoreException {

        createProductCmptPropertyReferencesForNotReferencedProperties();
        return moveProductCmptPropertyReferencesInternal(movedIndices, contextProperties, up);
    }

    private void createProductCmptPropertyReferencesForNotReferencedProperties() throws CoreException {
        for (IProductCmptProperty property : findProductCmptProperties(false, getIpsProject())) {
            if (getReferencedPropertyIndex(property) == -1) {
                newProductCmptPropertyReference(property);
            }
        }
    }

    private IProductCmptPropertyReference newProductCmptPropertyReference(IProductCmptProperty productCmptProperty) {
        IProductCmptPropertyReference reference = propertyReferences.newPart();
        reference.setReferencedProperty(productCmptProperty);
        return reference;
    }

    private int[] moveProductCmptPropertyReferencesInternal(int[] movedIndices,
            List<IProductCmptProperty> contextProperties,
            boolean up) {

        List<IProductCmptPropertyReference> contextReferences = getProductCmptPropertyReferences(contextProperties);
        IElementMover mover = new SubListElementMover<IProductCmptPropertyReference>(
                propertyReferences.getBackingList(), contextReferences);
        int[] newIndices = mover.move(movedIndices, up);

        if (!Arrays.equals(movedIndices, newIndices)) {
            partsMoved(contextReferences.toArray(new IIpsObjectPart[contextReferences.size()]));
        }
        return newIndices;
    }

    private List<IProductCmptPropertyReference> getProductCmptPropertyReferences(List<IProductCmptProperty> properties) {
        List<IProductCmptPropertyReference> references = new ArrayList<IProductCmptPropertyReference>(properties.size());
        for (IProductCmptProperty property : properties) {
            for (IProductCmptPropertyReference reference : propertyReferences) {
                if (reference.isReferencingProperty(property)) {
                    references.add(reference);
                    break;
                }
            }
        }
        return references;
    }

    @Override
    public List<IProductCmptProperty> findProductCmptPropertiesForCategory(IProductCmptCategory category,
            boolean searchSupertypeHierarchy,
            IIpsProject ipsProject) throws CoreException {

        List<IProductCmptProperty> properties = new ArrayList<IProductCmptProperty>();
        for (IProductCmptProperty property : findProductCmptPropertiesInOrder(searchSupertypeHierarchy, ipsProject)) {
            if (category.findIsContainingProperty(property, ipsProject)) {
                properties.add(property);
            }
        }

        filterOriginalsOfOverwrittenAttributes(properties);

        return properties;
    }

    private void filterOriginalsOfOverwrittenAttributes(List<IProductCmptProperty> properties) {
        List<IPolicyCmptTypeAttribute> overwriteAttributes = new ArrayList<IPolicyCmptTypeAttribute>();

        // Create a copy of the list to avoid concurrent modification
        List<IProductCmptProperty> propertiesCopy = new ArrayList<IProductCmptProperty>(properties);

        /*
         * Iterate over the copied list starting with the last element as attributes of sub types
         * are positioned towards the end of the list.
         */
        for (int i = propertiesCopy.size() - 1; i >= 0; i--) {
            if (!(propertiesCopy.get(i) instanceof IPolicyCmptTypeAttribute)) {
                continue;
            }

            IPolicyCmptTypeAttribute policyCmptTypeAttribute = (IPolicyCmptTypeAttribute)propertiesCopy.get(i);

            // Check whether a corresponding overwritten attribute was already encountered
            boolean correspondingOverwrittenAttributeEncountered = false;
            for (IPolicyCmptTypeAttribute overwrittenAttribute : overwriteAttributes) {
                if (policyCmptTypeAttribute.getName().equals(overwrittenAttribute.getName())) {
                    correspondingOverwrittenAttributeEncountered = true;
                    break;
                }
            }

            /*
             * If a corresponding overwritten attribute has already been found, remove the attribute
             * from the list of returned properties. Otherwise, remember the attribute if it is
             * marked as overwrite.
             */
            if (correspondingOverwrittenAttributeEncountered) {
                properties.remove(policyCmptTypeAttribute);
            } else if (policyCmptTypeAttribute.isOverwrite()) {
                overwriteAttributes.add(policyCmptTypeAttribute);
            }
        }
    }

    /**
     * Returns a list containing the {@link IProductCmptProperty}s belonging to this
     * {@link IProductCmptType} or the configured {@link IPolicyCmptType} in the order they are
     * referenced by the {@link IProductCmptCategory}s of this {@link IProductCmptType}.
     * <p>
     * Returns an empty list if there are no such {@link IProductCmptProperty}s.
     * 
     * @param searchSupertypeHierarchy flag indicating whether to include
     *            {@link IProductCmptProperty}s defined in the supertype hierarchy
     * @param ipsProject the {@link IIpsProject} whose {@link IIpsObjectPath} is used for the search
     * 
     * @throws CoreException if an error occurs during the search
     */
    public List<IProductCmptProperty> findProductCmptPropertiesInOrder(boolean searchSupertypeHierarchy,
            IIpsProject ipsProject) throws CoreException {

        List<IProductCmptProperty> properties = findProductCmptProperties(searchSupertypeHierarchy, ipsProject);
        Collections.sort(properties, new ProductCmptPropertyComparator(ipsProject));
        return properties;
    }

    @Override
    protected boolean isPartSavedToXml(IIpsObjectPart part) {
        if (part instanceof IProductCmptPropertyReference) {
            IProductCmptPropertyReference reference = (IProductCmptPropertyReference)part;
            try {
                return reference.findProductCmptProperty(getIpsProject()) != null;
            } catch (CoreException e) {
                /*
                 * If an error occurs during the search for the property, the property is not found
                 * but we cannot be sure whether it is obsolete.
                 */
                IpsPlugin.log(e);
                return true;
            }
        }
        return true;
    }

    /**
     * Returns whether at least two {@link IProductCmptCategory}s with the indicated name exist in
     * the supertype hierarchy of this {@link IProductCmptType} of in this {@link IProductCmptType}
     * itself.
     * 
     * @param categoryName the category name to check
     * @param ipsProject the {@link IIpsProject} whose {@link IIpsObjectPath} is used for the search
     * 
     * @throws CoreException if an error occurs while searching the supertype hierarchy
     */
    boolean findIsCategoryNameUsedTwiceInSupertypeHierarchy(final String categoryName, IIpsProject ipsProject)
            throws CoreException {

        class CategoryCounter extends TypeHierarchyVisitor<IProductCmptType> {
            int categoriesFound = 0;

            public CategoryCounter(IIpsProject ipsProject) {
                super(ipsProject);
            }

            @Override
            protected boolean visit(IProductCmptType currentType) throws CoreException {
                for (IProductCmptCategory category : currentType.getCategories()) {
                    if (categoryName.equals(category.getName())) {
                        categoriesFound++;
                    }
                }
                return categoriesFound < 2;
            }
        }
        CategoryCounter counter = new CategoryCounter(ipsProject);
        counter.start(this);
        return counter.categoriesFound > 1;
    }

    private int getReferencedPropertyIndex(IProductCmptProperty property) {
        int index = 0;
        for (IProductCmptPropertyReference reference : propertyReferences) {
            if (reference.isReferencingProperty(property)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    public void sortCategoriesAccordingToPosition() {
        Collections.sort(categories.getBackingList(), new Comparator<IProductCmptCategory>() {
            @Override
            public int compare(IProductCmptCategory o1, IProductCmptCategory o2) {
                if (o1.isAtLeftPosition() && o2.isAtRightPosition()) {
                    return -1;
                }
                if (o1.isAtRightPosition() && o2.isAtLeftPosition()) {
                    return 1;
                }
                return 0;
            }
        });
    }

    private static class ProductCmptPropertyComparator implements Comparator<IProductCmptProperty> {

        private final IIpsProject ipsProject;

        private ProductCmptPropertyComparator(IIpsProject ipsProject) {
            this.ipsProject = ipsProject;
        }

        @Override
        public int compare(IProductCmptProperty property1, IProductCmptProperty property2) {
            // Search the product component types the properties belong to
            IProductCmptType productCmptType1;
            IProductCmptType productCmptType2;
            try {
                productCmptType1 = property1.findProductCmptType(ipsProject);
                productCmptType2 = property2.findProductCmptType(ipsProject);
            } catch (CoreException e) {
                // Consider elements equal if the product component types cannot be found
                IpsPlugin.log(e);
                return 0;
            }

            if (productCmptType1 == null || productCmptType2 == null) {
                // Consider elements equal if the product component types cannot be found
                return 0;
            }

            if (productCmptType1.equals(productCmptType2)) {
                return comparePropertyIndices(property1, property2, productCmptType1, productCmptType2);
            } else {
                return compareSubtypeRelationship(ipsProject, productCmptType1, productCmptType2);
            }
        }

        /**
         * Compares the indices of the {@link IProductCmptProperty}s in the list of
         * {@link IProductCmptPropertyReference}.
         * <p>
         * Properties whose indices are greater are sorted towards the end.
         */
        private int comparePropertyIndices(IProductCmptProperty property1,
                IProductCmptProperty property2,
                IProductCmptType productCmptType1,
                IProductCmptType productCmptType2) {

            int index1 = ((ProductCmptType)productCmptType1).getReferencedPropertyIndex(property1);
            int index2 = ((ProductCmptType)productCmptType2).getReferencedPropertyIndex(property2);
            if (index1 == -1 || index2 == -1) {
                return 0;
            }

            if (index1 == index2) {
                return 0;
            } else if (index1 < index2) {
                return -1;
            } else {
                return 1;
            }
        }

        /**
         * Compares the provided {@link IProductCmptType}s according to their subtype/supertype
         * relationship.
         * <p>
         * Subtypes are sorted towards the end.
         */
        private int compareSubtypeRelationship(final IIpsProject ipsProject,
                IProductCmptType productCmptType1,
                IProductCmptType productCmptType2) {

            try {
                if (productCmptType1.isSubtypeOf(productCmptType2, ipsProject)) {
                    return 1;
                } else {
                    return -1;
                }
            } catch (CoreException e) {
                // Consider elements equal if it the subtype relationship cannot be determined
                return 0;
            }
        }

    }

    private static abstract class DefaultCategoryFinder extends TypeHierarchyVisitor<IProductCmptType> {

        private IProductCmptCategory defaultCategory;

        protected DefaultCategoryFinder(IIpsProject ipsProject) {
            super(ipsProject);
        }

        @Override
        protected boolean visit(IProductCmptType currentType) throws CoreException {
            for (IProductCmptCategory category : currentType.getCategories()) {
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

        /**
         * Indicates the type of the properties that are collected (if null, all properties are
         * collected).
         */
        private final ProductCmptPropertyType propertyType;

        private final boolean searchSupertypeHierarchy;

        private List<IProductCmptProperty> attributes = new ArrayList<IProductCmptProperty>();
        private List<IProductCmptProperty> tableStructureUsages = new ArrayList<IProductCmptProperty>();
        private List<IProductCmptProperty> formulaSignatureDefinitions = new ArrayList<IProductCmptProperty>();
        private List<IProductCmptProperty> policyCmptTypeAttributes = new ArrayList<IProductCmptProperty>();
        private List<IProductCmptProperty> validationRules = new ArrayList<IProductCmptProperty>();

        private Set<IPolicyCmptType> visitedPolicyCmptTypes = new HashSet<IPolicyCmptType>();

        public ProductCmptPropertyCollector(ProductCmptPropertyType propertyType, boolean searchSupertypeHierarchy,
                IIpsProject ipsProject) {

            super(ipsProject);
            this.propertyType = propertyType;
            this.searchSupertypeHierarchy = searchSupertypeHierarchy;
        }

        @Override
        protected boolean visit(IProductCmptType currentType) throws CoreException {
            collectProductCmptTypeAttributes(currentType);
            collectTableStructureUsages(currentType);
            collectFormulaSignatureDefinitions(currentType);

            IPolicyCmptType policyCmptType = currentType.findPolicyCmptType(ipsProject);
            if (policyCmptType == null || visitedPolicyCmptTypes.contains(policyCmptType)) {
                return searchSupertypeHierarchy;
            }

            collectPolicyCmptTypeAttributes(policyCmptType);
            collectValidationRules(policyCmptType);

            return searchSupertypeHierarchy;
        }

        private void collectValidationRules(IPolicyCmptType policyCmptType) {
            if (propertyType == null || ProductCmptPropertyType.VALIDATION_RULE.equals(propertyType)) {
                visitedPolicyCmptTypes.add(policyCmptType);
                validationRules.addAll(0,
                        policyCmptType.getProductCmptProperties(ProductCmptPropertyType.VALIDATION_RULE));
            }
        }

        private void collectPolicyCmptTypeAttributes(IPolicyCmptType policyCmptType) {
            if (propertyType == null || ProductCmptPropertyType.POLICY_CMPT_TYPE_ATTRIBUTE.equals(propertyType)) {
                visitedPolicyCmptTypes.add(policyCmptType);
                policyCmptTypeAttributes.addAll(0,
                        policyCmptType.getProductCmptProperties(ProductCmptPropertyType.POLICY_CMPT_TYPE_ATTRIBUTE));
            }
        }

        private void collectFormulaSignatureDefinitions(IProductCmptType currentType) {
            if (propertyType == null || ProductCmptPropertyType.FORMULA_SIGNATURE_DEFINITION.equals(propertyType)) {
                formulaSignatureDefinitions.addAll(0, currentType.getFormulaSignatures());
            }
        }

        private void collectTableStructureUsages(IProductCmptType currentType) {
            if (propertyType == null || ProductCmptPropertyType.TABLE_STRUCTURE_USAGE.equals(propertyType)) {
                tableStructureUsages.addAll(0, currentType.getTableStructureUsages());
            }
        }

        private void collectProductCmptTypeAttributes(IProductCmptType currentType) {
            if (propertyType == null || ProductCmptPropertyType.PRODUCT_CMPT_TYPE_ATTRIBUTE.equals(propertyType)) {
                attributes.addAll(0, currentType.getProductCmptTypeAttributes());
            }
        }

        public List<IProductCmptProperty> getProperties() {
            List<IProductCmptProperty> properties = new ArrayList<IProductCmptProperty>(size());
            properties.addAll(attributes);
            properties.addAll(tableStructureUsages);
            properties.addAll(formulaSignatureDefinitions);
            properties.addAll(policyCmptTypeAttributes);
            properties.addAll(validationRules);
            return properties;
        }

        public Map<String, IProductCmptProperty> getPropertyMap() {
            Map<String, IProductCmptProperty> propertyMap = new LinkedHashMap<String, IProductCmptProperty>(size());
            add(propertyMap, attributes);
            add(propertyMap, tableStructureUsages);
            add(propertyMap, formulaSignatureDefinitions);
            add(propertyMap, policyCmptTypeAttributes);
            add(propertyMap, validationRules);
            return propertyMap;
        }

        private void add(Map<String, IProductCmptProperty> propertyMap,
                List<? extends IProductCmptProperty> propertyList) {

            for (IProductCmptProperty property : propertyList) {
                propertyMap.put(property.getPropertyName(), property);
            }
        }

        private int size() {
            return attributes.size() + tableStructureUsages.size() + formulaSignatureDefinitions.size()
                    + policyCmptTypeAttributes.size() + validationRules.size();
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
