/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;

/**
 * This interface provides the package name of every kind of generated class within this package
 * structure. Since it is possible to generate multiple Java classes for an <tt>IIpsObject</tt> it
 * is not enough to just provide the {@link IIpsSrcFile} instance to the methods of the package
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
     *         {@link IIpsSrcFile} depending on the parameters
     */
    public String getPackageName(IIpsSrcFile ipsSrcFile, boolean internalArtifact, boolean mergableArtifacts);

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
    public String getBasePackageName(IIpsSrcFolderEntry entry, boolean internalArtifact, boolean mergableArtifacts);

}
