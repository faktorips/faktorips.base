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

import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsSrcFolderEntry;

/**
 * This interface provides the package name of every kind of generated class within this package
 * structure. Since it is possible to generate multiple Java classes for an <code>IIpsObject</code>
 * it is not enough to just provide the {@link IIpsSrcFile} instance to the methods of the package
 * structure. The additional parameters specify the kind of artifact that is generated.
 * 
 * @author Peter Erzberger
 */
public interface IJavaPackageStructure {

    /**
     * Returns the package string for the provided IpsObject depending on the parameter.
     * 
     * @param ipsSrcFile The {@link IIpsSrcFile} for which you want to get the generated package
     * @param internalArtifact <code>true</code> if you want the base package for internal artifacts
     *            <code>false</code> for published
     * @param mergableArtifacts <code>true</code> if you want the base package for mergable
     *            artifacts <code>false</code> for derived
     * 
     * @return The name of the package where the artifacts are generated for the given
     *             {@link IIpsSrcFile} depending on the parameters
     */
    String getPackageName(IIpsSrcFile ipsSrcFile, boolean internalArtifact, boolean mergableArtifacts);

    /**
     * Returns the base package for the given {@link IIpsSrcFolderEntry} depending on whether it is
     * a published artifact and/or mergable artifact
     * 
     * @param entry The {@link IIpsSrcFolderEntry} you want to get the base package for
     * @param internalArtifact <code>true</code> if you want the base package for internal artifacts
     *            <code>false</code> for published
     * @param mergableArtifacts <code>true</code> if you want the base package for mergable
     *            artifacts <code>false</code> for derived
     * 
     * @return The name of the base package generated for the given parameters
     */
    String getBasePackageName(IIpsSrcFolderEntry entry, boolean internalArtifact, boolean mergableArtifacts);

}
