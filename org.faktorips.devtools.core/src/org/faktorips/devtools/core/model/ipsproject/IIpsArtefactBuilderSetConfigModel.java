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

import org.faktorips.devtools.core.internal.model.ipsproject.IpsArtefactBuilderSetInfo;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class represents the configuration for an ips artefact builder set. It uses name, value
 * pairs as strings to describe its properties. A {@link IIpsArtefactBuilderSetConfig} instance
 * which is provided to a {@link IIpsArtefactBuilderSet} object in the initialization phase can be
 * created based on this object by means of the create(IIpsProject, IpsArtefactBuilderSetInfo)
 * method. Instances of this class are created based on the builder set configurations defined in
 * the ips project properties.Therefor the IpsArtefactBuilderSet tag of an .ipsproject file can
 * contain one IpsArtefactBuilderSetConfig tag. Here is an example for a configuration declaration:
 * 
 * <IpsArtefactBuilderSet id="org.faktorips.devtools.stdbuilder.ipsstdbuilderset">
 * <IpsArtefactBuilderSetConfig> <Property name="name" value="value"/> <Property name="name2"
 * value="value2"/> </IpsArtefactBuilderSetConfig> </IpsArtefactBuilderSet>
 * 
 * @author Peter Erzberger
 */
public interface IIpsArtefactBuilderSetConfigModel{

    /**
     * The xml tag name for instances of this type.
     */
    public final static String XML_ELEMENT = "IpsArtefactBuilderSetConfig"; //$NON-NLS-1$

    /**
     * Returns the names of all properties provided by this configuration.
     */
    public String[] getPropertyNames();

    /**
     * Returns the value of the property of the provided property name.
     */
    public String getPropertyValue(String propertyName);

    /**
     * Sets the value of the property specified by the property name
     */
    public void setPropertyValue(String propertyName, String value);
    
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
    
    /**
     * Create a IIpsArtefactBuilderSetConfig that is provided to the IIpsArtefactBuilderSets init method out of this
     * configuration model object.
     * 
     * @throws IllegalStateException if there are inconsistencies between the provided builder set info and this object
     */
    public IIpsArtefactBuilderSetConfig create(IIpsProject ipsProject, IIpsArtefactBuilderSetInfo builderSetInfo);

    /**
     * Validates this configuration against the provided {@link IpsArtefactBuilderSetInfo} object and returns a validation message object
     * if the validation fails otherwise <code>null</code>. 
     */
    public MessageList validate(IIpsProject ipsProject, IpsArtefactBuilderSetInfo builderSetInfo);

}
