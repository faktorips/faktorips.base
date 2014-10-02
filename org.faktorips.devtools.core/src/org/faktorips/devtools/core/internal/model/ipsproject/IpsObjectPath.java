/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArchive;
import org.faktorips.devtools.core.model.ipsproject.IIpsArchiveEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsContainerEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectRefEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.core.util.ArrayElementMover;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * Implementation of IIpsObjectPath.
 * 
 * @author Jan Ortmann
 */
public class IpsObjectPath implements IIpsObjectPath {

    private IIpsObjectPathEntry[] entries = new IIpsObjectPathEntry[0];

    private boolean outputDefinedPerSourceFolder = false;

    /** output folder for the generated Java files */
    private IFolder outputFolderMergableSources;

    /** base package for the generated Java files */
    private String basePackageMergable = ""; //$NON-NLS-1$

    /**
     * output folder for generated sources that are marked as derived, more precise this output
     * folder will be marked as derived and hence all members of it will be derived resources.
     * Derived resources will not be managed by the resource management system and will use the
     * output folder and base package for the extension Java files
     */
    private IFolder outputFolderDerivedSources;

    private String basePackageDerived = ""; //$NON-NLS-1$

    private IIpsProject ipsProject;

    /** map with QualifiedNameTypes as keys and cached IpsSrcFiles as values. */
    private Map<QualifiedNameType, CachedSrcFile> lookupCache = new HashMap<QualifiedNameType, CachedSrcFile>(1000);

    /**
     * if set to true, the {@link IIpsObjectPathEntry entries} are read from the manifest.mf and if
     * false the entries are read from the .ipsproject file
     */
    private boolean useManifest = false;

    public IpsObjectPath(IIpsProject ipsProject) {
        ArgumentCheck.notNull(ipsProject, this);
        this.ipsProject = ipsProject;
    }

    @Override
    public IIpsProject getIpsProject() {
        return ipsProject;
    }

    /**
     * Returns the index of the given entry.
     */
    public int getIndex(IIpsObjectPathEntry entry) {
        return getIndexIn(entry, Arrays.asList(entries));
    }

    private int getIndexIn(IIpsObjectPathEntry entry, List<IIpsObjectPathEntry> myEntries) {
        int i = 0;
        for (IIpsObjectPathEntry anEntry : myEntries) {
            if (anEntry.equals(entry)) {
                return i;
            }
            i++;
            if (anEntry instanceof IIpsContainerEntry) {
                List<IIpsObjectPathEntry> resolvedEntries = ((IIpsContainerEntry)anEntry).resolveEntries();
                int indexInContainer = getIndexIn(entry, resolvedEntries);
                if (indexInContainer > -1) {
                    return i + indexInContainer;
                } else {
                    i += resolvedEntries.size();
                }
            }
        }
        return -1;
    }

    @Override
    public IIpsProjectRefEntry[] getProjectRefEntries() {
        List<IIpsProjectRefEntry> projectRefEntries = collectProjectRefEntries(entries);
        return projectRefEntries.toArray(new IIpsProjectRefEntry[projectRefEntries.size()]);
    }

    private List<IIpsProjectRefEntry> collectProjectRefEntries(IIpsObjectPathEntry[] objectPathEntries) {
        List<IIpsProjectRefEntry> projectRefEntries = new ArrayList<IIpsProjectRefEntry>();
        for (IIpsObjectPathEntry entry : objectPathEntries) {
            if (isProjectRefEntry(entry)) {
                projectRefEntries.add((IIpsProjectRefEntry)entry);
            } else if (entry.isContainer()) {
                List<IIpsObjectPathEntry> resolveEntries = ((IIpsContainerEntry)entry).resolveEntries();
                projectRefEntries.addAll(collectProjectRefEntries(resolveEntries
                        .toArray(new IpsObjectPathEntry[resolveEntries.size()])));
            }
        }
        return projectRefEntries;
    }

    @Override
    public IIpsSrcFolderEntry[] getSourceFolderEntries() {
        List<IIpsSrcFolderEntry> srcEntries = new ArrayList<IIpsSrcFolderEntry>();
        for (IIpsObjectPathEntry entry : entries) {
            if (isSrcFolderEntry(entry)) {
                srcEntries.add((IIpsSrcFolderEntry)entry);
            }
        }
        return srcEntries.toArray(new IIpsSrcFolderEntry[srcEntries.size()]);
    }

    @Override
    public IIpsArchiveEntry[] getArchiveEntries() {
        List<IIpsArchiveEntry> archiveEntries = new ArrayList<IIpsArchiveEntry>();
        for (IIpsObjectPathEntry entrie : entries) {
            if (entrie.getType().equals(IIpsObjectPathEntry.TYPE_ARCHIVE)) {
                archiveEntries.add((IIpsArchiveEntry)entrie);
            }
        }
        return archiveEntries.toArray(new IIpsArchiveEntry[archiveEntries.size()]);
    }

    @Override
    public IIpsObjectPathEntry getEntry(String rootName) {
        if (rootName == null) {
            return null;
        }
        for (IIpsObjectPathEntry entry : entries) {
            if (entry.isContainer()) {
                IIpsContainerEntry containerEntry = (IIpsContainerEntry)entry;
                IIpsObjectPathEntry resolvedEntry = containerEntry.getResolvedEntry(rootName);
                if (resolvedEntry != null) {
                    return resolvedEntry;
                }
            } else {
                if (rootName.equals(entry.getIpsPackageFragmentRootName())) {
                    return entry;
                }
            }
        }
        return null;
    }

    @Override
    public IIpsObjectPathEntry[] getEntries() {
        IIpsObjectPathEntry[] copy = new IIpsObjectPathEntry[entries.length];
        System.arraycopy(entries, 0, copy, 0, entries.length);
        return copy;
    }

    @Override
    public void setEntries(IIpsObjectPathEntry[] newEntries) {
        entries = new IIpsObjectPathEntry[newEntries.length];
        System.arraycopy(newEntries, 0, entries, 0, newEntries.length);
    }

    private void addEntry(IIpsObjectPathEntry newEntry) {
        IIpsObjectPathEntry[] newEntries = new IIpsObjectPathEntry[entries.length + 1];
        System.arraycopy(entries, 0, newEntries, 0, entries.length);
        newEntries[newEntries.length - 1] = newEntry;
        entries = newEntries;
    }

    private void removeEntry(int i) {
        IIpsObjectPathEntry[] newEntries = new IIpsObjectPathEntry[entries.length - 1];
        System.arraycopy(entries, 0, newEntries, 0, i);
        System.arraycopy(entries, i + 1, newEntries, i, entries.length - i - 1);
        entries = newEntries;
    }

    @Override
    public IIpsSrcFolderEntry newSourceFolderEntry(IFolder srcFolder) {
        IIpsSrcFolderEntry newEntry = new IpsSrcFolderEntry(this, srcFolder);
        addEntry(newEntry);
        return newEntry;
    }

    @Override
    public IIpsArchiveEntry newArchiveEntry(IPath archivePath) throws CoreException {
        IPath correctArchivePath = archivePath;

        if (archivePath.segmentCount() >= 2 && archivePath.segment(0).equals(getIpsProject().getName())) {
            // Path should be project relative
            IFile archiveFile = ResourcesPlugin.getWorkspace().getRoot().getFile(archivePath);
            correctArchivePath = archiveFile.getProjectRelativePath();
        }

        for (IIpsArchiveEntry archiveEntry : getArchiveEntries()) {
            if (archiveEntry.getArchiveLocation().equals(correctArchivePath)) {
                // entry already exists.
                return archiveEntry;
            }
        }

        IIpsArchiveEntry newEntry = new IpsArchiveEntry(this);
        try {
            newEntry.initStorage(correctArchivePath);
        } catch (IOException e) {
            throw new CoreException(new IpsStatus(e));
        }
        addEntry(newEntry);
        return newEntry;
    }

    @Override
    public IIpsProjectRefEntry newIpsProjectRefEntry(IIpsProject referencedIpsProject) {
        if (containsProjectRefEntry(referencedIpsProject)) {
            for (IIpsObjectPathEntry entry : entries) {
                if (entry instanceof IpsProjectRefEntry) {
                    IpsProjectRefEntry ref = (IpsProjectRefEntry)entry;
                    if (ref.getReferencedIpsProject().equals(referencedIpsProject)) {
                        return ref;
                    }
                }
            }
        }
        IIpsProjectRefEntry newEntry = new IpsProjectRefEntry(this, referencedIpsProject);
        addEntry(newEntry);
        return newEntry;
    }

    @Override
    public boolean containsProjectRefEntry(IIpsProject ipsProject) {
        for (IIpsObjectPathEntry entry : entries) {
            if (entry instanceof IpsProjectRefEntry) {
                IpsProjectRefEntry ref = (IpsProjectRefEntry)entry;
                if (ref.getReferencedIpsProject().equals(ipsProject)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void removeProjectRefEntry(IIpsProject ipsProject) {
        for (int i = 0; i < entries.length; i++) {
            IIpsObjectPathEntry entry = entries[i];
            if (entry instanceof IpsProjectRefEntry) {
                IpsProjectRefEntry ref = (IpsProjectRefEntry)entry;
                if (ref.getReferencedIpsProject().equals(ipsProject)) {
                    removeEntry(i);
                    return;
                }
            }
        }
    }

    @Override
    public boolean containsArchiveEntry(IIpsArchive ipsArchive) {
        for (IIpsObjectPathEntry entry : entries) {
            if (entry instanceof IpsArchiveEntry) {
                IpsArchiveEntry ref = (IpsArchiveEntry)entry;
                if (ref.getIpsArchive().equals(ipsArchive)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void removeArchiveEntry(IIpsArchive ipsArchive) {
        for (int i = 0; i < entries.length; i++) {
            IIpsObjectPathEntry entry = entries[i];
            if (entry instanceof IpsArchiveEntry) {
                IpsArchiveEntry archiveEntry = (IpsArchiveEntry)entry;
                if (archiveEntry.getIpsArchive().equals(ipsArchive)) {
                    removeEntry(i);
                    return;
                }
            }
        }
    }

    @Override
    public boolean containsSrcFolderEntry(IFolder folder) {
        for (IIpsObjectPathEntry entry : entries) {
            if (entry instanceof IpsSrcFolderEntry) {
                IpsSrcFolderEntry ref = (IpsSrcFolderEntry)entry;
                if (ref.getSourceFolder().equals(folder)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void removeSrcFolderEntry(IFolder srcFolder) {
        for (int i = 0; i < entries.length; i++) {
            IIpsObjectPathEntry entry = entries[i];
            if (entry instanceof IpsSrcFolderEntry) {
                IpsSrcFolderEntry srcFolderEntry = (IpsSrcFolderEntry)entry;
                if (srcFolderEntry.getSourceFolder().equals(srcFolder)) {
                    removeEntry(i);
                    return;
                }
            }
        }
    }

    @Override
    public boolean isOutputDefinedPerSrcFolder() {
        return outputDefinedPerSourceFolder;
    }

    @Override
    public void setOutputDefinedPerSrcFolder(boolean newValue) {
        outputDefinedPerSourceFolder = newValue;
    }

    @Override
    public IFolder getOutputFolderForMergableSources() {
        return outputFolderMergableSources;
    }

    @Override
    public void setOutputFolderForMergableSources(IFolder outputFolder) {
        outputFolderMergableSources = outputFolder;
    }

    @Override
    public IFolder[] getOutputFolders() {
        if (!outputDefinedPerSourceFolder) {
            if (outputFolderMergableSources == null) {
                return new IFolder[0];
            } else {
                return new IFolder[] { outputFolderMergableSources };
            }
        }

        List<IFolder> result = new ArrayList<IFolder>(entries.length);
        for (IIpsObjectPathEntry entrie : entries) {
            if (entrie.getType() == IIpsObjectPathEntry.TYPE_SRC_FOLDER) {
                IIpsSrcFolderEntry srcEntry = (IIpsSrcFolderEntry)entrie;
                if (srcEntry.getOutputFolderForMergableJavaFiles() != null) {
                    result.add(srcEntry.getOutputFolderForMergableJavaFiles());
                }
            }
        }
        return result.toArray(new IFolder[result.size()]);
    }

    @Override
    public String getBasePackageNameForMergableJavaClasses() {
        return basePackageMergable;
    }

    @Override
    public void setBasePackageNameForMergableJavaClasses(String name) {
        basePackageMergable = name;
    }

    @Override
    public IFolder getOutputFolderForDerivedSources() {
        return outputFolderDerivedSources;
    }

    @Override
    public void setOutputFolderForDerivedSources(IFolder outputFolder) {
        outputFolderDerivedSources = outputFolder;
    }

    @Override
    public String getBasePackageNameForDerivedJavaClasses() {
        return basePackageDerived;
    }

    @Override
    public void setBasePackageNameForDerivedJavaClasses(String name) {
        basePackageDerived = name;
    }

    /**
     * Returns the first ips source file with the indicated qualified name type found on the path.
     * Returns <code>null</code> if no such object is found.
     */
    public IIpsSrcFile findIpsSrcFile(QualifiedNameType nameType, IpsObjectPathSearchContext searchContext) {

        int maxEntriesToSearch = entries.length;
        CachedSrcFile cachedSrcFile = lookupCache.get(nameType);
        if (cachedSrcFile != null) {
            if (!cachedSrcFile.file.exists()) {
                lookupCache.remove(nameType);
                cachedSrcFile = null;
            } else {
                if (cachedSrcFile.entryIndex == 0) {
                    /*
                     * if the file was found via the first entry, it is not possible that a file
                     * with the same name has been added to another entry that now shadows the found
                     * file.
                     */
                    return cachedSrcFile.file;
                } else {
                    maxEntriesToSearch = cachedSrcFile.entryIndex;
                }
            }
        }
        for (int i = 0; i < maxEntriesToSearch; i++) {
            IIpsSrcFile ipsSrcFile = ((IpsObjectPathEntry)entries[i]).findIpsSrcFile(nameType, searchContext);
            if (ipsSrcFile != null) {
                lookupCache.put(nameType, new CachedSrcFile(ipsSrcFile, i));
                return ipsSrcFile;
            }
        }
        return cachedSrcFile == null ? null : cachedSrcFile.file;
    }

    @Override
    public IIpsSrcFile findIpsSrcFile(QualifiedNameType nameType) {
        return findIpsSrcFile(nameType, new IpsObjectPathSearchContext(getIpsProject()));
    }

    /**
     * Adds all source files found in <code>IpsSrcFolderEntry</code>s on the path to the result
     * list.
     */
    public void collectAllIpsSrcFilesOfSrcFolderEntries(List<IIpsSrcFile> result) {
        for (IIpsObjectPathEntry entry : entries) {
            if (isSrcFolderEntry(entry)) {
                for (IpsObjectType currentType : IpsPlugin.getDefault().getIpsModel().getIpsObjectTypes()) {
                    result.addAll(entry.findIpsSrcFiles(currentType, new IpsObjectPathSearchContext(getIpsProject())));
                }
            }
        }
    }

    @Override
    public MessageList validate() throws CoreException {
        MessageList list = new MessageList();
        if (!isOutputDefinedPerSrcFolder()) {
            if (outputFolderMergableSources == null) {
                list.add(new Message(MSGCODE_MERGABLE_OUTPUT_FOLDER_NOT_SPECIFIED, NLS.bind(
                        Messages.IpsObjectPath_msgOutputFolderMergableMissing, getIpsProject()), Message.ERROR, this));
            } else {
                list.add(validateFolder(outputFolderMergableSources));
            }
            if (outputFolderDerivedSources == null) {
                list.add(new Message(MSGCODE_DERIVED_OUTPUT_FOLDER_NOT_SPECIFIED, NLS.bind(
                        Messages.IpsObjectPath_msgOutputFolderDerivedMissing, getIpsProject()), Message.ERROR, this));
            } else {
                list.add(validateFolder(outputFolderDerivedSources));
            }
        }
        IIpsSrcFolderEntry[] srcEntries = getSourceFolderEntries();
        if (srcEntries.length == 0) {
            list.add(new Message(MSGCODE_SRC_FOLDER_ENTRY_MISSING, Messages.IpsObjectPath_srcfolderentrymissing,
                    Message.ERROR, this));
        }
        IIpsObjectPathEntry[] objectPathEntries = getEntries();
        for (IIpsObjectPathEntry objectPathEntrie : objectPathEntries) {
            MessageList ml = objectPathEntrie.validate();
            list.add(ml);
        }

        return list;
    }

    /**
     * Validate that the given folder exists.
     */
    private MessageList validateFolder(IFolder folder) {
        MessageList result = new MessageList();
        if (!folder.exists()) {
            String text = NLS.bind(Messages.IpsSrcFolderEntry_msgMissingFolder, folder.getName());
            Message msg = new Message(IIpsObjectPathEntry.MSGCODE_MISSING_FOLDER, text, Message.ERROR, this);
            result.add(msg);
        }
        return result;
    }

    /**
     * Checks if there is a cycle inside the object path. Considers project references transitively.
     * <p>
     * Returns <code>true</code> if a cycle was detected. Returns <code>false</code> if there is no
     * cycle in this ips object path.
     */
    public boolean detectCycle() {
        CycleSearch cycleSearch = new CycleSearch(getIpsProject());
        searchIpsObjectPath(cycleSearch);
        return cycleSearch.isCycleDetected();
    }

    @Override
    public int[] moveEntries(int[] indices, boolean up) {
        ArgumentCheck.notNull(indices, this);

        ArrayElementMover mover = new ArrayElementMover(entries);

        int[] newSelection;
        if (up) {
            newSelection = mover.moveUp(indices);
        } else {
            newSelection = mover.moveDown(indices);
        }

        return newSelection;
    }

    @Override
    public boolean containsResource(String path) {
        ResourceSearch resourceSearch = new ResourceSearch(path);
        searchIpsObjectPath(resourceSearch);
        return resourceSearch.containsResource();
    }

    @Override
    public InputStream getResourceAsStream(String path) {
        ResourceSearch resourceSearch = new ResourceSearch(path);
        searchIpsObjectPath(resourceSearch);
        if (resourceSearch.containsResource()) {
            return resourceSearch.getResource().getResourceAsStream(path);
        }
        return null;
    }

    @Override
    public boolean isUsingManifest() {
        return useManifest;
    }

    @Override
    public void setUsingManifest(boolean useManifest) {
        this.useManifest = useManifest;
    }

    @Override
    public IIpsContainerEntry newContainerEntry(String containerTypeId, String optionalPath) {
        IIpsContainerEntry existingContainer = findExistingContainer(containerTypeId, optionalPath);
        if (existingContainer != null) {
            return existingContainer;
        }
        IpsContainerEntry ipsContainerEntry = new IpsContainerEntry(this);
        ipsContainerEntry.setContainerTypeId(containerTypeId);
        ipsContainerEntry.setOptionalPath(optionalPath);
        addEntry(ipsContainerEntry);
        return ipsContainerEntry;
    }

    /* private */IIpsContainerEntry findExistingContainer(String containerTypeId, String optionalPath) {
        for (IIpsObjectPathEntry ipsObjectPathEntry : entries) {
            if (ipsObjectPathEntry.isContainer()) {
                IIpsContainerEntry containerEntry = (IIpsContainerEntry)ipsObjectPathEntry;
                if (StringUtils.equals(containerEntry.getContainerTypeId(), containerTypeId)
                        && StringUtils.equals(containerEntry.getOptionalPath(), optionalPath)) {
                    return containerEntry;
                }
            }
        }
        return null;
    }

    @Override
    public List<IIpsSrcFile> findIpsSrcFiles(IpsObjectType ipsObjectType) {
        return findIpsSrcFiles(ipsObjectType, new IpsObjectPathSearchContext(getIpsProject()));
    }

    /**
     * finds all {@link IIpsSrcFile}s with the indicated {@link IpsObjectType}.
     */
    public List<IIpsSrcFile> findIpsSrcFiles(IpsObjectType ipsObjectType, IpsObjectPathSearchContext searchContext) {
        List<IIpsSrcFile> result = new ArrayList<IIpsSrcFile>();
        for (IIpsObjectPathEntry entry : entries) {
            result.addAll(entry.findIpsSrcFiles(ipsObjectType, searchContext));
        }
        return result;
    }

    /**
     * Returns all accessible {@link IpsProject IPS projects} referenced in this
     * {@link IpsObjectPath}. If <code>includeIndirect</code> is <code>true</code> all referenced
     * {@link IpsProject IPS projects} will be added to the resulting list (references will be
     * considered transitively). If <code>includeIndirect</code> is <code>false</code> only the
     * directly referenced {@link IpsProject}s will be included in the resulting list.
     */
    public List<IIpsProject> getReferencedIpsProjects(boolean includeIndirect) {
        ProjectSearch projectSearch = new ProjectSearch();
        projectSearch.setIncludeIndirect(includeIndirect);
        searchIpsObjectPath(projectSearch);
        return projectSearch.getProjects();
    }

    @Override
    public List<IIpsProject> getDirectlyReferencedIpsProjects() {
        return getReferencedIpsProjects(false);
    }

    @Override
    public List<IIpsProject> getAllReferencedIpsProjects() {
        return getReferencedIpsProjects(true);
    }

    /**
     * Searches all {@link IIpsObjectPathEntry}s for their appropriateness and safes them in
     * {@link AbstractSearch}.
     */
    private void searchIpsObjectPath(AbstractSearch search) {
        searchIpsObjectPath(search, new IpsObjectPathSearchContext(getIpsProject()));
    }

    private void searchIpsObjectPath(AbstractSearch search, IpsObjectPathSearchContext searchContext) {
        for (IIpsObjectPathEntry entry : getEntries()) {
            searchEntry(search, searchContext, entry);
            if (entry.isContainer()) {
                List<IIpsObjectPathEntry> childEntries = ((IIpsContainerEntry)entry).resolveEntries();
                for (IIpsObjectPathEntry childEntry : childEntries) {
                    searchEntry(search, searchContext, childEntry);
                }
            }
        }
    }

    private void searchEntry(AbstractSearch search, IpsObjectPathSearchContext searchContext, IIpsObjectPathEntry entry) {
        if (searchContext.visitAndConsiderContentsOf(entry)) {
            search.processEntry(entry);
            searchReferencedProject(search, searchContext, entry);
        }
    }

    private void searchReferencedProject(AbstractSearch search,
            IpsObjectPathSearchContext searchContext,
            IIpsObjectPathEntry entry) {
        if (search.isIncludeIndirect() && isProjectRefEntry(entry)) {
            IIpsProject referencedIpsProject = search.getReferencedIpsProject(entry);
            ((IpsProject)referencedIpsProject).getIpsObjectPathInternal().searchIpsObjectPath(search, searchContext);
        }
    }

    private boolean isProjectRefEntry(IIpsObjectPathEntry entry) {
        return entry.getType().equals(IIpsObjectPathEntry.TYPE_PROJECT_REFERENCE);
    }

    private boolean isSrcFolderEntry(IIpsObjectPathEntry entrie) {
        return entrie.getType().equals(IIpsObjectPathEntry.TYPE_SRC_FOLDER);
    }

    private static class CachedSrcFile {

        private IIpsSrcFile file;
        private int entryIndex;

        public CachedSrcFile(IIpsSrcFile file, int entryIndex) {
            super();
            this.file = file;
            this.entryIndex = entryIndex;
        }

    }

}
