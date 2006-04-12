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

import java.util.Locale;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.DynamicValueDatatype;
import org.faktorips.devtools.core.model.product.IProductCmptNamingStrategy;
import org.faktorips.util.message.MessageList;

/**
 * Properties of the ips project. The ips project can't keep the properties itself,
 * as it is a handle. The properties are persisted in the ".ipsproject" file.
 * 
 * @author Jan Ortmann
 */
public interface IIpsProjectProperties {
	
	public final static String PROPERTY_BUILDER_SET_ID = "builderSetId";
	
    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "IPSPROJECT-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the ips artefact builder set id is unknown.
     */
    public final static String MSGCODE_UNKNOWN_BUILDER_SET_ID = MSGCODE_PREFIX + "UnknwonBuilderSetId"; //$NON-NLS-1$
	
	/**
	 * Validates the project properties. 
	 */
	public MessageList validate(IIpsProject ipsProject) throws CoreException;

	/**
	 * Returns id of the builderset used to generate sourcecode from the model / product definition.
	 */
	public abstract String getBuilderSetId();

	/**
	 * Sets the id of the builderset used to generate sourcecode from the model / product definition.
	 */
	public abstract void setBuilderSetId(String id);

	/**
	 * Returns the objct path to lookup objets.
	 */
	public abstract IIpsObjectPath getIpsObjectPath();

	/**
	 * Sets the object path.
	 */
	public abstract void setIpsObjectPath(IIpsObjectPath path);

	/**
	 * Returns <code>true</code> if this is a project containing a (part of a) model,
	 * otherwise <code>false</code>. The model is made up of police component types,
	 * product component types and so on.
	 */
	public abstract boolean isModelProject();

	/**
	 * Sets if this is project containing model elements or not.
	 */
	public abstract void setModelProject(boolean modelProject);

	/**
	 * Returns <code>true</code> if this is a project containing product definition
	 * data, otherwise <code>false</code>. Product definition projects are shown
	 * in the product defintion perspektive.
	 */
	public abstract boolean isProductDefinitionProject();

	/**
	 * Sets if this is project contains product definition data.
	 */
	public abstract void setProductDefinitionProject(
			boolean productDefinitionProject);

	/**
	 * Returns the Locale that is used to generate names and identifiers in the 
	 * generated source code.
	 */
	public abstract Locale getJavaSrcLanguage();

	/**
	 * Sets the locale that is used to generate the names and identifiers
	 * in the generated source code.
	 * @param javaSrcLanguage
	 */
	public abstract void setJavaSrcLanguage(Locale javaSrcLanguage);

	/**
	 * Returns the strategy how product component names are composed. 
	 */
	public abstract IProductCmptNamingStrategy getProductCmptNamingStrategy();

	/**
	 * Sets the strategy how product component names are composed. 
	 */
	public abstract void setProductCmptNamingStrategy(
			IProductCmptNamingStrategy newStrategy);

	/**
	 * Sets the naming convention for changes over time (by id) used in the
	 * generated sourcecode.
	 * 
	 * @see IChangesOverTimeNamingConvention
	 */
	public abstract void setChangesOverTimeNamingConventionIdForGeneratedCode(
			String changesInTimeConventionIdForGeneratedCode);

	/**
	 * Returns the id of the naming convention for changes over time used in
	 * the generated sourcecode.
	 * 
	 * @see IChangesOverTimeNamingConvention
	 */
	public abstract String getChangesOverTimeNamingConventionIdForGeneratedCode();

	/**
	 * Returns predefined datatypes (by id) used by this project.
	 * Predefined datatypes are those that are defined by the extension
	 * <code>datatypeDefinition</code>.
	 */
	public abstract String[] getPredefinedDatatypesUsed();

	/**
	 * Sets the predefined datatypes (by id) used by this project.
	 * Predefined datatypes are those that are defined by the extension
	 * <code>datatypeDefinition</code>.
	 */
	public abstract void setPredefinedDatatypesUsed(String[] datatypes);

	/**
	 * Returns the value datatypes that are defined in this project.
	 */
	public abstract DynamicValueDatatype[] getDefinedDatatypes();

	/**
	 * Sets the value datatypes that are defined in this project.
	 */
	public abstract void setDefinedDatatypes(DynamicValueDatatype[] datatypes);

	/**
	 * Returns the prefix to be used for new runtime-ids for product components.
	 */
	public abstract String getRuntimeIdPrefix();

	/**
	 * Sets the new prefix to be used for new runtime-ids for product components.
	 * 
	 * @throws NullPointerException if the given prefix is <code>null</code>.
	 */
	public abstract void setRuntimeIdPrefix(String runtimeIdPrefix);

}