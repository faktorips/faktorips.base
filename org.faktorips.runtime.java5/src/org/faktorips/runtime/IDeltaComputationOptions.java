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

package org.faktorips.runtime;

/**
 * Callback interface for the delta computation.
 * 
 * <p>
 * <strong> The delta support is experimental in this version. The API might change without notice
 * until it is finalized in a future version. </strong>
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
     * computed. position 0: version A:c0, version B:c0 => empty delta position 1: version A:c1,
     * version B:cNew => changed position 2: version A:c2, version B:c1 => changed position 3:
     * version A:none, version B:c2 => added
     * <p>
     * If this method returns {@link ComputationMethod#BY_OBJECT}, the following deltas are
     * computed. c0: version A:position 0, version B:position 0 => empty delta c1: version
     * A:position 1, version B:position 2 => moved c2: version A:position 2, version B:position 3 =>
     * moved cNew: version A:position none, version B:position 1 => added
     * <p>
     * <p>
     * 2. Example for a 1-many association:<br>
     * Same scenario as above, but now c1 is removed and no coverage is added.
     * <p>
     * If this method returns {@link ComputationMethod#BY_POSITION}, the following deltas are
     * computed. position 0: version A:c0, version B:c0 => empty delta position 1: version A:c1,
     * version B:v2 => changed position 2: version A:c2, version B:none => removed
     * <p>
     * If this method returns {@link ComputationMethod#BY_OBJECT}, the following deltas are
     * computed. c0: version A:position 0, version B:position 0 => empty delta c1: version
     * A:position 1, version B:position none => removed c2: version A:position 2, version B:position
     * 1 => moved
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

}
