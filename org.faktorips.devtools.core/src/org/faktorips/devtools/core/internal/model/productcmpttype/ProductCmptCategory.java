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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.type.TypeHierarchyVisitor;
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

    private final List<IProductCmptProperty> assignedProperties = new ArrayList<IProductCmptProperty>(5);

    private boolean inherited;

    private boolean defaultForMethods;

    private boolean defaultForValidationRules;

    private boolean defaultForTableStructureUsages;

    private boolean defaultForPolicyCmptTypeAttributes;

    private boolean defaultForProductCmptTypeAttributes;

    private Side side;

    public ProductCmptCategory(IProductCmptType parent, String id) {
        super(parent, id);
        side = Side.LEFT;
    }

    @Override
    public IProductCmptType getProductCmptType() {
        return (IProductCmptType)getParent();
    }

    @Override
    public List<IProductCmptProperty> getAssignedProductCmptProperties() {
        return Collections.unmodifiableList(assignedProperties);
    }

    @Override
    public boolean isAssignedProductCmptProperty(IProductCmptProperty property) {
        for (IProductCmptProperty assignedProperty : assignedProperties) {
            if (assignedProperty.equals(property)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<IProductCmptProperty> findAllAssignedProductCmptProperties(IIpsProject ipsProject) throws CoreException {
        // Collect all assigned properties from the supertype hierarchy
        final Map<IProductCmptType, List<IProductCmptProperty>> typesToAssignedProperties = new LinkedHashMap<IProductCmptType, List<IProductCmptProperty>>();
        TypeHierarchyVisitor<IProductCmptType> visitor = new TypeHierarchyVisitor<IProductCmptType>(ipsProject) {
            @Override
            protected boolean visit(IProductCmptType currentType) throws CoreException {
                IProductCmptCategory category = currentType.getProductCmptCategoryIncludeSupertypeCopies(getName());
                typesToAssignedProperties.put(currentType, category.getAssignedProductCmptProperties());
                return true;
            }
        };
        visitor.start(getProductCmptType());
        // Sort so that properties that are farther up in the hierarchy are listed at the top
        List<IProductCmptProperty> sortedAssignedProperties = new ArrayList<IProductCmptProperty>();
        for (int i = visitor.getVisited().size() - 1; i >= 0; i--) {
            IType type = visitor.getVisited().get(i);
            sortedAssignedProperties.addAll(typesToAssignedProperties.get(type));
        }
        return Collections.unmodifiableList(sortedAssignedProperties);
    }

    @Override
    public boolean findIsAssignedProductCmptProperty(IProductCmptProperty property, IIpsProject ipsProject)
            throws CoreException {

        for (IProductCmptProperty assignedProperty : findAllAssignedProductCmptProperties(ipsProject)) {
            if (assignedProperty.equals(property)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean assignProductCmptProperty(IProductCmptProperty productCmptProperty) {
        ArgumentCheck.isTrue(productCmptProperty.getType().equals(getProductCmptType()));
        if (assignedProperties.contains(productCmptProperty)) {
            return false;
        }
        return assignedProperties.add(productCmptProperty);
    }

    @Override
    public boolean removeProductCmptProperty(IProductCmptProperty productCmptProperty) {
        return assignedProperties.remove(productCmptProperty);
    }

    @Override
    public void setName(String name) {
        String oldValue = this.name;
        this.name = name;
        valueChanged(oldValue, name);
    }

    @Override
    public boolean isInherited() {
        return inherited;
    }

    @Override
    public void setInherited(boolean inherited) {
        boolean oldValue = this.inherited;
        this.inherited = inherited;
        valueChanged(oldValue, inherited);
    }

    @Override
    public boolean isDefaultForMethods() {
        return defaultForMethods;
    }

    @Override
    public void setDefaultForMethods(boolean defaultForMethods) {
        boolean oldValue = this.defaultForMethods;
        this.defaultForMethods = defaultForMethods;
        valueChanged(oldValue, defaultForMethods);
    }

    @Override
    public boolean isDefaultForPolicyCmptTypeAttributes() {
        return defaultForPolicyCmptTypeAttributes;
    }

    @Override
    public void setDefaultForPolicyCmptTypeAttributes(boolean defaultForPolicyCmptTypeAttributes) {
        boolean oldValue = this.defaultForPolicyCmptTypeAttributes;
        this.defaultForPolicyCmptTypeAttributes = defaultForPolicyCmptTypeAttributes;
        valueChanged(oldValue, defaultForPolicyCmptTypeAttributes);
    }

    @Override
    public boolean isDefaultForProductCmptTypeAttributes() {
        return defaultForProductCmptTypeAttributes;
    }

    @Override
    public void setDefaultForProductCmptTypeAttributes(boolean defaultForProductCmptTypeAttributes) {
        boolean oldValue = this.defaultForProductCmptTypeAttributes;
        this.defaultForProductCmptTypeAttributes = defaultForProductCmptTypeAttributes;
        valueChanged(oldValue, defaultForProductCmptTypeAttributes);
    }

    @Override
    public boolean isDefaultForTableStructureUsages() {
        return defaultForTableStructureUsages;
    }

    @Override
    public void setDefaultForTableStructureUsages(boolean defaultForTableStructureUsages) {
        boolean oldValue = this.defaultForTableStructureUsages;
        this.defaultForTableStructureUsages = defaultForTableStructureUsages;
        valueChanged(oldValue, defaultForTableStructureUsages);
    }

    @Override
    public boolean isDefaultForValidationRules() {
        return defaultForValidationRules;
    }

    @Override
    public void setDefaultForValidationRules(boolean defaultForValidationRules) {
        boolean oldValue = this.defaultForValidationRules;
        this.defaultForValidationRules = defaultForValidationRules;
        valueChanged(oldValue, defaultForValidationRules);
    }

    @Override
    public void setSide(Side side) {
        ArgumentCheck.notNull(side);

        Side oldValue = this.side;
        this.side = side;
        valueChanged(oldValue, side);
    }

    @Override
    public Side getSide() {
        return side;
    }

    @Override
    public boolean isAtLeftSide() {
        return side == Side.LEFT;
    }

    @Override
    public boolean isAtRightSide() {
        return side == Side.RIGHT;
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        // TODO Auto-generated method stub
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        name = element.getAttribute(PROPERTY_NAME);
        inherited = Boolean.parseBoolean(element.getAttribute(PROPERTY_INHERITED));
        defaultForMethods = Boolean.parseBoolean(element.getAttribute(PROPERTY_DEFAULT_FOR_METHODS));
        defaultForPolicyCmptTypeAttributes = Boolean.parseBoolean(element
                .getAttribute(PROPERTY_DEFAULT_FOR_POLICY_CMPT_TYPE_ATTRIBUTES));
        defaultForProductCmptTypeAttributes = Boolean.parseBoolean(element
                .getAttribute(PROPERTY_DEFAULT_FOR_PRODUCT_CMPT_TYPE_ATTRIBUTES));
        defaultForTableStructureUsages = Boolean.parseBoolean(element
                .getAttribute(PROPERTY_DEFAULT_FOR_TABLE_STRUCTURE_USAGES));
        defaultForValidationRules = Boolean.parseBoolean(element.getAttribute(PROPERTY_DEFAULT_FOR_VALIDATION_RULES));
        side = Side.valueOf(element.getAttribute(PROPERTY_SIDE).toUpperCase());

        super.initPropertiesFromXml(element, id);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);

        element.setAttribute(PROPERTY_NAME, name);
        element.setAttribute(PROPERTY_INHERITED, Boolean.toString(inherited));
        element.setAttribute(PROPERTY_DEFAULT_FOR_METHODS, Boolean.toString(defaultForMethods));
        element.setAttribute(PROPERTY_DEFAULT_FOR_POLICY_CMPT_TYPE_ATTRIBUTES,
                Boolean.toString(defaultForPolicyCmptTypeAttributes));
        element.setAttribute(PROPERTY_DEFAULT_FOR_PRODUCT_CMPT_TYPE_ATTRIBUTES,
                Boolean.toString(defaultForProductCmptTypeAttributes));
        element.setAttribute(PROPERTY_DEFAULT_FOR_TABLE_STRUCTURE_USAGES,
                Boolean.toString(defaultForTableStructureUsages));
        element.setAttribute(PROPERTY_DEFAULT_FOR_VALIDATION_RULES, Boolean.toString(defaultForValidationRules));
        element.setAttribute(PROPERTY_SIDE, side.toString().toLowerCase());
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(XML_TAG_NAME);
    }

}
