/***************************************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere. Alle Rechte vorbehalten. Dieses Programm und
 * alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, etc.) dürfen nur unter
 * den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung
 * Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html eingesehen werden kann. Mitwirkende: Faktor Zehn GmbH -
 * initial API and implementation
 **************************************************************************************************/

package org.faktorips.devtools.core.model;

import java.io.Serializable;

import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.util.ArgumentCheck;

/**
 * This class describes a dependency of a source and a target IpsObject more precisely that the
 * source object dependents on the target object. The source and target IpsObjects are identified by
 * their qualified name types. The dependency type descibes the kind of the dependency. It is up to
 * the IpsBuilder how to interpret this type. Dependency instances are created by the dependsOn()
 * methods of IpsObjects to indicate the dependency to other IpsObjects. The DependencyGraph which
 * is used by the IpsBuilder to determine the dependent IpsObjects during an incremental build cycle
 * utilizes the dependsOn() method to determine its state.
 * 
 * @author Peter Erzberger
 */
public class Dependency implements Serializable{

    private static final long serialVersionUID = -4763466997240470890L;

    private QualifiedNameType source;
    private QualifiedNameType target;
    private int hashCode;
    private DependencyType dependencyType;

    private Dependency(QualifiedNameType source, QualifiedNameType target, DependencyType dependencyType) {
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
     * Creates a new Dependency between the specified source and target objects and defines if it is
     * a transitive dependency.
     */
    public final static Dependency create(QualifiedNameType source,
            QualifiedNameType target,
            DependencyType dependencyType) {
        return new Dependency(source, target, dependencyType);
    }

    /**
     * Creates a new Dependency instance indicating an instance of dependency between the specified
     * source and target objects. A Dependency instance indicates that the source is subtype of the
     * target and hence the source depends on the target.
     */
    public final static Dependency createSubtypeDependency(QualifiedNameType source, QualifiedNameType target) {
        return new Dependency(source, target, DependencyType.SUBTYPE);
    }

    /**
     * Creates a new Dependency instance indicating referencing dependency between the specified
     * source and target objects. A Dependency instance indicates that the source references the
     * target and hence the source depends on the target.
     */
    public final static Dependency createReferenceDependency(QualifiedNameType source, QualifiedNameType target) {
        return new Dependency(source, target, DependencyType.REFERENCE);
    }

    /**
     * Creates a new Dependency instance indicating special referencing dependency of the kind
     * compostion master to detail between the specified source and target objects. A Dependency
     * instance indicates that the source references the target and hence the source depends on the
     * target.
     */
    public final static Dependency createCompostionMasterDetailDependency(QualifiedNameType source,
            QualifiedNameType target) {
        return new Dependency(source, target, DependencyType.REFERENCE_COMPOSITION_MASTER_DETAIL);
    }

    /**
     * Creates a new Dependency instance indicating an instance of dependency between the specified
     * source and target objects. A Dependency instance indicates that the source uses the target
     * and hence the source depends on the target.
     */
    public final static Dependency createUsesDependency(QualifiedNameType source, QualifiedNameType target) {
        return new Dependency(source, target, DependencyType.USES);
    }

    /**
     * Creates a new Dependency instance indicating an instance of dependency between the specified
     * source and target objects. A Dependency instance indicates that the source is an instance of
     * the target and hence the source depends on the target.
     */
    public final static Dependency createInstanceOfDependency(QualifiedNameType source, QualifiedNameType target) {
        return new Dependency(source, target, DependencyType.INSTANCEOF);
    }

    /**
     * The source object
     */
    public QualifiedNameType getSource() {
        return source;
    }

    /**
     * The target object
     */
    public QualifiedNameType getTarget() {
        return target;
    }

    public DependencyType getType() {
        return dependencyType;
    }

    /**
     * Returns true if this is a referencing dependency.
     */
    public boolean isReference() {
        return dependencyType == DependencyType.REFERENCE;
    }

    /**
     * Returns true if this is a composition master to detail dependency.
     */
    public boolean isCompositionMasterDetail() {
        return dependencyType == DependencyType.REFERENCE_COMPOSITION_MASTER_DETAIL;
    }

    /**
     * Returns true if this is an instance of dependency.
     */
    public boolean isInstanceOf() {
        return dependencyType == DependencyType.INSTANCEOF;
    }

    /**
     * Returns true if this is a subtype dependency.
     */
    public boolean isSubtype() {
        return dependencyType == DependencyType.SUBTYPE;
    }

    /**
     * Returns true if this is an uses dependency.
     */
    public boolean isUses() {
        return dependencyType == DependencyType.USES;
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object o) {
        if (o instanceof Dependency) {
            Dependency other = (Dependency)o;
            return this.dependencyType.equals(other.dependencyType) && this.target.equals(other.target)
                    && this.source.equals(other.source);
        }
        return false;
    }

    private void calculateHashCode() {
        int result = 17;
        result = result * 37 + target.hashCode();
        result = result * 37 + source.hashCode();
        result = result * 37 + dependencyType.hashCode();
        hashCode = result;
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return hashCode;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "(" + source.toString() + " -> " + target.toString() + " type: " + dependencyType + ")";
    }
}
