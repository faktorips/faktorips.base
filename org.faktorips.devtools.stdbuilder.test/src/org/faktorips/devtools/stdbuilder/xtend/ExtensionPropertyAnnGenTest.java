/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xtend;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.model.extproperties.IExtensionPropertyDefinition;
import org.faktorips.devtools.model.extproperties.IExtensionPropertyDefinition.RetentionPolicy;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.stdbuilder.xmodel.AbstractGeneratorModelNode;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ExtensionPropertyAnnGenTest {

    private static final String LINE_SEPARATOR = System.lineSeparator();

    private ExtensionPropertyAnnGen extensionPropertyAnnotationGenerator = new ExtensionPropertyAnnGen();

    @Test
    public void testIsGenerateAnnotationFor_noExtensions() {
        AbstractGeneratorModelNode modelNode = modelNode();

        assertThat(extensionPropertyAnnotationGenerator.isGenerateAnnotationFor(modelNode), is(false));
    }

    @Test
    public void testIsGenerateAnnotationFor_withExtensions() {
        AbstractGeneratorModelNode modelNode = modelNode(withExtension("foo", "bar"));

        assertThat(extensionPropertyAnnotationGenerator.isGenerateAnnotationFor(modelNode), is(true));
    }

    @Test
    public void testIsGenerateAnnotationFor_unavailableExtensions() {
        AbstractGeneratorModelNode modelNode = modelNode(withExtension("foo", "bar", false));

        assertThat(extensionPropertyAnnotationGenerator.isGenerateAnnotationFor(modelNode), is(false));
    }

    @Test
    public void testCreateAnnotation_single() {
        AbstractGeneratorModelNode modelNode = modelNode(withExtension("foo", "bar"));

        JavaCodeFragment annotation = extensionPropertyAnnotationGenerator.createAnnotation(modelNode);

        assertThat(annotation, is(notNullValue()));
        assertThat(annotation.getSourcecode(),
                is(equalTo("@IpsExtensionProperties(@IpsExtensionProperty(id = \"foo\", value = \"bar\")" + ")"
                        + LINE_SEPARATOR)));
    }

    @Test
    public void testCreateAnnotation_multiple() {
        AbstractGeneratorModelNode modelNode = modelNode(withExtension("foo", "bar"), withExtension("foobar", "baz"));

        JavaCodeFragment annotation = extensionPropertyAnnotationGenerator.createAnnotation(modelNode);

        assertThat(annotation, is(notNullValue()));
        assertThat(annotation.getSourcecode(), is(equalTo("@IpsExtensionProperties({" + LINE_SEPARATOR
                + "\t@IpsExtensionProperty(id = \"foo\", value = \"bar\")," + LINE_SEPARATOR
                + "\t@IpsExtensionProperty(id = \"foobar\", value = \"baz\")" + "})" + LINE_SEPARATOR)));
    }

    @Test
    public void testCreateAnnotation_unavailable() {
        AbstractGeneratorModelNode modelNode = modelNode(withExtension("foo", "bar", false),
                withExtension("foobar", "baz"));

        JavaCodeFragment annotation = extensionPropertyAnnotationGenerator.createAnnotation(modelNode);

        assertThat(annotation, is(notNullValue()));
        assertThat(annotation.getSourcecode(),
                is(equalTo("@IpsExtensionProperties(@IpsExtensionProperty(id = \"foobar\", value = \"baz\")" + ")"
                        + LINE_SEPARATOR)));
    }

    @Test
    public void testCreateAnnotation_nullValue() {
        AbstractGeneratorModelNode modelNode = modelNode(withExtension("foo", null));

        JavaCodeFragment annotation = extensionPropertyAnnotationGenerator.createAnnotation(modelNode);

        assertThat(annotation, is(notNullValue()));
        assertThat(annotation.getSourcecode(),
                is(equalTo("@IpsExtensionProperties(@IpsExtensionProperty(id = \"foo\", isNull = true)" + ")"
                        + LINE_SEPARATOR)));
    }

    @Test
    public void testCreateAnnotation_cdataWithBreaks() {
        AbstractGeneratorModelNode modelNode = modelNode(
                withExtension("foo", (MockExtensionPropertyValue)valueElement -> valueElement
                        .appendChild(valueElement.getOwnerDocument().createCDATASection("bar\n\tba    z"))));

        JavaCodeFragment annotation = extensionPropertyAnnotationGenerator.createAnnotation(modelNode);

        assertThat(annotation, is(notNullValue()));
        assertThat(annotation.getSourcecode(),
                is(equalTo("@IpsExtensionProperties(@IpsExtensionProperty(id = \"foo\", value = \"bar\\n\\tba    z\")"
                        + ")" + LINE_SEPARATOR)));
    }

    @Test
    public void testIsGenerateAnnotationFor_RetentionDefinition_Single() {
        AbstractGeneratorModelNode modelNode = modelNode(withExtension("foo", "bar", RetentionPolicy.DEFINITION));

        assertThat(extensionPropertyAnnotationGenerator.isGenerateAnnotationFor(modelNode), is(false));
    }

    @Test
    public void testCreateAnnotation_RetentionDefinition_Mixed() {
        AbstractGeneratorModelNode modelNode = modelNode(withExtension("foo", "bar"),
                withExtension("foobar", "baz", RetentionPolicy.DEFINITION));

        JavaCodeFragment annotation = extensionPropertyAnnotationGenerator.createAnnotation(modelNode);

        assertThat(annotation, is(notNullValue()));
        assertThat(annotation.getSourcecode(), is(equalTo("@IpsExtensionProperties("
                + "@IpsExtensionProperty(id = \"foo\", value = \"bar\")" + ")" + LINE_SEPARATOR)));
    }

    @Ignore("Complex XML is not supported at the moment")
    @Test
    public void testCreateAnnotation_xml() {
        AbstractGeneratorModelNode modelNode = modelNode(
                withExtension("foo", (MockExtensionPropertyValue)valueElement -> {
                    Document doc = valueElement.getOwnerDocument();
                    Element foo = doc.createElement("foo");
                    Element bar = doc.createElement("bar");
                    bar.setAttribute("baz", "!ü ");
                    foo.appendChild(bar);
                    valueElement.appendChild(foo);
                }));

        JavaCodeFragment annotation = extensionPropertyAnnotationGenerator.createAnnotation(modelNode);

        assertThat(annotation, is(notNullValue()));
        assertThat(
                annotation.getSourcecode(),
                is(equalTo(
                        "@IpsExtensionProperties(@IpsExtensionProperty(id = \"foo\", value = \"<foo><bar baz=\\\"!\\u00FC \\\"/></foo>\")"
                                + LINE_SEPARATOR + ")" + LINE_SEPARATOR)));
    }

    private WithExtension withExtension(String id, Object value) {
        return withExtension(id, value, true);
    }

    private WithExtension withExtension(String id, Object value, boolean available) {
        return withExtension(id, value, available, RetentionPolicy.RUNTIME);
    }

    private WithExtension withExtension(String id, Object value, RetentionPolicy retentionType) {
        return withExtension(id, value, true, retentionType);
    }

    private WithExtension withExtension(String id, Object value, boolean available, RetentionPolicy retentionType) {
        return new WithExtension(id, value, available, retentionType);
    }

    private static final class WithExtension {
        private final String id;
        private final Object value;
        private final boolean available;
        public final RetentionPolicy retention;

        public WithExtension(String id, Object value, boolean available, RetentionPolicy retentionType) {
            this.id = id;
            this.value = value;
            this.available = available;
            retention = retentionType;
        }
    }

    private AbstractGeneratorModelNode modelNode(WithExtension... withExtensions) {
        IIpsProject ipsProject = mock(IIpsProject.class);
        when(ipsProject.getXmlFileCharset()).thenReturn("UTF-8");
        IIpsObjectPartContainer ipsObjectPartContainer = mock(IIpsObjectPartContainer.class);
        when(ipsObjectPartContainer.getIpsProject()).thenReturn(ipsProject);
        ArrayList<IExtensionPropertyDefinition> extensions = new ArrayList<>(
                withExtensions.length);
        for (WithExtension withExtension : withExtensions) {
            IExtensionPropertyDefinition extension = mock(MockExtensionPropertyDefinition.class);
            when(extension.getPropertyId()).thenReturn(withExtension.id);
            when(extension.getRetention()).thenReturn(withExtension.retention);
            when(extension.isRetainedAtRuntime()).thenReturn(withExtension.retention == RetentionPolicy.RUNTIME);
            doCallRealMethod().when(extension).valueToXml(any(Element.class), any());
            when(ipsObjectPartContainer.getExtPropertyValue(withExtension.id)).thenReturn(withExtension.value);
            when(ipsObjectPartContainer.isExtPropertyDefinitionAvailable(withExtension.id)).thenReturn(
                    withExtension.available);
            extensions.add(extension);
        }
        when(ipsObjectPartContainer.getExtensionPropertyDefinitions()).thenReturn(extensions);

        AbstractGeneratorModelNode modelNode = mock(AbstractGeneratorModelNode.class);
        when(modelNode.getIpsObjectPartContainer()).thenReturn(ipsObjectPartContainer);

        return modelNode;
    }

    public static abstract class MockExtensionPropertyDefinition implements IExtensionPropertyDefinition {

        @Override
        public void valueToXml(Element valueElement, Object value) {
            if (value instanceof MockExtensionPropertyValue) {
                ((MockExtensionPropertyValue)value).valueToXml(valueElement);
            } else {
                valueElement.appendChild(valueElement.getOwnerDocument().createTextNode(value.toString()));
            }
        }

    }

    private static interface MockExtensionPropertyValue {
        public void valueToXml(Element valueElement);
    }

}
