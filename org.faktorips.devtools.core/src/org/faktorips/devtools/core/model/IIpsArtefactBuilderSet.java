/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.builder.IJavaPackageStructure;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableAccessFunction;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.fl.CompilationResult;

/**
 * Interface for the extension point org.faktorips.plugin.artefactbuilderset of
 * this plugin. Only one implementation of this interface can be registered for
 * this extension point. If more than one extensions are declared only the first
 * one will be registed and the others will be ignored. An IpsArtefactBuilderSet
 * collects a list of IpsArtefactBuilders and makes them available to the build
 * system.
 * 
 * @author Peter Erzberger
 */
public interface IIpsArtefactBuilderSet extends IJavaPackageStructure {

	/**
	 * The xml element name.
	 */
	public final static String XML_ELEMENT = "IpsArtefactBuilderSet"; //$NON-NLS-1$

	/**
	 * Returns all IpsArtefactBuilders of this set.
	 * 
	 * @return if the set is empty an empty array has to be returned
	 */
	public IIpsArtefactBuilder[] getArtefactBuilders();

	/**
	 * Returns <code>true</code> if the builder set supports table access
	 * functions, otherwise false.
	 */
	public boolean isSupportTableAccess();

	/**
	 * Returns <code>true</code> if the builder set supports an formula
	 * language identifierResolver.
	 */
	public boolean isSupportFlIdentifierResolver();

	/**
	 * Returns a compilation result that gives access to a table via the
	 * indicated function. Returns <code>null</code> if this builder set does
	 * not support table access.
	 * 
	 * @param tableContents for table structures that allow multiple contents the table contents
	 * 			is needed to identify for which table contents of a table structure a table
	 * 			access function is called. Can be null for single content table structures 
	 * 
	 * @param fct
	 *            The table access function code should be generated for.
	 * @param argResults
	 *            Compilation Results for the function's arguments.
	 * 
	 * @throws CoreException
	 *             if an error occurs while generating the code.
	 */
	public CompilationResult getTableAccessCode(ITableContents tableContents, ITableAccessFunction fct,
			CompilationResult[] argResults) throws CoreException;

	/**
	 * Returns the
	 * <code>org.faktorips.devtools.core.model.IParameterIdentifierResolver</code>.
	 * Returns <code>null</code> if this builder set doesn't support an
	 * formula language identifier resolver.
	 */
	public IParameterIdentifierResolver getFlParameterIdentifierResolver();

	/**
	 * Returns the qualified classname for the enum type that is defined by the given
	 * table structure.
	 * 
	 * @throws NullPointerException if structure is <code>null</code>.
	 * @throws IllegalArgumentException if the structure does not define an enum type. 
	 */
	public String getClassNameForTableBasedEnum(ITableStructure structure);
	
	/**
	 * Returns the file that contain the runtime repository toc file.
	 * Note that the file might not exists.
	 */
	public IFile getRuntimeRepositoryTocFile(IIpsPackageFragmentRoot root) throws CoreException;

	/**
	 * When the builder set is loaded by the faktor ips plugin the extension id
	 * is set by means of this method. This method is called before
	 * initialization.
	 * 
	 * @param id
	 *            the extension id
	 */
	public void setId(String id);

	/**
	 * When the builder set is loaded by the faktor ips plugin the extension
	 * describtion label is set by means of this method. This method is called
	 * before initialization.
	 * 
	 * @param id
	 *            the extension description label
	 */
	public void setLabel(String label);

	/**
	 * Returns the extension id declared in the plugin descriptor.
	 */
	public String getId();

	/**
	 * Returns the extension description label declared in the plugin
	 * descriptor.
	 */
	public String getLabel();

	/**
	 * Initializes this set. Creation of IpsArtefactBuilders has to go here
	 * instead of the constructor of the set implementation.
	 */
	public void initialize() throws CoreException;
	
	/**
	 * The call to the clean method of the IpsBuilder by the eclipse framework is delegate to this
	 * builder set. It is in the reponsiblity of the builder set to decide what happends to the artifacts 
	 * generated by the IIpsArtifactBuilders of this builder set.
	 * @throws CoreException
	 */
	public void clean() throws CoreException;
}
