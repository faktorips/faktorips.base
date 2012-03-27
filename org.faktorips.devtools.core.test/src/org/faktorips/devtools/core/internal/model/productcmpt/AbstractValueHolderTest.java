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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.faktorips.abstracttest.SingletonMockHelper;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObject;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsSrcFileContent;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class AbstractValueHolderTest {

    private IpsModel mockModel;
    private SingletonMockHelper singletonMockHelper;
    private IIpsSrcFile mockIpsSrcFile;
    private IpsSrcFileContent mockContent;
    private IIpsObjectPart parentMock;

    @Before
    public void setUp() {
        singletonMockHelper = new SingletonMockHelper();
        IpsPlugin pluginMock = mock(IpsPlugin.class);
        singletonMockHelper.setSingletonInstance(IpsPlugin.class, pluginMock);

        mockModel = mock(IpsModel.class);
        when(pluginMock.getIpsModel()).thenReturn(mockModel);

        mockIpsSrcFile = mock(IIpsSrcFile.class);
        mockContent = mock(IpsSrcFileContent.class);
        when(mockModel.getIpsSrcFileContent(mockIpsSrcFile)).thenReturn(mockContent);

        parentMock = mock(IIpsObjectPart.class);
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

        abstractValueHolder.objectHasChanged(new ArrayList<Object>(), new ArrayList<Object>());
        verifyZeroInteractions(mockContent);

        abstractValueHolder.objectHasChanged("", "abc");
        verify(mockContent).ipsObjectChanged(any(ContentChangeEvent.class));
        reset(mockContent);

        ArrayList<Object> oldValue = new ArrayList<Object>();
        oldValue.add("as");
        abstractValueHolder.objectHasChanged(oldValue, new ArrayList<Object>());
        verify(mockContent).ipsObjectChanged(any(ContentChangeEvent.class));
    }

}
