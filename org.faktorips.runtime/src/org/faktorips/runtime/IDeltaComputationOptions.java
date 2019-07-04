/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime;

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
     * 1. Example for a 1-many association:<br>
     * A contract version A has three coverages c0, c1, and c2. A second version B of the same
     * contract has a new coverage cNew inserted between coverage c0 and c1. The coverages c0,
     * c1,and c2 are unchanged except for their position.
     * <p>
     * If this method returns {@link ComputationMethod#BY_POSITION}, the following deltas are
     * computed. position 0: version A:c0, version B:c0 ⇒ empty delta position 1: version A:c1,
     * version B:cNew ⇒ changed position 2: version A:c2, version B:c1 ⇒ changed position 3: version
     * A:none, version B:c2 ⇒ added
     * <p>
     * If this method returns {@link ComputationMethod#BY_OBJECT}, the following deltas are
     * computed. c0: version A:position 0, version B:position 0 ⇒ empty delta c1: version A:position
     * 1, version B:position 2 ⇒ moved c2: version A:position 2, version B:position 3 ⇒ moved cNew:
     * version A:position none, version B:position 1 ⇒ added
     * <p>
     * <p>
     * 2. Example for a 1-many association:<br>
     * Same scenario as above, but now c1 is removed and no coverage is added.
     * <p>
     * If this method returns {@link ComputationMethod#BY_POSITION}, the following deltas are
     * computed. position 0: version A:c0, version B:c0 ⇒ empty delta position 1: version A:c1,
     * version B:v2 ⇒ changed position 2: version A:c2, version B:none ⇒ removed
     * <p>
     * If this method returns {@link ComputationMethod#BY_OBJECT}, the following deltas are
     * computed. c0: version A:position 0, version B:position 0 ⇒ empty delta c1: version A:position
     * 1, version B:position none ⇒ removed c2: version A:position 2, version B:position 1 ⇒ moved
     * <p>
     * <p>
     * Example for a 1-1 association:<br>
     * A contract version A has the insured person p0. A second contract version B has the insured
     * person p1.
     * <p>
     * If this method returns {@link ComputationMethod#BY_POSITION}, the following delta is
     * computed. position 0: changed
     * <p>
     * If this method returns {@link ComputationMethod#BY_OBJECT}, the following deltas are
     * computed. p1: removed p2: added
     */
    public ComputationMethod getMethod(String association);

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
    public boolean isSame(IModelObject object1, IModelObject object2);

    /**
     * Returns <code>true</code> if the given property should be ignored in the delta computation.
     * If you compare for example two versions of the same contract, you might want to ignore the
     * different in the creation time, as otherwise two versions would always be different.
     * 
     * @param clazz The class the property belongs to.
     * @param property The name of the property.
     */
    public boolean ignore(Class<?> clazz, String property);

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
     *         removed subtree.
     * 
     * @since 3.15
     */
    public boolean isCreateSubtreeDelta();

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

}
