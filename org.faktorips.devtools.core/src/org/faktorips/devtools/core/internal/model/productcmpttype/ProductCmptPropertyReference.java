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
import org.faktorips.devtools.core.model.type.IType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Default implementation of {@link IProductCmptPropertyReference}.
 * <p>
 * This implementation uses the part id of the referenced {@link IProductCmptProperty}. As the part
 * id is not necessarily unique across types, and because product component types also store
 * references for properties originating from the supertype hierarchy, the {@link QualifiedNameType}
 * of the poperty's {@link IType} is stored as well.
 * 
 * @author Alexander Weickmann
 */
public class ProductCmptPropertyReference extends AtomicIpsObjectPart implements IProductCmptPropertyReference {

    final static String XML_TAG_NAME = "ProductCmptPropertyReference"; //$NON-NLS-1$

    private static final String XML_ATTRIBUTE_REFERENCED_PART_ID = "referencedPartId"; //$NON-NLS-1$

    private static final String XML_ATTRIBUTE_QUALIFIED_TYPE_NAME = "qualifiedTypeName"; //$NON-NLS-1$

    private static final String XML_ATTRIBUTE_IPS_OBJECT_TYPE = "ipsObjectType"; //$NON-NLS-1$

    private String referencedPartId = ""; //$NON-NLS-1$

    // TODO AW 24-11-11: Depends on must be updated
    private QualifiedNameType qualifiedNameType = new NullQualifiedNameType();

    public ProductCmptPropertyReference(IProductCmptType parent, String id) {
        super(parent, id);
    }

    @Override
    public void setReferencedProperty(IProductCmptProperty property) {
        referencedPartId = property.getId();
        qualifiedNameType = property.getType().getQualifiedNameType();
        objectHasChanged(ContentChangeEvent.newWholeContentChangedEvent(getIpsSrcFile()));
    }

    /**
     * Returns the part id of the referenced {@link IProductCmptProperty}.
     */
    String getReferencedPartId() {
        return referencedPartId;
    }

    /**
     * Returns a {@link QualifiedNameType} describing the origin of the referenced
     * {@link IProductCmptProperty}.
     */
    QualifiedNameType getQualifiedNameType() {
        return qualifiedNameType;
    }

    @Override
    public boolean isReferencingProperty(IProductCmptProperty property) {
        return isEqualQualifiedNameType(property) && isEqualId(property);
    }

    private boolean isEqualQualifiedNameType(IProductCmptProperty property) {
        return property.isOfType(qualifiedNameType);
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
        referencedPartId = element.getAttribute(XML_ATTRIBUTE_REFERENCED_PART_ID);
        String qualifiedTypeName = element.getAttribute(XML_ATTRIBUTE_QUALIFIED_TYPE_NAME);
        IpsObjectType ipsObjectType = IpsObjectType.getTypeForName(element.getAttribute(XML_ATTRIBUTE_IPS_OBJECT_TYPE));
        qualifiedNameType = new QualifiedNameType(qualifiedTypeName, ipsObjectType);

        super.initFromXml(element, id);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);

        element.setAttribute(XML_ATTRIBUTE_REFERENCED_PART_ID, referencedPartId);
        element.setAttribute(XML_ATTRIBUTE_QUALIFIED_TYPE_NAME, qualifiedNameType.getName());
        element.setAttribute(XML_ATTRIBUTE_IPS_OBJECT_TYPE, qualifiedNameType.getIpsObjectType().getId());
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(XML_TAG_NAME);
    }

    private IProductCmptType getProductCmptType() {
        return (IProductCmptType)getParent();
    }

}
