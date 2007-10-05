/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.model;

import org.faktorips.util.ArgumentCheck;

/**
 * This class describes a dependency of a source and a target IpsObject more precisely that the source
 * object is dependent on the target object. The source and target IpsObjects are identified by
 * their qualified name types. A dependency can be transitive. That means if an object A is
 * dependent on B and B is dependent on C than A is dependent on C. Dependency instances are created
 * by the dependsOn() methods of IpsObjects to indicate the dependency to other IpsObjects. The
 * DependencyGraph that is used by the IpsBuilder to determine the dependent IpsObjects during an
 * incremental build cycle utilized the dependsOn() method to determine its state.
 * 
 * @author Peter Erzberger
 */
public class Dependency {
    
    private QualifiedNameType source;
    private QualifiedNameType target;
    private boolean transitive;
    private int hashCode;
    
    private Dependency(QualifiedNameType source, QualifiedNameType target, boolean deep) {
        super();
        ArgumentCheck.notNull(source, this);
        ArgumentCheck.notNull(target, this);
        this.source = source;
        this.target = target;
        this.transitive = deep;
        calculateHashCode();
    }
 
    /**
     * Creates a new Dependency between the specified source and target objects and defines if it is a transitive dependency.
     */
    public final static Dependency create(QualifiedNameType source, QualifiedNameType target, boolean transitive){
        return new Dependency(source, target, transitive);
    }

    /**
     * Creates a new non transitive Dependency between the specified source and target objects.
     */
    public final static Dependency create(QualifiedNameType source, QualifiedNameType target){
        return new Dependency(source, target, false);
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

    /**
     * Indicates is this is a transitive dependency.
     */
    public boolean isTransitive(){
        return transitive;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean equals(Object o) {
        if (o instanceof Dependency) {
            Dependency other = (Dependency)o;
            return this.transitive == other.transitive && this.target.equals(other.target) && this.source.equals(other.source);
        }
        return false;
    }

    private void calculateHashCode(){
        int result = 17;
        result = result*37 + target.hashCode();
        result = result*37 + source.hashCode();
        result = result*37 + (transitive ? 0 : 1);
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
        return "(" + source.toString() + " -> " + target.toString() + " transitive: " + transitive + ")";
    }
}
