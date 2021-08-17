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

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;

import com.google.common.collect.Lists;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.model.internal.productcmpt.template.RemoveTemplateOperation.RemoveTemplateModification;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RemoveTemplateOperationTest {

    @Mock
    private IProductCmpt prodCmpt;
    @Mock
    private IProductCmptGeneration gen1;
    @Mock
    private IProductCmptGeneration gen2;

    @Mock
    private IPropertyValue p1;
    @Mock
    private IPropertyValue p2;
    @Mock
    private IPropertyValue g11;
    @Mock
    private IPropertyValue g12;
    @Mock
    private IPropertyValue g21;
    @Mock
    private IPropertyValue g22;

    @Mock
    private IProductCmptLink pl1;
    @Mock
    private IProductCmptLink pl2;
    @Mock
    private IProductCmptLink g1l1;
    @Mock
    private IProductCmptLink g1l2;
    @Mock
    private IProductCmptLink g2l1;
    @Mock
    private IProductCmptLink g2l2;

    @Before
    public void setUp() {
        when(p1.getTemplateValueStatus()).thenReturn(TemplateValueStatus.DEFINED);
        when(p2.getTemplateValueStatus()).thenReturn(TemplateValueStatus.INHERITED);
        when(g11.getTemplateValueStatus()).thenReturn(TemplateValueStatus.UNDEFINED);
        when(g12.getTemplateValueStatus()).thenReturn(TemplateValueStatus.INHERITED);
        when(g21.getTemplateValueStatus()).thenReturn(TemplateValueStatus.UNDEFINED);
        when(g22.getTemplateValueStatus()).thenReturn(TemplateValueStatus.INHERITED);
        when(pl1.getTemplateValueStatus()).thenReturn(TemplateValueStatus.DEFINED);
        when(pl2.getTemplateValueStatus()).thenReturn(TemplateValueStatus.INHERITED);
        when(g1l1.getTemplateValueStatus()).thenReturn(TemplateValueStatus.UNDEFINED);
        when(g1l2.getTemplateValueStatus()).thenReturn(TemplateValueStatus.INHERITED);
        when(g2l1.getTemplateValueStatus()).thenReturn(TemplateValueStatus.UNDEFINED);
        when(g2l2.getTemplateValueStatus()).thenReturn(TemplateValueStatus.INHERITED);
        ArrayList<IPropertyValue> pList = Lists.newArrayList(p1, p2);
        ArrayList<IPropertyValue> g1List = Lists.newArrayList(g11, g12);
        ArrayList<IPropertyValue> g2List = Lists.newArrayList(g21, g22);
        ArrayList<IProductCmptLink> pLinkList = Lists.newArrayList(pl1, pl2);
        ArrayList<IProductCmptLink> g1LinkList = Lists.newArrayList(g1l1, g1l2);
        ArrayList<IProductCmptLink> g2LinkList = Lists.newArrayList(g2l1, g2l2);

        when(prodCmpt.getAllPropertyValues()).thenReturn(pList);
        when(prodCmpt.getLinksAsList()).thenReturn(pLinkList);
        when(prodCmpt.getProductCmptGenerations()).thenReturn(Arrays.asList(gen1, gen2));
        when(gen1.getAllPropertyValues()).thenReturn(g1List);
        when(gen2.getAllPropertyValues()).thenReturn(g2List);
        when(gen1.getLinksAsList()).thenReturn(g1LinkList);
        when(gen2.getLinksAsList()).thenReturn(g2LinkList);
    }

    @Test
    public void testOperation() throws CoreException {
        RemoveTemplateModification singleEventModificationExtension = new RemoveTemplateModification(prodCmpt);
        singleEventModificationExtension.execute();

        verify(p1, never()).setTemplateValueStatus(TemplateValueStatus.DEFINED);
        verify(p2).setTemplateValueStatus(TemplateValueStatus.DEFINED);
        verify(g11, never()).setTemplateValueStatus(TemplateValueStatus.DEFINED);
        verify(g12).setTemplateValueStatus(TemplateValueStatus.DEFINED);
        verify(g21, never()).setTemplateValueStatus(TemplateValueStatus.DEFINED);
        verify(g22).setTemplateValueStatus(TemplateValueStatus.DEFINED);

        verify(pl1, never()).setTemplateValueStatus(TemplateValueStatus.DEFINED);
        verify(pl2).setTemplateValueStatus(TemplateValueStatus.DEFINED);
        verify(g1l1, never()).setTemplateValueStatus(TemplateValueStatus.DEFINED);
        verify(g1l2).setTemplateValueStatus(TemplateValueStatus.DEFINED);
        verify(g2l1, never()).setTemplateValueStatus(TemplateValueStatus.DEFINED);
        verify(g2l2).setTemplateValueStatus(TemplateValueStatus.DEFINED);
        verify(prodCmpt).removeUndefinedLinks();
        verify(gen1).removeUndefinedLinks();
        verify(gen2).removeUndefinedLinks();
    }

}
