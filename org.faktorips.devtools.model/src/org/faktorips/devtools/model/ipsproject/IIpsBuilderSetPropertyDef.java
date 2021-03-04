/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.ipsproject;

import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.util.message.Message;

/**
 * The properties that can be set for a IpsArtefactBuilderSet can be defined in the plug-in
 * descriptor and are represented by implementations of this interface.
 * 
 * @author Peter Erzberger
 */
public interface IIpsBuilderSetPropertyDef {

    /**
     * Prefix for all message codes of this class.
     */
    public static final String MSGCODE_PREFIX = "BUILDER_SET_PROPERTY_DEF-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the string value is not parsable by the
     * parseValue(String) method.
     */
    public static final String MSGCODE_NON_PARSABLE_VALUE = MSGCODE_PREFIX + "NonParsableValue"; //$NON-NLS-1$

    /**
     * The identifying name of this property definition.
     */
    public String getName();

    /**
     * The name of this property definition in human readable form.
     */
    public String getLabel();

    /**
     * Returns the type specified in the plug-in descriptor. The possible types are defined in the
     * schema. Currently supported are <em>boolean, enumeration, string, integer, extensionPoint</em>.
     */
    public String getType();

    /**
     * Returns a description of this property definition if available. Otherwise <code>null</code>.
     */
    public String getDescription();

    /**
     * Returns the value that disables a functionality based on the property described by this
     * property definition as string. To get the type based value the parseValue(String) method with
     * return value of this method as parameter has to be called.
     */
    public String getDisableValue(IIpsProject ipsProject);

    /**
     * The default value for a property described by this property definition as string. To get the
     * type based value the parseValue(String) method with return value of this method as parameter
     * has to be called.
     */
    public String getDefaultValue(IIpsProject ipsProject);

    /**
     * Returns if the property of this property definition has discrete values.
     */
    public boolean hasDiscreteValues();

    /**
     * The discrete values if this property definition has discrete values.
     */
    public String[] getDiscreteValues();

    /**
     * Converts the string representation of a value supported by this property definition into the
     * actual object. If <code>null</code> is provided as parameter value <code>null</code> will be
     * returned.
     */
    public Object parseValue(String value);

    /**
     * Returns if the property definition is available according to the IPS project settings.
     */
    public boolean isAvailable(IIpsProject ipsProject);

    /**
     * Validates if the provided string representation can be converted into an object described by
     * this property definition. If the validation fails a message object will be returned otherwise
     * <code>null</code> will be returned. If <code>null</code> is provided as parameter
     * <code>null</code> will be returned.
     */
    public Message validateValue(IIpsProject ipsProject, String value);

    /**
     * Initializes this property definition.
     * 
     * @param ipsModel can be used to get access to IPS resources
     * @param properties the values for the properties defined in the plug-in descriptor are
     *            provided with this map as (String, String) or (String,List) (key, value) pairs
     * @return a status object if errors occur during the initialization phase, <code>null</code> if
     *         none occurs
     */
    public IStatus initialize(IIpsModel ipsModel, Map<String, Object> properties);

}
