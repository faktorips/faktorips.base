/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.commands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.ui.handlers.HandlerUtil;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.internal.ipsobject.IpsObjectPartState;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsStorage;

/**
 * Copy of objects controlled by FaktorIps.
 */
public class IpsCopyHandler extends AbstractCopyPasteHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IStructuredSelection selection = (IStructuredSelection)HandlerUtil.getCurrentSelectionChecked(event);
        Clipboard clipboard = new Clipboard(HandlerUtil.getActiveShellChecked(event).getDisplay());

        copyToClipboard(selection, clipboard);
        return null;
    }

    public void copyToClipboard(IStructuredSelection selection, Clipboard clipboard) {
        List<IpsObjectPartState> copiedObjects = new ArrayList<>();
        List<IResource> copiedResources = new ArrayList<>();
        List<String> copiedResourceLinks = new ArrayList<>();

        IIpsObjectPart part;
        for (Iterator<Object> iter = getSelectionIterator(selection); iter.hasNext();) {
            Object object = iter.next();
            if (!(object instanceof IAdaptable adaptable)) {
                return;
            }
            if (adaptable.getAdapter(IIpsObjectPart.class) != null) {
                part = adaptable.getAdapter(IIpsObjectPart.class);
                copiedObjects.add(new IpsObjectPartState(part));
            } else if (adaptable.getAdapter(IIpsElement.class) != null) {
                IIpsElement selected = adaptable.getAdapter(IIpsElement.class);
                IIpsPackageFragmentRoot root = null;
                IIpsStorage ipsStorage = null;
                if (selected instanceof IIpsObject) {
                    root = ((IIpsObject)selected).getIpsPackageFragment().getRoot();
                } else if (selected instanceof IIpsPackageFragment) {
                    root = ((IIpsPackageFragment)selected).getRoot();
                }
                if (root != null) {
                    ipsStorage = root.getIpsStorage();
                }
                // copy links in an archive file
                if (ipsStorage != null) {
                    if (selected instanceof IIpsObject) {
                        copiedResourceLinks.add(getResourceLinkInArchive((IIpsObject)selected));
                        continue;
                    } else if (selected instanceof IIpsPackageFragment) {
                        copiedResourceLinks.add(getResourceLinkInArchive((IIpsPackageFragment)selected));
                        continue;
                    }
                }

                IResource resource = selected.getEnclosingResource().unwrap();
                if (resource != null) {
                    copiedResources.add(resource);
                }
            } else if (adaptable.getAdapter(IResource.class) != null) {
                copiedResources.add(adaptable.getAdapter(IResource.class));
            }
        }

        if (copiedObjects.size() > 0 || copiedResources.size() > 0 || copiedResourceLinks.size() > 0) {
            clipboard.setContents(getDataArray(copiedObjects, copiedResources, copiedResourceLinks),
                    getTypeArray(copiedObjects, copiedResources, copiedResourceLinks));
        }
    }

}
