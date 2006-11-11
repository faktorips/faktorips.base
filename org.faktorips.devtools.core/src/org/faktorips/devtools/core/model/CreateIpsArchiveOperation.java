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

package org.faktorips.devtools.core.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.faktorips.devtools.core.IpsStatus;

/**
 * 
 * @author Jan Ortmann
 */
public class CreateIpsArchiveOperation implements IWorkspaceRunnable {

    private IIpsPackageFragmentRoot[] roots;
    private IFile archive;
    
    public CreateIpsArchiveOperation(IIpsProject projectToArchive, IFile archive) throws CoreException {
        this.roots = projectToArchive.getIpsPackageFragmentRoots();
        this.archive = archive;
    }

    public CreateIpsArchiveOperation(IIpsPackageFragmentRoot rootToArchive, IFile archive) throws CoreException {
        this(new IIpsPackageFragmentRoot[]{rootToArchive}, archive);
    }

    public CreateIpsArchiveOperation(IIpsPackageFragmentRoot[] rootsToArchive, IFile archive) {
        this.roots = rootsToArchive;
        this.archive = archive;
    }

    /**
     * {@inheritDoc}
     */
    public void run(IProgressMonitor monitor) throws CoreException {
        if (monitor==null) {
            monitor = new NullProgressMonitor();
        }
        monitor.beginTask("Create archive", getWorkload()); //$NON-NLS-1$
        File file = archive.getLocation().toFile();
        if (archive.getLocalTimeStamp()==file.lastModified()) {
            try {
                // windows file system does not return milliseconde, only seconds
                // if the cached timestamp is equal to the file's timestamp on disk,
                // we wait for 2 seconds before creating the file. After that time we
                // are sure that the file on disk gets a differnt time stamp when writing
                // the file. This hack has to be done, because we write using java.io.OutputStream
                // (because we have to use the JarOutputStream to zip) and therefore have to
                // call refreshLocal() afterwards. RefreshLocal refreshes only if we have a
                // different time stamp.
                Thread.sleep(2000);   
            } catch (InterruptedException e) {
                throw new CoreException(new IpsStatus(e));
            }
        }
        JarOutputStream os;
        try {
            os = new JarOutputStream(new FileOutputStream(file));
        } catch (IOException e) {
            throw new CoreException(new IpsStatus("Error opening output stream for jar file " + file, e)); //$NON-NLS-1$
        }
        Properties ipsObjectsProperties = new Properties();
        for (int i = 0; i < roots.length; i++) {
            addToArchive(roots[i], os, ipsObjectsProperties, monitor);
        }
        createIpsObjectsPropertiesEntry(os, ipsObjectsProperties);
        try {
            os.close();
        } catch (Exception e) {
            throw new CoreException(new IpsStatus("Error closing output stream for jar file " + file, e)); //$NON-NLS-1$
        }
        archive.refreshLocal(0, null);
        monitor.done();
    }

    private void addToArchive(IIpsPackageFragmentRoot root, JarOutputStream os, Properties ipsObjectsProperties, IProgressMonitor monitor) throws CoreException {
        IIpsPackageFragment[] packs = root.getIpsPackageFragments();
        for (int i = 0; i < packs.length; i++) {
            addToArchive(packs[i], os, ipsObjectsProperties, monitor);
            if (monitor.isCanceled()) {
                throw new OperationCanceledException();
            }
        }
    }
    
    private void addToArchive(IIpsPackageFragment pack, JarOutputStream os, Properties ipsObjectsProperties, IProgressMonitor monitor) throws CoreException {
        IIpsElement[] elements = pack.getChildren();
        for (int i = 0; i < elements.length; i++) {
            addToArchive((IIpsSrcFile)elements[i], os, ipsObjectsProperties);
            monitor.worked(1);
            if (monitor.isCanceled()) {
                throw new OperationCanceledException();
            }
        }
    }
    
    private void addToArchive(IIpsSrcFile file, JarOutputStream os, Properties ipsObjectsProperties) throws CoreException {
        String content = file.getContentFromEnclosingResource();
        String entryName = IIpsArchive.IPSOBJECTS_FOLDER + IPath.SEPARATOR + file.getQualifiedNameType().toPath().toString();
        JarEntry newEntry = new JarEntry(entryName);
        try {
            os.putNextEntry(newEntry);
            os.write(content.getBytes(file.getIpsProject().getXmlFileCharset()));
        } catch (IOException e) {
            throw new CoreException(new IpsStatus("Error writing archive entry for ips src file " + file, e)); //$NON-NLS-1$
        }
        String path = file.getQualifiedNameType().toPath().toString();
        String basePackageProperty = path + IIpsArchive.QNT_PROPERTY_POSTFIX_SEPARATOR + IIpsArchive.PROPERTY_POSTFIX_BASE_PACKAGE;
        ipsObjectsProperties.setProperty(basePackageProperty, file.getBasePackageNameForGeneratedJavaClass());
        String extensionPackageProperty = path + IIpsArchive.QNT_PROPERTY_POSTFIX_SEPARATOR + IIpsArchive.PROPERTY_POSTFIX_EXTENSION_PACKAGE;
        ipsObjectsProperties.setProperty(extensionPackageProperty, file.getBasePackageNameForExtensionJavaClass());
    }
    
    private void createIpsObjectsPropertiesEntry(JarOutputStream os, Properties ipsObjectsProperties) throws CoreException {
        JarEntry newEntry = new JarEntry(IIpsArchive.JAVA_MAPPING_ENTRY_NAME);
        try {
           os.putNextEntry(newEntry);
           ipsObjectsProperties.store(os, null);
        } catch (IOException e) {
            throw new CoreException(new IpsStatus("Error creating entry ipsobjects.properties", e)); //$NON-NLS-1$
        }
    }
    
    private int getWorkload() throws CoreException {
        int load = 0;
        for (int i = 0; i < roots.length; i++) {
            load =+ roots[i].getIpsPackageFragments().length;
        }
        return load;
    }


}
