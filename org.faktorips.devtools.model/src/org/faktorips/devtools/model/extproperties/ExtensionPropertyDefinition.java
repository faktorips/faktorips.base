/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.extproperties;

import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.runtime.MessageList;
import org.faktorips.util.ArgumentCheck;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Element;

/**
 * Abstract implementation of <code>IExtensionPropertyDefinition</code>.
 * <p>
 * If you want to define an extension property that is being edited in the standard extension area,
 * without any validation rules, you can use the appropriate subclasses for the type (e.g.String,
 * Integer) in your extension point definition. In that case there is no need to create a subclass.
 * If you need to validate the property value or need to support a different type you should
 * subclass either this class or the appropriate subclass.
 * 
 * @author Jan Ortmann
 */
public abstract class ExtensionPropertyDefinition implements IExtensionPropertyDefinition {

    // protected because setter has to be implemented in subclasses. !BAD DESIGN!
    // CSOFF: VisibilityModifier
    protected Object defaultValue;
    // CSON: VisibilityModifier

    private Class<?> extendedType;

    private String propertyId;

    private String name;

    private String position;

    private RetentionPolicy retention;

    private int order = DEFAULT_ORDER;

    public ExtensionPropertyDefinition() {
        // Empty constructor needed because of Eclipse's extension point mechanism.
    }

    @Override
    public Class<?> getExtendedType() {
        return extendedType;
    }

    public void setExtendedType(Class<?> type) {
        ArgumentCheck.notNull(type);
        extendedType = type;
    }

    @Override
    public String getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(String id) {
        propertyId = id;
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

    @Override
    public Object getDefaultValue(IIpsObjectPartContainer ipsObjectPartContainer) {
        return getDefaultValue();
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * The method is called during object initialization to set the default value. Subclasses must
     * parse the String value and create an instance of the appropriate class.
     * 
     * @param s The default value as string as defined in the configuration.
     */
    public abstract void setDefaultValue(String s);

    /**
     * @param position The position to set.
     */
    public void setPosition(String position) {
        this.position = position;
    }

    @Override
    public String getPosition() {
        return position;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int getOrder() {
        return order;
    }

    /**
     * Sets the sort order.
     */
    public void setSortOrder(int sortOrder) {
        order = sortOrder;
    }

    @Override
    public void valueToXml(Element valueElement, Object value) {
        CDATASection valueSection = valueElement.getOwnerDocument().createCDATASection(value.toString());
        valueElement.appendChild(valueSection);
    }

    /**
     * Default implementation returns <code>null</code>.
     */
    @Override
    public MessageList validate(IIpsObjectPartContainer ipsObjectPart, Object value) {
        return null;
    }

    @Override
    public void afterSetValue(IIpsObjectPartContainer ipsObjectPart, Object value) {
        // Default implementation does nothing.
    }

    /**
     * Default implementation does nothing.
     */
    @Override
    public boolean beforeSetValue(IIpsObjectPartContainer ipsObjectPart, Object value) {
        return true;
    }

    /**
     * {@inheritDoc}
     * <p>
     * The default implementation simply always return <code>true</code>.
     */
    @Override
    public boolean isApplicableFor(IIpsObjectPartContainer ipsObjectPartContainer) {
        return true;
    }

    @Override
    public RetentionPolicy getRetention() {
        return retention;
    }

    public void setRetention(RetentionPolicy retention) {
        this.retention = retention;
    }

    @Override
    public boolean isRetainedAtRuntime() {
        return getRetention() == RetentionPolicy.RUNTIME;
    }

    @Override
    public int compareTo(IExtensionPropertyDefinition other) {
        if (order == other.getOrder()) {
            return propertyId.compareTo(other.getPropertyId());
        }
        return order - other.getOrder();
    }

    @Override
    public String toString() {
        return "ExtendedType:" + getExtendedType().getName() + ", PropertyId: " + getPropertyId(); //$NON-NLS-1$ //$NON-NLS-2$
    }

}
