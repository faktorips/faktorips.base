/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.faktorips.devtools.abstraction.AContainer;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.AJavaProject;
import org.faktorips.devtools.abstraction.APackageFragmentRoot;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.abstraction.AResource.AResourceTreeTraversalDepth;
import org.faktorips.devtools.abstraction.Abstractions;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.abstraction.util.PathUtil;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsArchive;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.util.IoUtil;

/**
 * An operation to create an IPS archive.
 *
 * @author Jan Ortmann
 */
public class CreateIpsArchiveOperation implements ICoreRunnable {

    private IIpsPackageFragmentRoot[] roots;
    private File archive;

    private boolean inclJavaSources;
    private boolean inclJavaBinaries;
    private Set<AFolder> handledRootFolders = new HashSet<>();
    private Set<String> handledEntries = new HashSet<>(1000);

    /**
     * Creates a new operation to create an IPS archive. From the given project the content from all
     * source folders are packed into the new archive.
     */
    public CreateIpsArchiveOperation(IIpsProject projectToArchive, File archive) {
        this.archive = archive;
        List<IIpsPackageFragmentRoot> rootsInt = new ArrayList<>();
        IIpsPackageFragmentRoot[] candidateRoots = projectToArchive.getIpsPackageFragmentRoots();
        for (IIpsPackageFragmentRoot candidateRoot : candidateRoots) {
            if (candidateRoot.isBasedOnSourceFolder()) {
                rootsInt.add(candidateRoot);
            }
        }
        roots = rootsInt.toArray(new IIpsPackageFragmentRoot[rootsInt.size()]);
    }

    public CreateIpsArchiveOperation(IIpsPackageFragmentRoot rootToArchive, File archive) {
        this(new IIpsPackageFragmentRoot[] { rootToArchive }, archive);
    }

    public CreateIpsArchiveOperation(IIpsPackageFragmentRoot[] rootsToArchive, File archive) {
        roots = rootsToArchive;
        this.archive = archive;
    }

    @Override
    public void run(IProgressMonitor monitor) {
        IProgressMonitor theMonitor = monitor == null ? new NullProgressMonitor() : monitor;

        SubMonitor subMonitor = SubMonitor.convert(theMonitor, Messages.CreateIpsArchiveOperation_Task_CreateArchive,
                100);

        SubMonitor exportMonitor = subMonitor.split(98);
        exportMonitor.beginTask(null, getWorkload());

        AFile workspaceFile = getWorkspaceFile();
        if (workspaceFile != null && workspaceFile.getLocalTimeStamp() == archive.lastModified()) {
            try {
                /*
                 * Force the archive's file to have different time stamp than the cached IFile. This
                 * is necessary so we can call refreshLocal() after finishing the archive, as
                 * eclipse refreshes only if a file had its time stamp changed.
                 *
                 * The reason for the 1010ms wait is that the windows file system does not track
                 * milliseconds but only full seconds with a file's last-modified date. Thus waiting
                 * for more than a second is necessary to guarantee that different time stamp.
                 *
                 * This little hack needs to be done, as we have to use JarOutputStream and thereby
                 * bypass eclipse's file-tracking.
                 */
                Thread.sleep(1010);
            } catch (InterruptedException e) {
                throw new IpsException(new IpsStatus(e));
            }
        }

        JarOutputStream os;
        try {
            os = new JarOutputStream(new FileOutputStream(archive));
        } catch (IOException e) {
            throw new IpsException(new IpsStatus("Error opening output stream for jar file " + archive, e)); //$NON-NLS-1$
        }
        Properties ipsObjectsProperties = new Properties();
        for (IIpsPackageFragmentRoot root : roots) {
            SubMonitor subExportMonitor = exportMonitor.split(root.getIpsPackageFragments().length);
            addToArchive(root, os, ipsObjectsProperties, subExportMonitor);
        }
        createIpsObjectsPropertiesEntry(os, ipsObjectsProperties);
        try {
            os.close();
            // CSOFF: IllegalCatch
        } catch (Exception e) {
            throw new IpsException(new IpsStatus("Error closing output stream for jar file " + archive, e)); //$NON-NLS-1$
        }
        // CSON: IllegalCatch
        SubMonitor refreshMonitor = subMonitor.split(2);
        refreshInWorkspaceIfNecessary(refreshMonitor);
    }

    /**
     * If the file exists in the workspace then refresh it.
     */
    private void refreshInWorkspaceIfNecessary(IProgressMonitor monitor) {
        AFile fileInWorkspace = getWorkspaceFile();
        if (fileInWorkspace == null) {
            // nothing to do, because the file dosn't exists in the workspace
            monitor.done();
            return;
        }
        if (fileInWorkspace.exists()) {
            fileInWorkspace.refreshLocal(AResourceTreeTraversalDepth.RESOURCE_ONLY, monitor);
        }
        // refresh parent, thus the file is new then the file will be visible in the workspace
        AContainer parent = fileInWorkspace.getParent();
        if (parent != null) {
            parent.refreshLocal(AResourceTreeTraversalDepth.RESOURCE_AND_DIRECT_MEMBERS, monitor);
        }
    }

    /**
     * Search and return the given file in the workspace, if the file isn't in the workspace return
     * <code>null</code>.
     */
    private AFile getWorkspaceFile() {
        for (AProject project : Abstractions.getWorkspace().getRoot().getProjects()) {
            Path projectPath = project.getLocation();
            Path filePath = archive.getAbsoluteFile().toPath();
            if (filePath.startsWith(projectPath)) {
                Path filePathInProject = projectPath.relativize(filePath);
                return project.getFile(filePathInProject);
            }
        }
        return null;
    }

    private void addToArchive(IIpsPackageFragmentRoot root,
            JarOutputStream os,
            Properties ipsObjectsProperties,
            IProgressMonitor monitor) {

        IIpsPackageFragment[] packs = root.getIpsPackageFragments();
        SubMonitor subMonitor = SubMonitor.convert(monitor, packs.length);
        for (IIpsPackageFragment pack : packs) {
            addToArchive(pack, os, ipsObjectsProperties, subMonitor.split(1));
            if (monitor.isCanceled()) {
                throw new OperationCanceledException();
            }
        }
        if (inclJavaBinaries || inclJavaSources) {
            addJavaFiles(root, os, monitor);
        }
    }

    private void addToArchive(IIpsPackageFragment pack,
            JarOutputStream os,
            Properties ipsObjectsProperties,
            IProgressMonitor monitor) {

        try {
            IIpsElement[] elements = pack.getChildren();
            monitor.beginTask(null, elements.length);
            for (IIpsElement element : elements) {
                addToArchive((IIpsSrcFile)element, os, ipsObjectsProperties);
                monitor.worked(1);
                if (monitor.isCanceled()) {
                    throw new OperationCanceledException();
                }
            }
        } finally {
            monitor.done();
        }
    }

    /**
     * Checks if the archive contains the given entry specified by the name, e.g. objects which were
     * copied from the source folder to the bin folder
     */
    private boolean isDuplicateEntry(String entryName) {
        if (handledEntries.contains(entryName)) {
            return true;
        }
        handledEntries.add(entryName);
        return false;
    }

    private void addToArchive(IIpsSrcFile file, JarOutputStream os, Properties ipsObjectsProperties) {
        InputStream content = file.getContentFromEnclosingResource();
        Path qualifiedNamePath = file.getQualifiedNameType().toPath();
        String path = qualifiedNamePath == null ? IpsStringUtils.EMPTY : PathUtil.toPortableString(qualifiedNamePath);
        String entryName = IIpsArchive.IPSOBJECTS_FOLDER + IPath.SEPARATOR
                + path;
        if (isDuplicateEntry(entryName)) {
            return;
        }
        writeJarEntry(os, content, entryName, file.getCorrespondingFile().getName());
        writeCustomIconIfApplicable(file, os);

        String basePackageProperty = path + IIpsArchive.QNT_PROPERTY_POSTFIX_SEPARATOR
                + IIpsArchive.PROPERTY_POSTFIX_BASE_PACKAGE_MERGABLE;
        ipsObjectsProperties.setProperty(basePackageProperty, file.getBasePackageNameForMergableArtefacts());
        String extensionPackageProperty = path + IIpsArchive.QNT_PROPERTY_POSTFIX_SEPARATOR
                + IIpsArchive.PROPERTY_POSTFIX_BASE_PACKAGE_DERIVED;
        ipsObjectsProperties.setProperty(extensionPackageProperty, file.getBasePackageNameForDerivedArtefacts());
    }

    /**
     * Creates a new {@link JarEntry} for the given {@link JarOutputStream} with the given entryName
     * and the content/data provided by the content {@link InputStream}.
     *
     * @param os the {@link JarOutputStream} to write the new {@link JarEntry} to.
     * @param content the content/data to be written to the new {@link JarEntry}.
     * @param entryName the name of the new {@link JarEntry}. Path segments will create
     *            folder/sub-folders in the jar. See {@link JarOutputStream} for details.
     * @param fileName the name of the original file whose content is written to the jar. It is used
     *            to create an error message in case an {@link IOException} occurs.
     * @throws IpsException if writing the {@link JarOutputStream}, or reading/closing the
     *             content-stream result in an {@link IOException}.
     */
    private void writeJarEntry(JarOutputStream os, InputStream content, String entryName, String fileName) {
        BufferedInputStream in = null;
        BufferedOutputStream out = null;
        try {
            in = new BufferedInputStream(content);
            out = new BufferedOutputStream(os);

            JarEntry newEntry = new JarEntry(entryName);
            os.putNextEntry(newEntry);

            int nextByte;
            while ((nextByte = in.read()) != -1) {
                out.write(nextByte);
            }
        } catch (IOException e) {
            throw new IpsException(new IpsStatus("Error writing archive entry for file " + fileName, e)); //$NON-NLS-1$
        } finally {
            IoUtil.close(in);
            try {
                // do not close the BufferedOutputStream, as that would also close the
                // JarOutputStream, which should only be closed after all entries have been written
                if (out != null) {
                    out.flush();
                }
            } catch (IOException e) {
                throw new IpsException(new IpsStatus("Unable to flush steam.", e)); //$NON-NLS-1$
            }
        }
    }

    /**
     * If the given {@link IIpsSrcFile} is an {@link IProductCmptType} its custom icon (if existent)
     * is added to the archive as well.
     *
     * @param file the {@link IIpsSrcFile} to process.
     * @param os the {@link JarOutputStream} to write the custom Icon to.
     * @throws IpsException if writing the {@link JarEntry} encounters problems.
     */
    private void writeCustomIconIfApplicable(IIpsSrcFile file, JarOutputStream os) {
        if (file.getIpsObjectType() == IpsObjectType.PRODUCT_CMPT_TYPE) {
            String iconPath = file.getPropertyValue(IProductCmptType.PROPERTY_ICON_FOR_INSTANCES);
            if (IpsStringUtils.isNotEmpty(iconPath)) {
                if (isDuplicateEntry(iconPath)) {
                    return;
                }
                InputStream iconStream = file.getIpsProject().getResourceAsStream(iconPath);
                if (iconStream != null) {
                    writeJarEntry(os, iconStream, iconPath, iconPath);
                }
            }
        }
    }

    private void createIpsObjectsPropertiesEntry(JarOutputStream os, Properties ipsObjectsProperties) {

        JarEntry newEntry = new JarEntry(IIpsArchive.JAVA_MAPPING_ENTRY_NAME);
        try {
            os.putNextEntry(newEntry);
            ipsObjectsProperties.store(os, null);
        } catch (IOException e) {
            throw new IpsException(new IpsStatus("Error creating entry ipsobjects.properties", e)); //$NON-NLS-1$
        }
    }

    private void addJavaFiles(IIpsPackageFragmentRoot root, JarOutputStream os, IProgressMonitor monitor) {
        APackageFragmentRoot javaRoot = root.getArtefactDestination(false);
        if (javaRoot == null) {
            throw new IpsException(
                    new IpsStatus("Can't find file Java root for IPS root " + root.getName())); //$NON-NLS-1$
        }
        if (inclJavaBinaries) {
            Path path = javaRoot.getOutputLocation();
            if (path == null) {
                AJavaProject javaProject = javaRoot.getJavaProject();
                if (javaProject != null) {
                    path = javaProject.getOutputLocation();
                }
            }
            if (path != null) {
                AFolder outFolder = Abstractions.getWorkspace().getRoot().getFolder(path);
                if (!handledRootFolders.contains(outFolder)) {
                    addFiles(outFolder, outFolder, os, monitor);
                    handledRootFolders.add(outFolder);
                }
            }
        }
        // Java sourcen
        if (inclJavaSources) {
            Path path = javaRoot.getPath();
            AFolder srcFolder = Abstractions.getWorkspace().getRoot().getFolder(path);
            if (handledRootFolders.contains(srcFolder)) {
                return;
            }
            addFiles(srcFolder, srcFolder, os, monitor);
            handledRootFolders.add(srcFolder);
        }
    }

    private void addFiles(AFolder rootFolder, AFolder folder, JarOutputStream os, IProgressMonitor monitor) {
        for (AResource member : folder) {
            if (member instanceof AFile file) {
                addFiles(rootFolder, file, os);
            } else if (member instanceof AFolder childFolder) {
                addFiles(rootFolder, childFolder, os, monitor);
            }
        }
    }

    private void addFiles(AFolder rootFolder, AFile fileToAdd, JarOutputStream os) {
        String name = PathUtil.toPortableString(
                rootFolder.getWorkspaceRelativePath().relativize(fileToAdd.getWorkspaceRelativePath()));
        if (isDuplicateEntry(name)) {
            return;
        }
        JarEntry newEntry = new JarEntry(name);
        try {
            os.putNextEntry(newEntry);
            byte[] contents = getContent(fileToAdd.getContents());
            os.write(contents);
        } catch (IOException e) {
            throw new IpsException(new IpsStatus("Error creating entry ipsobjects.properties", e)); //$NON-NLS-1$
        }
    }

    private byte[] getContent(InputStream contents) {
        try {
            byte[] content = new byte[contents.available()];
            contents.read(content);
            contents.close();
            return content;
        } catch (IOException e) {
            throw new IpsException(new IpsStatus(e));
        }
    }

    private int getWorkload() {
        int load = 1;
        for (int i = 0; i < roots.length; i++) {
            load = +roots[i].getIpsPackageFragments().length;
        }
        return load;
    }

    public boolean isInclJavaBinaries() {
        return inclJavaBinaries;
    }

    public void setInclJavaBinaries(boolean inclJavaBinaries) {
        this.inclJavaBinaries = inclJavaBinaries;
    }

    public boolean isInclJavaSources() {
        return inclJavaSources;
    }

    public void setInclJavaSources(boolean inclJavaSources) {
        this.inclJavaSources = inclJavaSources;
    }

}
