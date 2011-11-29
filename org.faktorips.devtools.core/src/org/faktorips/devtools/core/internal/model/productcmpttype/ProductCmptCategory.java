/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.internal.model.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;
import org.faktorips.devtools.core.model.type.TypeHierarchyVisitor;
import org.faktorips.runtime.internal.StringUtils;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Default implementation of {@link IProductCmptProperty}.
 * 
 * @author Alexander Weickmann
 */
public class ProductCmptCategory extends AtomicIpsObjectPart implements IProductCmptCategory {

    final static String XML_TAG_NAME = "Category"; //$NON-NLS-1$

    private boolean defaultForFormulaSignatureDefinitions;

    private boolean defaultForValidationRules;

    private boolean defaultForTableStructureUsages;

    private boolean defaultForPolicyCmptTypeAttributes;

    private boolean defaultForProductCmptTypeAttributes;

    private Position position = Position.LEFT;

    public ProductCmptCategory(IProductCmptType parent, String id) {
        super(parent, id);
    }

    @Override
    public IProductCmptType getProductCmptType() {
        return (IProductCmptType)getParent();
    }

    private ProductCmptType getProductCmptTypeImpl() {
        return (ProductCmptType)getProductCmptType();
    }

    @Override
    public boolean findIsContainingProperty(IProductCmptProperty property,
            IProductCmptType contextType,
            IIpsProject ipsProject) throws CoreException {

        // The queried property must be found by the context type
        if (contextType.findProductCmptProperty(property.getPropertyName(), ipsProject) == null) {
            return false;
        }

        // The name of this category must not be empty and must equal the property's category
        if (!name.isEmpty() && name.equals(property.getCategory())) {
            return true;
        }

        if (isDefaultFor(property)) {
            /*
             * If the name of the property's category does not match this category's name, this
             * category still may contain the property if this category is the corresponding default
             * category. In this case, if the property has no category or the property's category
             * cannot be found, the property belongs to this category.
             */
            return !property.hasCategory() || !contextType.findHasCategory(property.getCategory(), ipsProject);
        }

        return false;
    }

    @Override
    public boolean isDefaultFor(IProductCmptProperty property) {
        return isDefaultFor(property.getProductCmptPropertyType());
    }

    @Override
    public boolean isDefaultFor(ProductCmptPropertyType propertyType) {
        boolean isDefault = false;
        switch (propertyType) {
            case POLICY_CMPT_TYPE_ATTRIBUTE:
                isDefault = isDefaultForPolicyCmptTypeAttributes();
                break;
            case PRODUCT_CMPT_TYPE_ATTRIBUTE:
                isDefault = isDefaultForProductCmptTypeAttributes();
                break;
            case VALIDATION_RULE:
                isDefault = isDefaultForValidationRules();
                break;
            case FORMULA_SIGNATURE_DEFINITION:
                isDefault = isDefaultForFormulaSignatureDefinitions();
                break;
            case TABLE_STRUCTURE_USAGE:
                isDefault = isDefaultForTableStructureUsages();
                break;
        }
        return isDefault;
    }

    @Override
    public List<IProductCmptProperty> findProductCmptProperties(IProductCmptType contextType,
            boolean searchSupertypeHierarchy,
            IIpsProject ipsProject) throws CoreException {

        return ((ProductCmptType)contextType).findProductCmptPropertiesForCategory(this, searchSupertypeHierarchy,
                ipsProject);
    }

    @Override
    public List<IPropertyValue> findPropertyValues(IProductCmptType contextType,
            IProductCmptGeneration contextGeneration,
            IIpsProject ipsProject) throws CoreException {

        List<IProductCmptProperty> categoryProperties = findProductCmptProperties(contextType, true, ipsProject);
        return contextGeneration.getPropertyValuesIncludeProductCmpt(categoryProperties);
    }

    @Override
    public void setName(String name) {
        String oldValue = this.name;
        this.name = name;
        valueChanged(oldValue, name, PROPERTY_NAME);
    }

    @Override
    public boolean isDefaultForFormulaSignatureDefinitions() {
        return defaultForFormulaSignatureDefinitions;
    }

    @Override
    public void setDefaultForFormulaSignatureDefinitions(boolean defaultForFormulaSignatureDefinitions) {
        boolean oldValue = this.defaultForFormulaSignatureDefinitions;
        this.defaultForFormulaSignatureDefinitions = defaultForFormulaSignatureDefinitions;
        valueChanged(oldValue, defaultForFormulaSignatureDefinitions,
                PROPERTY_DEFAULT_FOR_FORMULA_SIGNATURE_DEFINITIONS);
    }

    @Override
    public boolean isDefaultForPolicyCmptTypeAttributes() {
        return defaultForPolicyCmptTypeAttributes;
    }

    @Override
    public void setDefaultForPolicyCmptTypeAttributes(boolean defaultForPolicyCmptTypeAttributes) {
        boolean oldValue = this.defaultForPolicyCmptTypeAttributes;
        this.defaultForPolicyCmptTypeAttributes = defaultForPolicyCmptTypeAttributes;
        valueChanged(oldValue, defaultForPolicyCmptTypeAttributes, PROPERTY_DEFAULT_FOR_POLICY_CMPT_TYPE_ATTRIBUTES);
    }

    @Override
    public boolean isDefaultForProductCmptTypeAttributes() {
        return defaultForProductCmptTypeAttributes;
    }

    @Override
    public void setDefaultForProductCmptTypeAttributes(boolean defaultForProductCmptTypeAttributes) {
        boolean oldValue = this.defaultForProductCmptTypeAttributes;
        this.defaultForProductCmptTypeAttributes = defaultForProductCmptTypeAttributes;
        valueChanged(oldValue, defaultForProductCmptTypeAttributes, PROPERTY_DEFAULT_FOR_PRODUCT_CMPT_TYPE_ATTRIBUTES);
    }

    @Override
    public boolean isDefaultForTableStructureUsages() {
        return defaultForTableStructureUsages;
    }

    @Override
    public void setDefaultForTableStructureUsages(boolean defaultForTableStructureUsages) {
        boolean oldValue = this.defaultForTableStructureUsages;
        this.defaultForTableStructureUsages = defaultForTableStructureUsages;
        valueChanged(oldValue, defaultForTableStructureUsages, PROPERTY_DEFAULT_FOR_TABLE_STRUCTURE_USAGES);
    }

    @Override
    public boolean isDefaultForValidationRules() {
        return defaultForValidationRules;
    }

    @Override
    public void setDefaultForValidationRules(boolean defaultForValidationRules) {
        boolean oldValue = this.defaultForValidationRules;
        this.defaultForValidationRules = defaultForValidationRules;
        valueChanged(oldValue, defaultForValidationRules, PROPERTY_DEFAULT_FOR_VALIDATION_RULES);
    }

    @Override
    public void setPosition(Position side) {
        Position oldValue = this.position;
        this.position = side;

        getProductCmptTypeImpl().sortCategoriesAccordingToPosition();

        valueChanged(oldValue, side, PROPERTY_POSITION);
    }

    @Override
    public Position getPosition() {
        return position;
    }

    @Override
    public boolean isAtLeftPosition() {
        return position == Position.LEFT;
    }

    @Override
    public boolean isAtRightPosition() {
        return position == Position.RIGHT;
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        if (!validateNameIsEmpty(list)) {
            return;
        }
        validateNameAlreadyUsedInTypeHierarchy(list, ipsProject);
        validateDuplicateDefaultsForFormulaSignatureDefinitions(list, ipsProject);
        validateDuplicateDefaultsForPolicyCmptTypeAttributes(list, ipsProject);
        validateDuplicateDefaultsForProductCmptTypeAttributes(list, ipsProject);
        validateDuplicateDefaultsForTableStructureUsages(list, ipsProject);
        validateDuplicateDefaultsForValidationRules(list, ipsProject);
    }

    private boolean validateNameIsEmpty(MessageList list) {
        if (StringUtils.isEmpty(name)) {
            list.newError(MSGCODE_NAME_IS_EMPTY, Messages.ProductCmptCategory_msgNameIsEmpty, this, PROPERTY_NAME);
            return false;
        }
        return true;
    }

    private boolean validateNameAlreadyUsedInTypeHierarchy(MessageList list, IIpsProject ipsProject)
            throws CoreException {

        if (getProductCmptTypeImpl().findIsCategoryNameUsedTwiceInSupertypeHierarchy(name, ipsProject)) {
            String text = NLS.bind(Messages.ProductCmptCategory_msgNameAlreadyUsedInTypeHierarchy, name,
                    getProductCmptType().getName());
            list.newError(MSGCODE_NAME_ALREADY_USED_IN_TYPE_HIERARCHY, text, this, PROPERTY_NAME);
            return false;
        }
        return true;
    }

    private void validateDuplicateDefaultsForFormulaSignatureDefinitions(MessageList list, IIpsProject ipsProject)
            throws CoreException {

        if (!defaultForFormulaSignatureDefinitions) {
            return;
        }

        DuplicateDefaultFinder duplicateFinder = new DuplicateDefaultFinder(
                ProductCmptPropertyType.FORMULA_SIGNATURE_DEFINITION, ipsProject);
        duplicateFinder.start(getProductCmptType());
        duplicateFinder.addValidationMessageIfDuplicateFound(list,
                MSGCODE_DUPLICATE_DEFAULTS_FOR_FORMULA_SIGNATURE_DEFINITIONS,
                Messages.ProductCmptCategory_DuplicateDefaultsForFormulaSignatureDefinitions,
                PROPERTY_DEFAULT_FOR_FORMULA_SIGNATURE_DEFINITIONS);
    }

    private void validateDuplicateDefaultsForValidationRules(MessageList list, IIpsProject ipsProject)
            throws CoreException {

        if (!defaultForValidationRules) {
            return;
        }

        DuplicateDefaultFinder duplicateFinder = new DuplicateDefaultFinder(ProductCmptPropertyType.VALIDATION_RULE,
                ipsProject);
        duplicateFinder.start(getProductCmptType());
        duplicateFinder
                .addValidationMessageIfDuplicateFound(list, MSGCODE_DUPLICATE_DEFAULTS_FOR_VALIDATION_RULES,
                        Messages.ProductCmptCategory_DuplicateDefaultsForValidationRules,
                        PROPERTY_DEFAULT_FOR_VALIDATION_RULES);
    }

    private void validateDuplicateDefaultsForTableStructureUsages(MessageList list, IIpsProject ipsProject)
            throws CoreException {

        if (!defaultForTableStructureUsages) {
            return;
        }

        DuplicateDefaultFinder duplicateFinder = new DuplicateDefaultFinder(
                ProductCmptPropertyType.TABLE_STRUCTURE_USAGE, ipsProject);
        duplicateFinder.start(getProductCmptType());
        duplicateFinder.addValidationMessageIfDuplicateFound(list,
                MSGCODE_DUPLICATE_DEFAULTS_FOR_TABLE_STRUCTURE_USAGES,
                Messages.ProductCmptCategory_DuplicateDefaultsForTableStructureUsages,
                PROPERTY_DEFAULT_FOR_TABLE_STRUCTURE_USAGES);
    }

    private void validateDuplicateDefaultsForPolicyCmptTypeAttributes(MessageList list, IIpsProject ipsProject)
            throws CoreException {

        if (!defaultForPolicyCmptTypeAttributes) {
            return;
        }

        DuplicateDefaultFinder duplicateFinder = new DuplicateDefaultFinder(
                ProductCmptPropertyType.POLICY_CMPT_TYPE_ATTRIBUTE, ipsProject);
        duplicateFinder.start(getProductCmptType());
        duplicateFinder.addValidationMessageIfDuplicateFound(list,
                MSGCODE_DUPLICATE_DEFAULTS_FOR_POLICY_CMPT_TYPE_ATTRIBUTES,
                Messages.ProductCmptCategory_DuplicateDefaultsForPolicyCmptTypeAttributes,
                PROPERTY_DEFAULT_FOR_POLICY_CMPT_TYPE_ATTRIBUTES);
    }

    private void validateDuplicateDefaultsForProductCmptTypeAttributes(MessageList list, IIpsProject ipsProject)
            throws CoreException {

        if (!defaultForProductCmptTypeAttributes) {
            return;
        }

        DuplicateDefaultFinder duplicateFinder = new DuplicateDefaultFinder(
                ProductCmptPropertyType.PRODUCT_CMPT_TYPE_ATTRIBUTE, ipsProject);
        duplicateFinder.start(getProductCmptType());
        duplicateFinder.addValidationMessageIfDuplicateFound(list,
                MSGCODE_DUPLICATE_DEFAULTS_FOR_PRODUCT_CMPT_TYPE_ATTRIBUTES,
                Messages.ProductCmptCategory_DuplicateDefaultsForProductCmptTypeAttributes,
                PROPERTY_DEFAULT_FOR_PRODUCT_CMPT_TYPE_ATTRIBUTES);
    }

    @Override
    public int[] moveProductCmptProperties(int[] indexes, boolean up, IProductCmptType contextType)
            throws CoreException {

        if (indexes.length == 0) {
            return new int[0];
        }

        List<IProductCmptProperty> contextProperties = findProductCmptProperties(contextType, false,
                contextType.getIpsProject());
        return ((ProductCmptType)contextType).movePropertyReferences(indexes, contextProperties, up);
    }

    @Override
    public boolean insertProductCmptProperty(IProductCmptProperty property,
            IProductCmptProperty targetProperty,
            boolean above) throws CoreException {

        IProductCmptType contextType = property.findProductCmptType(property.getIpsProject());
        if (contextType == null) {
            return false;
        }

        contextType.changeCategoryAndDeferPolicyChange(property, name);

        List<IProductCmptProperty> properties = findProductCmptProperties(contextType, false,
                contextType.getIpsProject());
        int propertyIndex = properties.indexOf(property);
        int targetPropertyIndex = targetProperty != null ? properties.indexOf(targetProperty) : properties.size() - 1;
        if (propertyIndex == -1 || targetPropertyIndex == -1) {
            return false;
        }

        insertProductCmptProperty(propertyIndex, targetPropertyIndex, contextType, above);

        return true;
    }

    private void insertProductCmptProperty(int propertyIndex,
            int targetPropertyIndex,
            IProductCmptType contextType,
            boolean above) throws CoreException {

        if (propertyIndex > targetPropertyIndex) {
            moveProductCmptPropertyUp(propertyIndex, targetPropertyIndex, contextType, above);
        } else if (propertyIndex < targetPropertyIndex) {
            moveProductCmptPropertyDown(propertyIndex, targetPropertyIndex, contextType, above);
        }
    }

    /**
     * Moves the {@link IProductCmptProperty} identified by the given index up until it is just
     * above or below the indicated target index.
     */
    private void moveProductCmptPropertyUp(int propertyIndex,
            int targetPropertyIndex,
            IProductCmptType contextType,
            boolean above) throws CoreException {

        int targetIndex = above ? targetPropertyIndex : targetPropertyIndex + 1;
        for (; propertyIndex > targetIndex; propertyIndex--) {
            moveProductCmptProperties(new int[] { propertyIndex }, true, contextType);
        }
    }

    /**
     * Moves the {@link IProductCmptProperty} identified by the given index down until it is just
     * above or below the indicated target index.
     */
    private void moveProductCmptPropertyDown(int propertyIndex,
            int targetPropertyIndex,
            IProductCmptType contextType,
            boolean above) throws CoreException {

        int targetIndex = above ? targetPropertyIndex - 1 : targetPropertyIndex;
        for (; propertyIndex < targetIndex; propertyIndex++) {
            moveProductCmptProperties(new int[] { propertyIndex }, false, contextType);
        }
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        name = element.getAttribute(PROPERTY_NAME);
        defaultForFormulaSignatureDefinitions = Boolean.parseBoolean(element
                .getAttribute(PROPERTY_DEFAULT_FOR_FORMULA_SIGNATURE_DEFINITIONS));
        defaultForPolicyCmptTypeAttributes = Boolean.parseBoolean(element
                .getAttribute(PROPERTY_DEFAULT_FOR_POLICY_CMPT_TYPE_ATTRIBUTES));
        defaultForProductCmptTypeAttributes = Boolean.parseBoolean(element
                .getAttribute(PROPERTY_DEFAULT_FOR_PRODUCT_CMPT_TYPE_ATTRIBUTES));
        defaultForTableStructureUsages = Boolean.parseBoolean(element
                .getAttribute(PROPERTY_DEFAULT_FOR_TABLE_STRUCTURE_USAGES));
        defaultForValidationRules = Boolean.parseBoolean(element.getAttribute(PROPERTY_DEFAULT_FOR_VALIDATION_RULES));
        position = Position.getValueById(element.getAttribute(PROPERTY_POSITION));

        super.initPropertiesFromXml(element, id);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);

        element.setAttribute(PROPERTY_NAME, name);
        element.setAttribute(PROPERTY_DEFAULT_FOR_FORMULA_SIGNATURE_DEFINITIONS,
                Boolean.toString(defaultForFormulaSignatureDefinitions));
        element.setAttribute(PROPERTY_DEFAULT_FOR_POLICY_CMPT_TYPE_ATTRIBUTES,
                Boolean.toString(defaultForPolicyCmptTypeAttributes));
        element.setAttribute(PROPERTY_DEFAULT_FOR_PRODUCT_CMPT_TYPE_ATTRIBUTES,
                Boolean.toString(defaultForProductCmptTypeAttributes));
        element.setAttribute(PROPERTY_DEFAULT_FOR_TABLE_STRUCTURE_USAGES,
                Boolean.toString(defaultForTableStructureUsages));
        element.setAttribute(PROPERTY_DEFAULT_FOR_VALIDATION_RULES, Boolean.toString(defaultForValidationRules));
        element.setAttribute(PROPERTY_POSITION, position.getId());
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(XML_TAG_NAME);
    }

    /**
     * {@link TypeHierarchyVisitor} that searches for the existence of at least two categories
     * marked as <em>default</em> for a specific {@link ProductCmptPropertyType}.
     */
    private class DuplicateDefaultFinder extends TypeHierarchyVisitor<IProductCmptType> {

        private final ProductCmptPropertyType propertyType;

        private boolean duplicateDefaultFound;

        /**
         * @param propertyType the {@link ProductCmptPropertyType} for which duplicate defaults are
         *            searched
         */
        protected DuplicateDefaultFinder(ProductCmptPropertyType propertyType, IIpsProject ipsProject) {
            super(ipsProject);
            this.propertyType = propertyType;
        }

        @Override
        protected boolean visit(IProductCmptType currentType) throws CoreException {
            for (IProductCmptCategory category : currentType.getCategories()) {
                if (category.isDefaultFor(propertyType) && !name.equals(category.getName())) {
                    duplicateDefaultFound = true;
                    return false;
                }
            }
            return true;
        }

        private void addValidationMessageIfDuplicateFound(MessageList list,
                String code,
                String text,
                String invalidProperty) {

            if (duplicateDefaultFound) {
                list.newWarning(code, text, ProductCmptCategory.this, invalidProperty);
            }
        }

    }

}
