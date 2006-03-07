package org.faktorips.devtools.core.model;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.DefaultTestContent;
import org.faktorips.devtools.core.IpsPluginTest;
import org.faktorips.devtools.core.internal.model.AllValuesValueSet;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class AllValuesValueSetTest extends IpsPluginTest {

	IConfigElement ce;
	
	public void setUp() throws Exception {
		super.setUp();
		DefaultTestContent content = new DefaultTestContent();
		IProductCmptGeneration gen = (IProductCmptGeneration)content.getComfortCollisionCoverageA().getGenerations()[0];
		ce = gen.newConfigElement();
	}
	
	public void testCreateFromXml() throws CoreException, SAXException, IOException, ParserConfigurationException {
		Document doc = getTestDocument();
		Element root = doc.getDocumentElement();
		Element element = XmlUtil.getFirstElement(root);

		IValueSet allValues = new AllValuesValueSet(ce, 1);
		allValues.initFromXml(element);
		assertNotNull(allValues);
	}

	public void testToXml() {
	    AllValuesValueSet allValues = new AllValuesValueSet(ce, 1);
		Element element = allValues.toXml(this.newDocument());
		IAllValuesValueSet allValues2 = new AllValuesValueSet(ce, 2);
		allValues2.initFromXml(element);
		assertNotNull(allValues2);
	}
	
	public void testContains () {
	    AllValuesValueSet allValues = new AllValuesValueSet(ce, 1);
	    assertTrue(allValues.containsValue("abc", Datatype.DECIMAL));
 	}
	
}