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

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.IProductCmptLinkContainer;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.Severity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.verification.VerificationMode;

@RunWith(MockitoJUnitRunner.class)
public class ProductCmptLinkContainerValidatorTest {

    @Mock
    private IIpsProject ipsProject;
    @Mock
    private IIpsProjectProperties props;
    @Mock
    private IProductCmptLinkContainer linkContainer;
    @Mock
    private IProductCmptType prodCmptType;
    @Mock
    private IProductCmptTypeAssociation association;

    private ProductCmptLinkContainerValidator validator;

    @Before
    public void setUp() throws CoreException {
        when(ipsProject.getReadOnlyProperties()).thenReturn(props);
        when(props.isReferencedProductComponentsAreValidOnThisGenerationsValidFromDateRuleEnabled()).thenReturn(false);

        MessageList messageList = new MessageList();
        messageList.add(Message.newError("code1", "errorText"));
        messageList.add(Message.newWarning("code2", "warningText"));
        messageList.add(Message.newError("code3", "errorText2"));
        when(association.validate(any(IIpsProject.class))).thenReturn(messageList);

        List<IProductCmptTypeAssociation> associations = new ArrayList<>();
        associations.add(association);
        when(prodCmptType.getProductCmptTypeAssociations()).thenReturn(associations);

        when(association.getTargetRoleSingular()).thenReturn("targetRole");
        when(association.isDerivedUnion()).thenReturn(false);
        when(linkContainer.isContainerFor(association)).thenReturn(true);

        validator = new ProductCmptLinkContainerValidator(ipsProject, linkContainer);
    }

    private MessageList callValidator() {
        MessageList list = new MessageList();
        validator.startAndAddMessagesToList(prodCmptType, list);
        return list;
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
    public void testVisitIgnoresDerivedUnions() {
        when(association.isDerivedUnion()).thenReturn(true);

        validator = spy(validator);
        callValidator();

        verifyAddMessagesCalled(association, never());

    }

    @Test
    public void testVisitIgnoresOppositeChangingOverTimeAssociations() {
        when(linkContainer.isContainerFor(association)).thenReturn(false);

        validator = spy(validator);
        callValidator();

        verifyAddMessagesCalled(association, never());
    }

    @Test
    public void testVisitCallsAddMessageMethods() {
        validator = spy(validator);
        callValidator();

        verifyAddMessagesCalled(association, times(1));
    }

    @Test
    public void testAddErrorsFromAssociation() {
        MessageList messageList = callValidator();

        assertTrue(messageList.containsErrorMsg());
    }

    @Test
    public void testNotAddWarningsFromAssociation() {
        MessageList messageList = callValidator();

        assertTrue(messageList.getMessagesBySeverity(Severity.WARNING).isEmpty());
    }

    @Test
    public void testNotValidateValidFromIfPropertyFalse() {
        when(props.isReferencedProductComponentsAreValidOnThisGenerationsValidFromDateRuleEnabled()).thenReturn(false);
        validator = spy(validator);
        callValidator();
        verify(validator, never()).addMessageIfTargetNotValidOnValidFromDate(eq(association),
                anyListOf(IProductCmptLink.class), any(MessageList.class));
    }

    @Test
    public void testValidateValidFromIfPropertyTrue() {
        when(props.isReferencedProductComponentsAreValidOnThisGenerationsValidFromDateRuleEnabled()).thenReturn(true);
        validator = spy(validator);
        callValidator();
        verify(validator, times(1)).addMessageIfTargetNotValidOnValidFromDate(eq(association),
                anyListOf(IProductCmptLink.class), any(MessageList.class));
    }

}
