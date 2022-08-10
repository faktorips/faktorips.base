/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.data;

import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.model.annotation.IpsAllowedValues;
import org.faktorips.runtime.model.annotation.IpsAllowedValuesSetter;
import org.faktorips.runtime.model.annotation.IpsAssociation;
import org.faktorips.runtime.model.annotation.IpsAssociationAdder;
import org.faktorips.runtime.model.annotation.IpsAssociations;
import org.faktorips.runtime.model.annotation.IpsAttribute;
import org.faktorips.runtime.model.annotation.IpsAttributeSetter;
import org.faktorips.runtime.model.annotation.IpsAttributes;
import org.faktorips.runtime.model.annotation.IpsConfiguredAttribute;
import org.faktorips.runtime.model.annotation.IpsDocumented;
import org.faktorips.runtime.model.annotation.IpsInverseAssociation;
import org.faktorips.runtime.model.annotation.IpsPolicyCmptType;
import org.faktorips.runtime.model.type.AssociationKind;
import org.faktorips.runtime.model.type.AttributeKind;
import org.faktorips.runtime.model.type.ValueSetKind;
import org.faktorips.values.Decimal;
import org.faktorips.values.Money;
import org.faktorips.valueset.IntegerRange;
import org.faktorips.valueset.UnrestrictedValueSet;
import org.faktorips.valueset.ValueSet;

@IpsPolicyCmptType(name = "TestDeckung")
@IpsAttributes({ "IntegerAttribute", "DecimalAttribute", "MoneyAttribute", "StringAttribute" })
@IpsAssociations({ "TestPolicy", "AndereTestDeckung" })
@IpsDocumented(bundleName = "org.faktorips.runtime.validation.TestDeckung", defaultLocale = "en")
public class TestDeckung implements IModelObject {

    public static final String PROPERTY_INTEGER_ATTRIBUTE = "IntegerAttribute";
    public static final String PROPERTY_DECIMAL_ATTRIBUTE = "DecimalAttribute";
    public static final String PROPERTY_MONEY_ATTRIBUTE = "MoneyAttribute";
    public static final String PROPERTY_STRING_ATTRIBUTE = "StringAttribute";

    public static final String ASSOCIATION_TESTPOLICY = "testPolicy";
    public static final IntegerRange MAX_MULTIPLICITY_OF_ANDERETESTDECKUNG = IntegerRange.valueOf(0, 1);
    public static final String ASSOCIATION_ANDERETESTDECKUNG = "andereTestDeckung";

    private Integer integerAttribute;
    private Decimal decimalAttribute;
    private Money moneyAttribute;
    private String stringAttribute;

    private ValueSet<Integer> setOfAllowedValuesIntegerAttribute = new UnrestrictedValueSet<>();
    private ValueSet<Decimal> setOfAllowedValuesDecimalAttribute = new UnrestrictedValueSet<>();
    private ValueSet<Money> setOfAllowedValuesMoneyAttribute = new UnrestrictedValueSet<>();
    private ValueSet<String> setOfAllowedValuesStringAttribute = new UnrestrictedValueSet<>();

    private TestPolicy testPolicy;
    private TestDeckung andereTestDeckung = null;

    @IpsAllowedValues("IntegerAttribute")
    public ValueSet<Integer> getSetOfAllowedValuesForIntegerAttribute() {
        return setOfAllowedValuesIntegerAttribute;
    }

    @IpsAllowedValuesSetter("IntegerAttribute")
    public void setAllowedValuesForIntegerAttribute(ValueSet<Integer> setOfAllowedValuesIntegerAttribute) {
        this.setOfAllowedValuesIntegerAttribute = setOfAllowedValuesIntegerAttribute;
    }

    @IpsAttribute(name = "IntegerAttribute", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.AllValues)
    @IpsConfiguredAttribute(changingOverTime = true)
    public Integer getIntegerAttribute() {
        return integerAttribute;
    }

    @IpsAttributeSetter("IntegerAttribute")
    public void setIntegerAttribute(Integer newValue) {
        integerAttribute = newValue;
    }

    @IpsAllowedValues("DecimalAttribute")
    public ValueSet<Decimal> getSetOfAllowedValuesForDecimalAttribute() {
        return setOfAllowedValuesDecimalAttribute;
    }

    @IpsAllowedValuesSetter("DecimalAttribute")
    public void setAllowedValuesForDecimalAttribute(ValueSet<Decimal> setOfAllowedValuesDecimalAttribute) {
        this.setOfAllowedValuesDecimalAttribute = setOfAllowedValuesDecimalAttribute;
    }

    @IpsAttribute(name = "DecimalAttribute", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.AllValues)
    @IpsConfiguredAttribute(changingOverTime = true)
    public Decimal getDecimalAttribute() {
        return decimalAttribute;
    }

    @IpsAttributeSetter("DecimalAttribute")
    public void setDecimalAttribute(Decimal newValue) {
        decimalAttribute = newValue;
    }

    @IpsAllowedValues("MoneyAttribute")
    public ValueSet<Money> getSetOfAllowedValuesForMoneyAttribute() {
        return setOfAllowedValuesMoneyAttribute;
    }

    @IpsAllowedValuesSetter("MoneyAttribute")
    public void setAllowedValuesForMoneyAttribute(ValueSet<Money> setOfAllowedValuesMoneyAttribute) {
        this.setOfAllowedValuesMoneyAttribute = setOfAllowedValuesMoneyAttribute;
    }

    @IpsAttribute(name = "MoneyAttribute", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.AllValues)
    @IpsConfiguredAttribute(changingOverTime = true)
    public Money getMoneyAttribute() {
        return moneyAttribute;
    }

    @IpsAttributeSetter("MoneyAttribute")
    public void setMoneyAttribute(Money newValue) {
        moneyAttribute = newValue;
    }

    @IpsAllowedValues("StringAttribute")
    public ValueSet<String> getSetOfAllowedValuesForStringAttribute() {
        return setOfAllowedValuesStringAttribute;
    }

    @IpsAllowedValuesSetter("StringAttribute")
    public void setAllowedValuesForStringAttribute(ValueSet<String> setOfAllowedValuesStringAttribute) {
        this.setOfAllowedValuesStringAttribute = setOfAllowedValuesStringAttribute;
    }

    @IpsAttribute(name = "StringAttribute", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.AllValues)
    @IpsConfiguredAttribute(changingOverTime = true)
    public String getStringAttribute() {
        return stringAttribute;
    }

    @IpsAttributeSetter("StringAttribute")
    public void setStringAttribute(String newValue) {
        stringAttribute = newValue;
    }

    @Override
    public MessageList validate(IValidationContext context) {
        return new MessageList();
    }

    @IpsAssociation(name = "AndereTestDeckung", pluralName = "", kind = AssociationKind.Association, targetClass = TestDeckung.class, min = 0, max = 1)
    public TestDeckung getAndereTestDeckung() {
        return andereTestDeckung;
    }

    @IpsAssociationAdder(association = "AndereTestDeckung")
    public void setAndereTestDeckung(TestDeckung newObject) {
        if (newObject == andereTestDeckung) {
            return;
        }
        andereTestDeckung = newObject;
    }

    @IpsAssociation(name = "TestPolicy", pluralName = "", kind = AssociationKind.CompositionToMaster, targetClass = TestPolicy.class, min = 0, max = 1)
    @IpsInverseAssociation("TestDeckung")
    public TestPolicy getTestPolicy() {
        return testPolicy;
    }

    @IpsAssociationAdder(association = "TestPolicy")
    public void setTestPolicyInternal(TestPolicy newParent) {
        testPolicy = newParent;
    }
}
