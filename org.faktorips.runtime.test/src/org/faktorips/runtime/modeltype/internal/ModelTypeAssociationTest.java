package org.faktorips.runtime.modeltype.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.faktorips.runtime.modeltype.IModelElement;
import org.faktorips.runtime.modeltype.IModelTypeAssociation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ModelTypeAssociationTest {

    @Mock
    private XMLStreamReader parser;

    @Mock
    private ModelType modelType;

    @Before
    public void setUp() throws Exception {
        when(modelType.getRepository()).thenReturn(null);
        when(parser.next()).thenReturn(XMLStreamConstants.END_DOCUMENT);
    }

    @Test
    public void testInitFromXmlNameAndNamePlural() throws XMLStreamException {
        when(parser.getAttributeCount()).thenReturn(2);
        when(parser.getAttributeLocalName(0)).thenReturn(IModelElement.PROPERTY_NAME);
        when(parser.getAttributeValue(0)).thenReturn("Name");
        when(parser.getAttributeLocalName(1)).thenReturn(IModelTypeAssociation.PROPERTY_NAME_PLURAL);
        when(parser.getAttributeValue(1)).thenReturn("");

        ModelTypeAssociation modelTypeAssociation = new ModelTypeAssociation(modelType);
        modelTypeAssociation.initFromXml(parser);

        assertEquals("Name", modelTypeAssociation.getName());
        assertNull(modelTypeAssociation.getNamePlural());
    }

    @Test
    public void testInitFromXmlMatchingAssociation() throws Exception {
        when(parser.getAttributeCount()).thenReturn(2);
        when(parser.getAttributeLocalName(0)).thenReturn(IModelTypeAssociation.PROPERTY_MATCHING_ASSOCIATION_NAME);
        when(parser.getAttributeValue(0)).thenReturn("PolicyName");
        when(parser.getAttributeLocalName(1)).thenReturn(IModelTypeAssociation.PROPERTY_MATCHING_ASSOCIATION_SOURCE);
        when(parser.getAttributeValue(1)).thenReturn("PolicySource");

        ModelTypeAssociation modelTypeAssociation = new ModelTypeAssociation(modelType);
        modelTypeAssociation.initFromXml(parser);

        assertEquals("PolicyName", modelTypeAssociation.getMatchingAssociationName());
        assertEquals("PolicySource", modelTypeAssociation.getMatchingAssociationSource());
    }
}
