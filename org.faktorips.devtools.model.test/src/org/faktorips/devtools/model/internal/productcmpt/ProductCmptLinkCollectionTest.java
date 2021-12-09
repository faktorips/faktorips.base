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

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus;
import org.junit.Before;
import org.junit.Test;

public class ProductCmptLinkCollectionTest extends AbstractIpsPluginTest {

    private ProductCmptLinkCollection linkCollection;
    private IProductCmptLink link1;
    private IProductCmptLink link2;
    private IProductCmptLink link3;
    private IProductCmptLink link4;
    private IProductCmptLink link5;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        linkCollection = new ProductCmptLinkCollection();
    }

    private void setUpCollectionWithLinks() {
        link1 = mock(IProductCmptLink.class);
        link2 = mock(IProductCmptLink.class);
        link3 = mock(IProductCmptLink.class);
        link4 = mock(IProductCmptLink.class);
        link5 = mock(IProductCmptLink.class);
        // deliberate reverse lexicographical order
        setUpAssociationAndID(link1, "oneAssociation", "id1");
        setUpAssociationAndID(link2, "anotherAssociation", "id2");
        setUpAssociationAndID(link3, "oneAssociation", "id3");
        setUpAssociationAndID(link4, "anotherAssociation", "id4");
        setUpAssociationAndID(link5, "yetAnotherAssociation", "id5");

        linkCollection.addLink(link1);
        linkCollection.addLink(link2);
        linkCollection.addLink(link3);
        linkCollection.addLink(link4);
        linkCollection.addLink(link5);

        assertEquals(5, linkCollection.size());
    }

    private void setUpAssociationAndID(IProductCmptLink link, String assoc, String id) {
        when(link.getAssociation()).thenReturn(assoc);
        when(link.getId()).thenReturn(id);
    }

    @Test
    public void testLinkOrder_afterLinkChange() {
        setUpCollectionWithLinks();
        when(link1.getAssociation()).thenReturn("anotherAssociation");

        List<IProductCmptLink> links = linkCollection.getLinks();
        assertEquals(link1, links.get(0));
        assertEquals(link2, links.get(1));
        assertEquals(link4, links.get(2));
        assertEquals(link3, links.get(3));
        assertEquals(link5, links.get(4));

    }

    @Test
    public void testLinkOrder_afterLinkChange2() {
        setUpCollectionWithLinks();
        when(link5.getAssociation()).thenReturn("oneAssociation");

        List<IProductCmptLink> links = linkCollection.getLinks();
        assertEquals(link1, links.get(0));
        assertEquals(link3, links.get(1));
        assertEquals(link5, links.get(2));
        assertEquals(link2, links.get(3));
        assertEquals(link4, links.get(4));
    }

    @Test
    public void testGetLinks_EmptyCollection() {
        List<IProductCmptLink> links = new ProductCmptLinkCollection().getLinks();
        assertNotNull(links);
        assertTrue(links.isEmpty());
    }

    @Test
    public void testGetLinks() {
        setUpCollectionWithLinks();

        List<IProductCmptLink> links = linkCollection.getLinks();
        assertEquals(link1, links.get(0));
        assertEquals(link3, links.get(1));
        assertEquals(link2, links.get(2));
        assertEquals(link4, links.get(3));
        assertEquals(link5, links.get(4));
    }

    @Test
    public void testGetLinksAsMap() {
        setUpCollectionWithLinks();
        Map<String, List<IProductCmptLink>> map = linkCollection.getLinksAsMap();
        assertEquals(3, map.size());

        List<IProductCmptLink> linksOne = map.get("oneAssociation");
        assertEquals(link1, linksOne.get(0));
        assertEquals(link3, linksOne.get(1));
        assertEquals(2, linksOne.size());

        List<IProductCmptLink> linksAnother = map.get("anotherAssociation");
        assertEquals(link2, linksAnother.get(0));
        assertEquals(link4, linksAnother.get(1));
        assertEquals(2, linksAnother.size());

        List<IProductCmptLink> linksYetAnother = map.get("yetAnotherAssociation");
        assertEquals(link5, linksYetAnother.get(0));
        assertEquals(1, linksYetAnother.size());
    }

    @Test
    public void testGetLinksAsMap_withNullAssociation() {
        setUpCollectionWithLinks();
        IProductCmptLink linkWithNullAssoc = mock(IProductCmptLink.class);
        setUpAssociationAndID(linkWithNullAssoc, null, "idNullAssoc");
        linkCollection.addLink(linkWithNullAssoc);

        Map<String, List<IProductCmptLink>> map = linkCollection.getLinksAsMap();
        assertEquals(4, map.size());

        List<IProductCmptLink> linksOne = map.get("oneAssociation");
        assertEquals(link1, linksOne.get(0));
        assertEquals(link3, linksOne.get(1));
        assertEquals(2, linksOne.size());

        List<IProductCmptLink> linksAnother = map.get("anotherAssociation");
        assertEquals(link2, linksAnother.get(0));
        assertEquals(link4, linksAnother.get(1));
        assertEquals(2, linksAnother.size());

        List<IProductCmptLink> linksYetAnother = map.get("yetAnotherAssociation");
        assertEquals(link5, linksYetAnother.get(0));
        assertEquals(1, linksYetAnother.size());

        List<IProductCmptLink> linksNullAssoc = map.get(null);
        assertEquals(linkWithNullAssoc, linksNullAssoc.get(0));
        assertEquals(1, linksNullAssoc.size());
    }

    @Test
    public void testGetLinksForAssociation() {
        setUpCollectionWithLinks();
        List<IProductCmptLink> links = linkCollection.getLinks("anotherAssociation");
        assertEquals(2, links.size());
        assertTrue(links.contains(link2));
        assertTrue(links.contains(link4));

        assertEquals(link2, links.get(0));
        assertEquals(link4, links.get(1));
    }

    @Test
    public void testGetLinksForAssociation2() {
        setUpCollectionWithLinks();
        List<IProductCmptLink> links = linkCollection.getLinks("oneAssociation");
        assertEquals(2, links.size());
        assertTrue(links.contains(link1));
        assertTrue(links.contains(link3));

        assertEquals(link1, links.get(0));
        assertEquals(link3, links.get(1));
    }

    @Test
    public void testGetLinksForAssociation3() {
        setUpCollectionWithLinks();
        List<IProductCmptLink> links = linkCollection.getLinks("yetAnotherAssociation");
        assertEquals(1, links.size());
        assertTrue(links.contains(link5));
    }

    @Test
    public void testGetLinksForAssociation_NoAssciations() {
        setUpCollectionWithLinks();
        List<IProductCmptLink> links = linkCollection.getLinks("inexistentAssociation");
        assertNotNull(links);
        assertTrue(links.isEmpty());
    }

    @Test
    public void testCreateAndAddNewLink_addsLink() {
        IProductCmptGeneration container = createContainer();
        linkCollection.createAndAddNewLink(container, "oneAssociation", "id1");
        linkCollection.createAndAddNewLink(container, "anotherAssociation", "id2");
        linkCollection.createAndAddNewLink(container, "oneAssociation", "id3");

        assertEquals(3, linkCollection.size());
    }

    @Test
    public void testCreateAndAddNewLink() {
        IProductCmptGeneration container = createContainer();
        IProductCmptLink newLink = linkCollection.createAndAddNewLink(container, "oneAssociation", "id1");
        assertEquals("id1", newLink.getId());
        assertEquals(container, newLink.getParent());
        assertEquals("oneAssociation", newLink.getAssociation());
    }

    private IProductCmptGeneration createContainer() {
        IIpsProject ipsProject = newIpsProject();
        ProductCmpt productCmpt = newProductCmpt(ipsProject, "ProdCmpt");
        IProductCmptGeneration gen = (IProductCmptGeneration)productCmpt.newGeneration(new GregorianCalendar());
        return gen;
    }

    @Test
    public void testRemove() {
        setUpCollectionWithLinks();
        linkCollection.remove(link3);
        assertEquals(4, linkCollection.size());
        assertFalse(linkCollection.getLinks().contains(link3));
    }

    @Test
    public void testRemoveMultipleTimes() {
        setUpCollectionWithLinks();
        linkCollection.remove(link3);
        assertEquals(4, linkCollection.size());
        assertFalse(linkCollection.getLinks().contains(link3));
        linkCollection.remove(link3);
        assertEquals(4, linkCollection.size());
        assertFalse(linkCollection.getLinks().contains(link3));
    }

    @Test
    public void testRemove_null() {
        setUpCollectionWithLinks();
        linkCollection.remove(null);
        assertEquals(5, linkCollection.size());
        assertTrue(linkCollection.getLinks().contains(link3));
    }

    @Test
    public void testRemove_onEmptyList() {
        linkCollection.remove(link1);
        assertTrue(linkCollection.getLinks().isEmpty());
    }

    @Test
    public void testClear() {
        setUpCollectionWithLinks();
        linkCollection.clear();
        assertEquals(0, linkCollection.size());
    }

    @Test
    public void testContainsLink_existingAssociation() {
        setUpCollectionWithLinks();
        IProductCmptLink linkX = mock(IProductCmptLink.class);
        setUpAssociationAndID(linkX, "oneAssociation", "idX");

        assertFalse(linkCollection.containsLink(linkX));
    }

    @Test
    public void testContainsLink_nonexistentAssociation() {
        setUpCollectionWithLinks();
        IProductCmptLink linkX = mock(IProductCmptLink.class);
        setUpAssociationAndID(linkX, "newAssociation", "idX");

        assertFalse(linkCollection.containsLink(linkX));
    }

    @Test
    public void testContainsLink_equalLink() {
        setUpCollectionWithLinks();
        assertTrue(linkCollection.containsLink(link4));
    }

    @Test
    public void testContainsLink_null() {
        setUpCollectionWithLinks();
        assertFalse(linkCollection.containsLink(null));
    }

    @Test
    public void testContainsLink_nullAssociation() {
        setUpCollectionWithLinks();
        IProductCmptLink linkX = mock(IProductCmptLink.class);
        setUpAssociationAndID(linkX, null, "idX");

        assertFalse(linkCollection.containsLink(linkX));
    }

    @Test
    public void testMoveLink_moveNull() {
        setUpCollectionWithLinks();

        linkCollection.moveLink(null, link3, true);

        List<IProductCmptLink> links = linkCollection.getLinks();
        assertEquals(link1, links.get(0));
        assertEquals(link3, links.get(1));
        assertEquals(link2, links.get(2));
        assertEquals(link4, links.get(3));
        assertEquals(link5, links.get(4));
    }

    @Test
    public void testMoveLink_moveLinkNotInCollection() {
        setUpCollectionWithLinks();
        IProductCmptLink linkNotInCollection = mock(IProductCmptLink.class);
        setUpAssociationAndID(linkNotInCollection, "differentAssociation", "idX");

        linkCollection.moveLink(linkNotInCollection, link3, true);

        List<IProductCmptLink> links = linkCollection.getLinks();
        assertEquals(link1, links.get(0));
        assertEquals(link3, links.get(1));
        assertEquals(link2, links.get(2));
        assertEquals(link4, links.get(3));
        assertEquals(link5, links.get(4));
    }

    @Test
    public void testMoveLink_moveAboveNull() {
        setUpCollectionWithLinks();

        linkCollection.moveLink(link1, null, true);

        List<IProductCmptLink> links = linkCollection.getLinks();
        assertEquals(link1, links.get(0));
        assertEquals(link3, links.get(1));
        assertEquals(link2, links.get(2));
        assertEquals(link4, links.get(3));
        assertEquals(link5, links.get(4));
    }

    @Test
    public void testMoveLink_moveAboveLinkNotInCollection() {
        setUpCollectionWithLinks();
        IProductCmptLink linkNotInCollection = mock(IProductCmptLink.class);
        setUpAssociationAndID(linkNotInCollection, "differentAssociation", "idX");

        linkCollection.moveLink(link1, linkNotInCollection, true);

        List<IProductCmptLink> links = linkCollection.getLinks();
        assertEquals(link1, links.get(0));
        assertEquals(link3, links.get(1));
        assertEquals(link2, links.get(2));
        assertEquals(link4, links.get(3));
        assertEquals(link5, links.get(4));
    }

    @Test
    public void testMoveLink_moveAbove() {
        setUpCollectionWithLinks();

        linkCollection.moveLink(link5, link2, true);

        List<IProductCmptLink> links = linkCollection.getLinks();
        assertEquals(link1, links.get(0));
        assertEquals(link3, links.get(1));
        assertEquals(link5, links.get(2));
        assertEquals(link2, links.get(3));
        assertEquals(link4, links.get(4));
        verify(link5).setAssociation(link2.getAssociation());
    }

    @Test
    public void testMoveLink_moveBelow() {
        setUpCollectionWithLinks();

        linkCollection.moveLink(link1, link4, false);

        List<IProductCmptLink> links = linkCollection.getLinks();
        assertEquals(link2, links.get(0));
        assertEquals(link4, links.get(1));
        assertEquals(link3, links.get(2));
        assertEquals(link1, links.get(3));
        assertEquals(link5, links.get(4));
        verify(link1).setAssociation(link4.getAssociation());
    }

    @Test
    public void testMoveLink_moveAboveFirst() {
        setUpCollectionWithLinks();

        linkCollection.moveLink(link5, link1, true);

        List<IProductCmptLink> links = linkCollection.getLinks();
        assertEquals(link5, links.get(0));
        assertEquals(link1, links.get(1));
        assertEquals(link3, links.get(2));
        assertEquals(link2, links.get(3));
        assertEquals(link4, links.get(4));
        verify(link5).setAssociation(link1.getAssociation());
    }

    @Test
    public void testMoveLink_moveBelowLast() {
        setUpCollectionWithLinks();

        linkCollection.moveLink(link1, link5, false);

        List<IProductCmptLink> links = linkCollection.getLinks();
        assertEquals(link2, links.get(0));
        assertEquals(link4, links.get(1));
        assertEquals(link3, links.get(2));
        assertEquals(link1, links.get(3));
        assertEquals(link5, links.get(4));
        verify(link1).setAssociation(link5.getAssociation());
    }

    @Test
    public void testInsertLink() {
        setUpCollectionWithLinks();
        IProductCmptLink linkToInsert = mock(IProductCmptLink.class);
        setUpAssociationAndID(linkToInsert, "oneAssociation", "idX");
        linkCollection.insertLink(linkToInsert, link3);

        List<IProductCmptLink> links = linkCollection.getLinks();
        assertEquals(6, links.size());

        assertEquals(link1, links.get(0));
        assertEquals(linkToInsert, links.get(1));
        assertEquals(link3, links.get(2));
        assertEquals(link2, links.get(3));
        assertEquals(link4, links.get(4));
        assertEquals(link5, links.get(5));
    }

    /**
     * linkToInsert belongs to a different association than link3. This test shows that it is
     * expected that insert does not necessarily ensure the order of links.
     */
    @Test
    public void testInsertLink_DifferentAssociation() {
        setUpCollectionWithLinks();
        IProductCmptLink linkToInsert = mock(IProductCmptLink.class);
        setUpAssociationAndID(linkToInsert, "differentAssociation", "idX");
        linkCollection.insertLink(linkToInsert, link3);

        List<IProductCmptLink> links = linkCollection.getLinks();
        assertEquals(6, links.size());

        assertEquals(link1, links.get(0));
        assertEquals(link3, links.get(1));
        assertEquals(link2, links.get(2));
        assertEquals(link4, links.get(3));
        assertEquals(linkToInsert, links.get(4));
        assertEquals(link5, links.get(5));
    }

    @Test
    public void testInsertLink_aboveNull() {
        setUpCollectionWithLinks();
        IProductCmptLink linkToInsert = mock(IProductCmptLink.class);
        setUpAssociationAndID(linkToInsert, null, "forthAssociation");
        linkCollection.insertLink(linkToInsert, null);

        List<IProductCmptLink> links = linkCollection.getLinks();
        assertEquals(6, links.size());

        assertEquals(link1, links.get(0));
        assertEquals(link3, links.get(1));
        assertEquals(link2, links.get(2));
        assertEquals(link4, links.get(3));
        assertEquals(link5, links.get(4));
        assertEquals(linkToInsert, links.get(5));
    }

    @Test
    public void testInsertLink_aboveNotInCollection() {
        setUpCollectionWithLinks();
        IProductCmptLink linkToInsert = mock(IProductCmptLink.class);
        setUpAssociationAndID(linkToInsert, null, "forthAssociation");
        IProductCmptLink linkNotInCollection = mock(IProductCmptLink.class);
        linkCollection.insertLink(linkToInsert, linkNotInCollection);

        List<IProductCmptLink> links = linkCollection.getLinks();
        assertEquals(6, links.size());

        assertEquals(link1, links.get(0));
        assertEquals(link3, links.get(1));
        assertEquals(link2, links.get(2));
        assertEquals(link4, links.get(3));
        assertEquals(link5, links.get(4));
        assertEquals(linkToInsert, links.get(5));
    }

    @Test
    public void testAddLink_allowDuplicateLinks() {
        setUpCollectionWithLinks();
        boolean linkAdded = linkCollection.addLink(link1);
        assertTrue(linkAdded);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddLink_disallowNull() {
        linkCollection.addLink(null);
    }

    @Test
    public void testSizeEmpty() {
        assertEquals(0, linkCollection.size());
    }

    @Test
    public void testSize() {
        setUpCollectionWithLinks();
        assertEquals(5, linkCollection.size());
    }

    @Test
    public void testRemoveUndefinedLinks() {
        setUpCollectionWithLinks();
        when(link1.getTemplateValueStatus()).thenReturn(TemplateValueStatus.UNDEFINED);
        when(link2.getTemplateValueStatus()).thenReturn(TemplateValueStatus.DEFINED);
        when(link3.getTemplateValueStatus()).thenReturn(TemplateValueStatus.INHERITED);
        when(link4.getTemplateValueStatus()).thenReturn(TemplateValueStatus.UNDEFINED);

        linkCollection.removeUndefinedLinks();
        assertThat(linkCollection.size(), is(3));
        assertThat(linkCollection.getLinks(), hasItems(link2, link3, link5));
    }

    @Test
    public void testRemoveUndefinedLinks_NoLinks() {
        assertThat(linkCollection.size(), is(0));
        linkCollection.removeUndefinedLinks();
        assertThat(linkCollection.size(), is(0));
    }

    @Test
    public void testRemoveUndefinedLinks_NoUndefinedLinks() {
        setUpCollectionWithLinks();
        when(link1.getTemplateValueStatus()).thenReturn(TemplateValueStatus.DEFINED);
        when(link2.getTemplateValueStatus()).thenReturn(TemplateValueStatus.DEFINED);
        when(link3.getTemplateValueStatus()).thenReturn(TemplateValueStatus.DEFINED);
        when(link4.getTemplateValueStatus()).thenReturn(null);
        when(link5.getTemplateValueStatus()).thenReturn(null);

        linkCollection.removeUndefinedLinks();
        assertThat(linkCollection.size(), is(5));
        assertThat(linkCollection.getLinks(), hasItems(link1, link2, link3, link4, link5));
    }
}
