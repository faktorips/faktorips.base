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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItems;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.runtime.data.TestDeckung;
import org.faktorips.runtime.data.TestPolicy;
import org.junit.Test;

public class GenericVisitorSupportTest {

    @Test
    public void testGenericVisitorWithChildren() {
        TestPolicy policy = new TestPolicy();
        TestDeckung d1 = new TestDeckung();
        policy.addTestDeckung(d1);
        TestDeckung d2 = new TestDeckung();
        policy.addTestDeckung(d2);
        TestDeckung d3 = new TestDeckung();
        policy.addTestDeckung(d3);

        List<IModelObject> visited = new ArrayList<>();
        new GenericVisitorSupport(policy).accept(modelObject -> {
            visited.add(modelObject);
            return true;
        });

        assertThat(visited.size(), is(4));
        assertThat(visited, hasItems(policy, d1, d2, d3));
    }

    @Test
    public void testGenericVisitorWithAssociations() {
        TestPolicy policy = new TestPolicy();
        TestDeckung d1 = new TestDeckung();
        policy.addTestDeckung(d1);
        TestDeckung d2 = new TestDeckung();
        policy.addTestDeckung(d2);
        TestDeckung d3 = new TestDeckung();
        d2.setAndereTestDeckung(d3);

        List<IModelObject> visited = new ArrayList<>();
        new GenericVisitorSupport(policy).accept(modelObject -> {
            visited.add(modelObject);
            return true;
        });

        assertThat(visited.size(), is(3));
        assertThat(visited, hasItems(policy, d1, d2));
    }

    @Test
    public void testGenericVisitorWithoutChildren() {
        TestPolicy policy = new TestPolicy();

        List<IModelObject> visited = new ArrayList<>();
        new GenericVisitorSupport(policy).accept(modelObject -> {
            visited.add(modelObject);
            return false;
        });

        assertThat(visited.size(), is(1));
        assertThat(visited, hasItems(policy));
    }

    @Test
    public void testGenericVisitorStoping() {
        TestPolicy policy = new TestPolicy();
        policy.addTestDeckung(new TestDeckung());

        List<IModelObject> visited = new ArrayList<>();
        new GenericVisitorSupport(policy).accept(modelObject -> {
            visited.add(modelObject);
            return false;
        });

        assertThat(visited.size(), is(1));
        assertThat(visited, hasItems(policy));
    }
}
