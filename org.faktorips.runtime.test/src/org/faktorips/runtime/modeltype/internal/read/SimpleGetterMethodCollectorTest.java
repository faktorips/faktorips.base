package org.faktorips.runtime.modeltype.internal.read;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.faktorips.runtime.model.annotation.AnnotatedDeclaration;
import org.faktorips.runtime.modeltype.IModelElement;
import org.faktorips.runtime.modeltype.internal.AbstractModelElement;
import org.faktorips.runtime.modeltype.internal.DocumentationType;
import org.faktorips.runtime.modeltype.internal.read.SimpleTypeModelPartsReader.ModelElementCreator;
import org.faktorips.runtime.modeltype.internal.read.SimpleTypeModelPartsReader.NameAccessor;
import org.faktorips.runtime.modeltype.internal.read.SimpleTypeModelPartsReader.NamesAccessor;
import org.faktorips.runtime.util.MessagesHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SimpleGetterMethodCollectorTest {

    @Mock
    IModelElement parentModel;

    @Mock
    private ModelElementCreator<DummyElement> modelElementCreator;

    @Mock
    private NameAccessor<ChildAnnotation> nameAccessor;

    @Mock
    private NamesAccessor<ParentAnnotation> namesAccessor;

    @Test
    public void testGetNames() {
        SimpleGetterMethodCollector<DummyElement, ParentAnnotation, ChildAnnotation> simpleGetterMethodCollector = new SimpleGetterMethodCollector<DummyElement, ParentAnnotation, ChildAnnotation>(
                ParentAnnotation.class, namesAccessor, ChildAnnotation.class, nameAccessor, modelElementCreator);
        simpleGetterMethodCollector.getNames(AnnotatedDeclaration.from(Parent.class));

        verify(namesAccessor).getNames(Parent.class.getAnnotation(ParentAnnotation.class));
    }

    @Test
    public void testGetNames_noAnnotation() {
        SimpleGetterMethodCollector<DummyElement, ParentAnnotation, ChildAnnotation> simpleGetterMethodCollector = new SimpleGetterMethodCollector<DummyElement, ParentAnnotation, ChildAnnotation>(
                ParentAnnotation.class, namesAccessor, ChildAnnotation.class, nameAccessor, modelElementCreator);
        String[] names = simpleGetterMethodCollector.getNames(AnnotatedDeclaration.from(String.class));

        assertThat(names, is(notNullValue()));
        assertThat(names.length, is(0));
        verify(namesAccessor, never()).getNames(any(ParentAnnotation.class));
    }

    @Retention(RetentionPolicy.RUNTIME)
    private static @interface ParentAnnotation {
    }

    @Retention(RetentionPolicy.RUNTIME)
    private static @interface ChildAnnotation {
    }

    @ParentAnnotation
    private static class Parent {

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
        protected MessagesHelper getMessageHelper() {
            return null;
        }

    }

}
