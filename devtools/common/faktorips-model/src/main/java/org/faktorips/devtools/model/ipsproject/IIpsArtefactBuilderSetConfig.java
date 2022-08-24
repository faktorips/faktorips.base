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

/**
 * A configuration object for IPS artifact builder sets. Provides string values for string keys. An
 * IPS artifact builder set instance can be configured by means of the IPS project properties.
 * Therefore the IpsArtefactBuilderSet tag of an .ipsproject file can contain one
 * IpsArtefactBuilderSetConfig tag. Here is an example for a configuration declaration:
 * 
 * @author Peter Erzberger
 */
public interface IIpsArtefactBuilderSetConfig {

    /**
     * Returns the names of all properties provided by this configuration.
     */
    String[] getPropertyNames();

    /**
     * Returns the value of the property of the provided property name.
     */
    Object getPropertyValue(String propertyName);

    /**
     * Tries to get a value for the provided property name and expects it to be a Boolean if not a
     * RuntimeException is thrown.
     */
    Boolean getPropertyValueAsBoolean(String propertyName);

    /**
     * Tries to get a value for the provided property name and expects it to be a String if not a
     * RuntimeException is thrown.
     */
    String getPropertyValueAsString(String propertyName);

    /**
     * Tries to get a value for the provided property name and expects it to be an Integer if not a
     * RuntimeException is thrown.
     */
    Integer getPropertyValueAsInteger(String propertyName);

}
