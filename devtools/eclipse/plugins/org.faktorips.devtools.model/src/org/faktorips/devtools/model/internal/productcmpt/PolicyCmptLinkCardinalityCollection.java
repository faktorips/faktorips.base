/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.SequencedSet;
import java.util.SortedSet;
import java.util.TreeSet;

import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.productcmpt.IPolicyCmptLinkCardinality;
import org.faktorips.devtools.model.productcmpt.IProductCmptLinkContainer;
import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * A collection of {@link IPolicyCmptLinkCardinality policy component cardinalities}, to reduce code
 * duplication in {@link ProductCmpt} and {@link ProductCmptGeneration}.
 */
class PolicyCmptLinkCardinalityCollection {

    private final SortedSet<IPolicyCmptLinkCardinality> policyCmptLinkCardinalities = Collections
            .synchronizedSortedSet(
                    new TreeSet<>(
                            (o1, o2) -> nullsFirst(comparing(IPolicyCmptLinkCardinality::getAssociation,
                                    nullsFirst(naturalOrder())))
                                            .thenComparing(IPolicyCmptLinkCardinality::getCardinality,
                                                    nullsFirst(o1.getValueComparator()))
                                            .compare(o1, o2)));

    private final IProductCmptLinkContainer linkContainer;

    PolicyCmptLinkCardinalityCollection(IProductCmptLinkContainer linkContainer) {
        this.linkContainer = linkContainer;
    }

    /**
     * Creates a new, uninitialized, {@link IPolicyCmptLinkCardinality policy component cardinality}
     * for this collection's parent with the given part ID.
     *
     * @param partId the part id to be used for the created {@link IPolicyCmptLinkCardinality policy
     *            component cardinality}
     * @return the created {@link IPolicyCmptLinkCardinality policy component cardinality}, added to
     *             this collection.
     */
    IPolicyCmptLinkCardinality newPolicyCmptLinkCardinality(String partId) {
        var linkCardinality = new PolicyCmptLinkCardinality(linkContainer, partId);
        policyCmptLinkCardinalities.add(linkCardinality);
        return linkCardinality;
    }

    /**
     * Creates a new {@link IPolicyCmptLinkCardinality policy component cardinality} for this
     * collection's parent with the given part ID, configured for the given association.
     *
     * @param partId the part id to be used for the created {@link IPolicyCmptLinkCardinality policy
     *            component cardinality}
     * @param policyAssociationName the name of a {@link IPolicyCmptTypeAssociation} marked as
     *            {@link IPolicyCmptTypeAssociation#isCardinalityConfigurable() cardinality
     *            configurable}.
     * @return the created {@link IPolicyCmptLinkCardinality policy component cardinality}, added to
     *             this collection.
     */
    IPolicyCmptLinkCardinality newPolicyCmptLinkCardinality(String partId, String policyAssociationName) {
        var linkCardinality = new PolicyCmptLinkCardinality(linkContainer, partId, policyAssociationName);
        policyCmptLinkCardinalities.add(linkCardinality);
        return linkCardinality;
    }

    /**
     * Returns the {@link IPolicyCmptLinkCardinality policy component cardinality} for the given
     * association name as an {@link Optional} or an empty {@link Optional}, if the given name does
     * not refer to an {@link IPolicyCmptTypeAssociation} marked as
     * {@link IPolicyCmptTypeAssociation#isCardinalityConfigurable() cardinality configurable}.
     *
     * @param policyAssociationName the name (=target role singular) of the association to return
     *            the cardinality configuration for
     */
    Optional<IPolicyCmptLinkCardinality> getPolicyCmptLinkCardinality(String policyAssociationName) {
        return policyCmptLinkCardinalities.stream()
                .filter(c -> Objects.equals(policyAssociationName, c.getAssociation())).findFirst();
    }

    /**
     * Returns all {@link IPolicyCmptLinkCardinality policy component cardinalities}.
     */
    SequencedSet<IPolicyCmptLinkCardinality> getAll() {
        return Collections.unmodifiableSequencedSet(policyCmptLinkCardinalities);
    }

    /**
     * Removes all {@link IPolicyCmptLinkCardinality policy component cardinalities}.
     */
    void clear() {
        policyCmptLinkCardinalities.clear();
    }

    /**
     * Removes the given link cardinality value from this collection.
     *
     * @param policyCmptLinkCardinality the link cardinality to be removed
     * @return {@code true} if the given link cardinality was removed from this collection,
     *             {@code false} otherwise.
     */
    boolean remove(IPolicyCmptLinkCardinality policyCmptLinkCardinality) {
        return policyCmptLinkCardinalities.remove(policyCmptLinkCardinality);
    }

    /**
     * Adds the given link cardinality to this collection.
     *
     * @param policyCmptLinkCardinality the link cardinality to be added
     * @return {@code true} if the link cardinality could be added to this collection, {@code false}
     *             otherwise.
     * @throws NullPointerException if the given link cardinality is {@code null}.
     * @throws IllegalArgumentException if the given link does not belong to any association (when
     *             {@link IPolicyCmptLinkCardinality#getAssociation()} returns {@code null} or an
     *             empty String).
     */
    boolean add(IPolicyCmptLinkCardinality policyCmptLinkCardinality) {
        if (IpsStringUtils.isBlank(policyCmptLinkCardinality.getAssociation())) {
            throw new IllegalArgumentException("Can't add a " + IPolicyCmptLinkCardinality.class.getSimpleName()
                    + " that does not reference an association");
        }
        return policyCmptLinkCardinalities.add(policyCmptLinkCardinality);
    }
}
