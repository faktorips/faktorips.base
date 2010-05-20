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

package org.faktorips.devtools.core.model.ipsobject;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;

/**
 * Each IPS object and each IPS object part have a fixed set of standard properties, e.g. an
 * attribute has a name, a data type a default value and so on. If you need other properties you can
 * define so called extension properties for each object or part. Such an an extension property is
 * represented by this interface.
 * <p>
 * Extension properties are defined by an extension of the extension point
 * <code>org.faktorips.plugin.objectExtensionProperty</code>.
 * <p>
 * A list of the extension properties for a type in the model can be obtained from the
 * <code>IpsModel</code>.
 * 
 * @see org.faktorips.devtools.core.model.IIpsModel#getExtensionPropertyDefinitions(Class, boolean)
 * 
 * @author Jan Ortmann
 */
public interface IExtensionPropertyDefinition extends Comparable<IExtensionPropertyDefinition> {

    /**
     * Position to indicate that controls to edit the extension property should be placed above the
     * standard Faktor-IPS controls.
     */
    public final static String POSITION_TOP = "top"; //$NON-NLS-1$

    /**
     * Position to indicate that controls to edit the extension property should be placed below the
     * standard Faktor-IPS controls.
     */
    public final static String POSITION_BOTTOM = "bottom"; //$NON-NLS-1$

    /**
     * Default sort order.
     */
    public final static int DEFAULT_ORDER = 10000;

    /**
     * Returns the type this object defines a property for, e.g.
     * <code>org.faktorips.plugin.model.pctype.IAttribute</code>.
     */
    public Class<?> getExtendedType();

    /**
     * Returns the unique property id. The id is the unique id of the extension that defines this
     * property. E.g. if an extension property is defined in a plug-in with id 'foo.bar' and the
     * extension id is 'attribute.prop0' the resulting property is 'foo.bar.attribute.prop0'; It is
     * is recommended that the unqualified name of the extended type is include in the property id.
     * E.g. if the IPS model type IAttribute is extended with a 'prop0' property, the recommended
     * extension id is 'attribute.prop0'.
     */
    public String getPropertyId();

    /**
     * Returns the order of the extension property. By default the order is used for display
     * purposes. The extension properties are displayed in an ascending order in the display area of
     * the according editor, view or dialog.
     * <p>
     * It is recommended that all extension properties defined by a plug-in use a small range e.g.
     * 1000-1999 so that extension properties of the same plug-in are grouped together. Don't use
     * sequential numbers for your initial properties, so that you can insert others later.
     */
    public int getOrder();

    /**
     * Returns the default value for the property.
     */
    public Object getDefaultValue();

    /**
     * Stores the value in the XML element. Simple values should be appended as CDATA sections to
     * the given element (textual representation of value "blabla" is
     * &ltvalue&gtblabla&lt/value&gt).
     * 
     * @param valueElement The value element (textual representation <value></value>).
     * @param value The value to add to the XML. The passed value is <strong>never</strong>
     *            <code>null</code>. Null handling is done before this method is called.
     */
    public void valueToXml(Element valueElement, Object value);

    /**
     * Returns the property's value from the given XML element. The method is only called if a value
     * other than <code>null</code> is stored in the element. Null handling is done before this
     * method is called!
     * 
     * @param valueElement The value element (textual representation <value></value>).
     */
    public Object getValueFromXml(Element valueElement);

    /**
     * Returns the property's value from the given string. The method is only called if a value
     * other than <code>null</code> is stored in the element. Null handling is done before this
     * method is called!
     * 
     * @param value The value element (textual representation &lt;value&gt;&lt;/value&gt;).
     */
    public Object getValueFromString(String value);

    /**
     * This method is called before a value is set to an object's extension property.
     * 
     * @param ipsObjectPart The IPS object part which property is about to be set.
     * @param value The value that will be set to the extension property via the setExtProperty
     *            method.
     * 
     * @return <code>true</code> if the value can be set, <code>false</code> if the value can't be
     *         set. Note that model objects should allow inconsistent state and report
     *         inconsistencies via the validate() method. So use wisely.
     */
    public boolean beforeSetValue(IIpsObjectPartContainer ipsObjectPart, Object value);

    /**
     * This method is called after a value was set to a object's extension property.
     * 
     * @param ipsObjectPart The IPS object part which property has been set.
     * @param value The value that will be set to the property via the setExtProperty method.
     */
    public void afterSetValue(IIpsObjectPartContainer ipsObjectPart, Object value);

    /**
     * Validates the property value for the given IPS object part.
     * 
     * @param ipsObjectPart The IPS object part which property value it is.
     * @param value The property value in string format.
     * 
     * @return A list of messages describing invalid property state or warnings about the state.
     *         <code>null</code> if the value is valid.
     * 
     * @throws CoreException if an error occurs while validating the property.
     */
    public MessageList validate(IIpsObjectPartContainer ipsObjectPart, Object value) throws CoreException;

    /**
     * Returns the position of the label and editing field of the extension property in the display
     * area of the editor, view or dialog. Only the values of the positioning constants of this
     * interface a valid to return by this method.
     * 
     * @see #POSITION_BOTTOM
     * @see #POSITION_TOP
     */
    public String getPosition();

    /**
     * Returns the property's name. This name is also the label of the editing field of this
     * property.
     */
    public String getName();

}
