/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt;

import org.faktorips.devtools.model.ContentChangeEvent;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.internal.ipsobject.IpsSrcFileContent;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.AttributeValueType;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.IValueHolder;
import org.faktorips.devtools.model.util.XmlUtil;
import org.faktorips.runtime.Severity;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The abstract implementation of value holders used in attribute values holding the value. We use
 * the holder abstraction to exclude the XML support and validation code to an extra class as well
 * as supporting different type of objects stored in an attribute value.
 * 
 * @author dirmeier
 */
public abstract class AbstractValueHolder<T> implements IValueHolder<T> {

    /**
     * Name of the XML attribute for the value holder type.
     */
    public static final String XML_ATTRIBUTE_VALUE_TYPE = "valueType"; //$NON-NLS-1$

    private final IAttributeValue parent;

    public AbstractValueHolder(IAttributeValue parent) {
        this.parent = parent;
    }

    @Override
    public IAttributeValue getParent() {
        return parent;
    }

    /**
     * Performs an change event on the parent object.
     */
    protected void objectHasChanged(Object oldValue, Object newValue) {
        if (oldValue == newValue) {
            return;
        }
        // (oldValue == null) ==> (newValue != null) because of last check
        if (oldValue == null || !oldValue.equals(newValue)) {
            ContentChangeEvent event = ContentChangeEvent.newPartChangedEvent(getParent());
            IpsSrcFileContent content = IIpsModel.get().getIpsSrcFileContent(getParent().getIpsSrcFile());
            if (content != null) {
                content.ipsObjectChanged(event);
            }
        }
    }

    @Override
    public boolean isValid(IIpsProject ipsProject) throws CoreRuntimeException {
        return getValidationResultSeverity(ipsProject) != Severity.ERROR;
    }

    @Override
    public IIpsProject getIpsProject() {
        return parent.getIpsProject();
    }

    @Override
    public Severity getValidationResultSeverity(IIpsProject ipsProject) throws CoreRuntimeException {
        return validate(ipsProject).getSeverity();
    }

    @Override
    public Element toXml(Document doc) {
        Element valueEl = doc.createElement(ValueToXmlHelper.XML_TAG_VALUE);
        valueEl.setAttribute(ValueToXmlHelper.XML_ATTRIBUTE_IS_NULL, Boolean.toString(isNullValue()));
        valueEl.setAttribute(XML_ATTRIBUTE_VALUE_TYPE, getType().getXmlTypeName());
        contentToXml(valueEl, doc);
        return valueEl;
    }

    /**
     * Called by {@link #toXml(Document)} with the already created value element. The name of the
     * element is {@link ValueToXmlHelper#XML_TAG_VALUE}. Also the attribute
     * {@link #XML_ATTRIBUTE_VALUE_TYPE} is already set with the type returned by {@link #getType()}
     * .
     * 
     * @param valueEl The XML element with the name {@link ValueToXmlHelper#XML_TAG_VALUE} holding
     *            the value.
     * @param doc The owner document to create additional XML elements
     */
    protected abstract void contentToXml(Element valueEl, Document doc);

    /**
     * Returns the type of this value holder. The implementation should return an constant type of
     * {@link AttributeValueType}.
     */
    protected abstract AttributeValueType getType();

    /**
     * A static helper method that creates a new value holder by reading the type specified in the
     * XML element and initializes the newly created value holder by calling
     * {@link #initFromXml(Element)}. The given {@link IAttributeValue} is only used as parent of
     * the new value holder but the new holder is not set in the attribute value!
     * 
     * @param attributeValue The attribute value used as parent. The new holder is not set
     *            automatically.
     * @param valueEl The XML element used to initializes the new holder
     * 
     * @return Returns the newly created value holder.
     */
    public static IValueHolder<?> initValueHolder(IAttributeValue attributeValue, Element valueEl) {
        AttributeValueType attributeValueType = AttributeValueType.getType(valueEl);
        IValueHolder<?> newValueInstance = attributeValueType.newHolderInstance(attributeValue);
        newValueInstance.initFromXml(valueEl);
        return newValueInstance;
    }

    @Override
    public IValueHolder<?> copy(IAttributeValue parent) {
        Element element = toXml(XmlUtil.getDefaultDocumentBuilder().newDocument());
        return AbstractValueHolder.initValueHolder(parent, element);
    }

    @Override
    public String toString() {
        return "ValueHolder: " + getStringValue(); //$NON-NLS-1$
    }

    @Override
    public boolean equalsValueHolder(IValueHolder<?> o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }

        if (o instanceof DelegatingValueHolder) {
            return equals(((DelegatingValueHolder<?>)o).getDelegate());
        } else {
            return equals(o);
        }
    }

    /**
     * Creates a new validator to validate this value holder.
     * 
     * @param parentForValidation the parent that the validator assumes that this value holder is a
     *            part of when validating the value holder
     * @param ipsProject the IPS project to use
     */
    protected abstract IValueHolderValidator newValidator(IAttributeValue parentForValidation, IIpsProject ipsProject);

}
