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

import static org.mockito.Mockito.verify;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.faktorips.devtools.model.ContentChangeEvent;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IpsObjectPartPmoTest {
    @Mock
    private IIpsObjectPartContainer ipsObjectPartContainer;

    @Mock
    private IIpsObjectPart part;

    @Mock
    private PropertyChangeListener listener;

    @Mock
    private PropertyChangeEvent propertyChangeEvent;

    private IpsObjectPartPmo ipsObjectPartPmo;

    @Before
    public void setUpIpsObjectPartPmo() {
        ipsObjectPartPmo = new IpsObjectPartPmo(part);
    }

    @Test
    public void testContentsChanged_notifyChangeListener() throws Exception {
        ipsObjectPartPmo.addPropertyChangeListener(listener);
        ContentChangeEvent event = ContentChangeEvent.newPartChangedEvent(part, propertyChangeEvent);

        ipsObjectPartPmo.contentsChanged(event);

        verify(listener).propertyChange(propertyChangeEvent);
    }

}
