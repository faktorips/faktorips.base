/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.valueset;

import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IRangeValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;

public class ValueSetTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IPolicyCmptTypeAttribute owner1;
    private IPolicyCmptTypeAttribute owner2;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        IPolicyCmptType type = newPolicyCmptType(ipsProject, "Policy");
        owner1 = type.newPolicyCmptTypeAttribute();
        owner1.setDatatype(Datatype.INTEGER.getName());
        owner2 = type.newPolicyCmptTypeAttribute();
        owner2.setDatatype(Datatype.INTEGER.getName());
    }

    public void testIsDetailedSpecificationOf_Unrestricted() {
        IValueSet set1 = owner1.changeValueSetType(ValueSetType.UNRESTRICTED);

        // unrestricted
        IValueSet set2 = owner2.changeValueSetType(ValueSetType.UNRESTRICTED);
        assertTrue(set2.isDetailedSpecificationOf(set1));

        // enum
        set2 = owner2.changeValueSetType(ValueSetType.ENUM);
        set2.setAbstract(true);
        assertTrue(set2.isDetailedSpecificationOf(set1));
        set2.setAbstract(false);
        assertTrue(set2.isDetailedSpecificationOf(set1));

        // range
        set2 = owner2.changeValueSetType(ValueSetType.RANGE);
        set2.setAbstract(true);
        assertTrue(set2.isDetailedSpecificationOf(set1));
        set2.setAbstract(false);
        assertTrue(set2.isDetailedSpecificationOf(set1));
    }

    public void testIsDetailedSpecificationOf_Range() {
        IValueSet set1 = owner1.changeValueSetType(ValueSetType.RANGE);
        set1.setAbstract(true);

        IValueSet set2 = owner2.changeValueSetType(ValueSetType.UNRESTRICTED);
        assertFalse(set2.isDetailedSpecificationOf(set1));

        set2 = owner2.changeValueSetType(ValueSetType.ENUM);
        assertFalse(set2.isDetailedSpecificationOf(set1));

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
        IRangeValueSet range1 = (IRangeValueSet)set1;
        range1.setAbstract(false);
        range1.setLowerBound("0");
        range1.setUpperBound("9");
        assertFalse(range2.isDetailedSpecificationOf(range1));
        assertTrue(range1.isDetailedSpecificationOf(range2));
    }

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
    }
}
