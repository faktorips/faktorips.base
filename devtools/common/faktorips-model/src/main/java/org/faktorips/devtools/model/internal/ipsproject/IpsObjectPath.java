/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsproject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.Abstractions;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.abstraction.util.PathUtil;
import org.faktorips.devtools.model.internal.ipsproject.search.AbstractSearch;
import org.faktorips.devtools.model.internal.ipsproject.search.CycleSearch;
import org.faktorips.devtools.model.internal.ipsproject.search.DuplicateIpsSrcFileSearch;
import org.faktorips.devtools.model.internal.ipsproject.search.IpsSrcFileSearch;
import org.faktorips.devtools.model.internal.ipsproject.search.IpsSrcFilesSearch;
import org.faktorips.devtools.model.internal.ipsproject.search.IpsSrcFilesSearchInSrcFolder;
import org.faktorips.devtools.model.internal.ipsproject.search.ProjectSearch;
import org.faktorips.devtools.model.internal.ipsproject.search.ResourceSearch;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsArchive;
import org.faktorips.devtools.model.ipsproject.IIpsArchiveEntry;
import org.faktorips.devtools.model.ipsproject.IIpsContainerEntry;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectRefEntry;
import org.faktorips.devtools.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.util.ArrayElementMover;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.util.ArgumentCheck;

/**
 * Implementation of IIpsObjectPath.
 * 
 * @author Jan Ortmann
 */
public class IpsObjectPath implements IIpsObjectPath {

    private IIpsObjectPathEntry[] entries = {};

    private boolean outputDefinedPerSourceFolder = false;

    /** output folder for the generated Java files */
    private AFolder outputFolderMergableSources;

    /** base package for the generated Java files */
    private String basePackageMergable = ""; //$NON-NLS-1$

    /**
     * output folder for generated sources that are marked as derived, more precise this output
     * folder will be marked as derived and hence all members of it will be derived resources.
     * Derived resources will not be managed by the resource management system and will use the
     * output folder and base package for the extension Java files
     */
    private AFolder outputFolderDerivedSources;

    private String basePackageDerived = ""; //$NON-NLS-1$

    private IIpsProject ipsProject;

    /** map with QualifiedNameTypes as keys and cached IpsSrcFiles as values. */
    private Map<QualifiedNameType, IIpsSrcFile> lookupCache = new HashMap<>(1000);

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
        List<IIpsProjectRefEntry> projectRefEntries = new ArrayList<>();
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
        List<IIpsSrcFolderEntry> srcEntries = new ArrayList<>();
        for (IIpsObjectPathEntry entry : entries) {
            if (isSrcFolderEntry(entry)) {
                srcEntries.add((IIpsSrcFolderEntry)entry);
            }
        }
        return srcEntries.toArray(new IIpsSrcFolderEntry[srcEntries.size()]);
    }

    @Override
    public IIpsArchiveEntry[] getArchiveEntries() {
        List<IIpsArchiveEntry> archiveEntries = new ArrayList<>();
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
    public IIpsSrcFolderEntry newSourceFolderEntry(AFolder srcFolder) {
        IIpsSrcFolderEntry newEntry = new IpsSrcFolderEntry(this, srcFolder);
        addEntry(newEntry);
        return newEntry;
    }

    @Override
    public IIpsArchiveEntry newArchiveEntry(Path archivePath) {
        Path correctArchivePath = archivePath;

        if (archivePath.getNameCount() >= 2
                && PathUtil.segment(archivePath, 0).equals(getIpsProject().getName())) {
            // Path should be project relative
            AFile archiveFile = Abstractions.getWorkspace().getRoot().getFile(archivePath);
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
            throw new IpsException(new IpsStatus(e));
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
    public boolean containsSrcFolderEntry(AFolder folder) {
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
    public void removeSrcFolderEntry(AFolder srcFolder) {
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
    public AFolder getOutputFolderForMergableSources() {
        return outputFolderMergableSources;
    }

    @Override
    public void setOutputFolderForMergableSources(AFolder outputFolder) {
        outputFolderMergableSources = outputFolder;
    }

    @Override
    public AFolder[] getOutputFolders() {
        if (!outputDefinedPerSourceFolder) {
            if (outputFolderMergableSources == null) {
                return new AFolder[0];
            } else {
                return new AFolder[] { outputFolderMergableSources };
            }
        }

        List<AFolder> result = new ArrayList<>(entries.length);
        for (IIpsObjectPathEntry entrie : entries) {
            if (IIpsObjectPathEntry.TYPE_SRC_FOLDER.equals(entrie.getType())) {
                IIpsSrcFolderEntry srcEntry = (IIpsSrcFolderEntry)entrie;
                if (srcEntry.getOutputFolderForMergableJavaFiles() != null) {
                    result.add(srcEntry.getOutputFolderForMergableJavaFiles());
                }
            }
        }
        return result.toArray(new AFolder[result.size()]);
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
    public AFolder getOutputFolderForDerivedSources() {
        return outputFolderDerivedSources;
    }

    @Override
    public void setOutputFolderForDerivedSources(AFolder outputFolder) {
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
     * Returns the first IPS source file with the indicated qualified name type found on the path.
     * Returns <code>null</code> if no such object is found.
     */
    @Override
    public IIpsSrcFile findIpsSrcFile(QualifiedNameType nameType) {
        IIpsSrcFile cachedSrcFile = lookupCache.get(nameType);
        if (cachedSrcFile != null) {
            if (!cachedSrcFile.exists()) {
                lookupCache.remove(nameType);
            } else {
                return cachedSrcFile;
            }
        }
        IpsSrcFileSearch search = new IpsSrcFileSearch(nameType);
        searchIpsObjectPath(search);
        IIpsSrcFile ipsSrcFile = search.getIpsSrcFile();
        if (ipsSrcFile != null) {
            lookupCache.put(nameType, ipsSrcFile);
            return ipsSrcFile;
        }
        return null;
    }

    @Override
    public boolean findDuplicateIpsSrcFile(QualifiedNameType nameType) {
        DuplicateIpsSrcFileSearch search = new DuplicateIpsSrcFileSearch(nameType);
        searchIpsObjectPath(search);
        return search.foundDuplicateIpsSrcFile();
    }

    /**
     * Adds all source files found in <code>IpsSrcFolderEntry</code>s on the path to the result
     * list.
     */
    public void collectAllIpsSrcFilesOfSrcFolderEntries(List<IIpsSrcFile> result) {
        result.addAll(findIpsSrcFilesInSrcFolder());
    }

    @Override
    public MessageList validate() {
        MessageList list = new MessageList();
        if (!isOutputDefinedPerSrcFolder()) {
            if (outputFolderMergableSources == null) {
                list.add(new Message(MSGCODE_MERGABLE_OUTPUT_FOLDER_NOT_SPECIFIED, MessageFormat.format(
                        Messages.IpsObjectPath_msgOutputFolderMergableMissing, getIpsProject()), Message.ERROR, this));
            } else {
                list.add(validateFolder(outputFolderMergableSources));
            }
            if (outputFolderDerivedSources == null) {
                list.add(new Message(MSGCODE_DERIVED_OUTPUT_FOLDER_NOT_SPECIFIED, MessageFormat.format(
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
    private MessageList validateFolder(AFolder folder) {
        MessageList result = new MessageList();
        if (!folder.exists()) {
            String text = MessageFormat.format(Messages.IpsSrcFolderEntry_msgMissingFolder, folder.getName());
            Message msg = new Message(IIpsObjectPathEntry.MSGCODE_MISSING_FOLDER, text, Message.ERROR, this);
            result.add(msg);
        }
        return result;
    }

    /**
     * Checks if there is a cycle inside the object path. Considers project references transitively.
     * <p>
     * Returns <code>true</code> if a cycle was detected. Returns <code>false</code> if there is no
     * cycle in this IPS object path.
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
        return resourceSearch.getResourceAsStream();
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
                if (Objects.equals(containerEntry.getContainerTypeId(), containerTypeId)
                        && Objects.equals(containerEntry.getOptionalPath(), optionalPath)) {
                    return containerEntry;
                }
            }
        }
        return null;
    }

    private List<IIpsSrcFile> findIpsSrcFilesInSrcFolder(IpsObjectType... ipsObjectType) {
        IpsSrcFilesSearch search = new IpsSrcFilesSearchInSrcFolder(ipsObjectType);
        searchIpsObjectPath(search);
        return search.getIpsSrcFiles();
    }

    @Override
    public List<IIpsSrcFile> findIpsSrcFiles(IpsObjectType... ipsObjectType) {
        IpsSrcFilesSearch search = new IpsSrcFilesSearch(ipsObjectType);
        searchIpsObjectPath(search);
        return search.getIpsSrcFiles();
    }

    /**
     * Returns all accessible {@link IIpsProject IPS projects} referenced in this
     * {@link IpsObjectPath}. If <code>includeIndirect</code> is <code>true</code> all referenced
     * {@link IIpsProject IPS projects} will be added to the resulting list (references will be
     * considered transitively). If <code>includeIndirect</code> is <code>false</code> only the
     * directly referenced {@link IIpsProject IPS projects} will be included in the resulting list.
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
     * Searches all {@link IIpsObjectPathEntry}s for their appropriateness and saves them in
     * {@link AbstractSearch}.
     */
    private void searchIpsObjectPath(AbstractSearch search) {
        searchIpsObjectPath(search, new IpsObjectPathSearchContext(getIpsProject()));
    }

    private void searchIpsObjectPath(AbstractSearch search, IpsObjectPathSearchContext searchContext) {
        for (IIpsObjectPathEntry entry : getEntries()) {
            searchEntry(search, searchContext, entry);
            if (search.isStopSearch()) {
                break;
            }
        }
    }

    private void searchEntry(AbstractSearch search,
            IpsObjectPathSearchContext searchContext,
            IIpsObjectPathEntry entry) {
        if (searchContext.visitAndConsiderContentsOf(entry)) {
            if (entry.isContainer()) {
                searchContainerIpsObjectPath(search, searchContext, entry);
            } else {
                search.processEntry(entry);
                if (!search.isStopSearch()) {
                    searchReferencedProject(search, searchContext, entry);
                }
            }
        }
    }

    private void searchContainerIpsObjectPath(AbstractSearch search,
            IpsObjectPathSearchContext searchContext,
            IIpsObjectPathEntry entry) {
        List<IIpsObjectPathEntry> childEntries = ((IIpsContainerEntry)entry).resolveEntries();
        for (IIpsObjectPathEntry childEntry : childEntries) {
            searchEntry(search, searchContext, childEntry);
            if (search.isStopSearch()) {
                break;
            }
        }
    }

    /**
     * Does nothing if the referenced project is <code>null</code>.
     */
    private void searchReferencedProject(AbstractSearch search,
            IpsObjectPathSearchContext searchContext,
            IIpsObjectPathEntry entry) {
        if (search.isIncludeIndirect() && isProjectRefEntry(entry)) {
            IIpsProject referencedIpsProject = search.getReferencedIpsProject(entry);
            if (referencedIpsProject != null) {
                ((IpsProject)referencedIpsProject).getIpsObjectPathInternal()
                        .searchIpsObjectPath(search, searchContext);
            }
        }
    }

    private boolean isProjectRefEntry(IIpsObjectPathEntry entry) {
        return entry.getType().equals(IIpsObjectPathEntry.TYPE_PROJECT_REFERENCE);
    }

    private boolean isSrcFolderEntry(IIpsObjectPathEntry entry) {
        return entry.getType().equals(IIpsObjectPathEntry.TYPE_SRC_FOLDER);
    }
}
