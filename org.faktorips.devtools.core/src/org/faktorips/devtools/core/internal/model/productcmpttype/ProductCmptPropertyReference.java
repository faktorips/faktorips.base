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
 * Default implementation of {@link IProductCmptPropertyReference}.
 * <p>
 * This implementation uses the part id of the referenced {@link IProductCmptProperty}. As the part
 * id is not necessarily unique across types, it is furthermore stored whether the
 * {@link IProductCmptProperty} originates from the product side or the policy side.
 * 
 * @author Alexander Weickmann
 */
public class ProductCmptPropertyReference extends AtomicIpsObjectPart implements IProductCmptPropertyReference {

    final static String XML_TAG_NAME = "ProductCmptPropertyReference"; //$NON-NLS-1$

    private static final String XML_ATTRIBUTE_REFERENCED_PART_ID = "referencedPartId"; //$NON-NLS-1$

    private static final String XML_ATTRIBUTE_SOURCE_TYPE = "sourceType"; //$NON-NLS-1$

    private String referencedPartId;

    private SourceType sourceType;

    public ProductCmptPropertyReference(IProductCmptType parent, String id) {
        super(parent, id);
    }

    @Override
    public void setReferencedProperty(IProductCmptProperty property) {
        setReferencedPartId(property.getId());
        setSourceType(SourceType.getValueByProperty(property));
        objectHasChanged(ContentChangeEvent.newWholeContentChangedEvent(getIpsSrcFile()));
    }

    /**
     * Returns the part id of the referenced {@link IProductCmptProperty}.
     */
    String getReferencedPartId() {
        return referencedPartId;
    }

    private void setReferencedPartId(String referencedPartId) {
        this.referencedPartId = referencedPartId;
    }

    /**
     * Returns whether the referenced {@link IProductCmptProperty} originates from the product side
     * or the policy side.
     * 
     * @see SourceType
     */
    SourceType getSourceType() {
        return sourceType;
    }

    private void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    @Override
    public boolean isReferencingProperty(IProductCmptProperty property) {
        return isEqualSourceType(property) && isEqualId(property) && isEqualProductCmptType(property);
    }

    private boolean isEqualSourceType(IProductCmptProperty property) {
        return (sourceType == SourceType.POLICY && property.isPolicyCmptTypeProperty())
                || (sourceType == SourceType.PRODUCT && !property.isPolicyCmptTypeProperty());
    }

    private boolean isEqualId(IProductCmptProperty property) {
        return getReferencedPartId().equals(property.getId());
    }

    private boolean isEqualProductCmptType(IProductCmptProperty property) {
        return property.isOfType(getProductCmptType().getQualifiedName())
                || property.isOfType(getProductCmptType().getPolicyCmptType());
    }

    @Override
    public IProductCmptProperty findProductCmptProperty(IIpsProject ipsProject) throws CoreException {
        return ((ProductCmptType)getProductCmptType()).findProductCmptProperty(this, ipsProject);
    }

    @Override
    protected void initFromXml(Element element, String id) {
        referencedPartId = element.getAttribute(XML_ATTRIBUTE_REFERENCED_PART_ID);
        sourceType = SourceType.getValueByIdentifier(element.getAttribute(XML_ATTRIBUTE_SOURCE_TYPE));

        super.initFromXml(element, id);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);

        element.setAttribute(XML_ATTRIBUTE_REFERENCED_PART_ID, referencedPartId);
        element.setAttribute(XML_ATTRIBUTE_SOURCE_TYPE, sourceType.id);
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(XML_TAG_NAME);
    }

    private IProductCmptType getProductCmptType() {
        return (IProductCmptType)getParent();
    }

    /**
     * Defines the origin of an {@link IProductCmptProperty}, that is the product side or the policy
     * side.
     */
    public static enum SourceType {

        PRODUCT("product"), //$NON-NLS-1$

        POLICY("policy"); //$NON-NLS-1$

        private static SourceType getValueByProperty(IProductCmptProperty property) {
            return property.isPolicyCmptTypeProperty() ? POLICY : PRODUCT;
        }

        /**
         * Returns the {@link SourceType} corresponding to the provided id.
         */
        private static SourceType getValueByIdentifier(String id) {
            for (SourceType type : values()) {
                if (id.equals(type.id)) {
                    return type;
                }
            }
            return null;
        }

        private final String id;

        private SourceType(String id) {
            this.id = id;
        }

        /**
         * Returns the id of this {@link SourceType}.
         */
        public String getId() {
            return id;
        }

    }

}
