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
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
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
        monitor.beginTask("Create archive", getWorkload());
        if (archive.exists()) {
            archive.delete(true, true, null);
        }
        File file = archive.getLocation().toFile();
        JarOutputStream os ;
        try {
            os = new JarOutputStream(new FileOutputStream(file));
        } catch (IOException e) {
            throw new CoreException(new IpsStatus("Error opending output stream for jar file " + file, e));
        }
        for (int i = 0; i < roots.length; i++) {
            addToArchive(roots[i], os, monitor);
        }
        try {
            os.close();
        } catch (IOException e) {
            throw new CoreException(new IpsStatus("Error closing output stream for jar file " + file, e));
        }
        archive.refreshLocal(0, null);
        monitor.done();
    }
    
    private void addToArchive(IIpsPackageFragmentRoot root, JarOutputStream os, IProgressMonitor monitor) throws CoreException {
        IIpsPackageFragment[] packs = root.getIpsPackageFragments();
        for (int i = 0; i < packs.length; i++) {
            addToArchive(packs[i], os, monitor);
            if (monitor.isCanceled()) {
                throw new OperationCanceledException();
            }
        }
    }
    
    private void addToArchive(IIpsPackageFragment pack, JarOutputStream os, IProgressMonitor monitor) throws CoreException {
        IIpsElement[] elements = pack.getChildren();
        for (int i = 0; i < elements.length; i++) {
            addToArchive((IIpsSrcFile)elements[i], os);
            monitor.worked(1);
            if (monitor.isCanceled()) {
                throw new OperationCanceledException();
            }
        }
    }
    
    private void addToArchive(IIpsSrcFile file, JarOutputStream os) throws CoreException {
        String content = file.getContentFromEnclosingResource();
        JarEntry newEntry = new JarEntry(file.getQualifiedNameType().toPath().toString());
        try {
            os.putNextEntry(newEntry);
            os.write(content.getBytes(file.getIpsProject().getXmlFileCharset()));
        } catch (IOException e) {
            throw new CoreException(new IpsStatus("Error writing archive entry for ips src file " + file, e));
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
