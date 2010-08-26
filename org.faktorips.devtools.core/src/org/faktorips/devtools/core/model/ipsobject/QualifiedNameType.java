/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.model.ipsobject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.util.ArgumentCheck;

/**
 * Combines the qualified name and IPS object type.
 * 
 * @author Jan Ortmann
 */
public class QualifiedNameType implements Serializable, Comparable<QualifiedNameType> {

    private static final long serialVersionUID = -5891585006868536302L;

    private String qualifiedName;
    private transient IpsObjectType type;
    private transient int hashCode;

    // Cached Path
    // as Path is an immutable value object, we don't have any threading problems here
    // if two threads create two different paths we don't have a problem as the two paths are equal.
    private transient IPath path = null;

    /**
     * Returns the qualified name type for he given path.
     * 
     * @param pathToFile a relative path to an IPS source file, e.g. base/motor/MotorPolicy.ipspct
     * @return The qualified name type
     * 
     * @throws IllegalArgumentException if the path can't be parsed to a qualified name type
     */
    public final static QualifiedNameType newQualifedNameType(String pathToFile) {
        int index = pathToFile.lastIndexOf('.');
        if (index == -1 || index == pathToFile.length() - 1) {
            throw new IllegalArgumentException("Path " + pathToFile + " can't be parsed to a qualified name type."); //$NON-NLS-1$ //$NON-NLS-2$
        }
        IpsObjectType type = IpsObjectType.getTypeForExtension(pathToFile.substring(index + 1));
        if (type == null) {
            throw new IllegalArgumentException("Path " + pathToFile + " does not specifiy an ips object type."); //$NON-NLS-1$ //$NON-NLS-2$
        }
        String qName = pathToFile.substring(0, index).replace(IPath.SEPARATOR, IIpsPackageFragment.SEPARATOR);
        if (qName.equals("")) { //$NON-NLS-1$
            throw new IllegalArgumentException("Path " + pathToFile + " does not specifiy a qualified name."); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return new QualifiedNameType(qName, type);
    }

    public QualifiedNameType(String name, IpsObjectType type) {
        ArgumentCheck.notNull(name, this);
        ArgumentCheck.notNull(type, this);
        qualifiedName = name;
        this.type = type;
        calculateHashCode();
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
        int index = qualifiedName.lastIndexOf('.');
        if (index == -1) {
            return ""; //$NON-NLS-1$
        }
        return qualifiedName.substring(0, index);
    }

    /**
     * Returns the unqualified name.
     */
    public String getUnqualifiedName() {
        int index = qualifiedName.lastIndexOf('.');
        if (index == -1) {
            return qualifiedName;
        }
        if (index == qualifiedName.length() - 1) {
            return ""; //$NON-NLS-1$
        }
        return qualifiedName.substring(index + 1);
    }

    /**
     * Transforms this qualified name part into an IPath. E.g.: mycompany.motor.MotorPolicy of type
     * PolicyCmptType becomes mycompany/motor/MotorPolicy.ipspct
     */
    public IPath toPath() {
        if (path == null) {
            path = new Path(qualifiedName.replace(IIpsPackageFragment.SEPARATOR, IPath.SEPARATOR) + '.'
                    + type.getFileExtension());
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
        return type + ": " + qualifiedName; //$NON-NLS-1$
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
        type = IpsPlugin.getDefault().getIpsModel().getIpsObjectType(typeName);
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
