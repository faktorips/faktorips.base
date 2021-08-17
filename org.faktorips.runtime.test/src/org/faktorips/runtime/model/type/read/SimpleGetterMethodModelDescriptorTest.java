/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.model.type.read;

import static org.mockito.Mockito.verify;

import java.lang.reflect.Method;

import org.faktorips.runtime.model.type.DocumentationKind;
import org.faktorips.runtime.model.type.ModelElement;
import org.faktorips.runtime.model.type.read.SimpleTypePartsReader.ModelElementCreator;
import org.faktorips.runtime.util.MessagesHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SimpleGetterMethodModelDescriptorTest {

    @Mock
    private ModelElementCreator<DummyElement> modelElementCreator;
    @Mock
    private ModelElement parentElement;
    @InjectMocks
    private SimpleGetterMethodModelDescriptor<DummyElement> simpleGetterMethodModelDescriptor;

    @Test
    public void testCreate() throws Exception {
        Method getterMethod = DummyElement.class.getMethod("getMessageHelper");
        simpleGetterMethodModelDescriptor.setGetterMethod(getterMethod);
        simpleGetterMethodModelDescriptor.setName("foo");

        simpleGetterMethodModelDescriptor.create(parentElement);

        verify(modelElementCreator).create(parentElement, "foo", getterMethod);
    }

    private static class DummyElement extends ModelElement {

        public DummyElement(String name) {
            super(name, null);
        }

        @Override
        protected String getMessageKey(DocumentationKind messageType) {
            return null;
        }

        @Override
        public MessagesHelper getMessageHelper() {
            return null;
        }

    }

}
