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
import org.faktorips.devtools.core.model.ipsobject.NullQualifiedNameType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptPropertyReference;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Default implementation of {@link IProductCmptPropertyReference}.
 * 
 * @author Alexander Weickmann
 */
public class ProductCmptPropertyReference extends AtomicIpsObjectPart implements IProductCmptPropertyReference {

    final static String XML_TAG_NAME = "ProductCmptPropertyReference"; //$NON-NLS-1$

    private String referencedPartId = ""; //$NON-NLS-1$

    private QualifiedNameType referencedQualifiedNameType = new NullQualifiedNameType();

    public ProductCmptPropertyReference(IProductCmptType parent, String id) {
        super(parent, id);
    }

    @Override
    public void setReferencedProperty(IProductCmptProperty property) {
        referencedPartId = property.getId();
        referencedQualifiedNameType = property.getType().getQualifiedNameType();
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
    public String getReferencedType() {
        return referencedQualifiedNameType.getName();
    }

    @Override
    public void setReferencedType(String qualifiedTypeName) {
        String oldValue = referencedQualifiedNameType.getName();
        referencedQualifiedNameType = new QualifiedNameType(qualifiedTypeName,
                referencedQualifiedNameType.getIpsObjectType());
        valueChanged(oldValue, qualifiedTypeName, PROPERTY_REFERENCED_TYPE);
    }

    @Override
    public IpsObjectType getReferencedIpsObjectType() {
        return referencedQualifiedNameType.getIpsObjectType();
    }

    @Override
    public void setReferencedIpsObjectType(IpsObjectType ipsObjectType) {
        IpsObjectType oldValue = referencedQualifiedNameType.getIpsObjectType();
        referencedQualifiedNameType = new QualifiedNameType(referencedQualifiedNameType.getName(), ipsObjectType);
        valueChanged(oldValue, ipsObjectType, PROPERTY_REFERENCED_IPS_OBJECT_TYPE);
    }

    @Override
    public boolean isReferencedProperty(IProductCmptProperty property) {
        return isEqualQualifiedNameType(property) && isEqualId(property);
    }

    private boolean isEqualQualifiedNameType(IProductCmptProperty property) {
        return property.isOfType(referencedQualifiedNameType);
    }

    private boolean isEqualId(IProductCmptProperty property) {
        return getReferencedPartId().equals(property.getId());
    }

    @Override
    public IProductCmptProperty findProductCmptProperty(IIpsProject ipsProject) throws CoreException {
        return ((ProductCmptType)getProductCmptType()).findProductCmptProperty(this, ipsProject);
    }

    @Override
    protected void initFromXml(Element element, String id) {
        referencedPartId = element.getAttribute(PROPERTY_REFERENCED_PART_ID);
        String qualifiedTypeName = element.getAttribute(PROPERTY_REFERENCED_TYPE);
        IpsObjectType ipsObjectType = IpsObjectType.getTypeForName(element
                .getAttribute(PROPERTY_REFERENCED_IPS_OBJECT_TYPE));
        referencedQualifiedNameType = new QualifiedNameType(qualifiedTypeName, ipsObjectType);

        super.initFromXml(element, id);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);

        element.setAttribute(PROPERTY_REFERENCED_PART_ID, referencedPartId);
        element.setAttribute(PROPERTY_REFERENCED_TYPE, referencedQualifiedNameType.getName());
        element.setAttribute(PROPERTY_REFERENCED_IPS_OBJECT_TYPE, referencedQualifiedNameType.getIpsObjectType()
                .getId());
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(XML_TAG_NAME);
    }

    private IProductCmptType getProductCmptType() {
        return (IProductCmptType)getParent();
    }

}
