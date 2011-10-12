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
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptPropertyDirectReference;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of {@link IProductCmptPropertyDirectReference}, please see the interface for more
 * details.
 * 
 * @author Alexander Weickmann
 */
public final class ProductCmptPropertyDirectReference extends ProductCmptPropertyReference implements
        IProductCmptPropertyDirectReference {

    private IProductCmptProperty productCmptProperty;

    public ProductCmptPropertyDirectReference(IProductCmptCategory parentCategory, String id) {
        super(parentCategory, id);
    }

    @Override
    public String getName() {
        return productCmptProperty.getName();
    }

    void setProductCmptProperty(IProductCmptProperty property) {
        productCmptProperty = property;
    }

    @Override
    public IProductCmptProperty getProductCmptProperty() {
        return productCmptProperty;
    }

    @Override
    public ProductCmptPropertyType getProductCmptPropertyType() {
        return productCmptProperty.getProductCmptPropertyType();
    }

    @Override
    public boolean isIdentifyingProperty(IProductCmptProperty property) {
        return productCmptProperty.equals(property);
    }

    @Override
    public IProductCmptProperty findReferencedProductCmptProperty(IIpsProject ipsProject) throws CoreException {
        return productCmptProperty;
    }

    @Override
    public void initFromXml(Element element) {
        String name = element.getAttribute(PROPERTY_NAME);
        ProductCmptPropertyType propertyType = ProductCmptPropertyType.valueOf(element.getAttribute(
                PROPERTY_PROPERTY_TYPE).toUpperCase());
        productCmptProperty = findReferencedProductCmptProperty(name, propertyType);

        super.initFromXml(element);
    }

    private IProductCmptProperty findReferencedProductCmptProperty(String name, ProductCmptPropertyType propertyType) {
        IProductCmptProperty productCmptProperty = null;
        switch (propertyType) {
            case PRODUCT_CMPT_TYPE_ATTRIBUTE:
                productCmptProperty = getProductCmptType().getProductCmptTypeAttribute(name);
                break;
            case TABLE_STRUCTURE_USAGE:
                productCmptProperty = getProductCmptType().getTableStructureUsage(name);
                break;
            case FORMULA_SIGNATURE_DEFINITION:
                productCmptProperty = getProductCmptType().getFormulaSignature(name);
                break;
            default:
                break;
        }
        return productCmptProperty;
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);

        element.setAttribute(PROPERTY_NAME, getName());
        element.setAttribute(PROPERTY_PROPERTY_TYPE, getProductCmptPropertyType().toString().toLowerCase());
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(XML_TAG_NAME);
    }

}
