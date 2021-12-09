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

import static org.junit.Assert.assertEquals;
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
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.model.IIpsModelExtensions;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
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
    private IProductCmptLink prodCmptLink;
    @Mock
    private IProductCmptType prodCmptType;
    @Mock
    private IProductCmpt prodCmpt;
    @Mock
    private IProductCmptTypeAssociation association;

    private ProductCmptLinkContainerValidator validator;
    private String validFrom;
    private String productCmptQualifiedName;

    @Before
    public void setUp() throws CoreRuntimeException {
        GregorianCalendar validFromDate = new GregorianCalendar(2021, 0, 1);
        validFrom = IIpsModelExtensions.get().getModelPreferences().getDateFormat()
                .format(validFromDate.getTime());
        productCmptQualifiedName = "prodCmpt";

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

        when(prodCmptLink.findTarget(any(IIpsProject.class))).thenReturn(prodCmpt);
        when(linkContainer.getValidFrom()).thenReturn(validFromDate);
        when(prodCmpt.getGenerationEffectiveOn(any(GregorianCalendar.class))).thenReturn(null);
        when(prodCmpt.getQualifiedName()).thenReturn(productCmptQualifiedName);
        when(prodCmpt.findProductCmptType(any(IIpsProject.class))).thenReturn(prodCmptType);

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

    @Test
    public void testVisitTargetNotValidOnValidFromDate_WithGenerations() {
        String generationName = IIpsModelExtensions.get().getModelPreferences()
                .getChangesOverTimeNamingConvention().getGenerationConceptNameSingular();
        when(prodCmptType.isChangingOverTime()).thenReturn(true);

        MessageList list = new MessageList();
        validator.addMessageIfTargetNotValidOnValidFromDate(association, List.of(prodCmptLink), list);

        String text = NLS.bind(
                Messages.ProductCmptGeneration_msgNoGenerationInLinkedTargetForEffectiveDate,
                new Object[] { productCmptQualifiedName, generationName, validFrom });
        assertEquals(text, list.getFirstMessage(Severity.ERROR).getText());
    }

    @Test
    public void testVisitTargetNotValidOnValidFromDate_WithoutGenerations() {
        when(prodCmptType.isChangingOverTime()).thenReturn(false);

        MessageList list = new MessageList();
        validator.addMessageIfTargetNotValidOnValidFromDate(association, List.of(prodCmptLink), list);

        String text = NLS.bind(
                Messages.ProductCmptGeneration_msgEffectiveDateInLinkedTargetAfterEffectiveDate,
                new Object[] { productCmptQualifiedName, validFrom });
        assertEquals(text, list.getFirstMessage(Severity.ERROR).getText());
    }
}
