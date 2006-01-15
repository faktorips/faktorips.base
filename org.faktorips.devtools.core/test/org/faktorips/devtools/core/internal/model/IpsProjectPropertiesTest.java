package org.faktorips.devtools.core.internal.model;

import org.faktorips.devtools.core.IpsPluginTest;
import org.faktorips.devtools.core.model.IIpsObjectPath;
import org.faktorips.devtools.core.model.IIpsProject;
import org.w3c.dom.Element;

public class IpsProjectPropertiesTest extends IpsPluginTest {

	private IIpsProject ipsProject;
	
	protected void setUp() throws Exception {
		super.setUp();
        ipsProject = this.newIpsProject("TestProject");
	}

	public void testToXml() {
		IpsProjectProperties projectData = new IpsProjectProperties();
		projectData.setBuilderSetId("myBuilder");
		IIpsObjectPath path = new IpsObjectPath();
		path.newSourceFolderEntry(ipsProject.getProject().getFolder("model"));
		projectData.setIpsObjectPath(path);
		projectData.setPredefinedDatatypesUsed(new String[]{"Integer", "Boolean"});
		Element projectEl = projectData.toXml(newDocument());

		projectData = new IpsProjectProperties();
		projectData.initFromXml(ipsProject, projectEl);
		assertEquals("myBuilder", projectData.getBuilderSetId());
		path = projectData.getIpsObjectPath();
		assertNotNull(path);
		assertEquals(1, path.getEntries().length);
		String[] datatypes = projectData.getPredefinedDatatypesUsed();
		assertNotNull(datatypes);
		assertEquals(2, datatypes.length);
		assertEquals("Integer", datatypes[0]);
		assertEquals("Boolean", datatypes[1]);
		
	}

	public void testInitFromXml() {
		Element docEl = this.getTestDocument().getDocumentElement();
		IpsProjectProperties props = new IpsProjectProperties();
		props.initFromXml(ipsProject, docEl);
		assertEquals("org.faktorips.devtools.stdbuilder.ipsstdbuilderset", props.getBuilderSetId());
		IIpsObjectPath path = props.getIpsObjectPath();
		assertNotNull(path);
		assertEquals(1, path.getEntries().length);
		
        // used predefined datatype
        String[] datatypes = props.getPredefinedDatatypesUsed();
		assertNotNull(datatypes);
		assertEquals(2, datatypes.length);
		assertEquals("Boolean", datatypes[0]);
		assertEquals("Integer", datatypes[1]);
        
        // datatype definitions
        DynamicValueDatatype[] dynTypes = props.getDefinedDatatypes();
        assertEquals(1, dynTypes.length);
        assertEquals("PaymentMode", dynTypes[0].getQualifiedName());
	}

}
