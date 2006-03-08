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

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewActionDelegate;
import org.faktorips.devtools.core.IpsPlugin;

/**
 * Generic action wrapping action delegates defined by other plugins.
 * 
 * @author Thorsten Guenther
 */
public class WrapperAction extends IpsAction {

	/**
	 * The action delegate this action wrapps.
	 */
	private IViewActionDelegate wrappedActionDelegate = null;

	/**
	 * Creates a new wrapper action for the given ids
	 * 
	 * @param selectionProvider The provider to get the selection from to let the wrapped action work on.
	 * @param actionSetId The id of the action set to get the action from.
	 * @param actionId The id of the action to wrap.
	 */
	public WrapperAction(ISelectionProvider selectionProvider, String name, String actionSetId, String actionId) {
		super(selectionProvider);
		setText(name);

		String className = null;
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IConfigurationElement[] elems = registry.getConfigurationElementsFor("org.eclipse.ui.actionSets"); //$NON-NLS-1$
        for (int i = 0; i < elems.length; i++) {
        	if (elems[i].getName().equals("actionSet") && elems[i].getAttribute("id").equals(actionSetId)) { //$NON-NLS-1$ //$NON-NLS-2$
        		IConfigurationElement[] childElems = elems[i].getChildren("action"); //$NON-NLS-1$
        		for (int j = 0; j < childElems.length; j++) {
					if (childElems[j].getAttribute("id").equals(actionId)) { //$NON-NLS-1$
		        		className = childElems[j].getAttribute("class"); //$NON-NLS-1$
		        		break;
					}
				}
        	}
		}
        
        if (className != null) {
        	try {
				wrappedActionDelegate = (IViewActionDelegate)Class.forName(className).newInstance();
			} catch (InstantiationException e) {
				IpsPlugin.log(e);
			} catch (IllegalAccessException e) {
				IpsPlugin.log(e);
			} catch (ClassNotFoundException e) {
				IpsPlugin.log(e);
			}
        }
        if (wrappedActionDelegate == null) {
        	super.setEnabled(false);
        }
	}

	/** 
	 * {@inheritDoc}
	 */
	public void run(IStructuredSelection selection) {
		if (wrappedActionDelegate != null) {
			wrappedActionDelegate.selectionChanged(this, selection);
			wrappedActionDelegate.run(this);
		}
	}
}
