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

import static org.faktorips.devtools.model.ipsobject.Identifier.getAttribute;
import static org.faktorips.devtools.model.ipsobject.Identifier.getId;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IIpsModelExtensions;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.model.enums.IEnumLiteralNameAttributeValue;
import org.faktorips.devtools.model.enums.IEnumValue;
import org.faktorips.devtools.model.enums.IEnumValueContainer;
import org.faktorips.devtools.model.internal.enums.EnumType;
import org.faktorips.devtools.model.internal.ipsobject.IpsObjectPartContainer;
import org.faktorips.devtools.model.internal.productcmpttype.TableStructureUsage.TableStructureReference;
import org.faktorips.devtools.model.internal.tablecontents.Row;
import org.faktorips.devtools.model.internal.testcase.TestPolicyCmpt;
import org.faktorips.devtools.model.internal.testcase.TestPolicyCmptLink;
import org.faktorips.devtools.model.internal.testcase.TestValue;
import org.faktorips.devtools.model.internal.type.Association;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.IConfigElement;
import org.faktorips.devtools.model.productcmpt.IConfiguredDefault;
import org.faktorips.devtools.model.productcmpt.IConfiguredValueSet;
import org.faktorips.devtools.model.productcmpt.IExpression;
import org.faktorips.devtools.model.productcmpt.IFormula;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.model.tablestructure.ColumnRangeType;
import org.faktorips.devtools.model.tablestructure.IColumnRange;
import org.faktorips.devtools.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.model.testcase.ITestValue;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.devtools.model.util.XmlUtil;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

// Bug in the Eclipse imports organizer thinks the static imports are needed here
@SuppressWarnings("unused")
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
     * @param index a counter for {@link IPartIdentifiedByIndex parts identified by their index}
     * @return a new {@link Identifier}
     */
    static Identifier of(IIpsObjectPart part, AtomicInteger index) {
        String id = part.getId();
        // TODO Java 21 turn to switch
        if (part instanceof ILabel label) {
            return LabelIdentifier.of(label);
        }
        if (part instanceof IDescription description) {
            return new DescriptionIdentifier(id, description.getLocale());
        }
        if (part instanceof IColumnRange columnRange) {
            return ColumnRangeIdentifier.of(columnRange);
        }
        if (part instanceof ITestValue testValue) {
            return TestValueIdentifier.of(testValue);
        }
        if (part instanceof IAttributeValue attributeValue) {
            return AttributeValueIdentifier.of(attributeValue);
        }
        if (part instanceof IConfiguredDefault configuredDefault) {
            return ConfiguredDefaultIdentifier.of(configuredDefault);
        }
        if (part instanceof IConfiguredValueSet configuredValueSet) {
            return ConfiguredValueSetIdentifier.of(configuredValueSet);
        }
        if (part instanceof IAssociation association) {
            return AssociationIdentifier.of(association);
        }
        if (part instanceof ITableStructureUsage tableStructureUsage) {
            return TableStructureUsageIdentifier.of(tableStructureUsage);
        }
        if (part instanceof TableStructureReference tableStructureReference) {
            return TableStructureReferenceIdentifier.of(tableStructureReference);
        }
        if (part instanceof ITableContentUsage tableContentUsage) {
            return TableContentUsageIdentifier.of(tableContentUsage);
        }
        if (part instanceof IIpsObjectGeneration generation) {
            return GenerationIdentifier.of(generation);
        }
        if (part instanceof IFormula formula) {
            return FormulaIdentifier.of(formula);
        }
        if (part instanceof IProductCmptLink link) {
            return LinkIdentifier.of(link);
        }
        if (part instanceof ITestPolicyCmpt testPolicyCmpt) {
            return TestPolicyCmptIdentifier.of(testPolicyCmpt);
        }
        if (part instanceof TestPolicyCmptLink testPolicyCmptLink) {
            return TestPolicyCmptLinkIdentifier.of(testPolicyCmptLink);
        }
        if (part instanceof IEnumValue enumValue) {
            return EmumValueIdentifier.of(enumValue);
        }
        if (part instanceof IEnumLiteralNameAttributeValue enumLiteralNameAttributeValue) {
            return new ByTypeIdentifier(enumLiteralNameAttributeValue.getId(), IEnumLiteralNameAttributeValue.XML_TAG);
        }

        if (part instanceof IPartIdentifiedByIndex indexedPart) {
            return new IndexedIdentifier(indexedPart.getId(), index.getAndIncrement());
        }
        String name = part.getName();
        if (IpsStringUtils.isNotBlank(name)) {
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
    // CSOFF: NestedBlocks
    /**
     * Creates a new {@link Identifier} based on the type of the given {@link Element} xml.
     *
     * @param partEl the partElement
     * @param container the parent container, to help identify child elements
     * @param index a counter for {@link IPartIdentifiedByIndex parts identified by their index}
     * @return a new {@link Identifier}
     */
    static Identifier of(Element partEl, IpsObjectPartContainer container, AtomicInteger index) {
        return switch (partEl.getNodeName()) {
            case ILabel.XML_TAG_NAME -> LabelIdentifier.of(partEl);
            case IDescription.XML_TAG_NAME -> DescriptionIdentifier.of(partEl);
            case IColumnRange.PROPERTY_NAME -> ColumnRangeIdentifier.of(partEl);
            case TestValue.TAG_NAME -> TestValueIdentifier.of(partEl);
            case IAttributeValue.TAG_NAME -> AttributeValueIdentifier.of(partEl);
            case IConfiguredDefault.TAG_NAME -> ConfiguredDefaultIdentifier.of(partEl);
            case IConfiguredValueSet.TAG_NAME -> ConfiguredValueSetIdentifier.of(partEl);
            case Association.TAG_NAME -> AssociationIdentifier.of(partEl);
            case ITableStructureUsage.TAG_NAME -> TableStructureUsageIdentifier.of(partEl);
            case ITableStructureUsage.TAG_NAME_TABLE_STRUCTURE -> TableStructureReferenceIdentifier.of(partEl);
            case ITableContentUsage.TAG_NAME -> TableContentUsageIdentifier.of(partEl);
            case IIpsObjectGeneration.TAG_NAME -> GenerationIdentifier.of(partEl);
            case IExpression.TAG_NAME -> FormulaIdentifier.of(partEl);
            case IProductCmptLink.TAG_NAME -> {
                // TODO Java 21 use case ... when
                if (TestPolicyCmpt.TAG_NAME.equals(partEl.getParentNode().getNodeName())) {
                    yield TestPolicyCmptLinkIdentifier.of(partEl);
                }
                yield LinkIdentifier.of(partEl);
            }
            case TestPolicyCmpt.TAG_NAME -> TestPolicyCmptIdentifier.of(partEl);
            case IEnumValue.XML_TAG -> EmumValueIdentifier.of(partEl, (IEnumValueContainer)container);
            case IEnumLiteralNameAttributeValue.XML_TAG -> new ByTypeIdentifier(getId(partEl),
                    IEnumAttributeValue.XML_TAG);
            case Row.TAG_NAME, IEnumAttributeValue.XML_TAG -> new IndexedIdentifier(getId(partEl),
                    index.getAndIncrement());
            default -> {
                String name = getAttribute(partEl, IIpsElement.PROPERTY_NAME);
                if (IpsStringUtils.isNotEmpty(name)) {
                    yield new NamedIdentifier(getId(partEl), name);
                }
                List<IdentityProvider> list = IIpsModelExtensions.get().getIdentifierForIpsObjectParts().get();
                yield list.stream().map(idProvider -> idProvider.getIdentity(partEl))
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(new IdIdentifier(getId(partEl)));
            }
        };
    }
    // CSON: NestedBlocks
    // CSON: CyclomaticComplexityCheck

    static String getAttribute(Element partEl, String attributeName) {
        return partEl.getAttribute(attributeName).trim();
    }

    static String getId(Element partEl) {
        return getAttribute(partEl, IIpsObjectPart.PROPERTY_ID);
    }

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

    record IndexedIdentifier(String id, int index) implements Identifier {

        @Override
        public final boolean isSame(Identifier other) {
            return other instanceof IndexedIdentifier otherIdentifier
                    && (isEqualsNotNull(id, otherIdentifier.id) || index == otherIdentifier.index);
        }
    }

    record EmumValueIdentifier(String id, String idAttributeValue) implements Identifier {

        static Identifier of(IEnumValue enumValue) {
            IEnumValueContainer enumValueContainer = enumValue.getEnumValueContainer();
            return findIdAttribute(enumValueContainer)
                    .map(idAttribute -> (Identifier)new EmumValueIdentifier(enumValue.getId(),
                            enumValue.getEnumAttributeValue(idAttribute).getStringValue()))
                    .orElseGet(() -> new IdIdentifier(enumValue.getId()));
        }

        private static Optional<IEnumAttribute> findIdAttribute(IEnumValueContainer enumValueContainer) {
            return enumValueContainer.findEnumType(enumValueContainer.getIpsProject())
                    .getEnumAttributes(false).stream()
                    .filter(a -> a.findIsIdentifier(enumValueContainer.getIpsProject()))
                    .findFirst();
        }

        static Identifier of(Element partEl, IEnumValueContainer enumValueContainer) {
            int indexOfIdAttribute;
            if (enumValueContainer instanceof EnumType enumType) {
                indexOfIdAttribute = findIdAttributeIndex(partEl);
            } else {
                indexOfIdAttribute = findIdAttribute(enumValueContainer)
                        .map(idAttribute -> enumValueContainer.findEnumType(enumValueContainer.getIpsProject())
                                .getIndexOfEnumAttribute(idAttribute, false))
                        .orElse(-1);
            }
            if (indexOfIdAttribute < 0) {
                return new IdIdentifier(getId(partEl));
            }
            Element idAttributeElement = XmlUtil.getElement(partEl, IEnumAttributeValue.XML_TAG, indexOfIdAttribute);
            String idAttributeValue = XmlUtil.getCDATAorTextContent(idAttributeElement);
            return new EmumValueIdentifier(getId(partEl), idAttributeValue);
        }

        private static int findIdAttributeIndex(Element partEl) {
            Node enumTypeNode = partEl.getParentNode();
            List<Element> attributeElements = org.faktorips.runtime.internal.XmlUtil.getChildElements(enumTypeNode,
                    IEnumAttribute.XML_TAG);
            for (int i = 0; i < attributeElements.size(); i++) {
                if (XmlUtil.getBooleanAttributeOrFalse(attributeElements.get(i), IEnumAttribute.PROPERTY_IDENTIFIER)) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public final boolean isSame(Identifier other) {
            return other instanceof EmumValueIdentifier otherIdentifier
                    && (isEqualsNotNull(id, otherIdentifier.id)
                            || isEqualsNotNull(idAttributeValue, otherIdentifier.idAttributeValue));
        }
    }

    record TestPolicyCmptIdentifier(String id, String testPolicyCmptTypeParameter) implements Identifier {

        static TestPolicyCmptIdentifier of(ITestPolicyCmpt testPolicyCmpt) {
            String testPolicyCmptTypeParameter = testPolicyCmpt.getTestPolicyCmptTypeParameter();
            return new TestPolicyCmptIdentifier(testPolicyCmpt.getId(), testPolicyCmptTypeParameter);
        }

        static TestPolicyCmptIdentifier of(Element partEl) {
            String testPolicyCmptTypeParameter = getAttribute(partEl, ITestPolicyCmpt.PROPERTY_TESTPOLICYCMPTTYPE);
            return new TestPolicyCmptIdentifier(getId(partEl), testPolicyCmptTypeParameter);
        }

        @Override
        public final boolean isSame(Identifier other) {
            return other instanceof TestPolicyCmptIdentifier otherIdentifier
                    && (isEqualsNotNull(id, otherIdentifier.id) || isEqualsNotNull(testPolicyCmptTypeParameter,
                            otherIdentifier.testPolicyCmptTypeParameter));
        }
    }

    record TestPolicyCmptLinkIdentifier(String id, String testPolicyCmptTypeParameter, String targetId)
            implements Identifier {

        static TestPolicyCmptLinkIdentifier of(TestPolicyCmptLink testPolicyCmptLink) {
            ITestPolicyCmpt target = testPolicyCmptLink.findTarget();
            String targetId = target != null ? target.getProductCmpt() : "";
            return new TestPolicyCmptLinkIdentifier(testPolicyCmptLink.getId(),
                    testPolicyCmptLink.getTestPolicyCmptTypeParameter(), targetId);
        }

        static TestPolicyCmptLinkIdentifier of(Element partEl) {
            Element targetElement = XmlUtil.getFirstElement(partEl, ITestPolicyCmpt.TAG_NAME);
            String targetId = targetElement != null
                    ? getAttribute(targetElement, ITestPolicyCmpt.PROPERTY_PRODUCTCMPT)
                    : "";
            if (targetId == null) {
                targetId = "";
            }
            String testPolicyCmptTypeParameter = getAttribute(partEl, TestPolicyCmptLink.PROPERTY_POLICYCMPTTYPE);
            return new TestPolicyCmptLinkIdentifier(getId(partEl), testPolicyCmptTypeParameter, targetId);
        }

        @Override
        public final boolean isSame(Identifier other) {
            return other instanceof TestPolicyCmptLinkIdentifier otherIdentifier
                    && (isEqualsNotNull(id, otherIdentifier.id)
                            || (isEqualsNotNull(testPolicyCmptTypeParameter,
                                    otherIdentifier.testPolicyCmptTypeParameter)
                                    && isEqualsNotNull(targetId, otherIdentifier.targetId)));
        }
    }

    record FormulaIdentifier(String id, String formulaSignature) implements Identifier {

        static FormulaIdentifier of(IFormula formula) {
            return new FormulaIdentifier(formula.getId(), formula.getFormulaSignature());
        }

        static FormulaIdentifier of(Element partEl) {
            String formulaSignature = getAttribute(partEl, IExpression.PROPERTY_FORMULA_SIGNATURE_NAME);
            return new FormulaIdentifier(getId(partEl), formulaSignature);
        }

        @Override
        public final boolean isSame(Identifier other) {
            return other instanceof FormulaIdentifier otherIdentifier
                    && (isEqualsNotNull(id, otherIdentifier.id)
                            || isEqualsNotNull(formulaSignature, otherIdentifier.formulaSignature));
        }
    }

    record GenerationIdentifier(String id, String validFrom) implements Identifier {

        static GenerationIdentifier of(IIpsObjectGeneration generation) {
            return new GenerationIdentifier(generation.getId(),
                    XmlUtil.gregorianCalendarToXmlDateString(generation.getValidFrom()));
        }

        static GenerationIdentifier of(Element partEl) {
            String validFrom = getAttribute(partEl, IIpsObjectGeneration.PROPERTY_VALID_FROM);
            return new GenerationIdentifier(getId(partEl), validFrom);
        }

        @Override
        public final boolean isSame(Identifier other) {
            return other instanceof GenerationIdentifier otherIdentifier
                    && (isEqualsNotNull(id, otherIdentifier.id)
                            || isEqualsNotNull(validFrom, otherIdentifier.validFrom));
        }
    }

    record ByTypeIdentifier(String id, String type) implements Identifier {

        @Override
        public final boolean isSame(Identifier other) {
            return other instanceof ByTypeIdentifier otherIdentifier
                    && (isEqualsNotNull(id, otherIdentifier.id) || isEqualsNotNull(type, otherIdentifier.type));
        }
    }

    record TableStructureUsageIdentifier(String id, String roleName) implements Identifier {

        static TableStructureUsageIdentifier of(ITableStructureUsage tableStructureUsage) {
            return new TableStructureUsageIdentifier(tableStructureUsage.getId(), tableStructureUsage.getRoleName());
        }

        static TableStructureUsageIdentifier of(Element partEl) {
            String roleName = getAttribute(partEl, ITableStructureUsage.PROPERTY_ROLENAME);
            return new TableStructureUsageIdentifier(getId(partEl), roleName);
        }

        @Override
        public final boolean isSame(Identifier other) {
            return other instanceof TableStructureUsageIdentifier otherIdentifier
                    && (isEqualsNotNull(id, otherIdentifier.id) || isEqualsNotNull(roleName, otherIdentifier.roleName));
        }
    }

    record TableStructureReferenceIdentifier(String id, String tableStructureName) implements Identifier {

        static TableStructureReferenceIdentifier of(TableStructureReference tableStructureReference) {
            return new TableStructureReferenceIdentifier(tableStructureReference.getId(),
                    tableStructureReference.getTableStructure());
        }

        static TableStructureReferenceIdentifier of(Element partEl) {
            String tableStructureName = getAttribute(partEl, ITableStructureUsage.PROPERTY_TABLESTRUCTURE);
            return new TableStructureReferenceIdentifier(getId(partEl), tableStructureName);
        }

        @Override
        public final boolean isSame(Identifier other) {
            return other instanceof TableStructureReferenceIdentifier otherIdentifier
                    && (isEqualsNotNull(id, otherIdentifier.id)
                            || isEqualsNotNull(tableStructureName, otherIdentifier.tableStructureName));
        }
    }

    record TableContentUsageIdentifier(String id, String roleName) implements Identifier {

        static TableContentUsageIdentifier of(ITableContentUsage tableContentUsage) {
            return new TableContentUsageIdentifier(tableContentUsage.getId(), tableContentUsage.getStructureUsage());
        }

        static TableContentUsageIdentifier of(Element partEl) {
            String tableUsageName = getAttribute(partEl, ITableContentUsage.PROPERTY_STRUCTURE_USAGE);
            return new TableContentUsageIdentifier(getId(partEl), tableUsageName);
        }

        @Override
        public final boolean isSame(Identifier other) {
            return other instanceof TableContentUsageIdentifier otherIdentifier
                    && (isEqualsNotNull(id, otherIdentifier.id) || isEqualsNotNull(roleName, otherIdentifier.roleName));
        }
    }

    record AttributeValueIdentifier(String id, String attributeName) implements Identifier {

        static AttributeValueIdentifier of(IAttributeValue attributeValue) {
            return new AttributeValueIdentifier(attributeValue.getId(), attributeValue.getAttribute());
        }

        static AttributeValueIdentifier of(Element partEl) {
            String attributeName = getAttribute(partEl, IAttributeValue.PROPERTY_ATTRIBUTE);
            return new AttributeValueIdentifier(getId(partEl), attributeName);
        }

        @Override
        public final boolean isSame(Identifier other) {
            return other instanceof AttributeValueIdentifier otherIdentifier
                    && (isEqualsNotNull(id, otherIdentifier.id)
                            || isEqualsNotNull(attributeName, otherIdentifier.attributeName));
        }
    }

    record ConfiguredDefaultIdentifier(String id, String attributeName) implements Identifier {

        static ConfiguredDefaultIdentifier of(IConfiguredDefault configuredDefault) {
            return new ConfiguredDefaultIdentifier(configuredDefault.getId(),
                    configuredDefault.getPolicyCmptTypeAttribute());
        }

        static ConfiguredDefaultIdentifier of(Element partEl) {
            String attributeName = getAttribute(partEl, IConfigElement.PROPERTY_POLICY_CMPT_TYPE_ATTRIBUTE);
            return new ConfiguredDefaultIdentifier(getId(partEl), attributeName);
        }

        @Override
        public final boolean isSame(Identifier other) {
            return other instanceof ConfiguredDefaultIdentifier otherIdentifier
                    && (isEqualsNotNull(id, otherIdentifier.id)
                            || isEqualsNotNull(attributeName, otherIdentifier.attributeName));
        }
    }

    record ConfiguredValueSetIdentifier(String id, String attributeName) implements Identifier {

        static ConfiguredValueSetIdentifier of(IConfiguredValueSet configuredValueSet) {
            return new ConfiguredValueSetIdentifier(configuredValueSet.getId(),
                    configuredValueSet.getPolicyCmptTypeAttribute());
        }

        static ConfiguredValueSetIdentifier of(Element partEl) {
            String attributeName = getAttribute(partEl, IConfigElement.PROPERTY_POLICY_CMPT_TYPE_ATTRIBUTE);
            return new ConfiguredValueSetIdentifier(getId(partEl), attributeName);
        }

        @Override
        public final boolean isSame(Identifier other) {
            return other instanceof ConfiguredValueSetIdentifier otherIdentifier
                    && (isEqualsNotNull(id, otherIdentifier.id)
                            || isEqualsNotNull(attributeName, otherIdentifier.attributeName));
        }
    }

    record AssociationIdentifier(String id, String targetRoleName) implements Identifier {

        static AssociationIdentifier of(IAssociation association) {
            return new AssociationIdentifier(association.getId(), association.getTargetRoleSingular());
        }

        static AssociationIdentifier of(Element partEl) {
            String targetRoleName = getAttribute(partEl, Association.PROPERTY_TARGET_ROLE_SINGULAR);
            return new AssociationIdentifier(getId(partEl), targetRoleName);
        }

        @Override
        public final boolean isSame(Identifier other) {
            return other instanceof AssociationIdentifier otherIdentifier
                    && (isEqualsNotNull(id, otherIdentifier.id)
                            || isEqualsNotNull(targetRoleName, otherIdentifier.targetRoleName));
        }
    }

    record LabelIdentifier(String id, Locale locale) implements Identifier {

        static LabelIdentifier of(ILabel label) {
            return new LabelIdentifier(label.getId(), label.getLocale());
        }

        static LabelIdentifier of(Element partEl) {
            String localeCode = getAttribute(partEl, ILabel.PROPERTY_LOCALE);
            return new LabelIdentifier(getId(partEl),
                    IpsStringUtils.isNotEmpty(localeCode) ? new Locale(localeCode) : null);
        }

        @Override
        public final boolean isSame(Identifier other) {
            return other instanceof LabelIdentifier otherLabel
                    && (isEqualsNotNull(id, otherLabel.id) || isEqualsNotNull(locale, otherLabel.locale));
        }
    }

    record DescriptionIdentifier(String id, Locale locale) implements Identifier {

        static DescriptionIdentifier of(IDescription description) {
            return new DescriptionIdentifier(description.getId(), description.getLocale());
        }

        static DescriptionIdentifier of(Element partEl) {
            String localeCode = getAttribute(partEl, IDescription.PROPERTY_LOCALE);
            return new DescriptionIdentifier(getId(partEl),
                    IpsStringUtils.isNotEmpty(localeCode) ? new Locale(localeCode) : null);
        }

        @Override
        public final boolean isSame(Identifier other) {
            return other instanceof DescriptionIdentifier otherDescription
                    && (isEqualsNotNull(id, otherDescription.id) || isEqualsNotNull(locale, otherDescription.locale));
        }
    }

    record ColumnRangeIdentifier(String id, String parameterName, ColumnRangeType rangeType) implements Identifier {

        static ColumnRangeIdentifier of(IColumnRange columnRange) {
            return new ColumnRangeIdentifier(columnRange.getId(), columnRange.getParameterName(),
                    columnRange.getColumnRangeType());
        }

        static ColumnRangeIdentifier of(Element partEl) {
            String parameterName = getAttribute(partEl, IColumnRange.PROPERTY_PARAMETER_NAME);
            String rangeTypeId = getAttribute(partEl, IColumnRange.PROPERTY_RANGE_TYPE);
            ColumnRangeType rangeType = ColumnRangeType.getValueById(rangeTypeId);
            return new ColumnRangeIdentifier(getId(partEl), parameterName, rangeType);
        }

        @Override
        public boolean isSame(Identifier other) {
            return other instanceof ColumnRangeIdentifier otherColumnRange
                    && (isEqualsNotNull(id, otherColumnRange.id)
                            || (isEqualsNotNull(parameterName, otherColumnRange.parameterName)
                                    && rangeType == otherColumnRange.rangeType));
        }
    }

    record LinkIdentifier(String id, String associationName, String targetRuntimeId) implements Identifier {

        static LinkIdentifier of(IProductCmptLink link) {
            return new LinkIdentifier(link.getId(), link.getAssociation(), link.getTargetRuntimeId());
        }

        static LinkIdentifier of(Element partEl) {
            String associationName = getAttribute(partEl, IProductCmptLink.PROPERTY_ASSOCIATION);
            String targetRuntimeId = getAttribute(partEl, IProductCmptLink.PROPERTY_TARGET_RUNTIME_ID);
            return new LinkIdentifier(getId(partEl), associationName, targetRuntimeId);
        }

        @Override
        public boolean isSame(Identifier other) {
            return other instanceof LinkIdentifier otherColumnRange
                    && (isEqualsNotNull(id, otherColumnRange.id)
                            || (isEqualsNotNull(associationName, otherColumnRange.associationName)
                                    && isEqualsNotNull(targetRuntimeId, otherColumnRange.targetRuntimeId)));
        }
    }

    record TestValueIdentifier(String id, String testParameterName) implements Identifier {

        static TestValueIdentifier of(ITestValue testValue) {
            return new TestValueIdentifier(testValue.getId(), testValue.getTestParameterName());
        }

        static TestValueIdentifier of(Element partEl) {
            String testParameterName = getAttribute(partEl, ITestValue.PROPERTY_VALUE_PARAMETER);
            return new TestValueIdentifier(getId(partEl), testParameterName);
        }

        @Override
        public final boolean isSame(Identifier other) {
            return other instanceof TestValueIdentifier otherTestValue
                    && (isEqualsNotNull(id, otherTestValue.id)
                            || isEqualsNotNull(testParameterName, otherTestValue.testParameterName));
        }
    }
}
