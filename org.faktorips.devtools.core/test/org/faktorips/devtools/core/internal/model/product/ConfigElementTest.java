package org.faktorips.devtools.core.internal.model.product;

import org.faktorips.devtools.core.internal.model.IpsObjectTestCase;
import org.faktorips.devtools.core.model.EnumValueSet;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.Range;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.product.ConfigElementType;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 */
public class ConfigElementTest extends IpsObjectTestCase {

    private IProductCmpt productCmpt;
    private IProductCmptGeneration generation;
    private IConfigElement configElement;

    protected void setUp() throws Exception {
        super.setUp(IpsObjectType.PRODUCT_CMPT);
    }

    protected void createObjectAndPart() {
        productCmpt = new ProductCmpt(pdSrcFile);
        generation = (IProductCmptGeneration)productCmpt.newGeneration();
        configElement = generation.newConfigElement();
    }

    protected IConfigElement createconfigElement() {
        productCmpt = new ProductCmpt(pdSrcFile);
        generation = (IProductCmptGeneration)productCmpt.newGeneration();
        return configElement = generation.newConfigElement();
    }

    public void testSetValue() {
        configElement.setValue("newValue");
        assertEquals("newValue", configElement.getValue());
        assertTrue(pdSrcFile.isDirty());
    }

    public void testInitFromXml() {
        Document doc = this.getTestDocument();
        configElement.initFromXml((Element)doc.getDocumentElement());
        assertEquals(42, configElement.getId());
        assertEquals(ConfigElementType.PRODUCT_ATTRIBUTE, configElement.getType());
        assertEquals("rate", configElement.getPcTypeAttribute());
        assertEquals("1.5", configElement.getValue());
    }

    /*
     * Class under test for Element toXml(Document)
     */
    public void testToXmlDocument() {
        IConfigElement cfgElement = createconfigElement();
        cfgElement.setValue("value");
        cfgElement.setValueSet(new Range("22", "33", "4"));
        Element xmlElement = cfgElement.toXml(getTestDocument());

        IConfigElement newCfgElement = createconfigElement();
        newCfgElement.initFromXml(xmlElement);
        assertEquals("value", newCfgElement.getValue());
        assertEquals("22", ((Range)newCfgElement.getValueSet()).getLowerBound());
        assertEquals("33", ((Range)newCfgElement.getValueSet()).getUpperBound());
        assertEquals("4", ((Range)newCfgElement.getValueSet()).getStep());

        EnumValueSet enumValueSet = new EnumValueSet();
        enumValueSet.addValue("one");
        enumValueSet.addValue("two");
        enumValueSet.addValue("three");
        enumValueSet.addValue("four");

        cfgElement.setValueSet(enumValueSet);
        xmlElement = cfgElement.toXml(getTestDocument());
        assertEquals(4, ((EnumValueSet)cfgElement.getValueSet()).getElements().length);
        assertEquals("one", ((EnumValueSet)cfgElement.getValueSet()).getElements()[0]);
        assertEquals("two", ((EnumValueSet)cfgElement.getValueSet()).getElements()[1]);
        assertEquals("three", ((EnumValueSet)cfgElement.getValueSet()).getElements()[2]);
        assertEquals("four", ((EnumValueSet)cfgElement.getValueSet()).getElements()[3]);

    }

    /**
     * Tests for the correct type of excetion to be thrown - no part of any type could ever be created.
     */
    public void testNewPart() {
    	try {
			configElement.newPart(IAttribute.class);
			fail();
		} catch (IllegalArgumentException e) {
			//nothing to do :-)
		}
    }
}
