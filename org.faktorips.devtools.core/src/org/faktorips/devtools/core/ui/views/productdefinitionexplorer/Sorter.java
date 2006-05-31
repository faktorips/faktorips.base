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

package org.faktorips.devtools.core.ui.views.productdefinitionexplorer;

import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.ui.FolderPropertiesPage;

/**
 * Class to sort folders displayed in the product component explorer by the sorting number 
 * set in the folder properties.
 * 
 * @author Thorsten Guenther
 */
public class Sorter extends ViewerSorter {

    public int compare(Viewer viewer, Object o1, Object o2) {
    	if (o1 instanceof IIpsProject) {
    		// if we have a project, the first given project is always lesser than the second one. 
    		// this is a workarround for a strange behaviour of the ViewerSorter, which results
    		// in switched projects sometimes, which is confusing for the user...
    		return -1;
    	}
    	
		if (!(o1 instanceof IIpsPackageFragment) && !(o2 instanceof IIpsPackageFragment)) {
			// no folder involved, let the superclass decide
			return super.compare(viewer, o1, o2);
		}
		
		// place folders in front of files
		if (o1 instanceof IIpsPackageFragment && !(o2 instanceof IIpsPackageFragment)) {
			return -1;
		}
		else if (!(o1 instanceof IIpsPackageFragment) && o2 instanceof IIpsPackageFragment) {
			return 1;
		}
		
		// sort folders by sorting-property
		try {
			IFolder f1 = (IFolder)((IIpsPackageFragment)o1).getCorrespondingResource();
			IFolder f2 = (IFolder)((IIpsPackageFragment)o2).getCorrespondingResource();
			
			if (f1 == null || !f1.exists() || f2 == null || !f2.exists()) {
				return 0;
			}
			
			String o1Value = f1.getPersistentProperty(FolderPropertiesPage.SORTING_ORDER_PROPERTY);
			String o2Value = f2.getPersistentProperty(FolderPropertiesPage.SORTING_ORDER_PROPERTY);
			
			if (o1Value == null) {
				o1Value = "0"; //$NON-NLS-1$
			}
			if (o2Value == null) {
				o2Value = "0"; //$NON-NLS-1$
			}
			
			Integer o1Order = Integer.valueOf(o1Value);
			Integer o2Order = Integer.valueOf(o2Value);
			int comp = o1Order.compareTo(o2Order);
			return comp;
			
		} catch (Exception e) {
			IpsPlugin.log(e);
		}
		
		return 0;
    	
    }

}
