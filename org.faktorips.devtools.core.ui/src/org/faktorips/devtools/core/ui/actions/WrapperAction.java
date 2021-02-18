/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

/**
 * Generic action wrapping action delegates defined by other plugins.
 * 
 * @author Thorsten Guenther
 * @author Stefan Widmaier
 */
public class WrapperAction extends IpsAction {

    /**
     * The action delegate this action wrapps.
     */
    private IActionDelegate wrappedActionDelegate = null;

    /**
     * Creates a new wrapper action for the given ids. The WrapperAction therefor searches action
     * definitions of extensions/plugins to retrieve the classname of the actual action.
     * <p>
     * First all actionset definitions are searched for the given actionset id and action id.
     * Secondly all popup menu definitions are searched for the given action id, the actionset id is
     * ignored in this case.
     * <p>
     * The found classname is used to instanciate the requested delegate action. If no action
     * delegate can be instanciated this wrapperaction does nothing when run.
     * 
     * @param selectionProvider The provider to get the selection from to let the wrapped action
     *            work on.
     * @param label The label of this action in the GUI.
     * @param tooltip The tooltip of this action.
     * @param actionSetId The id of the action set to get the action from.
     * @param actionId The id of the action to wrap.
     */
    public WrapperAction(ISelectionProvider selectionProvider, String label, String tooltip, String actionSetId,
            String actionId) {

        super(selectionProvider);
        setText(label);
        setToolTipText(tooltip);

        initDelegate(actionSetId, actionId);
    }

    /**
     * The wrapperaction is created with the image given by imageName. If no image with this name
     * can be found, the action will be created without an icon.
     * 
     * @param selectionProvider The provider to get the selection from to let the wrapped action
     *            work on.
     * @param label The label of this action in the GUI.
     * @param tooltip The tooltip of this action.
     * @param imageName The name of the icon/image that should be used for this action in the GUI.
     *            If no image with the given name can be found or if imageName ist null this action
     *            is created without an image.
     * @param actionSetId The id of the action set to get the action from.
     * @param actionId The id of the action to wrap.
     */
    public WrapperAction(ISelectionProvider selectionProvider, String label, String tooltip, String imageName,
            String actionSetId, String actionId) {

        this(selectionProvider, label, tooltip, actionSetId, actionId);
        if (imageName != null) {
            ImageDescriptor imageDescriptor = IpsUIPlugin.getImageHandling().createImageDescriptor(imageName);
            if (imageDescriptor != null) {
                setImageDescriptor(imageDescriptor);
            }
        }
    }

    private void initDelegate(String actionSetId, String actionId) {
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IConfigurationElement[] elems = registry.getConfigurationElementsFor("org.eclipse.ui.actionSets"); //$NON-NLS-1$
        String className = searchActionSetsForDefinitions(actionSetId, actionId, elems);

        if (className == null) {
            className = searchPopupMenuForDefinitions(actionId, registry);
        }

        if (className != null) {
            try {
                wrappedActionDelegate = (IActionDelegate)Class.forName(className).getConstructor().newInstance();
                // CSOFF: IllegalCatch
            } catch (Exception e) {
                // CSON: IllegalCatch
                IpsPlugin.log(e);
            }
        }
        if (wrappedActionDelegate == null) {
            super.setEnabled(false);
        }
    }

    private String searchPopupMenuForDefinitions(String actionId, IExtensionRegistry registry) {
        String className = null;
        IConfigurationElement[] popupElements = registry.getConfigurationElementsFor("org.eclipse.ui.popupMenus"); //$NON-NLS-1$
        for (IConfigurationElement popupElement : popupElements) {
            IConfigurationElement[] actionElements = popupElement.getChildren("action"); //$NON-NLS-1$
            for (IConfigurationElement actionElement : actionElements) {
                if (actionElement.getAttribute("id").equals(actionId)) { //$NON-NLS-1$
                    className = actionElement.getAttribute("class"); //$NON-NLS-1$
                    break;
                }
            }
            if (className != null) {
                break;
            }
        }
        return className;
    }

    private String searchActionSetsForDefinitions(String actionSetId, String actionId, IConfigurationElement[] elems) {
        String className = null;
        for (IConfigurationElement elem : elems) {
            if (elem.getName().equals("actionSet") && elem.getAttribute("id").equals(actionSetId)) { //$NON-NLS-1$ //$NON-NLS-2$
                IConfigurationElement[] childElems = elem.getChildren("action"); //$NON-NLS-1$
                for (IConfigurationElement childElem : childElems) {
                    if (childElem.getAttribute("id").equals(actionId)) { //$NON-NLS-1$
                        className = childElem.getAttribute("class"); //$NON-NLS-1$
                        break;
                    }
                }
            }
            if (className != null) {
                break;
            }
        }
        return className;
    }

    /**
     * Sets the selection of the wrapped action and runs it. Does nothing if no action delegate
     * could be instanciated when this action was initialized. {@inheritDoc}
     */
    @Override
    public void run(IStructuredSelection selection) {
        if (wrappedActionDelegate != null) {
            if (wrappedActionDelegate instanceof IObjectActionDelegate) {
                IObjectActionDelegate objectActionDelegate = (IObjectActionDelegate)wrappedActionDelegate;
                objectActionDelegate.setActivePart(this, PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                        .getPartService().getActivePart());
            }
            wrappedActionDelegate.selectionChanged(this, selection);
            wrappedActionDelegate.run(this);
        }
    }
}
