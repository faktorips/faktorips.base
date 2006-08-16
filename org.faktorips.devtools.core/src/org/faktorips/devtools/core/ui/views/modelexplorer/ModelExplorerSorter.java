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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.faktorips.devtools.core.internal.model.IpsProject;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;

/**
 * Sorter for the ModelExplorer-TreeViwer. Sorts folders displayed in the ModelExplorer by the sorting number 
 * set in the folder properties. PackageFragments are placed above Files, PolicyCmptTypes are placed above Tablestructures.
 * @author Stefan Widmaier
 */
public class ModelExplorerSorter extends ViewerSorter{

	public int compare(Viewer viewer, Object o1, Object o2) {
		if(o1==null || o2==null){
			return 0;
		}
		// place TableStructures below PolicyComponentTypes
		if(o1 instanceof IPolicyCmptType && o2 instanceof ITableStructure){
			return -1;
		}
		if(o1 instanceof ITableStructure && o2 instanceof IPolicyCmptType){
			return 1;
		}
		// place TableContents below ProductCmpts
		if(o1 instanceof IProductCmpt && o2 instanceof ITableContents){
			return -1;
		}
		if(o1 instanceof ITableContents && o2 instanceof IProductCmpt){
			return 1;
		}

		// place folders above files
		if (o1 instanceof IIpsPackageFragment && !(o2 instanceof IIpsPackageFragment)) {
			return -1;
		}
		else if (!(o1 instanceof IIpsPackageFragment) && o2 instanceof IIpsPackageFragment) {
			return 1;
		}

		// sort IpsProjects lexicographically, super implementation is not sufficient/consistent in this case
		if(o1 instanceof IpsProject && o2 instanceof IpsProject){
			return ((IpsProject)o1).getName().compareTo(((IpsProject)o2).getName());
		}

		if (o1 instanceof IIpsPackageFragment && o2 instanceof IIpsPackageFragment) {
			IIpsPackageFragment fragment= ((IIpsPackageFragment)o1);
			IIpsPackageFragment fragment2= ((IIpsPackageFragment)o2);
			// place defaultpackage at top
			if(fragment.isDefaultPackage()){
				return -1;
			}
			if(fragment2.isDefaultPackage()){
				return 1;
			}
			// sort IpsProjects lexicographically
			return fragment.getName().compareTo(fragment2.getName());
		}
		
		// ------- IResource sorting -------
		// place IpsProjects above IProjects
		if(o1 instanceof IpsProject && o2 instanceof IProject){
			return -1;
		}
		if(o1 instanceof IProject && o2 instanceof IpsProject){
			return 1;
		}
		// Place model-data above other resources
		if(o1 instanceof IIpsElement && o2 instanceof IResource){
			return -1;
		}
		if(o1 instanceof IResource && o2 instanceof IIpsElement){
			return 1;
		}
		// Place folders above files
		if(o1 instanceof IFolder && o2 instanceof IFile){
			return -1;
		}
		if(o1 instanceof IFile && o2 instanceof IFolder){
			return 1;
		}
		
		// otherwise let the superclass decide
		return super.compare(viewer, o1, o2);
		
	}
	
}
