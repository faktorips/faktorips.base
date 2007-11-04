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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsPackageNameComparator;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IType;

/**
 * Sorter for the ModelExplorer-TreeViewer. Sorts folders displayed in the ModelExplorer by the sorting number
 * set in the folder properties. PackageFragments are placed above Files, PolicyCmptTypes are placed above Tablestructures.
 * @author Stefan Widmaier
 */
public class ModelExplorerSorter extends ViewerSorter{

    private IpsPackageNameComparator packageComparator;

    public ModelExplorerSorter() {
        packageComparator = new IpsPackageNameComparator(false);
    }

	public int compare(Viewer viewer, Object o1, Object o2) {
		o1 = mapIpsSrcFileToIpsObject(o1);
		o2 = mapIpsSrcFileToIpsObject(o2);
        
        if(o1==null || o2==null){
			return 0;
		}
		// place TableStructures below PolicyComponentTypes
		if(o1 instanceof IType && o2 instanceof ITableStructure){
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
		// place pakages above files
		if (o1 instanceof IIpsPackageFragment && !(o2 instanceof IIpsPackageFragment)) {
			return -1;
		}
		else if (!(o1 instanceof IIpsPackageFragment) && o2 instanceof IIpsPackageFragment) {
			return 1;
		}
		if (o1 instanceof IIpsPackageFragmentRoot && o2 instanceof IIpsPackageFragmentRoot) {
            IIpsPackageFragmentRoot root1 = ((IIpsPackageFragmentRoot)o1);
            IIpsPackageFragmentRoot root2= ((IIpsPackageFragmentRoot)o2);
            try {
                return root1.getIpsObjectPathEntry().getIndex() - root2.getIpsObjectPathEntry().getIndex();
            } catch (CoreException e) {
                IpsPlugin.log(e);
                return root1.getName().compareTo(root2.getName());
            }
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
			// sort IpsPackages by SortDefinition
			return packageComparator.compare(o1, o2);
		}
        
        if (o1 instanceof IProductCmptGeneration && o2 instanceof IProductCmptGeneration) {
            // sort newest generation first
            IProductCmptGeneration g1 = (IProductCmptGeneration)o1;
            IProductCmptGeneration g2 = (IProductCmptGeneration)o2;
            return g1.getValidFrom().after(g2.getValidFrom()) ? -1 : g1.getValidFrom().before(g2.getValidFrom()) ? 1 : 0;
        }
        int typeMemberOrder1 = getTypeMemberOrder(o1);
        int typeMemberOrder2 = getTypeMemberOrder(o2);
        if (typeMemberOrder1>-1) {
            if (typeMemberOrder1==typeMemberOrder2) {
                return super.compare(viewer, o1, o2);
            } else if (typeMemberOrder2>-1) {
                return typeMemberOrder1 - typeMemberOrder2;
            }
        }
        if(o1 instanceof IPolicyCmptTypeAssociation && o2 instanceof IPolicyCmptTypeAttribute){
            return 1;
        }
        if(o1 instanceof IPolicyCmptTypeAttribute && o2 instanceof IPolicyCmptTypeAssociation){
            return -1;
        }

		// ------- IResource sorting -------
		// sort IpsProjects and IProjects lexicographically (ignoring case)
		if((o1 instanceof IIpsProject||o1 instanceof IProject) && (o2 instanceof IIpsProject||o2 instanceof IProject)){
			return getProjectName(o1).compareToIgnoreCase(getProjectName(o2));
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

    private String getProjectName(Object o) {
        if(o instanceof IProject){
            return ((IProject)o).getName();
        }else if(o instanceof IIpsProject){
            return ((IIpsProject)o).getName();
        }else{
            return ""; //$NON-NLS-1$
        }
    }

    private Object mapIpsSrcFileToIpsObject(Object o1) {
        if (! (o1 instanceof IIpsSrcFile)){
            return o1;
        }
        IIpsSrcFile ipsSrcFile = (IIpsSrcFile) o1;
        IpsObjectType ipsObjectType = ipsSrcFile.getIpsObjectType();
        if (ipsObjectType == null){
            return null;
        }
        return ipsObjectType.newObject(ipsSrcFile);
    }
    
    private int getTypeMemberOrder(Object member) {
        if (member instanceof IAttribute) {
            return 1;
        }
        if (member instanceof IAssociation) {
            return 2;
        }
        if (member instanceof IMethod) {
            return 3;
        }
        if (member instanceof ITableStructure) {
            return 4;
        }
        return -1;
        
    }
}
