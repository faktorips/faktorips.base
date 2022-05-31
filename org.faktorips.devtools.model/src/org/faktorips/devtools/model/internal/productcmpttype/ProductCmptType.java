/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpttype;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.DependencyType;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.dependency.IDependency;
import org.faktorips.devtools.model.dependency.IDependencyDetail;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.internal.SingleEventModification;
import org.faktorips.devtools.model.internal.dependency.IpsObjectDependency;
import org.faktorips.devtools.model.internal.ipsobject.IpsObjectPartCollection;
import org.faktorips.devtools.model.internal.ipsobject.IpsObjectPartContainer;
import org.faktorips.devtools.model.internal.method.BaseMethod;
import org.faktorips.devtools.model.internal.type.DuplicatePropertyNameValidator;
import org.faktorips.devtools.model.internal.type.Type;
import org.faktorips.devtools.model.internal.util.TreeSetHelper;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.productcmpt.IFormula;
import org.faktorips.devtools.model.productcmpttype.FormulaSignatureFinder;
import org.faktorips.devtools.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.model.productcmpttype.IProductCmptCategory.Position;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.devtools.model.type.IChangingOverTimeProperty;
import org.faktorips.devtools.model.type.IMethod;
import org.faktorips.devtools.model.type.IProductCmptProperty;
import org.faktorips.devtools.model.type.IType;
import org.faktorips.devtools.model.type.ITypePart;
import org.faktorips.devtools.model.type.ProductCmptPropertyType;
import org.faktorips.devtools.model.type.TypeHierarchyVisitor;
import org.faktorips.devtools.model.util.IElementMover;
import org.faktorips.devtools.model.util.SubListElementMover;
import org.faktorips.devtools.model.util.XmlUtil;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.ObjectProperty;
import org.faktorips.util.ArgumentCheck;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.umd.cs.findbugs.annotations.CheckForNull;

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
    private boolean changingOverTime = getIpsProject().getReadOnlyProperties().isChangingOverTimeDefaultEnabled();

    private final IpsObjectPartCollection<IProductCmptTypeAttribute> attributes;
    private final IpsObjectPartCollection<ITableStructureUsage> tableStructureUsages;
    private final IpsObjectPartCollection<IProductCmptTypeMethod> methods;
    private final IpsObjectPartCollection<IProductCmptTypeAssociation> associations;
    private final IpsObjectPartCollection<IProductCmptCategory> categories;
    @Deprecated(forRemoval = true, since = "22.6")
    private final IpsObjectPartCollection<org.faktorips.devtools.model.productcmpttype.IProductCmptPropertyReference> propertyReferences;

    /**
     * A map that stores changes to category assignments of product component properties belonging
     * to policy component types.
     * <p>
     * If we would directly set the category of such properties, using
     * {@link IProductCmptProperty#setCategory(String)}, we would need to immediately save the
     * {@link IIpsSrcFile} of the {@link IPolicyCmptType} if it was not dirty. As this is not the
     * expected behavior for clients, we instead want to save the policy {@link IIpsSrcFile} only if
     * the client saves the category composition in the {@link IProductCmptType}.
     * <p>
     * Therefore, we remember all changes of category assignments for product component properties
     * belonging to policy component types done by the client. We persist them in the
     * {@link IPolicyCmptType} as soon as the client saves the {@link IProductCmptType}. In the
     * meantime, finders must take this map into account to determine the correct
     * {@link IProductCmptCategory} of an {@link IProductCmptProperty}.
     */
    private Map<IProductCmptProperty, Map<CategoryChange, String>> pendingPolicyChanges = new HashMap<>();

    @SuppressWarnings("removal")
    public ProductCmptType(IIpsSrcFile file) {
        super(file);

        tableStructureUsages = new IpsObjectPartCollection<>(this, TableStructureUsage.class,
                ITableStructureUsage.class, TableStructureUsage.TAG_NAME);
        attributes = new IpsObjectPartCollection<>(this, ProductCmptTypeAttribute.class,
                IProductCmptTypeAttribute.class, ProductCmptTypeAttribute.TAG_NAME);
        methods = new IpsObjectPartCollection<>(this, ProductCmptTypeMethod.class,
                IProductCmptTypeMethod.class, BaseMethod.XML_ELEMENT_NAME);
        associations = new IpsObjectPartCollection<>(this, ProductCmptTypeAssociation.class,
                IProductCmptTypeAssociation.class, ProductCmptTypeAssociation.TAG_NAME);
        categories = new IpsObjectPartCollection<>(this, ProductCmptCategory.class,
                IProductCmptCategory.class, ProductCmptCategory.XML_TAG_NAME);
        propertyReferences = new IpsObjectPartCollection<>(this,
                ProductCmptPropertyReference.class,
                org.faktorips.devtools.model.productcmpttype.IProductCmptPropertyReference.class,
                ProductCmptPropertyReference.XML_TAG_NAME);
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
    public IProductCmptType findSupertype(IIpsProject project) {
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
    public boolean isChangingOverTime() {
        return changingOverTime;
    }

    @Override
    public void setChangingOverTime(boolean changesOverTime) {
        boolean oldValue = this.changingOverTime;
        this.changingOverTime = changesOverTime;
        valueChanged(oldValue, changesOverTime, PROPERTY_CHANGING_OVER_TIME);
    }

    @Override
    public IPolicyCmptType findPolicyCmptType(IIpsProject ipsProject) {
        if (!configurationForPolicyCmptType) {
            return null;
        }
        return ipsProject.findPolicyCmptType(policyCmptType);
    }

    @Override
    public IProductCmptType findSuperProductCmptType(IIpsProject project) {
        return (IProductCmptType)project.findIpsObject(IpsObjectType.PRODUCT_CMPT_TYPE, getSupertype());
    }

    /**
     * Returns the {@link IProductCmptProperty} corresponding to the provided
     * {@link org.faktorips.devtools.model.productcmpttype.IProductCmptPropertyReference} or
     * {@code null} if no such property is found.
     * 
     * @param reference the
     *            {@link org.faktorips.devtools.model.productcmpttype.IProductCmptPropertyReference}
     *            to search the corresponding {@link IProductCmptProperty} for
     * 
     * @throws IpsException if an error occurs during the search
     * @deprecated for removal since 22.6
     */
    @CheckForNull
    @Deprecated(forRemoval = true, since = "22.6")
    IProductCmptProperty findProductCmptProperty(
            org.faktorips.devtools.model.productcmpttype.IProductCmptPropertyReference reference,
            IIpsProject ipsProject) {
        for (IProductCmptProperty property : findProductCmptProperties(false, ipsProject)) {
            if (reference.isReferencedProperty(property)) {
                return property;
            }
        }
        return null;
    }

    @Override
    public List<IProductCmptProperty> findProductCmptProperties(IIpsProject ipsProject) {
        return findProductCmptProperties(true, ipsProject);
    }

    @Override
    public List<IProductCmptProperty> findProductCmptProperties(boolean searchSupertypeHierarchy,
            IIpsProject ipsProject) {
        return findProductCmptProperties(null, searchSupertypeHierarchy, ipsProject);
    }

    @Override
    public List<IProductCmptProperty> findProductCmptProperties(ProductCmptPropertyType propertyType,
            boolean searchSupertypeHierarchy,
            IIpsProject ipsProject) {

        ProductCmptPropertyCollector collector = new ProductCmptPropertyCollector(propertyType,
                searchSupertypeHierarchy, ipsProject);
        collector.start(this);
        return collector.getProperties();
    }

    @Override
    public IProductCmptProperty findProductCmptProperty(String propertyName, IIpsProject ipsProject) {
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
            IIpsProject ipsProject) {

        if (ProductCmptPropertyType.PRODUCT_CMPT_TYPE_ATTRIBUTE == type) {
            return findProductCmptTypeAttribute(propName, ipsProject);
        }
        if (ProductCmptPropertyType.FORMULA_SIGNATURE_DEFINITION == type) {
            return findFormulaSignature(propName, ipsProject);
        }
        if (ProductCmptPropertyType.TABLE_STRUCTURE_USAGE == type) {
            return findTableStructureUsage(propName, ipsProject);
        }
        return findProductCmptPropertyInPolicy(type, propName, ipsProject);
    }

    private IProductCmptProperty findProductCmptPropertyInPolicy(ProductCmptPropertyType type,
            String propName,
            IIpsProject ipsProject) {
        IPolicyCmptType foundPolicyCmptType = findPolicyCmptType(ipsProject);
        if (foundPolicyCmptType == null) {
            return null;
        }
        if (ProductCmptPropertyType.VALIDATION_RULE == type) {
            IValidationRule rule = foundPolicyCmptType.findValidationRule(propName, ipsProject);
            if (rule != null && rule.isConfigurableByProductComponent()) {
                return rule;
            }
        }
        if (ProductCmptPropertyType.POLICY_CMPT_TYPE_ATTRIBUTE == type) {
            IPolicyCmptTypeAttribute attr = foundPolicyCmptType.findPolicyCmptTypeAttribute(propName, ipsProject);
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
     */
    public Map<String, IProductCmptProperty> findProductCmptPropertyMap(IIpsProject ipsProject) {
        ProductCmptPropertyCollector collector = new ProductCmptPropertyCollector(null, true, ipsProject);
        collector.start(this);
        return collector.getPropertyMap();
    }

    /**
     * Returns a map containing the property names as keys and the properties as values. This method
     * searches the super type hierarchy.
     * <p>
     * Note this is a model internal method, it is not part of the published interface.
     * 
     * @param propertyType The type of properties that should be included in the map.
     *            <code>null</code> indicates that all properties should be included in the map.
     */
    public Map<String, IProductCmptProperty> findProductCmptPropertyMap(ProductCmptPropertyType propertyType,
            IIpsProject ipsProject) {

        ProductCmptPropertyCollector collector = new ProductCmptPropertyCollector(propertyType, true, ipsProject);
        collector.start(this);
        return collector.getPropertyMap();
    }

    @Override
    public List<IProductCmptTypeAssociation> findAllNotDerivedAssociations(IIpsProject ipsProject) {
        NotDerivedAssociationCollector collector = new NotDerivedAssociationCollector(ipsProject);
        collector.start(this);
        return collector.getAssociationsFound();
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
    public IProductCmptTypeAttribute findProductCmptTypeAttribute(String name, IIpsProject ipsProject) {
        return (IProductCmptTypeAttribute)findAttribute(name, ipsProject);
    }

    @Override
    public List<IProductCmptTypeAttribute> getProductCmptTypeAttributes() {
        return attributes.asList();
    }

    @Override
    protected void initFromXml(Element element, String id) {
        pendingPolicyChanges.clear();
        super.initFromXml(element, id);
    }

    /**
     * This method is only intended for migration to Faktor-IPS 22.6 and will be removed again in a
     * future release.
     *
     * @since 22.6
     * @deprecated for removal
     */
    @Deprecated(forRemoval = true, since = "22.6")
    public void migrateReferences() {
        // to avoid collisions with existing positions, we start with a higher number
        int n = getChildren().length;
        for (int i = 0; i < propertyReferences.size(); i++) {
            org.faktorips.devtools.model.productcmpttype.IProductCmptPropertyReference part = propertyReferences
                    .getPart(i);
            IProductCmptProperty productCmptProperty = part.findProductCmptProperty(getIpsProject());
            if (productCmptProperty != null) {
                productCmptProperty.setCategoryPosition(i + n);
            }
        }
        // now assign continuous numbers inside each category
        IPolicyCmptType policyType = findPolicyCmptType(getIpsProject());
        for (IProductCmptCategory category : findCategories(getIpsProject())) {
            List<IProductCmptProperty> propertiesInCategory = category.findProductCmptProperties(this, true,
                    getIpsProject());
            int i = 0;
            for (IProductCmptProperty property : propertiesInCategory) {
                if (property.getParent() == this || property.getParent() == policyType) {
                    property.setCategoryPosition(++i);
                } else {
                    i = property.getCategoryPosition();
                }
            }
        }
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        policyCmptType = XmlUtil.getAttributeOrEmptyString(element, PROPERTY_POLICY_CMPT_TYPE);
        configurationForPolicyCmptType = XmlUtil.getBooleanAttributeOrFalse(element,
                PROPERTY_CONFIGURATION_FOR_POLICY_CMPT_TYPE);
        layerSupertype = XmlUtil.getBooleanAttributeOrFalse(element, PROPERTY_LAYER_SUPERTYPE);
        instancesIconPath = XmlUtil.getAttributeOrEmptyString(element, PROPERTY_ICON_FOR_INSTANCES);
        if (element.hasAttribute(PROPERTY_CHANGING_OVER_TIME)) {
            changingOverTime = Boolean.parseBoolean(element.getAttribute(PROPERTY_CHANGING_OVER_TIME));
        }
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        if (configurationForPolicyCmptType) {
            element.setAttribute(PROPERTY_CONFIGURATION_FOR_POLICY_CMPT_TYPE,
                    String.valueOf(configurationForPolicyCmptType));
        }
        if (layerSupertype) {
            element.setAttribute(PROPERTY_LAYER_SUPERTYPE, String.valueOf(layerSupertype));
        }
        if (StringUtils.isNotEmpty(policyCmptType)) {
            element.setAttribute(PROPERTY_POLICY_CMPT_TYPE, policyCmptType);
        }
        if (StringUtils.isNotEmpty(instancesIconPath)) {
            element.setAttribute(PROPERTY_ICON_FOR_INSTANCES, instancesIconPath);
        }
        element.setAttribute(PROPERTY_CHANGING_OVER_TIME, String.valueOf(changingOverTime));
    }

    @Override
    public Element toXml(Document doc) {
        Element element = super.toXml(doc);
        if (!pendingPolicyChanges.isEmpty()) {
            savePendingPolicyChanges();
        }
        return element;
    }

    private void savePendingPolicyChanges() {
        IProductCmptProperty[] policyProperties = pendingPolicyChanges.keySet()
                .toArray(new IProductCmptProperty[pendingPolicyChanges.size()]);
        IIpsSrcFile policySrcFile = policyProperties[0].getIpsSrcFile();
        if (policySrcFile.isMutable()) {
            boolean isDirtyState = policySrcFile.isDirty();
            for (IProductCmptProperty property : pendingPolicyChanges.keySet()) {
                property.setCategory(pendingPolicyChanges.get(property).get(CategoryChange.NAME));
                String stringPosition = pendingPolicyChanges.get(property).get(CategoryChange.POSITION);
                int position = -1;
                if (stringPosition != null) {
                    position = Integer.valueOf(stringPosition);
                }
                property.setCategoryPosition(position);
            }
            if (!isDirtyState) {
                policySrcFile.save(null);
            }
        }
        pendingPolicyChanges.clear();
    }

    @Override
    public IProductCmptTypeAssociation newProductCmptTypeAssociation() {
        return (IProductCmptTypeAssociation)newAssociation();
    }

    @Override
    public ITableStructureUsage findTableStructureUsage(String roleName, IIpsProject project) {
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
        ArrayList<IProductCmptTypeMethod> result = new ArrayList<>();
        for (IProductCmptTypeMethod method : methods) {
            if (!method.isFormulaSignatureDefinition()) {
                result.add(method);
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
    public List<IProductCmptTypeMethod> findSignaturesOfOverloadedFormulas(IIpsProject ipsProject) {
        ArrayList<IProductCmptTypeMethod> overloadedMethods = new ArrayList<>();
        for (IProductCmptTypeMethod method : methods) {
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
        for (IProductCmptTypeMethod method : methods) {
            if (method.isFormulaSignatureDefinition() && formulaName.equalsIgnoreCase(method.getFormulaName())) {
                return method;
            }
        }
        return null;
    }

    @Override
    public List<IProductCmptTypeMethod> getFormulaSignatures() {
        ArrayList<IProductCmptTypeMethod> result = new ArrayList<>();
        for (IProductCmptTypeMethod method : methods) {
            if (method.isFormulaSignatureDefinition()) {
                result.add(method);
            }
        }
        return result;
    }

    @Override
    public IProductCmptTypeMethod findFormulaSignature(String formulaName, IIpsProject ipsProject) {
        FormulaSignatureFinder finder = new FormulaSignatureFinder(ipsProject, formulaName, true);
        finder.start(this);
        return (IProductCmptTypeMethod)(finder.getMethods().size() != 0 ? finder.getMethods().get(0) : null);
    }

    @Override
    public List<IMethod> findOverrideMethodCandidates(boolean onlyNotImplementedAbstractMethods,
            IIpsProject ipsProject) {
        List<IMethod> candidates = super.findOverrideMethodCandidates(onlyNotImplementedAbstractMethods, ipsProject);
        List<IProductCmptTypeMethod> overloadedMethods = findSignaturesOfOverloadedFormulas(ipsProject);
        List<IMethod> result = new ArrayList<>(candidates.size());
        for (IMethod candidate : candidates) {
            if (overloadedMethods.contains(candidate)) {
                continue;
            }
            result.add(candidate);
        }
        return result;
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) {
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
        validateSuperProductCmptTypeHasSameChangingOverTimeSetting(supertype, list);

        validateIfAnOverrideOfOverloadedFormulaExists(list, ipsProject);

        validateIconPath(list, ipsProject);
        validateDefaultCategoryForFormulaSignatureDefinition(list, ipsProject);
        validateDefaultCategoryForPolicyCmptTypeAttribute(list, ipsProject);
        validateDefaultCategoryForProductCmptTypeAttribute(list, ipsProject);
        validateDefaultCategoryForTableStructureUsages(list, ipsProject);
        validateDefaultCategoryForValidationRules(list, ipsProject);
    }

    private void validateSuperProductCmptTypeHasSameChangingOverTimeSetting(IProductCmptType superProductCmptType,
            MessageList list) {
        ProductCmptTypeValidations.validateSuperProductCmptTypeHasSameChangingOverTimeSetting(list, this,
                superProductCmptType);
    }

    private void validateLayerSupertype(MessageList list, IIpsProject ipsProject) {
        if (isLayerSupertype() && hasSupertype()) {
            IProductCmptType supertype = findSupertype(ipsProject);
            if (supertype != null && !supertype.isLayerSupertype()) {
                String text = MessageFormat.format(Messages.ProductCmptType_error_supertypeNotMarkedAsLayerSupertype,
                        supertype.getName());
                list.add(new Message(MSGCODE_SUPERTYPE_NOT_MARKED_AS_LAYER_SUPERTYPE, text, Message.ERROR, this,
                        PROPERTY_LAYER_SUPERTYPE));
            }
        }
    }

    private void validateIfAnOverrideOfOverloadedFormulaExists(MessageList msgList, IIpsProject ipsProject) {
        ArrayList<IProductCmptTypeMethod> overloadedSupertypeFormulaSignatures = new ArrayList<>();
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
                    String text = MessageFormat.format(
                            Messages.ProductCmptType_msgOverloadedFormulaMethodCannotBeOverridden,
                            overloadedMethod.getFormulaName());
                    msgList.add(new Message(MSGCODE_OVERLOADED_FORMULA_CANNOT_BE_OVERRIDDEN, text, Message.ERROR,
                            nonFormula, IIpsElement.PROPERTY_NAME));
                }
            }
        }
    }

    private void validateIconPath(MessageList msgList, IIpsProject ipsProject) {
        if (isUseCustomInstanceIcon()) {
            InputStream stream = ipsProject.getResourceAsStream(getInstancesIcon());
            if (stream == null) {
                String text = Messages.ProductCmptType_iconFileCannotBeResolved + getInstancesIcon() + "\"."; //$NON-NLS-1$
                msgList.add(
                        new Message(MSGCODE_ICON_PATH_INVALID, text, Message.ERROR, this, PROPERTY_ICON_FOR_INSTANCES));
            } else {
                try {
                    stream.close();
                } catch (IOException e) {
                    throw new IpsException(new IpsStatus(e));
                }
            }
        }
    }

    @Override
    public DuplicatePropertyNameValidator createDuplicatePropertyNameValidator(IIpsProject ipsProject) {
        return new ProductCmptTypeDuplicatePropertyNameValidator(ipsProject);
    }

    private void validateProductCmptTypeAbstractWhenPolicyCmptTypeAbstract(MessageList msgList,
            IIpsProject ipsProject) {
        if (StringUtils.isEmpty(getPolicyCmptType())) {
            return;
        }
        IPolicyCmptType foundPolicyCmptType = findPolicyCmptType(ipsProject);
        if (foundPolicyCmptType != null) {
            msgList.add(ProductCmptTypeValidations.validateProductCmptTypeAbstractWhenPolicyCmptTypeAbstract(
                    foundPolicyCmptType.isAbstract(), isAbstract(), this));
        }
    }

    private void validatePolicyCmptTypeReference(IProductCmptType supertype, IIpsProject ipsProject, MessageList list) {
        IPolicyCmptType policyCmptTypeObj = findPolicyCmptType(ipsProject);
        if (policyCmptTypeObj == null) {
            String text = MessageFormat.format(Messages.ProductCmptType_PolicyCmptTypeDoesNotExist, policyCmptType);
            list.add(new Message(MSGCODE_POLICY_CMPT_TYPE_DOES_NOT_EXIST, text, Message.ERROR, this,
                    PROPERTY_POLICY_CMPT_TYPE));
            return;
        }
        if (!policyCmptTypeObj.isConfigurableByProductCmptType()) {
            String text = MessageFormat.format(Messages.ProductCmptType_notMarkedAsConfigurable, policyCmptType);
            list.add(new Message(MSGCODE_POLICY_CMPT_TYPE_IS_NOT_MARKED_AS_CONFIGURABLE, text, Message.ERROR, this,
                    PROPERTY_POLICY_CMPT_TYPE));
            return;
        }
        if (!isSubtypeOrSameType(policyCmptTypeObj.findProductCmptType(ipsProject), ipsProject)) {
            String text = MessageFormat.format(Messages.ProductCmptType_policyCmptTypeDoesNotSpecifyThisType,
                    policyCmptType);
            list.add(new Message(MSGCODE_POLICY_CMPT_TYPE_DOES_NOT_SPECIFY_THIS_TYPE, text, Message.ERROR, this,
                    PROPERTY_POLICY_CMPT_TYPE));
            return;
        }
        Message msg = ProductCmptTypeValidations.validateSupertype(this, supertype,
                policyCmptTypeObj.getQualifiedName(), policyCmptTypeObj.getSupertype(), ipsProject);
        if (msg != null) {
            list.add(msg);
        }

        if (!policyCmptTypeObj.isValid(policyCmptTypeObj.getIpsProject())) {
            String text = MessageFormat.format(Messages.ProductCmptType_policyCmptTypeNotValid, policyCmptType);
            list.add(new Message(MSGCODE_POLICY_CMPT_TYPE_NOT_VALID, text, Message.WARNING, this,
                    PROPERTY_POLICY_CMPT_TYPE));
        }
    }

    private void validateDefaultCategoryForFormulaSignatureDefinition(MessageList list, IIpsProject ipsProject) {
        boolean propertyTypeExistsInTypeHierarchy = findProductCmptProperties(
                ProductCmptPropertyType.FORMULA_SIGNATURE_DEFINITION, true, ipsProject).size() > 0;
        if (propertyTypeExistsInTypeHierarchy
                && findDefaultCategoryForFormulaSignatureDefinitions(ipsProject) == null) {
            String text = MessageFormat.format(Messages.ProductCmptCategory_NoDefaultForFormulaSignatureDefinitions,
                    getName());
            list.newError(MSGCODE_NO_DEFAULT_CATEGORY_FOR_FORMULA_SIGNATURE_DEFINITIONS, text, ProductCmptType.this);
        }
    }

    private void validateDefaultCategoryForPolicyCmptTypeAttribute(MessageList list, IIpsProject ipsProject) {
        boolean propertyTypeExistsInTypeHierarchy = findProductCmptProperties(
                ProductCmptPropertyType.POLICY_CMPT_TYPE_ATTRIBUTE, true, ipsProject).size() > 0;
        if (propertyTypeExistsInTypeHierarchy && findDefaultCategoryForPolicyCmptTypeAttributes(ipsProject) == null) {
            String text = MessageFormat.format(Messages.ProductCmptCategory_NoDefaultForPolicyCmptTypeAttributes,
                    getName());
            list.newError(MSGCODE_NO_DEFAULT_CATEGORY_FOR_POLICY_CMPT_TYPE_ATTRIBUTES, text, ProductCmptType.this);
        }
    }

    private void validateDefaultCategoryForProductCmptTypeAttribute(MessageList list, IIpsProject ipsProject) {
        boolean propertyTypeExistsInTypeHierarchy = findProductCmptProperties(
                ProductCmptPropertyType.PRODUCT_CMPT_TYPE_ATTRIBUTE, true, ipsProject).size() > 0;
        if (propertyTypeExistsInTypeHierarchy && findDefaultCategoryForProductCmptTypeAttributes(ipsProject) == null) {
            String text = MessageFormat.format(Messages.ProductCmptCategory_NoDefaultForProductCmptTypeAttributes,
                    getName());
            list.newError(MSGCODE_NO_DEFAULT_CATEGORY_FOR_PRODUCT_CMPT_TYPE_ATTRIBUTES, text, ProductCmptType.this);
        }
    }

    private void validateDefaultCategoryForTableStructureUsages(MessageList list, IIpsProject ipsProject) {
        boolean propertyTypeExistsInTypeHierarchy = findProductCmptProperties(
                ProductCmptPropertyType.TABLE_STRUCTURE_USAGE, true, ipsProject).size() > 0;
        if (propertyTypeExistsInTypeHierarchy && findDefaultCategoryForTableStructureUsages(ipsProject) == null) {
            String text = MessageFormat.format(Messages.ProductCmptCategory_NoDefaultForTableStructureUsages,
                    getName());
            list.newError(MSGCODE_NO_DEFAULT_CATEGORY_FOR_TABLE_STRUCTURE_USAGES, text, ProductCmptType.this);
        }
    }

    private void validateDefaultCategoryForValidationRules(MessageList list, IIpsProject ipsProject) {
        boolean propertyTypeExistsInTypeHierarchy = findProductCmptProperties(ProductCmptPropertyType.VALIDATION_RULE,
                true, ipsProject).size() > 0;
        if (propertyTypeExistsInTypeHierarchy && findDefaultCategoryForValidationRules(ipsProject) == null) {
            String text = MessageFormat.format(Messages.ProductCmptCategory_NoDefaultForValidationRules, getName());
            list.newError(MSGCODE_NO_DEFAULT_CATEGORY_FOR_VALIDATION_RULES, text, ProductCmptType.this);
        }
    }

    @Override
    protected IDependency[] dependsOn(Map<IDependency, List<IDependencyDetail>> details) {
        Set<IDependency> dependencies = new HashSet<>();
        if (!StringUtils.isEmpty(getPolicyCmptType())) {
            IDependency dependency = IpsObjectDependency.createConfiguresDependency(getQualifiedNameType(),
                    new QualifiedNameType(getPolicyCmptType(), IpsObjectType.POLICY_CMPT_TYPE));
            dependencies.add(dependency);
            addDetails(details, dependency, this, PROPERTY_POLICY_CMPT_TYPE);
        }
        dependsOnAddValidationDependency(dependencies);

        dependsOnAddExplicitlyMatchingAssociations(dependencies);
        dependsOnAddTables(dependencies, details);

        super.dependsOn(dependencies, details);

        return dependencies.toArray(new IDependency[dependencies.size()]);

    }

    /**
     * Adding a validation dependency to force a check if a policy component type exists with the
     * same qualified name.
     * 
     * @param dependencies is the result set which will contain all dependencies
     */
    private void dependsOnAddValidationDependency(Set<IDependency> dependencies) {
        dependencies.add(IpsObjectDependency.create(getQualifiedNameType(),
                new QualifiedNameType(getQualifiedName(), IpsObjectType.POLICY_CMPT_TYPE), DependencyType.VALIDATION));
    }

    /**
     * Adding dependency for explicitly specified matching associations for differing policy and
     * product structure. @see FIPS-563
     * 
     * @param dependencies the result set will contain all dependencies that have been found
     */
    private void dependsOnAddExplicitlyMatchingAssociations(Set<IDependency> dependencies) {
        for (IProductCmptTypeAssociation association : getProductCmptTypeAssociations()) {
            if (association.constrainsPolicyCmptTypeAssociation(getIpsProject())) {
                IPolicyCmptTypeAssociation matchingPolicyCmptTypeAssociations = association
                        .findMatchingPolicyCmptTypeAssociation(getIpsProject());
                if (!matchingPolicyCmptTypeAssociations.getPolicyCmptType().isConfigurableByProductCmptType()) {
                    IpsObjectDependency dependency = IpsObjectDependency.createReferenceDependency(
                            getQualifiedNameType(),
                            matchingPolicyCmptTypeAssociations.getPolicyCmptType().getQualifiedNameType());
                    dependencies.add(dependency);
                }
            }
        }
    }

    /**
     * Adding TableStructure dependencies for ProductCompTypes. @see FIPS-1171
     * 
     * @param dependencies representing List of all Reference Dependencies
     * @param details contains all details on the dependencies.
     */
    private void dependsOnAddTables(Set<IDependency> dependencies, Map<IDependency, List<IDependencyDetail>> details) {
        for (ITableStructureUsage tableUsage : getTableStructureUsages()) {
            String[] table = tableUsage.getTableStructures();
            for (String string : table) {
                QualifiedNameType tableName = new QualifiedNameType(string, IpsObjectType.TABLE_STRUCTURE);

                IpsObjectDependency dependencyTable = IpsObjectDependency
                        .createReferenceDependency(getQualifiedNameType(), tableName);
                dependencies.add(dependencyTable);

                addDetails(details, dependencyTable,
                        ((TableStructureUsage)tableUsage).getTableStructureReference(string),
                        ITableStructureUsage.PROPERTY_TABLESTRUCTURE);
            }
        }
    }

    @Override
    public Collection<IIpsSrcFile> searchProductComponents(boolean includeSubtypes) {
        return searchMetaObjectSrcFiles(includeSubtypes);
    }

    @Override
    public Collection<IIpsSrcFile> searchMetaObjectSrcFiles(boolean includeSubtypes) {
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
    public String getCaption(Locale locale) {
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
        List<IProductCmptCategory> positionCategories = new ArrayList<>();
        for (IProductCmptCategory category : categories) {
            if (position.equals(category.getPosition())) {
                positionCategories.add(category);
            }
        }
        return positionCategories;
    }

    @Override
    public List<IProductCmptCategory> findCategories(IIpsProject ipsProject) {
        // Collect all categories from the supertype hierarchy
        final Map<IProductCmptType, List<IProductCmptCategory>> typesToOriginalCategories = new LinkedHashMap<>();
        TypeHierarchyVisitor<IProductCmptType> visitor = new TypeHierarchyVisitor<>(ipsProject) {
            @Override
            protected boolean visit(IProductCmptType currentType) {
                typesToOriginalCategories.put(currentType, currentType.getCategories());
                return true;
            }
        };
        visitor.start(this);

        // Sort so categories originating from farther up in the hierarchy are listed at the top
        List<IProductCmptCategory> sortedCategories = new ArrayList<>();
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
    public boolean findHasCategory(String name, IIpsProject ipsProject) {
        return findCategory(name, ipsProject) != null;
    }

    @Override
    public IProductCmptCategory findDefaultCategoryForFormulaSignatureDefinitions(IIpsProject ipsProject) {
        DefaultCategoryFinder defaultCategoryFinder = new DefaultCategoryFinder(
                ProductCmptPropertyType.FORMULA_SIGNATURE_DEFINITION, ipsProject);
        defaultCategoryFinder.start(this);
        return defaultCategoryFinder.defaultCategory;
    }

    @Override
    public IProductCmptCategory findDefaultCategoryForPolicyCmptTypeAttributes(IIpsProject ipsProject) {
        DefaultCategoryFinder defaultCategoryFinder = new DefaultCategoryFinder(
                ProductCmptPropertyType.POLICY_CMPT_TYPE_ATTRIBUTE, ipsProject);
        defaultCategoryFinder.start(this);
        return defaultCategoryFinder.defaultCategory;
    }

    @Override
    public IProductCmptCategory findDefaultCategoryForProductCmptTypeAttributes(IIpsProject ipsProject) {
        DefaultCategoryFinder defaultCategoryFinder = new DefaultCategoryFinder(
                ProductCmptPropertyType.PRODUCT_CMPT_TYPE_ATTRIBUTE, ipsProject);
        defaultCategoryFinder.start(this);
        return defaultCategoryFinder.defaultCategory;
    }

    @Override
    public IProductCmptCategory findDefaultCategoryForTableStructureUsages(IIpsProject ipsProject) {
        DefaultCategoryFinder defaultCategoryFinder = new DefaultCategoryFinder(
                ProductCmptPropertyType.TABLE_STRUCTURE_USAGE, ipsProject);
        defaultCategoryFinder.start(this);
        return defaultCategoryFinder.defaultCategory;
    }

    @Override
    public IProductCmptCategory findDefaultCategoryForValidationRules(IIpsProject ipsProject) {
        DefaultCategoryFinder defaultCategoryFinder = new DefaultCategoryFinder(ProductCmptPropertyType.VALIDATION_RULE,
                ipsProject);
        defaultCategoryFinder.start(this);
        return defaultCategoryFinder.defaultCategory;
    }

    @Override
    public IProductCmptCategory findCategory(final String name, IIpsProject ipsProject) {

        ProductCmptCategoryFinder visitor = new ProductCmptCategoryFinder(ipsProject, name);
        visitor.start(this);

        return visitor.category;
    }

    /**
     * Gets the name of the category for the given property.
     * <p>
     * In contrast to only asking the property for its category, this method considers pending
     * policy changes. The result may be an empty String or {@code null} if no category is set.
     * 
     * @param property the property you want to get the name of the category for
     * @return The name of the category in respect to pending changes. May be {@code null} or empty
     *         String if no category is set.
     */
    String getCategoryNameFor(IProductCmptProperty property) {
        String pendingCategory = pendingPolicyChanges.getOrDefault(property, Map.of()).get(CategoryChange.NAME);
        if (pendingCategory != null) {
            return pendingCategory;
        }
        return property.getCategory();
    }

    /**
     * Gets the position inside its category for the given property.
     * <p>
     * In contrast to only asking the property for its position, this method considers pending
     * policy changes. The result may be {@code -1} if no explicit position is set.
     * 
     * @param property the property you want to get the position for
     * @return The position inside the category in respect to pending changes. May be {@code -1} if
     *         no position is set.
     */
    int getCategoryPositionFor(IProductCmptProperty property) {
        String pendingPosition = pendingPolicyChanges.getOrDefault(property, Map.of()).get(CategoryChange.POSITION);
        if (pendingPosition != null) {
            return Integer.parseInt(pendingPosition);
        }
        return property.getCategoryPosition();
    }

    @Override
    public boolean moveCategories(List<IProductCmptCategory> categories, boolean up) {
        // Check that all categories to be moved belong to this type
        for (IProductCmptCategory category : categories) {
            ArgumentCheck.equals(this, category.getProductCmptType());
        }

        // Split the categories to be moved according to position
        List<IProductCmptCategory> leftCategories = new ArrayList<>();
        List<IProductCmptCategory> rightCategories = new ArrayList<>();
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

        SubListElementMover<IProductCmptCategory> mover = new SubListElementMover<>(
                this.categories.getBackingList(), contextCategories);
        int[] newIndices = mover.move(indices, up);
        return !Arrays.equals(indices, newIndices);
    }

    /**
     * Moves property references within this {@link IProductCmptType} up or down.
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
     * <p>
     * Note that only a single <em>whole content changed</em> event will be fired by this operation.
     * 
     * @param movedIndices the indices identifying the properties of the context list to be moved
     * @param contextProperties only references corresponding to these properties are swapped with
     *            each other. This is necessary to be able to change the ordering of properties that
     *            belong to the same {@link IProductCmptCategory} without interference from
     *            properties belonging to other categories. To achieve this, clients must provide
     *            all move-enabled properties assigned to the category in question
     * @param up flag indicating whether to move up or down
     * 
     * @return the new indices within the context list
     * 
     * @throws IpsException if an error occurs during the move
     */
    int[] movePropertyReferences(final int[] movedIndices,
            final List<IProductCmptProperty> contextProperties,
            final boolean up) {

        return (int[])((IpsModel)getIpsModel())
                .executeModificationsWithSingleEvent(new SingleEventModification<>(getIpsSrcFile()) {
                    private Object result;

                    @Override
                    protected boolean execute() {
                        result = moveProductCmptPropertyReferencesInternal(movedIndices, contextProperties, up);
                        return true;
                    }

                    @Override
                    protected Object getResult() {
                        return result;
                    }
                });
    }

    private int[] moveProductCmptPropertyReferencesInternal(int[] movedIndices,
            List<IProductCmptProperty> contextProperties,
            boolean up) {

        IElementMover mover = new SubListElementMover<>(
                contextProperties, contextProperties);
        int[] newIndices = mover.move(movedIndices, up);

        if (!Arrays.equals(movedIndices, newIndices)) {
            AtomicInteger i = new AtomicInteger(0);
            contextProperties.forEach(p -> {
                int incrementAndGet = i.incrementAndGet();
                if (!p.isPolicyCmptTypeProperty()) {
                    // Immediately change product component type properties
                    p.setCategoryPosition(incrementAndGet);
                } else {
                    deferPolicyChange(p, getCategoryNameFor(p), incrementAndGet);
                }
            });
            partsMoved(contextProperties.toArray(new IIpsObjectPart[contextProperties.size()]));
        }
        return newIndices;
    }

    @SuppressWarnings("removal")
    @Override
    protected boolean isPartSavedToXml(IIpsObjectPart part) {
        if (part instanceof org.faktorips.devtools.model.productcmpttype.IProductCmptPropertyReference) {
            return false;
        }
        return true;
    }

    /**
     * Returns whether at least two categories with the indicated name exist in the supertype
     * hierarchy of this {@link IProductCmptType} of in this {@link IProductCmptType} itself.
     * 
     * @throws IpsException if an error occurs while searching the supertype hierarchy
     */
    boolean findIsCategoryNameUsedTwiceInSupertypeHierarchy(final String categoryName, IIpsProject ipsProject) {
        CategoryCounter counter = new CategoryCounter(categoryName, ipsProject);
        counter.start(this);
        return counter.categoriesFound > 1;
    }

    /**
     * Sorts the categories of this {@link IProductCmptType} in such a way that all categories with
     * {@link Position#LEFT} are stored before all categories with {@link Position#RIGHT}.
     */
    void sortCategoriesAccordingToPosition() {
        Collections.sort(categories.getBackingList(), (o1, o2) -> {
            if (o1.isAtLeftPosition() && o2.isAtRightPosition()) {
                return -1;
            }
            if (o1.isAtRightPosition() && o2.isAtLeftPosition()) {
                return 1;
            }
            return 0;
        });
    }

    @Override
    public void changeCategoryAndDeferPolicyChange(IProductCmptProperty property, String category) {
        if (property.isPolicyCmptTypeProperty()) {
            deferPolicyChange(property, category, -1);
        } else {
            // Immediately change product component type properties
            property.setCategory(category);
        }
    }

    private void deferPolicyChange(IProductCmptProperty property, String category, int position) {
        if (property.getType().getIpsSrcFile().isMutable()) {
            pendingPolicyChanges.put(property, Map.of(
                    CategoryChange.NAME, category,
                    CategoryChange.POSITION, String.valueOf(position)));
            objectHasChanged();
        }
    }

    /**
     * {@link TypeHierarchyVisitor} that allows to search the supertype hierarchy for categories
     * marked as default for a given {@link ProductCmptPropertyType}.
     */
    private static class DefaultCategoryFinder extends TypeHierarchyVisitor<IProductCmptType> {

        private final ProductCmptPropertyType propertyType;

        private IProductCmptCategory defaultCategory;

        private DefaultCategoryFinder(ProductCmptPropertyType propertyType, IIpsProject ipsProject) {
            super(ipsProject);
            this.propertyType = propertyType;
        }

        @Override
        protected boolean visit(IProductCmptType currentType) {
            for (IProductCmptCategory category : currentType.getCategories()) {
                if (category.isDefaultFor(propertyType)) {
                    defaultCategory = category;
                    return false;
                }
            }
            return true;
        }

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

        private List<IProductCmptProperty> attributes = new ArrayList<>();
        private List<IProductCmptProperty> tableStructureUsages = new ArrayList<>();
        private List<IProductCmptProperty> formulaSignatureDefinitions = new ArrayList<>();
        private List<IProductCmptProperty> policyCmptTypeAttributes = new ArrayList<>();
        private List<IProductCmptProperty> validationRules = new ArrayList<>();

        private Set<IPolicyCmptType> visitedPolicyCmptTypes = new LinkedHashSet<>();

        public ProductCmptPropertyCollector(ProductCmptPropertyType propertyType, boolean searchSupertypeHierarchy,
                IIpsProject ipsProject) {

            super(ipsProject);
            this.propertyType = propertyType;
            this.searchSupertypeHierarchy = searchSupertypeHierarchy;
        }

        @Override
        protected boolean visit(IProductCmptType currentType) {
            collectProductCmptTypeAttributes(currentType);
            collectTableStructureUsages(currentType);
            collectFormulaSignatureDefinitions(currentType);

            IPolicyCmptType policyCmptType;
            policyCmptType = currentType.findPolicyCmptType(getIpsProject());
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
            List<IProductCmptProperty> properties = new ArrayList<>(size());
            properties.addAll(attributes);
            properties.addAll(tableStructureUsages);
            properties.addAll(formulaSignatureDefinitions);
            properties.addAll(policyCmptTypeAttributes);
            properties.addAll(validationRules);
            return properties;
        }

        public Map<String, IProductCmptProperty> getPropertyMap() {
            Map<String, IProductCmptProperty> propertyMap = new LinkedHashMap<>(size());
            add(propertyMap, attributes);
            add(propertyMap, tableStructureUsages);
            add(propertyMap, formulaSignatureDefinitions);
            add(propertyMap, policyCmptTypeAttributes);
            add(propertyMap, validationRules);

            if (propertyType == ProductCmptPropertyType.POLICY_CMPT_TYPE_ATTRIBUTE || propertyType == null) {
                fixOverwrittenPolicyCmptTypeAttributes(propertyMap);
            }

            return propertyMap;
        }

        /**
         * Changes the provided property map with regard to two things:
         * <ul>
         * <li>Removes all policy component type attributes that have been overwritten at the lowest
         * hierarchy level to be non-product-relevant.
         * <li>Adds all policy component type attributes that have been overwritten at the lowest
         * hierarchy level to be product-relevant.
         * </ul>
         */
        private void fixOverwrittenPolicyCmptTypeAttributes(Map<String, IProductCmptProperty> propertyMap) {
            // Iteration trough policy component types starts from the bottom of the hierarchy
            Set<String> analyzedPolicyAttributes = new HashSet<>();
            for (IPolicyCmptType policyCmptType : visitedPolicyCmptTypes) {
                for (IPolicyCmptTypeAttribute attribute : policyCmptType.getPolicyCmptTypeAttributes()) {
                    if (!attribute.isOverwrite() || analyzedPolicyAttributes.contains(attribute.getName())) {
                        continue;
                    }
                    if (!attribute.isProductRelevant()) {
                        propertyMap.remove(attribute.getName());
                    } else {
                        propertyMap.put(attribute.getName(), attribute);
                    }
                    analyzedPolicyAttributes.add(attribute.getName());
                }
            }
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

        private Set<IProductCmptType> productCmptTypes = new LinkedHashSet<>();

        public ProductCmptTypeDuplicatePropertyNameValidator(IIpsProject ipsProject) {
            super(ipsProject);
        }

        @Override
        public void addMessagesForDuplicates(IType currentType, MessageList messages) {
            super.addMessagesForDuplicates(currentType, messages);
            for (IProductCmptType productCmptType : productCmptTypes) {
                String propertyName = productCmptType.getUnqualifiedName().toLowerCase();
                ObjectProperty[] potentialProperties = getProperties().get(propertyName);
                if (potentialProperties != null) {
                    List<ObjectProperty> duplicateProperties = new ArrayList<>();
                    for (ObjectProperty potentialProperty : potentialProperties) {
                        Object object = potentialProperty.getObject();
                        String property = potentialProperty.getProperty();
                        if (isPartOfPolicyTypeOrProductTypeChangingOverTime(object)
                                && (isPolicyPartOrProductPartChangingOverTime(object)
                                        /*
                                         * although we only have a problem when StandardBuilderSet.
                                         * CONFIG_PROPERTY_GENERATE_CONVENIENCE_GETTERS is set, we
                                         * can't access that builder setting from the model and
                                         * therefore prohibit all attributes, even when no
                                         * convenience getter is generated
                                         */
                                        || object instanceof IAttribute)) {
                            if (!(object instanceof IAssociation)
                                    || hasProblematicTargetRole((IAssociation)object, property)) {
                                duplicateProperties.add(potentialProperty);
                            }
                        }
                    }
                    if (!duplicateProperties.isEmpty()) {
                        duplicateProperties.add(0, new ObjectProperty(productCmptType, IProductCmptType.PROPERTY_NAME));
                        messages.add(createMessage(propertyName, duplicateProperties.toArray(ObjectProperty[]::new)));
                    }
                }
            }
        }

        private boolean isPartOfPolicyTypeOrProductTypeChangingOverTime(Object object) {
            if (object instanceof ITypePart) {
                IType type = ((ITypePart)object).getType();
                boolean policyType = type instanceof IPolicyCmptType;
                boolean productTypeChangingOverTime = type instanceof IProductCmptType
                        && ((IProductCmptType)type).isChangingOverTime();
                return policyType || productTypeChangingOverTime;
            } else {
                return false;
            }
        }

        private boolean isPolicyPartOrProductPartChangingOverTime(Object object) {
            return !(object instanceof IChangingOverTimeProperty)
                    || ((IChangingOverTimeProperty)object).isChangingOverTime();
        }

        /*
         * The target role is problematic, when it is used to generate a 0-args-getter, which is
         * true for the singular-target-role for a to-1-association and for the plural-target-role
         * for a to-n-association. If the singular and plural are the same, only the singular
         * property is collected, so in that case it has to be checked for a to-n-association.
         */
        private boolean hasProblematicTargetRole(IAssociation association, String property) {
            boolean problematicSingular = association.is1To1()
                    && IAssociation.PROPERTY_TARGET_ROLE_SINGULAR.equals(property);
            boolean problematicPlural = association.is1ToMany()
                    && (IAssociation.PROPERTY_TARGET_ROLE_PLURAL.equals(property)
                            || (IAssociation.PROPERTY_TARGET_ROLE_SINGULAR.equals(property)
                                    && association.getTargetRoleSingular()
                                            .equalsIgnoreCase(association.getTargetRolePlural())));
            return problematicSingular || problematicPlural;
        }

        @Override
        protected boolean ignore(IType currentType, ObjectProperty[] duplicateObjectProperties) {
            if (isIgnoreDuplicatedPolicyPart(duplicateObjectProperties)) {
                return true;
            } else {
                return super.ignore(currentType, duplicateObjectProperties);
            }
        }

        /**
         * The visitor adds every policy attribute to the list of properties. However only conflicts
         * of policy attributes with product attributes or with table structure usages should be
         * considered. To avoid false validation messages for example if there are only two policy
         * attributes with the same name but no product attribute, we check if there is a real
         * conflict. Hence we could ignore the message if there is any policy attribute but no
         * product attribute nor table structure usage.
         */
        private boolean isIgnoreDuplicatedPolicyPart(ObjectProperty[] duplicateObjectProperties) {
            boolean foundRelevantProductPart = false;
            boolean foundPolicyAttribute = false;
            for (ObjectProperty objectProperty : duplicateObjectProperties) {
                if (isRelevantProductPart(objectProperty)) {
                    foundRelevantProductPart = true;
                }
                if (objectProperty.getObject() instanceof IPolicyCmptTypeAttribute) {
                    foundPolicyAttribute = true;
                }
            }
            return foundPolicyAttribute && !foundRelevantProductPart;
        }

        private boolean isRelevantProductPart(ObjectProperty objectProperty) {
            return objectProperty.getObject() instanceof IProductCmptTypeAttribute
                    || objectProperty.getObject() instanceof ITableStructureUsage;
        }

        @Override
        protected IType getMatchingType(IType currentType) {
            return ((IProductCmptType)currentType).findPolicyCmptType(getIpsProject());
        }

        @Override
        protected String getObjectKindNamePlural(ObjectProperty invalidObjProperty) {
            IIpsObjectPartContainer objectPartContainer = ((IIpsObjectPartContainer)invalidObjProperty.getObject());
            if (objectPartContainer instanceof IFormula) {
                return Messages.ProductCmptTypeMethod_Formula_msg_Plural;
            }
            if (objectPartContainer instanceof ITableStructureUsage) {
                return Messages.TableStructureUsage_msg_Plural;
            }
            return super.getObjectKindNamePlural(invalidObjProperty);
        }

        @Override
        protected String getObjectKindNameSingular(IpsObjectPartContainer objectPartContainer) {
            if (objectPartContainer instanceof IFormula) {
                return Messages.ProductCmptTypeMethod_Formula_msg_Singular;
            }
            if (objectPartContainer instanceof ITableStructureUsage) {
                return Messages.TableStructureUsage_msg_Singular;
            }
            return super.getObjectKindNameSingular(objectPartContainer);
        }

        @Override
        protected boolean visit(IType currentType) {
            ProductCmptType productCmptType = (ProductCmptType)currentType;
            productCmptTypes.add(productCmptType);
            for (ITableStructureUsage tableStructureUsage : productCmptType.tableStructureUsages) {
                add(tableStructureUsage.getRoleName(),
                        new ObjectProperty(tableStructureUsage, ITableStructureUsage.PROPERTY_ROLENAME));
            }
            for (IProductCmptTypeMethod method : productCmptType.getMethodPartCollection()) {
                if (method.isFormulaSignatureDefinition() && StringUtils.isNotEmpty(method.getFormulaName())
                        && !method.isOverloadsFormula()) {
                    add(method.getFormulaName(),
                            new ObjectProperty(method, IProductCmptTypeMethod.PROPERTY_FORMULA_NAME));
                }
            }
            super.visit(currentType);
            return true;
        }
    }

    private static class NotDerivedAssociationCollector extends AbstractAssociationFinder<IProductCmptTypeAssociation> {

        public NotDerivedAssociationCollector(IIpsProject ipsProject) {
            super(true, ipsProject);
        }

        @Override
        protected boolean isAssociationWanted(IAssociation association) {
            return !association.isDerived() && super.isAssociationWanted(association);
        }

        @Override
        protected List<IProductCmptTypeAssociation> getAssociations(IType currentType) {
            return ((IProductCmptType)currentType).getProductCmptTypeAssociations();
        }

    }

    private static class CategoryCounter extends TypeHierarchyVisitor<IProductCmptType> {
        private int categoriesFound = 0;
        private String categoryName;

        public CategoryCounter(String categoryName, IIpsProject ipsProject) {
            super(ipsProject);
            this.categoryName = categoryName;
        }

        @Override
        protected boolean visit(IProductCmptType currentType) {
            for (IProductCmptCategory category : currentType.getCategories()) {
                if (categoryName.equals(category.getName())) {
                    categoriesFound++;
                }
            }
            return categoriesFound < 2;
        }
    }

    private static class ProductCmptCategoryFinder extends TypeHierarchyVisitor<IProductCmptType> {

        private final String categoryName;

        private IProductCmptCategory category;

        private ProductCmptCategoryFinder(IIpsProject ipsProject, String categoryName) {
            super(ipsProject);
            this.categoryName = categoryName;
        }

        @Override
        protected boolean visit(IProductCmptType currentType) {
            if (currentType.getCategory(categoryName) != null) {
                category = currentType.getCategory(categoryName);
                return false;
            }
            return true;
        }
    }

    private static enum CategoryChange {
        NAME,
        POSITION;
    }

}
