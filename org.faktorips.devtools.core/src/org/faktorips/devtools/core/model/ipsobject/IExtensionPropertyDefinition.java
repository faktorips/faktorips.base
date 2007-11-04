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

package org.faktorips.devtools.core.model.ipsobject;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;

/**
 * Each ips object and each ips object part have a fixed set of standard properties, e.g. an
 * attribute has a name, a datatype a default value and so on. If you need other properties
 * you can define so called extension properties for each object or part. Such an an 
 * extension property is represented by this interface. 
 * <p>
 * Extension properties are defined by an extension of the extension point 
 * <code>org.faktorips.plugin.objectExtensionProperty</code>.
 * <p>
 * A list of the extension properties for a type in the model can be obtained from the <code>IpsModel</code>.
 * 
 * @see org.faktorips.devtools.core.model.IIpsModel#getExtensionPropertyDefinitions(Class, boolean)
 * 
 * @author Jan Ortmann
 */
public interface IExtensionPropertyDefinition extends Comparable {

    /**
     * Position to indicate that controls to edit the extension property should be placed above
     * the standard FaktorIPS controls.
     */
    public final static String POSITION_TOP = "top"; //$NON-NLS-1$
    
    /**
     * Position to indicate that controls to edit the extension property should be placed below
     * the standard FaktorIPS controls.
     */
    public final static String POSITION_BOTTOM = "bottom"; //$NON-NLS-1$
    
    /**
     * Default sort order.
     */
    public final static int DEFAULT_SORT_ORDER = 10000;
    
    /**
     * Returns the type this object defines a property for, e.g. <code>org.faktorips.plugin.model.pctype.IAttribute</code>.
     */
    public Class getExtendedType();

    /**
     * Returns the unique property id. The id is the unqiue id of the extension that defines this property.
     * E.g. if an extension property is defined in a plugin with id 'foo.bar' and the extension id is 'attribute.prop0'
     * the resulting property is 'foo.bar.attribute.prop0';
     * It is is recommended that the unqalified name of the extended type is include in the property id. E.g.
     * if the ips model type IAttribute is extended with a 'prop0' property, the reccommended extension id
     * is 'attribute.prop0'.
     */
    public String getPropertyId();
    
    /**
     * Returns the property's sort order of this extension property. The extenstion properties shown in 
     * the standard extension area are ordered according to this property.
     * <p>
     * It is recommended that all extension properties defined by a plugin use a small range e.g. 1000-1999
     * so that extension properties of the same plugin are grouped together. Don't use sequentiel numbers
     * for your initial properties, so that you can insert others later.
     */
    public int getSortOrder();
    
    /**
     * Returns the default value for the property.
     */
    public Object getDefaultValue();
    
    /**
     * Stores the value in the xml element. Simple values should be appended as CDATA sections to
     * the given element (textual representation of value "blabla" is &ltvalue&gtblabla&lt/value&gt).
     * 
     * @param valueElement The value element (textual representation <value></value>).
     * @param value The value to add to the xml. The passed value is <strong>never</strong><code>null</code>.
     * Null handling is done before this method is called.
     */
    public void valueToXml(Element valueElement, Object value);
    
    /**
     * Returns the property's value from the given xml element. The method is only called if a 
     * value other than <code>null</code> is stored in the element. Null handling is done before 
     * this method is called!
     * 
     * @param valueElement The value element (textual representation <value></value>).
     */
    public Object getValueFromXml(Element valueElement);
    
    /**
     * This method is called before a value is set to an object's extension property.
     * 
     * @param ipsObjectPart The ips object part which property is about to be set.
     * @param Object value The value that will be set to the extension property via the setExtProperty
     * method.
     * 
     * @return <code>true</code> if the value can be set, <code>false</code> if the value can't be set.
     * Note that model objects should allow inconsistent state and report inconsistencies via the 
     * validate() method. So use wisely.
     * 
     * @see IpsObjectPartContainer.setExtProperty(Object)
     */
    public boolean beforeSetValue(IIpsObjectPartContainer ipsObjectPart, Object value);

    /**
     * This method is called after a value was set to a object's extension property.
     * 
     * @param ipsObjectPart The ips object part which property has been set.
     * @param Object value The value that will be set to the property via the setExtProperty
     * method.
     * 
     * @see IpsObjectPartContainer.setExtProperty(Object)
     */
    public void afterSetValue(IIpsObjectPartContainer ipsObjectPart, Object value);

    /**
     * Validates the property value for the given ips object part.
     * 
     * @param ipsObjectPart  The ips object part which property value it is.
     * @param value The property value in string format.
     * 
     * @return A list of messages decribing invalid property state or warnings about the state.
     * <code>null</code> if the value is valid.
     * 
     * @throws CoreException if an error occurs while validating the property.
     */
    public MessageList validate(IIpsObjectPartContainer ipsObjectPart, Object value) throws CoreException;

    /**
     * Returns <code>true</code> if the property can be edited in the standard area for extension properties,
     * otherwise <code>false</code>. If <code>true</code> <code></code>getDisplayName()</code> must return
     * the name used as label text and <code>newEditField</code> must return an EditField that allowes the 
     * value to be edited.
     */
    public boolean isEditedInStandardExtensionArea();

    /**
     * Returns the position where the extension property should be displayed and edited.
     * 
     * @return one the position constants defined in this interface.
     * 
     * @see #POSITION_BOTTOM
     * @see #POSITION_TOP
     */
	public String getEditedInStandardTextArea();
    
    /**
     * Returns the property's name used to present it to the user.
     */
    public String getDisplayName();

    /**
     * Returns a new EditField that allows to edit the property value. Can return <code>null</code> if 
     * <code>isEditedInStandardExtensionArea()</code> returns <code>false</code>.
     * 
     * @param ipsObjectPart The ips object part which extension property an edit field is created for.
     * @param extensionArea The standard extension area composite. A new control that allows editing the property
     * value has to be added to this composite in subclasses. The EditField has to be constructed based on
     * the control. 
     * @param toolkit The ui toolkit to be used to create the control to ensure a consistent user interface.
     */
    public EditField newEditField(IIpsObjectPartContainer ipsObjectPart, Composite extensionArea, UIToolkit toolkit);
    
}
