/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.model.extproperties;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.MessageList;
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

    private Class<?> extendedType;
    private String propertyId;
    protected Object defaultValue; // protected because setter has to be implemented in subclasses.
    private String name;
    private String position;
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
    public MessageList validate(IIpsObjectPartContainer ipsObjectPart, Object value) throws CoreException {
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

    @Override
    public int compareTo(IExtensionPropertyDefinition other) {
        if (order == other.getOrder()) {
            return propertyId.compareTo(other.getPropertyId());
        }
        return order - other.getOrder();
    }

    @Override
    public String toString() {
        return "ExtendedType:" + extendedType.getName() + ", PropertyId: " + propertyId; //$NON-NLS-1$ //$NON-NLS-2$
    }

}
