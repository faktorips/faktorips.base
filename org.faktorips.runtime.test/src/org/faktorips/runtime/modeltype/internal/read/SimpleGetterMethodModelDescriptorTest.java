package org.faktorips.runtime.modeltype.internal.read;

import static org.mockito.Mockito.verify;

import java.lang.reflect.Method;

import org.faktorips.runtime.modeltype.IModelElement;
import org.faktorips.runtime.modeltype.internal.AbstractModelElement;
import org.faktorips.runtime.modeltype.internal.DocumentationType;
import org.faktorips.runtime.modeltype.internal.read.SimpleTypeModelPartsReader.ModelElementCreator;
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
    private IModelElement parentElement;
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

    private static class DummyElement extends AbstractModelElement {

        public DummyElement(String name) {
            super(name, null);
        }

        @Override
        protected String getMessageKey(DocumentationType messageType) {
            return null;
        }

        @Override
        public MessagesHelper getMessageHelper() {
            return null;
        }

    }

}
