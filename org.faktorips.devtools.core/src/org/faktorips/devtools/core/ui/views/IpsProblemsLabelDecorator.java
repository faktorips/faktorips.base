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

package org.faktorips.devtools.core.ui.views;

import java.util.ArrayList;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.ui.JavaElementImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.faktorips.devtools.core.ImageDescriptorRegistry;
import org.faktorips.devtools.core.ImageImageDescriptor;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;

/**
 * Problemdecorator for Ips-projects. This decorator marks IpsObjects themselves,
 * packagefragments, packagefragmentroots and IpsProjects the objects are 
 * located in with warning and error icons if problems are detected.<p>
 * The IpsProblemsLabelDecorator is configurable for flat or hierarchical
 * layout styles in treeviewers. 
 * 
 * @author Stefan Widmaier
 */
public class IpsProblemsLabelDecorator implements ILabelDecorator, ILightweightLabelDecorator {
	/**
	 * Indicates if the LabelDecorator works with a flat or hierarchical viewstructure.
	 * True means flat layout, false means hierarchical layout.
	 * Default is false for use with the hierarchical ProductStructureExplorer.
	 */
	private boolean isFlatLayout= false;
	/**
	 * {@inheritDoc}
	 */	
	public Image decorateImage(Image image, Object element) {
		if(image != null){
			try {
				ImageDescriptor baseImage= new ImageImageDescriptor(image);
				Rectangle bounds= image.getBounds();
				return getRegistry().get(
	                    new JavaElementImageDescriptor(baseImage, computeAdornmentFlags(element), new Point(bounds.width, bounds.height)));
			} catch (CoreException e) {
				IpsPlugin.log(e);
			}
		}
		return image;
	}

	private int computeAdornmentFlags(Object element) throws CoreException {
		if (element instanceof IIpsElement) {
			IIpsElement ipsElement = ((IIpsElement)element);
	        if (ipsElement != null) {
				IResource res = ipsElement.getEnclosingResource();
				
				if (res == null || !res.isAccessible()) {
					return 0;
				}
		
				int flag = 0;
				 
				IMarker[] markers;
				if(isFlatLayout){
					/* In flat layout every packagefragment is represented in its own treeitem, 
					 * thus packagefragments of parentfolders should not be decorated. 
					 * Only search the packagefragments children (files) for problems,
					 * no search is needed in the folder tree.
					 */
					markers= res.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_ONE);
				}else{
					markers= res.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
				}
				for (int i= 0; i < markers.length && (flag != JavaElementImageDescriptor.ERROR); i++) {
					if (markers[i].exists()) {
						int prio = markers[i].getAttribute(IMarker.SEVERITY, -1);
						if (prio == IMarker.SEVERITY_WARNING) {
							flag = JavaElementImageDescriptor.WARNING;
						} 
						else if (prio == IMarker.SEVERITY_ERROR) {
							flag = JavaElementImageDescriptor.ERROR;
						}
					}
				}			
				return flag;
			}
        }
		return 0;
	}

	private ImageDescriptorRegistry registry = null;
	private ImageDescriptorRegistry getRegistry() {
		if (registry == null) {
			this.registry = new ImageDescriptorRegistry();
		}
		return this.registry;
	}

	public String decorateText(String text, Object element) {
		return text;
	}

	private ArrayList listeners = null;
	public void addListener(ILabelProviderListener listener) {
		if (listeners == null) {
			listeners = new ArrayList();
		}
		listeners.add(listener);
		
	}

	public void dispose() {
		this.listeners = null;
		this.registry = null;
	}

	public boolean isLabelProperty(Object element, String property) {
		return true;
	}

	public void removeListener(ILabelProviderListener listener) {
		if (listener != null) {
			this.listeners.remove(listener);
		}
	}

	public void decorate(Object element, IDecoration decoration) {
		
		try {
			int adornmentFlags= computeAdornmentFlags(element);
			if (adornmentFlags == JavaElementImageDescriptor.ERROR) {
				decoration.addOverlay(IpsPlugin.getDefault().getImageDescriptor("ovr16/error_co.gif")); //$NON-NLS-1$
			} else if (adornmentFlags == JavaElementImageDescriptor.WARNING) {
				decoration.addOverlay(IpsPlugin.getDefault().getImageDescriptor("ovr16/warning_co.gif")); //$NON-NLS-1$
			}
		} catch (CoreException e) {
			IpsPlugin.log(e);
		}		
	}

	/**
	 * Sets the layout style the Decorator should work on.
	 */
	public void setFlatLayout(boolean isFlatLayout) {
		this.isFlatLayout = isFlatLayout;
	}

}
