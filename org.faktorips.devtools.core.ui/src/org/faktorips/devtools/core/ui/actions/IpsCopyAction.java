/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsArchive;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;

/**
 * Copy of objects controlled by FaktorIps. This action activates/deactivates itself according to
 * the current selection.
 * 
 * @author Thorsten Guenther
 * @author Stefan Widmaier
 */
public class IpsCopyAction extends IpsAction implements ISelectionChangedListener {

    private Clipboard clipboard;

    public IpsCopyAction(ISelectionProvider selectionProvider, Shell shell) {
        super(selectionProvider);
        clipboard = new Clipboard(shell.getDisplay());
        selectionProvider.addSelectionChangedListener(this);
    }

    @Override
    public void run(IStructuredSelection selection) {
        List<String> copiedObjects = new ArrayList<String>();
        List<IResource> copiedResources = new ArrayList<IResource>();
        List<String> copiedResourceLinks = new ArrayList<String>();

        // IIpsObjectPart part;
        for (Iterator<Object> iter = getSelectionIterator(selection); iter.hasNext();) {
            Object selected = iter.next();

            if (selected instanceof IIpsObjectPart) {
                // part = (IIpsObjectPart)selected;
                // TODO to be refactored with IpsDeleteAction, when inserting and deleting of
                // attributes is allowed. See FS#330
                // copiedObjects.add(new IpsObjectPartState(part).toString());
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
            clipboard.setContents(getDataArray(copiedObjects, copiedResources, copiedResourceLinks), getTypeArray(
                    copiedObjects, copiedResources, copiedResourceLinks));
        }
    }

    /**
     * Disabled this action if no copyable IpsElement is selected. {@inheritDoc}
     */
    @Override
    public void selectionChanged(SelectionChangedEvent event) {
        if (event.getSelection() instanceof IStructuredSelection) {
            IStructuredSelection selection = (IStructuredSelection)event.getSelection();
            Object[] objects = selection.toArray();
            boolean enabled = true;
            for (Object object : objects) {
                if (object instanceof IIpsObjectPart) {
                    enabled = false;
                }
            }
            setEnabled(enabled);
        } else {
            setEnabled(false);
        }
    }
}
