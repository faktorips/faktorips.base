/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model;

import java.util.EnumSet;

import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * An enum that describes the type of dependency.
 * <p>
 * Next to the type of the dependencies this enum specifies the types which should be considered
 * when searching for transitive dependencies.
 * 
 */
public enum DependencyType {

    INSTANCEOF("instance of dependency") { //$NON-NLS-1$
        @Override
        public EnumSet<DependencyType> getTransitiveTypes(IIpsProject ipsProject) {
            return EnumSet.of(INSTANCEOF);
        }
    },

    SUBTYPE("subtype dependency") { //$NON-NLS-1$
        @Override
        public EnumSet<DependencyType> getTransitiveTypes(IIpsProject ipsProject) {
            return EnumSet.allOf(DependencyType.class);
        }
    },

    /**
     * The configuration dependency is used to specify the dependency between policy and product. A
     * policy component type is configured by a product component type. Hence this dependency type
     * has a policy component type as source object and a product component type as target object.
     */
    CONFIGUREDBY("configured by dependency") { //$NON-NLS-1$
        @Override
        public EnumSet<DependencyType> getTransitiveTypes(IIpsProject ipsProject) {
            return EnumSet.of(SUBTYPE, INSTANCEOF);
        }
    },

    /**
     * The configuration dependency is used to specify the dependency between product and policy. A
     * product component type configures a policy component type. Hence this dependency type has a
     * product component type as source object and a policy component type as target object.
     */
    CONFIGURES("configures dependency") { //$NON-NLS-1$
        @Override
        public EnumSet<DependencyType> getTransitiveTypes(IIpsProject ipsProject) {
            return EnumSet.of(SUBTYPE, INSTANCEOF);
        }
    },

    REFERENCE_COMPOSITION_MASTER_DETAIL("master to detail composition dependency") { //$NON-NLS-1$
        @Override
        public EnumSet<DependencyType> getTransitiveTypes(IIpsProject ipsProject) {
            boolean containsAggregateRootBuilder = ipsProject.getIpsArtefactBuilderSet().containsAggregateRootBuilder();
            if (containsAggregateRootBuilder) {
                return EnumSet.of(REFERENCE_COMPOSITION_MASTER_DETAIL);
            } else {
                return EnumSet.noneOf(DependencyType.class);
            }
        }
    },

    REFERENCE("reference dependency") { //$NON-NLS-1$
        @Override
        public EnumSet<DependencyType> getTransitiveTypes(IIpsProject ipsProject) {
            return EnumSet.noneOf(DependencyType.class);
        }
    },

    DATATYPE("datatype dependency") { //$NON-NLS-1$
        @Override
        public EnumSet<DependencyType> getTransitiveTypes(IIpsProject ipsProject) {
            return EnumSet.of(SUBTYPE, CONFIGURES, INSTANCEOF);
        }
    },

    /**
     * Dependency type for objects that normally have no relation to each other, in the sense of
     * references in a model, but require validating if one of them changes.
     * <p>
     * Note that this type of dependency is <em>only</em> used in <em>error cases</em>. Objects are
     * invalid but <em>become valid</em> if one of them changes (thus the re-validation). In the
     * opposite case, where objects <em>become invalid</em> if one of them changes, "natural"
     * dependencies exist, e.g. references.
     * <p>
     * For example this type is used for table contents of single-table structures. If there are two
     * table contents, both have errors. If one of them is deleted the other must be re-validated to
     * become error free.
     */
    VALIDATION("validation dependency") { //$NON-NLS-1$
        @Override
        public EnumSet<DependencyType> getTransitiveTypes(IIpsProject ipsProject) {
            return EnumSet.noneOf(DependencyType.class);
        }
    };

    private String name;

    DependencyType(String name) {
        this.name = name;
    }

    /**
     * Returns the set of {@link DependencyType} that should be considered when searching for
     * transitive dependencies. While searching for transitive dependencies, the dependency resolver
     * will always use the intersection of the previous transitive types and the current transitive
     * types. For example: First a dependency of type {@link #DATATYPE} was found. This dependency
     * type returns {@link #SUBTYPE}, {@link #CONFIGURES} and {@link #INSTANCEOF} as transitive
     * types. For further search only these types of dependencies are considered. In the next step a
     * dependency of type {@link #CONFIGURES} was found. Hence only {@link #SUBTYPE} and
     * {@link #INSTANCEOF} dependencies will be considered. In case of a dependency of type
     * {@link #SUBTYPE} the next step will also consider only {@link #SUBTYPE} and
     * {@link #INSTANCEOF} because of the intersection of the two transitive type sets.
     * <p>
     * To get the transitive types of the next search simply call
     * {@link #getNextTransitiveTypes(EnumSet, IIpsProject)} with the enum set of the current
     * transitive types.
     * 
     * @param ipsProject The project is used in some special cases where the transitive types
     *            depends on the project's configuration.
     * @return The dependency types that should be considered while searching for transitive
     *             dependencies
     */
    public abstract EnumSet<DependencyType> getTransitiveTypes(IIpsProject ipsProject);

    /**
     * This method returns the intersect of the transitive types returned by this type and the
     * current transitive types specified by the parameter.
     * 
     * @param currentTransitiveTypes The current transitive types from previous search steps
     * @param ipsProject The current {@link IIpsProject} for special cases where transitive types
     *            depends on the project's configuration.
     * @return The enum set with the dependency types that should be considered in next transitive
     *             search step
     */
    public EnumSet<DependencyType> getNextTransitiveTypes(EnumSet<DependencyType> currentTransitiveTypes,
            IIpsProject ipsProject) {
        EnumSet<DependencyType> nextTransitiveTypes = EnumSet.copyOf(currentTransitiveTypes);
        nextTransitiveTypes.retainAll(getTransitiveTypes(ipsProject));
        return nextTransitiveTypes;
    }

    @Override
    public String toString() {
        return name;
    }

}
