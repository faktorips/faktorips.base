/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt.template;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import org.faktorips.devtools.model.internal.productcmpt.template.ProductCmptLinkHistograms.LinkContainerHistogram;
import org.faktorips.devtools.model.productcmpt.Cardinality;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink.LinkIdentifier;
import org.faktorips.devtools.model.productcmpt.IProductCmptLinkContainer;
import org.faktorips.values.Decimal;
import org.junit.Test;

public class ProductCmptLinkHistogramsTest {

    private final Cardinality c010 = new Cardinality(0, 1, 0);
    private final Cardinality c111 = new Cardinality(1, 1, 1);
    private final IProductCmptLink[] NO_LINKS = {};

    @Test
    public void testCreateFor_NoLinks() {
        IProductCmpt prod = mockProductCmpt(NO_LINKS);
        mockProductCmptGeneration(prod, NO_LINKS);
        ProductCmptLinkHistograms p = ProductCmptLinkHistograms.createFor(List.of(prod));
        assertThat(p.size(), is(0));
    }

    @Test
    public void testCreateFor_SingleProductCmpt() {
        IProductCmptLink aToT = mockLink("a", "t", c010);
        IProductCmptLink bToU = mockLink("b", "u", c111);

        IProductCmptLink.LinkIdentifier aToTId = new LinkIdentifier(aToT);
        IProductCmptLink.LinkIdentifier bToUId = new LinkIdentifier(bToU);

        IProductCmpt prod = mockProductCmpt(aToT, bToU);
        mockProductCmptGeneration(prod, aToT, bToU);

        ProductCmptLinkHistograms p = ProductCmptLinkHistograms.createFor(List.of(prod));
        assertThat(p.size(), is(2));

        assertThat(p.getHistogram(aToTId), is(notNullValue()));
        assertThat(p.getHistogram(aToTId).countElements(), is(1));
        assertThat(p.getHistogram(aToTId).getElements(c010), hasItem(prod));

        assertThat(p.getHistogram(bToUId), is(notNullValue()));
        assertThat(p.getHistogram(bToUId).countElements(), is(1));
        assertThat(p.getHistogram(bToUId).getElements(c111), hasItem(prod));

        // No links with that cardinality
        assertThat(p.getHistogram(aToTId).getElements(new Cardinality(9, 9, 9)).size(), is(0));
        assertThat(p.getHistogram(bToUId).getElements(new Cardinality(9, 9, 9)).size(), is(0));

        // No links with that association/target
        assertThat(p.getHistogram(new LinkIdentifier("x", "y")), is(nullValue()));
    }

    @Test
    public void testCreateFor_HistogramHasEntriesForMissingLinks() {
        IProductCmptLink aToT = mockLink("a", "t", c010);
        IProductCmptLink bToU = mockLink("b", "u", c111);

        IProductCmptLink.LinkIdentifier aToTId = new LinkIdentifier(aToT);
        IProductCmptLink.LinkIdentifier bToUId = new LinkIdentifier(bToU);

        IProductCmpt prod1 = mockProductCmpt(aToT);
        IProductCmpt prod2 = mockProductCmpt(bToU);

        ProductCmptLinkHistograms p = ProductCmptLinkHistograms.createFor(List.of(prod1, prod2));
        assertThat(p.size(), is(2));

        assertThat(p.getHistogram(aToTId), is(notNullValue()));
        assertThat(p.getHistogram(aToTId).countElements(), is(2));
        assertThat(p.getHistogram(aToTId).getElements(c010), hasItem(prod1));
        assertThat(p.getHistogram(aToTId).getElements(Cardinality.UNDEFINED), hasItem(prod2));

        assertThat(p.getHistogram(bToUId), is(notNullValue()));
        assertThat(p.getHistogram(bToUId).countElements(), is(2));
        assertThat(p.getHistogram(bToUId).getElements(c111), hasItem(prod2));
        assertThat(p.getHistogram(bToUId).getElements(Cardinality.UNDEFINED), hasItem(prod1));
    }

    @Test
    public void testCreateFor_ProductCmptLinks() {
        IProductCmptLink pToT1 = mockLink("p", "t", c010);
        IProductCmptLink pToU1 = mockLink("p", "u", c010);
        IProductCmptLink pToT2 = mockLink("p", "t", c111);
        IProductCmptLink pToU2 = mockLink("p", "u", c111);

        IProductCmptLink.LinkIdentifier pToTId = new LinkIdentifier("p", "t");
        IProductCmptLink.LinkIdentifier pToUId = new LinkIdentifier("p", "u");

        IProductCmpt prod1 = mockProductCmpt(pToT1, pToU1);
        mockProductCmptGeneration(prod1, NO_LINKS);
        IProductCmpt prod2 = mockProductCmpt(pToT2, pToU2);
        // Add a link to the generation. The link should be ignored
        mockProductCmptGeneration(prod2, mockLink("x", "y", c111));

        ProductCmptLinkHistograms p = ProductCmptLinkHistograms.createFor(List.of(prod1, prod2));

        assertThat(p.size(), is(2));
        assertThat(p.getHistogram(pToTId).countElements(), is(2));
        assertThat(p.getHistogram(pToTId).getElements(c010), hasItem(prod1));
        assertThat(p.getHistogram(pToTId).getElements(c111), hasItem(prod2));

        assertThat(p.getHistogram(pToUId).countElements(), is(2));
        assertThat(p.getHistogram(pToUId).getElements(c010), hasItem(prod1));
        assertThat(p.getHistogram(pToUId).getElements(c111), hasItem(prod2));

        assertThat(p.getHistogram(new LinkIdentifier("x", "y")), is(nullValue()));
    }

    @Test
    public void testCreateFor_GenerationLinks() {
        IProductCmptLink gToT1 = mockLink("g", "t", c010);
        IProductCmptLink gToT2 = mockLink("g", "t", c111);

        IProductCmptLink.LinkIdentifier gToTId = new LinkIdentifier("g", "t");

        IProductCmptGeneration gen1 = mockProductCmptGeneration(mockProductCmpt(NO_LINKS), gToT1);
        IProductCmptGeneration gen2 = mockProductCmptGeneration(mockProductCmpt(NO_LINKS), gToT2);

        ProductCmptLinkHistograms p = ProductCmptLinkHistograms.createFor(List.of(gen1, gen2));

        assertThat(p.size(), is(1));
        assertThat(p.getHistogram(gToTId), is(notNullValue()));
        assertThat(p.getHistogram(gToTId).countElements(), is(2));
        assertThat(p.getHistogram(gToTId).getElements(c010), hasItem(gen1));
        assertThat(p.getHistogram(gToTId).getElements(c111), hasItem(gen2));
    }

    @Test
    public void testLinkContainerHistogram() {

        final IProductCmptLinkContainer c1 = mockProductCmpt(NO_LINKS);
        final IProductCmptLinkContainer c2 = mockProductCmpt(NO_LINKS);
        final IProductCmptLinkContainer c3 = mockProductCmpt(NO_LINKS);

        Collection<IProductCmptLinkContainer> elements = List.of(c1, c2, c3);
        Function<IProductCmptLinkContainer, Cardinality> elementToValueFunction = c -> (c == c1 || c == c2)
                ? Cardinality.UNDEFINED
                : c111;
        LinkContainerHistogram h = new LinkContainerHistogram(elementToValueFunction, elements);

        // Undefined cardinality occurs most often
        assertThat(h.getRelativeDistribution().get(Cardinality.UNDEFINED), is(Decimal.valueOf(67, 2)));
        assertThat(h.getRelativeDistribution().get(c111), is(Decimal.valueOf(33, 2)));

        // Cardinality.UNDEFINED cannot be the best value
        assertThat(h.getBestValue(Decimal.valueOf(5, 1)).isPresent(), is(false));
        assertThat(h.getBestValue(Decimal.valueOf(33, 2)).isPresent(), is(true));
        assertThat(h.getBestValue(Decimal.valueOf(33, 2)).getValue(), is(c111));

    }

    private IProductCmptLink mockLink(String association, String target, Cardinality cardinality) {
        IProductCmptLink link = mock(IProductCmptLink.class);
        when(link.getAssociation()).thenReturn(association);
        when(link.getTarget()).thenReturn(target);
        when(link.getCardinality()).thenReturn(cardinality);
        return link;
    }

    private IProductCmpt mockProductCmpt(IProductCmptLink... links) {
        IProductCmpt productCmpt = mock(IProductCmpt.class);
        addMockedLinks(productCmpt, links);
        IProductCmptGeneration productCmptGeneration = mock(IProductCmptGeneration.class);
        when(productCmpt.getLatestProductCmptGeneration()).thenReturn(productCmptGeneration);
        return productCmpt;
    }

    private void addMockedLinks(IProductCmptLinkContainer c, IProductCmptLink... links) {
        for (IProductCmptLink link : links) {
            when(link.getProductCmptLinkContainer()).thenReturn(c);
        }
        when(c.getLinksAsList(anyString())).thenReturn(Arrays.asList(links));
        when(c.getLinksAsList()).thenReturn(Arrays.asList(links));
    }

    private IProductCmptGeneration mockProductCmptGeneration(IProductCmpt cmpt, IProductCmptLink... links) {
        IProductCmptGeneration gen = mock(IProductCmptGeneration.class);
        addMockedLinks(gen, links);
        when(cmpt.getLatestProductCmptGeneration()).thenReturn(gen);
        return gen;
    }
}
