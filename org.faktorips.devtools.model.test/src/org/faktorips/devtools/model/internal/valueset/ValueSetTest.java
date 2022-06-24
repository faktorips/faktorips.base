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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.abstracttest.builder.TestIpsArtefactBuilderSet;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.valueset.IEnumValueSet;
import org.faktorips.devtools.model.valueset.IRangeValueSet;
import org.faktorips.devtools.model.valueset.IUnrestrictedValueSet;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.devtools.model.valueset.ValueSetType;
import org.junit.Before;
import org.junit.Test;

public class ValueSetTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IPolicyCmptTypeAttribute owner1;
    private IPolicyCmptTypeAttribute owner2;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        IPolicyCmptType type = newPolicyCmptType(ipsProject, "Policy");
        owner1 = type.newPolicyCmptTypeAttribute();
        owner1.setDatatype(Datatype.INTEGER.getName());
        owner2 = type.newPolicyCmptTypeAttribute();
        owner2.setDatatype(Datatype.INTEGER.getName());
    }

    @Test
    public void testIsDetailedSpecificationOf_Unrestricted() {
        IValueSet set1 = owner1.changeValueSetType(ValueSetType.UNRESTRICTED);
        set1.setContainsNull(true);

        // unrestricted
        IValueSet set2 = owner2.changeValueSetType(ValueSetType.UNRESTRICTED);
        set2.setContainsNull(false);
        assertTrue(set2.isDetailedSpecificationOf(set1));
        set1.setContainsNull(false);
        set2.setContainsNull(true);
        assertFalse(set2.isDetailedSpecificationOf(set1));

        // enum
        set1.setContainsNull(true);
        set2 = owner2.changeValueSetType(ValueSetType.ENUM);
        set2.setAbstract(true);
        assertTrue(set2.isDetailedSpecificationOf(set1));
        set2.setAbstract(false);
        assertTrue(set2.isDetailedSpecificationOf(set1));
        set1.setContainsNull(false);
        set2.setContainsNull(true);
        assertFalse(set2.isDetailedSpecificationOf(set1));

        // range
        set1.setContainsNull(true);
        set2 = owner2.changeValueSetType(ValueSetType.RANGE);
        set2.setAbstract(true);
        assertTrue(set2.isDetailedSpecificationOf(set1));
        set2.setAbstract(false);
        assertTrue(set2.isDetailedSpecificationOf(set1));
        set1.setContainsNull(false);
        set2.setContainsNull(true);
        assertFalse(set2.isDetailedSpecificationOf(set1));

        // derived
        set1.setContainsNull(true);
        set2 = owner2.changeValueSetType(ValueSetType.DERIVED);
        set2.setAbstract(true);
        assertTrue(set2.isDetailedSpecificationOf(set1));
        set2.setAbstract(false);
        assertTrue(set2.isDetailedSpecificationOf(set1));
        set1.setContainsNull(false);
        assertTrue(set2.isDetailedSpecificationOf(set1));

        // stringLength
        set1.setContainsNull(true);
        set2 = owner2.changeValueSetType(ValueSetType.STRINGLENGTH);
        set2.setAbstract(true);
        assertTrue(set2.isDetailedSpecificationOf(set1));
        set2.setAbstract(false);
        assertTrue(set2.isDetailedSpecificationOf(set1));
        set1.setContainsNull(false);
        assertTrue(set2.isDetailedSpecificationOf(set1));
        set1.setContainsNull(false);
        set2.setContainsNull(true);
        assertFalse(set2.isDetailedSpecificationOf(set1));
    }

    @Test
    public void testIsDetailedSpecificationOf_Derived() {
        IValueSet set1 = owner1.changeValueSetType(ValueSetType.DERIVED);

        // unrestricted
        IValueSet set2 = owner2.changeValueSetType(ValueSetType.UNRESTRICTED);
        set2.setContainsNull(false);
        assertTrue(set2.isDetailedSpecificationOf(set1));
        set2.setContainsNull(true);
        assertTrue(set2.isDetailedSpecificationOf(set1));

        // enum
        set2 = owner2.changeValueSetType(ValueSetType.ENUM);
        set2.setAbstract(true);
        assertTrue(set2.isDetailedSpecificationOf(set1));
        set2.setAbstract(false);
        assertTrue(set2.isDetailedSpecificationOf(set1));
        set2.setContainsNull(true);
        assertTrue(set2.isDetailedSpecificationOf(set1));

        // range
        set2 = owner2.changeValueSetType(ValueSetType.RANGE);
        set2.setAbstract(true);
        assertTrue(set2.isDetailedSpecificationOf(set1));
        set2.setAbstract(false);
        assertTrue(set2.isDetailedSpecificationOf(set1));
        set2.setContainsNull(true);
        assertTrue(set2.isDetailedSpecificationOf(set1));

        // derived
        set2 = owner2.changeValueSetType(ValueSetType.DERIVED);
        set2.setAbstract(true);
        assertTrue(set2.isDetailedSpecificationOf(set1));
        set2.setAbstract(false);
        assertTrue(set2.isDetailedSpecificationOf(set1));

        // stringLength
        set2 = owner2.changeValueSetType(ValueSetType.STRINGLENGTH);
        set2.setAbstract(true);
        assertTrue(set2.isDetailedSpecificationOf(set1));
        set2.setAbstract(false);
        assertTrue(set2.isDetailedSpecificationOf(set1));
    }

    @Test
    public void testIsDetailedSpecificationOf_Range() {
        IValueSet set1 = owner1.changeValueSetType(ValueSetType.RANGE);
        set1.setAbstract(true);

        IValueSet set2 = owner2.changeValueSetType(ValueSetType.UNRESTRICTED);
        assertFalse(set2.isDetailedSpecificationOf(set1));

        // abstract enum
        set2 = owner2.changeValueSetType(ValueSetType.ENUM);
        assertFalse(set2.isDetailedSpecificationOf(set1));

        // not unified value set methods
        ((TestIpsArtefactBuilderSet)ipsProject.getIpsArtefactBuilderSet()).setUsesUnifiedValueSets(true);
        assertTrue(set2.isDetailedSpecificationOf(set1));

        // concrete enum of concrete range
        IRangeValueSet range1 = (IRangeValueSet)set1;
        range1.setAbstract(false);
        range1.setLowerBound("0");
        range1.setUpperBound("10");
        IEnumValueSet enum2 = (IEnumValueSet)set2;
        enum2.addValue("1");
        enum2.addValue("5");
        assertTrue(set2.isDetailedSpecificationOf(set1));

        // concrete enum of concrete range - step mismatch
        range1.setStep("5");
        assertFalse(set2.isDetailedSpecificationOf(set1));

        // concrete enum of concrete range - outside range
        range1.setStep("1");
        enum2.addValue("99");
        assertFalse(set2.isDetailedSpecificationOf(set1));

        range1.setStep("");
        range1.setLowerBound("");
        range1.setUpperBound("");
        range1.setAbstract(true);

        // both ranges abstract => true
        set2 = owner2.changeValueSetType(ValueSetType.RANGE);
        set2.setAbstract(true);
        assertTrue(set2.isDetailedSpecificationOf(set1));

        // super range candidate abstract, 'this' range concrete => true
        IRangeValueSet range2 = (IRangeValueSet)set2;
        range2.setAbstract(false);
        range2.setLowerBound("0");
        range2.setUpperBound("10");
        assertTrue(range2.isDetailedSpecificationOf(set1));

        // super range candidate concrete, 'this' range abstract => false
        assertFalse(set1.isDetailedSpecificationOf(range2));

        // super range candidate concrete, 'this' range concrete
        range1 = (IRangeValueSet)set1;
        range1.setAbstract(false);
        range1.setLowerBound("0");
        range1.setUpperBound("9");
        assertFalse(range2.isDetailedSpecificationOf(range1));
        assertTrue(range1.isDetailedSpecificationOf(range2));

        // super range candidate contains not null, 'this' range contains null
        range1.setContainsNull(false);
        range2.setContainsNull(true);
        assertFalse(range2.isDetailedSpecificationOf(range1));

        // super range candidate contains null, 'this' range contains not null
        range1.setContainsNull(true);
        range1.setUpperBound("10");
        range2.setContainsNull(false);
        assertTrue(range2.isDetailedSpecificationOf(range1));

        // derived
        set1.setContainsNull(true);
        set2 = owner2.changeValueSetType(ValueSetType.DERIVED);
        set2.setAbstract(true);
        assertFalse(set2.isDetailedSpecificationOf(set1));
        set2.setAbstract(false);
        assertFalse(set2.isDetailedSpecificationOf(set1));
        set1.setContainsNull(false);
        assertFalse(set2.isDetailedSpecificationOf(set1));

        // stringLength
        set1.setContainsNull(true);
        set2 = owner2.changeValueSetType(ValueSetType.STRINGLENGTH);
        assertFalse(set2.isDetailedSpecificationOf(set1));
    }

    @Test
    public void testIsDetailedSpecificationOf_Enum() {
        IValueSet set1 = owner1.changeValueSetType(ValueSetType.ENUM);
        set1.setAbstract(true);

        IValueSet set2 = owner2.changeValueSetType(ValueSetType.UNRESTRICTED);
        assertFalse(set2.isDetailedSpecificationOf(set1));

        set2 = owner2.changeValueSetType(ValueSetType.RANGE);
        assertFalse(set2.isDetailedSpecificationOf(set1));

        // both enums abstract
        set2 = owner2.changeValueSetType(ValueSetType.ENUM);
        set2.setAbstract(true);
        assertTrue(set2.isDetailedSpecificationOf(set1));

        // super enum candidate abstract, 'this' enum concrete => true
        IEnumValueSet enum2 = (IEnumValueSet)set2;
        enum2.setAbstract(false);
        enum2.addValue("1");
        enum2.addValue("2");
        enum2.addValue("3");
        assertTrue(enum2.isDetailedSpecificationOf(set1));

        // super enum candidate concrete, 'this' enum abstract => false
        assertFalse(set1.isDetailedSpecificationOf(enum2));

        // super enum candidate concrete, 'this' enum concrete
        IEnumValueSet enum1 = (IEnumValueSet)set1;
        enum1.setAbstract(false);
        enum1.addValue("1");
        enum1.addValue("2");
        assertFalse(enum2.isDetailedSpecificationOf(enum1));
        assertTrue(enum1.isDetailedSpecificationOf(enum2));

        // super enum candidate contains not null, 'this' enum contains null
        enum1.setContainsNull(false);
        enum2.setContainsNull(true);
        assertFalse(enum2.isDetailedSpecificationOf(enum1));

        // super enum candidate contains null, 'this' enum contains not null
        enum1.setContainsNull(true);
        enum1.addValue("3");
        enum2.setContainsNull(false);
        assertTrue(enum2.isDetailedSpecificationOf(enum1));

        // derived
        set1.setContainsNull(true);
        set2 = owner2.changeValueSetType(ValueSetType.DERIVED);
        set2.setAbstract(true);
        assertFalse(set2.isDetailedSpecificationOf(set1));
        set2.setAbstract(false);
        assertFalse(set2.isDetailedSpecificationOf(set1));
        set1.setContainsNull(false);
        assertFalse(set2.isDetailedSpecificationOf(set1));

        // stringLength
        set1.setContainsNull(true);
        set2 = owner2.changeValueSetType(ValueSetType.STRINGLENGTH);
        assertFalse(set2.isDetailedSpecificationOf(set1));
    }

    @Test
    public void testIsDetailedSpecificationOf_StringLength() {
        IValueSet set1 = owner1.changeValueSetType(ValueSetType.STRINGLENGTH);
        set1.setAbstract(true);

        // unrestricted
        IValueSet set2 = owner2.changeValueSetType(ValueSetType.UNRESTRICTED);
        assertFalse(set2.isDetailedSpecificationOf(set1));

        // range
        set1.setContainsNull(true);
        set2 = owner2.changeValueSetType(ValueSetType.RANGE);
        assertFalse(set2.isDetailedSpecificationOf(set1));

        // enum
        set1.setContainsNull(true);
        set2 = owner2.changeValueSetType(ValueSetType.ENUM);
        set2.setAbstract(true);
        assertTrue(set2.isDetailedSpecificationOf(set1));
        set2.setAbstract(false);
        assertTrue(set2.isDetailedSpecificationOf(set1));
        set1.setContainsNull(false);
        set2.setContainsNull(true);
        assertFalse(set2.isDetailedSpecificationOf(set1));

        // derived
        set1.setContainsNull(true);
        set2 = owner2.changeValueSetType(ValueSetType.DERIVED);
        assertTrue(set2.isDetailedSpecificationOf(set1));

        // stringLength
        set1.setContainsNull(true);
        set2 = owner2.changeValueSetType(ValueSetType.STRINGLENGTH);
        set2.setAbstract(true);
        assertTrue(set2.isDetailedSpecificationOf(set1));
        set2.setAbstract(false);
        assertTrue(set2.isDetailedSpecificationOf(set1));
        set1.setContainsNull(false);
        assertTrue(set2.isDetailedSpecificationOf(set1));
    }

    @Test
    public void testIsContainingNullAllowed() {
        UnrestrictedValueSet unrestricted = new UnrestrictedValueSet(owner1, "1");
        unrestricted.setContainsNull(true);
        owner1.setDatatype(Datatype.PRIMITIVE_INT.getName());
        assertFalse(unrestricted.isContainingNullAllowed(ipsProject));
    }

    @Test
    public void testCompareTo_enum_range() throws Exception {
        IRangeValueSet range1 = new RangeValueSet(owner1, "123");
        IEnumValueSet enum1 = new EnumValueSet(owner1, "yx");

        assertThat(range1.compareTo(enum1), is(1));
        assertThat(enum1.compareTo(range1), is(-1));
    }

    @Test
    public void testCompareTo_unrestricted_range() throws Exception {
        IRangeValueSet range1 = new RangeValueSet(owner1, "123");
        IUnrestrictedValueSet unrestricted1 = new UnrestrictedValueSet(owner1, "yx");

        assertThat(range1.compareTo(unrestricted1), is(1));
        assertThat(unrestricted1.compareTo(range1), is(-1));
    }

    @Test
    public void testCompareTo_unrestricted_enum() throws Exception {
        IEnumValueSet enum1 = new EnumValueSet(owner1, "yx");
        IUnrestrictedValueSet unrestricted1 = new UnrestrictedValueSet(owner1, "yx");

        assertThat(enum1.compareTo(unrestricted1), is(1));
        assertThat(unrestricted1.compareTo(enum1), is(-1));
    }

}
