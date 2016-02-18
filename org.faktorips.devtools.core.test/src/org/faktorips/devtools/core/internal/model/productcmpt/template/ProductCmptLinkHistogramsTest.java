/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.productcmpt.template;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import com.google.common.collect.Lists;

import org.faktorips.devtools.core.internal.model.productcmpt.Cardinality;
import org.faktorips.devtools.core.internal.model.productcmpt.IProductCmptLinkContainer;
import org.faktorips.devtools.core.internal.model.productcmpt.template.ProductCmptLinkHistograms.LinkIdentifier;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.junit.Test;

public class ProductCmptLinkHistogramsTest {

    private final Cardinality c010 = new Cardinality(0, 1, 0);
    private final Cardinality c111 = new Cardinality(1, 1, 1);
    private final IProductCmptLink[] NO_LINKS = new IProductCmptLink[0];

    @Test
    public void testCreateFor_NoLinks() {
        IProductCmpt prod = mockProductCmpt(NO_LINKS);
        mockProductCmptGeneration(prod, NO_LINKS);
        ProductCmptLinkHistograms p = ProductCmptLinkHistograms.createFor(Lists.newArrayList(prod));
        assertThat(p.size(), is(0));
    }

    @Test
    public void testCreateFor_SingleProductCmpt() {
        IProductCmptLink aToT = mockLink("a", "t", c010);
        IProductCmptLink bToU = mockLink("b", "u", c111);

        LinkIdentifier aToTId = LinkIdentifier.createFor(aToT);
        LinkIdentifier bToUId = LinkIdentifier.createFor(bToU);

        IProductCmpt prod = mockProductCmpt(aToT, bToU);
        mockProductCmptGeneration(prod, aToT, bToU);

        ProductCmptLinkHistograms p = ProductCmptLinkHistograms.createFor(Lists.newArrayList(prod));
        assertThat(p.size(), is(2));

        assertThat(p.getHistogram(aToTId), is(notNullValue()));
        assertThat(p.getHistogram(aToTId).countElements(), is(1));
        assertThat(p.getHistogram(aToTId).getElements(c010), hasItem(aToT));

        assertThat(p.getHistogram(bToUId), is(notNullValue()));
        assertThat(p.getHistogram(bToUId).countElements(), is(1));
        assertThat(p.getHistogram(bToUId).getElements(c111), hasItem(bToU));

        // No links with that cardinality
        assertThat(p.getHistogram(aToTId).getElements(new Cardinality(9, 9, 9)).size(), is(0));
        assertThat(p.getHistogram(bToUId).getElements(new Cardinality(9, 9, 9)).size(), is(0));

        // No links with that association/target
        assertThat(p.getHistogram(LinkIdentifier.createFor("x", "y")), is(nullValue()));
    }

    @Test
    public void testCreateFor_ProductCmptLinks() {
        IProductCmptLink pToT1 = mockLink("p", "t", c010);
        IProductCmptLink pToU1 = mockLink("p", "u", c010);
        IProductCmptLink pToT2 = mockLink("p", "t", c111);
        IProductCmptLink pToU2 = mockLink("p", "u", c111);

        LinkIdentifier pToTId = LinkIdentifier.createFor("p", "t");
        LinkIdentifier pToUId = LinkIdentifier.createFor("p", "u");

        IProductCmpt prod1 = mockProductCmpt(pToT1, pToU1);
        mockProductCmptGeneration(prod1, NO_LINKS);
        IProductCmpt prod2 = mockProductCmpt(pToT2, pToU2);
        // Add a link to the generation. The link should be ignored
        mockProductCmptGeneration(prod2, mockLink("x", "y", c111));

        ProductCmptLinkHistograms p = ProductCmptLinkHistograms.createFor(Lists.newArrayList(prod1, prod2));

        assertThat(p.size(), is(2));
        assertThat(p.getHistogram(pToTId).countElements(), is(2));
        assertThat(p.getHistogram(pToTId).getElements(c010), hasItem(pToT1));
        assertThat(p.getHistogram(pToTId).getElements(c111), hasItem(pToT2));

        assertThat(p.getHistogram(pToUId).countElements(), is(2));
        assertThat(p.getHistogram(pToUId).getElements(c010), hasItem(pToU1));
        assertThat(p.getHistogram(pToUId).getElements(c111), hasItem(pToU2));

        assertThat(p.getHistogram(LinkIdentifier.createFor("x", "y")), is(nullValue()));
    }

    @Test
    public void testCreateFor_GenerationLinks() {
        IProductCmptLink gToT1 = mockLink("g", "t", c010);
        IProductCmptLink gToT2 = mockLink("g", "t", c111);

        LinkIdentifier gToTId = LinkIdentifier.createFor("g", "t");

        IProductCmptGeneration gen1 = mockProductCmptGeneration(mockProductCmpt(NO_LINKS), gToT1);
        IProductCmptGeneration gen2 = mockProductCmptGeneration(mockProductCmpt(NO_LINKS), gToT2);

        ProductCmptLinkHistograms p = ProductCmptLinkHistograms.createFor(Lists.newArrayList(gen1, gen2));

        assertThat(p.size(), is(1));
        assertThat(p.getHistogram(gToTId), is(notNullValue()));
        assertThat(p.getHistogram(gToTId).countElements(), is(2));
        assertThat(p.getHistogram(gToTId).getElements(c010), hasItem(gToT1));
        assertThat(p.getHistogram(gToTId).getElements(c111), hasItem(gToT2));
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
        when(c.getLinksAsList()).thenReturn(Arrays.asList(links));
    }

    private IProductCmptGeneration mockProductCmptGeneration(IProductCmpt cmpt, IProductCmptLink... links) {
        IProductCmptGeneration gen = mock(IProductCmptGeneration.class);
        addMockedLinks(gen, links);
        when(cmpt.getLatestProductCmptGeneration()).thenReturn(gen);
        return gen;
    }
}
