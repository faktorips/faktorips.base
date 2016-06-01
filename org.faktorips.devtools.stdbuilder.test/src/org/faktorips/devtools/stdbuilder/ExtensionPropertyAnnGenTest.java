/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.stdbuilder;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.stdbuilder.xpand.model.AbstractGeneratorModelNode;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ExtensionPropertyAnnGenTest {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

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
                is(equalTo("@IpsExtensionProperties(@IpsExtensionProperty(id = \"foo\", value = \"bar\")"
                        + LINE_SEPARATOR + ")" + LINE_SEPARATOR)));
    }

    @Test
    public void testCreateAnnotation_multiple() {
        AbstractGeneratorModelNode modelNode = modelNode(withExtension("foo", "bar"), withExtension("foobar", "baz"));

        JavaCodeFragment annotation = extensionPropertyAnnotationGenerator.createAnnotation(modelNode);

        assertThat(annotation, is(notNullValue()));
        assertThat(annotation.getSourcecode(),
                is(equalTo("@IpsExtensionProperties({\n\t@IpsExtensionProperty(id = \"foo\", value = \"bar\")"
                        + LINE_SEPARATOR + "\n\t@IpsExtensionProperty(id = \"foobar\", value = \"baz\")"
                        + LINE_SEPARATOR + " })" + LINE_SEPARATOR)));
    }

    @Test
    public void testCreateAnnotation_unavailable() {
        AbstractGeneratorModelNode modelNode = modelNode(withExtension("foo", "bar", false),
                withExtension("foobar", "baz"));

        JavaCodeFragment annotation = extensionPropertyAnnotationGenerator.createAnnotation(modelNode);

        assertThat(annotation, is(notNullValue()));
        assertThat(annotation.getSourcecode(),
                is(equalTo("@IpsExtensionProperties(@IpsExtensionProperty(id = \"foobar\", value = \"baz\")"
                        + LINE_SEPARATOR + ")" + LINE_SEPARATOR)));
    }

    @Test
    public void testCreateAnnotation_nullValue() {
        AbstractGeneratorModelNode modelNode = modelNode(withExtension("foo", null));

        JavaCodeFragment annotation = extensionPropertyAnnotationGenerator.createAnnotation(modelNode);

        assertThat(annotation, is(notNullValue()));
        assertThat(annotation.getSourcecode(),
                is(equalTo("@IpsExtensionProperties(@IpsExtensionProperty(id = \"foo\", isNull = true)"
                        + LINE_SEPARATOR + ")" + LINE_SEPARATOR)));
    }

    @Test
    public void testCreateAnnotation_cdataWithBreaks() {
        AbstractGeneratorModelNode modelNode = modelNode(withExtension("foo", new MockExtensionPropertyValue() {

            @Override
            public void valueToXml(Element valueElement) {
                valueElement.appendChild(valueElement.getOwnerDocument().createCDATASection("bar\n\tba    z"));
            }
        }));

        JavaCodeFragment annotation = extensionPropertyAnnotationGenerator.createAnnotation(modelNode);

        assertThat(annotation, is(notNullValue()));
        assertThat(
                annotation.getSourcecode(),
                is(equalTo("@IpsExtensionProperties(@IpsExtensionProperty(id = \"foo\", value = \"<![CDATA[bar\\n\\tba    z]]>\")"
                        + LINE_SEPARATOR + ")" + LINE_SEPARATOR)));
    }

    @Test
    public void testCreateAnnotation_xml() {
        AbstractGeneratorModelNode modelNode = modelNode(withExtension("foo", new MockExtensionPropertyValue() {

            @Override
            public void valueToXml(Element valueElement) {
                Document doc = valueElement.getOwnerDocument();
                Element foo = doc.createElement("foo");
                Element bar = doc.createElement("bar");
                bar.setAttribute("baz", "!ü ");
                foo.appendChild(bar);
                valueElement.appendChild(foo);
            }
        }));

        JavaCodeFragment annotation = extensionPropertyAnnotationGenerator.createAnnotation(modelNode);

        assertThat(annotation, is(notNullValue()));
        assertThat(
                annotation.getSourcecode(),
                is(equalTo("@IpsExtensionProperties(@IpsExtensionProperty(id = \"foo\", value = \"<foo><bar baz=\\\"!\\u00FC \\\"/></foo>\")"
                        + LINE_SEPARATOR + ")" + LINE_SEPARATOR)));
    }

    private WithExtension withExtension(String id, Object value) {
        return new WithExtension(id, value, true);
    }

    private WithExtension withExtension(String id, Object value, boolean available) {
        return new WithExtension(id, value, available);
    }

    private static final class WithExtension {
        private final String id;
        private final Object value;
        private final boolean available;

        public WithExtension(String id, Object value, boolean available) {
            this.id = id;
            this.value = value;
            this.available = available;
        }
    }

    private AbstractGeneratorModelNode modelNode(WithExtension... withExtensions) {
        IIpsProject ipsProject = mock(IIpsProject.class);
        when(ipsProject.getXmlFileCharset()).thenReturn("UTF-8");
        IIpsObjectPartContainer ipsObjectPartContainer = mock(IIpsObjectPartContainer.class);
        when(ipsObjectPartContainer.getIpsProject()).thenReturn(ipsProject);
        ArrayList<IExtensionPropertyDefinition> extensions = new ArrayList<IExtensionPropertyDefinition>(
                withExtensions.length);
        for (WithExtension withExtension : withExtensions) {
            IExtensionPropertyDefinition extension = mock(MockExtensionPropertyDefinition.class);
            when(extension.getPropertyId()).thenReturn(withExtension.id);
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
