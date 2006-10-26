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
import org.faktorips.datatype.ValueDatatype;
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
	
	public final static String PROPERTY_BUILDER_SET_ID = "builderSetId"; //$NON-NLS-1$
	
    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "IPSPROJECT-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the ips artefact builder set id is unknown.
     */
    public final static String MSGCODE_UNKNOWN_BUILDER_SET_ID = MSGCODE_PREFIX + "UnknwonBuilderSetId"; //$NON-NLS-1$
	
    /**
     * Validation message code to indicate that a used predefined datatype is unknown.
     */
    public final static String MSGCODE_UNKNOWN_PREDEFINED_DATATYPE = MSGCODE_PREFIX + "UnknownPredefinedDatatype"; //$NON-NLS-1$

    /**
	 * Validates the project properties. 
	 */
	public MessageList validate(IIpsProject ipsProject) throws CoreException;

	/**
	 * Returns id of the builderset used to generate sourcecode from the model / product definition.
	 */
	public String getBuilderSetId();

	/**
	 * Sets the id of the builderset used to generate sourcecode from the model / product definition.
	 */
	public void setBuilderSetId(String id);

    /**
     * Returns the IpsProject specific configuration of the IIpsArtefactBuilderSet.
     */
    public IIpsArtefactBuilderSetConfig getBuilderSetConfig();

    /**
     * Sets the IpsProjects specific configuration of the IIpsArtefactBuilderSet.
     */
    public void setBuilderSetConfig(IIpsArtefactBuilderSetConfig config);
    
	/**
	 * Returns the objct path to lookup objets.
	 */
	public IIpsObjectPath getIpsObjectPath();

	/**
	 * Sets the object path.
	 */
	public void setIpsObjectPath(IIpsObjectPath path);

	/**
	 * Returns <code>true</code> if this is a project containing a (part of a) model,
	 * otherwise <code>false</code>. The model is made up of police component types,
	 * product component types and so on.
	 */
	public boolean isModelProject();

	/**
	 * Sets if this is project containing model elements or not.
	 */
	public void setModelProject(boolean modelProject);

	/**
	 * Returns <code>true</code> if this is a project containing product definition
	 * data, otherwise <code>false</code>. Product definition projects are shown
	 * in the product defintion perspektive.
	 */
	public boolean isProductDefinitionProject();

	/**
	 * Sets if this is project contains product definition data.
	 */
	public void setProductDefinitionProject(
			boolean productDefinitionProject);

	/**
	 * Returns the Locale that is used to generate names and identifiers in the 
	 * generated source code.
	 */
	public Locale getJavaSrcLanguage();

	/**
	 * Sets the locale that is used to generate the names and identifiers
	 * in the generated source code.
	 * @param javaSrcLanguage
	 */
	public void setJavaSrcLanguage(Locale javaSrcLanguage);

	/**
	 * Returns the strategy how product component names are composed. 
	 */
	public IProductCmptNamingStrategy getProductCmptNamingStrategy();

	/**
	 * Sets the strategy how product component names are composed. 
	 */
	public void setProductCmptNamingStrategy(
			IProductCmptNamingStrategy newStrategy);

	/**
	 * Sets the naming convention for changes over time (by id) used in the
	 * generated sourcecode.
	 * 
	 * @see IChangesOverTimeNamingConvention
	 */
	public void setChangesOverTimeNamingConventionIdForGeneratedCode(
			String changesInTimeConventionIdForGeneratedCode);

	/**
	 * Returns the id of the naming convention for changes over time used in
	 * the generated sourcecode.
	 * 
	 * @see IChangesOverTimeNamingConvention
	 */
	public String getChangesOverTimeNamingConventionIdForGeneratedCode();

	/**
	 * Returns predefined datatypes (by id) used by this project.
	 * Predefined datatypes are those that are defined by the extension
	 * <code>datatypeDefinition</code>.
	 */
	public String[] getPredefinedDatatypesUsed();

	/**
	 * Sets the predefined datatypes (by id) used by this project.
	 * Predefined datatypes are those that are defined by the extension
	 * <code>datatypeDefinition</code>.
	 * 
	 * @throws NullPointerException if datatypes is <code>null</code>.
	 */
	public void setPredefinedDatatypesUsed(String[] datatypes);

	/**
	 * Sets the predefined datatypes used by this project.
	 * Predefined datatypes are those that are defined by the extension
	 * <code>datatypeDefinition</code>.
	 * <p>
	 * If one of the datatypes isn't a predefined one, the project properties
	 * become invalid. 
	 * 
	 * @throws NullPointerException if datatypes is <code>null</code>.
	 */
	public void setPredefinedDatatypesUsed(ValueDatatype[] datatypes);

	/**
	 * Returns the value datatypes that are defined in this project.
	 */
	public DynamicValueDatatype[] getDefinedDatatypes();

	/**
	 * Sets the value datatypes that are defined in this project.
	 */
	public void setDefinedDatatypes(DynamicValueDatatype[] datatypes);
	
	/**
	 * Adds the defined value datatype. If the project properties contain another
	 * datatype with the same id, the new datatype replaces the old one.
	 * 
	 * @throws NullPointerException if datatype is <code>null</code>.
	 */
	public void addDefinedDatatype(DynamicValueDatatype datatype);

	/**
	 * Returns the prefix to be used for new runtime-ids for product components.
	 */
	public String getRuntimeIdPrefix();

	/**
	 * Sets the new prefix to be used for new runtime-ids for product components.
	 * 
	 * @throws NullPointerException if the given prefix is <code>null</code>.
	 */
	public void setRuntimeIdPrefix(String runtimeIdPrefix);
	
	/**
	 * Returns <code>true</code> if the Java project belonging to the ips project, contains
	 * (value) classes that are used as defined dynamic datatype, otherwise <code>false</code>.
	 * <p>
	 * Note that is preferable to develop and access these classes either in a seperate Java project or
	 * to provide them in a Jarfile. The reason for this is that in this scenario the clean build won't
	 * work properly. When the IpsBuilder builds the project the dynamic datatype needs to load the 
	 * class it is based upon. However as the Java builder hasn't compiled the Java sourcefile into
	 * a classfile the dynamic datatype won't find it's class, the datatype becomes invalid and hence 
	 * we can't build the project.
	 * 
	 * @see DynamicValueDatatype
	 * @see org.faktorips.devtools.core.internal.model.ClassLoaderProvider
	 */
	public boolean isJavaProjectContainsClassesForDynamicDatatypes();

	/**
	 * @see #isJavaProjectContainsClassesForDynamicDatatypes()
	 */
	public void setJavaProjectContainsClassesForDynamicDatatypes(boolean newValue);
    
    /**
     * Returns <code>true</code> if the rule is enabled, otherwise <code>false</code>.
     * See the message code for the violation of this rule for further details.
     * 
     * @see org.faktorips.devtools.core.model.pctype.IPolicyCmptType#MSGCODE_MUST_IMPLEMENT_CONTAINER_RELATION
     */
    public boolean isContainerRelationIsImplementedRuleEnabled();
    
    /**
     * @see #isContainerRelationIsImplementedRuleEnabled()
     */
    public void setContainerRelationIsImplementedRuleEnabled(boolean enabled);
	
    /**
     * @return The ids of all required features.
     */
    public String[] getRequiredIpsFeatureIds();
    
    /**
     * @param featureId The id of the feature the min version has to be returned
     * @return The version number for the given feature id or <code>null</code>, if no entry is found for the given feature id.
     */
    public String getMinRequiredVersionNumber(String featureId);
    
    /**
     * Set the min version for the given feature id. If the feature id was not used before, a new entry with the given 
     * feature id is created.
     * 
     * @param featureId The id of the required feature.
     * @param version The minimum version number for this feature.
     */
    public void setMinRequiredVersionNumber(String featureId, String version);

}