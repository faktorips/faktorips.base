/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.builder;

import java.io.ByteArrayInputStream;
import java.util.Locale;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.MultiStatus;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilder;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.LocalizedStringsSet;

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
    public void init(IIpsArtefactBuilderSet builderSet) {
        if (this.builderSet == null) {
            ArgumentCheck.notNull(builderSet);
            this.builderSet = builderSet;
        }
    }

    @Override
    public IIpsArtefactBuilderSet getBuilderSet() {
        return builderSet;
    }

    public IIpsProject getIpsProject() {
        return builderSet.getIpsProject();
    }

    @Override
    public void beforeBuildProcess(IIpsProject project, int buildKind) throws CoreException {
        // default implementation does nothing
    }

    @Override
    public void afterBuildProcess(IIpsProject project, int buildKind) throws CoreException {
        // default implementation does nothing
    }

    @Override
    public void beforeBuild(IIpsSrcFile ipsSrcFile, MultiStatus status) throws CoreException {
        // default implementation does nothing
    }

    @Override
    public void afterBuild(IIpsSrcFile ipsSrcFile) throws CoreException {
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
        return getName();
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
     * @throws CoreException if an Exception occurs during the creation procedure
     * @throws RuntimeException if the provided file parameter is <code>null</code>
     */
    protected boolean createFileIfNotThere(IFile file) throws CoreException {
        ArgumentCheck.notNull(file, this);
        if (!file.exists()) {
            IContainer parent = file.getParent();

            if (parent instanceof IFolder) {
                createFolderIfNotThere((IFolder)parent);
            }
            file.create(new ByteArrayInputStream("".getBytes()), true, null); //$NON-NLS-1$
            file.setDerived(buildsDerivedArtefacts() && getBuilderSet().isMarkNoneMergableResourcesAsDerived());
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
     * @throws CoreException if an Exception occurs during the creation procedure
     * @throws RuntimeException if the provided folder parameter is <code>null</code>
     */
    protected boolean createFolderIfNotThere(IFolder folder) throws CoreException {

        ArgumentCheck.notNull(folder, this);
        if (!folder.exists()) {
            IContainer parent = folder.getParent();
            if (parent instanceof IFolder) {
                createFolderIfNotThere((IFolder)parent);
            }
            folder.create(true, true, null);
            folder.setDerived(buildsDerivedArtefacts() && getBuilderSet().isMarkNoneMergableResourcesAsDerived());
            return true;
        }
        return false;
    }

    /**
     * Returns the language in that variables, methods are named and and Java documentations are
     * written in.
     */
    public Locale getLanguageUsedInGeneratedSourceCode() {
        return getBuilderSet().getLanguageUsedInGeneratedSourceCode();
    }

    /**
     * Returns the localized text for the provided key. Calling this method is only allowed during
     * the build cycle. If it is called outside the build cycle a RuntimeException is thrown. In
     * addition if no LocalizedStringSet has been set to this builder a RuntimeException is thrown.
     * 
     * @param element the IPS element used to access the IPS project where the language to use is
     *            defined.
     * @param key the key that identifies the requested text
     */
    public String getLocalizedText(IIpsElement element, String key) {
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
     * @param element the IPS element used to access the IPS project where the language to use is
     *            defined.
     * @param key the key that identifies the requested text
     * @param replacement an indicated region within the text is replaced by the string
     *            representation of this value
     */
    public String getLocalizedText(IIpsElement element, String key, Object replacement) {
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
     * @param element the IPS element used to access the IPS project where the language to use is
     *            defined.
     * @param key the key that identifies the requested text
     * @param replacements indicated regions within the text are replaced by the string
     *            representations of these values.
     */
    public String getLocalizedText(IIpsElement element, String key, Object[] replacements) {
        if (localizedStringsSet == null) {
            throw new RuntimeException(
                    "A LocalizedStringSet has to be set to this builder to be able to call this method."); //$NON-NLS-1$
        }
        return getLocalizedStringSet().getString(key, getLanguageUsedInGeneratedSourceCode(), replacements);
    }

}
