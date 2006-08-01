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

package org.faktorips.devtools.core.ui.views.modelexplorer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.faktorips.devtools.core.internal.model.IpsPackageFragment;
import org.faktorips.devtools.core.internal.model.IpsPackageFragmentRoot;
import org.faktorips.devtools.core.internal.model.IpsProject;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsProject;

/**
 * Configuration class for <code>ModelExlporer</code>s, that can be asked if a specific
 * class or object is allowed and should be displayed in the explorer. 
 * The configuration does not check if a given object or class is subclass of an allowed 
 * type. Thus the concrete classes must be used instead of ther published interfaces. <p>
 * Provides an allow-mechanism for filtering <code>IpsProject</code>s. 
 * @author Stefan Widmaier
 */
public class ModelExplorerConfiguration {
	/**
	 * Mask for allowing modelprojects. Value is 1.
	 */
	public static final int ALLOW_MODEL_PROJECTS= 1;
	/**
	 * Mask for allowing productdefinition projects. Value is 2.
	 */
	public static final int ALLOW_PRODUCTDEFINITION_PROJECTS= 1<<1;
	/**
	 * Mask for allowing projects that ar neither model- nor 
	 * productdefinition projects. Value is 4.
	 */
	public static final int ALLOW_NONMODEL_NONPRODUCTDEFINTION_PROJECTS= 1<<2;
	
	/**
	 * Integer value set at instanciation. The three least significant bits
	 * specify which projects are allowed by this configuration. <p>
	 * The lowest bit indicates modelprojects are allowed. The second lowest indicates
	 * that productdefinition projects are allowed. The third bit indicates projects are 
	 * allowed that are neither model- nor productdefinition projects. <p>
	 * A value of 6 (110 in binary) for example indicates that projects without flags and
	 * productdefinition projects are allowed, modelprojects are not. 
	 * The value of this variable should be set using the ALLOW-bitmasks
	 * and the "|" (logical OR) operator. The value in the example above is 
	 * <code>ALLOW_PRODUCTDEFINITION_PROJECTS | ALLOW_NONMODEL_NONPRODUCTDEFINTION_PROJECTS</code> <p>
	 * Possible values range from 0 to 7, 111 in binary (default). 
	 */
	private int allowsProjects= ALLOW_MODEL_PROJECTS 
		| ALLOW_PRODUCTDEFINITION_PROJECTS 
		| ALLOW_NONMODEL_NONPRODUCTDEFINTION_PROJECTS;

	private HashSet allowedIpsElementTypes = new HashSet();

	private HashSet allowedResourceTypes = new HashSet();
	
	/**
	 * Constructs a default ModelExplorerConfiguration that allows all structural
	 * IpsElements that need to be displayed. The created instance allows types:
	 *  <code>IpsProject</code>, <code>IpsPackageFragmentRoot</code>
	 *  and <code>IpsPackageFragment</code>.
	 */
	public ModelExplorerConfiguration() {
		this(new Class[0], new Class[0], 
				ALLOW_MODEL_PROJECTS
				|ALLOW_PRODUCTDEFINITION_PROJECTS
				|ALLOW_NONMODEL_NONPRODUCTDEFINTION_PROJECTS);
	}

	/**
	 * Constructs a ModelExplorerConfiguration that allows the given list of
	 * IpsElement types and the given list of resource-types. IpsProject, 
	 * IpsPackageFragmentRoot and IpsPackageFragment are allowed by default. <p>
	 * The <code>allowsProjects</code> value specifies which projects are allowed by this 
	 * configuration.
	 * @param ipsElementTypes List of allowed IpsElement types.
	 * @param resourceTypes List of allowed resource types.
	 * @param allowsProjects Bitmask used for filtering projects.
	 */
	public ModelExplorerConfiguration(Class[] ipsElementTypes,
			Class[] resourceTypes, int allowsProjects) {
		// add default allowed types
		allowedIpsElementTypes.add(IpsProject.class);
		allowedIpsElementTypes.add(IpsPackageFragmentRoot.class);
		allowedIpsElementTypes.add(IpsPackageFragment.class);
		
		for (int i = 0, size = ipsElementTypes.length; i < size; i++) {
			allowedIpsElementTypes.add(ipsElementTypes[i]);
		}
		for (int i = 0, size = resourceTypes.length; i < size; i++) {
			allowedResourceTypes.add(resourceTypes[i]);
		}
		this.allowsProjects= allowsProjects;
	}

	/**
	 * Returns true if the given IpsElement's class is allowed by this
	 * configuration, false otherwise.
	 */
	public boolean isAllowedIpsElementType(IIpsElement type) {
		return allowedIpsElementTypes.contains(type.getClass());
	}
	/**
	 * Returns true if the given type is allowed by this
	 * configuration, false otherwise.
	 */
	public boolean isAllowedIpsElementType(Class type) {
		return allowedIpsElementTypes.contains(type);
	}
	
	/**
	 * Returns the list of allowed IpsElement types. No guarantees are made as to the
	 * iteration order of the list; in particular, it is not guaranteed that the order 
	 * will remain constant over time.
	 */
	public List getAllowedIpsElementTypes() {
		// create defensive copy
		return new ArrayList(allowedIpsElementTypes);
	}

	public boolean isAllowedResourceType(Object type) {
		return allowedResourceTypes.contains(type.getClass());
	}
	/**
	 * Returns true if the given project matches the filter specified by the 
	 * <code>allowsProjects</code> value.  
	 */
	public boolean isAllowedIpsProjectType(IIpsProject project) {
		if(project.isModelProject() & project.isProductDefinitionProject()){
			return (allowsProjects & (ALLOW_PRODUCTDEFINITION_PROJECTS | ALLOW_MODEL_PROJECTS)) != 0;
		}else if(project.isProductDefinitionProject()){
			return (allowsProjects & ALLOW_PRODUCTDEFINITION_PROJECTS) != 0;
		}else if(project.isModelProject()){
			return (allowsProjects & ALLOW_MODEL_PROJECTS) != 0;
		}else{ /* !isModelProject & !isProductDefinitionProject */
			return (allowsProjects & ALLOW_NONMODEL_NONPRODUCTDEFINTION_PROJECTS) != 0;
		}
	}
}
