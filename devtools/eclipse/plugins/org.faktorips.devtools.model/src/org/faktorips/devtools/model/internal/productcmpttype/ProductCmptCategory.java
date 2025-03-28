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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.internal.SingleEventModification;
import org.faktorips.devtools.model.internal.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.faktorips.devtools.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.devtools.model.type.IOverridableElement;
import org.faktorips.devtools.model.type.IProductCmptProperty;
import org.faktorips.devtools.model.type.ProductCmptPropertyType;
import org.faktorips.devtools.model.type.TypeHierarchyVisitor;
import org.faktorips.devtools.model.util.XmlUtil;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Default implementation of {@link IProductCmptProperty}.
 */
public class ProductCmptCategory extends AtomicIpsObjectPart implements IProductCmptCategory {

    static final String XML_TAG_NAME = "Category"; //$NON-NLS-1$

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
            IIpsProject ipsProject) {
        // The queried property must be found by the context type
        if (contextType.findProductCmptProperty(property.getPropertyName(), ipsProject) == null) {
            return false;
        }

        String categoryName = ((ProductCmptType)contextType).getCategoryNameFor(property);
        // The name of this category must not be empty and must equal the property's category
        if (IpsStringUtils.isNotEmpty(categoryName) && categoryName.equals(name)) {
            return true;
        }

        if (isDefaultFor(property)) {
            /*
             * If the name of the property's category does not match this category's name, this
             * category still may contain the property if this category is the corresponding default
             * category. In this case, if the property has no category or the property's category
             * cannot be found, the property belongs to this category.
             */
            return IpsStringUtils.isEmpty(categoryName) || !contextType.findHasCategory(categoryName, ipsProject);
        }

        return false;
    }

    @Override
    public boolean isDefaultFor(IProductCmptProperty property) {
        ProductCmptPropertyType productCmptPropertyType = property.getProductCmptPropertyType();
        if (productCmptPropertyType == null) {
            return false;
        }
        return isDefaultFor(productCmptPropertyType);
    }

    @Override
    public boolean isDefaultFor(ProductCmptPropertyType propertyType) {
        return switch (propertyType) {
            case POLICY_CMPT_TYPE_ATTRIBUTE -> isDefaultForPolicyCmptTypeAttributes();
            case PRODUCT_CMPT_TYPE_ATTRIBUTE -> isDefaultForProductCmptTypeAttributes();
            case VALIDATION_RULE -> isDefaultForValidationRules();
            case FORMULA_SIGNATURE_DEFINITION -> isDefaultForFormulaSignatureDefinitions();
            case TABLE_STRUCTURE_USAGE -> isDefaultForTableStructureUsages();
        };
    }

    @Override
    public List<IProductCmptProperty> findProductCmptProperties(IProductCmptType contextType,
            final boolean searchSupertypeHierarchy,
            IIpsProject ipsProject) {

        class CategoryPropertyCollector extends TypeHierarchyVisitor<IProductCmptType> {

            private final List<IProductCmptProperty> properties = new ArrayList<>();

            /**
             * {@link Set} that is used to store all property names of properties that overwrite
             * another property from the supertype hierarchy.
             * <p>
             * When testing whether a given {@link IProductCmptProperty} shall be included in an
             * {@link IProductCmptCategory}, it is first checked whether an
             * {@link IProductCmptProperty} with the same property name is contained within this
             * set. In this case, the {@link IProductCmptProperty} has been overwritten by a subtype
             * which means that the supertype {@link IProductCmptProperty} is not to be added to the
             * {@link IProductCmptCategory}.
             */
            private final Set<String> overwritingProperties = new HashSet<>();

            private CategoryPropertyCollector(IIpsProject ipsProject) {
                super(ipsProject);
            }

            @Override
            protected boolean visit(IProductCmptType currentType) {
                for (IProductCmptProperty property : currentType.findProductCmptProperties(false,
                        getIpsProject())) {
                    /*
                     * First, check whether the property has been overwritten by a subtype - in this
                     * case we do not add the property to the category.
                     */
                    if (overwritingProperties.contains(property.getPropertyName())) {
                        continue;
                    }

                    /*
                     * Memorize the property if it is overwriting another property from the
                     * supertype hierarchy.
                     */
                    if (isOverwriteProperty(property)) {
                        overwritingProperties.add(property.getPropertyName());
                    }

                    /*
                     * Now, check if the property is visible. If not, the property will not be added
                     * to the category. Note that it is still important to check if it is
                     * overwritten, first, so that super attributes that are not hidden, will not be
                     * displayed.
                     */
                    if (!isVisible(property)) {
                        continue;
                    }

                    if (findIsContainingProperty(property, currentType, getIpsProject())
                            && !properties.contains(property)) {
                        properties.add(property);
                    }
                }

                return searchSupertypeHierarchy;
            }

            /**
             * Returns whether the given {@link IProductCmptProperty} overwrites another
             * {@link IProductCmptProperty} from the supertype hierarchy.
             */
            private boolean isOverwriteProperty(IProductCmptProperty property) {
                return switch (property) {
                    case IAttribute attribute -> attribute.isOverwrite();
                    case IProductCmptTypeMethod method -> method.isOverloadsFormula();
                    default -> false;
                };
            }

            /**
             * Returns true if the given {@link IProductCmptProperty} is visible, false otherwise.
             */
            private boolean isVisible(IProductCmptProperty property) {
                if (property instanceof IProductCmptTypeAttribute attribute) {
                    return attribute.isVisible();
                }
                return true;
            }

        }

        CategoryPropertyCollector collector = new CategoryPropertyCollector(ipsProject);
        collector.start(contextType);

        Collections.sort(collector.properties, new ProductCmptPropertyComparator(contextType));

        return collector.properties;
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
        Position oldValue = position;
        position = side;

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
    protected void validateThis(MessageList list, IIpsProject ipsProject) {
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
        if (IpsStringUtils.isEmpty(name)) {
            list.newError(MSGCODE_NAME_IS_EMPTY, Messages.ProductCmptCategory_msgNameIsEmpty, this, PROPERTY_NAME);
            return false;
        }
        return true;
    }

    private boolean validateNameAlreadyUsedInTypeHierarchy(MessageList list, IIpsProject ipsProject) {
        if (getProductCmptTypeImpl().findIsCategoryNameUsedTwiceInSupertypeHierarchy(name, ipsProject)) {
            String text = MessageFormat.format(Messages.ProductCmptCategory_msgNameAlreadyUsedInTypeHierarchy, name,
                    getProductCmptType().getName());
            list.newError(MSGCODE_NAME_ALREADY_USED_IN_TYPE_HIERARCHY, text, this, PROPERTY_NAME);
            return false;
        }
        return true;
    }

    private void validateDuplicateDefaultsForFormulaSignatureDefinitions(MessageList list, IIpsProject ipsProject) {

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

    private void validateDuplicateDefaultsForValidationRules(MessageList list, IIpsProject ipsProject) {

        if (!defaultForValidationRules) {
            return;
        }

        DuplicateDefaultFinder duplicateFinder = new DuplicateDefaultFinder(ProductCmptPropertyType.VALIDATION_RULE,
                ipsProject);
        duplicateFinder.start(getProductCmptType());
        duplicateFinder.addValidationMessageIfDuplicateFound(list, MSGCODE_DUPLICATE_DEFAULTS_FOR_VALIDATION_RULES,
                Messages.ProductCmptCategory_DuplicateDefaultsForValidationRules,
                PROPERTY_DEFAULT_FOR_VALIDATION_RULES);
    }

    private void validateDuplicateDefaultsForTableStructureUsages(MessageList list, IIpsProject ipsProject) {

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

    private void validateDuplicateDefaultsForPolicyCmptTypeAttributes(MessageList list, IIpsProject ipsProject) {

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

    private void validateDuplicateDefaultsForProductCmptTypeAttributes(MessageList list, IIpsProject ipsProject) {

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
    public int[] moveProductCmptProperties(int[] indexes, boolean up, IProductCmptType contextType) {

        if (indexes.length == 0) {
            return new int[0];
        }

        List<IProductCmptProperty> contextProperties = findProductCmptProperties(contextType, false,
                contextType.getIpsProject());
        return ((ProductCmptType)contextType).movePropertyReferences(indexes, contextProperties, up);
    }

    @Override
    public boolean insertProductCmptProperty(final IProductCmptProperty property,
            final IProductCmptProperty targetProperty,
            final boolean above) {

        final IProductCmptType contextType = property.findProductCmptType(property.getIpsProject());
        if (contextType == null) {
            return false;
        }
        // CSOFF: AnonInnerLength
        return ((IpsModel)getIpsModel())
                .executeModificationsWithSingleEvent(new SingleEventModification<Boolean>(contextType.getIpsSrcFile()) {
                    private boolean result = true;

                    @Override
                    protected boolean execute() {
                        contextType.changeCategoryAndDeferPolicyChange(property, name);
                        List<IProductCmptProperty> properties = findProductCmptProperties(contextType, false,
                                contextType.getIpsProject());
                        int propertyIndex = properties.indexOf(property);
                        int targetPropertyIndex = targetProperty != null ? properties.indexOf(targetProperty)
                                : properties.size() - 1;
                        if (propertyIndex == -1 || targetPropertyIndex == -1) {
                            result = false;
                        } else {
                            insertProductCmptProperty(propertyIndex, targetPropertyIndex, contextType, above);
                        }
                        return true;
                    }

                    @Override
                    protected Boolean getResult() {
                        return result;
                    }
                });
        // CSON: AnonInnerLength
    }

    private void insertProductCmptProperty(int propertyIndex,
            int targetPropertyIndex,
            IProductCmptType contextType,
            boolean above) {

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
            boolean above) {

        int targetIndex = above ? targetPropertyIndex : targetPropertyIndex + 1;
        for (int i = propertyIndex; i > targetIndex; i--) {
            moveProductCmptProperties(new int[] { i }, true, contextType);
        }
    }

    /**
     * Moves the {@link IProductCmptProperty} identified by the given index down until it is just
     * above or below the indicated target index.
     */
    private void moveProductCmptPropertyDown(int propertyIndex,
            int targetPropertyIndex,
            IProductCmptType contextType,
            boolean above) {

        int targetIndex = above ? targetPropertyIndex - 1 : targetPropertyIndex;
        for (int i = propertyIndex; i < targetIndex; i++) {
            moveProductCmptProperties(new int[] { i }, false, contextType);
        }
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        name = element.getAttribute(PROPERTY_NAME);
        defaultForFormulaSignatureDefinitions = XmlUtil.getBooleanAttributeOrFalse(element,
                PROPERTY_DEFAULT_FOR_FORMULA_SIGNATURE_DEFINITIONS);
        defaultForPolicyCmptTypeAttributes = XmlUtil.getBooleanAttributeOrFalse(element,
                PROPERTY_DEFAULT_FOR_POLICY_CMPT_TYPE_ATTRIBUTES);
        defaultForProductCmptTypeAttributes = XmlUtil.getBooleanAttributeOrFalse(element,
                PROPERTY_DEFAULT_FOR_PRODUCT_CMPT_TYPE_ATTRIBUTES);
        defaultForTableStructureUsages = XmlUtil.getBooleanAttributeOrFalse(element,
                PROPERTY_DEFAULT_FOR_TABLE_STRUCTURE_USAGES);
        defaultForValidationRules = XmlUtil.getBooleanAttributeOrFalse(element, PROPERTY_DEFAULT_FOR_VALIDATION_RULES);
        position = Position.getValueById(element.getAttribute(PROPERTY_POSITION));

        super.initPropertiesFromXml(element, id);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);

        element.setAttribute(PROPERTY_NAME, name);
        if (defaultForFormulaSignatureDefinitions) {
            element.setAttribute(PROPERTY_DEFAULT_FOR_FORMULA_SIGNATURE_DEFINITIONS,
                    Boolean.toString(defaultForFormulaSignatureDefinitions));
        }
        if (defaultForPolicyCmptTypeAttributes) {
            element.setAttribute(PROPERTY_DEFAULT_FOR_POLICY_CMPT_TYPE_ATTRIBUTES,
                    Boolean.toString(defaultForPolicyCmptTypeAttributes));
        }
        if (defaultForProductCmptTypeAttributes) {
            element.setAttribute(PROPERTY_DEFAULT_FOR_PRODUCT_CMPT_TYPE_ATTRIBUTES,
                    Boolean.toString(defaultForProductCmptTypeAttributes));
        }
        if (defaultForTableStructureUsages) {
            element.setAttribute(PROPERTY_DEFAULT_FOR_TABLE_STRUCTURE_USAGES,
                    Boolean.toString(defaultForTableStructureUsages));
        }
        if (defaultForValidationRules) {
            element.setAttribute(PROPERTY_DEFAULT_FOR_VALIDATION_RULES, Boolean.toString(defaultForValidationRules));
        }
        element.setAttribute(PROPERTY_POSITION, position.getId());
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(XML_TAG_NAME);
    }

    /** Returns whether the given property is overriding a property from a super type. */
    public static boolean isOverriding(IProductCmptProperty property) {
        return property instanceof IOverridableElement overridable && overridable.isOverriding();
    }

    /**
     * {@link Comparator} that can be used to sort product component properties according to the
     * reference list stored in the {@link IProductCmptType}, with properties belonging to
     * supertypes being sorted towards the beginning of the list by default.
     */
    static class ProductCmptPropertyComparator implements Comparator<IProductCmptProperty> {

        private final IProductCmptType productCmptType;
        private final List<String> categories;

        ProductCmptPropertyComparator(IProductCmptType productCmptType) {
            this.productCmptType = productCmptType;
            categories = productCmptType.getCategories().stream().map(IProductCmptCategory::getName)
                    .collect(Collectors.toList());
        }

        @Override
        public int compare(IProductCmptProperty property1, IProductCmptProperty property2) {
            IProductCmptProperty prop1 = getOverriddenProperty(property1);
            IProductCmptProperty prop2 = getOverriddenProperty(property2);
            // First, try to sort properties of the supertype hierarchy to the top
            int subtypeCompare = compareSubtypeRelationship(prop1, prop2);

            // If the indices are equal, compare the indices of the properties in the reference list
            return subtypeCompare != 0 ? subtypeCompare : comparePropertyIndices(prop1, prop2);
        }

        private IProductCmptProperty getOverriddenProperty(IProductCmptProperty property) {
            if (property instanceof IOverridableElement overridable && overridable.isOverriding()) {
                var overriddenProperty = (IProductCmptProperty)overridable
                        .findOverriddenElement(property.getIpsProject());
                return getOverriddenProperty(overriddenProperty);
            }
            return property;
        }

        /**
         * Compares the provided product component types according to their subtype/supertype
         * relationship.
         * <p>
         * Subtypes are sorted towards the end.
         */
        private int compareSubtypeRelationship(IProductCmptProperty property1, IProductCmptProperty property2) {
            // Search the product component types the properties belong to
            IProductCmptType productCmptType1;
            IProductCmptType productCmptType2;
            try {
                productCmptType1 = property1.findProductCmptType(productCmptType.getIpsProject());
                productCmptType2 = property2.findProductCmptType(productCmptType.getIpsProject());
            } catch (IpsException e) {
                // Consider elements equal if the product component types cannot be found
                IpsLog.log(e);
                return 0;
            }

            // Consider elements equal if the product component types cannot be found
            // Consider elements equal if both properties belong to the same product component type
            if (productCmptType1 == null || productCmptType2 == null || productCmptType1.equals(productCmptType2)) {
                return 0;
            }

            // Sort supertypes towards the beginning
            if (productCmptType1.isSubtypeOf(productCmptType2, productCmptType.getIpsProject())) {
                return 1;
            } else {
                return -1;
            }
        }

        /**
         * Compares the indices of the given product component properties in the list of property
         * references.
         * <p>
         * Properties whose indices are greater are sorted towards the end.
         */
        private int comparePropertyIndices(IProductCmptProperty property1, IProductCmptProperty property2) {
            IProductCmptType contextType = null;
            try {
                contextType = property1.findProductCmptType(property1.getIpsProject());
            } catch (IpsException e) {
                /*
                 * Consider the properties equal if the product component type containing the
                 * references cannot be found.
                 */
                IpsLog.log(e);
                return 0;
            }

            /*
             * Consider the properties equal if the product component type containing the references
             * cannot be found.
             */
            if (contextType == null) {
                return 0;
            }

            String category1 = findCategory(property1, contextType);
            String category2 = findCategory(property2, contextType);
            if (!category1.equals(category2)) {
                int categoryIndex1 = categories.indexOf(category1);
                int categoryIndex2 = categories.indexOf(category2);
                return categoryIndex1 - categoryIndex2;
            }

            int index1 = ((ProductCmptType)contextType).getCategoryPositionFor(property1);
            int index2 = ((ProductCmptType)contextType).getCategoryPositionFor(property2);

            // If no reference exists for a property, it is sorted towards the end
            if (index1 == -1) {
                index1 = Integer.MAX_VALUE;
            }
            if (index2 == -1) {
                index2 = Integer.MAX_VALUE;
            }

            return index1 - index2;
        }

        private String findCategory(IProductCmptProperty property, IProductCmptType contextType) {
            String category = ((ProductCmptType)contextType).getCategoryNameFor(property);
            if (IpsStringUtils.isBlank(category)) {
                IIpsProject ipsProject = contextType.getIpsProject();
                return switch (property.getProductCmptPropertyType()) {
                    case FORMULA_SIGNATURE_DEFINITION -> contextType
                            .findDefaultCategoryForFormulaSignatureDefinitions(ipsProject).getName();
                    case POLICY_CMPT_TYPE_ATTRIBUTE -> contextType
                            .findDefaultCategoryForPolicyCmptTypeAttributes(ipsProject).getName();
                    case PRODUCT_CMPT_TYPE_ATTRIBUTE -> contextType
                            .findDefaultCategoryForProductCmptTypeAttributes(ipsProject).getName();
                    case TABLE_STRUCTURE_USAGE -> contextType.findDefaultCategoryForTableStructureUsages(ipsProject)
                            .getName();
                    case VALIDATION_RULE -> contextType.findDefaultCategoryForValidationRules(ipsProject).getName();
                    default -> category;
                };
            }
            return category;
        }

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
        protected boolean visit(IProductCmptType currentType) {
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
