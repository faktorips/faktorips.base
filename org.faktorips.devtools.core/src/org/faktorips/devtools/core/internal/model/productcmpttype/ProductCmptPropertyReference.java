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
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
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

    private static final String XML_ATTRIBUTE_REFERENCED_PART_ID = "referencedPartId"; //$NON-NLS-1$

    private static final String XML_ATTRIBUTE_SOURCE_TYPE = "sourceType"; //$NON-NLS-1$

    private String referencedPartId;

    private SourceType sourceType;

    public ProductCmptPropertyReference(IProductCmptType parentProductCmptType, String id) {
        super(parentProductCmptType, id);
    }

    @Override
    public void setReferencedProperty(IProductCmptProperty property) {
        setReferencedPartId(property.getId());
        setSourceType(SourceType.getValueByProperty(property));
        objectHasChanged(ContentChangeEvent.newWholeContentChangedEvent(getIpsSrcFile()));
    }

    public void setReferencedPartId(String referencedPartId) {
        this.referencedPartId = referencedPartId;
    }

    public String getReferencedPartId() {
        return referencedPartId;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    @Override
    public boolean isReferencingProperty(IProductCmptProperty property) {
        if ((sourceType == SourceType.PRODUCT && property.isPolicyCmptTypeProperty())
                || (sourceType == SourceType.POLICY && !property.isPolicyCmptTypeProperty())) {
            return false;
        }
        return getReferencedPartId().equals(property.getId());
    }

    @Override
    public IProductCmptProperty findProductCmptProperty(IIpsProject ipsProject) throws CoreException {
        for (IProductCmptProperty property : getProductCmptType().findProductCmptProperties(false, ipsProject)) {
            if (isReferencingProperty(property)) {
                return property;
            }
        }
        return null;
    }

    @Override
    protected void initFromXml(Element element, String id) {
        referencedPartId = element.getAttribute(XML_ATTRIBUTE_REFERENCED_PART_ID);
        sourceType = SourceType.getValueByIdentifier(element.getAttribute(XML_ATTRIBUTE_SOURCE_TYPE));

        super.initFromXml(element, id);
    }

    @Override
    protected final void propertiesToXml(Element element) {
        super.propertiesToXml(element);

        element.setAttribute(XML_ATTRIBUTE_REFERENCED_PART_ID, referencedPartId);
        element.setAttribute(XML_ATTRIBUTE_SOURCE_TYPE, sourceType.identifier);
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(XML_TAG_NAME);
    }

    private IProductCmptType getProductCmptType() {
        return (IProductCmptType)getParent();
    }

    public static enum SourceType {

        POLICY("policy"), //$NON-NLS-1$

        PRODUCT("product"); //$NON-NLS-1$

        private static SourceType getValueByProperty(IProductCmptProperty property) {
            return property.isPolicyCmptTypeProperty() ? POLICY : PRODUCT;
        }

        private static SourceType getValueByIdentifier(String identifier) {
            for (SourceType type : values()) {
                if (type.identifier.equals(identifier)) {
                    return type;
                }
            }
            return null;
        }

        private final String identifier;

        private SourceType(String identifier) {
            this.identifier = identifier;
        }

    }

}
