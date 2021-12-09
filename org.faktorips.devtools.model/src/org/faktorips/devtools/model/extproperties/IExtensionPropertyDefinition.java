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

import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.runtime.MessageList;
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
 * @see org.faktorips.devtools.model.IIpsModel#getExtensionPropertyDefinitions(IIpsObjectPartContainer)
 *      )
 * 
 * @author Jan Ortmann
 */
public interface IExtensionPropertyDefinition extends Comparable<IExtensionPropertyDefinition> {

    /**
     * Position to indicate that controls to edit the extension property should be placed above the
     * standard Faktor-IPS controls.
     */
    public static final String POSITION_TOP = "top"; //$NON-NLS-1$

    /**
     * Position to indicate that controls to edit the extension property should be placed below the
     * standard Faktor-IPS controls.
     */
    public static final String POSITION_BOTTOM = "bottom"; //$NON-NLS-1$

    /**
     * Default sort order.
     */
    public static final int DEFAULT_ORDER = 10000;

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
     * Returns the default value configured by this definition.
     * <p>
     * Note that since 3.10 the default value may vary from container to
     * {@link IIpsObjectPartContainer container}. To retrieve the default value for a specific
     * {@link IIpsObjectPartContainer} instance call
     * {@link #getDefaultValue(IIpsObjectPartContainer)}.
     */
    public Object getDefaultValue();

    /**
     * Returns the initial value (default value) of the extension property when it is created for
     * the given {@link IIpsObjectPartContainer}.
     * <p>
     * The extension property's default value is dependent on a concrete
     * {@link IIpsObjectPartContainer} instance. Thus the default value may vary from container to
     * container, even for the same extension property definition.
     * 
     * @param ipsObjectPartContainer The part for which the extension property is created
     * @return The default value that is set when the new extension property is created
     */
    public Object getDefaultValue(IIpsObjectPartContainer ipsObjectPartContainer);

    /**
     * Stores the value in the XML element. Simple values should be appended as CDATA sections to
     * the given element (textual representation of value "blabla" is {@code <value>blabla</value>}.
     * 
     * @param valueElement The value element (textual representation &lt;value&gt;&lt;/value&gt;).
     * @param value The value to add to the XML. The passed value is <strong>never</strong>
     *            <code>null</code>. Null handling is done before this method is called.
     */
    public void valueToXml(Element valueElement, Object value);

    /**
     * Returns the property's value from the given XML element. The method is only called if a value
     * other than <code>null</code> is stored in the element. Null handling is done before this
     * method is called!
     * 
     * @param valueElement The value element (textual representation &lt;value&gt;&lt;/value&gt;).
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
     * @throws CoreRuntimeException if an error occurs while validating the property.
     */
    public MessageList validate(IIpsObjectPartContainer ipsObjectPart, Object value) throws CoreRuntimeException;

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

    /**
     * This method is called by the extension property framework to decide whether this extension
     * property is applicable for the given {@link IIpsObjectPartContainer part} or not.
     * 
     * @param ipsObjectPartContainer The {@link IIpsObjectPartContainer part} for which the
     *            extension property should be active or inactive
     * @return <code>true</code> if this extension property is active for the given part
     */
    public boolean isApplicableFor(IIpsObjectPartContainer ipsObjectPartContainer);

    /**
     * Indicates how long extension properties are to be retained and thus where they can be
     * accessed.
     * <p>
     * DEFINITION-level extension properties are retained and accessible during design-time in model
     * or product definitions (but not at runtime).
     * <p>
     * RUNTIME-level extension properties are retained and accessible at runtime (via runtime
     * libraries). This includes the DEFINITION-level.
     * <p>
     * The retention policy is only a recommendation for the code generator. The code generator may
     * override the retention policy (e.g. for performance reasons) and extend the scope (use
     * RUNTIME where DEFINITION is defined).
     * <p>
     * The default is RUNTIME.
     * 
     * @return The defined {@link RetentionPolicy}
     */
    public RetentionPolicy getRetention();

    public boolean isRetainedAtRuntime();

    public enum RetentionPolicy {
        DEFINITION,
        RUNTIME;
    }
}
