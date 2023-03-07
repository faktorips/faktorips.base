/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.dependency;

import java.io.Serializable;

import org.faktorips.devtools.model.DependencyType;
import org.faktorips.devtools.model.dependency.IDependency;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.util.ArgumentCheck;

/**
 * An implementation of the {@link IDependency} interface that describes a dependency between two
 * IpsObjects.
 * 
 * @author Peter Erzberger
 */
public class IpsObjectDependency implements IDependency, Serializable {

    private static final long serialVersionUID = -4763466997240470890L;

    private QualifiedNameType source;
    private QualifiedNameType target;
    private int hashCode;
    private DependencyType dependencyType;

    private IpsObjectDependency(QualifiedNameType source, QualifiedNameType target, DependencyType dependencyType) {
        super();
        ArgumentCheck.notNull(source, this);
        ArgumentCheck.notNull(target, this);
        ArgumentCheck.notNull(dependencyType, this);
        this.source = source;
        this.target = target;
        this.dependencyType = dependencyType;
        calculateHashCode();
    }

    /**
     * Creates a new dependency between the specified source and target objects and defines if it is
     * a transitive dependency.
     */
    public static final IpsObjectDependency create(QualifiedNameType source,
            QualifiedNameType target,
            DependencyType dependencyType) {

        return new IpsObjectDependency(source, target, dependencyType);
    }

    /**
     * Creates a new dependency indicating a sub type dependency between the specified source and
     * target objects. Such a dependency indicates that the source is sub type of the target and
     * hence the source depends on the target.
     */
    public static final IpsObjectDependency createSubtypeDependency(QualifiedNameType source,
            QualifiedNameType target) {
        return new IpsObjectDependency(source, target, DependencyType.SUBTYPE);
    }

    /**
     * Creates a new dependency indicating a configuration dependency of type
     * {@link DependencyType#CONFIGUREDBY}. The source needs to be a policy component type, the
     * target is a product component type.
     */
    public static final IpsObjectDependency createConfiguredByDependency(QualifiedNameType source,
            QualifiedNameType target) {
        return new IpsObjectDependency(source, target, DependencyType.CONFIGUREDBY);
    }

    /**
     * Creates a new dependency indicating a configuration dependency of type
     * {@link DependencyType#CONFIGURES}. The source needs to be a product component type, the
     * target is a policy component type.
     */
    public static final IpsObjectDependency createConfiguresDependency(QualifiedNameType source,
            QualifiedNameType target) {
        return new IpsObjectDependency(source, target, DependencyType.CONFIGURES);
    }

    /**
     * Creates a new dependency indicating referencing dependency between the specified source and
     * target objects. Such a dependency indicates that the source references the target and hence
     * the source depends on the target.
     */
    public static final IpsObjectDependency createReferenceDependency(QualifiedNameType source,
            QualifiedNameType target) {
        return new IpsObjectDependency(source, target, DependencyType.REFERENCE);
    }

    /**
     * Creates a new dependency indicating special referencing dependency of the kind composition
     * master to detail between the specified source and target objects. Such a dependency indicates
     * that the source references the target and hence the source depends on the target.
     */
    public static final IpsObjectDependency createCompostionMasterDetailDependency(QualifiedNameType source,
            QualifiedNameType target) {
        return new IpsObjectDependency(source, target, DependencyType.REFERENCE_COMPOSITION_MASTER_DETAIL);
    }

    /**
     * Creates a new dependency indicating an instance of dependency between the specified source
     * and target objects. Such a dependency indicates that the source is "kind of an instance" of
     * the target and hence the source depends on the target.
     * <p>
     * Note that the term "instance of" is used rather loosely here and does not necessarily mean
     * that source is a Java instance of target, e.g. a product component can have an instance of
     * dependency to its product template although technically it is an instance of it product
     * component type (and not of its template).
     */
    public static final IpsObjectDependency createInstanceOfDependency(QualifiedNameType source,
            QualifiedNameType target) {
        return new IpsObjectDependency(source, target, DependencyType.INSTANCEOF);
    }

    @Override
    public QualifiedNameType getSource() {
        return source;
    }

    public QualifiedNameType getTargetAsQNameType() {
        return target;
    }

    @Override
    public Object getTarget() {
        return target;
    }

    @Override
    public DependencyType getType() {
        return dependencyType;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof IpsObjectDependency other)) {
            return false;
        }
        return dependencyType.equals(other.getType()) && target.equals(other.getTarget())
                && source.equals(other.getSource());
    }

    private void calculateHashCode() {
        int result = 17;
        result = result * 37 + target.hashCode();
        result = result * 37 + source.hashCode();
        result = result * 37 + dependencyType.hashCode();
        hashCode = result;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return "(" + source.toString() + " -> " + target.toString() + ", type: " + dependencyType + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }

}
