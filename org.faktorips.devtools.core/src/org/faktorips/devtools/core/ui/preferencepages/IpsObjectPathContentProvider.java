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

import java.util.ArrayList;

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

    /**
     * {@inheritDoc}
     */
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof IIpsSrcFolderEntry) {
            IIpsSrcFolderEntry entry = (IIpsSrcFolderEntry) parentElement;
            IIpsObjectPath objectPath = entry.getIpsObjectPath();
            boolean outputDefinedPerSrcFolder = objectPath.isOutputDefinedPerSrcFolder();

            ArrayList attributes = new ArrayList();
            if (outputDefinedPerSrcFolder) {

                // append the SrcFolderEntry's attributes (like basePackageDerived, basePackageMergable, outputFolderDerived, outputFolderMergable, 
                // sourceFolder and tocPath)
                IpsSrcFolderEntryAttribute attribute = newOutputFolderForDerivedJavaFiles(entry);
                attributes.add(attribute);
                
                attribute = newOutputFolderForMergableJavaFiles(entry);
                attributes.add(attribute);
                
                attribute = newTocPath(entry);
                if (attribute != null) {
                    attributes.add(attribute);
                }
            }

            return attributes.toArray();
        }

        return null;
    }

    private IpsSrcFolderEntryAttribute newTocPath(IIpsSrcFolderEntry entry) {
        IpsSrcFolderEntryAttribute attribute = null;
        
        if (entry.getBasePackageRelativeTocPath() != null)
            attribute = new IpsSrcFolderEntryAttribute(
                    IIpsSrcFolderEntryAttribute.SPECIFIC_TOC_PATH,
                    entry.getBasePackageRelativeTocPath());

        return attribute;
    }

    private IpsSrcFolderEntryAttribute newOutputFolderForMergableJavaFiles(IIpsSrcFolderEntry entry) {
        IpsSrcFolderEntryAttribute attribute;
        
        if (entry.getSpecificOutputFolderForMergableJavaFiles() == null)
            attribute = new IpsSrcFolderEntryAttribute(
                    IIpsSrcFolderEntryAttribute.DEFAULT_OUTPUT_FOLDER_FOR_MERGABLE_SOURCES, 
                    entry.getIpsObjectPath().getOutputFolderForMergableSources());
        else {
            attribute = new IpsSrcFolderEntryAttribute(
                    IIpsSrcFolderEntryAttribute.SPECIFIC_OUTPUT_FOLDER_FOR_MERGABLE_SOURCES,
                    entry.getSpecificOutputFolderForMergableJavaFiles()
            );
        }
        return attribute;
    }

    private IpsSrcFolderEntryAttribute newOutputFolderForDerivedJavaFiles(IIpsSrcFolderEntry entry) {
        IpsSrcFolderEntryAttribute attribute;
        if (entry.getSpecificOutputFolderForDerivedJavaFiles() == null)
            attribute = new IpsSrcFolderEntryAttribute(
                    IIpsSrcFolderEntryAttribute.DEFAULT_OUTPUT_FOLDER_FOR_DERIVED_SOURCES, 
                    entry.getIpsObjectPath().getOutputFolderForDerivedSources());
        else {
            attribute = new IpsSrcFolderEntryAttribute(
                    IIpsSrcFolderEntryAttribute.SPECIFIC_OUTPUT_FOLDER_FOR_DERIVED_SOURCES,
                    entry.getSpecificOutputFolderForDerivedJavaFiles()
            );
        }
        return attribute;
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
    public void dispose() { /* nothing to do */  }

    
    /**
     * {@inheritDoc}
     */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) { /* nothing to do */ }
    
}    
