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

package org.faktorips.devtools.core.ui.preferencepages;

import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;

/**
 * Content provider for the IPS object path
 * @author Roman Grutza
 */
public class IpsObjectPathContentProvider implements ITreeContentProvider {

    // TODO: add missing IIpsObjectPathEntry code paths!!
    
    private Viewer viewer;
    private IIpsObjectPath model;

    /**
     * {@inheritDoc}
     */
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof IIpsSrcFolderEntry) {
            IIpsSrcFolderEntry entry = (IIpsSrcFolderEntry) parentElement;
            IIpsObjectPath objectPath = entry.getIpsObjectPath();
            boolean outputDefinedPerSrcFolder = objectPath.isOutputDefinedPerSrcFolder();

            IFolder[] specificFolders = null;
            if (outputDefinedPerSrcFolder) {
                specificFolders = new IFolder[2];
                specificFolders[0] = entry.getSpecificOutputFolderForDerivedJavaFiles();
                specificFolders[1] = entry.getSpecificOutputFolderForMergableJavaFiles();
            }

            return specificFolders;
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Object getParent(Object element) {
        if (element instanceof IIpsObjectPathEntry) {
            return ((IIpsObjectPathEntry) element).getIpsObjectPath();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasChildren(Object element) {
        if (element instanceof IIpsObjectPathEntry) {
            IIpsObjectPath objectPath = ((IIpsObjectPathEntry) element).getIpsObjectPath() ;
            return objectPath.isOutputDefinedPerSrcFolder();
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof IIpsObjectPath)
            return ((IIpsObjectPath) inputElement).getEntries();
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void dispose() {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        this.viewer = viewer;
        if (newInput instanceof IIpsObjectPath) {
            model = (IIpsObjectPath) newInput;
        } else {
            model = null;
        }
    }
}    
