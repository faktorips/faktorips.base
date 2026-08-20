/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt.deltaentries;

import static org.faktorips.abstracttest.MockUtil.createMocks;
import static org.mockito.Mockito.verify;

import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoSession;

public class RemovedTemplateLinkEntryTest {


    @Mock
    private IProductCmptLink deletedLink;

    private MockitoSession mockito;

    @BeforeEach
    void setUp() {
        mockito = createMocks(this);
    }

    @AfterEach
    void tearDown() {
        mockito.finishMocking();
    }



    @Test
    public void testFix() throws Exception {
        RemovedTemplateLinkEntry removedTemplateLinkEntry = new RemovedTemplateLinkEntry(deletedLink);

        removedTemplateLinkEntry.fix();

        verify(deletedLink).delete();
    }

}
