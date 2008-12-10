/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
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
 * without any validation rules, you can use the appropriate subclasses for the type (e.g.String, Integer)
 * in your extension point definition. In that case there is no need to create a subclass. If you need
 * to validate the property value or need to support a different type you should subclass either this
 * class or the appropriate subclass. 
 * 
 * @author Jan Ortmann
 */
public abstract class ExtensionPropertyDefinition implements IExtensionPropertyDefinition {
    
    private Class extendedType;
    private String propertyId;
    protected Object defaultValue; // protected because setter has to be implemented in subclasses.
    private String displayName;
    private String editedInStandardExtensionArea;
    private int sortOrder = DEFAULT_SORT_ORDER;
    
    /**
     * Empty constructor needed because of Eclipse's extension point mechanism.
     */
    public ExtensionPropertyDefinition() {
    }
    
    /**
     * {@inheritDoc}
     */
    public Class getExtendedType() {
        return extendedType;
    }
    
    public void setExtendedType(Class type) {
        ArgumentCheck.notNull(type);
        this.extendedType = type;
    }
    
    /**
     * {@inheritDoc}
     */
    public String getPropertyId() {
        return propertyId;
    }
    
    public void setPropertyId(String id) {
        this.propertyId = id;
    }
    
    /**
     * {@inheritDoc}
     */
    public Object getDefaultValue() {
        return defaultValue;
    }
    
    /**
     * The method is called during object initialization to set the default value.
     * Subclasses must parse the String value and create an instance of the appropriate 
     * class.  
     * 
     * @param s The default value as string as defined in the configuration.
     */
    public abstract void setDefaultValue(String s);
    
    /**
     * Returns <code>true</code>.
     */
    public boolean isEditedInStandardExtensionArea() {
        return !editedInStandardExtensionArea.equals("false"); //$NON-NLS-1$
    }

    /**
     * @param editedInStandardExtensionArea The editedInStandardExtensionArea to set.
     */
    public void setEditedInStandardExtensionArea(String editedInStandardExtensionArea) {
        this.editedInStandardExtensionArea = editedInStandardExtensionArea;
    }

	public String getEditedInStandardTextArea() {
		return editedInStandardExtensionArea;
	}
    
    /**
     * {@inheritDoc}
     */
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String name) {
        this.displayName = name;
    }
    
    /**
     * {@inheritDoc}
     */
    public int getSortOrder() {
        return sortOrder;
    }

    /**
     * Sets the sort order.
     */
    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
    
    /**
     * {@inheritDoc}
     */
    public void valueToXml(Element valueElement, Object value) {
        CDATASection valueSection = valueElement.getOwnerDocument().createCDATASection(value.toString());
        valueElement.appendChild(valueSection);
    }
    
    /**
     * Default implementation returns <code>null</code>.
     */
    public MessageList validate(IIpsObjectPartContainer ipsObjectPart, Object value) throws CoreException {
		return null;
	}

    /**
     * Default implementation doesn nothing.
     */
	public void afterSetValue(IIpsObjectPartContainer ipsObjectPart, Object value) {
    }

    /**
     * Default implementation does nothing.
     */
    public boolean beforeSetValue(IIpsObjectPartContainer ipsObjectPart, Object value) {
        return true;
    }
    
    /**
     * {@inheritDoc}
     */
    public int compareTo(Object o) {
        IExtensionPropertyDefinition other = (IExtensionPropertyDefinition)o;
        if (this.sortOrder == other.getSortOrder()) {
            return propertyId.compareTo(other.getPropertyId());
        }
        return sortOrder - other.getSortOrder();
    }

    public String toString() {
        return "ExtendedType:" + extendedType.getName() + ", PropertyId: " + propertyId; //$NON-NLS-1$ //$NON-NLS-2$
    }

}
