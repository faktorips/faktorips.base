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

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsProject;
/**
 * ViewerFilter for the ModelExplorer viewpart. It is used to filter objects 
 * in the tree by their types/classes. Uses a <code>ModelExplorerConfiguration</code>
 * to filter objects.
 * 
 * @author Stefan Widmaier
 */
public class ModelExplorerFilter extends ViewerFilter {
	private ModelExplorerConfiguration configuration;
	
	public ModelExplorerFilter(ModelExplorerConfiguration config){
		configuration= config;
	}
	
	/**
	 * Returns <code>true</code> for IIpsElements allowed by 
	 * <code>ModelExplorerConfiguration#isAllowedIpsElementType()</code> and for IpsProjects
	 * allowed by <code>ModelExplorerConfiguration#isAllowedIpsProject()</code>.
	 * False is returned for IpsElements, IpsProjetcs and other types not allowed by the configuration.
	 */
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if(element instanceof IIpsElement){
			IIpsElement ipsElement= (IIpsElement)element;
			if(configuration.isAllowedIpsElementType(ipsElement)){
				if(ipsElement instanceof IIpsProject){
					return configuration.isAllowedIpsProjectType((IIpsProject)ipsElement);
				}
				return true;
			}else{
				return false;
			}
		}else{
			return configuration.isAllowedResourceType(element);
		}
	}

}
