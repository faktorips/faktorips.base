/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Locale;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.datatype.util.LocalizedStringsSet;
import org.faktorips.devtools.abstraction.ABuildKind;
import org.faktorips.devtools.abstraction.AContainer;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.APackageFragmentRoot;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilder;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;

/**
 * Abstract base class for artefact builders.
 * 
 * @author Jan Ortmann
 */
public abstract class AbstractArtefactBuilder implements IIpsArtefactBuilder {

    private IIpsArtefactBuilderSet builderSet;
    private final LocalizedStringsSet localizedStringsSet;

    public AbstractArtefactBuilder(IIpsArtefactBuilderSet builderSet) {
        this(builderSet, null);
    }

    public AbstractArtefactBuilder(IIpsArtefactBuilderSet builderSet, LocalizedStringsSet localizedStringsSet) {
        this.builderSet = builderSet;
        this.localizedStringsSet = localizedStringsSet;
    }

    @Override
    public IIpsArtefactBuilderSet getBuilderSet() {
        return builderSet;
    }

    public IIpsProject getIpsProject() {
        return builderSet.getIpsProject();
    }

    @Override
    public void beforeBuildProcess(IIpsProject project, ABuildKind buildKind) {
        // default implementation does nothing
    }

    @Override
    public void afterBuildProcess(IIpsProject project, ABuildKind buildKind) {
        // default implementation does nothing
    }

    @Override
    public void beforeBuild(IIpsSrcFile ipsSrcFile, MultiStatus status) {
        // default implementation does nothing
    }

    @Override
    public void afterBuild(IIpsSrcFile ipsSrcFile) {
        // default implementation does nothing
    }

    /**
     * Returns false.
     */
    @Override
    public boolean buildsDerivedArtefacts() {
        return false;
    }

    @Override
    public String toString() {
        return "Builder: " + getName(); //$NON-NLS-1$
    }

    /**
     * This method needs to be used in subclasses of this builder when a file is created during the
     * build cycle. This method creates a file only if the file handle points to a file that doesn't
     * exist. Also the folder hierarchy up to the destination folder specified in the IPS project
     * properties is created if it doesn't exist. The files and folders are marked as derived if
     * this builder builds derived artefacts. See the method <code>buildDerivedArtefacts()</code>.
     * 
     * @param file the file handle
     * 
     * @return true if the file needs to be created, false if the file already exists
     * 
     * @throws IpsException if an Exception occurs during the creation procedure
     * @throws RuntimeException if the provided file parameter is <code>null</code>
     */
    protected boolean createFileIfNotThere(AFile file) {
        ArgumentCheck.notNull(file, this);
        if (!file.exists()) {
            AContainer parent = file.getParent();
            if (parent instanceof AFolder) {
                createFolderIfNotThere((AFolder)parent);
            }
            file.create(new ByteArrayInputStream("".getBytes()), null); //$NON-NLS-1$
            file.setDerived(buildsDerivedArtefacts() && getBuilderSet().isMarkNoneMergableResourcesAsDerived(), null);
            return true;
        }

        return false;
    }

    /**
     * This method needs to be used in subclasses of this builder when a folder is created during
     * the build cycle. This method creates a folder only if the folder handle points to a folder
     * that doesn't exist. Also the folder hierarchy up to the destination folder specified in the
     * IPS project properties is created if it doesn't exist. The folders are marked as derived if
     * this builder builds derived artefacts. See the method <code>buildDerivedArtefacts()</code>.
     * 
     * @param folder the folder handle
     * 
     * @return true if the folder needs to be created, false if the folder already exists
     * 
     * @throws IpsException if an Exception occurs during the creation procedure
     * @throws RuntimeException if the provided folder parameter is <code>null</code>
     */
    protected boolean createFolderIfNotThere(AFolder folder) {

        ArgumentCheck.notNull(folder, this);
        if (!folder.exists()) {
            AContainer parent = folder.getParent();
            if (parent instanceof AFolder) {
                createFolderIfNotThere((AFolder)parent);
            }
            folder.create(null);
            folder.setDerived(buildsDerivedArtefacts() && getBuilderSet().isMarkNoneMergableResourcesAsDerived(), null);
            return true;
        }
        return false;
    }

    /**
     * Returns the language in that variables, methods are named and and Java documentations are
     * written in.
     */
    public final Locale getLanguageUsedInGeneratedSourceCode() {
        return getBuilderSet().getLanguageUsedInGeneratedSourceCode();
    }

    /**
     * Returns the localized text for the provided key. Calling this method is only allowed during
     * the build cycle. If it is called outside the build cycle a RuntimeException is thrown. In
     * addition if no LocalizedStringSet has been set to this builder a RuntimeException is thrown.
     * 
     * @param key the key that identifies the requested text
     */
    public String getLocalizedText(String key) {
        if (localizedStringsSet == null) {
            throw new RuntimeException(
                    "A LocalizedStringSet has to be set to this builder to be able to call this method."); //$NON-NLS-1$
        }
        return getLocalizedStringSet().getString(key, getLanguageUsedInGeneratedSourceCode());
    }

    /**
     * Returns the localized string set of this builder.
     */
    protected LocalizedStringsSet getLocalizedStringSet() {
        return localizedStringsSet;
    }

    /**
     * Returns the localized text for the provided key. Calling this method is only allowed during
     * the build cycle. If it is called outside the build cycle a RuntimeException is thrown. In
     * addition if no LocalizedStringSet has been set to this builder a RuntimeException is thrown.
     * 
     * @param key the key that identifies the requested text
     * @param replacement an indicated region within the text is replaced by the string
     *            representation of this value
     */
    public String getLocalizedText(String key, Object replacement) {
        if (localizedStringsSet == null) {
            throw new RuntimeException(
                    "A LocalizedStringSet has to be set to this builder to be able to call this method."); //$NON-NLS-1$
        }
        return getLocalizedStringSet().getString(key, getLanguageUsedInGeneratedSourceCode(), replacement);
    }

    /**
     * Returns the localized text for the provided key. Calling this method is only allowed during
     * the build cycle. If it is called outside the build cycle a RuntimeException is thrown. In
     * addition if no LocalizedStringSet has been set to this builder a RuntimeException is thrown.
     * 
     * @param key the key that identifies the requested text
     * @param replacements indicated regions within the text are replaced by the string
     *            representations of these values.
     */
    public String getLocalizedText(String key, Object... replacements) {
        if (localizedStringsSet == null) {
            throw new RuntimeException(
                    "A LocalizedStringSet has to be set to this builder to be able to call this method."); //$NON-NLS-1$
        }
        return getLocalizedStringSet().getString(key, getLanguageUsedInGeneratedSourceCode(), replacements);
    }

    /**
     * This methods calls {@link IWorkspace#validateEdit(IFile[], Object)} before writing to the
     * file if the file is marked as read only. This giving the chance to version control system to
     * checkout the file for writing before calling writing.
     * 
     * 
     * @param file The file to write to
     * @param inputStream the content that should be written to the file
     * @param keepHistory setting keeping the history when writing to the file @see
     *            {@link AFile#setContents(InputStream, boolean, org.eclipse.core.runtime.IProgressMonitor)}
     * @throws IpsException in case of an error while setting the new content to the file @see
     *             {@link AFile#setContents(InputStream, boolean, org.eclipse.core.runtime.IProgressMonitor)}
     */
    public void writeToFile(AFile file, InputStream inputStream, boolean keepHistory) {
        file.setContents(inputStream, keepHistory, new NullProgressMonitor());
    }

    /**
     * Returns the artefact destination. The destination can either be the output folder for
     * mergeable artefacts or the one for derived artefacts.
     */
    protected APackageFragmentRoot getArtefactDestination(IIpsSrcFile ipsSrcFile) {
        return ipsSrcFile.getIpsPackageFragment().getRoot().getArtefactDestination(buildsDerivedArtefacts());
    }

}
