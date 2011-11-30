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
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptPropertyReference;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Default implementation of {@link IProductCmptPropertyReference}.
 * 
 * @author Alexander Weickmann, Faktor Zehn AG
 */
public class ProductCmptPropertyReference extends AtomicIpsObjectPart implements IProductCmptPropertyReference {

    final static String XML_TAG_NAME = "ProductCmptPropertyReference"; //$NON-NLS-1$

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
    public IProductCmptProperty findProductCmptProperty(IIpsProject ipsProject) throws CoreException {
        return ((ProductCmptType)getProductCmptType()).findProductCmptProperty(this, ipsProject);
    }

    @Override
    protected void initFromXml(Element element, String id) {
        referencedPartId = element.getAttribute(PROPERTY_REFERENCED_PART_ID);
        referencedIpsObjectType = IpsObjectType.getTypeForName(element
                .getAttribute(PROPERTY_REFERENCED_IPS_OBJECT_TYPE));

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
