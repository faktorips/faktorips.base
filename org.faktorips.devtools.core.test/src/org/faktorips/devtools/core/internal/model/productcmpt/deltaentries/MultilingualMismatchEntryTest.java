/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpt.deltaentries;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.productcmpt.MultiValueHolder;
import org.faktorips.devtools.core.internal.model.productcmpt.SingleValueHolder;
import org.faktorips.devtools.core.internal.model.value.InternationalStringValue;
import org.faktorips.devtools.core.internal.model.value.StringValue;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.ipsproject.ISupportedLanguage;
import org.faktorips.devtools.core.model.productcmpt.DeltaType;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.value.IValue;
import org.faktorips.values.LocalizedString;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test for {@link MultilingualMismatchEntry}
 * 
 * @author frank
 */
public class MultilingualMismatchEntryTest extends AbstractIpsPluginTest {

    private static final String TEST_VALUE = "abc123";
    private static final String TEST_VALUE2 = "xyz123";

    @Mock
    private IProductCmptTypeAttribute attribute;

    @Mock
    private IAttributeValue attributeValue;

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private IIpsProjectProperties properties;

    @Mock
    private ISupportedLanguage supportedLanguage;

    @Mock
    private IIpsObject ipsObject;

    @Mock
    private IIpsSrcFile ipsSrcFile;

    private InternationalStringValue internationalStringValue;

    @Override
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        doReturn(ipsProject).when(attributeValue).getIpsProject();
        doReturn(ipsObject).when(attributeValue).getIpsObject();
        doReturn(ipsSrcFile).when(ipsObject).getIpsSrcFile();
        doReturn(properties).when(ipsProject).getProperties();
        doReturn(properties).when(ipsProject).getReadOnlyProperties();
        doReturn(supportedLanguage).when(properties).getDefaultLanguage();
        doReturn(Locale.GERMAN).when(supportedLanguage).getLocale();

        internationalStringValue = new InternationalStringValue();
        internationalStringValue.getContent().add(new LocalizedString(Locale.GERMAN, TEST_VALUE));
        internationalStringValue.getContent().add(new LocalizedString(Locale.ENGLISH, TEST_VALUE2));
    }

    @Test
    public void testFix_SingleValueHolderStringToInternationalString() {
        SingleValueHolder holder = new SingleValueHolder(attributeValue, TEST_VALUE);
        doReturn(holder).when(attributeValue).getValueHolder();

        MultilingualMismatchEntry lingualMismatchEntry = new MultilingualMismatchEntry(attributeValue, attribute);
        lingualMismatchEntry.fix();

        assertTrue(holder.getValue() instanceof InternationalStringValue);
        InternationalStringValue value = (InternationalStringValue)holder.getValue();
        assertEquals(TEST_VALUE, value.getContent().get(Locale.GERMAN).getValue());
    }

    @Test
    public void testFix_SingleValueHolderStringNullToInternationalString() {
        SingleValueHolder holder = new SingleValueHolder(attributeValue, (String)null);
        doReturn(holder).when(attributeValue).getValueHolder();

        MultilingualMismatchEntry lingualMismatchEntry = new MultilingualMismatchEntry(attributeValue, attribute);
        lingualMismatchEntry.fix();

        assertTrue(holder.getValue() instanceof InternationalStringValue);
        InternationalStringValue value = (InternationalStringValue)holder.getValue();
        assertEquals(new LocalizedString(Locale.GERMAN, StringUtils.EMPTY), value.getContent().get(Locale.GERMAN));
    }

    @Test
    public void testFix_SingleValueHolderInternationalStringToString() {
        SingleValueHolder holder = new SingleValueHolder(attributeValue, internationalStringValue);
        doReturn(holder).when(attributeValue).getValueHolder();

        MultilingualMismatchEntry lingualMismatchEntry = new MultilingualMismatchEntry(attributeValue, attribute);
        lingualMismatchEntry.fix();

        assertTrue(holder.getValue() instanceof StringValue);
        StringValue value = (StringValue)holder.getValue();
        assertEquals(TEST_VALUE, value.getContent());
    }

    @Test
    public void testFix_SingleValueHolderInternationalStringEmptyToString() {
        internationalStringValue = new InternationalStringValue();
        SingleValueHolder holder = new SingleValueHolder(attributeValue, internationalStringValue);
        doReturn(holder).when(attributeValue).getValueHolder();

        MultilingualMismatchEntry lingualMismatchEntry = new MultilingualMismatchEntry(attributeValue, attribute);
        lingualMismatchEntry.fix();

        assertTrue(holder.getValue() instanceof StringValue);
        StringValue value = (StringValue)holder.getValue();
        assertEquals(StringUtils.EMPTY, value.getContent());
    }

    @Test
    public void testFix_MultiValueHolderStringToInternationalString() {
        List<SingleValueHolder> list = new ArrayList<SingleValueHolder>();
        list.add(new SingleValueHolder(attributeValue, TEST_VALUE));
        list.add(new SingleValueHolder(attributeValue, TEST_VALUE2));
        MultiValueHolder holder = new MultiValueHolder(attributeValue, list);
        doReturn(holder).when(attributeValue).getValueHolder();

        MultilingualMismatchEntry lingualMismatchEntry = new MultilingualMismatchEntry(attributeValue, attribute);
        lingualMismatchEntry.fix();

        IValue<?> iValue = holder.getValue().get(0).getValue();
        assertTrue(iValue instanceof InternationalStringValue);
        assertEquals(TEST_VALUE, ((InternationalStringValue)iValue).getContent().get(Locale.GERMAN).getValue());
        IValue<?> iValue2 = holder.getValue().get(1).getValue();
        assertTrue(iValue2 instanceof InternationalStringValue);
        assertEquals(TEST_VALUE2, ((InternationalStringValue)iValue2).getContent().get(Locale.GERMAN).getValue());
    }

    @Test
    public void testFix_MultiValueHolderInternationalStringToString() {
        List<SingleValueHolder> list = new ArrayList<SingleValueHolder>();
        list.add(new SingleValueHolder(attributeValue, internationalStringValue));
        InternationalStringValue internationalStringValue2 = new InternationalStringValue();
        internationalStringValue2.getContent().add(new LocalizedString(Locale.CANADA, TEST_VALUE));
        internationalStringValue2.getContent().add(new LocalizedString(Locale.GERMAN, TEST_VALUE2));
        list.add(new SingleValueHolder(attributeValue, internationalStringValue2));
        MultiValueHolder holder = new MultiValueHolder(attributeValue, list);
        doReturn(holder).when(attributeValue).getValueHolder();

        MultilingualMismatchEntry lingualMismatchEntry = new MultilingualMismatchEntry(attributeValue, attribute);
        lingualMismatchEntry.fix();

        IValue<?> iValue = holder.getValue().get(0).getValue();
        assertTrue(iValue instanceof StringValue);
        assertEquals(TEST_VALUE, iValue.getContent());

        IValue<?> iValue2 = holder.getValue().get(1).getValue();
        assertTrue(iValue2 instanceof StringValue);
        assertEquals(TEST_VALUE2, iValue2.getContent());
    }

    @Test
    public void testGetDescription() {
        MultilingualMismatchEntry lingualMismatchEntry = new MultilingualMismatchEntry(attributeValue, attribute);
        doReturn(true).when(attribute).isMultilingual();
        String msg1 = lingualMismatchEntry.getDescription();

        doReturn(false).when(attribute).isMultilingual();
        String msg2 = lingualMismatchEntry.getDescription();

        assertTrue(msg1.compareTo(msg2) != 0);
    }

    @Test
    public void testDeltaType() {
        MultilingualMismatchEntry lingualMismatchEntry = new MultilingualMismatchEntry(attributeValue, attribute);
        assertEquals(DeltaType.MULTILINGUAL_MISMATCH, lingualMismatchEntry.getDeltaType());
    }
}
