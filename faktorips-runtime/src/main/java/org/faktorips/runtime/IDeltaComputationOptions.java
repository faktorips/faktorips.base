/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime;

import org.faktorips.runtime.model.type.AssociationKind;

/**
 * Callback interface for the delta computation.
 * 
 * @author Jan Ortmann
 */
public interface IDeltaComputationOptions {

    public enum ComputationMethod {

        /**
         * Constant for the computation by position. See {{@link #getMethod(String)} for more
         * details.
         */
        BY_POSITION,

        /**
         * Constant for the computation by object. See {{@link #getMethod(String)} for more details.
         */
        BY_OBJECT;
    }

    /**
     * Returns if the delta computation computes child deltas per position/index or per object.
     * <p>
     * <strong>1. Example for a 1-many association:</strong> A contract version A has three
     * coverages c0, c1, and c2. A second version B of the same contract has a new coverage cNew
     * inserted between coverage c0 and c1. The coverages c0, c1,and c2 are unchanged except for
     * their position.
     * <p>
     * If this method returns {@link ComputationMethod#BY_POSITION}, the following deltas are
     * computed.
     * <ul>
     * <li>position 0: version A:c0, version B:c0 ⇒ empty delta</li>
     * <li>position 1: version A:c1, version B:cNew ⇒ changed</li>
     * <li>position 2: version A:c2, version B:c1 ⇒ changed</li>
     * <li>position 3: version A:none, version B:c2 ⇒ added</li>
     * </ul>
     * If this method returns {@link ComputationMethod#BY_OBJECT}, the following deltas are
     * computed.
     * <ul>
     * <li>c0: version A:position 0, version B:position 0 ⇒ empty delta</li>
     * <li>c1: version A:position 1, version B:position 2 ⇒ moved</li>
     * <li>c2: version A:position 2, version B:position 3 ⇒ moved</li>
     * <li>cNew: version A:position none, version B:position 1 ⇒ added</li>
     * </ul>
     * <p>
     * <strong>2. Example for a 1-many association:</strong> Same scenario as above, but now c1 is
     * removed and no coverage is added.
     * <p>
     * If this method returns {@link ComputationMethod#BY_POSITION}, the following deltas are
     * computed.
     * <ul>
     * <li>position 0: version A:c0, version B:c0 ⇒ empty delta</li>
     * <li>position 1: version A:c1, version B:v2 ⇒ changed</li>
     * <li>position 2: version A:c2, version B:none ⇒ removed</li>
     * </ul>
     * <p>
     * If this method returns {@link ComputationMethod#BY_OBJECT}, the following deltas are
     * computed.
     * <ul>
     * <li>c0: version A:position 0, version B:position 0 ⇒ empty delta</li>
     * <li>c1: version A:position 1, version B:position none ⇒ removed</li>
     * <li>c2: version A:position 2, version B:position 1 ⇒ moved</li>
     * </ul>
     * <strong>Example for a 1-1 association:</strong> A contract version A has the insured person
     * p0. A second contract version B has the insured person p1.
     * <p>
     * If this method returns {@link ComputationMethod#BY_POSITION}, the following delta is
     * computed.
     * <ul>
     * <li>position 0: changed</li>
     * </ul>
     * If this method returns {@link ComputationMethod#BY_OBJECT}, the following deltas are
     * computed.
     * <ul>
     * <li>p0: removed</li>
     * <li>p1: added</li>
     * </ul>
     */
    ComputationMethod getMethod(String association);

    /**
     * Returns <code>true</code> if the two objects represent the same conceptual object, otherwise
     * <code>false</code>. What the conceptual object is, depends on the use case in that you need
     * the delta computation and on your object model.
     * <p>
     * If the objects are for example contract versions, they represent the same contract, just at
     * different point in times. In this case the check has to compare the contract numbers but
     * ignore the version numbers. If the objects are coverages you might consider them the same, if
     * they are of the same coverage type, e.g. both are accidental damages coverages.
     */
    boolean isSame(IModelObject object1, IModelObject object2);

    /**
     * Returns <code>true</code> if the given property should be ignored in the delta computation.
     * If you compare for example two versions of the same contract, you might want to ignore the
     * different in the creation time, as otherwise two versions would always be different.
     * 
     * @param clazz The class the property belongs to.
     * @param property The name of the property.
     */
    boolean ignore(Class<?> clazz, String property);

    /**
     * Controls whether delta objects for added or removed subtree elements should be created
     * explicitly.
     * <p>
     * This method returns <code>true</code> if a delta should be created for added or removed
     * subtrees. If this method returns <code>false</code> a delta is only created for the root of
     * an added or removed subtree. For non-composition associations, this property is ignored
     * because such associated objects are never processed recursively.
     * <p>
     * The creation of delta elements for subtree nodes is based on reflection and may cause a
     * performance issue if used in massive delta computation with many added or removed subtrees.
     * 
     * @return <code>true</code> if a delta should be created for every element of an added or
     *             removed subtree.
     * 
     * @since 3.15
     */
    boolean isCreateSubtreeDelta();

    /**
     * Compares two values for equality by the given model class and property, where either one or
     * both values may be <code>null</code>.
     * 
     * @param clazz The class the property belongs to.
     * @param property The name of the property.
     * @param value1 The first value to compare
     * @param value2 The second value to compare
     * @return <code>true</code> if the values are the same
     */
    boolean areValuesEqual(Class<?> clazz, String property, Object value1, Object value2);

    /**
     * Controls whether {@link AssociationKind#Association associations} should be ignored when
     * computing deltas. If set to <code>true</code>, only {@link AssociationKind#Composition
     * parent-to-child relations} will be included.
     * 
     * @return whether {@link AssociationKind#Association associations} should be ignored.
     * 
     * @since 19.12
     */
    boolean ignoreAssociations();

    /**
     * Controls whether {@link IModelObjectDelta#MOVED moved} associations should be ignored when
     * computing deltas. If set to <code>true</code>, moved associations are treated as unchanged as
     * long as they don't contain any other changes.
     * <p>
     * Associations are only considered moved when the {@link #getMethod(String) delta computation
     * method} is set to {@link IDeltaComputationOptions.ComputationMethod#BY_OBJECT BY_OBJECT}.
     * 
     * @return whether {@link IModelObjectDelta#MOVED moved} associations should be ignored.
     * 
     * @since 22.6
     */
    default boolean ignoreMoved() {
        return false;
    }

}
