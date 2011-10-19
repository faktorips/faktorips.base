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

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptPropertyReference;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of {@link IProductCmptPropertyReference}, please see the interface for more
 * details.
 * 
 * @author Alexander Weickmann
 */
public final class ProductCmptPropertyReference extends AtomicIpsObjectPart implements IProductCmptPropertyReference {

    private ProductCmptPropertyType propertyType;

    public ProductCmptPropertyReference(IProductCmptType parentProductCmptType, String id) {
        super(parentProductCmptType, id);
    }

    @Override
    public void setName(String name) {
        String oldValue = this.name;
        this.name = name;
        valueChanged(oldValue, name, PROPERTY_NAME);
    }

    @Override
    public void setProductCmptPropertyType(ProductCmptPropertyType propertyType) {
        ProductCmptPropertyType oldValue = this.propertyType;
        this.propertyType = propertyType;
        valueChanged(oldValue, propertyType, PROPERTY_PROPERTY_TYPE);
    }

    @Override
    public ProductCmptPropertyType getProductCmptPropertyType() {
        return propertyType;
    }

    @Override
    public boolean isReferencingProperty(IProductCmptProperty property) {
        return getName().equals(property.getName()) && propertyType == property.getProductCmptPropertyType();
    }

    @Override
    public IProductCmptProperty findProductCmptProperty(IIpsProject ipsProject) throws CoreException {
        IProductCmptProperty property = null;

        // First, try to find the property in the product component type
        switch (propertyType) {
            case PRODUCT_CMPT_TYPE_ATTRIBUTE:
                property = getProductCmptType().getProductCmptTypeAttribute(name);
                break;
            case FORMULA_SIGNATURE_DEFINITION:
                property = getProductCmptType().getFormulaSignature(name);
                break;
            case TABLE_STRUCTURE_USAGE:
                property = getProductCmptType().getTableStructureUsage(name);
                break;
            default:
                break;
        }

        // If not found, search the property in the policy component type (more expensive)
        if (property == null) {
            IPolicyCmptType policyCmptType = getProductCmptType().findPolicyCmptType(ipsProject);
            switch (propertyType) {
                case POLICY_CMPT_TYPE_ATTRIBUTE:
                    property = policyCmptType.getPolicyCmptTypeAttribute(name);
                    break;
                case VALIDATION_RULE:
                    property = policyCmptType.getValidationRule(name);
                    break;
                default:
                    break;
            }
        }

        return property;
    }

    @Override
    protected void initFromXml(Element element, String id) {
        name = element.getAttribute(PROPERTY_NAME);
        propertyType = ProductCmptPropertyType.getValueById(element.getAttribute(PROPERTY_PROPERTY_TYPE));

        super.initFromXml(element, id);
    }

    @Override
    protected final void propertiesToXml(Element element) {
        super.propertiesToXml(element);

        element.setAttribute(PROPERTY_NAME, getName());
        element.setAttribute(PROPERTY_PROPERTY_TYPE, getProductCmptPropertyType().getId());
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(XML_TAG_NAME);
    }

    private IProductCmptType getProductCmptType() {
        return (IProductCmptType)getParent();
    }

}
