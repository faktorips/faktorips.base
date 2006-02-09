package org.faktorips.devtools.core.internal.model;

import java.util.Locale;

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
		IpsProjectProperties props = new IpsProjectProperties();
		props.setModelProject(true);
		props.setProductDefinitionProject(true);
		props.setJavaSrcLanguage(Locale.ITALIAN);
		props.setChangesInTimeConventionIdForGeneratedCode("myConvention");
		props.setBuilderSetId("myBuilder");
		IIpsObjectPath path = new IpsObjectPath();
		path.newSourceFolderEntry(ipsProject.getProject().getFolder("model"));
		props.setIpsObjectPath(path);
		props.setPredefinedDatatypesUsed(new String[]{"Integer", "Boolean"});
		Element projectEl = props.toXml(newDocument());

		props = new IpsProjectProperties();
		props.initFromXml(ipsProject, projectEl);
		assertTrue(props.isModelProject());
		assertTrue(props.isProductDefinitionProject());
		assertEquals(Locale.ITALIAN, props.getJavaSrcLanguage());
		assertEquals("myConvention", props.getChangesInTimeConventionIdForGeneratedCode());
		assertEquals("myBuilder", props.getBuilderSetId());
		path = props.getIpsObjectPath();
		assertNotNull(path);
		assertEquals(1, path.getEntries().length);
		String[] datatypes = props.getPredefinedDatatypesUsed();
		assertNotNull(datatypes);
		assertEquals(2, datatypes.length);
		assertEquals("Integer", datatypes[0]);
		assertEquals("Boolean", datatypes[1]);
	}

	public void testInitFromXml() {
		Element docEl = this.getTestDocument().getDocumentElement();
		IpsProjectProperties props = new IpsProjectProperties();
		props.initFromXml(ipsProject, docEl);
		assertTrue(props.isModelProject());
		assertTrue(props.isProductDefinitionProject());
		assertEquals(Locale.ITALIAN, props.getJavaSrcLanguage());
		assertEquals("myConvention", props.getChangesInTimeConventionIdForGeneratedCode());
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
	
	public void testGetLocale() {
		assertEquals(Locale.ENGLISH, IpsProjectProperties.getLocale(""));
		assertEquals(Locale.ENGLISH, IpsProjectProperties.getLocale(Locale.ENGLISH.toString()));
		assertEquals(Locale.GERMANY, IpsProjectProperties.getLocale(Locale.GERMANY.toString()));
		assertEquals(new Locale("de", "DE", "variantA"), IpsProjectProperties.getLocale("de_DE_variantA"));
		
	}

}
