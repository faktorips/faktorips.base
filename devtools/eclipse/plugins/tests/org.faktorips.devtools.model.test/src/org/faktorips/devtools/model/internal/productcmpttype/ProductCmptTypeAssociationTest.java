/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpttype;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * 
 * @author Stefan Widmaier
 */
@RunWith(MockitoJUnitRunner.class)
public class ProductCmptTypeAssociationTest {

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private ProductCmptTypeAssociation derivedUnionMock;

    private ProductCmptTypeAssociation subsetMock;

    private ArgumentCaptor<Message> messageCaptor;

    private MessageList messageList;

    @Test
    public void testDerivedUnionStatic_SubsetChanging() {
        setUpAssociations(true, false);

        verifyErrorMessageAdded();
    }

    @Test
    public void testDerivedUnionChanging_SubsetStatic() {
        setUpAssociations(false, true);

        verifyErrorMessageAdded();
    }

    @Test
    public void testNoMismatch() {
        setUpAssociations(false, false);

        subsetMock.validateDerivedUnionChangingOverTimeProperty(messageList, ipsProject);
        verify(messageList, never()).add(any(Message.class));
    }

    @Test
    public void testNoMismatch2() {
        setUpAssociations(true, true);

        subsetMock.validateDerivedUnionChangingOverTimeProperty(messageList, ipsProject);
        verify(messageList, never()).add(any(Message.class));
    }

    private void setUpAssociations(boolean duChanging, boolean subsetChanging) {
        subsetMock = mock(ProductCmptTypeAssociation.class, CALLS_REAL_METHODS);
        messageList = mock(MessageList.class);
        messageCaptor = ArgumentCaptor.forClass(Message.class);

        doReturn(derivedUnionMock).when(subsetMock).findSubsettedDerivedUnion(ipsProject);
        when(subsetMock.isSubsetOfADerivedUnion()).thenReturn(true);
        when(subsetMock.isChangingOverTime()).thenReturn(subsetChanging);
        when(derivedUnionMock.isChangingOverTime()).thenReturn(duChanging);
    }

    private void verifyErrorMessageAdded() {
        subsetMock.validateDerivedUnionChangingOverTimeProperty(messageList, ipsProject);
        verify(messageList).add(messageCaptor.capture());
        Message message = messageCaptor.getValue();
        assertEquals(IProductCmptTypeAssociation.MSGCODE_DERIVED_UNION_CHANGING_OVER_TIME_MISMATCH, message.getCode());
        assertEquals(subsetMock, message.getInvalidObjectProperties().get(0).getObject());
    }
}
