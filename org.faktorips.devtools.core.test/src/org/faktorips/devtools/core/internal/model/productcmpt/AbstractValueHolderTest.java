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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.core.internal.model.productcmpt.AbstractValueHolder;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.junit.Test;
import org.mockito.Mockito;

public class AbstractValueHolderTest {

    @Test
    public void testObjectChange() throws Exception {
        AbstractValueHolder abstractValueHolder = mock(AbstractValueHolder.class, Mockito.CALLS_REAL_METHODS);
        IIpsObjectPart parentMock = mock(IIpsObjectPart.class);
        when(abstractValueHolder.getParent()).thenReturn(parentMock);

        abstractValueHolder.objectHasChanged(null, null);

    }

}
