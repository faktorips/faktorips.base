/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.ipsobject;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IIpsModelExtensions;
import org.faktorips.devtools.model.internal.productcmpt.Formula;
import org.faktorips.devtools.model.internal.tablecontents.Row;
import org.faktorips.devtools.model.internal.testcase.TestPolicyCmptLink;
import org.faktorips.devtools.model.internal.testcase.TestValue;
import org.faktorips.devtools.model.internal.testcasetype.TestAttribute;
import org.faktorips.devtools.model.internal.valueset.ValueSet;
import org.faktorips.devtools.model.productcmpt.IFormula;
import org.faktorips.devtools.model.tablecontents.IRow;
import org.faktorips.devtools.model.tablestructure.ColumnRangeType;
import org.faktorips.devtools.model.tablestructure.IColumnRange;
import org.faktorips.devtools.model.testcase.ITestValue;
import org.faktorips.devtools.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.model.testcasetype.TestParameterType;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.w3c.dom.Element;

public interface Identifier {

    /**
     * When comparing {@link IIpsObjectPart IIpsObjectParts} their unique id is used. If an
     * {@link IIpsObjectPart} can be uniquely identified by their name, this method can be used to
     * implement this behavior.
     *
     * @param other the other {@link IIpsObjectPart IIpsObjectParts} identifier
     * @return true if the identifier finds that the two objects are the same
     */
    boolean isSame(Identifier other);

    // CSOFF: CyclomaticComplexity
    /**
     * Creates a new {@link Identifier} based on the type of the given {@link IIpsObjectPart}
     *
     * @param part the IIpsObjectPart
     * @return a new {@link Identifier}
     */
    static Identifier of(IIpsObjectPart part) {
        String id = part.getId();
        if (part instanceof ILabel label) {
            return new LabelIdentifier(id, label.getLocale());
        }
        if (part instanceof IDescription description) {
            return new DescriptionIdentifier(id, description.getLocale());
        }
        if (part instanceof IColumnRange columnRange) {
            String parameterName = columnRange.getParameterName();
            ColumnRangeType rangeType = columnRange.getColumnRangeType();
            return new ColumnRangeIdentifier(id, parameterName, rangeType);
        }
        if (part instanceof ITestAttribute testAttribute) {
            String name = testAttribute.getName();
            TestParameterType type = testAttribute.getTestAttributeType();
            return new TestAttributeIdentifier(id, name, type);
        }
        if (part instanceof ITestValue testValue) {
            String testParameterName = testValue.getTestParameterName();
            return new TestValueIdentifier(id, testParameterName);
        }
        if ((part instanceof IRow) || (part instanceof IFormula) || (part instanceof TestPolicyCmptLink)) {
            return new IdIdentifier(id);
        }
        String name = part.getName();
        if (IpsStringUtils.isNotBlank(name) && !(part instanceof ValueSet)) {
            return new NamedIdentifier(id, name);
        }
        List<IdentityProvider> list = IIpsModelExtensions.get().getIdentifierForIpsObjectParts().get();
        return list.stream().map(idProvider -> idProvider.getIdentity(part))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(new IdIdentifier(id));
    }
    // CSON: CyclomaticComplexityCheck

    // CSOFF: CyclomaticComplexity
    /**
     * Creates a new {@link Identifier} based on the type of the given {@link Element} xml.
     *
     * @param partEl the partElement
     * @return a new {@link Identifier}
     */
    static Identifier of(Element partEl) {
        String id = partEl.getAttribute(IIpsObjectPart.PROPERTY_ID).trim();

        switch (partEl.getNodeName()) {
            case ILabel.XML_TAG_NAME:
                String localeCode = partEl.getAttribute(ILabel.PROPERTY_LOCALE).trim();
                return new LabelIdentifier(id, IpsStringUtils.isNotEmpty(localeCode) ? new Locale(localeCode) : null);

            case IDescription.XML_TAG_NAME:
                String descrLocaleCode = partEl.getAttribute(IDescription.PROPERTY_LOCALE).trim();
                return new DescriptionIdentifier(id,
                        IpsStringUtils.isNotEmpty(descrLocaleCode) ? new Locale(descrLocaleCode) : null);

            case IColumnRange.PROPERTY_NAME:
                String parameterName = partEl.getAttribute(IColumnRange.PROPERTY_PARAMETER_NAME);
                String rangeTypeId = partEl.getAttribute(IColumnRange.PROPERTY_RANGE_TYPE);
                ColumnRangeType rangeType = ColumnRangeType.getValueById(rangeTypeId);
                return new ColumnRangeIdentifier(id, parameterName, rangeType);

            case TestAttribute.TAG_NAME:
                String name = partEl.getAttribute(ITestAttribute.PROPERTY_NAME).trim();
                String typeId = partEl.getAttribute(ITestAttribute.PROPERTY_TEST_ATTRIBUTE_TYPE);
                TestParameterType type = TestParameterType.getTestParameterType(typeId);
                return new TestAttributeIdentifier(id, name, type);

            case TestValue.TAG_NAME:
                String testParameterName = partEl.getAttribute(ITestValue.PROPERTY_VALUE_PARAMETER).trim();
                return new TestValueIdentifier(id, testParameterName);

            case Row.TAG_NAME, Formula.TAG_NAME, TestPolicyCmptLink.PROPERTY_NAME:
                return new IdIdentifier(id);
        }
        String name = partEl.getAttribute(IIpsElement.PROPERTY_NAME).trim();
        if (IpsStringUtils.isNotEmpty(name)) {
            return new NamedIdentifier(id, name);
        }
        List<IdentityProvider> list = IIpsModelExtensions.get().getIdentifierForIpsObjectParts().get();
        return list.stream().map(idProvider -> idProvider.getIdentity(partEl))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(new IdIdentifier(id));
    }
    // CSON: CyclomaticComplexityCheck

    default boolean isEqualsNotNull(String a, String b) {
        return IpsStringUtils.isNotBlank(a) && IpsStringUtils.isNotBlank(b) && a.equals(b);
    }

    default boolean isEqualsNotNull(Object a, Object b) {
        return a != null && b != null && a.equals(b);
    }

    record IdIdentifier(String id) implements Identifier {

        @Override
        public final boolean isSame(Identifier other) {
            return other instanceof IdIdentifier otherIdentifier
                    && (isEqualsNotNull(id, otherIdentifier.id));
        }
    }

    record NamedIdentifier(String id, String name) implements Identifier {

        @Override
        public final boolean isSame(Identifier other) {
            return other instanceof NamedIdentifier otherIdentifier
                    && (isEqualsNotNull(id, otherIdentifier.id) || isEqualsNotNull(name, otherIdentifier.name));
        }
    }

    record LabelIdentifier(String id, Locale locale) implements Identifier {

        @Override
        public final boolean isSame(Identifier other) {
            return other instanceof LabelIdentifier otherLabel
                    && (isEqualsNotNull(id, otherLabel.id) || isEqualsNotNull(locale, otherLabel.locale));
        }
    }

    record DescriptionIdentifier(String id, Locale locale) implements Identifier {

        @Override
        public final boolean isSame(Identifier other) {
            return other instanceof DescriptionIdentifier otherDescription
                    && (isEqualsNotNull(id, otherDescription.id) || isEqualsNotNull(locale, otherDescription.locale));
        }
    }

    record ColumnRangeIdentifier(String id, String parameterName, ColumnRangeType rangeType) implements Identifier {

        @Override
        public boolean isSame(Identifier other) {
            return other instanceof ColumnRangeIdentifier otherColumnRange
                    && (isEqualsNotNull(id, otherColumnRange.id)
                            || (isEqualsNotNull(parameterName, otherColumnRange.parameterName)
                                    && rangeType == otherColumnRange.rangeType));
        }
    }

    record TestAttributeIdentifier(String id, String name, TestParameterType type) implements Identifier {

        @Override
        public boolean isSame(Identifier other) {
            return other instanceof TestAttributeIdentifier otherTestAttribute
                    && (isEqualsNotNull(id, otherTestAttribute.id) || isEqualsNotNull(name, otherTestAttribute.name)
                            || isEqualsNotNull(type, otherTestAttribute.type));
        }
    }

    record TestValueIdentifier(String id, String testParameterName) implements Identifier {

        @Override
        public final boolean isSame(Identifier other) {
            return other instanceof TestValueIdentifier otherTestValue
                    && (isEqualsNotNull(id, otherTestValue.id)
                            || isEqualsNotNull(testParameterName, otherTestValue.testParameterName));
        }
    }
}
