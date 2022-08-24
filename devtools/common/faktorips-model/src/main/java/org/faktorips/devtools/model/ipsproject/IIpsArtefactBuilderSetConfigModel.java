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

import org.faktorips.runtime.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class represents the configuration for an IPS artifact builder set. It uses name, value
 * pairs as strings to describe its properties. A {@link IIpsArtefactBuilderSetConfig} instance
 * which is provided to a {@link IIpsArtefactBuilderSet} object in the initialization phase can be
 * created based on this object by means of the create(IIpsProject, IpsArtefactBuilderSetInfo)
 * method. Instances of this class are created based on the builder set configurations defined in
 * the IPS project properties.Therefor the IpsArtefactBuilderSet tag of an .ipsproject file can
 * contain one IpsArtefactBuilderSetConfig tag. Here is an example for a configuration declaration:
 * 
 * <pre>
 * {@code
 * <IpsArtefactBuilderSet id="org.faktorips.devtools.stdbuilder.ipsstdbuilderset">
 *      <IpsArtefactBuilderSetConfig>
 *          <Property name="name" value="value"/>
 *          <Property name="name2" value="value2"/>
 *      </IpsArtefactBuilderSetConfig>
 * </IpsArtefactBuilderSet>
 * }
 * </pre>
 * 
 * @author Peter Erzberger
 */
public interface IIpsArtefactBuilderSetConfigModel {

    /**
     * The XML tag name for instances of this type.
     */
    String XML_ELEMENT = "IpsArtefactBuilderSetConfig"; //$NON-NLS-1$

    /**
     * Returns the names of all properties provided by this configuration.
     */
    String[] getPropertyNames();

    /**
     * Returns the description of the property if specified.
     * 
     * @param propertyName the name for which the description is requested. Cannot be
     *            <code>null</code>
     * @return the description of the specified property or <code>null</code> if no description is
     *             available
     */
    String getPropertyDescription(String propertyName);

    /**
     * Returns the value of the property of the provided property name.
     */
    String getPropertyValue(String propertyName);

    /**
     * Sets the value of the property specified by the property name
     * 
     * @param propertyName the name of the property. Cannot be <code>null</code>
     * @param value the value of the property. Cannot be <code>null</code>
     * @param description optional description of the property. Can be null
     */
    void setPropertyValue(String propertyName, String value, String description);

    /**
     * @param doc The document to create the element with.
     * @return The element containing all configuration data which can be parsed back to a new
     *             configuration using initFromXml().
     */
    Element toXml(Document doc);

    /**
     * @param config The configuration to be parsed.
     */
    void initFromXml(Element config);

    /**
     * Create a IIpsArtefactBuilderSetConfig that is provided to the IIpsArtefactBuilderSets init
     * method out of this configuration model object.
     * 
     * @throws IllegalStateException if there are inconsistencies between the provided builder set
     *             info and this object
     */
    IIpsArtefactBuilderSetConfig create(IIpsProject ipsProject, IIpsArtefactBuilderSetInfo builderSetInfo);

    /**
     * Validates this configuration against the provided {@link IIpsArtefactBuilderSetInfo} object
     * and returns a validation message object if the validation fails otherwise <code>null</code>.
     */
    MessageList validate(IIpsProject ipsProject, IIpsArtefactBuilderSetInfo builderSetInfo);

}
