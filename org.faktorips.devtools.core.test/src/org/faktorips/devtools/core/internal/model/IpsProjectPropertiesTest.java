/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model;

import java.util.Locale;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.product.DateBasedProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.IIpsObjectPath;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsProjectProperties;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;

public class IpsProjectPropertiesTest extends AbstractIpsPluginTest {

	private IIpsProject ipsProject;
	
	protected void setUp() throws Exception {
		super.setUp();
        ipsProject = this.newIpsProject("TestProject");
	}
	
	public void testValidate_DefinedDatatypes() throws CoreException {
		IpsProjectProperties props = new IpsProjectProperties();
		MessageList list = props.validate(ipsProject);
		int numOfMessages = list.getNoOfMessages();
		DynamicValueDatatype dynDatatype = new DynamicValueDatatype(ipsProject);
		props.setDefinedDatatypes(new DynamicValueDatatype[]{dynDatatype});
		list = props.validate(ipsProject);
		assertTrue(list.getNoOfMessages()>numOfMessages); // there should be at least one or message
	}

	public void testValidate_PredefinedDatatypes() throws CoreException {
		IpsProjectProperties props = new IpsProjectProperties();
		MessageList list = props.validate(ipsProject);
		int numOfMessages = list.getNoOfMessages();
		props.setPredefinedDatatypesUsed(ipsProject.getIpsModel().getPredefinedValueDatatypes());
		list = props.validate(ipsProject);
		assertEquals(numOfMessages, list.getNoOfMessages()); // there should be at least one or message
		
		props.setPredefinedDatatypesUsed(new String[]{"unknownDatatype"});
		list = props.validate(ipsProject);
		assertTrue(list.getNoOfMessages()>numOfMessages); // there should be at least one or message
		assertTrue(list.getMessageByCode(IIpsProjectProperties.MSGCODE_UNKNOWN_PREDEFINED_DATATYPE)!=null);
	}

	public void testToXml() {
		IpsProjectProperties props = new IpsProjectProperties();
		props.setModelProject(true);
		props.setProductDefinitionProject(true);
		props.setJavaSrcLanguage(Locale.ITALIAN);
		props.setChangesOverTimeNamingConventionIdForGeneratedCode("myConvention");
		props.setBuilderSetId("myBuilder");
		props.setRuntimeIdPrefix("newRuntimeIdPrefix");
		props.setProductCmptNamingStrategy(new DateBasedProductCmptNamingStrategy(" ", "yyyy-MM", true));
		IIpsObjectPath path = new IpsObjectPath();
		path.newSourceFolderEntry(ipsProject.getProject().getFolder("model"));
		props.setIpsObjectPath(path);
		props.setPredefinedDatatypesUsed(new String[]{"Integer", "Boolean"});
		Element projectEl = props.toXml(newDocument());

		props = new IpsProjectProperties();
		props.initFromXml(ipsProject, projectEl);
		assertTrue(props.isModelProject());
		assertTrue(props.isProductDefinitionProject());
		assertEquals("newRuntimeIdPrefix", props.getRuntimeIdPrefix());
		assertEquals(Locale.ITALIAN, props.getJavaSrcLanguage());
		assertEquals("myConvention", props.getChangesOverTimeNamingConventionIdForGeneratedCode());
		assertEquals("myBuilder", props.getBuilderSetId());
		assertTrue(props.getProductCmptNamingStrategy() instanceof DateBasedProductCmptNamingStrategy);
		DateBasedProductCmptNamingStrategy strategy = (DateBasedProductCmptNamingStrategy)props.getProductCmptNamingStrategy();
		assertEquals("yyyy-MM", strategy.getDateFormatPattern());
		assertEquals(" ", strategy.getVersionIdSeparator());
		assertTrue(strategy.isPostfixAllowed());
		path = props.getIpsObjectPath();
		assertNotNull(path);
		assertEquals(1, path.getEntries().length);
		String[] datatypes = props.getPredefinedDatatypesUsed();
		assertNotNull(datatypes);
		assertEquals(2, datatypes.length);
		assertEquals("Integer", datatypes[0]);
		assertEquals("Boolean", datatypes[1]);
	}
	
	public void testAddDefinedDatatype() {
		IpsProjectProperties props = new IpsProjectProperties();

		DynamicValueDatatype type1 = new DynamicValueDatatype(ipsProject);
		type1.setQualifiedName("type1");
		props.addDefinedDatatype(type1);
		assertEquals(type1, props.getDefinedDatatypes()[0]);
		
		DynamicValueDatatype type2 = new DynamicValueDatatype(ipsProject);
		type2.setQualifiedName("type2");
		props.addDefinedDatatype(type2);
		assertEquals(type1, props.getDefinedDatatypes()[0]);
		assertEquals(type2, props.getDefinedDatatypes()[1]);
		
		DynamicValueDatatype type1b = new DynamicValueDatatype(ipsProject);
		type1b.setQualifiedName("type1");
		props.addDefinedDatatype(type1b);
		assertEquals(type1b, props.getDefinedDatatypes()[0]);
		assertEquals(type2, props.getDefinedDatatypes()[1]);

		// type with qName=null
		DynamicValueDatatype type3 = new DynamicValueDatatype(ipsProject);
		props.addDefinedDatatype(type3);
		assertEquals(type3, props.getDefinedDatatypes()[2]);
	}

	public void testInitFromXml() {
		Element docEl = this.getTestDocument().getDocumentElement();
		IpsProjectProperties props = new IpsProjectProperties();
		props.initFromXml(ipsProject, docEl);
		assertTrue(props.isModelProject());
		assertTrue(props.isProductDefinitionProject());
		assertEquals(Locale.ITALIAN, props.getJavaSrcLanguage());
		assertEquals("myConvention", props.getChangesOverTimeNamingConventionIdForGeneratedCode());
		assertEquals("testPrefix", props.getRuntimeIdPrefix());
		
		DateBasedProductCmptNamingStrategy namingStrategy = (DateBasedProductCmptNamingStrategy)props.getProductCmptNamingStrategy();
		assertEquals(" ", namingStrategy.getVersionIdSeparator());
		assertEquals("yyyy-MM", namingStrategy.getDateFormatPattern());
		assertTrue(namingStrategy.isPostfixAllowed());
		assertEquals(ipsProject, namingStrategy.getIpsProject());
		
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
        assertEquals(2, dynTypes.length);
        assertEquals("PaymentMode", dynTypes[0].getQualifiedName());
        assertTrue(dynTypes[0] instanceof DynamicEnumDatatype);
        assertEquals("PaymentMode", dynTypes[1].getQualifiedName());
        assertTrue(!(dynTypes[1] instanceof DynamicEnumDatatype));
	}
	
	public void testGetLocale() {
		assertEquals(Locale.ENGLISH, IpsProjectProperties.getLocale(""));
		assertEquals(Locale.ENGLISH, IpsProjectProperties.getLocale(Locale.ENGLISH.toString()));
		assertEquals(Locale.GERMANY, IpsProjectProperties.getLocale(Locale.GERMANY.toString()));
		assertEquals(new Locale("de", "DE", "variantA"), IpsProjectProperties.getLocale("de_DE_variantA"));
		
	}

}
