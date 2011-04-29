/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.model.ipsproject;

import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * A class that hold information about IIpsArtefactBuilderSets that are registered with the
 * corresponding extension point.
 * 
 * @see IpsModel#getIpsArtefactBuilderSetInfos()
 * 
 * @author Peter Erzberger
 */
public interface IIpsArtefactBuilderSetInfo {

    /**
     * A message code that indicates that a property is not supported by this builder set. A
     * validation message with this message code is created when a IpsArtefactBuilderConfig is
     * validated against the definition of this builder set and its allowed properties.
     */
    public final static String MSG_CODE_PROPERTY_NOT_SUPPORTED = "propertyNotSupported"; //$NON-NLS-1$

    /**
     * A message code that indicates that a property is in accordance with the JDK that is selected
     * for the java project of this builder set. A validation message with this message code is
     * created when a
     * <code>org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetConfig</code> is
     * validated against the definition of this builder set and its allowed properties.
     */
    public final static String MSG_CODE_PROPERTY_NO_JDK_COMPLIANCE = "propertyNoJdkCompliance"; //$NON-NLS-1$

    /**
     * Creates a {@link IIpsArtefactBuilderSet} instance based on this
     * {@link IIpsArtefactBuilderSetInfo} and returns it.
     */
    public IIpsArtefactBuilderSet create(IIpsProject ipsProject);

    /**
     * Creates a default configuration for a builder set defined by this info.
     */
    public IIpsArtefactBuilderSetConfigModel createDefaultConfiguration(IIpsProject ipsProject);

    /**
     * Returns the id by which the <code>IIpsArtefactBuilderSet</code> is registered with the
     * system.
     */
    public String getBuilderSetId();

    /**
     * Returns the label for the corresponding <code>IIpsArtefactBuilderSet</code>.
     */
    public String getBuilderSetLabel();

    /**
     * Returns the property definition object for the specified name or <code>null</code> if it
     * doesn't exist.
     */
    public IIpsBuilderSetPropertyDef getPropertyDefinition(String name);

    /**
     * Returns the properties defined for this IpsArtefactBuilderSet.
     */
    public IIpsBuilderSetPropertyDef[] getPropertyDefinitions();

    /**
     * Validates the provided IIpsArtefactBuilderSetConfig against this definition. Especially the
     * properties of the configuration are checked for their existence and the correct value.
     */
    public MessageList validateIpsArtefactBuilderSetConfig(IIpsProject ipsProject,
            IIpsArtefactBuilderSetConfigModel builderSetConfig);

    /**
     * Validates the property value of the property of an IpsArtefactBuilderSetConfig specified by
     * the propertyName. It returns <code>null</code> if validation is correct otherwise a
     * {@link Message} object is returned.
     */
    public Message validateIpsBuilderSetPropertyValue(IIpsProject ipsProject, String propertyName, String propertyValue);

}
