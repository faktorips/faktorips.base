/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.productcmpt;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsSrcFileContent;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.AttributeValueType;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IValueHolder;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.faktorips.util.message.Message;
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

    private final IIpsObjectPart parent;

    public AbstractValueHolder(IIpsObjectPart parent) {
        this.parent = parent;
    }

    @Override
    public IIpsObjectPart getParent() {
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
            IpsModel model = (IpsModel)IpsPlugin.getDefault().getIpsModel();
            ContentChangeEvent event = ContentChangeEvent.newPartChangedEvent(getParent());
            IpsSrcFileContent content = model.getIpsSrcFileContent(getParent().getIpsSrcFile());
            if (content != null) {
                content.ipsObjectChanged(event);
            }
        }
    }

    @Deprecated
    @Override
    public boolean isValid() throws CoreException {
        return isValid(getIpsProject());
    }

    @Override
    public boolean isValid(IIpsProject ipsProject) throws CoreException {
        return getValidationResultSeverity(ipsProject) != Message.ERROR;
    }

    @Override
    public IIpsProject getIpsProject() {
        return parent.getIpsProject();
    }

    @Deprecated
    @Override
    public int getValidationResultSeverity() throws CoreException {
        return getValidationResultSeverity(getIpsProject());
    }

    @Override
    public int getValidationResultSeverity(IIpsProject ipsProject) throws CoreException {
        return validate(ipsProject).getSeverity();
    }

    @Override
    public Element toXml(Document doc) {
        Element valueEl = doc.createElement(ValueToXmlHelper.XML_TAGNAME_VALUE);
        valueEl.setAttribute(ValueToXmlHelper.XML_ATTRIBUTE_IS_NULL, Boolean.toString(isNullValue()));
        valueEl.setAttribute(XML_ATTRIBUTE_VALUE_TYPE, getType().getXmlTypeName());
        contentToXml(valueEl, doc);
        return valueEl;
    }

    /**
     * Called by {@link #toXml(Document)} with the already created value element. The name of the
     * element is {@link ValueToXmlHelper#XML_TAGNAME_VALUE}. Also the attribtue
     * {@link #XML_ATTRIBUTE_VALUE_TYPE} is already set with the type returned by {@link #getType()}
     * .
     * 
     * @param valueEl The XML element with the name {@link ValueToXmlHelper#XML_TAGNAME_VALUE}
     *            holding the value.
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
    public String toString() {
        return "ValueHolder: " + getStringValue(); //$NON-NLS-1$
    }

}
