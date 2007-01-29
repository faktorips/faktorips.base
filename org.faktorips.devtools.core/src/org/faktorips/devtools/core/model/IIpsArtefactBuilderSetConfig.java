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

package org.faktorips.devtools.core.model;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A configuration object for ips artefact builder sets. Provides string values for string keys.
 * An ips artefact builder set instance can be configured by means of the ips project properties. Therefor 
 * the IpsArtefactBuilderSet tag of an .ipsproject file can contain one IpsArtefactBuilderSetConfig tag.
 * Here is an example for a configuration declaration:
 * 
 * <IpsArtefactBuilderSet id="org.faktorips.devtools.stdbuilder.ipsstdbuilderset">
 *      <IpsArtefactBuilderSetConfig>
 *          <Property name="name" value="value"/>
 *          <Property name="name2" value="value2"/>
 *      </IpsArtefactBuilderSetConfig>
 * </IpsArtefactBuilderSet> 
 * 
 * @author Peter Erzberger
 */
public interface IIpsArtefactBuilderSetConfig{

    /**
     * The xml tag name for instances of this type.
     */
    public final static String XML_ELEMENT = "IpsArtefactBuilderSetConfig"; //$NON-NLS-1$
    
    /**
     * Returns the value of the property of the provided property name.
     */
    public String getPropertyValue(String propertyName);

    /**
     * Returns the boolean value of the property of the provided property name.
     * Returns the default value if the property with the specified property name is not found.
     * 
     * @see Boolean#valueOf(java.lang.String) this method is used for converting boolean string 
     *      representations into boolean values
     */
    public boolean getBooleanPropertyValue(String propertName, boolean defaultValue);

    /**
     * @param doc The document to create the element with.
     * @return The element containing all configuration data which can be parsed back to a new configuration 
     * using initFromXml().
     */
    public Element toXml(Document doc);

    /**
     * @param config The configuration to be parsed.
     * @return The element containing all configuration data which can be parsed back to a new configuration 
     * using initFromXml().
     */
    public void initFromXml(Element config);
}
