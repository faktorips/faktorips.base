/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject.jarbundle;

import java.io.IOException;
import java.util.jar.JarFile;

import org.eclipse.core.runtime.IPath;

/**
 * This class simply creates a JarFile for a previously specified path. After creating a
 * {@link JarFileFactory} with a specified {@link IPath} you could create as many {@link JarFile jar
 * files} as you want by calling {@link #createJarFile()}. You have to verify for yourself that the
 * jar file is closed correctly after use.
 * 
 * 
 * @author dirmeier
 */
public class JarFileFactory {

    private final IPath jarPath;

    /**
     * Create the {@link JarFileFactory}, the jarPath is the absolute path to the jar file this
     * factory will construct.
     * 
     * @param jarPath The absolute path to a jar file
     */
    public JarFileFactory(IPath jarPath) {
        this.jarPath = jarPath;
    }

    /**
     * Returns the path of the jar file this factory could construct
     * 
     * @return The absolute jar file path
     */
    public IPath getJarPath() {
        return jarPath;
    }

    /**
     * Creates a new jar file every time you call this method. You have to ensure that the jar file
     * is closed correctly after you do not need it any longer.
     * 
     * @return A new jar file of the path specified in this factory
     * 
     * @throws IOException in case of any IO exception while creating the {@link JarFileFactory}
     * @see JarFile#JarFile(java.io.File)
     */
    public JarFile createJarFile() throws IOException {
        return new JarFile(jarPath.toFile());
    }

}
