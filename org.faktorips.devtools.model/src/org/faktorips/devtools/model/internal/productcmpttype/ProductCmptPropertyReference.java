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

import org.faktorips.devtools.model.ContentChangeEvent;
import org.faktorips.devtools.model.internal.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptPropertyReference;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.type.IProductCmptProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Default implementation of {@link IProductCmptPropertyReference}.
 */
public class ProductCmptPropertyReference extends AtomicIpsObjectPart implements IProductCmptPropertyReference {

    static final String XML_TAG_NAME = "ProductCmptPropertyReference"; //$NON-NLS-1$

    private String referencedPartId = ""; //$NON-NLS-1$

    private IpsObjectType referencedIpsObjectType = new NullIpsObjectType();

    public ProductCmptPropertyReference(IProductCmptType parent, String id) {
        super(parent, id);
    }

    @Override
    public void setReferencedProperty(IProductCmptProperty property) {
        referencedPartId = property.getId();
        referencedIpsObjectType = property.getType().getIpsObjectType();
        objectHasChanged(ContentChangeEvent.newWholeContentChangedEvent(getIpsSrcFile()));
    }

    @Override
    public String getReferencedPartId() {
        return referencedPartId;
    }

    @Override
    public void setReferencedPartId(String partId) {
        String oldValue = referencedPartId;
        referencedPartId = partId;
        valueChanged(oldValue, partId, PROPERTY_REFERENCED_PART_ID);
    }

    @Override
    public IpsObjectType getReferencedIpsObjectType() {
        return referencedIpsObjectType;
    }

    @Override
    public void setReferencedIpsObjectType(IpsObjectType ipsObjectType) {
        IpsObjectType oldValue = referencedIpsObjectType;
        referencedIpsObjectType = ipsObjectType;
        valueChanged(oldValue, ipsObjectType, PROPERTY_REFERENCED_IPS_OBJECT_TYPE);
    }

    @Override
    public boolean isReferencedProperty(IProductCmptProperty property) {
        return isEqualIpsObjectType(property) && isEqualId(property) && isEqualProductCmptType(property);
    }

    private boolean isEqualIpsObjectType(IProductCmptProperty property) {
        return referencedIpsObjectType.equals(property.getType().getIpsObjectType());
    }

    private boolean isEqualId(IProductCmptProperty property) {
        return referencedPartId.equals(property.getId());
    }

    private boolean isEqualProductCmptType(IProductCmptProperty property) {
        if (property.isPolicyCmptTypeProperty()) {
            IPolicyCmptType policyCmptType = (IPolicyCmptType)property.getType();
            return getProductCmptType().getQualifiedName().equals(policyCmptType.getProductCmptType());
        } else {
            return property.isOfType(getProductCmptType().getQualifiedNameType());
        }
    }

    @Override
    public IProductCmptProperty findProductCmptProperty(IIpsProject ipsProject) {
        return ((ProductCmptType)getProductCmptType()).findProductCmptProperty(this, ipsProject);
    }

    @Override
    protected void initFromXml(Element element, String id) {
        referencedPartId = element.getAttribute(PROPERTY_REFERENCED_PART_ID);
        referencedIpsObjectType = IpsObjectType
                .getTypeForName(element.getAttribute(PROPERTY_REFERENCED_IPS_OBJECT_TYPE));

        super.initFromXml(element, id);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);

        element.setAttribute(PROPERTY_REFERENCED_PART_ID, referencedPartId);
        element.setAttribute(PROPERTY_REFERENCED_IPS_OBJECT_TYPE, referencedIpsObjectType.getId());
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(XML_TAG_NAME);
    }

    private IProductCmptType getProductCmptType() {
        return (IProductCmptType)getParent();
    }

    private static class NullIpsObjectType extends IpsObjectType {

        protected NullIpsObjectType() {
            super("", "", "", "", "", false, false, null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        }

    }

}
