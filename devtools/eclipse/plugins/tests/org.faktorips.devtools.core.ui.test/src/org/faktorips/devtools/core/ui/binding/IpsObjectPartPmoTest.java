/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.binding;

import static org.faktorips.abstracttest.MockUtil.createMocks;
import static org.mockito.Mockito.verify;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.faktorips.devtools.model.ContentChangeEvent;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoSession;

public class IpsObjectPartPmoTest {

    @Mock
    private IIpsObjectPartContainer ipsObjectPartContainer;

    @Mock
    private IIpsObjectPart part;

    @Mock
    private PropertyChangeListener listener;

    @Mock
    private PropertyChangeEvent propertyChangeEvent;

    private MockitoSession mockito;

    private IpsObjectPartPmo ipsObjectPartPmo;

    @BeforeEach
    public void setUpIpsObjectPartPmo() {
        mockito = createMocks(this);
        ipsObjectPartPmo = new IpsObjectPartPmo(part);
    }

    @AfterEach
    void tearDown() {
        mockito.finishMocking();
    }

    @Test
    public void testContentsChanged_notifyChangeListener() throws Exception {
        ipsObjectPartPmo.addPropertyChangeListener(listener);
        ContentChangeEvent event = ContentChangeEvent.newPartChangedEvent(part, propertyChangeEvent);

        ipsObjectPartPmo.contentsChanged(event);

        verify(listener).propertyChange(propertyChangeEvent);
    }

}
