/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.ipsobject;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Path;

import org.faktorips.runtime.internal.IpsStringUtils;
import org.eclipse.core.runtime.IPath;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.util.ArgumentCheck;

import edu.umd.cs.findbugs.annotations.CheckForNull;

/**
 * Combines the qualified name and IPS object type.
 * 
 * @author Jan Ortmann
 */
public class QualifiedNameType implements Serializable, Comparable<QualifiedNameType> {

    public static final char FILE_EXTENSION_SEPERATOR = '.';

    private static final long serialVersionUID = -5891585006868536302L;

    private String qualifiedName;
    private transient IpsObjectType type;
    private transient int hashCode;

    // Cached Path
    // as Path is an immutable value object, we don't have any threading problems here
    // if two threads create two different paths we don't have a problem as the two paths are equal.
    private transient Path path = null;

    public QualifiedNameType(String name, IpsObjectType type) {
        ArgumentCheck.notNull(name);
        ArgumentCheck.notNull(type);
        qualifiedName = name;
        this.type = type;
        calculateHashCode();
    }

    /**
     * Returns the qualified name type for he given path.
     * 
     * @param pathToFile a relative path to an IPS source file, e.g. base/motor/MotorPolicy.ipspct
     * @return The qualified name type
     * 
     * @throws IllegalArgumentException if the path can't be parsed to a qualified name type
     */
    public static final QualifiedNameType newQualifedNameType(String pathToFile) {
        if (!representsQualifiedNameType(pathToFile)) {
            throw new IllegalArgumentException("Path " + pathToFile + " does not specifiy an IPS object type."); //$NON-NLS-1$ //$NON-NLS-2$
        }

        int index = pathToFile.lastIndexOf(FILE_EXTENSION_SEPERATOR);

        IpsObjectType type = IpsObjectType.getTypeForExtension(pathToFile.substring(index + 1));

        String qNameWithBackslash = pathToFile.substring(0, index).replace(IPath.SEPARATOR,
                IIpsPackageFragment.SEPARATOR);
        String qName = qNameWithBackslash.replace('\\', IIpsPackageFragment.SEPARATOR);

        return new QualifiedNameType(qName, type);
    }

    public static final boolean representsQualifiedNameType(@CheckForNull String pathToFile) {
        if (pathToFile == null) {
            return false;
        }
        int index = pathToFile.lastIndexOf(FILE_EXTENSION_SEPERATOR);
        if (index == -1 || index == pathToFile.length() - 1) {
            return false;
        }
        IpsObjectType type = IpsObjectType.getTypeForExtension(pathToFile.substring(index + 1));
        if (type == null) {
            return false;
        }
        String qName = pathToFile.substring(0, index).replace(IPath.SEPARATOR, IIpsPackageFragment.SEPARATOR);
        if (qName.equals(IpsStringUtils.EMPTY)) {
            return false;
        }

        return true;
    }

    /**
     * Returns the qualified name.
     */
    public String getName() {
        return qualifiedName;
    }

    /**
     * Returns the IPS object type.
     */
    public IpsObjectType getIpsObjectType() {
        return type;
    }

    /**
     * Returns the package name part of the qualified name.
     */
    public String getPackageName() {
        int index = qualifiedName.lastIndexOf(IIpsPackageFragment.SEPARATOR);
        if (index == -1) {
            return ""; //$NON-NLS-1$
        }
        return qualifiedName.substring(0, index);
    }

    /**
     * Returns the unqualified name.
     */
    public String getUnqualifiedName() {
        int index = qualifiedName.lastIndexOf(IIpsPackageFragment.SEPARATOR);
        if (index == -1) {
            return qualifiedName;
        }
        if (index == qualifiedName.length() - 1) {
            return ""; //$NON-NLS-1$
        }
        return qualifiedName.substring(index + 1);
    }

    /**
     * Transforms this qualified name part into a Path. E.g.: mycompany.motor.MotorPolicy of type
     * PolicyCmptType becomes mycompany/motor/MotorPolicy.ipspct
     */
    public Path toPath() {
        if (path == null) {
            path = Path.of(qualifiedName.replace(IIpsPackageFragment.SEPARATOR, File.separatorChar)
                    + FILE_EXTENSION_SEPERATOR + type.getFileExtension());
        }
        return path;
    }

    /**
     * Returns the name for files in that an IPS object with this qualified name type is stored.
     * E.g.: for "mycompany.motor.MotorPolicy" of type PolicyCmptType the method returns
     * "MotorPolicy.ipspct"
     */
    public String getFileName() {
        return type.getFileName(getUnqualifiedName());
    }

    private void calculateHashCode() {
        int result = 17;
        result = result * 37 + qualifiedName.hashCode();
        result = result * 37 + type.hashCode();
        hashCode = result;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof QualifiedNameType) {
            QualifiedNameType other = (QualifiedNameType)obj;
            return type.equals(other.type) && qualifiedName.equals(other.qualifiedName);
        }
        return false;
    }

    @Override
    public String toString() {
        return type.getDisplayName() + ": " + qualifiedName; //$NON-NLS-1$
    }

    /**
     * @serialData the default serialization is called followed by the IpsObjectType name
     */
    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        s.writeObject(type.getId());
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        String typeName = (String)s.readObject();
        type = IIpsModel.get().getIpsObjectType(typeName);
        if (type == null) {
            throw new IllegalStateException(
                    "Unable to deserialize this qualified name type because the IpsObjectType could not be resolved: " //$NON-NLS-1$
                            + type);
        }
        calculateHashCode();
    }

    /**
     * {@inheritDoc}
     * <p>
     * QualifiedNameTypes are compared by their package name, then by their unqualified name and
     * then by their IPS object type's name.
     */
    @Override
    public int compareTo(QualifiedNameType other) {
        int c = getPackageName().compareTo(other.getPackageName());
        if (c != 0) {
            return c;
        }
        c = getUnqualifiedName().compareTo(other.getUnqualifiedName());
        if (c != 0) {
            return c;
        }
        return getIpsObjectType().getId().compareTo(other.getIpsObjectType().getId());
    }

}
