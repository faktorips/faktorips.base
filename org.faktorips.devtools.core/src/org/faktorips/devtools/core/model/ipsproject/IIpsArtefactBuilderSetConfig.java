/*******************************************************************************
 * Copyright © 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 ******************************************************************************/
package org.faktorips.devtools.core.model.ipsproject;

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
    public String[] getPropertyNames();

    /**
     * Returns the value of the property of the provided property name.
     */
    public Object getPropertyValue(String propertyName);

    /**
     * Tries to get a value for the provided property name and expects it to be a Boolean if not a
     * RuntimeException is thrown.
     */
    public Boolean getPropertyValueAsBoolean(String propertyName);

    /**
     * Tries to get a value for the provided property name and expects it to be a String if not a
     * RuntimeException is thrown.
     */
    public String getPropertyValueAsString(String propertyName);

    /**
     * Tries to get a value for the provided property name and expects it to be an Integer if not a
     * RuntimeException is thrown.
     */
    public Integer getPropertyValueAsInteger(String propertyName);

}
