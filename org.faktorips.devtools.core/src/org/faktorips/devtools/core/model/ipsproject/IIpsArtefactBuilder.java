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

package org.faktorips.devtools.core.model.ipsproject;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.MultiStatus;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;

// TODO Improve comment, the methods beforeFullBuild and afterFullBuild no longer exist ...
/**
 * An implementation of this interface is supposed to create one artifact for an IpsObject. The
 * isBuilderFor() method indicates to the IPS build framework which kind of IpsObjects this builder
 * is interested in. This interface describes a defined build cycle. For every IpsObject this
 * builder builds an artifact for, the following methods are called sequentially beforeBuild(),
 * build(), afterBuild(). If a full build is started the beforeFullBuild() method is called before
 * the first IpsSrcFile that hosts the IpsObject is provided to this builder and the
 * afterFullBuild() method is called after the last IpsSrcFile has been provided to this builder. A
 * set of IpsArtefactBuilders are collected within an IpsArtefactBuilderSet. The builders are made
 * available to the building system by registering the IpsArtefactBuilderSet at the according
 * extension point.
 * 
 * @author Peter Erzberger
 */
public interface IIpsArtefactBuilder {

    /**
     * Returns the builder's name.
     */
    public String getName();

    /**
     * Returns the builder set this builder belongs to.
     */
    public IIpsArtefactBuilderSet getBuilderSet();

    /**
     * Is called on every registered IpsArtefactBuilder before a build process starts.
     * 
     * @param buildKind One of the build kinds defined in
     *            org.eclipse.core.resources.IncrementalProjectBuilder
     * 
     * @throws CoreException implementations can throw or delegate rising CoreExceptions. Throwing a
     *             CoreException or RuntimeException will stop the current build cycle of this
     *             builder.
     */
    public void beforeBuildProcess(IIpsProject ipsProject, int buildKind) throws CoreException;

    /**
     * Is called on every registered IpsArtefactBuilder after a build process has finished.
     * 
     * @param buildKind One of the build kinds defined in
     *            org.eclipse.core.resources.IncrementalProjectBuilder
     * 
     * @throws CoreException implementations can throw or delegate rising CoreExceptions.
     */
    public void afterBuildProcess(IIpsProject ipsProject, int buildKind) throws CoreException;

    /**
     * Is called directly before the build method is called if the isBuilderFor method has returned
     * true for the provided IpsSrcFile. This method is supposed to be used to set the builder in a
     * defined state before the actual build process starts.
     * 
     * @param ipsSrcFile the IpsSrcFile that is used by this artifact builder
     * @param status exceptional states can be reported to this multi status object. This will not
     *            interrupt the current build cycle. The exception will be reported to the used by
     *            means of a dialog at the end of the build routine. In addition the exception will
     *            be logged to the eclipse log file.
     * @throws CoreException implementations can throw or delegate rising CoreExceptions. Throwing a
     *             CoreException or a RuntimeException will stop the current build cycle of this
     *             builder. Only the afterBuild(IpsSrcFile) method is called to be able to clean up
     *             a builder implementation. The exception will be reported to the used by means of
     *             a dialog at the end of the build routine. In addition the exception will be
     *             logged to the eclipse log file.
     */
    public void beforeBuild(IIpsSrcFile ipsSrcFile, MultiStatus status) throws CoreException;

    /**
     * Is called directly after the build method has finished only if the isBuilderFor method has
     * returned true for the provided IpsSrcFile. This method is supposed to be used for clean up
     * after the build has finished.
     * 
     * @param ipsSrcFile the IpsSrcFile that is used by this artifact builder
     * @throws CoreException implementations can throw or delegate rising CoreExceptions. The
     *             exception will be reported to the used by means of a dialog at the end of the
     *             build routine. In addition the exception will be logged to the eclipse log file.
     */
    public void afterBuild(IIpsSrcFile ipsSrcFile) throws CoreException;

    /**
     * Is called during full or incremental build if the isBuilderFor method has returned true for
     * the provided IpsSrcFile.
     * 
     * @param ipsSrcFile the IpsSrcFile that is used by this artifact builder
     * @throws CoreException implementations can throw or delegate rising CoreExceptions. Throwing a
     *             CoreException or a RuntimeException will stop the current build cycle of this
     *             builder. Only the afterBuild(IpsSrcFile) method is called to be able to clean up
     *             a builder implementation. The exception will be reported to the used by means of
     *             a dialog at the end of the build routine. In addition the exception will be
     *             logged to the eclipse log file.
     */
    public void build(IIpsSrcFile ipsSrcFile) throws CoreException;

    /**
     * Is supposed to return <tt>true</tt> if this builder is a builder for the provided
     * <tt>IIpsSrcFile</tt>.
     */
    // TODO AW: This method does not need to - and should not - throw a checked exception.
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws CoreException;

    /**
     * Indicates if the builder generates files that are considered derived. This means that the
     * files will be regenerated during each build cycle and be deleted during a clean build cycle.
     * Code that is added to these files between builds will be removed. The location where the
     * artifact is put is defined in the IpsObjectPath or alternatively in the IpsSrcFolderEntry of
     * an IpsObjectPath.
     */
    public boolean buildsDerivedArtefacts();

    /**
     * Deletes the artifact that is created by this builder upon the provided IpsSrcFile.
     * 
     * @param ipsSrcFile the IpsSrcFile that is used by this artifact builder
     * @throws CoreException implementations can throw or delegate rising CoreExceptions. The
     *             exception will be reported to the used by means of a dialog at the end of the
     *             build routine. In addition the exception will be logged to the eclipse log file.
     */
    public void delete(IIpsSrcFile ipsSrcFile) throws CoreException;

    /**
     * Initializes this builder's builder set if it hasn't been initialized yet. Does nothing
     * otherwise.
     * 
     * @param builderSet this builder's builder set. Must not be <code>null</code>.
     */
    public abstract void init(IIpsArtefactBuilderSet builderSet);

}
