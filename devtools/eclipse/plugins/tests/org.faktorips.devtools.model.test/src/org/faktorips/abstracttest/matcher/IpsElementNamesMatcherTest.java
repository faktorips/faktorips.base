/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.abstracttest.matcher;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.faktorips.devtools.model.IIpsElement;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

/**
 * This is the test for {@link IpsElementNamesMatcher}. It is in devtools-core because we do not
 * have a dedicated test source for abstracttest.
 * 
 * @author dirmeier
 */
public class IpsElementNamesMatcherTest {

    private IIpsElement ipsElement1;
    private IIpsElement ipsElement2;
    private IIpsElement ipsElement3;

    @Before
    public void sutUp() {
        ipsElement1 = mock(IIpsElement.class);
        when(ipsElement1.getName()).thenReturn("ipsElement1");
        ipsElement2 = mock(IIpsElement.class);
        when(ipsElement2.getName()).thenReturn("ipsElement2");
        ipsElement3 = mock(IIpsElement.class);
        when(ipsElement3.getName()).thenReturn("ipsElement3");
    }

    @Test
    public void testMatchesSafely_ContainsInOrder_Empty_Empty() throws Exception {
        Matcher<? super Collection<? extends IIpsElement>> matcher = IpsElementNamesMatcher.containsInOrder();

        assertTrue(matcher.matches(Collections.emptyList()));
    }

    @Test
    public void testMatchesSafely_ContainsInOrder_Something_Empty() throws Exception {
        Matcher<? super Collection<? extends IIpsElement>> matcher = IpsElementNamesMatcher.containsInOrder("a", "b",
                "c");

        assertFalse(matcher.matches(Collections.emptyList()));
    }

    @Test
    public void testMatchesSafely_ContainsInOrder_Empty_Something() throws Exception {
        Matcher<? super Collection<? extends IIpsElement>> matcher = IpsElementNamesMatcher.containsInOrder();

        assertFalse(matcher.matches(Arrays.asList(ipsElement1, ipsElement2, ipsElement3)));
    }

    @Test
    public void testMatchesSafely_ContainsInOrder_OtherNames() throws Exception {
        Matcher<? super Collection<? extends IIpsElement>> matcher = IpsElementNamesMatcher.containsInOrder("a", "b",
                "c");

        assertFalse(matcher.matches(Arrays.asList(ipsElement1, ipsElement2, ipsElement3)));
    }

    @Test
    public void testMatchesSafely_ContainsInOrder_Match() throws Exception {
        Matcher<? super Collection<? extends IIpsElement>> matcher = IpsElementNamesMatcher
                .containsInOrder("ipsElement1", "ipsElement2", "ipsElement3");

        assertTrue(matcher.matches(Arrays.asList(ipsElement1, ipsElement2, ipsElement3)));
    }

    @Test
    public void testMatchesSafely_ContainsInOrder_Partly() throws Exception {
        Matcher<? super Collection<? extends IIpsElement>> matcher = IpsElementNamesMatcher
                .containsInOrder("ipsElement1", "ipsElement2");

        assertFalse(matcher.matches(Arrays.asList(ipsElement1, ipsElement2, ipsElement3)));
    }

    @Test
    public void testMatchesSafely_ContainsInOrder_OneMore() throws Exception {
        Matcher<? super Collection<? extends IIpsElement>> matcher = IpsElementNamesMatcher
                .containsInOrder("ipsElement1", "ipsElement2", "ipsElement3");

        assertFalse(matcher.matches(Arrays.asList(ipsElement1, ipsElement2)));
    }

    @Test
    public void testMatchesSafely_ContainsInOrder_MatchDuplicateElement() throws Exception {
        Matcher<? super Collection<? extends IIpsElement>> matcher = IpsElementNamesMatcher
                .containsInOrder("ipsElement1", "ipsElement1", "ipsElement2");

        assertTrue(matcher.matches(Arrays.asList(ipsElement1, ipsElement1, ipsElement2)));
    }

    @Test
    public void testMatchesSafely_ContainsInOrder_WrongOrder() throws Exception {
        Matcher<? super Collection<? extends IIpsElement>> matcher = IpsElementNamesMatcher
                .containsInOrder("ipsElement2", "ipsElement1", "ipsElement3");

        assertFalse(matcher.matches(Arrays.asList(ipsElement1, ipsElement2, ipsElement3)));
    }

    @Test
    public void testMatchesSafely_ContainsPartInOrder_Empty_Empty() throws Exception {
        Matcher<? super Collection<? extends IIpsElement>> matcher = IpsElementNamesMatcher.containsSubsetInOrder();

        assertTrue(matcher.matches(Collections.emptyList()));
    }

    @Test
    public void testMatchesSafely_ContainsPartInOrder_Something_Empty() throws Exception {
        Matcher<? super Collection<? extends IIpsElement>> matcher = IpsElementNamesMatcher.containsSubsetInOrder("a",
                "b", "c");

        assertFalse(matcher.matches(Collections.emptyList()));
    }

    @Test
    public void testMatchesSafely_ContainsPartInOrder_Empty_Something() throws Exception {
        Matcher<? super Collection<? extends IIpsElement>> matcher = IpsElementNamesMatcher.containsSubsetInOrder();

        assertTrue(matcher.matches(Arrays.asList(ipsElement1, ipsElement2, ipsElement3)));
    }

    @Test
    public void testMatchesSafely_ContainsPartInOrder_OtherNames() throws Exception {
        Matcher<? super Collection<? extends IIpsElement>> matcher = IpsElementNamesMatcher.containsSubsetInOrder("a",
                "b", "c");

        assertFalse(matcher.matches(Arrays.asList(ipsElement1, ipsElement2, ipsElement3)));
    }

    @Test
    public void testMatchesSafely_ContainsPartInOrder_Match() throws Exception {
        Matcher<? super Collection<? extends IIpsElement>> matcher = IpsElementNamesMatcher
                .containsSubsetInOrder("ipsElement1", "ipsElement2", "ipsElement3");

        assertTrue(matcher.matches(Arrays.asList(ipsElement1, ipsElement2, ipsElement3)));
    }

    @Test
    public void testMatchesSafely_ContainsPartInOrder_MatchFirstPart() throws Exception {
        Matcher<? super Collection<? extends IIpsElement>> matcher = IpsElementNamesMatcher
                .containsSubsetInOrder("ipsElement1", "ipsElement2");

        assertTrue(matcher.matches(Arrays.asList(ipsElement1, ipsElement2, ipsElement3)));
    }

    @Test
    public void testMatchesSafely_ContainsPartInOrder_MatchMiddlePart() throws Exception {
        Matcher<? super Collection<? extends IIpsElement>> matcher = IpsElementNamesMatcher
                .containsSubsetInOrder("ipsElement2");

        assertTrue(matcher.matches(Arrays.asList(ipsElement1, ipsElement2, ipsElement3)));
    }

    @Test
    public void testMatchesSafely_ContainsPartInOrder_MatchLastPart() throws Exception {
        Matcher<? super Collection<? extends IIpsElement>> matcher = IpsElementNamesMatcher
                .containsSubsetInOrder("ipsElement2", "ipsElement3");

        assertTrue(matcher.matches(Arrays.asList(ipsElement1, ipsElement2, ipsElement3)));
    }

    @Test
    public void testMatchesSafely_ContainsPartInOrder_MatchFirstAndLastPart() throws Exception {
        Matcher<? super Collection<? extends IIpsElement>> matcher = IpsElementNamesMatcher
                .containsSubsetInOrder("ipsElement1", "ipsElement3");

        assertTrue(matcher.matches(Arrays.asList(ipsElement1, ipsElement2, ipsElement3)));
    }

    @Test
    public void testMatchesSafely_ContainsPartInOrder_MatchDuplicateElement() throws Exception {
        Matcher<? super Collection<? extends IIpsElement>> matcher = IpsElementNamesMatcher
                .containsSubsetInOrder("ipsElement1", "ipsElement1", "ipsElement3");

        assertFalse(matcher.matches(Arrays.asList(ipsElement1, ipsElement1, ipsElement2)));
    }

    @Test
    public void testMatchesSafely_ContainsPartInOrder_NotMatchedDuplicateElement() throws Exception {
        Matcher<? super Collection<? extends IIpsElement>> matcher = IpsElementNamesMatcher
                .containsSubsetInOrder("ipsElement1", "ipsElement1", "ipsElement2");

        assertFalse(matcher.matches(Arrays.asList(ipsElement1, ipsElement2)));
    }

    @Test
    public void testMatchesSafely_ContainsPartInOrder_WrongOrder() throws Exception {
        Matcher<? super Collection<? extends IIpsElement>> matcher = IpsElementNamesMatcher
                .containsSubsetInOrder("ipsElement2", "ipsElement1", "ipsElement3");

        assertFalse(matcher.matches(Arrays.asList(ipsElement1, ipsElement2, ipsElement3)));
    }

    @Test
    public void testMatchesSafely_ContainsPartInOrder_WrongOder2() throws Exception {
        Matcher<? super Collection<? extends IIpsElement>> matcher = IpsElementNamesMatcher
                .containsSubsetInOrder("ipsElement2", "ipsElement1");

        assertFalse(matcher.matches(Arrays.asList(ipsElement1, ipsElement1, ipsElement2)));
    }

    @Test
    public void testMatchesSafely_ContainsPartInOrder_MatchInDuplicate() throws Exception {
        Matcher<? super Collection<? extends IIpsElement>> matcher = IpsElementNamesMatcher
                .containsSubsetInOrder("ipsElement1", "ipsElement2");

        assertTrue(matcher.matches(Arrays.asList(ipsElement1, ipsElement1, ipsElement2)));
    }

}
