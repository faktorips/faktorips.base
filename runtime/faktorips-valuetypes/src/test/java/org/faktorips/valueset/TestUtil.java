/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.valueset;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

public abstract class TestUtil {

    @Test
    public static final void testSerializable(Serializable serializableObject) throws IOException,
            ClassNotFoundException {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(bos);
        os.writeObject(serializableObject);
        byte[] bytes = bos.toByteArray();

        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInputStream is = new ObjectInputStream(bis);
        Object deserializedObject = is.readObject();
        assertEquals(serializableObject, deserializedObject);
    }

    static <T> Matcher<ValueSet<T>> subsetOf(ValueSet<T> otherValueSet) {
        return new TypeSafeMatcher<ValueSet<T>>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("a subset of ");
                description.appendValue(otherValueSet);
            }

            @Override
            protected boolean matchesSafely(ValueSet<T> item) {
                return item.isSubsetOf(otherValueSet);
            }
        };
    }

}
