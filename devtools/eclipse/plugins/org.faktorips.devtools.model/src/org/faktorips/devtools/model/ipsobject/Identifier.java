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

import java.util.Arrays;
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
import org.faktorips.devtools.model.internal.method.BaseMethod;
import org.faktorips.devtools.model.internal.productcmpttype.TableStructureUsage.TableStructureReference;
import org.faktorips.devtools.model.internal.tablecontents.Row;
import org.faktorips.devtools.model.internal.tablestructure.ForeignKey;
import org.faktorips.devtools.model.internal.tablestructure.Index;
import org.faktorips.devtools.model.internal.tablestructure.Key;
import org.faktorips.devtools.model.internal.testcase.TestPolicyCmpt;
import org.faktorips.devtools.model.internal.testcase.TestPolicyCmptLink;
import org.faktorips.devtools.model.internal.testcase.TestValue;
import org.faktorips.devtools.model.internal.testcasetype.TestParameter;
import org.faktorips.devtools.model.internal.testcasetype.TestPolicyCmptTypeParameter;
import org.faktorips.devtools.model.internal.testcasetype.TestRuleParameter;
import org.faktorips.devtools.model.internal.testcasetype.TestValueParameter;
import org.faktorips.devtools.model.internal.type.Association;
import org.faktorips.devtools.model.method.IBaseMethod;
import org.faktorips.devtools.model.method.IParameter;
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
import org.faktorips.devtools.model.tablestructure.IKey;
import org.faktorips.devtools.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.model.testcase.ITestValue;
import org.faktorips.devtools.model.testcasetype.ITestParameter;
import org.faktorips.devtools.model.testcasetype.TestParameterType;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.devtools.model.type.IMethod;
import org.faktorips.devtools.model.util.XmlUtil;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

// Bug in the Eclipse imports organizer thinks the static imports are needed here
@SuppressWarnings("unused")
public interface Identifier {

    // CSOFF: CyclomaticComplexity
    /**
     * Creates a new {@link Identifier} based on the type of the given {@link IIpsObjectPart}
     *
     * @param part the IIpsObjectPart
     * @param index a counter for {@link IPartIdentifiedByIndex parts identified by their index}
     * @return a new {@link Identifier}
     */
    static Identifier of(IIpsObjectPart part, AtomicInteger index) {
        // TODO Java 21 turn to switch
        if (part instanceof ILabel label) {
            return LabelIdentifier.of(label);
        }
        if (part instanceof IDescription description) {
            return new DescriptionIdentifier(description.getLocale());
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
        if (part instanceof IKey key) {
            return KeyIdentifier.of(key);
        }
        if (part instanceof IIpsObjectGeneration generation) {
            return GenerationIdentifier.of(generation);
        }
        if (part instanceof IFormula formula) {
            return FormulaIdentifier.of(formula);
        }
        if (part instanceof IMethod method) {
            return MethodIdentifier.of(method);
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
        if (part instanceof TestParameter testParameter) {
            return TestParameterIdentifier.of(testParameter);
        }
        if (part instanceof IEnumValue enumValue) {
            return EmumValueIdentifier.of(enumValue);
        }
        if (part instanceof IEnumLiteralNameAttributeValue enumLiteralNameAttributeValue) {
            return new ByTypeIdentifier(IEnumLiteralNameAttributeValue.XML_TAG);
        }

        if (part instanceof IPartIdentifiedByIndex indexedPart) {
            return new IndexedIdentifier(index.getAndIncrement());
        }
        String name = part.getName();
        if (IpsStringUtils.isNotBlank(name)) {
            return new NamedIdentifier(name);
        }
        List<IdentityProvider> list = IIpsModelExtensions.get().getIdentifierForIpsObjectParts().get();
        return list.stream().map(idProvider -> idProvider.getIdentity(part))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
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
            case ForeignKey.TAG_NAME, Index.TAG_NAME, "UniqueKey" -> KeyIdentifier.of(partEl);
            case IIpsObjectGeneration.TAG_NAME -> GenerationIdentifier.of(partEl);
            case IExpression.TAG_NAME -> FormulaIdentifier.of(partEl);
            case IProductCmptLink.TAG_NAME -> {
                // TODO Java 21 use case ... when
                if (TestPolicyCmpt.TAG_NAME.equals(partEl.getParentNode().getNodeName())) {
                    yield TestPolicyCmptLinkIdentifier.of(partEl);
                }
                yield LinkIdentifier.of(partEl);
            }
            case BaseMethod.XML_ELEMENT_NAME -> MethodIdentifier.of(partEl);
            case TestPolicyCmpt.TAG_NAME -> TestPolicyCmptIdentifier.of(partEl);
            case TestPolicyCmptTypeParameter.TAG_NAME, TestRuleParameter.TAG_NAME, TestValueParameter.TAG_NAME -> TestParameterIdentifier
                    .of(partEl);
            case IEnumValue.XML_TAG -> EmumValueIdentifier.of(partEl, (IEnumValueContainer)container);
            case IEnumLiteralNameAttributeValue.XML_TAG -> new ByTypeIdentifier(IEnumAttributeValue.XML_TAG);
            case Row.TAG_NAME, IEnumAttributeValue.XML_TAG -> new IndexedIdentifier(index.getAndIncrement());
            default -> {
                String name = getAttribute(partEl, IIpsElement.PROPERTY_NAME);
                if (IpsStringUtils.isNotEmpty(name)) {
                    yield new NamedIdentifier(name);
                }
                List<IdentityProvider> list = IIpsModelExtensions.get().getIdentifierForIpsObjectParts().get();
                yield list.stream().map(idProvider -> idProvider.getIdentity(partEl))
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(null);
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

    record NamedIdentifier(String name) implements Identifier {

        public NamedIdentifier(String name) {
            this.name = IpsStringUtils.isBlank(name) ? "" : name;
        }
    }

    record IndexedIdentifier(int index) implements Identifier {
    }

    record KeyIdentifier(String[] keyItemNames) implements Identifier {

        public KeyIdentifier(String[] keyItemNames) {
            this.keyItemNames = keyItemNames == null ? new String[0] : keyItemNames;
        }

        static KeyIdentifier of(IKey key) {
            return new KeyIdentifier(key.getKeyItemNames());
        }

        static KeyIdentifier of(Element partEl) {
            List<Element> itemElements = org.faktorips.runtime.internal.XmlUtil.getChildElements(partEl,
                    Key.KEY_ITEM_TAG_NAME);
            String[] keyItemNames = itemElements.stream().map(itemElement -> getAttribute(itemElement, "name"))
                    .toArray(String[]::new);
            return new KeyIdentifier(keyItemNames);
        }
    }

    record EmumValueIdentifier(String idAttributeValue) implements Identifier {

        public EmumValueIdentifier(String idAttributeValue) {
            this.idAttributeValue = IpsStringUtils.isBlank(idAttributeValue) ? "" : idAttributeValue;
        }

        static Identifier of(IEnumValue enumValue) {
            IEnumValueContainer enumValueContainer = enumValue.getEnumValueContainer();
            return findIdAttribute(enumValueContainer)
                    .map(idAttribute -> (Identifier)new EmumValueIdentifier(
                            enumValue.getEnumAttributeValue(idAttribute).getStringValue()))
                    .orElse(null);
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
                return null;
            }
            Element idAttributeElement = XmlUtil.getElement(partEl, IEnumAttributeValue.XML_TAG, indexOfIdAttribute);
            String idAttributeValue = XmlUtil.getCDATAorTextContent(idAttributeElement);
            return new EmumValueIdentifier(idAttributeValue);
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
    }

    record TestPolicyCmptIdentifier(String testPolicyCmptTypeParameter) implements Identifier {

        public TestPolicyCmptIdentifier(String testPolicyCmptTypeParameter) {
            this.testPolicyCmptTypeParameter = IpsStringUtils.isBlank(testPolicyCmptTypeParameter) ? ""
                    : testPolicyCmptTypeParameter;
        }

        static TestPolicyCmptIdentifier of(ITestPolicyCmpt testPolicyCmpt) {
            String testPolicyCmptTypeParameter = testPolicyCmpt.getTestPolicyCmptTypeParameter();
            return new TestPolicyCmptIdentifier(testPolicyCmptTypeParameter);
        }

        static TestPolicyCmptIdentifier of(Element partEl) {
            String testPolicyCmptTypeParameter = getAttribute(partEl, ITestPolicyCmpt.PROPERTY_TESTPOLICYCMPTTYPE);
            return new TestPolicyCmptIdentifier(testPolicyCmptTypeParameter);
        }
    }

    record TestPolicyCmptLinkIdentifier(String testPolicyCmptTypeParameter, String targetId, String targetName)
            implements Identifier {

        public TestPolicyCmptLinkIdentifier(String testPolicyCmptTypeParameter, String targetId, String targetName) {
            this.testPolicyCmptTypeParameter = IpsStringUtils.isBlank(testPolicyCmptTypeParameter) ? ""
                    : testPolicyCmptTypeParameter;
            this.targetId = IpsStringUtils.isBlank(targetId) ? "" : targetId;
            this.targetName = IpsStringUtils.isBlank(targetName) ? "" : targetName;
        }

        static TestPolicyCmptLinkIdentifier of(TestPolicyCmptLink testPolicyCmptLink) {
            ITestPolicyCmpt target = testPolicyCmptLink.findTarget();
            String targetId = target != null ? target.getProductCmpt() : "";
            String targetName = target != null ? target.getName() : "";
            return new TestPolicyCmptLinkIdentifier(testPolicyCmptLink.getTestPolicyCmptTypeParameter(), targetId,
                    targetName);
        }

        static TestPolicyCmptLinkIdentifier of(Element partEl) {
            Element targetElement = XmlUtil.getFirstElement(partEl, ITestPolicyCmpt.TAG_NAME);
            String targetId = targetElement != null
                    ? getAttribute(targetElement, ITestPolicyCmpt.PROPERTY_PRODUCTCMPT)
                    : "";
            if (targetId == null) {
                targetId = "";
            }
            String targetName = targetElement != null
                    ? getAttribute(targetElement, IIpsElement.PROPERTY_NAME)
                    : "";
            if (targetName == null) {
                targetName = "";
            }
            String testPolicyCmptTypeParameter = getAttribute(partEl, TestPolicyCmptLink.PROPERTY_POLICYCMPTTYPE);
            return new TestPolicyCmptLinkIdentifier(testPolicyCmptTypeParameter, targetId, targetName);
        }

    }

    record TestParameterIdentifier(String name, TestParameterType type) implements Identifier {

        public TestParameterIdentifier(String name, TestParameterType type) {
            this.name = IpsStringUtils.isBlank(name) ? "" : name;
            this.type = type;
        }

        static TestParameterIdentifier of(TestParameter testParameter) {
            return new TestParameterIdentifier(testParameter.getName(), testParameter.getTestParameterType());
        }

        static TestParameterIdentifier of(Element partEl) {
            String name = getAttribute(partEl, IIpsElement.PROPERTY_NAME);
            TestParameterType type = TestParameterType
                    .getTestParameterType(partEl.getAttribute(ITestParameter.PROPERTY_TEST_PARAMETER_TYPE));
            return new TestParameterIdentifier(name, type);
        }

    }

    record FormulaIdentifier(String formulaSignature) implements Identifier {

        public FormulaIdentifier(String formulaSignature) {
            this.formulaSignature = IpsStringUtils.isBlank(formulaSignature) ? "" : formulaSignature;
        }

        static FormulaIdentifier of(IFormula formula) {
            return new FormulaIdentifier(formula.getFormulaSignature());
        }

        static FormulaIdentifier of(Element partEl) {
            String formulaSignature = getAttribute(partEl, IExpression.PROPERTY_FORMULA_SIGNATURE_NAME);
            return new FormulaIdentifier(formulaSignature);
        }
    }

    record GenerationIdentifier(String validFrom) implements Identifier {

        public GenerationIdentifier(String validFrom) {
            this.validFrom = IpsStringUtils.isBlank(validFrom) ? "" : validFrom;
        }

        static GenerationIdentifier of(IIpsObjectGeneration generation) {
            return new GenerationIdentifier(XmlUtil.gregorianCalendarToXmlDateString(generation.getValidFrom()));
        }

        static GenerationIdentifier of(Element partEl) {
            String validFrom = getAttribute(partEl, IIpsObjectGeneration.PROPERTY_VALID_FROM);
            return new GenerationIdentifier(validFrom);
        }
    }

    record ByTypeIdentifier(String type) implements Identifier {

        public ByTypeIdentifier(String type) {
            this.type = IpsStringUtils.isBlank(type) ? "" : type;
        }
    }

    record TableStructureUsageIdentifier(String roleName) implements Identifier {

        public TableStructureUsageIdentifier(String roleName) {
            this.roleName = IpsStringUtils.isBlank(roleName) ? "" : roleName;
        }

        static TableStructureUsageIdentifier of(ITableStructureUsage tableStructureUsage) {
            return new TableStructureUsageIdentifier(tableStructureUsage.getRoleName());
        }

        static TableStructureUsageIdentifier of(Element partEl) {
            String roleName = getAttribute(partEl, ITableStructureUsage.PROPERTY_ROLENAME);
            return new TableStructureUsageIdentifier(roleName);
        }
    }

    record TableStructureReferenceIdentifier(String tableStructureName) implements Identifier {

        public TableStructureReferenceIdentifier(String tableStructureName) {
            this.tableStructureName = IpsStringUtils.isBlank(tableStructureName) ? "" : tableStructureName;
        }

        static TableStructureReferenceIdentifier of(TableStructureReference tableStructureReference) {
            return new TableStructureReferenceIdentifier(tableStructureReference.getTableStructure());
        }

        static TableStructureReferenceIdentifier of(Element partEl) {
            String tableStructureName = getAttribute(partEl, ITableStructureUsage.PROPERTY_TABLESTRUCTURE);
            return new TableStructureReferenceIdentifier(tableStructureName);
        }
    }

    record TableContentUsageIdentifier(String roleName) implements Identifier {

        public TableContentUsageIdentifier(String roleName) {
            this.roleName = IpsStringUtils.isBlank(roleName) ? "" : roleName;
        }

        static TableContentUsageIdentifier of(ITableContentUsage tableContentUsage) {
            return new TableContentUsageIdentifier(tableContentUsage.getStructureUsage());
        }

        static TableContentUsageIdentifier of(Element partEl) {
            String tableUsageName = getAttribute(partEl, ITableContentUsage.PROPERTY_STRUCTURE_USAGE);
            return new TableContentUsageIdentifier(tableUsageName);
        }
    }

    record AttributeValueIdentifier(String attributeName) implements Identifier {

        public AttributeValueIdentifier(String attributeName) {
            this.attributeName = IpsStringUtils.isBlank(attributeName) ? "" : attributeName;
        }

        static AttributeValueIdentifier of(IAttributeValue attributeValue) {
            return new AttributeValueIdentifier(attributeValue.getAttribute());
        }

        static AttributeValueIdentifier of(Element partEl) {
            String attributeName = getAttribute(partEl, IAttributeValue.PROPERTY_ATTRIBUTE);
            return new AttributeValueIdentifier(attributeName);
        }
    }

    record ConfiguredDefaultIdentifier(String attributeName) implements Identifier {

        public ConfiguredDefaultIdentifier(String attributeName) {
            this.attributeName = IpsStringUtils.isBlank(attributeName) ? "" : attributeName;
        }

        static ConfiguredDefaultIdentifier of(IConfiguredDefault configuredDefault) {
            return new ConfiguredDefaultIdentifier(configuredDefault.getPolicyCmptTypeAttribute());
        }

        static ConfiguredDefaultIdentifier of(Element partEl) {
            String attributeName = getAttribute(partEl, IConfigElement.PROPERTY_POLICY_CMPT_TYPE_ATTRIBUTE);
            return new ConfiguredDefaultIdentifier(attributeName);
        }
    }

    record ConfiguredValueSetIdentifier(String attributeName) implements Identifier {

        public ConfiguredValueSetIdentifier(String attributeName) {
            this.attributeName = IpsStringUtils.isBlank(attributeName) ? "" : attributeName;
        }

        static ConfiguredValueSetIdentifier of(IConfiguredValueSet configuredValueSet) {
            return new ConfiguredValueSetIdentifier(configuredValueSet.getPolicyCmptTypeAttribute());
        }

        static ConfiguredValueSetIdentifier of(Element partEl) {
            String attributeName = getAttribute(partEl, IConfigElement.PROPERTY_POLICY_CMPT_TYPE_ATTRIBUTE);
            return new ConfiguredValueSetIdentifier(attributeName);
        }
    }

    record AssociationIdentifier(String targetRoleName) implements Identifier {

        public AssociationIdentifier(String targetRoleName) {
            this.targetRoleName = IpsStringUtils.isBlank(targetRoleName) ? "" : targetRoleName;
        }

        static AssociationIdentifier of(IAssociation association) {
            return new AssociationIdentifier(association.getTargetRoleSingular());
        }

        static AssociationIdentifier of(Element partEl) {
            String targetRoleName = getAttribute(partEl, Association.PROPERTY_TARGET_ROLE_SINGULAR);
            return new AssociationIdentifier(targetRoleName);
        }
    }

    record LabelIdentifier(Locale locale) implements Identifier {

        static LabelIdentifier of(ILabel label) {
            return new LabelIdentifier(label.getLocale());
        }

        static LabelIdentifier of(Element partEl) {
            String localeCode = getAttribute(partEl, ILabel.PROPERTY_LOCALE);
            return new LabelIdentifier(IpsStringUtils.isNotEmpty(localeCode) ? new Locale(localeCode) : null);
        }
    }

    record DescriptionIdentifier(Locale locale) implements Identifier {

        static DescriptionIdentifier of(IDescription description) {
            return new DescriptionIdentifier(description.getLocale());
        }

        static DescriptionIdentifier of(Element partEl) {
            String localeCode = getAttribute(partEl, IDescription.PROPERTY_LOCALE);
            return new DescriptionIdentifier(IpsStringUtils.isNotEmpty(localeCode) ? new Locale(localeCode) : null);
        }
    }

    record ColumnRangeIdentifier(String parameterName, ColumnRangeType rangeType) implements Identifier {

        public ColumnRangeIdentifier(String parameterName, ColumnRangeType rangeType) {
            this.parameterName = IpsStringUtils.isBlank(parameterName) ? "" : parameterName;
            this.rangeType = rangeType;
        }

        static ColumnRangeIdentifier of(IColumnRange columnRange) {
            return new ColumnRangeIdentifier(columnRange.getParameterName(), columnRange.getColumnRangeType());
        }

        static ColumnRangeIdentifier of(Element partEl) {
            String parameterName = getAttribute(partEl, IColumnRange.PROPERTY_PARAMETER_NAME);
            String rangeTypeId = getAttribute(partEl, IColumnRange.PROPERTY_RANGE_TYPE);
            ColumnRangeType rangeType = ColumnRangeType.getValueById(rangeTypeId);
            return new ColumnRangeIdentifier(parameterName, rangeType);
        }
    }

    record LinkIdentifier(String associationName, String targetRuntimeId) implements Identifier {

        public LinkIdentifier(String associationName, String targetRuntimeId) {
            this.associationName = IpsStringUtils.isBlank(associationName) ? "" : associationName;
            this.targetRuntimeId = IpsStringUtils.isBlank(targetRuntimeId) ? "" : targetRuntimeId;
        }

        static LinkIdentifier of(IProductCmptLink link) {
            return new LinkIdentifier(link.getAssociation(), link.getTargetRuntimeId());
        }

        static LinkIdentifier of(Element partEl) {
            String associationName = getAttribute(partEl, IProductCmptLink.PROPERTY_ASSOCIATION);
            String targetRuntimeId = getAttribute(partEl, IProductCmptLink.PROPERTY_TARGET_RUNTIME_ID);
            return new LinkIdentifier(associationName, targetRuntimeId);
        }
    }

    record TestValueIdentifier(String testParameterName) implements Identifier {

        public TestValueIdentifier(String testParameterName) {
            this.testParameterName = IpsStringUtils.isBlank(testParameterName) ? "" : testParameterName;
        }

        static TestValueIdentifier of(ITestValue testValue) {
            return new TestValueIdentifier(testValue.getTestParameterName());
        }

        static TestValueIdentifier of(Element partEl) {
            String testParameterName = getAttribute(partEl, ITestValue.PROPERTY_VALUE_PARAMETER);
            return new TestValueIdentifier(testParameterName);
        }
    }

    record MethodIdentifier(String name, String returnValue, List<String> parameters) implements Identifier {

        public MethodIdentifier(String name, String returnValue, List<String> parameters) {
            this.name = IpsStringUtils.isBlank(name) ? "" : name;
            this.returnValue = IpsStringUtils.isBlank(returnValue) ? "" : returnValue;
            this.parameters = parameters == null ? List.of() : parameters;
        }

        static MethodIdentifier of(IMethod method) {
            return new MethodIdentifier(method.getName(), method.getDatatype(),
                    Arrays.stream(method.getParameters()).map(IParameter::getDatatype).toList());
        }

        static MethodIdentifier of(Element partEl) {
            partEl.getChildNodes();
            return new MethodIdentifier(getAttribute(partEl, IBaseMethod.PROPERTY_NAME),
                    getAttribute(partEl, IBaseMethod.PROPERTY_DATATYPE),
                    org.faktorips.runtime.internal.XmlUtil.getChildElements(partEl,
                            IParameter.TAG_NAME).stream().map(p -> p.getAttribute(IParameter.PROPERTY_DATATYPE))
                            .toList());
        }
    }
}
