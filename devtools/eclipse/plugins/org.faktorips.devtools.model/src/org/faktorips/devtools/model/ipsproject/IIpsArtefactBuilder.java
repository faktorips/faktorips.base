/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.ipsproject;

import org.eclipse.core.runtime.MultiStatus;
import org.faktorips.devtools.abstraction.ABuildKind;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;

/**
 * An implementation of this interface is supposed to create one artifact for an IpsObject. The
 * {@link #isBuilderFor(IIpsSrcFile)} method indicates to the IPS build framework which kind of
 * IpsObjects this builder is interested in. This interface describes a defined build cycle. For
 * every IpsObject this builder builds an artifact for, the following methods are called
 * sequentially {@link #beforeBuild(IIpsSrcFile, MultiStatus)}, {@link #build(IIpsSrcFile)},
 * {@link #afterBuild(IIpsSrcFile)}. A set of IpsArtefactBuilders are collected within an
 * IpsArtefactBuilderSet. The builders are made available to the building system by registering the
 * IpsArtefactBuilderSet at the according extension point.
 * 
 * @author Peter Erzberger
 */
public interface IIpsArtefactBuilder {

    /**
     * Returns the builder's name.
     */
    String getName();

    /**
     * Returns the builder set this builder belongs to.
     */
    IIpsArtefactBuilderSet getBuilderSet();

    /**
     * Is called on every registered IpsArtefactBuilder before a build process starts.
     * 
     * @param buildKind One of the build kinds defined in
     *            org.eclipse.core.resources.IncrementalProjectBuilder
     * 
     * @throws IpsException implementations can throw or delegate rising CoreExceptions. Throwing a
     *             CoreException or RuntimeException will stop the current build cycle of this
     *             builder.
     */
    void beforeBuildProcess(IIpsProject ipsProject, ABuildKind buildKind) throws IpsException;

    /**
     * Is called on every registered IpsArtefactBuilder after a build process has finished.
     * 
     * @param buildKind One of the build kinds defined in
     *            org.eclipse.core.resources.IncrementalProjectBuilder
     * 
     * @throws IpsException implementations can throw or delegate rising CoreExceptions.
     */
    void afterBuildProcess(IIpsProject ipsProject, ABuildKind buildKind) throws IpsException;

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
     * @throws IpsException implementations can throw or delegate rising CoreExceptions. Throwing a
     *             CoreException or a RuntimeException will stop the current build cycle of this
     *             builder. Only the afterBuild(IpsSrcFile) method is called to be able to clean up
     *             a builder implementation. The exception will be reported to the used by means of
     *             a dialog at the end of the build routine. In addition the exception will be
     *             logged to the eclipse log file.
     */
    void beforeBuild(IIpsSrcFile ipsSrcFile, MultiStatus status) throws IpsException;

    /**
     * Is called directly after the build method has finished only if the isBuilderFor method has
     * returned true for the provided IpsSrcFile. This method is supposed to be used for clean up
     * after the build has finished.
     * 
     * @param ipsSrcFile the IpsSrcFile that is used by this artifact builder
     * @throws IpsException implementations can throw or delegate rising CoreExceptions. The
     *             exception will be reported to the used by means of a dialog at the end of the
     *             build routine. In addition the exception will be logged to the eclipse log file.
     */
    void afterBuild(IIpsSrcFile ipsSrcFile) throws IpsException;

    /**
     * Is called during full or incremental build if the isBuilderFor method has returned true for
     * the provided IpsSrcFile.
     * 
     * @param ipsSrcFile the IpsSrcFile that is used by this artifact builder
     * @throws IpsException implementations can throw or delegate rising CoreExceptions. Throwing a
     *             CoreException or a RuntimeException will stop the current build cycle of this
     *             builder. Only the afterBuild(IpsSrcFile) method is called to be able to clean up
     *             a builder implementation. The exception will be reported to the used by means of
     *             a dialog at the end of the build routine. In addition the exception will be
     *             logged to the eclipse log file.
     */
    void build(IIpsSrcFile ipsSrcFile) throws IpsException;

    /**
     * Is supposed to return <code>true</code> if this builder is a builder for the provided
     * <code>IIpsSrcFile</code>.
     */
    // TODO AW: This method does not need to - and should not - throw a checked exception.
    boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws IpsException;

    /**
     * Indicates if the builder generates files that are considered derived. This means that the
     * files will be regenerated during each build cycle and be deleted during a clean build cycle.
     * Code that is added to these files between builds will be removed. The location where the
     * artifact is put is defined in the IpsObjectPath or alternatively in the IpsSrcFolderEntry of
     * an IpsObjectPath.
     */
    boolean buildsDerivedArtefacts();

    /**
     * Indicates if the builder generates files that are considered internal. Internal files are
     * generated in internal packages.
     * 
     * @return <code>true</code> if this builder generates internal artifacts <code>false</code> for
     *             published artifacts
     */
    boolean isBuildingInternalArtifacts();

    /**
     * Deletes the artifact that is created by this builder upon the provided IpsSrcFile.
     * 
     * @param ipsSrcFile the IpsSrcFile that is used by this artifact builder
     * @throws IpsException implementations can throw or delegate rising CoreExceptions. The
     *             exception will be reported to the used by means of a dialog at the end of the
     *             build routine. In addition the exception will be logged to the eclipse log file.
     */
    void delete(IIpsSrcFile ipsSrcFile) throws IpsException;
}
