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

import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;

/**
 * A class that hold information about IIpsArtefactBuilderSets that are registered with the
 * corresponding extension point.
 * 
 * @see IIpsModel#getIpsArtefactBuilderSetInfos()
 * 
 * @author Peter Erzberger
 */
public interface IIpsArtefactBuilderSetInfo {

    /**
     * A message code that indicates that a property is not supported by this builder set. A
     * validation message with this message code is created when a IpsArtefactBuilderConfig is
     * validated against the definition of this builder set and its allowed properties.
     */
    String MSG_CODE_PROPERTY_NOT_SUPPORTED = "propertyNotSupported"; //$NON-NLS-1$

    /**
     * A message code that indicates that a property is in accordance with the JDK that is selected
     * for the java project of this builder set. A validation message with this message code is
     * created when a
     * <code>org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetConfig</code> is
     * validated against the definition of this builder set and its allowed properties.
     */
    String MSG_CODE_PROPERTY_NO_JDK_COMPLIANCE = "propertyNoJdkCompliance"; //$NON-NLS-1$

    /**
     * Creates a {@link IIpsArtefactBuilderSet} instance based on this
     * {@link IIpsArtefactBuilderSetInfo} and returns it.
     */
    IIpsArtefactBuilderSet create(IIpsProject ipsProject);

    /**
     * Creates a default configuration for a builder set defined by this info.
     */
    IIpsArtefactBuilderSetConfigModel createDefaultConfiguration(IIpsProject ipsProject);

    /**
     * Returns the id by which the <code>IIpsArtefactBuilderSet</code> is registered with the
     * system.
     */
    String getBuilderSetId();

    /**
     * Returns the label for the corresponding <code>IIpsArtefactBuilderSet</code>.
     */
    String getBuilderSetLabel();

    /**
     * Returns the property definition object for the specified name or <code>null</code> if it
     * doesn't exist.
     */
    IIpsBuilderSetPropertyDef getPropertyDefinition(String name);

    /**
     * Returns the properties defined for this IpsArtefactBuilderSet.
     */
    IIpsBuilderSetPropertyDef[] getPropertyDefinitions();

    /**
     * Validates the provided IIpsArtefactBuilderSetConfig against this definition. Especially the
     * properties of the configuration are checked for their existence and the correct value.
     */
    MessageList validateIpsArtefactBuilderSetConfig(IIpsProject ipsProject,
            IIpsArtefactBuilderSetConfigModel builderSetConfig);

    /**
     * Validates the property value of the property of an IpsArtefactBuilderSetConfig specified by
     * the propertyName. It returns <code>null</code> if validation is correct otherwise a
     * {@link Message} object is returned.
     */
    Message validateIpsBuilderSetPropertyValue(IIpsProject ipsProject,
            String propertyName,
            String propertyValue);

}
