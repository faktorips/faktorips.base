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

import static org.faktorips.abstracttest.matcher.Matchers.hasValue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.SequencedSet;

import org.faktorips.devtools.model.productcmpt.IPolicyCmptLinkCardinality;
import org.faktorips.devtools.model.productcmpt.IProductCmptLinkContainer;
import org.junit.Before;
import org.junit.Test;

public class PolicyCmptLinkCardinalityCollectionTest {

    private PolicyCmptLinkCardinalityCollection collection;

    @Before
    public void setUp() {
        collection = new PolicyCmptLinkCardinalityCollection(mock(IProductCmptLinkContainer.class));
    }

    @Test
    public void testAdd_SameInstanceTwice_ReturnsFalseAndKeepsSizeOne() {
        IPolicyCmptLinkCardinality cardinality = cardinalityFor("Alpha");
        collection.add(cardinality);

        boolean addedSecondTime = collection.add(cardinality);

        assertThat(addedSecondTime, is(false));
        assertThat(collection.size(), is(1));
    }

    @Test
    public void testAdd_BlankAssociation_Throws() {
        IPolicyCmptLinkCardinality cardinalityWithoutAssociation = cardinalityFor(" ");

        assertThrows(IllegalArgumentException.class, () -> collection.add(cardinalityWithoutAssociation));
    }

    @Test
    public void testAdd_NullAssociation_Throws() {
        IPolicyCmptLinkCardinality cardinalityWithoutAssociation = cardinalityFor(null);

        assertThrows(IllegalArgumentException.class, () -> collection.add(cardinalityWithoutAssociation));
    }

    @Test
    public void testRemove_UnknownInstance_ReturnsFalse() {
        collection.add(cardinalityFor("Alpha"));

        boolean removed = collection.remove(cardinalityFor("Alpha"));

        assertThat(removed, is(false));
        assertThat(collection.size(), is(1));
    }

    @Test
    public void testRemove_KnownInstance_ReturnsTrue() {
        IPolicyCmptLinkCardinality cardinality = cardinalityFor("Alpha");
        collection.add(cardinality);

        boolean removed = collection.remove(cardinality);

        assertThat(removed, is(true));
        assertThat(collection.size(), is(0));
    }

    @Test
    public void testGetAll_SameAssociationKeepsInsertionOrder() {
        IPolicyCmptLinkCardinality firstAlpha = cardinalityFor("Alpha");
        IPolicyCmptLinkCardinality secondAlpha = cardinalityFor("Alpha");
        IPolicyCmptLinkCardinality beta = cardinalityFor("Beta");
        collection.add(beta);
        collection.add(firstAlpha);
        collection.add(secondAlpha);

        List<IPolicyCmptLinkCardinality> all = List.copyOf(collection.getAll());

        assertThat(all, contains(firstAlpha, secondAlpha, beta));
    }

    @Test
    public void testGetAll_AssociationBecomesNullAfterAdd_ComesFirst() {
        IPolicyCmptLinkCardinality alpha = cardinalityFor("Alpha");
        IPolicyCmptLinkCardinality associationRemovedLater = cardinalityFor("Beta");
        collection.add(alpha);
        collection.add(associationRemovedLater);
        when(associationRemovedLater.getAssociation()).thenReturn(null);

        List<IPolicyCmptLinkCardinality> all = List.copyOf(collection.getAll());

        assertThat(all, contains(associationRemovedLater, alpha));
    }

    @Test
    public void testClear_RemovesAllCardinalities() {
        collection.add(cardinalityFor("Alpha"));
        collection.add(cardinalityFor("Beta"));

        collection.clear();

        assertThat(collection.size(), is(0));
        assertThat(collection.getAll(), hasSize(0));
    }

    @Test
    public void testSize_CountsCardinalitiesWithSameAssociation() {
        collection.add(cardinalityFor("Alpha"));
        collection.add(cardinalityFor("Alpha"));

        assertThat(collection.size(), is(2));
    }

    @Test
    public void testGetPolicyCmptLinkCardinality_SameAssociationTwice_ReturnsFirstInserted() {
        IPolicyCmptLinkCardinality firstAlpha = cardinalityFor("Alpha");
        IPolicyCmptLinkCardinality secondAlpha = cardinalityFor("Alpha");
        collection.add(firstAlpha);
        collection.add(secondAlpha);

        Optional<IPolicyCmptLinkCardinality> found = collection.getPolicyCmptLinkCardinality("Alpha");

        assertThat(found, hasValue(firstAlpha));
    }

    @Test
    public void testGetPolicyCmptLinkCardinality_UnknownAssociation_ReturnsEmpty() {
        collection.add(cardinalityFor("Alpha"));

        Optional<IPolicyCmptLinkCardinality> found = collection.getPolicyCmptLinkCardinality("Beta");

        assertThat(found.isPresent(), is(false));
    }

    @Test
    public void testGetAll_AddAfterwards_PreviouslyReturnedSetIsUnchanged() {
        collection.add(cardinalityFor("Alpha"));
        SequencedSet<IPolicyCmptLinkCardinality> snapshot = collection.getAll();

        collection.add(cardinalityFor("Beta"));

        assertThat(snapshot, hasSize(1));
        assertThat(collection.getAll(), hasSize(2));
    }

    @Test
    public void testGetAll_RemoveAfterwards_PreviouslyReturnedSetIsUnchanged() {
        IPolicyCmptLinkCardinality alpha = cardinalityFor("Alpha");
        collection.add(alpha);
        SequencedSet<IPolicyCmptLinkCardinality> snapshot = collection.getAll();

        collection.remove(alpha);

        assertThat(snapshot, hasSize(1));
        assertThat(collection.getAll(), hasSize(0));
    }

    @Test
    public void testGetAll_ReturnedSetIsUnmodifiable() {
        SequencedSet<IPolicyCmptLinkCardinality> all = collection.getAll();
        IPolicyCmptLinkCardinality alpha = cardinalityFor("Alpha");

        assertThrows(UnsupportedOperationException.class, () -> all.add(alpha));
    }

    private static IPolicyCmptLinkCardinality cardinalityFor(String association) {
        IPolicyCmptLinkCardinality cardinality = mock(IPolicyCmptLinkCardinality.class);
        when(cardinality.getAssociation()).thenReturn(association);
        return cardinality;
    }
}
