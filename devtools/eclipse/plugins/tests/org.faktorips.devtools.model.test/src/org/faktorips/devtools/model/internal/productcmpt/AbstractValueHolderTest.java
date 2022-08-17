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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.faktorips.abstracttest.SingletonMockHelper;
import org.faktorips.devtools.model.ContentChangeEvent;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.internal.ipsobject.IpsObject;
import org.faktorips.devtools.model.internal.ipsobject.IpsSrcFileContent;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class AbstractValueHolderTest {

    private IpsModel mockModel;
    private SingletonMockHelper singletonMockHelper;
    private IIpsSrcFile mockIpsSrcFile;
    private IpsSrcFileContent mockContent;
    private IAttributeValue parentMock;

    @Before
    public void setUp() {
        singletonMockHelper = new SingletonMockHelper();
        mockModel = mock(IpsModel.class);
        singletonMockHelper.setSingletonInstance(IpsModel.class, mockModel);

        mockIpsSrcFile = mock(IIpsSrcFile.class);
        mockContent = mock(IpsSrcFileContent.class);
        doReturn(mockContent).when(mockModel).getIpsSrcFileContent(mockIpsSrcFile);

        parentMock = mock(IAttributeValue.class);
        when(parentMock.getIpsSrcFile()).thenReturn(mockIpsSrcFile);

        IpsObject mockObject = mock(IpsObject.class);
        when(parentMock.getIpsObject()).thenReturn(mockObject);

        when(mockObject.getIpsSrcFile()).thenReturn(mockIpsSrcFile);
    }

    @After
    public void tearDown() {
        singletonMockHelper.reset();
    }

    @Test
    public void testObjectChange() throws Exception {
        AbstractValueHolder<?> abstractValueHolder = mock(AbstractValueHolder.class, Mockito.CALLS_REAL_METHODS);
        when(abstractValueHolder.getParent()).thenReturn(parentMock);

        abstractValueHolder.objectHasChanged(null, null);
        verifyZeroInteractions(mockContent);

        abstractValueHolder.objectHasChanged("", "");
        verifyZeroInteractions(mockContent);

        abstractValueHolder.objectHasChanged(new ArrayList<>(), new ArrayList<>());
        verifyZeroInteractions(mockContent);

        abstractValueHolder.objectHasChanged("", "abc");
        verify(mockContent).ipsObjectChanged(any(ContentChangeEvent.class));
        reset(mockContent);

        ArrayList<Object> oldValue = new ArrayList<>();
        oldValue.add("as");
        abstractValueHolder.objectHasChanged(oldValue, new ArrayList<>());
        verify(mockContent).ipsObjectChanged(any(ContentChangeEvent.class));
    }

}
