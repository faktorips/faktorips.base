/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsproject.bundle;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;

/**
 * This {@link AbstractIpsBundleContentIndex} is a helper to explore {@link AbstractIpsBundle IPS
 * bundles}.
 * <p>
 * The content index is used to get information about the model folder of an {@link IIpsElement} or
 * any other resource stored in the IPS model folder. It is also used to get a list of all objects
 * stored in the bundle. It should handle everything located to the content index of the IPS bundle.
 * <p>
 * Subclasses must call {@link #registerPath(Path, Path)} to build up the content index.
 * 
 * 
 * @author dicker
 */
public abstract class AbstractIpsBundleContentIndex {

    private final Map<Path, Path> fileToModelPath = new HashMap<>();
    private final Set<QualifiedNameType> qualifiedNameTypes = new HashSet<>();

    /**
     * registers the given relativePath of the modelPath and stores it.
     * <p>
     * If the given relativePath represents an {@link IIpsObject}, a {@link QualifiedNameType} is
     * created and stored in the Set of {@link QualifiedNameType}
     * 
     */
    protected void registerPath(Path modelPath, Path relativePath) {
        fileToModelPath.put(relativePath, modelPath);

        QualifiedNameType qualifiedNameType = createQualifiedNameType(relativePath.toString());
        if (qualifiedNameType != null) {
            qualifiedNameTypes.add(qualifiedNameType);
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
    public Path getModelPath(Path path) {
        return fileToModelPath.get(path);
    }

    /**
     * Returns a list of all {@link QualifiedNameType qualified name types} that were found in any
     * model folder in the archive.
     * 
     * @return A set of all the qualified names of all IPS objects found in any model path
     * @see #getQualifiedNameTypes(String)
     */
    public Set<QualifiedNameType> getQualifiedNameTypes() {
        return Collections.unmodifiableSet(qualifiedNameTypes);
    }

    /**
     * Returns a list of all {@link QualifiedNameType qualified name types} that were found in any
     * model folder but have the specified package name. Only qualified names of exactly this
     * package name are returned, no sub packages.
     * 
     * @param packageName The name of the package where all the qualified names should be located
     * @return A set of the qualified names located in the given package, independent of the model
     *             folder
     * @see #getQualifiedNameTypes()
     */
    public Set<QualifiedNameType> getQualifiedNameTypes(String packageName) {
        Set<QualifiedNameType> qualifiedNameTypesByPackageName = new HashSet<>();
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
        Set<String> packageNames = new HashSet<>();
        for (QualifiedNameType qualifiedNameType : getQualifiedNameTypes()) {
            packageNames.add(qualifiedNameType.getPackageName());
        }
        return packageNames;
    }

}
