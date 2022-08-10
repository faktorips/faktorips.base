/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class DefaultObjectReferenceStoreTest {

    @Test
    public void testGetObject() {
        DefaultObjectReferenceStore store = new DefaultObjectReferenceStore();
        IMyInterface someObject = new MyImplementation();
        store.putObject("4711", someObject);

        assertEquals(someObject, store.getObject(MyImplementation.class, "4711"));
        assertEquals(someObject, store.getObject(IMyInterface.class, "4711"));

        assertNull(store.getObject(String.class, "4711"));
        assertNull(store.getObject(MyImplementation.class, "unknownId"));
    }

    interface IMyInterface {

    }

    class MyImplementation implements IMyInterface {

    }

}
