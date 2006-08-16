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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsPackageFragment;

/**
 * Filter used by ModelExplorer to filter out empty parent packages in flat
 * layout style.
 * 
 * @author Stefan Widmaier
 */
public class ModelExplorerFlatLayoutFilter extends ViewerFilter {

	/**
	 * Returns false for packagefragments that do not contain children (files)
	 * and at the same time contain subfragments (subfolders) (prevents empty 
	 * or newly created packagefragments from being hidden in the view). False
	 * is also returned if the given <code>element</code> is the the default
	 * packagefragment of its packagefragment root. In all other cases true is
	 * returned.
	 * {@inheritDoc}
	 */
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (element instanceof IIpsElement) {
			try {
				if (element instanceof IIpsPackageFragment) {
					IIpsPackageFragment frag = (IIpsPackageFragment) element;
					return frag.hasChildren() || frag.getIpsChildPackageFragments().length == 0;
				} else {
					return true;
				}
			} catch (CoreException e) {
				IpsPlugin.log(e);
				return true;
			}
		} else if (element instanceof IResource) {
			return true;
		} else {
			return true;
		}
	}

}
