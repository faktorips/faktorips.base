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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IModelObjectVisitor;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.IVisitorSupport;
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
import org.faktorips.runtime.model.annotation.IpsDocumented;
import org.faktorips.runtime.model.annotation.IpsPolicyCmptType;
import org.faktorips.runtime.model.type.AssociationKind;
import org.faktorips.runtime.model.type.AttributeKind;
import org.faktorips.runtime.model.type.ValueSetKind;
import org.faktorips.values.Decimal;
import org.faktorips.values.Money;
import org.faktorips.valueset.IntegerRange;
import org.faktorips.valueset.LongRange;
import org.faktorips.valueset.OrderedValueSet;
import org.faktorips.valueset.UnrestrictedValueSet;
import org.faktorips.valueset.ValueSet;

@IpsPolicyCmptType(name = "TestPolicy")
@IpsAttributes({ "IntAttribute", "IntegerAttribute", "DecimalAttribute", "MoneyAttribute", "StringAttribute",
        "BooleanAttribute",
        "EnumAttribute", "RangeAttribute", "onTheFly", "constant", "computed", "BigDecimalAttribute", "ByteAttribute",
        "ShortAttribute", "LongAttribute", "FloatAttribute", "DoubleAttribute" })
@IpsAssociations({ "TestDeckung" })
@IpsDocumented(bundleName = "org.faktorips.runtime.validation.TestPolicy", defaultLocale = "en")
public class TestPolicyWithVisitor implements IVisitorSupport, IModelObject {

    public static final String PROPERTY_INT_ATTRIBUTE = "IntAttribute";
    public static final String PROPERTY_INTEGER_ATTRIBUTE = "IntegerAttribute";
    public static final String PROPERTY_DECIMAL_ATTRIBUTE = "DecimalAttribute";
    public static final String PROPERTY_MONEY_ATTRIBUTE = "MoneyAttribute";
    public static final String PROPERTY_STRING_ATTRIBUTE = "StringAttribute";
    public static final String PROPERTY_BOOLEAN_ATTRIBUTE = "BooleanAttribute";
    public static final String PROPERTY_ENUM_ATTRIBUTE = "EnumAttribute";
    public static final String PROPERTY_RANGE_ATTRIBUTE = "RangeAttribute";
    public static final String PROPERTY_ONTHEFLY_ATTRIBUTE = "onTheFly";
    public static final IntegerRange MAX_MULTIPLICITY_OF_TESTDECKUNG = IntegerRange.valueOf(0, 2147483647);
    public static final String ASSOCIATION_TESTDECKUNGUNGEN = "testDeckungungen";
    public static final String PROPERTY_CONSTANT = "constant";
    public static final String PROPERTY_COMPUTED = "computed";
    public static final String PROPERTY_BIGDECIMAL_ATTRIBUTE = "BigDecimalAttribute";
    public static final String PROPERTY_BYTE_ATTRIBUTE = "ByteAttribute";
    public static final String PROPERTY_SHORT_ATTRIBUTE = "ShortAttribute";
    public static final String PROPERTY_LONG_ATTRIBUTE = "LongAttribute";
    public static final String PROPERTY_FLOAT_ATTRIBUTE = "FloatAttribute";
    public static final String PROPERTY_DOUBLE_ATTRIBUTE = "DoubleAttribute";

    @IpsAttribute(name = "constant", kind = AttributeKind.CONSTANT, valueSetKind = ValueSetKind.AllValues, primitive = true)
    public static final int CONSTANT = 42;

    private int intAttribute;
    private Integer integerAttribute;
    private Decimal decimalAttribute;
    private Money moneyAttribute;
    private String stringAttribute;
    private Boolean booleanAttribute;
    private TestEnum enumAttribute;
    private Long rangeAttribute;
    private int computed;
    private final List<TestDeckungWithVisitor> testDeckungungen = new ArrayList<>();
    private BigDecimal bigDecimalAttribute;
    private Byte byteAttribute;
    private Short shortAttribute;
    private Long longAttribute;
    private Float floatAttribute;
    private Double doubleAttribute;

    private ValueSet<Integer> setOfAllowedValuesIntAttribute = new UnrestrictedValueSet<>();
    private ValueSet<Integer> setOfAllowedValuesIntegerAttribute = new UnrestrictedValueSet<>();
    private ValueSet<Decimal> setOfAllowedValuesDecimalAttribute = new UnrestrictedValueSet<>();
    private ValueSet<BigDecimal> setOfAllowedValuesBigDecimalAttribute = new UnrestrictedValueSet<>();
    private ValueSet<Money> setOfAllowedValuesMoneyAttribute = new UnrestrictedValueSet<>();
    private ValueSet<String> setOfAllowedValuesStringAttribute = new UnrestrictedValueSet<>();
    private ValueSet<Boolean> setOfAllowedValuesBooleanAttribute = new UnrestrictedValueSet<>();
    private OrderedValueSet<TestEnum> setOfAllowedValuesEnumAttribute = new OrderedValueSet<>(true, null,
            TestEnum.values());
    private OrderedValueSet<String> setOfAllowedValuesOnTheFly = new OrderedValueSet<>(false,
            "SOMEVAL");
    private ValueSet<Byte> setOfAllowedValuesByteAttribute = new UnrestrictedValueSet<>();
    private ValueSet<Short> setOfAllowedValuesShortAttribute = new UnrestrictedValueSet<>();
    private ValueSet<Long> setOfAllowedValuesLongAttribute = new UnrestrictedValueSet<>();
    private ValueSet<Float> setOfAllowedValuesFloatAttribute = new UnrestrictedValueSet<>();
    private ValueSet<Double> setOfAllowedValuesDoubleAttribute = new UnrestrictedValueSet<>();
    private LongRange rangeForRangeAttribute = LongRange.valueOf(2L, 5L, 1L, true);

    @IpsAllowedValues("IntAttribute")
    public ValueSet<Integer> getSetOfAllowedValuesForIntAttribute() {
        return setOfAllowedValuesIntAttribute;
    }

    @IpsAllowedValuesSetter("IntAttribute")
    public void setAllowedValuesForIntAttribute(ValueSet<Integer> setOfAllowedValuesIntAttribute) {
        this.setOfAllowedValuesIntAttribute = setOfAllowedValuesIntAttribute;
    }

    @IpsAttribute(name = "IntAttribute", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.AllValues)
    @IpsConfiguredAttribute(changingOverTime = true)
    public int getIntAttribute() {
        return intAttribute;
    }

    @IpsAttributeSetter("IntAttribute")
    public void setIntAttribute(int newValue) {
        intAttribute = newValue;
    }

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

    @IpsAllowedValues("ByteAttribute")
    public ValueSet<Byte> getSetOfAllowedValuesForByteAttribute() {
        return setOfAllowedValuesByteAttribute;
    }

    @IpsAllowedValuesSetter("ByteAttribute")
    public void setAllowedValuesForByteAttribute(ValueSet<Byte> setOfAllowedValuesByteAttribute) {
        this.setOfAllowedValuesByteAttribute = setOfAllowedValuesByteAttribute;
    }

    @IpsAttribute(name = "ByteAttribute", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.AllValues)
    @IpsConfiguredAttribute(changingOverTime = true)
    public Byte getByteAttribute() {
        return byteAttribute;
    }

    @IpsAttributeSetter("ByteAttribute")
    public void setByteAttribute(Byte newValue) {
        byteAttribute = newValue;
    }

    @IpsAllowedValues("ShortAttribute")
    public ValueSet<Short> getSetOfAllowedValuesForShortAttribute() {
        return setOfAllowedValuesShortAttribute;
    }

    @IpsAllowedValuesSetter("ShortAttribute")
    public void setAllowedValuesForShortAttribute(ValueSet<Short> setOfAllowedValuesShortAttribute) {
        this.setOfAllowedValuesShortAttribute = setOfAllowedValuesShortAttribute;
    }

    @IpsAttribute(name = "ShortAttribute", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.AllValues)
    @IpsConfiguredAttribute(changingOverTime = true)
    public Short getShortAttribute() {
        return shortAttribute;
    }

    @IpsAttributeSetter("ShortAttribute")
    public void setShortAttribute(Short newValue) {
        shortAttribute = newValue;
    }

    @IpsAllowedValues("LongAttribute")
    public ValueSet<Long> getSetOfAllowedValuesForLongAttribute() {
        return setOfAllowedValuesLongAttribute;
    }

    @IpsAllowedValuesSetter("LongAttribute")
    public void setAllowedValuesForLongAttribute(ValueSet<Long> setOfAllowedValuesLongAttribute) {
        this.setOfAllowedValuesLongAttribute = setOfAllowedValuesLongAttribute;
    }

    @IpsAttribute(name = "LongAttribute", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.AllValues)
    @IpsConfiguredAttribute(changingOverTime = true)
    public Long getLongAttribute() {
        return longAttribute;
    }

    @IpsAttributeSetter("LongAttribute")
    public void setLongAttribute(Long newValue) {
        longAttribute = newValue;
    }

    @IpsAllowedValues("FloatAttribute")
    public ValueSet<Float> getSetOfAllowedValuesForFloatAttribute() {
        return setOfAllowedValuesFloatAttribute;
    }

    @IpsAllowedValuesSetter("FloatAttribute")
    public void setAllowedValuesForFloatAttribute(ValueSet<Float> setOfAllowedValuesFloatAttribute) {
        this.setOfAllowedValuesFloatAttribute = setOfAllowedValuesFloatAttribute;
    }

    @IpsAttribute(name = "FloatAttribute", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.AllValues)
    @IpsConfiguredAttribute(changingOverTime = true)
    public Float getFloatAttribute() {
        return floatAttribute;
    }

    @IpsAttributeSetter("FloatAttribute")
    public void setFloatAttribute(Float newValue) {
        floatAttribute = newValue;
    }

    @IpsAllowedValues("DoubleAttribute")
    public ValueSet<Double> getSetOfAllowedValuesForDoubleAttribute() {
        return setOfAllowedValuesDoubleAttribute;
    }

    @IpsAllowedValuesSetter("DoubleAttribute")
    public void setAllowedValuesForDoubleAttribute(ValueSet<Double> setOfAllowedValuesDoubleAttribute) {
        this.setOfAllowedValuesDoubleAttribute = setOfAllowedValuesDoubleAttribute;
    }

    @IpsAttribute(name = "DoubleAttribute", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.AllValues)
    @IpsConfiguredAttribute(changingOverTime = true)
    public Double getDoubleAttribute() {
        return doubleAttribute;
    }

    @IpsAttributeSetter("DoubleAttribute")
    public void setDoubleAttribute(Double newValue) {
        doubleAttribute = newValue;
    }

    @IpsAllowedValues("BigDecimalAttribute")
    public ValueSet<BigDecimal> getSetOfAllowedValuesForBigDecimalAttribute() {
        return setOfAllowedValuesBigDecimalAttribute;
    }

    @IpsAllowedValuesSetter("DecimalAttribute")
    public void setAllowedValuesForBigDecimalAttribute(ValueSet<BigDecimal> setOfAllowedValuesBigDecimalAttribute) {
        this.setOfAllowedValuesBigDecimalAttribute = setOfAllowedValuesBigDecimalAttribute;
    }

    @IpsAttribute(name = "BigDecimalAttribute", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.AllValues)
    @IpsConfiguredAttribute(changingOverTime = true)
    public BigDecimal getBigDecimalAttribute() {
        return bigDecimalAttribute;
    }

    @IpsAttributeSetter("BigDecimalAttribute")
    public void setBigDecimalAttribute(BigDecimal newValue) {
        bigDecimalAttribute = newValue;
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

    @IpsAttribute(name = "BooleanAttribute", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.AllValues)
    @IpsConfiguredAttribute(changingOverTime = true)
    public Boolean getBooleanAttribute() {
        return booleanAttribute;
    }

    @IpsAttributeSetter("BooleanAttribute")
    public void setBooleanAttribute(Boolean newValue) {
        booleanAttribute = newValue;
    }

    @IpsAllowedValues("BooleanAttribute")
    public ValueSet<Boolean> getSetOfAllowedValuesForBooleanAttribute() {
        return setOfAllowedValuesBooleanAttribute;
    }

    @IpsAllowedValuesSetter("BooleanAttribute")
    public void setAllowedValuesForBooleanAttribute(ValueSet<Boolean> setOfAllowedValuesBooleanAttribute) {
        this.setOfAllowedValuesBooleanAttribute = setOfAllowedValuesBooleanAttribute;
    }

    @IpsAttribute(name = "EnumAttribute", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.Enum)
    @IpsConfiguredAttribute(changingOverTime = true)
    public TestEnum getEnumAttribute() {
        return enumAttribute;
    }

    @IpsAttributeSetter("EnumAttribute")
    public void setEnumAttribute(TestEnum newValue) {
        enumAttribute = newValue;
    }

    @IpsAllowedValues("EnumAttribute")
    public OrderedValueSet<TestEnum> getAllowedValuesForEnumAttribute() {
        return setOfAllowedValuesEnumAttribute;
    }

    @IpsAllowedValuesSetter("EnumAttribute")
    public void setAllowedValuesForEnumAttribute(ValueSet<TestEnum> setOfAllowedValuesEnumAttribute) {
        this.setOfAllowedValuesEnumAttribute = (OrderedValueSet<TestEnum>)setOfAllowedValuesEnumAttribute;
    }

    @IpsAttribute(name = "RangeAttribute", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.Range)
    @IpsConfiguredAttribute(changingOverTime = true)
    public Long getRangeAttribute() {
        return rangeAttribute;
    }

    @IpsAttributeSetter("RangeAttribute")
    public void setRangeAttribute(Long newValue) {
        rangeAttribute = newValue;
    }

    @IpsAllowedValues("RangeAttribute")
    public LongRange getRangeForRangeAttribute() {
        return rangeForRangeAttribute;
    }

    @IpsAllowedValuesSetter("RangeAttribute")
    public void setAllowedValuesForRangeAttribute(ValueSet<Long> rangeForRangeAttribute) {
        this.rangeForRangeAttribute = (LongRange)rangeForRangeAttribute;
    }

    @IpsAttribute(name = "computed", kind = AttributeKind.DERIVED_BY_EXPLICIT_METHOD_CALL, valueSetKind = ValueSetKind.AllValues, primitive = true)
    public int getComputed() {
        return computed;
    }

    @Override
    public boolean accept(IModelObjectVisitor visitor) {
        visitor.visit(this);
        for (TestDeckungWithVisitor deckung : testDeckungungen) {
            visitor.visit(deckung);
        }
        return true;
    }

    @Override
    public MessageList validate(IValidationContext context) {
        return new MessageList();
    }

    public int getNumOfTestDeckungungen() {
        return testDeckungungen.size();
    }

    public boolean containsTestDeckung(TestDeckungWithVisitor objectToTest) {
        return testDeckungungen.contains(objectToTest);
    }

    @IpsAssociation(name = "TestDeckung", pluralName = "TestDeckungungen", kind = AssociationKind.Composition, targetClass = TestDeckungWithVisitor.class, min = 0, max = Integer.MAX_VALUE)
    public List<? extends TestDeckungWithVisitor> getTestDeckungungen() {
        return Collections.unmodifiableList(testDeckungungen);
    }

    public TestDeckungWithVisitor getTestDeckung(int index) {
        return testDeckungungen.get(index);
    }

    @IpsAssociationAdder(association = "TestDeckung")
    public void addTestDeckung(TestDeckungWithVisitor objectToAdd) {
        addTestDeckungInternal(objectToAdd);
    }

    public void addTestDeckungInternal(TestDeckungWithVisitor objectToAdd) {
        if (objectToAdd == null) {
            throw new NullPointerException("Can't add null to association TestDeckung of " + this);
        }
        if (testDeckungungen.contains(objectToAdd)) {
            return;
        }
        testDeckungungen.add(objectToAdd);
    }

    public TestDeckungWithVisitor newTestDeckung() {
        TestDeckungWithVisitor newTestDeckung = new TestDeckungWithVisitor();
        addTestDeckungInternal(newTestDeckung);
        return newTestDeckung;
    }

    @IpsAssociationRemover(association = "TestDeckung")
    public void removeTestDeckung(TestDeckungWithVisitor objectToRemove) {
        if (objectToRemove == null) {
            return;
        }
        testDeckungungen.remove(objectToRemove);
    }

    public void validateDependants(MessageList ml, IValidationContext context) {
        if (getNumOfTestDeckungungen() > 0) {
            for (TestDeckungWithVisitor rel : getTestDeckungungen()) {
                ml.add(rel.validate(context));
            }
        }
    }

    @IpsAllowedValues("onTheFly")
    public ValueSet<String> getAllowedValuesForOnTheFly() {
        return setOfAllowedValuesOnTheFly;
    }

    @IpsAllowedValuesSetter("onTheFly")
    public void setAllowedValuesForOnTheFly(OrderedValueSet<String> setOfAllowedValuesOnTheFly) {
        this.setOfAllowedValuesOnTheFly = setOfAllowedValuesOnTheFly;
    }

    @IpsAttribute(name = "onTheFly", kind = AttributeKind.DERIVED_ON_THE_FLY, valueSetKind = ValueSetKind.Enum)
    public String getOnTheFly() {
        // begin-user-code
        return "SOMEVAL";
        // end-user-code
    }
}
