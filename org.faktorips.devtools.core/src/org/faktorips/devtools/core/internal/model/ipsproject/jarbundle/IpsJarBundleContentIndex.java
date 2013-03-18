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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;

/**
 * The {@link IpsJarBundleContentIndex} reads the list of entries of a {@link JarFile} and caches
 * some information about the qualified names and folders.
 * <p>
 * The content index is used to get information about the model folder of an {@link IIpsElement} or
 * any other resource stored in the IPS model folder. It is also used to get a list of all objects
 * stored in the bundle. It should handle everything located to the content index of an JAR packed
 * IPS bundle.
 * 
 * @author dicker
 */
public class IpsJarBundleContentIndex {

    private final Map<IPath, IPath> modelPathsByQualifiedNameType = new HashMap<IPath, IPath>();

    private final Set<QualifiedNameType> qualifiedNameTypes = new HashSet<QualifiedNameType>();

    /**
     * Create an {@link IpsJarBundleContentIndex} reading from the specified {@link JarFile}. The
     * JarFile should be ready to read and it will be closed after the object was constructed.
     * <p>
     * Every file located in any of the given model folders is registered in the index. The files
     * are indexed relative to the model folder.
     * 
     * @param jarFile The {@link JarFile} that should be read and indexed
     * @param modelFolders The list of model folders, the paths are relativ to the root of the jar
     *            file
     */
    public IpsJarBundleContentIndex(JarFile jarFile, List<IPath> modelFolders) {
        try {
            Assert.isNotNull(jarFile, "jarFile must not be null"); //$NON-NLS-1$
            Assert.isNotNull(modelFolders, "modelFolders must not be null"); //$NON-NLS-1$

            Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();

                registerJarEntry(jarEntry, modelFolders);
            }
        } finally {
            try {
                jarFile.close();
            } catch (IOException e) {
                throw new RuntimeException("Error while closing jar file " + jarFile.getName(), e); //$NON-NLS-1$
            }
        }
    }

    private final void registerJarEntry(JarEntry jarEntry, List<IPath> modelFolders) {
        String pathToFile = jarEntry.getName();

        IPath path = new Path(pathToFile);

        registerPath(path, modelFolders);
    }

    private final void registerPath(IPath path, List<IPath> modelFolders) {
        for (IPath modelPath : modelFolders) {
            if (modelPath.isPrefixOf(path)) {

                IPath relativePath = path.makeRelativeTo(modelPath);

                modelPathsByQualifiedNameType.put(relativePath, modelPath);

                QualifiedNameType qualifiedNameType = createQualifiedNameType(relativePath.toString());
                if (qualifiedNameType != null) {
                    qualifiedNameTypes.add(qualifiedNameType);
                }
                return;
            }
        }
    }

    private QualifiedNameType createQualifiedNameType(String pathToFile) {
        if (!QualifiedNameType.representsQualifiedNameType(pathToFile)) {
            return null;
        } else {
            return QualifiedNameType.newQualifedNameType(pathToFile);
        }
    }

    /**
     * Returns the model path in which the file identified by the path parameter is stored in. For
     * example your file path is 'org/test/AnyFile.txt' and it is located in the model folder
     * 'model' then the full path relative to the root of the jar file would be
     * 'model/org/test/AnyFile.txt'. If you call this method like
     * <code>getModelPath(new Path("org/test/AnyFile.txt"))</code> the result will be a path of the
     * folder 'model'.
     * 
     * @param path A path to a file relative to any model folder
     * @return The path of the model folder in which the file is located
     */
    public IPath getModelPath(IPath path) {
        return modelPathsByQualifiedNameType.get(path);
    }

    /**
     * Returns a list of all {@link QualifiedNameType qualified name types} that were found in any
     * model folder in the archive.
     * 
     * @return A set of all the qualified names of all IPS objects found in any model path
     * @see #getQualifiedNameTypes(String)
     */
    public Set<QualifiedNameType> getQualifiedNameTypes() {
        return new CopyOnWriteArraySet<QualifiedNameType>(qualifiedNameTypes);
    }

    /**
     * Returns a list of all {@link QualifiedNameType qualified name types} that were found in any
     * model folder but have the specified package name. Only qualified names of exactly this
     * package name are returned, no sub packages.
     * 
     * @param packageName The name of the package where all the qualified names should be located
     * @return A set of the qualified names located in the given package, independent of the model
     *         folder
     * @see #getQualifiedNameTypes()
     */
    public Set<QualifiedNameType> getQualifiedNameTypes(String packageName) {
        Set<QualifiedNameType> qualifiedNameTypesByPackageName = new HashSet<QualifiedNameType>();
        for (QualifiedNameType qualifiedNameType : qualifiedNameTypes) {
            if (qualifiedNameType.getPackageName().equals(packageName)) {
                qualifiedNameTypesByPackageName.add(qualifiedNameType);
            }
        }

        return qualifiedNameTypesByPackageName;
    }

    /**
     * Returns a set of all packages found in any model path that contains at least one IPS object.
     * Empty parent packages, that means packages that contains a non-empty-package but do not
     * contain a IPS object are treated as empty packages. In other words: the result is the set of
     * the packages of all IPS objects stored in the JAR file.
     * 
     * @return All non-empty packages found in this JAR file
     */
    public Set<String> getNonEmptyPackagePaths() {
        Set<String> packageNames = new HashSet<String>();
        for (QualifiedNameType qualifiedNameType : getQualifiedNameTypes()) {
            packageNames.add(qualifiedNameType.getPackageName());
        }
        return packageNames;
    }
}
