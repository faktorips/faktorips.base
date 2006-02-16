package org.faktorips.devtools.core.internal.model.product;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPluginTest;
import org.faktorips.devtools.core.model.EnumValueSet;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.Range;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.product.ConfigElementType;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 */
public class ConfigElementTest extends IpsPluginTest {

	private IPolicyCmptType policyCmptType;
    private IProductCmpt productCmpt;
    private IIpsSrcFile pdSrcFile;
    private IProductCmptGeneration generation;
    private IConfigElement configElement;

    protected void setUp() throws Exception {
        super.setUp();
        IIpsProject project = newIpsProject("TestProject");
        policyCmptType = (IPolicyCmptType)newIpsObject(project, IpsObjectType.POLICY_CMPT_TYPE, "TestPolicy");
        productCmpt = (IProductCmpt)newIpsObject(project, IpsObjectType.PRODUCT_CMPT, "TestProduct");
        pdSrcFile = productCmpt.getIpsSrcFile();
        productCmpt.setPolicyCmptType(policyCmptType.getQualifiedName());
        generation = (IProductCmptGeneration)productCmpt.newGeneration();
        configElement = generation.newConfigElement();
        
        policyCmptType.getIpsSrcFile().save(true, null);
        productCmpt.getIpsSrcFile().save(true, null);
    }
    
    public void testValidate_UnknownAttribute() throws CoreException {
    	configElement.setPcTypeAttribute("a");
    	MessageList ml = configElement.validate();
    	assertNotNull(ml.getMessageByCode(IConfigElement.MSGCODE_UNKNWON_ATTRIBUTE));
    	
    	policyCmptType.newAttribute().setName("a");
    	ml = configElement.validate();
    	assertNull(ml.getMessageByCode(IConfigElement.MSGCODE_UNKNWON_ATTRIBUTE));
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
        IConfigElement cfgElement = generation.newConfigElement();
        cfgElement.setValue("value");
        cfgElement.setValueSet(new Range("22", "33", "4"));
        Element xmlElement = cfgElement.toXml(getTestDocument());

        IConfigElement newCfgElement = generation.newConfigElement();
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
