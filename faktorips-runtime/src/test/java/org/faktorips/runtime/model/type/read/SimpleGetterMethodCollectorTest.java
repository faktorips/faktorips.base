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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Optional;

import org.faktorips.runtime.model.annotation.AnnotatedDeclaration;
import org.faktorips.runtime.model.type.DocumentationKind;
import org.faktorips.runtime.model.type.ModelElement;
import org.faktorips.runtime.model.type.read.SimpleTypePartsReader.ModelElementCreator;
import org.faktorips.runtime.model.type.read.SimpleTypePartsReader.NameAccessor;
import org.faktorips.runtime.model.type.read.SimpleTypePartsReader.NamesAccessor;
import org.faktorips.runtime.util.MessagesHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SimpleGetterMethodCollectorTest {

    @Mock
    ModelElement parentModel;

    @Mock
    private ModelElementCreator<DummyElement> modelElementCreator;

    @Mock
    private NameAccessor<ChildAnnotation> nameAccessor;

    @Mock
    private NamesAccessor<ParentAnnotation> namesAccessor;

    @Test
    public void testGetNames() {
        SimpleGetterMethodCollector<DummyElement, ParentAnnotation, ChildAnnotation> simpleGetterMethodCollector = new SimpleGetterMethodCollector<>(
                ParentAnnotation.class, namesAccessor, ChildAnnotation.class, nameAccessor, modelElementCreator);
        simpleGetterMethodCollector.getNames(AnnotatedDeclaration.from(Parent.class));

        verify(namesAccessor).getNames(Parent.class.getAnnotation(ParentAnnotation.class));
    }

    @Test
    public void testGetNames_noAnnotation() {
        SimpleGetterMethodCollector<DummyElement, ParentAnnotation, ChildAnnotation> simpleGetterMethodCollector = new SimpleGetterMethodCollector<>(
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

    private static class DummyElement extends ModelElement {

        public DummyElement(String name) {
            super(name, null, Optional.empty());
        }

        @Override
        protected String getMessageKey(DocumentationKind messageType) {
            return null;
        }

        @Override
        protected MessagesHelper getMessageHelper() {
            return null;
        }

    }

}
