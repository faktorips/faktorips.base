/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpttype;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
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
    public void testDerivedUnionStatic_SubsetChanging() throws CoreException {
        setUpAssociations(true, false);

        verifyErrorMessageAdded();
    }

    @Test
    public void testDerivedUnionChanging_SubsetStatic() throws CoreException {
        setUpAssociations(false, true);

        verifyErrorMessageAdded();
    }

    @Test
    public void testNoMismatch() throws CoreException {
        setUpAssociations(false, false);

        subsetMock.validateDerivedUnionChangingOverTimeProperty(messageList, ipsProject);
        verify(messageList, never()).add(any(Message.class));
    }

    @Test
    public void testNoMismatch2() throws CoreException {
        setUpAssociations(true, true);

        subsetMock.validateDerivedUnionChangingOverTimeProperty(messageList, ipsProject);
        verify(messageList, never()).add(any(Message.class));
    }

    private void setUpAssociations(boolean duChanging, boolean subsetChanging) throws CoreException {
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
        assertEquals(subsetMock, message.getInvalidObjectProperties()[0].getObject());
    }
}
