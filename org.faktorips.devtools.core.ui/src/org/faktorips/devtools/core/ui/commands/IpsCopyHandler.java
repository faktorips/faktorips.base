/***************************************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 * 
 **************************************************************************************************/

package org.faktorips.devtools.core.ui.commands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.handlers.HandlerUtil;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPartState;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsArchive;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;

/**
 * Copy of objects controlled by FaktorIps.
 */
public class IpsCopyHandler extends IpsAbstractHandler {

    @Override
    public void execute(ExecutionEvent event, IWorkbenchPage activePage, IIpsSrcFile ipsSrcFile)
            throws ExecutionException {
        IStructuredSelection selection = (IStructuredSelection)HandlerUtil.getCurrentSelectionChecked(event);
        Clipboard clipboard = new Clipboard(HandlerUtil.getActiveShellChecked(event).getDisplay());

        copyToClipboard(selection, clipboard);
    }

    public void copyToClipboard(IStructuredSelection selection, Clipboard clipboard) {
        List<IpsObjectPartState> copiedObjects = new ArrayList<IpsObjectPartState>();
        List<IResource> copiedResources = new ArrayList<IResource>();
        List<String> copiedResourceLinks = new ArrayList<String>();

        IIpsObjectPart part;
        for (Iterator<Object> iter = getSelectionIterator(selection); iter.hasNext();) {
            Object selected = iter.next();

            if (selected instanceof IIpsObjectPart) {
                part = (IIpsObjectPart)selected;
                copiedObjects.add(new IpsObjectPartState(part));
            } else if (selected instanceof IIpsElement) {
                IIpsPackageFragmentRoot root = null;
                IIpsArchive ipsArchive = null;
                if (selected instanceof IIpsObject) {
                    root = ((IIpsObject)selected).getIpsPackageFragment().getRoot();
                } else if (selected instanceof IIpsPackageFragment) {
                    root = ((IIpsPackageFragment)selected).getRoot();
                }
                if (root != null) {
                    try {
                        ipsArchive = root.getIpsArchive();
                    } catch (CoreException e) {
                        IpsPlugin.log(e);
                    }
                }
                // copy links in an archive file
                if (ipsArchive != null) {
                    if (selected instanceof IIpsObject) {
                        copiedResourceLinks.add(getResourceLinkInArchive((IIpsObject)selected));
                        continue;
                    } else if (selected instanceof IIpsPackageFragment) {
                        copiedResourceLinks.add(getResourceLinkInArchive((IIpsPackageFragment)selected));
                        continue;
                    }
                }

                IResource resource = ((IIpsElement)selected).getEnclosingResource();
                if (resource != null) {
                    copiedResources.add(resource);
                }
            } else if (selected instanceof IFolder | selected instanceof IFile) {
                copiedResources.add((IResource)selected);
            }
        }

        if (copiedObjects.size() > 0 || copiedResources.size() > 0 || copiedResourceLinks.size() > 0) {
            clipboard.setContents(getDataArray(copiedObjects, copiedResources, copiedResourceLinks),
                    getTypeArray(copiedObjects, copiedResources, copiedResourceLinks));
        }
    }

}
