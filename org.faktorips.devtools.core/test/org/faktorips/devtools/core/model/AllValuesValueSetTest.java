package org.faktorips.devtools.core.model;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.util.XmlAbstractTestCase;
import org.faktorips.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class AllValuesValueSetTest extends XmlAbstractTestCase {

	public void testCreateFromXml() throws CoreException {
		Document doc = getTestDocument();
		Element root = doc.getDocumentElement();
		Element element = XmlUtil.getFirstElement(root);
		AllValuesValueSet allValues = (AllValuesValueSet)AllValuesValueSet.createFromXml(element);
		assertNotNull(allValues);
	}

	public void testToXml() {
	    AllValuesValueSet allValues = new AllValuesValueSet();
		Element element = allValues.toXml(this.newDocument());
		AllValuesValueSet allValues2 = (AllValuesValueSet)AllValuesValueSet.createFromXml(element);
		assertNotNull(allValues2);
	}
	
	public void testContains () {
	    AllValuesValueSet allValues = new AllValuesValueSet();
	    assertTrue(allValues.contains("abc", Datatype.DECIMAL));
 	}
	
}