/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.model.pctype.persistence.IPersistentAttributeInfo;
import org.faktorips.devtools.model.pctype.persistence.IPersistentTypePartInfo;

/**
 * Allows to specify several vendor specific JPA annotations.
 * 
 * @author Joerg Ortmann
 */
public interface IPersistenceProvider {

    /**
     * Returns <code>true</code> if the persistent provider supports the orphan removal (private
     * owned) annotation.
     */
    boolean isSupportingOrphanRemoval();

    /**
     * If orphan removal is supported then this method must be used to add the necessary annotation
     * to the given java code fragment (e.g. PrivateOwned)
     */
    void addAnnotationOrphanRemoval(JavaCodeFragmentBuilder fragmentBuilder);

    /**
     * If orphan removal is supported then this method must be used to get the attribute to the
     * relationship annotation (e.g. orphanRemoval=true). Returns an empty string ("" or
     * <code>null</code>) if no attribute is necessary.
     */
    String getRelationshipAnnotationAttributeOrphanRemoval();

    /**
     * Returns <code>true</code> if the persistent provider supports converters.
     */
    boolean isSupportingConverters();

    /**
     * If converters are supported then this method must be used to add the necessary annotation to
     * the given java code fragment.
     */
    JavaCodeFragment getConverterAnnotations(IPersistentAttributeInfo persistentAttributeInfo);

    /**
     * Returns <code>true</code> if the persistent provider supports the @Index annotation
     */
    boolean isSupportingIndex();

    /**
     * If index annotations are supported, this method returns the generated index annotation.
     */
    JavaCodeFragment getIndexAnnotations(IPersistentTypePartInfo persistentAttributeInfo);

    String getQualifiedName(PersistenceClass persistenceClass);

    public interface PersistenceClass {
        // marker for class name enums
    }

    public enum PersistenceAnnotation implements PersistenceClass {
        Column,
        Convert,
        DiscriminatorColumn,
        DiscriminatorValue,
        Entity,
        Inheritance,
        JoinColumn,
        JoinTable,
        ManyToMany,
        ManyToOne,
        MappedSuperclass,
        OneToMany,
        OneToOne,
        Table,
        Temporal,
        Transient;
    }

    public enum PersistenceEnum implements PersistenceClass {
        CascadeType,
        DiscriminatorType,
        FetchType,
        InheritanceType,
        TemporalType;
    }

}
