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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.internal.model.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;
import org.faktorips.devtools.core.model.type.TypeHierarchyVisitor;
import org.faktorips.runtime.internal.StringUtils;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of {@link IProductCmptProperty}, please see the interface for more details.
 * 
 * @author Alexander Weickmann
 */
public final class ProductCmptCategory extends AtomicIpsObjectPart implements IProductCmptCategory {

    private boolean defaultForFormulaSignatureDefinitions;

    private boolean defaultForValidationRules;

    private boolean defaultForTableStructureUsages;

    private boolean defaultForPolicyCmptTypeAttributes;

    private boolean defaultForProductCmptTypeAttributes;

    private Position position;

    public ProductCmptCategory(final IProductCmptType parent, String id) {
        super(parent, id);
        position = Position.LEFT;
    }

    @Override
    public IProductCmptType getProductCmptType() {
        return (IProductCmptType)getParent();
    }

    @Override
    public List<IProductCmptProperty> findProductCmptProperties(IProductCmptType contextType, IIpsProject ipsProject)
            throws CoreException {

        List<IProductCmptProperty> properties = new ArrayList<IProductCmptProperty>();
        for (IProductCmptProperty property : ((ProductCmptType)contextType)
                .findProductCmptPropertiesInReferencedOrder(ipsProject)) {
            if (property.getCategory().equals(name)
                    || (property.getCategory().isEmpty() && isDefaultFor(property.getProductCmptPropertyType()))) {
                properties.add(property);
            }
        }
        return properties;
    }

    private boolean isDefaultFor(ProductCmptPropertyType propertyType) {
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
        ArgumentCheck.notNull(side);

        Position oldValue = this.position;
        this.position = side;
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

        boolean valid = !isNameAlreadyUsedInThisType();
        if (valid) {
            valid = !isNameAlreadyUsedInSupertypeHierarchy(ipsProject);
        }

        if (!valid) {
            String text = NLS.bind(Messages.ProductCmptCategory_msgNameAlreadyUsedInTypeHierarchy, name,
                    getProductCmptType().getName());
            list.newError(MSGCODE_NAME_ALREADY_USED_IN_TYPE_HIERARCHY, text, this, PROPERTY_NAME);
        }

        return valid;
    }

    private boolean isNameAlreadyUsedInThisType() {
        for (IProductCmptCategory category : getProductCmptType().getProductCmptCategories()) {
            if (category == this) {
                continue;
            }
            if (category.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    private boolean isNameAlreadyUsedInSupertypeHierarchy(IIpsProject ipsProject) throws CoreException {
        if (getProductCmptType().hasSupertype()) {
            IProductCmptType superProductCmptType = getProductCmptType().findSuperProductCmptType(ipsProject);
            if (superProductCmptType != null) {
                if (((ProductCmptType)superProductCmptType).findProductCmptCategory(name, ipsProject) != null) {
                    return true;
                }
            }
        }
        return false;
    }

    private void validateDuplicateDefaultsForFormulaSignatureDefinitions(MessageList list, IIpsProject ipsProject)
            throws CoreException {

        if (!defaultForFormulaSignatureDefinitions) {
            return;
        }

        DuplicateDefaultFinder duplicateFinder = new DuplicateDefaultFinder(ipsProject) {
            @Override
            protected boolean isDefault(IProductCmptCategory category) {
                return category.isDefaultForFormulaSignatureDefinitions();
            }
        };
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

        DuplicateDefaultFinder duplicateFinder = new DuplicateDefaultFinder(ipsProject) {
            @Override
            protected boolean isDefault(IProductCmptCategory category) {
                return category.isDefaultForValidationRules();
            }
        };
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

        DuplicateDefaultFinder duplicateFinder = new DuplicateDefaultFinder(ipsProject) {
            @Override
            protected boolean isDefault(IProductCmptCategory category) {
                return category.isDefaultForTableStructureUsages();
            }
        };
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

        DuplicateDefaultFinder duplicateFinder = new DuplicateDefaultFinder(ipsProject) {
            @Override
            protected boolean isDefault(IProductCmptCategory category) {
                return category.isDefaultForPolicyCmptTypeAttributes();
            }
        };
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

        DuplicateDefaultFinder duplicateFinder = new DuplicateDefaultFinder(ipsProject) {
            @Override
            protected boolean isDefault(IProductCmptCategory category) {
                return category.isDefaultForProductCmptTypeAttributes();
            }
        };
        duplicateFinder.start(getProductCmptType());
        duplicateFinder.addValidationMessageIfDuplicateFound(list,
                MSGCODE_DUPLICATE_DEFAULTS_FOR_PRODUCT_CMPT_TYPE_ATTRIBUTES,
                Messages.ProductCmptCategory_DuplicateDefaultsForProductCmptTypeAttributes,
                PROPERTY_DEFAULT_FOR_PRODUCT_CMPT_TYPE_ATTRIBUTES);
    }

    @Override
    public boolean moveProductCmptProperties(List<IProductCmptProperty> properties, boolean up) throws CoreException {
        if (properties.isEmpty()) {
            return false;
        }
        IProductCmptType productCmptType = properties.get(0).findProductCmptType(getIpsProject());
        for (IProductCmptProperty property : properties) {
            ArgumentCheck.equals(productCmptType, property.findProductCmptType(getIpsProject()));
        }
        return ((ProductCmptType)productCmptType).moveProductCmptPropertyReferences(properties, up);
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
        position = Position.valueOf(element.getAttribute(PROPERTY_POSITION).toUpperCase());

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
        element.setAttribute(PROPERTY_POSITION, position.toString().toLowerCase());
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(XML_TAG_NAME);
    }

    private abstract class DuplicateDefaultFinder extends TypeHierarchyVisitor<IProductCmptType> {

        private boolean duplicateDefaultFound;

        protected DuplicateDefaultFinder(IIpsProject ipsProject) {
            super(ipsProject);
        }

        @Override
        protected boolean visit(IProductCmptType currentType) throws CoreException {
            for (IProductCmptCategory category : currentType.getProductCmptCategories()) {
                if (isDefault(category) && !category.getName().equals(name)) {
                    duplicateDefaultFound = true;
                    return false;
                }
            }
            return true;
        }

        protected abstract boolean isDefault(IProductCmptCategory category);

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
