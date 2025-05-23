/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.datatype;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;

import javax.xml.parsers.ParserConfigurationException;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.abstracttest.TestEnumType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.util.XmlUtil;
import org.faktorips.runtime.MessageList;
import org.faktorips.util.StringUtil;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

public class DynamicValueDatatypeTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject("TestProject");
    }

    @Test
    public void testCreateFromXml_NoneEnumType() {
        Element docEl = getTestDocument().getDocumentElement();
        Element el = XmlUtil.getElement(docEl, "Datatype", 0);
        DynamicValueDatatype type = DynamicValueDatatype.createFromXml(ipsProject, el);
        assertThat(type.getAdaptedClassName(), is("foo.bar.MyDate"));
        assertThat(type.getValueOfMethodName(), is("getMyDate"));
        assertThat(type.getIsParsableMethodName(), is("isMyDate"));
        assertThat(type.hasNullObject(), is(false));
    }

    @Test
    public void testCreateFromXml_EnumType() {
        Element docEl = getTestDocument().getDocumentElement();
        Element el = XmlUtil.getElement(docEl, "Datatype", 1);
        DynamicEnumDatatype type = (DynamicEnumDatatype)DynamicValueDatatype.createFromXml(ipsProject, el);
        assertThat(type.getAdaptedClassName(), is("foo.bar.PaymentMode"));
        assertThat(type.getValueOfMethodName(), is("getPaymentMode"));
        assertThat(type.getIsParsableMethodName(), is("isPaymentMode"));
        assertThat(type.getToStringMethodName(), is("getId"));
        assertThat(type.getGetNameMethodName(), is("getName"));
        assertThat(type.getAllValuesMethodName(), is("getPaymentModes"));
        assertThat(type.isSupportingNames(), is(true));
        assertThat(type.hasNullObject(), is(false));
    }

    @Test
    public void testCreateFromXml_NullObjectWithIdNotNull() {
        Element docEl = getTestDocument().getDocumentElement();
        Element el = XmlUtil.getElement(docEl, "Datatype", 2);
        DynamicEnumDatatype type = (DynamicEnumDatatype)DynamicValueDatatype.createFromXml(ipsProject, el);
        assertThat(type.hasNullObject(), is(true));
        assertThat(type.getNullObjectId(), is("n"));
    }

    @Test
    public void testCreateFromXml_NullObjectWithIdNull() {
        Element docEl = getTestDocument().getDocumentElement();
        Element el = XmlUtil.getElement(docEl, "Datatype", 3);
        DynamicEnumDatatype type = (DynamicEnumDatatype)DynamicValueDatatype.createFromXml(ipsProject, el);
        assertThat(type.hasNullObject(), is(true));
        assertThat(type.getNullObjectId(), is(nullValue()));
    }

    @Test
    public void testGetNamedDatatype() {
        Element docEl = getTestDocument().getDocumentElement();
        Element el = XmlUtil.getElement(docEl, "Datatype", 4);
        DynamicValueDatatype type = DynamicValueDatatype.createFromXml(ipsProject, el);

        assertThat(type.getAdaptedClassName(), is("foo.bar.MyDataType"));
        assertThat(type.isEnum(), is(false));
        assertThat(type.isSupportingNames(), is(true));
        assertThat(type.getGetNameMethodName(), is("getSymbol"));
        assertThat(type.getGetValueByNameMethodName(), is("getValueByName"));
    }

    @Test
    public void testCheckGetValueByName() {
        Class<TestEnumType> adaptedClass = TestEnumType.class;
        DynamicValueDatatype dataType = new DynamicValueDatatype(ipsProject);

        dataType.setAdaptedClass(adaptedClass);
        dataType.setIsSupportingNames(true);
        dataType.setQualifiedName(StringUtil.unqualifiedName(adaptedClass.getName()));
        dataType.setGetNameMethodName("getName");
        dataType.setValueOfMethodName("valueOf");
        dataType.setIsParsableMethodName(null);
        dataType.setToStringMethodName("toString");
        dataType.setGetValueByNameMethodName(null);

        dataType.setAllValuesMethodName(null);

        MessageList messages = dataType.checkReadyToUse();
        assertThat(messages.getMessages().size(), is(1));
        assertThat(messages.getMessages().get(0).getCode(),
                is(DynamicValueDatatype.MSGCODE_GET_VALUE_BY_NAME_METHOD_IS_BLANK));

        dataType.setAllValuesMethodName("getAllValues");

        messages = dataType.checkReadyToUse();
        assertThat(messages.getMessages().size(), is(0));
    }

    @Test
    public void testFindValueByNameInAllValues() {
        Class<TestEnumType> adaptedClass = TestEnumType.class;
        DynamicValueDatatype dataType = new DynamicValueDatatype(ipsProject);

        dataType.setAdaptedClass(adaptedClass);
        dataType.setIsSupportingNames(true);
        dataType.setQualifiedName(StringUtil.unqualifiedName(adaptedClass.getName()));
        dataType.setGetNameMethodName("getName");
        dataType.setValueOfMethodName("valueOf");
        dataType.setIsParsableMethodName(null);
        dataType.setToStringMethodName("toString");
        dataType.setGetValueByNameMethodName(null);
        dataType.setAllValuesMethodName("getAllValues");

        Object valueByName = dataType.getValueByName("third");
        assertThat(valueByName, is(TestEnumType.THIRDVALUE));
    }

    @Test
    public void testXmlRoundtrip() throws ParserConfigurationException {
        Class<TestEnumType> adaptedClass = TestEnumType.class;
        DynamicValueDatatype dataType = new DynamicValueDatatype(ipsProject);

        dataType.setAdaptedClass(adaptedClass);
        dataType.setIsSupportingNames(true);
        dataType.setQualifiedName(StringUtil.unqualifiedName(adaptedClass.getName()));
        dataType.setGetNameMethodName("getName");
        dataType.setValueOfMethodName("valueOf");
        dataType.setIsParsableMethodName("isParsable");
        dataType.setToStringMethodName("toString");
        dataType.setGetValueByNameMethodName("parseName");
        dataType.setAllValuesMethodName("getAllValues");
        dataType.setJaxbXmlJavaTypeAdapterClass("jaxbXmlJavaAdpater");

        Element documentElement = createXmlDocument("Datatype").getDocumentElement();

        dataType.writeToXml(documentElement);

        DynamicValueDatatype dataType2 = DynamicValueDatatype.createFromXml(ipsProject, documentElement);
        assertThat(dataType2.getGetNameMethodName(), is("getName"));
        assertThat(dataType2.getValueOfMethodName(), is("valueOf"));
        assertThat(dataType2.getIsParsableMethodName(), is("isParsable"));
        assertThat(dataType2.getToStringMethodName(), is("toString"));
        assertThat(dataType2.getGetValueByNameMethodName(), is("parseName"));
        assertThat(dataType2.getAllValuesMethodName(), is("getAllValues"));
        assertThat(dataType2.isSupportingNames(), is(true));
        assertThat(dataType2.hasNullObject(), is(false));
        assertThat(dataType2.getJaxbXmlJavaTypeAdapterClass(), is("jaxbXmlJavaAdpater"));
    }
}
