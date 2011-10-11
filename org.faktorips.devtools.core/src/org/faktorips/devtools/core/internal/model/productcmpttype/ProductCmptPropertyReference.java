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

import org.faktorips.devtools.core.internal.model.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptPropertyReference;
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

    public ProductCmptPropertyReference(IProductCmptCategory parentCategory, String id) {
        super(parentCategory, id);
    }

    @Override
    public void setName(String name) {
        String oldValue = this.name;
        this.name = name;
        valueChanged(oldValue, name);
    }

    @Override
    public void setProductCmptPropertyType(ProductCmptPropertyType propertyType) {
        ProductCmptPropertyType oldValue = this.propertyType;
        this.propertyType = propertyType;
        valueChanged(oldValue, propertyType);
    }

    @Override
    public ProductCmptPropertyType getProductCmptPropertyType() {
        return propertyType;
    }

    @Override
    public boolean isIdentifyingProperty(IProductCmptProperty property) {
        return getName().equals(property.getName()) && propertyType == property.getProductCmptPropertyType();
    }

    @Override
    protected void initFromXml(Element element, String id) {
        name = element.getAttribute(PROPERTY_NAME);
        propertyType = ProductCmptPropertyType.valueOf(element.getAttribute(PROPERTY_PROPERTY_TYPE).toUpperCase());

        super.initFromXml(element, id);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);

        element.setAttribute(PROPERTY_NAME, name);
        element.setAttribute(PROPERTY_PROPERTY_TYPE, propertyType.toString().toLowerCase());
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(XML_TAG_NAME);
    }

}
