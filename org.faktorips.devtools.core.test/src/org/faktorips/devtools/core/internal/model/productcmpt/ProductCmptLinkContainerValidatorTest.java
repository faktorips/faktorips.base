/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpt;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProductCmptLinkContainerValidatorTest {

    @Mock
    IIpsProject ipsProject;
    @Mock
    IProductCmptLinkContainer linkContainer;
    @Mock
    IProductCmptType prodCmptType;
    @Mock
    IProductCmptLink link1;
    @Mock
    IProductCmptLink link2;
    private ProductCmptLinkContainerValidator validator;

    @Before
    public void setUp() {
        validator = new ProductCmptLinkContainerValidator(ipsProject, linkContainer);
    }

    private MessageList callValidator() {
        MessageList list = new MessageList();
        try {
            validator.visit(prodCmptType);
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
        return list;
    }

    private List<IProductCmptLink> list(IProductCmptLink... links) {
        return Arrays.asList(links);
    }

    @Test
    public void testVisitIgnoredDerivedUnions() {
        IProductCmptTypeAssociation assoc = mock(IProductCmptTypeAssociation.class);
        IProductCmptTypeAssociation assoc2 = mock(IProductCmptTypeAssociation.class);
        when(assoc.isDerivedUnion()).thenReturn(true);
        List<IAssociation> associations = new ArrayList<IAssociation>();
        associations.add(assoc);
        associations.add(assoc2);
        when(prodCmptType.getAssociations()).thenReturn(associations);

        validator = spy(validator);
        verif y (validator, never()).addMessageIfAssociationHasValidationMessages(eq(assoc), any(MessageList.class));
        verify(validator, never()).addMessageIfDuplicateTargetPresent(eq(assoc), anyListOf(IProductCmptLink.class),
                any(MessageList.class));
        verify(validator, never()).addMessageIfLessLinksThanMinCard(eq(assoc), anyListOf(IProductCmptLink.class),
                any(MessageList.class));
        verify(validator, never()).addMessageIfMoreLinksThanMaxCard(eq(assoc), anyListOf(IProductCmptLink.class),
                any(MessageList.class));
        callValidator();

        verify(validator, never()).addMessageIfAssociationHasValidationMessages(eq(assoc), any(MessageList.class));
        verify(validator, never()).addMessageIfDuplicateTargetPresent(eq(assoc), anyListOf(IProductCmptLink.class),
                any(MessageList.class));
        verify(validator, never()).addMessageIfLessLinksThanMinCard(eq(assoc), anyListOf(IProductCmptLink.class),
                any(MessageList.class));
        verify(validator, never()).addMessageIfMoreLinksThanMaxCard(eq(assoc), anyListOf(IProductCmptLink.class),
                any(MessageList.class));

        verify(validator, times(1)).addMessageIfAssociationHasValidationMessages(eq(assoc2), any(MessageList.class));
        verify(validator, times(1)).addMessageIfDuplicateTargetPresent(eq(assoc2), anyListOf(IProductCmptLink.class),
                any(MessageList.class));
        verify(validator, times(1)).addMessageIfLessLinksThanMinCard(eq(assoc2), anyListOf(IProductCmptLink.class),
                any(MessageList.class));
        verify(validator, times(1)).addMessageIfMoreLinksThanMaxCard(eq(assoc2), anyListOf(IProductCmptLink.class),
                any(MessageList.class));
    }

    @Test
    public void testVisitCallsAddMessageMethods() {
    }

    @Test
    public void testValidateMinumumCardinality() {
        MessageList list = callValidator();
        assertEquals(1, list.size());
    }

    @Test
    public void testValidateMaximumCardinality() {

    }

    @Test
    public void testValidateDuplicateTarget() {

    }

    @Test
    public void testValidateChangingOverTime() {

    }
}
