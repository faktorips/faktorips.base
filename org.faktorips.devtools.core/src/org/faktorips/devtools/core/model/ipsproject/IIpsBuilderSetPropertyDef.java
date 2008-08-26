/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.model.ipsproject;

import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.util.message.Message;

/**
 * The properties that can be set for a IpsArtefactBuilderSet can be defined in the plugin descriptor and are represented by implementations
 * of this interface.
 * 
 * @author Peter Erzberger
 */
public interface IIpsBuilderSetPropertyDef {

    /**
     * The identifing name of this property definition.
     */
    public String getName();

    /**
     * The name of this property definition in human readable form. 
     */
    public String getLabel();
    
    /**
     * Returns the type specified in the plugin descriptor.
     */
    public ValueDatatype getType();
    
    /**
     * Returns a description of this property definition if available. Otherwise <code>null</code>.
     */
    public String getDescription();

    /**
     * Returns the value that disables a functionalitiy based on the property descripted by this property definiton as string. To
     * get the type based value the parseValue(String) method with return value of this method as parameter has to be called.
     */
    public String getDisableValue(IIpsProject ipsProject);

    /**
     * The default value for a property descripted by this property definition as string. To get the type
     * based value the parseValue(String) method with return value of this method as parameter has
     * to be called.
     */
    public String getDefaultValue(IIpsProject ipsProject);

    /**
     * Returns if the property of this property definition has discrete values.
     */
    public boolean hasDiscreteValues();
    
    /**
     * The discrete values if this property definition has discrete values. 
     */
    public Object[] getDiscreteValues();
    
    /**
     * Converts the string representation of a value supported by this property definition into the actual object  
     */
    public Object parseValue(String value);
    
    /**
     * Returns if the property defintion is available according to the ips project settings.
     */
    public boolean isAvailable(IIpsProject ipsProject);

    /**
     * Validates if the provided string representation can be converted into an object described by this property definition.
     */
    public Message validateValue(String value);
    
    /**
     * Initializes this property defintion.
     * 
     * @param ipsModel can be used to get access to IPS resources
     * @param properties the values for the properties defined in the plugin descriptor are provided with this map as (String, String)
     *          (key, value) pairs 
     * @return a status object if errors occure during the initialization phase, <code>null</code> if none occurs
     */
    public IStatus initialize(IIpsModel ipsModel, Map properties);
}
