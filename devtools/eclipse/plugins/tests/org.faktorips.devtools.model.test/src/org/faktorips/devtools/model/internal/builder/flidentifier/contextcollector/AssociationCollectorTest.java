/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.builder.flidentifier.contextcollector;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.faktorips.devtools.model.internal.builder.flidentifier.ast.AssociationNode;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.productcmpt.IFormula;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.type.IAssociation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AssociationCollectorTest {

    @Mock
    private AssociationNode node;

    @Mock
    private ContextProductCmptFinder finder;

    @InjectMocks
    private AssociationCollector associationCollector;

    @Mock
    private AbstractProductCmptCollector otherCollector;

    @Mock
    private IAssociation association;

    @Mock
    private IPolicyCmptType policyCmptType;

    @Mock
    private IProductCmptType productCmptType;

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private IProductCmpt productCmpt;

    @Mock
    private IProductCmpt contextProductCmpt1;

    @Mock
    private IProductCmpt contextProductCmpt2;

    @Mock
    private IProductCmptGeneration contextProductCmptGeneration;

    @Mock
    private IProductCmptLink productCmptLink;

    private Collection<IIpsSrcFile> productCmptsFiles;

    @Mock
    private IAssociation matchingAssociation;

    @Mock
    private IFormula expression;

    @Before
    public void setUpFinderAndNode() {
        when(finder.createCollector()).thenReturn(otherCollector);
        when(finder.getIpsProject()).thenReturn(ipsProject);
        when(finder.getExpression()).thenReturn(expression);
        when(node.getAssociation()).thenReturn(association);
    }

    @Before
    public void setUpProductCmpts() throws Exception {
        productCmptsFiles = new ArrayList<>();
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        when(ipsSrcFile.getIpsObject()).thenReturn(productCmpt);
        productCmptsFiles.add(ipsSrcFile);
    }

    @Test
    public void testGetContextProductCmpts_contextNull() throws Exception {
        when(otherCollector.getContextProductCmpts()).thenReturn(null);
        when(association.findTarget(ipsProject)).thenReturn(policyCmptType);
        when(policyCmptType.findProductCmptType(ipsProject)).thenReturn(productCmptType);
        when(productCmptType.searchProductComponents(true)).thenReturn(productCmptsFiles);

        Set<IProductCmpt> contextProductCmpts = associationCollector.getContextProductCmpts();

        assertEquals(1, contextProductCmpts.size());
        assertThat(contextProductCmpts, hasItem(productCmpt));
    }

    @Test
    public void testGetContextProductCmpts_emptyContextNoMatching() throws Exception {
        doReturn(Collections.emptySet()).when(otherCollector).getContextProductCmpts();

        Set<IProductCmpt> contextProductCmpts = associationCollector.getContextProductCmpts();

        assertNull(contextProductCmpts);
    }

    @Test
    public void testGetContextProductCmpts_emptyContext() throws Exception {
        doReturn(Collections.emptySet()).when(otherCollector).getContextProductCmpts();
        when(association.findMatchingAssociation()).thenReturn(matchingAssociation);

        Set<IProductCmpt> contextProductCmpts = associationCollector.getContextProductCmpts();

        assertTrue(contextProductCmpts.isEmpty());
    }

    @Test
    public void testGetContextProductCmpts_withContext_staticLinks() throws Exception {
        Set<IProductCmpt> contextCmpts = createContextProductCmpts();
        when(otherCollector.getContextProductCmpts()).thenReturn(contextCmpts);
        when(association.findMatchingAssociation()).thenReturn(matchingAssociation);
        when(matchingAssociation.getName()).thenReturn("myAssociationName");
        List<IProductCmptLink> links = createLinks();
        when(contextProductCmpt2.getLinksAsList(matchingAssociation.getName())).thenReturn(links);

        Set<IProductCmpt> contextProductCmpts = associationCollector.getContextProductCmpts();

        assertEquals(1, contextProductCmpts.size());
        assertThat(contextProductCmpts, hasItem(productCmpt));
    }

    @Test
    public void testGetContextProductCmpts_withContext_changeOverTimeLinks() throws Exception {
        Set<IProductCmpt> contextCmpts = createContextProductCmpts();
        when(otherCollector.getContextProductCmpts()).thenReturn(contextCmpts);
        when(association.findMatchingAssociation()).thenReturn(matchingAssociation);
        when(matchingAssociation.getName()).thenReturn("myAssociationName");
        List<IProductCmptLink> links = createLinks();
        when(contextProductCmptGeneration.getLinksAsList(matchingAssociation.getName())).thenReturn(links);

        Set<IProductCmpt> contextProductCmpts = associationCollector.getContextProductCmpts();

        assertEquals(1, contextProductCmpts.size());
        assertThat(contextProductCmpts, hasItem(productCmpt));
    }

    @Test
    public void testGetContextProductCmpts_withContext_changeOverTimeLinks_latestGeneration() throws Exception {
        Set<IProductCmpt> contextCmpts = createContextProductCmpts();
        when(otherCollector.getContextProductCmpts()).thenReturn(contextCmpts);
        when(association.findMatchingAssociation()).thenReturn(matchingAssociation);
        when(matchingAssociation.getName()).thenReturn("myAssociationName");
        List<IProductCmptLink> links = createLinks();
        when(contextProductCmptGeneration.getLinksAsList(matchingAssociation.getName())).thenReturn(links);
        when(contextProductCmpt2.getLatestProductCmptGeneration()).thenReturn(contextProductCmptGeneration);
        when(expression.getPropertyValueContainer()).thenReturn(null);

        Set<IProductCmpt> contextProductCmpts = associationCollector.getContextProductCmpts();

        assertEquals(1, contextProductCmpts.size());
        assertThat(contextProductCmpts, hasItem(productCmpt));
    }

    private Set<IProductCmpt> createContextProductCmpts() {
        GregorianCalendar validFrom = mockValidFrom();
        when(contextProductCmpt2.getGenerationEffectiveOn(validFrom)).thenReturn(contextProductCmptGeneration);
        LinkedHashSet<IProductCmpt> context = new LinkedHashSet<>();
        context.add(contextProductCmpt1);
        context.add(contextProductCmpt2);
        return context;
    }

    private GregorianCalendar mockValidFrom() {
        GregorianCalendar validFrom = mock(GregorianCalendar.class);
        IProductCmptGeneration generation = mock(IProductCmptGeneration.class);
        when(generation.getValidFrom()).thenReturn(validFrom);
        when(expression.getPropertyValueContainer()).thenReturn(generation);
        return validFrom;
    }

    private List<IProductCmptLink> createLinks() throws Exception {
        ArrayList<IProductCmptLink> result = new ArrayList<>();
        when(productCmptLink.findTarget(ipsProject)).thenReturn(productCmpt);
        result.add(productCmptLink);
        return result;
    }

}
