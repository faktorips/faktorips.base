/*******************************************************************************
 * Copyright © 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 ******************************************************************************/
package org.faktorips.devtools.core.model;

import java.io.Serializable;

import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.util.ArgumentCheck;

/**
 * Instances of this class indicate a dependency between an IpsObject and a data type. It is assumed
 * that the source is an IpsObject and therefore the qualified name type is used to identify it. The
 * target is assumed to be a datatype where only the qualified name is known.
 * 
 * @author Peter Erzberger
 */
public class DatatypeDependency implements IDependency, Serializable {

    private static final long serialVersionUID = 6487956167551523725L;

    private QualifiedNameType source;
    private String target;
    private DependencyType type;
    private int hashCode;

    /**
     * Creates a new instance.
     * 
     * @param source The source of this dependency. Must not be <code>null</code>.
     * @param target The target of this dependency. Must nott be <code>null</code>.
     */
    public DatatypeDependency(QualifiedNameType source, String target) {
        super();
        ArgumentCheck.notNull(source, this);
        ArgumentCheck.notNull(target, this);
        this.source = source;
        this.target = target;
        type = DependencyType.DATATYPE;
        calculateHashCode();
    }

    @Override
    public QualifiedNameType getSource() {
        return source;
    }

    /**
     * Returns the target as string which is the actual data type of the target property.
     */
    public String getTargetAsQualifiedName() {
        return target;
    }

    @Override
    public Object getTarget() {
        return target;
    }

    @Override
    public DependencyType getType() {
        return type;
    }

    private void calculateHashCode() {
        int result = 17;
        result = result * 37 + source.hashCode();
        result = result * 37 + target.hashCode();
        hashCode = result;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DatatypeDependency)) {
            return false;
        }

        DatatypeDependency other = (DatatypeDependency)obj;

        return getSource().equals(other.getSource()) && getTarget().equals(other.getTarget())
                && getType().equals(other.getType());
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return "(" + source.toString() + " -> " + target.toString() + ", type: " + type + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }

}
