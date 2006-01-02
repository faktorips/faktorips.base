package org.faktorips.devtools.core.model;

import junit.framework.TestCase;

/**
 *
 */
public class PdObjectTypeTest extends TestCase {

    public void testNewObject() {
        IpsObjectType[] types = IpsObjectType.ALL_TYPES;
        for (int i=0; i<types.length; i++) {
            assertNotNull(types[i].newObject(null));
        }
    }

}
