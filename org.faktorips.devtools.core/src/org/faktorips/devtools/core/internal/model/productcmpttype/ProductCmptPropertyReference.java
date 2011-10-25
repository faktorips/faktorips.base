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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of {@link IProductCmptPropertyReference}, please see the interface for more
 * details.
 * 
 * @author Alexander Weickmann
 */
public final class ProductCmptPropertyReference extends AtomicIpsObjectPart implements IProductCmptPropertyReference {

    private String referencedPartId;

    private SourceType sourceType;

    public ProductCmptPropertyReference(IProductCmptType parentProductCmptType, String id) {
        super(parentProductCmptType, id);
    }

    @Override
    public void setReferencedPartId(String referencedPartId) {
        String oldValue = this.referencedPartId;
        this.referencedPartId = referencedPartId;
        valueChanged(oldValue, referencedPartId, PROPERTY_REFERENCED_PART_ID);
    }

    @Override
    public String getReferencedPartId() {
        return referencedPartId;
    }

    @Override
    public void setSourceType(SourceType sourceType) {
        SourceType oldValue = this.sourceType;
        this.sourceType = sourceType;
        valueChanged(oldValue, sourceType, PROPERTY_SOURCE_TYPE);
    }

    @Override
    public SourceType getSourceType() {
        return sourceType;
    }

    @Override
    public boolean isReferencingProperty(IProductCmptProperty property) {
        return getReferencedPartId().equals(property.getId());
    }

    @Override
    public IProductCmptProperty findProductCmptProperty(IIpsProject ipsProject) throws CoreException {
        for (IProductCmptProperty property : getProductCmptType().findProductCmptProperties(false, ipsProject)) {
            if ((property.getType() instanceof IPolicyCmptType && sourceType == SourceType.PRODUCT)
                    || property.getType() instanceof IProductCmptType && sourceType == SourceType.POLICY) {
                continue;
            }
            if (referencedPartId.equals(property.getId())) {
                return property;
            }
        }
        return null;
    }

    @Override
    protected void initFromXml(Element element, String id) {
        referencedPartId = element.getAttribute(PROPERTY_REFERENCED_PART_ID);
        sourceType = SourceType.getValueByIdentifier(element.getAttribute(PROPERTY_SOURCE_TYPE));

        super.initFromXml(element, id);
    }

    @Override
    protected final void propertiesToXml(Element element) {
        super.propertiesToXml(element);

        element.setAttribute(PROPERTY_REFERENCED_PART_ID, referencedPartId);
        element.setAttribute(PROPERTY_SOURCE_TYPE, sourceType.getIdentifier());
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(XML_TAG_NAME);
    }

    private IProductCmptType getProductCmptType() {
        return (IProductCmptType)getParent();
    }

}
