/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.valueset;

import static org.faktorips.testsupport.IpsMatchers.hasMessageCode;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.osgi.util.NLS;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.PrimitiveIntegerDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.ContentChangeEvent;
import org.faktorips.devtools.model.ContentsChangeListener;
import org.faktorips.devtools.model.IIpsModelExtensions;
import org.faktorips.devtools.model.IValidationMsgCodesForInvalidValues;
import org.faktorips.devtools.model.internal.enums.EnumContent;
import org.faktorips.devtools.model.internal.enums.EnumType;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpt.IConfiguredValueSet;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.util.XmlUtil;
import org.faktorips.devtools.model.valueset.IEnumValueSet;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.ObjectProperty;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class EnumValueSetTest extends AbstractIpsPluginTest {

    private static final String MY_ENUM_CONTENT = "MyEnumContent";

    private static final String MY_EXTENSIBLE_ENUM = "MyExtensibleEnum";

    private IPolicyCmptType policyCmptType;
    private IProductCmptType productCmptType;

    private IPolicyCmptTypeAttribute attr;
    private IConfiguredValueSet cValueSet;

    private IIpsProject ipsProject;
    private IIpsProject productIpsProject;
    private IProductCmptGeneration generation;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        ipsProject = newIpsProject();
        productIpsProject = newIpsProject();
        IIpsObjectPath ipsObjectPath = productIpsProject.getIpsObjectPath();
        ipsObjectPath.newIpsProjectRefEntry(ipsProject);
        productIpsProject.setIpsObjectPath(ipsObjectPath);
        policyCmptType = newPolicyCmptType(ipsProject, "test.Base");
        attr = policyCmptType.newPolicyCmptTypeAttribute();
        attr.setName("attr");
        attr.setDatatype(Datatype.MONEY.getQualifiedName());
        policyCmptType.getIpsSrcFile().save(null);

        productCmptType = newProductCmptType(ipsProject, "test.Product", policyCmptType);

        IProductCmpt cmpt = newProductCmpt(productIpsProject, "test.Product");
        cmpt.setProductCmptType(productCmptType.getQualifiedName());
        generation = (IProductCmptGeneration)cmpt.newGeneration(new GregorianCalendar(20006, 4, 26));

        cValueSet = generation.newPropertyValue(attr, IConfiguredValueSet.class);

        EnumType enumType = newEnumType(ipsProject, MY_EXTENSIBLE_ENUM);
        enumType.setExtensible(true);
        enumType.setEnumContentName(MY_ENUM_CONTENT);
        EnumContent newEnumContent = newEnumContent(productIpsProject, MY_ENUM_CONTENT);
        newEnumContent.setEnumType(MY_EXTENSIBLE_ENUM);
    }

    @Test
    public void testCopy() {
        EnumValueSet set = new EnumValueSet(cValueSet, "1");
        set.addValue("10");
        set.addValue("5");
        set.addValue("1");

        IEnumValueSet copy = (IEnumValueSet)set.copy(generation.newPropertyValue(attr, IConfiguredValueSet.class), "1");
        assertEquals(3, copy.size());
        assertEquals(0, copy.getPositions("10").get(0).intValue());
        assertEquals(1, copy.getPositions("5").get(0).intValue());
        assertEquals(2, copy.getPositions("1").get(0).intValue());
    }

    @Test
    public void testCopyPropertiesFrom() {
        EnumValueSet set = new EnumValueSet(cValueSet, "1");
        set.addValue("10");
        set.addValue("5");
        set.addValue("1");

        EnumValueSet set2 = new EnumValueSet(generation.newPropertyValue(attr, IConfiguredValueSet.class), "1");
        set2.copyPropertiesFrom(set);

        assertEquals(3, set2.size());
        assertEquals(0, set2.getPositions("10").get(0).intValue());
        assertEquals(1, set2.getPositions("5").get(0).intValue());
        assertEquals(2, set2.getPositions("1").get(0).intValue());
    }

    @Test
    public void testGetPositions() {
        EnumValueSet set = new EnumValueSet(cValueSet, "1");
        List<Integer> positions = set.getPositions("1");
        assertEquals(0, positions.size());

        set.addValue("1");
        positions = set.getPositions("1");
        assertEquals(1, positions.size());
        assertEquals(0, positions.get(0).intValue());

        set.addValue("2");
        set.addValue("3");
        set.addValue("1");
        positions = set.getPositions("1");
        assertEquals(2, positions.size());
        assertEquals(0, positions.get(0).intValue());
        assertEquals(3, positions.get(1).intValue());

        set.addValue("1");
        positions = set.getPositions("1");
        assertEquals(3, positions.size());
        assertEquals(0, positions.get(0).intValue());
        assertEquals(3, positions.get(1).intValue());
        assertEquals(4, positions.get(2).intValue());

        set.removeValue(0);
        positions = set.getPositions("1");
        assertEquals(2, positions.size());
        assertEquals(2, positions.get(0).intValue());
        assertEquals(3, positions.get(1).intValue());
    }

    @Test
    public void testContainsValue() throws Exception {
        EnumValueSet set = new EnumValueSet(cValueSet, "1");
        set.addValue("10EUR");
        set.addValue("20EUR");
        set.addValue("30EUR");
        assertTrue(set.containsValue("10EUR", ipsProject));
        assertTrue(set.containsValue("10 EUR", ipsProject));
        assertFalse(set.containsValue("15 EUR", ipsProject));
        assertFalse(set.containsValue("abc", ipsProject));
        assertFalse(set.containsValue(null, ipsProject));

        set.addValue(null);
        assertTrue(set.containsValue(null, ipsProject));

        assertFalse(set.containsValue("15 EUR", ipsProject));

        assertTrue(set.containsValue("10 EUR", ipsProject));
    }

    @Test
    public void testContainsValueInvalidSet() throws Exception {
        EnumValueSet set = new EnumValueSet(cValueSet, "1");
        set.addValue("1");
        assertFalse(set.containsValue("10 EUR", ipsProject));

        set.addValue("10EUR");
        assertTrue(set.containsValue("10 EUR", ipsProject));
    }

    @Test
    public void testContainsValueSet() throws Exception {
        EnumValueSet superset = new EnumValueSet(cValueSet, "id1");
        superset.addValue("1EUR");
        superset.addValue("2EUR");
        superset.addValue("3EUR");

        EnumValueSet subset = new EnumValueSet(cValueSet, "id2");
        assertTrue(superset.containsValueSet(subset));

        subset.addValue("1EUR");
        assertTrue(superset.containsValueSet(subset));

        subset.addValue("2EUR");
        subset.addValue("3EUR");
        assertTrue(superset.containsValueSet(subset));

        assertTrue(superset.containsValueSet(subset));

        subset.addValue("4EUR");
        assertFalse(superset.containsValueSet(subset));

        EnumValueSet abstractSet = new EnumValueSet(cValueSet, "60");
        abstractSet.setAbstract(true);
        assertTrue(abstractSet.containsValueSet(subset));
        assertFalse(subset.containsValueSet(abstractSet));

        assertFalse(superset.containsValueSet(subset));

        subset.removeValue("4EUR");
        subset.addValue(null);
        assertFalse(superset.containsValueSet(subset));

        superset.addValue(null);
        assertTrue(superset.containsValueSet(subset));

        IPolicyCmptTypeAttribute attr2 = policyCmptType.newPolicyCmptTypeAttribute();
        attr2.setName("attr2");
        attr2.setDatatype(Datatype.STRING.getQualifiedName());
        policyCmptType.getIpsSrcFile().save(null);

        IConfiguredValueSet cValueSet2 = generation.newPropertyValue(attr2, IConfiguredValueSet.class);

        subset = new EnumValueSet(cValueSet2, "50");
        subset.addValue("2EUR");

        assertFalse(superset.containsValueSet(subset));
    }

    @Test
    public void testContainsValueSet_EqualValueSetsWithNull() {
        EnumValueSet superset = new EnumValueSet(cValueSet, "id1");
        superset.setContainsNull(true);
        EnumValueSet subset = new EnumValueSet(cValueSet, "id2");
        subset.setContainsNull(true);

        assertTrue(superset.containsValueSet(subset));
    }

    @Test
    public void testContainsValueSet_EqualValueSetsWithoutNull() {
        EnumValueSet superset = new EnumValueSet(cValueSet, "id1");
        superset.setContainsNull(false);
        EnumValueSet subset = new EnumValueSet(cValueSet, "id2");
        subset.setContainsNull(false);

        assertTrue(superset.containsValueSet(subset));
    }

    @Test
    public void testContainsValueSet_ContainsSubValueSet() {
        EnumValueSet superset = new EnumValueSet(cValueSet, "id1");
        superset.setContainsNull(true);
        EnumValueSet subset = new EnumValueSet(cValueSet, "id2");
        subset.setContainsNull(false);

        assertTrue(superset.containsValueSet(subset));
    }

    @Test
    public void testContainsValueSet_ContainsSubValueSetNot() {
        EnumValueSet superset = new EnumValueSet(cValueSet, "id1");
        superset.setContainsNull(false);
        EnumValueSet subset = new EnumValueSet(cValueSet, "id2");
        subset.setContainsNull(true);

        assertFalse(superset.containsValueSet(subset));
    }

    @Test
    public void testContainsValueSet_differentProjects() {
        EnumValueSet superset = new EnumValueSet(attr, "id1");
        EnumValueSet subset = new EnumValueSet(cValueSet, "id2");

        boolean containsValueSet = superset.containsValueSet(subset);

        assertTrue(containsValueSet);
    }

    @Test
    public void testContainsValueSet_differentProjects_extensibleEnum() {
        attr.setDatatype(MY_EXTENSIBLE_ENUM);
        EnumValueSet superset = new EnumValueSet(attr, "id1");
        EnumValueSet subset = new EnumValueSet(cValueSet, "id2");

        boolean containsValueSet = superset.containsValueSet(subset);

        assertTrue(containsValueSet);
    }

    @Test
    public void testAddValue() {
        IEnumValueSet set = new EnumValueSet(cValueSet, "1");
        set.addValue("one");
        set.addValue("two");
        set.addValue("one");
        assertEquals(3, set.getValues().length);
        assertEquals("one", set.getValue(0));
        assertEquals("two", set.getValue(1));
        assertEquals("one", set.getValue(2));
    }

    @Test
    public void testRemoveValue_byValue() {
        IEnumValueSet set = new EnumValueSet(cValueSet, "1");
        set.addValue("one");
        set.addValue("two");
        set.addValue("three");
        set.addValue("one");
        set.addValue("four");
        set.addValue("one");
        set.addValue("three");
        set.addValue("five");

        // remove something that is not part of the set
        set.removeValue("unknown");
        String[] values = set.getValues();
        assertEquals(8, values.length);

        // "four" has one occurance
        set.removeValue("four");
        values = set.getValues();
        assertEquals(7, values.length);
        assertEquals("one", values[0]);
        assertEquals("two", values[1]);
        assertEquals("three", values[2]);
        assertEquals("one", values[3]);
        assertEquals("three", values[5]);
        assertEquals("one", values[4]);
        assertEquals("five", values[6]);

        // "one" has multiple occurances and is first entry
        set.removeValue("one");
        values = set.getValues();
        assertEquals(4, values.length);
        assertEquals("two", values[0]);
        assertEquals("three", values[1]);
        assertEquals("three", values[2]);
        assertEquals("five", values[3]);

        // "five" has one occurance and is last index
        set.removeValue("five");
        values = set.getValues();
        assertEquals(3, values.length);
        assertEquals("two", values[0]);
        assertEquals("three", values[1]);
        assertEquals("three", values[2]);

        // "three" has multiple occurances and is last entry
        set.removeValue("three");
        values = set.getValues();
        assertEquals(1, values.length);
        assertEquals("two", values[0]);

        // "three" has multiple occurances and is last entry
        set.removeValue("two");
        values = set.getValues();
        assertEquals(0, values.length);

    }

    @Test
    public void testRemoveValue_byIndex() {
        IEnumValueSet set = new EnumValueSet(cValueSet, "1");
        set.addValue("one");
        set.addValue("two");
        set.addValue("one");
        assertEquals(3, set.getValues().length);
        List<Integer> positions = set.getPositions("one");
        assertEquals(2, positions.size());
        assertEquals(0, positions.get(0).intValue());
        assertEquals(2, positions.get(1).intValue());
        positions = set.getPositions("two");
        assertEquals(1, positions.size());
        assertEquals(1, positions.get(0).intValue());

        set.removeValue(0);
        // set is now: two, one
        assertEquals(2, set.getValues().length);
        assertEquals("two", set.getValue(0));
        positions = set.getPositions("one");
        assertEquals(1, positions.size());
        assertEquals(1, positions.get(0).intValue());
        positions = set.getPositions("two");
        assertEquals(1, positions.size());
        assertEquals(0, positions.get(0).intValue());

        set.removeValue(0);
        // set is now: one
        assertEquals(1, set.getValues().length);
        assertEquals("one", set.getValue(0));
        positions = set.getPositions("one");
        assertEquals(1, positions.size());
        assertEquals(0, positions.get(0).intValue());
        positions = set.getPositions("two");
        assertEquals(0, positions.size());

        set.removeValue(0);
        // set is now empty
        assertEquals(0, set.getValues().length);
        positions = set.getPositions("one");
        assertEquals(0, positions.size());
        positions = set.getPositions("two");
        assertEquals(0, positions.size());
    }

    @Test
    public void testRemoveValue_byIndex_LastEntry() {
    }

    @Test
    public void testGetValue() {
        IEnumValueSet set = new EnumValueSet(cValueSet, "1");
        set.addValue("one");
        set.addValue("two");
        set.addValue("two");

        assertEquals("one", set.getValue(0));
        assertEquals("two", set.getValue(1));
        assertEquals("two", set.getValue(2));

    }

    @Test
    public void testSetValue_oldValueHasOneOccurenceBeforeTheOperation_NewValueHasNoOccurenceBeforeTheOperation() {
        IEnumValueSet set = new EnumValueSet(cValueSet, "1");
        set.addValue("one");
        set.addValue("two");
        set.addValue("three");

        set.setValue(1, "four");
        assertEquals("four", set.getValue(1));
        List<Integer> positions = set.getPositions("two");
        assertEquals(0, positions.size());
        positions = set.getPositions("four");
        assertEquals(1, positions.size());
        assertEquals(1, positions.get(0).intValue());
    }

    @Test
    public void testSetValue_oldValueHasOneOccurenceBeforeTheOperation_NewValueHasOneOccurenceBeforeTheOperation() {
        IEnumValueSet set = new EnumValueSet(cValueSet, "1");
        set.addValue("one");
        set.addValue("two");
        set.addValue("three");

        set.setValue(1, "three");
        assertEquals("three", set.getValue(1));
        List<Integer> positions = set.getPositions("two");
        assertEquals(0, positions.size());
        positions = set.getPositions("three");
        assertEquals(2, positions.size());
        assertEquals(1, positions.get(0).intValue());
        assertEquals(2, positions.get(1).intValue());
    }

    @Test
    public void testSetValue_oldValueHasOneOccurenceBeforeTheOperation_NewValueHasMultipleOccurencesBeforeTheOperation() {
        IEnumValueSet set = new EnumValueSet(cValueSet, "1");
        set.addValue("one");
        set.addValue("two");
        set.addValue("three");
        set.addValue("three");

        set.setValue(1, "three");
        assertEquals("three", set.getValue(1));
        List<Integer> positions = set.getPositions("two");
        assertEquals(0, positions.size());
        positions = set.getPositions("three");
        assertEquals(3, positions.size());
        assertEquals(1, positions.get(0).intValue());
        assertEquals(2, positions.get(1).intValue());
        assertEquals(3, positions.get(2).intValue());
    }

    @Test
    public void testSetValue_oldValueHasMultipleOccurencesBeforeAndOneAfterTheOperation_NewValueHasNoOccurenceBeforeTheOperation() {
        IEnumValueSet set = new EnumValueSet(cValueSet, "1");
        set.addValue("one");
        set.addValue("two");
        set.addValue("two");

        set.setValue(1, "three");
        assertEquals("three", set.getValue(1));
        List<Integer> positions = set.getPositions("two");
        assertEquals(1, positions.size());
        assertEquals(2, positions.get(0).intValue());
        positions = set.getPositions("three");
        assertEquals(1, positions.size());
        assertEquals(1, positions.get(0).intValue());
    }

    @Test
    public void testSetValue_oldValueHasMultipleOccurencesBeforeAndOneAfterTheOperation_NewValueHasOneOccurenceBeforeTheOperation() {
        IEnumValueSet set = new EnumValueSet(cValueSet, "1");
        set.addValue("one");
        set.addValue("two");
        set.addValue("two");

        set.setValue(1, "one");
        assertEquals("one", set.getValue(1));
        List<Integer> positions = set.getPositions("two");
        assertEquals(1, positions.size());
        assertEquals(2, positions.get(0).intValue());
        positions = set.getPositions("one");
        assertEquals(2, positions.size());
        assertEquals(0, positions.get(0).intValue());
        assertEquals(1, positions.get(1).intValue());
    }

    @Test
    public void testSetValue_oldValueHasMultipleOccurencesBeforeAndOneAfterTheOperation_NewValueHasMultipleOccurencesBeforeTheOperation() {
        IEnumValueSet set = new EnumValueSet(cValueSet, "1");
        set.addValue("one");
        set.addValue("two");
        set.addValue("two");
        set.addValue("one");

        set.setValue(1, "one");
        assertEquals("one", set.getValue(1));
        List<Integer> positions = set.getPositions("two");
        assertEquals(1, positions.size());
        assertEquals(2, positions.get(0).intValue());
        positions = set.getPositions("one");
        assertEquals(3, positions.size());
        assertEquals(0, positions.get(0).intValue());
        assertEquals(1, positions.get(1).intValue());
        assertEquals(3, positions.get(2).intValue());
    }

    @Test
    public void testSetValue_oldValueHasMultipleOccurencesBeforeAndAfterTheOperation_NewValueHasNoOccurenceBeforeTheOperation() {
        IEnumValueSet set = new EnumValueSet(cValueSet, "1");
        set.addValue("one");
        set.addValue("two");
        set.addValue("two");
        set.addValue("two");

        set.setValue(1, "three");
        assertEquals("three", set.getValue(1));
        List<Integer> positions = set.getPositions("two");
        assertEquals(2, positions.size());
        assertEquals(2, positions.get(0).intValue());
        assertEquals(3, positions.get(1).intValue());
        positions = set.getPositions("three");
        assertEquals(1, positions.size());
        assertEquals(1, positions.get(0).intValue());
    }

    @Test
    public void testSetValue_oldValueHasMultipleOccurencesBeforeAndAfterTheOperation_NewValueHasOneOccurenceBeforeTheOperation() {
        IEnumValueSet set = new EnumValueSet(cValueSet, "1");
        set.addValue("one");
        set.addValue("two");
        set.addValue("two");
        set.addValue("two");

        set.setValue(1, "one");
        assertEquals("one", set.getValue(1));
        List<Integer> positions = set.getPositions("two");
        assertEquals(2, positions.size());
        assertEquals(2, positions.get(0).intValue());
        assertEquals(3, positions.get(1).intValue());
        positions = set.getPositions("one");
        assertEquals(2, positions.size());
        assertEquals(0, positions.get(0).intValue());
        assertEquals(1, positions.get(1).intValue());
    }

    @Test
    public void testSetValue_oldValueHasMultipleOccurencesBeforeAndAfterTheOperation_NewValueHasMultipleOccurencesBeforeTheOperation() {
        IEnumValueSet set = new EnumValueSet(cValueSet, "1");
        set.addValue("one");
        set.addValue("two");
        set.addValue("two");
        set.addValue("two");
        set.addValue("one");

        set.setValue(1, "one");
        assertEquals("one", set.getValue(1));
        List<Integer> positions = set.getPositions("two");
        assertEquals(2, positions.size());
        assertEquals(2, positions.get(0).intValue());
        assertEquals(3, positions.get(1).intValue());
        positions = set.getPositions("one");
        assertEquals(3, positions.size());
        assertEquals(0, positions.get(0).intValue());
        assertEquals(1, positions.get(1).intValue());
        assertEquals(4, positions.get(2).intValue());
    }

    @Test
    public void testCreateFromXml() {
        Document doc = getTestDocument();
        Element root = doc.getDocumentElement();
        Element element = XmlUtil.getFirstElement(root);
        IEnumValueSet set = new EnumValueSet(cValueSet, "1");
        set.initFromXml(element);
        assertEquals("one", set.getValue(0));
        assertEquals("two", set.getValue(1));
        assertEquals("three", set.getValue(2));
    }

    @Test
    public void testToXml() {
        EnumValueSet set = new EnumValueSet(cValueSet, "1");
        set.addValue("one");
        set.addValue("two");
        set.addValue("two");
        set.setContainsNull(true);
        Element element = set.toXml(newDocument());
        IEnumValueSet set2 = new EnumValueSet(cValueSet, "1");
        set2.initFromXml(element);
        assertEquals("one", set2.getValue(0));
        assertEquals("two", set2.getValue(1));
        assertEquals("two", set2.getValue(2));
        assertTrue(set2.isContainsNull());

    }

    @Test
    public void testValidate() throws Exception {
        EnumValueSet set = new EnumValueSet(cValueSet, "1");
        MessageList list = set.validate(ipsProject);
        assertEquals(0, list.size());

        set.addValue("2EUR");
        list = set.validate(ipsProject);
        assertEquals(0, list.size());

        set.addValue("2w");
        list = set.validate(ipsProject);
        assertEquals(1, list.size());

        Datatype[] vds = ipsProject.findDatatypes(true, false);
        ArrayList<Datatype> vdlist = new ArrayList<>();
        vdlist.addAll(Arrays.asList(vds));
        vdlist.add(new PrimitiveIntegerDatatype());
        IIpsProjectProperties properties = ipsProject.getProperties();
        properties.setPredefinedDatatypesUsed(vdlist.toArray(new ValueDatatype[vdlist.size()]));
        ipsProject.setProperties(properties);

        IPolicyCmptTypeAttribute attr = cValueSet.findPcTypeAttribute(ipsProject);
        attr.setDatatype(Datatype.PRIMITIVE_INT.getQualifiedName());
        attr.getIpsObject().getIpsSrcFile().save(null);

        list.clear();
        list = set.validate(ipsProject);
        assertNotNull(list
                .getMessageByCode(IValidationMsgCodesForInvalidValues.MSGCODE_VALUE_IS_NOT_INSTANCE_OF_VALUEDATATYPE));

        set.removeValue(0);
        set.removeValue(0);
        set.addValue("1");
        set.addValue(null);
        list.clear();
        list = set.validate(ipsProject);
        assertNotNull(list.getMessageByCode(IValueSet.MSGCODE_NULL_NOT_SUPPORTED));

        // test with unkonwn datatype
        EnumValueSet set2 = new EnumValueSet(cValueSet, "2");
        set2.addValue("1");
        set2.addValue("2");
        list = set2.validate(ipsProject);
        assertEquals(0, list.size());

        ((IProductCmptGeneration)cValueSet.getPropertyValueContainer()).getProductCmpt().setProductCmptType("unkown");
        list = set2.validate(ipsProject);
        assertEquals(1, list.size());
        assertThat(list, hasMessageCode(IValueSet.MSGCODE_UNKNOWN_DATATYPE));
        assertThat(list.getMessageByCode(IValueSet.MSGCODE_UNKNOWN_DATATYPE).getSeverity(), is(Message.WARNING));
    }

    @Test
    public void testValidate_CheckDuplicates() {
        EnumValueSet set = new EnumValueSet(cValueSet, "1");
        MessageList list = set.validate(ipsProject);
        assertEquals(0, list.size());

        set.addValue("2EUR");
        list = set.validate(ipsProject);
        assertEquals(0, list.size());

        set.addValue("3EUR");
        list = set.validate(ipsProject);
        assertEquals(0, list.size());

        set.addValue("2EUR");
        list = set.validate(ipsProject);
        assertEquals(1, list.size());
        Message msg0 = list.getMessage(0);
        assertEquals(IEnumValueSet.MSGCODE_DUPLICATE_VALUE, msg0.getCode());
        List<ObjectProperty> ops = msg0.getInvalidObjectProperties();
        assertEquals(3, ops.size());
        assertEquals(cValueSet, ops.get(0).getObject());
        assertEquals(0, ops.get(1).getIndex());
        assertEquals(2, ops.get(2).getIndex());

        set.addValue("2EUR");
        list = set.validate(ipsProject);
        assertEquals(1, list.size());
        msg0 = list.getMessage(0);
        ops = msg0.getInvalidObjectProperties();
        assertEquals(4, ops.size());

        set.setValue(3, "4EUR");
        list = set.validate(ipsProject);
        assertEquals(1, list.size());
        msg0 = list.getMessage(0);
        ops = msg0.getInvalidObjectProperties();
        assertEquals(3, ops.size());
    }

    @Test
    public void testValidateValue() {
        EnumValueSet set = new EnumValueSet(cValueSet, "1");
        set.addValue("2EUR");
        set.addValue("3EUR");
        set.addValue("2EUR");

        MessageList list = set.validateValue(0, ipsProject);
        assertEquals(1, list.size());
        Message msg0 = list.getMessage(0);
        assertEquals(IEnumValueSet.MSGCODE_DUPLICATE_VALUE, msg0.getCode());
        List<ObjectProperty> ops = msg0.getInvalidObjectProperties();
        assertEquals(1, ops.size());
        assertEquals(0, ops.get(0).getIndex());

        list = set.validateValue(1, ipsProject);
        assertTrue(list.isEmpty());

        list = set.validateValue(2, ipsProject);
        assertEquals(1, list.size());
        msg0 = list.getMessage(0);
        assertEquals(IEnumValueSet.MSGCODE_DUPLICATE_VALUE, msg0.getCode());
        ops = msg0.getInvalidObjectProperties();
        assertEquals(1, ops.size());
        assertEquals(2, ops.get(0).getIndex());
    }

    @Test
    public void testGetValues() {
        EnumValueSet set = new EnumValueSet(cValueSet, "50");
        String[] values = set.getValues();

        assertEquals(0, values.length);

        set.addValue("1");
        values = set.getValues();
        assertEquals(1, values.length);

        set.addValue(null);
        values = set.getValues();
        assertEquals(2, values.length);
    }

    @Test
    public void testIsContainsNull_SetContainsNull() {
        EnumValueSet set = new EnumValueSet(cValueSet, "50");

        set.setContainsNull(true);
        assertTrue(set.isContainsNull());

        set.setContainsNull(false);
        assertFalse(set.isContainsNull());
    }

    @Test
    public void testAddValues() {
        EnumValueSet set = spy(new EnumValueSet(cValueSet, "50"));
        ContentsChangeListener mockedListener = mock(ContentsChangeListener.class);
        set.getIpsModel().addChangeListener(mockedListener);
        set.getIpsModel().addChangeListener(mockedListener);

        set.addValue("1");
        set.addValue("two");
        set.addValue("THREE");
        assertEquals(3, set.size());
        verify(mockedListener, times(3)).contentsChanged(any(ContentChangeEvent.class));

        List<String> values = new ArrayList<>();
        values.add("4");
        values.add("five");
        values.add("SIX");

        set.addValues(values);
        assertEquals(6, set.size());
        verify(mockedListener, times(4)).contentsChanged(any(ContentChangeEvent.class));

        set.getIpsModel().removeChangeListener(mockedListener);
    }

    @Test
    public void testRemoveValues() {
        EnumValueSet set = spy(new EnumValueSet(cValueSet, "50"));
        ContentsChangeListener mockedListener = mock(ContentsChangeListener.class);
        set.getIpsModel().addChangeListener(mockedListener);

        set.addValue("1");
        set.addValue("two");
        set.addValue("THREE");
        set.addValue("4");
        set.addValue("five");
        set.addValue("SIX");
        assertEquals(6, set.size());
        verify(mockedListener, times(6)).contentsChanged(any(ContentChangeEvent.class));

        List<String> values = new ArrayList<>();
        values.add("two");
        values.add("4");
        values.add("SIX");

        set.removeValues(values);
        assertEquals(3, set.size());
        verify(mockedListener, times(7)).contentsChanged(any(ContentChangeEvent.class));

        set.getIpsModel().removeChangeListener(mockedListener);
    }

    @Test
    public void testFormatList_empty() throws Exception {
        EnumValueSet enumValueSet = new EnumValueSet(cValueSet, "id");

        String shortString = enumValueSet.toShortString();

        assertEquals("{}", shortString);
    }

    @Test
    public void testFormatList_oneElement() throws Exception {
        EnumValueSet enumValueSet = new EnumValueSet(cValueSet, Arrays.asList("a"), "id");

        String shortString = enumValueSet.toShortString();

        assertEquals("{a}", shortString);
    }

    @Test
    public void testFormatList_twoElement() throws Exception {
        EnumValueSet enumValueSet = new EnumValueSet(cValueSet, Arrays.asList("a", "b"), "id");

        String shortString = enumValueSet.toShortString();

        assertEquals("{a | b}", shortString);
    }

    @Test
    public void testFormatList_EnumValueSet_abstract_excludingNull() throws Exception {
        EnumValueSet enumValueSet = new EnumValueSet(cValueSet, "id");
        enumValueSet.setAbstract(true);

        String shortString = enumValueSet.toShortString();

        String nullText = NLS.bind(Messages.ValueSet_excludingNull,
                IIpsModelExtensions.get().getModelPreferences().getNullPresentation());
        assertEquals(NLS.bind(Messages.EnumValueSet_abstract, nullText), shortString);
    }

    @Test
    public void testFormatList_EnumValueSet_abstract_includingNull() throws Exception {
        EnumValueSet enumValueSet = new EnumValueSet(cValueSet, "id");
        enumValueSet.setAbstract(true);
        enumValueSet.setContainsNull(true);

        String shortString = enumValueSet.toShortString();

        String nullText = NLS.bind(Messages.ValueSet_includingNull,
                IIpsModelExtensions.get().getModelPreferences().getNullPresentation());
        assertEquals(NLS.bind(Messages.EnumValueSet_abstract, nullText), shortString);
    }

    @Test
    public void testCompareTo_Eq_empty() throws Exception {
        IEnumValueSet enum1 = createEnumValueSet();
        IEnumValueSet enum2 = createEnumValueSet();

        assertThat(enum1.compareTo(enum2), is(0));
        assertThat(enum2.compareTo(enum1), is(0));
    }

    @Test
    public void testCompareTo_Eq_anyValue() throws Exception {
        IEnumValueSet enum1 = createEnumValueSet("abc123");
        IEnumValueSet enum2 = createEnumValueSet("abc123");

        assertThat(enum1.compareTo(enum2), is(0));
        assertThat(enum2.compareTo(enum1), is(0));
    }

    @Test
    public void testCompareTo_Eq_DatatypeCompare() throws Exception {
        attr.setDatatype(Datatype.INTEGER.getQualifiedName());
        IEnumValueSet enum1 = createEnumValueSet("0001");
        IEnumValueSet enum2 = createEnumValueSet("1");

        assertThat(enum1.compareTo(enum2), is(0));
        assertThat(enum2.compareTo(enum1), is(0));
    }

    @Test
    public void testCompareTo_Eq_MultipleValues() throws Exception {
        IEnumValueSet enum1 = createEnumValueSet("1", "asdf", "");
        IEnumValueSet enum2 = createEnumValueSet("1", "asdf", "");

        assertThat(enum1.compareTo(enum2), is(0));
        assertThat(enum2.compareTo(enum1), is(0));
    }

    @Test
    public void testCompareTo_Eq_MultipleValues_InklNullAndEmpty() throws Exception {
        IEnumValueSet enum1 = createEnumValueSet("", null, "");
        IEnumValueSet enum2 = createEnumValueSet("", null, "");

        assertThat(enum1.compareTo(enum2), is(0));
        assertThat(enum2.compareTo(enum1), is(0));
    }

    @Test
    public void testCompareTo_Empty_NonEmpty() throws Exception {
        IEnumValueSet enum1 = createEnumValueSet();
        IEnumValueSet enum2 = createEnumValueSet("");

        assertThat(enum1.compareTo(enum2), is(-1));
        assertThat(enum2.compareTo(enum1), is(1));
    }

    @Test
    public void testCompareTo_DifferentValues() throws Exception {
        IEnumValueSet enum1 = createEnumValueSet("123");
        IEnumValueSet enum2 = createEnumValueSet("312");

        // less-than matcher not available in current hamcrest version
        assertTrue(enum1.compareTo(enum2) < 0);
        assertTrue(enum2.compareTo(enum1) > 0);
    }

    @Test
    public void testCompareTo_DifferentAmountOfSameValues() throws Exception {
        IEnumValueSet enum1 = createEnumValueSet("1");
        IEnumValueSet enum2 = createEnumValueSet("1", "1");

        assertThat(enum1.compareTo(enum2), is(-1));
        assertThat(enum2.compareTo(enum1), is(1));
    }

    @Test
    public void testCompareTo_DifferentAmountOfValues() throws Exception {
        IEnumValueSet enum1 = createEnumValueSet("1");
        IEnumValueSet enum2 = createEnumValueSet("1", "2");

        assertThat(enum1.compareTo(enum2), is(-1));
        assertThat(enum2.compareTo(enum1), is(1));
    }

    @Test
    public void testCompareTo_FirstValuesSame() throws Exception {
        IEnumValueSet enum1 = createEnumValueSet("1", "1", "2");
        IEnumValueSet enum2 = createEnumValueSet("1", "1", "3");

        assertThat(enum1.compareTo(enum2), is(-1));
        assertThat(enum2.compareTo(enum1), is(1));
    }

    @Test
    public void testCompareTo_InklNull() throws Exception {
        IEnumValueSet enum1 = createEnumValueSet(null, "1", "2");
        IEnumValueSet enum2 = createEnumValueSet("1", "1", "3");

        assertThat(enum1.compareTo(enum2), is(-1));
        assertThat(enum2.compareTo(enum1), is(1));
    }

    @Test
    public void testCompareTo_SameValuesDifferentOrder() throws Exception {
        IEnumValueSet enum1 = createEnumValueSet("2", "1", "3");
        IEnumValueSet enum2 = createEnumValueSet("1", "2", "3");

        assertThat(enum1.compareTo(enum2), is(1));
        assertThat(enum2.compareTo(enum1), is(-1));
    }

    @Test
    public void testCompareTo_transitivity() throws Exception {
        IEnumValueSet enum1 = createEnumValueSet("1", "2");
        IEnumValueSet enum2 = createEnumValueSet("2", "1");
        IEnumValueSet enum3 = createEnumValueSet("2", "2");

        assertThat(enum1.compareTo(enum2), is(-1));
        assertThat(enum2.compareTo(enum3), is(-1));
        assertThat(enum1.compareTo(enum3), is(-1));
        assertThat(enum3.compareTo(enum1), is(1));
        assertThat(enum3.compareTo(enum2), is(1));
        assertThat(enum2.compareTo(enum1), is(1));
    }

    @Test
    public void testCompareTo_SameValuesDifferentDatatypes() throws Exception {
        attr.setDatatype(Datatype.INTEGER.getQualifiedName());
        IEnumValueSet enum1 = createEnumValueSet("1", "2", "3");

        IPolicyCmptTypeAttribute attr2 = policyCmptType.newPolicyCmptTypeAttribute();
        attr2.setName("attr2");
        attr2.setDatatype(Datatype.DECIMAL.getQualifiedName());
        IConfiguredValueSet cValueSet2 = generation.newPropertyValue(attr2, IConfiguredValueSet.class);
        EnumValueSet enum2 = new EnumValueSet(cValueSet2, "id");
        enum2.addValues(Arrays.asList("1", "2", "3"));

        assertTrue(enum1.compareTo(enum2) > 0);
        assertTrue(enum2.compareTo(enum1) < 0);
    }

    private IEnumValueSet createEnumValueSet(String... values) {
        EnumValueSet enumValueSet = new EnumValueSet(cValueSet, "id");
        enumValueSet.addValues(Arrays.asList(values));
        return enumValueSet;
    }

    @Test
    public void testMove() throws Exception {
        attr.setDatatype(Datatype.STRING.getQualifiedName());
        IEnumValueSet valueSet = createEnumValueSet("1", "2", "3", "4", "5");

        List<Integer> indices = Arrays.asList(1);
        valueSet.move(indices, true);

        String[] values = valueSet.getValues();
        assertThat(values[0], is("2"));
        assertThat(values[1], is("1"));
        assertThat(values[2], is("3"));
        assertThat(values[3], is("4"));
        assertThat(values[4], is("5"));
    }

    @Test
    public void testMove_Multiple() throws Exception {
        attr.setDatatype(Datatype.STRING.getQualifiedName());
        IEnumValueSet valueSet = createEnumValueSet("1", "2", "3", "4", "5");

        List<Integer> indices = Arrays.asList(1, 3, 4);
        valueSet.move(indices, true);

        String[] values = valueSet.getValues();
        assertThat(values[0], is("2"));
        assertThat(values[1], is("1"));
        assertThat(values[2], is("4"));
        assertThat(values[3], is("5"));
        assertThat(values[4], is("3"));
    }

}
