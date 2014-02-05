/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpt;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
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
import org.mockito.verification.VerificationMode;

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
        validator.start(prodCmptType);
        return list;
    }

    @Test
    public void testVisitIgnoresDerivedUnions() {
        IProductCmptTypeAssociation assoc = mock(IProductCmptTypeAssociation.class);
        when(assoc.isDerivedUnion()).thenReturn(true);

        List<IAssociation> associations = new ArrayList<IAssociation>();
        associations.add(assoc);
        when(prodCmptType.getAssociations()).thenReturn(associations);

        validator = spy(validator);
        callValidator();

        verifyAddMessagesCalled(assoc, never());

    }

    private void verifyAddMessagesCalled(IProductCmptTypeAssociation assoc, VerificationMode mode) {
        verify(validator, mode).validateAssociation(eq(assoc));
        verify(validator, mode).addMessageIfAssociationHasValidationMessages(eq(assoc), any(MessageList.class));
        verify(validator, mode).addMessageIfDuplicateTargetPresent(eq(assoc), anyListOf(IProductCmptLink.class),
                any(MessageList.class));
        verify(validator, mode).addMessageIfLessLinksThanMinCard(eq(assoc), anyListOf(IProductCmptLink.class),
                any(MessageList.class));
        verify(validator, mode).addMessageIfMoreLinksThanMaxCard(eq(assoc), anyListOf(IProductCmptLink.class),
                any(MessageList.class));
    }

    @Test
    public void testVisitIgnoresOppositeChangingOverTimeAssociations() {
        IProductCmptTypeAssociation assoc = mock(IProductCmptTypeAssociation.class);
        when(linkContainer.isContainerFor(assoc)).thenReturn(false);

        List<IAssociation> associations = new ArrayList<IAssociation>();
        associations.add(assoc);
        when(prodCmptType.getAssociations()).thenReturn(associations);

        validator = spy(validator);
        callValidator();

        verifyAddMessagesCalled(assoc, never());
    }

    @Test
    public void testVisitCallsAddMessageMethods() throws CoreException {
        IProductCmptTypeAssociation assoc = mock(IProductCmptTypeAssociation.class);
        when(assoc.isDerivedUnion()).thenReturn(false);
        when(linkContainer.isContainerFor(assoc)).thenReturn(true);
        MessageList messageList = mock(MessageList.class);
        when(messageList.isEmpty()).thenReturn(true);
        when(assoc.validate(any(IIpsProject.class))).thenReturn(messageList);

        List<IProductCmptTypeAssociation> associations = new ArrayList<IProductCmptTypeAssociation>();
        associations.add(assoc);
        when(prodCmptType.getProductCmptTypeAssociations()).thenReturn(associations);

        validator = spy(validator);
        callValidator();

        verifyAddMessagesCalled(assoc, times(1));
    }

}
