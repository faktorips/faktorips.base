package org.faktorips.devtools.core.model.extproperties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.internal.model.IpsObjectPartContainer;
import org.faktorips.devtools.core.model.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
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
    private boolean editedInStandardExtensionArea;
    private int sortOrder = DEFAULT_SORT_ORDER;
    
    /**
     * Empty constructor needed because of Eclipse's extension point mechanism.
     */
    public ExtensionPropertyDefinition() {
    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.model.IExtensionPropertyDefinition#getExtendedType()
     */
    public Class getExtendedType() {
        return extendedType;
    }
    
    public void setExtendedType(Class type) {
        ArgumentCheck.notNull(type);
        this.extendedType = type;
    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.model.IExtensionPropertyDefinition#getPropertyId()
     */
    public String getPropertyId() {
        return propertyId;
    }
    
    public void setPropertyId(String id) {
        this.propertyId = id;
    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.model.IExtensionPropertyDefinition#getDefaultValue()
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
     * 
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.model.IExtensionPropertyDefinition#isEditedInStandardExtensionArea()
     */
    public boolean isEditedInStandardExtensionArea() {
        return editedInStandardExtensionArea;
    }

    /**
     * @param editedInStandardExtensionArea The editedInStandardExtensionArea to set.
     */
    public void setEditedInStandardExtensionArea(boolean editedInStandardExtensionArea) {
        this.editedInStandardExtensionArea = editedInStandardExtensionArea;
    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.model.IExtensionPropertyDefinition#getDisplayName()
     */
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String name) {
        this.displayName = name;
    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.model.IExtensionPropertyDefinition#getSortOrder()
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
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.model.IExtensionPropertyDefinition#valueToXml(org.w3c.dom.Element, java.lang.Object)
     */
    public void valueToXml(Element valueElement, Object value) {
        CDATASection valueSection = valueElement.getOwnerDocument().createCDATASection(value.toString());
        valueElement.appendChild(valueSection);
    }
    
    /**
     * Default implementation returns <code>null</code>.
     * 
     * Overridden IMethod.
     */
    public MessageList validate(IpsObjectPartContainer ipsObjectPart, Object value) throws CoreException {
		return null;
	}

    /**
     * Default implementation doesn nothing.
     * 
     * Overridden IMethod.
     */
	public void afterSetValue(IpsObjectPartContainer ipsObjectPart, Object value) {
    }

    /**
     * Default implementation doesn nothing.
     * 
     * Overridden IMethod.
     */
    public boolean beforeSetValue(IpsObjectPartContainer ipsObjectPart, Object value) {
        return true;
    }

    /**
     * Adds a <code>Text</code> control to the extension area and returns a <code>TextField</code> based
     * on it.
     * 
     * Overridden IMethod.
     */
    public EditField newEditField(IpsObjectPartContainer ipsObjectPart, Composite extensionArea, UIToolkit toolkit) {
        Text text = toolkit.createText(extensionArea);
        return new TextField(text);
    }
    
    /**
     * Overridden IMethod.
     *
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o) {
        IExtensionPropertyDefinition other = (IExtensionPropertyDefinition)o;
        if (this.sortOrder == other.getSortOrder()) {
            return propertyId.compareTo(other.getPropertyId());
        }
        return sortOrder - other.getSortOrder();
    }

    public String toString() {
        return "ExtendedType:" + extendedType.getName() + ", PropertyId: " + propertyId;
    }
}
