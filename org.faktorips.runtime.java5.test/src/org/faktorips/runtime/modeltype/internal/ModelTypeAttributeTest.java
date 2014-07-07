package org.faktorips.runtime.modeltype.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.internal.AbstractModelObject;
import org.faktorips.runtime.modeltype.IModelElement;
import org.faktorips.runtime.modeltype.IModelTypeAttribute;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ModelTypeAttributeTest {

    private ModelTypeAttribute modelTypeAttribute;

    private MtA source;

    @Mock
    private ModelType modelType;

    @Mock
    private XMLStreamReader parser;

    @Mock
    private IRuntimeRepository repository;

    @Before
    public void setUp() throws Exception {
        when(modelType.getRepository()).thenReturn(repository);
        when(parser.next()).thenReturn(XMLStreamConstants.END_DOCUMENT);
        when(parser.getAttributeCount()).thenReturn(2);
        when(parser.getAttributeLocalName(0)).thenReturn(IModelElement.PROPERTY_NAME);
        when(parser.getAttributeLocalName(1)).thenReturn(IModelTypeAttribute.PROPERTY_DATATYPE);
        modelTypeAttribute = new ModelTypeAttribute(modelType);
        source = new MtA();
    }

    @Test
    public void testGetValueBoolean() throws XMLStreamException {
        when(parser.getAttributeValue(0)).thenReturn("aBooleanValue");
        when(parser.getAttributeValue(1)).thenReturn("boolean");

        modelTypeAttribute.initFromXml(parser);
        Object value = modelTypeAttribute.getValue(source);

        assertNotNull(value);
        assertEquals(source.isABooleanValue(), value);
    }

    @Test
    public void testGetValueString() throws XMLStreamException {
        when(parser.getAttributeValue(0)).thenReturn("aStringValue");
        when(parser.getAttributeValue(1)).thenReturn("String");

        modelTypeAttribute.initFromXml(parser);
        Object value = modelTypeAttribute.getValue(source);

        assertNotNull(value);
        assertEquals(source.getAStringValue(), value);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueHandleGetterErrorField() throws XMLStreamException {
        when(parser.getAttributeValue(0)).thenReturn("aValue");
        when(parser.getAttributeValue(1)).thenReturn("String");

        modelTypeAttribute.initFromXml(parser);
        modelTypeAttribute.getValue(source);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueHandleGetterErrorMethod() throws XMLStreamException {
        when(parser.getAttributeValue(0)).thenReturn("aValueField");
        when(parser.getAttributeValue(1)).thenReturn("String");

        modelTypeAttribute.initFromXml(parser);
        modelTypeAttribute.getValue(source);
    }

    private class MtA extends AbstractModelObject {
        private final boolean aBooleanValue = true;
        private final String aStringValue = "Test";
        private final String aValueField = "Test2";

        public boolean isABooleanValue() {
            return aBooleanValue;
        }

        public String getAStringValue() {
            return aStringValue;
        }

        @SuppressWarnings("unused")
        public String aValueMethod() {
            return aValueField;
        }
    }

}
