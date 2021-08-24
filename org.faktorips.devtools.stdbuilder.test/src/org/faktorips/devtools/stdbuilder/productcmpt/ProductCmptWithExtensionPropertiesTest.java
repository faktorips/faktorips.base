/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.stdbuilder.productcmpt;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.TestIpsModelExtensions;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.extproperties.BooleanExtensionPropertyDefinition;
import org.faktorips.devtools.model.extproperties.IExtensionPropertyDefinition.RetentionPolicy;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilder;
import org.faktorips.devtools.model.pctype.AttributeType;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpt.IConfiguredDefault;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.util.XmlUtil;
import org.faktorips.devtools.stdbuilder.AbstractStdBuilderTest;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class ProductCmptWithExtensionPropertiesTest extends AbstractStdBuilderTest {

    @SuppressWarnings("deprecation")
    @Test
    public void testExtensionXml() throws CoreException, IOException, SAXException {
        try (TestIpsModelExtensions testIpsModelExtensions = new TestIpsModelExtensions()) {
            TestExtensionPropertyDefinition extensionPropertyDefinition = new TestExtensionPropertyDefinition();
            extensionPropertyDefinition.setName("TestExtension");
            extensionPropertyDefinition.setPropertyId("TestExtensionId");
            extensionPropertyDefinition.setExtendedType(IConfiguredDefault.class);
            extensionPropertyDefinition.setPosition("top");
            extensionPropertyDefinition.setRetention(RetentionPolicy.RUNTIME);
            testIpsModelExtensions.setExtensionPropertyDefinitions(
                    Map.of(IConfiguredDefault.class, Collections.singletonList(extensionPropertyDefinition)));

            IpsModel.reInit();

            IPolicyCmptType policyCmptType = newPolicyAndProductCmptType(ipsProject, "PolicyType", "ProductType");
            IProductCmptType productCmptType = policyCmptType.findProductCmptType(ipsProject);
            IPolicyCmptTypeAttribute attribute = policyCmptType.newPolicyCmptTypeAttribute("TestAttribute");
            attribute.setAttributeType(AttributeType.CHANGEABLE);
            attribute.setDatatype(Datatype.STRING.getQualifiedName());
            attribute.setChangingOverTime(false);
            attribute.setValueSetConfiguredByProduct(true);
            policyCmptType.getIpsSrcFile().save(true, null);

            IProductCmpt productCmpt = newProductCmpt(productCmptType, "Product");
            productCmpt.setValidFrom(new GregorianCalendar(2021, Calendar.JANUARY, 1));
            IConfiguredDefault configuredDefault = productCmpt.newPropertyValue(attribute, IConfiguredDefault.class);
            configuredDefault.setValue(" test \t ");
            configuredDefault.setExtPropertyValue(extensionPropertyDefinition.getPropertyId(), true);
            productCmpt.getIpsSrcFile().save(true, null);

            String fileContent = getFileContent(productCmpt.getIpsSrcFile());

            Document document = XmlUtil.getDefaultDocumentBuilder()
                    .parse(productCmpt.getIpsSrcFile().getContentFromEnclosingResource());
            Element documentElement = document.getDocumentElement();
            Element configuredDefaultElement = XmlUtil.getFirstElement(documentElement, "ConfiguredDefault");
            assertThat(XmlUtil.getCDATAorTextContent(configuredDefaultElement), is(" test \t "));

            assertThat(fileContent.replaceAll(" id=\"[a-f0-9-]+\"", ""), is(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                            + "<ProductCmpt productCmptType=\"ProductType\" runtimeId=\"Product\" template=\"\" xml:space=\"preserve\">\n"
                            + " <validTo isNull=\"true\"/>\n"
                            + " <Description locale=\"de\"/>\n"
                            + " <Description locale=\"en\"/>\n"
                            + " <Generation validFrom=\"2021-01-01\"/>\n"
                            + " <ConfiguredDefault attribute=\"TestAttribute\" templateValueStatus=\"defined\"> test \t <ExtensionProperties>\n"
                            + "   <Value id=\"TestExtensionId\" isNull=\"false\"><![CDATA[true]]></Value>\n"
                            + "  </ExtensionProperties>\n"
                            + " </ConfiguredDefault>\n"
                            + "</ProductCmpt>"));

            ProductCmptXMLBuilder productCmptXMLBuilder = findProductCmptXMLBuilder();
            productCmptXMLBuilder.build(productCmpt.getIpsSrcFile());
            IFile xmlContentFile = productCmptXMLBuilder.getXmlContentFile(productCmpt.getIpsSrcFile());

            document = XmlUtil.getDefaultDocumentBuilder().parse(xmlContentFile.getContents());
            documentElement = document.getDocumentElement();
            configuredDefaultElement = XmlUtil.getFirstElement(documentElement, "ConfiguredDefault");
            assertThat(XmlUtil.getCDATAorTextContent(configuredDefaultElement), is(" test \t "));
            fileContent = getFileContent(xmlContentFile);
            assertThat(fileContent.replaceAll(" id=\"[a-f0-9-]+\"", ""), is(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                            + "<ProductCmpt productCmptType=\"ProductType\" runtimeId=\"Product\" template=\"\" validFrom=\"2021-01-01\" xml:space=\"preserve\">\n"
                            + " <validTo isNull=\"true\"/>\n"
                            + " <Description locale=\"de\"/>\n"
                            + " <Description locale=\"en\"/>\n"
                            + " <Generation validFrom=\"2021-01-01\"/>\n"
                            + " <ConfiguredDefault attribute=\"TestAttribute\" templateValueStatus=\"defined\"> test \t <ExtensionProperties>\n"
                            + "   <Value id=\"TestExtensionId\" isNull=\"false\"><![CDATA[true]]></Value>\n"
                            + "  </ExtensionProperties>\n"
                            + " </ConfiguredDefault>\n"
                            + "</ProductCmpt>"));
        }
    }

    private ProductCmptXMLBuilder findProductCmptXMLBuilder() {
        IIpsArtefactBuilder[] builders = ipsProject.getIpsArtefactBuilderSet().getArtefactBuilders();
        for (IIpsArtefactBuilder builder : builders) {
            if (builder instanceof ProductCmptXMLBuilder) {
                return (ProductCmptXMLBuilder)builder;
            }
        }
        fail("No ProductCmptXMLBuilder found");
        return null;
    }

    public static class TestExtensionPropertyDefinition extends BooleanExtensionPropertyDefinition {

        public TestExtensionPropertyDefinition() {
            super();
        }

    }

}
