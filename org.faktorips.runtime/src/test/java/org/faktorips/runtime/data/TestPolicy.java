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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.model.annotation.IpsAllowedValues;
import org.faktorips.runtime.model.annotation.IpsAllowedValuesSetter;
import org.faktorips.runtime.model.annotation.IpsAssociation;
import org.faktorips.runtime.model.annotation.IpsAssociationAdder;
import org.faktorips.runtime.model.annotation.IpsAssociationRemover;
import org.faktorips.runtime.model.annotation.IpsAssociations;
import org.faktorips.runtime.model.annotation.IpsAttribute;
import org.faktorips.runtime.model.annotation.IpsAttributeSetter;
import org.faktorips.runtime.model.annotation.IpsAttributes;
import org.faktorips.runtime.model.annotation.IpsConfiguredAttribute;
import org.faktorips.runtime.model.annotation.IpsDerivedUnion;
import org.faktorips.runtime.model.annotation.IpsDocumented;
import org.faktorips.runtime.model.annotation.IpsInverseAssociation;
import org.faktorips.runtime.model.annotation.IpsPolicyCmptType;
import org.faktorips.runtime.model.annotation.IpsSubsetOfDerivedUnion;
import org.faktorips.runtime.model.type.AssociationKind;
import org.faktorips.runtime.model.type.AttributeKind;
import org.faktorips.runtime.model.type.ValueSetKind;
import org.faktorips.values.Decimal;
import org.faktorips.values.Money;
import org.faktorips.valueset.IntegerRange;
import org.faktorips.valueset.UnrestrictedValueSet;
import org.faktorips.valueset.ValueSet;

@IpsPolicyCmptType(name = "TestPolicy")
@IpsAttributes({ "IntegerAttribute", "DecimalAttribute", "MoneyAttribute", "StringAttribute" })
@IpsAssociations({ "TestDeckung", "DerivedTestDeckung" })
@IpsDocumented(bundleName = "org.faktorips.runtime.validation.TestPolicy", defaultLocale = "en")
public class TestPolicy implements IModelObject {

    public static final String PROPERTY_INTEGER_ATTRIBUTE = "IntegerAttribute";
    public static final String PROPERTY_DECIMAL_ATTRIBUTE = "DecimalAttribute";
    public static final String PROPERTY_MONEY_ATTRIBUTE = "MoneyAttribute";
    public static final String PROPERTY_STRING_ATTRIBUTE = "StringAttribute";

    public static final IntegerRange MAX_MULTIPLICITY_OF_TESTDECKUNG = IntegerRange.valueOf(0, 2147483647);
    public static final String ASSOCIATION_TESTDECKUNGUNGEN = "testDeckungungen";
    public static final String ASSOCIATION_DERIVEDTESTDECKUNGEN = "derivedTestDeckungen";

    private Integer integerAttribute;
    private Decimal decimalAttribute;
    private Money moneyAttribute;
    private String stringAttribute;
    private final List<TestDeckung> testDeckungungen = new ArrayList<>();

    private ValueSet<Integer> setOfAllowedValuesIntegerAttribute = new UnrestrictedValueSet<>();
    private ValueSet<Decimal> setOfAllowedValuesDecimalAttribute = new UnrestrictedValueSet<>();
    private ValueSet<Money> setOfAllowedValuesMoneyAttribute = new UnrestrictedValueSet<>();
    private ValueSet<String> setOfAllowedValuesStringAttribute = new UnrestrictedValueSet<>();

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

    public int getNumOfTestDeckungen() {
        return testDeckungungen.size();
    }

    public boolean containsTestDeckung(TestDeckung objectToTest) {
        return testDeckungungen.contains(objectToTest);
    }

    @IpsAssociation(name = "TestDeckung", pluralName = "TestDeckungen", kind = AssociationKind.Composition, targetClass = TestDeckung.class, min = 0, max = Integer.MAX_VALUE)
    @IpsSubsetOfDerivedUnion("DerivedTestDeckung")
    @IpsInverseAssociation("TestPolicy")
    public List<? extends TestDeckung> getTestDeckungen() {
        return Collections.unmodifiableList(testDeckungungen);
    }

    public TestDeckung getTestDeckung(int index) {
        return testDeckungungen.get(index);
    }

    @IpsAssociationAdder(association = "TestDeckung")
    public void addTestDeckung(TestDeckung objectToAdd) {
        addTestDeckungInternal(objectToAdd);
    }

    public void addTestDeckungInternal(TestDeckung objectToAdd) {
        if (objectToAdd == null) {
            throw new NullPointerException("Can't add null to association TestDeckung of " + this);
        }
        if (testDeckungungen.contains(objectToAdd)) {
            return;
        }
        testDeckungungen.add(objectToAdd);
        objectToAdd.setTestPolicyInternal(this);
    }

    public TestDeckung newTestDeckung() {
        TestDeckung newTestDeckung = new TestDeckung();
        addTestDeckungInternal(newTestDeckung);
        return newTestDeckung;
    }

    @IpsAssociationRemover(association = "TestDeckung")
    public void removeTestDeckung(TestDeckung objectToRemove) {
        if (objectToRemove == null) {
            return;
        }
        testDeckungungen.remove(objectToRemove);
    }

    public void validateDependants(MessageList ml, IValidationContext context) {
        if (getNumOfTestDeckungen() > 0) {
            for (TestDeckung rel : getTestDeckungen()) {
                ml.add(rel.validate(context));
            }
        }
    }

    @IpsAssociation(name = "DerivedTestDeckung", pluralName = "DerivedTestDeckungen", kind = AssociationKind.Composition, targetClass = TestDeckung.class, min = 0, max = Integer.MAX_VALUE)
    @IpsDerivedUnion
    public List<TestDeckung> getDerivedTestDeckungen() {
        List<TestDeckung> result = new ArrayList<>(getNumOfDerivedTestDeckungenInternal());
        result.addAll(getTestDeckungen());
        return result;
    }

    public int getNumOfDerivedTestDeckungen() {
        return getNumOfDerivedTestDeckungenInternal();
    }

    private int getNumOfDerivedTestDeckungenInternal() {
        int num = 0;
        num += getNumOfTestDeckungen();
        return num;
    }
}
