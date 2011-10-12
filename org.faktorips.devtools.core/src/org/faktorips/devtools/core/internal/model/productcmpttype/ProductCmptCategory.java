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
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPart;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptPropertyDirectReference;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptPropertyExternalReference;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptPropertyReference;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
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
public final class ProductCmptCategory extends IpsObjectPart implements IProductCmptCategory {

    /*
     * Cannot use IpsObjectPartCollection as there are multiple implementations that need to be
     * stored in one collection in order to preserve the insert ordering.
     */
    private final List<IProductCmptPropertyReference> propertyReferences = new ArrayList<IProductCmptPropertyReference>();

    private boolean inherited;

    private boolean defaultForMethods;

    private boolean defaultForValidationRules;

    private boolean defaultForTableStructureUsages;

    private boolean defaultForPolicyCmptTypeAttributes;

    private boolean defaultForProductCmptTypeAttributes;

    private Position position;

    public ProductCmptCategory(IProductCmptType parent, String id) {
        super(parent, id);
        position = Position.LEFT;
    }

    @Override
    public IProductCmptType getProductCmptType() {
        return (IProductCmptType)getParent();
    }

    @Override
    public List<IProductCmptProperty> findReferencedProductCmptProperties(IIpsProject ipsProject) throws CoreException {
        List<IProductCmptProperty> properties = new ArrayList<IProductCmptProperty>(propertyReferences.size());
        for (IProductCmptPropertyReference reference : propertyReferences) {
            properties.add(reference.findReferencedProductCmptProperty(ipsProject));
        }
        return properties;
    }

    @Override
    public boolean isReferencedProductCmptProperty(IProductCmptProperty property) {
        for (IProductCmptPropertyReference reference : propertyReferences) {
            if (reference.isIdentifyingProperty(property)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<IProductCmptProperty> findAllReferencedProductCmptProperties(IIpsProject ipsProject)
            throws CoreException {

        if (!isInherited()) {
            return findReferencedProductCmptProperties(ipsProject);
        }

        // Collect all assigned properties from the supertype hierarchy
        final Map<IProductCmptType, List<IProductCmptProperty>> typesToAssignedProperties = new LinkedHashMap<IProductCmptType, List<IProductCmptProperty>>();
        TypeHierarchyVisitor<IProductCmptType> visitor = new TypeHierarchyVisitor<IProductCmptType>(ipsProject) {
            @Override
            protected boolean visit(IProductCmptType currentType) throws CoreException {
                IProductCmptCategory category = currentType.getProductCmptCategoryIncludeSupertypeCopies(getName());
                typesToAssignedProperties.put(currentType, category.findReferencedProductCmptProperties(ipsProject));
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
    public boolean findIsReferencedProductCmptProperty(final IProductCmptProperty property, IIpsProject ipsProject)
            throws CoreException {

        if (!isInherited()) {
            return isReferencedProductCmptProperty(property);
        }

        class ProductCmptPropertyFinder extends TypeHierarchyVisitor<IProductCmptType> {

            public ProductCmptPropertyFinder(IIpsProject ipsProject) {
                super(ipsProject);
            }

            private boolean referenced;

            @Override
            protected boolean visit(IProductCmptType currentType) throws CoreException {
                IProductCmptCategory category = currentType.getProductCmptCategoryIncludeSupertypeCopies(getName());
                if (category != null && category.isReferencedProductCmptProperty(property)) {
                    referenced = true;
                    return false;
                }
                return true;
            }

        }

        ProductCmptPropertyFinder finder = new ProductCmptPropertyFinder(ipsProject);
        finder.start(getProductCmptType());
        return finder.referenced;
    }

    @Override
    public IProductCmptPropertyDirectReference newProductCmptPropertyReference(IProductCmptTypeAttribute productCmptTypeAttribute) {
        ArgumentCheck.equals(productCmptTypeAttribute.getProductCmptType(), getProductCmptType());
        return newProductCmptPropertyDirectReference(productCmptTypeAttribute);
    }

    @Override
    public IProductCmptPropertyExternalReference newProductCmptPropertyReference(IPolicyCmptTypeAttribute policyCmptTypeAttribute) {
        ArgumentCheck.equals(policyCmptTypeAttribute.getPolicyCmptType().getQualifiedName(), getProductCmptType()
                .getPolicyCmptType());
        ArgumentCheck.isTrue(policyCmptTypeAttribute.isProductRelevant());
        return newProductCmptPropertyExternalReference(policyCmptTypeAttribute);
    }

    @Override
    public IProductCmptPropertyDirectReference newProductCmptPropertyReference(IProductCmptTypeMethod productCmptTypeMethod) {
        ArgumentCheck.equals(productCmptTypeMethod.getProductCmptType(), getProductCmptType());
        ArgumentCheck.isTrue(productCmptTypeMethod.isFormulaSignatureDefinition());
        return newProductCmptPropertyDirectReference(productCmptTypeMethod);
    }

    @Override
    public IProductCmptPropertyDirectReference newProductCmptPropertyReference(ITableStructureUsage tableStructureUsage) {
        ArgumentCheck.equals(tableStructureUsage.getProductCmptType(), getProductCmptType());
        return newProductCmptPropertyDirectReference(tableStructureUsage);
    }

    @Override
    public IProductCmptPropertyExternalReference newProductCmptPropertyReference(IValidationRule validationRule) {
        ArgumentCheck.equals(validationRule.getType().getQualifiedName(), getProductCmptType().getPolicyCmptType());
        return newProductCmptPropertyExternalReference(validationRule);
    }

    private IProductCmptPropertyExternalReference newProductCmptPropertyExternalReference(IProductCmptProperty productCmptProperty) {
        IProductCmptPropertyExternalReference reference = (IProductCmptPropertyExternalReference)newPart(ProductCmptPropertyExternalReference.class);
        reference.setName(productCmptProperty.getPropertyName());
        reference.setProductCmptPropertyType(productCmptProperty.getProductCmptPropertyType());
        return reference;
    }

    private IProductCmptPropertyDirectReference newProductCmptPropertyDirectReference(IProductCmptProperty productCmptProperty) {
        IProductCmptPropertyDirectReference reference = (IProductCmptPropertyDirectReference)newPart(ProductCmptPropertyDirectReference.class);
        ((ProductCmptPropertyDirectReference)reference).setProductCmptProperty(productCmptProperty);
        return reference;
    }

    @Override
    public boolean deleteProductCmptPropertyReference(IProductCmptProperty productCmptProperty) {
        for (IProductCmptPropertyReference reference : propertyReferences) {
            if (reference.isIdentifyingProperty(productCmptProperty)) {
                reference.delete();
                return true;
            }
        }
        return false;
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
    public void setPosition(Position side) {
        ArgumentCheck.notNull(side);

        Position oldValue = this.position;
        this.position = side;
        valueChanged(oldValue, side);
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
        position = Position.valueOf(element.getAttribute(PROPERTY_POSITION).toUpperCase());

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
        element.setAttribute(PROPERTY_POSITION, position.toString().toLowerCase());
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(XML_TAG_NAME);
    }

    @Override
    protected IIpsElement[] getChildrenThis() {
        IIpsElement[] children = new IIpsElement[propertyReferences.size()];
        for (int i = 0; i < children.length; i++) {
            children[i] = propertyReferences.get(i);
        }
        return children;
    }

    @Override
    protected void reinitPartCollectionsThis() {
        propertyReferences.clear();
    }

    @Override
    protected boolean addPartThis(IIpsObjectPart part) {
        if (part instanceof IProductCmptPropertyReference) {
            propertyReferences.add((IProductCmptPropertyReference)part);
            return true;
        }
        return false;
    }

    @Override
    protected boolean removePartThis(IIpsObjectPart part) {
        if (part instanceof IProductCmptPropertyReference) {
            return propertyReferences.remove(part);
        }
        return false;
    }

    @Override
    protected IIpsObjectPart newPartThis(Element xmlTag, String id) {
        String nodeName = xmlTag.getNodeName();
        if (nodeName.equals(IProductCmptPropertyDirectReference.XML_TAG_NAME)) {
            return newPart(ProductCmptPropertyDirectReference.class);
        } else if (nodeName.equals(IProductCmptPropertyExternalReference.XML_TAG_NAME)) {
            return newPart(ProductCmptPropertyExternalReference.class);
        }
        return null;
    }

    @Override
    protected IIpsObjectPart newPartThis(Class<? extends IIpsObjectPart> partType) {
        IProductCmptPropertyReference reference = null;
        if (partType == ProductCmptPropertyDirectReference.class) {
            reference = new ProductCmptPropertyDirectReference(this, getNextPartId());
            propertyReferences.add(reference);
        } else if (partType == ProductCmptPropertyExternalReference.class) {
            reference = new ProductCmptPropertyExternalReference(this, getNextPartId());
            propertyReferences.add(reference);
        }
        return reference;
    }

}
