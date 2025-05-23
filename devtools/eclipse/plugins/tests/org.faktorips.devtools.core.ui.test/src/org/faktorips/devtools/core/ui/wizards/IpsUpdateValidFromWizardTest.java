/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.List;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.joda.LocalDateDatatype;
import org.faktorips.datatype.joda.LocalDateTimeDatatype;
import org.faktorips.devtools.core.ui.wizards.refactor.IpsUpdateValidfromWizard;
import org.faktorips.devtools.model.internal.productcmpt.MultiValueHolder;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmpt;
import org.faktorips.devtools.model.internal.productcmpt.SingleValueHolder;
import org.faktorips.devtools.model.internal.valueset.EnumValueSet;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpt.DateBasedProductCmptNamingStrategy;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.IConfiguredDefault;
import org.faktorips.devtools.model.productcmpt.IConfiguredValueSet;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.type.AssociationType;
import org.faktorips.devtools.model.valueset.ValueSetType;
import org.junit.Before;
import org.junit.Test;

public class IpsUpdateValidFromWizardTest extends AbstractIpsPluginTest {

    private IProductCmptType productCmptType;
    private IProductCmpt productCmpt;
    private IProductCmptGeneration productCmptGen;
    private IProductCmptTypeAssociation association;
    private IIpsProject ipsProject;
    private ProductCmpt productCmptTarget;
    private ProductCmpt productCmptTarget2;
    private ProductCmpt productCmptSharedTarget;

    @Before
    public void setup() {

        ipsProject = this.newIpsProject("TestProject");

        setProjectProperty(ipsProject, properties -> {
            properties.setProductCmptNamingStrategy(new DateBasedProductCmptNamingStrategy(" ", "yyyy-MM", true));
            properties.setPredefinedDatatypesUsed(new String[] {
                    Datatype.DECIMAL.getName(),
                    Datatype.MONEY.getName(),
                    Datatype.INTEGER.getName(),
                    Datatype.PRIMITIVE_INT.getName(),
                    Datatype.PRIMITIVE_LONG.getName(),
                    Datatype.PRIMITIVE_BOOLEAN.getName(),
                    Datatype.STRING.getName(),
                    Datatype.BOOLEAN.getName(),
                    LocalDateDatatype.DATATYPE.getName(),
                    LocalDateDatatype.GREGORIAN_CALENDAR.getName(),
                    LocalDateTimeDatatype.DATATYPE.getName()
            });
        });

        IPolicyCmptType policyCmptType = newPolicyAndProductCmptType(ipsProject, "TestPolicy", "dummy1");
        productCmptType = policyCmptType.findProductCmptType(ipsProject);

        IPolicyCmptType policyCmptTypeTarget = newPolicyAndProductCmptType(ipsProject, "TestTarget", "dummy2");
        IProductCmptType productCmptTypeTarget = policyCmptTypeTarget.findProductCmptType(ipsProject);

        IPolicyCmptType policyCmptTypeSharedTarget = newPolicyAndProductCmptType(ipsProject, "TestSharedTarget",
                "dummy23");
        IProductCmptType productCmptTypeSharedTarget = policyCmptTypeSharedTarget.findProductCmptType(ipsProject);

        association = productCmptType.newProductCmptTypeAssociation();
        association.setAssociationType(AssociationType.AGGREGATION);
        association.setTargetRoleSingular("TestRelation");
        association.setTarget(productCmptTypeTarget.getQualifiedName());

        productCmpt = newProductCmpt(productCmptType, "products.TestProduct 2025-01");
        productCmpt.setValidFrom(new GregorianCalendar(2025, 0, 1));

        productCmptGen = productCmpt.getProductCmptGeneration(0);

        IProductCmptTypeAssociation sharedTargetAssociation = productCmptTypeTarget.newProductCmptTypeAssociation();
        sharedTargetAssociation.setAssociationType(AssociationType.AGGREGATION);
        sharedTargetAssociation.setTargetRoleSingular("TestTargetRelation");
        sharedTargetAssociation.setTarget(productCmptTypeSharedTarget.getQualifiedName());

        productCmptTarget = newProductCmpt(productCmptTypeTarget, "products.TestProductTarget 2025-01");
        productCmptTarget2 = newProductCmpt(productCmptTypeTarget, "products.TestProductTarget2 2025-01");
        productCmptSharedTarget = newProductCmpt(productCmptTypeTarget, "products.TestProductSharedTarget 2025-01");

        productCmptTarget.setValidFrom(new GregorianCalendar(2025, 0, 1));
        productCmptTarget2.setValidFrom(new GregorianCalendar(2025, 0, 1));
        productCmptSharedTarget.setValidFrom(new GregorianCalendar(2025, 0, 1));

        IProductCmptLink link = productCmptGen.newLink(association.getName());
        link.setTarget(productCmptTarget.getQualifiedName());

        link = productCmptGen.newLink(association.getName());
        link.setTarget(productCmptTarget2.getQualifiedName());

        IProductCmptGeneration target1Gen = productCmptTarget.getProductCmptGeneration(0);

        IProductCmptLink sharedlink = target1Gen.newLink(sharedTargetAssociation.getName());
        sharedlink.setTarget(productCmptSharedTarget.getQualifiedName());

        IProductCmptGeneration target2Gen = productCmptTarget2.getProductCmptGeneration(0);

        IProductCmptLink sharedlink2 = target2Gen.newLink(sharedTargetAssociation.getName());
        sharedlink2.setTarget(productCmptSharedTarget.getQualifiedName());

        String oldDate = "2025-01-01";
        String oldDateTime = "2025-01-01T12:00:00";

        String newDate = "2028-01-01";
        String newDateTime = "2028-01-01T12:00:00";

        // PRODUCT ATTRIBUTES

        // Single/Unrestricted
        createUnrestrictedAttributeProduct(productCmptType, productCmptGen, "LocalDate", oldDate, false);
        createUnrestrictedAttributeProduct(productCmptType, productCmptGen, "GregorianCalendar", oldDate, false);
        createUnrestrictedAttributeProduct(productCmptType, productCmptGen, "LocalDateTime", oldDateTime, false);

        // Multi/Unrestricted
        createUnrestrictedAttributeProduct(productCmptType, productCmptGen, "LocalDate", oldDate, true);
        createUnrestrictedAttributeProduct(productCmptType, productCmptGen, "LocalDateTime", oldDateTime, true);

        // Single/Enum
        createEnumAttributeProduct(productCmptType, productCmptGen, "LocalDate", List.of(oldDate, newDate), false);
        createEnumAttributeProduct(productCmptType, productCmptGen, "LocalDateTime", List.of(oldDateTime, newDateTime),
                false);
        createEnumAttributeProduct(productCmptType, productCmptGen, "GregorianCalendar", List.of(oldDate, newDate),
                false);

        // Multi/Enum
        createEnumAttributeProduct(productCmptType, productCmptGen, "LocalDate", List.of(oldDate, newDate), true);
        createEnumAttributeProduct(productCmptType, productCmptGen, "LocalDateTime", List.of(oldDateTime, newDateTime),
                true);
        createEnumAttributeProduct(productCmptType, productCmptGen, "GregorianCalendar", List.of(oldDate, newDate),
                true);

        // POLICY ATTRIBUTES

        // Enums
        createEnumAttributePolicy(policyCmptType, "LocalDate", List.of(oldDate, newDate));
        createEnumAttributePolicy(policyCmptType, "GregorianCalendar", List.of(oldDate, newDate));
        createEnumAttributePolicy(policyCmptType, "LocalDateTime", List.of(oldDateTime, newDateTime));

        // Unrestricted
        createUnrestrictedAttributepPolicy(policyCmptType, "LocalDate", oldDate);
        createUnrestrictedAttributepPolicy(policyCmptType, "GregorianCalendar", oldDate);
        createUnrestrictedAttributepPolicy(policyCmptType, "LocalDateTime", oldDateTime);

        // Unrestricted configured Enum
        createUnrestrictedAttributepPolicyConfiguredEnum(policyCmptType, "LocalDate", oldDate);
        createUnrestrictedAttributepPolicyConfiguredEnum(policyCmptType, "GregorianCalendar", oldDate);
        createUnrestrictedAttributepPolicyConfiguredEnum(policyCmptType, "LocalDateTime", oldDate);

    }

    @Test
    public void testPerform_withoutAttributeChange() {

        // to avoid testing the whole eclipse refactoring in this unit test
        IpsUpdateValidfromWizard wizard = new IpsUpdateValidfromWizard(productCmpt) {
            @Override
            protected void performRenameRefactoring(IIpsObjectPartContainer target, String newName) {
                if (target instanceof IProductCmpt product) {
                    product.setRuntimeId(newName);
                }
            }
        };

        wizard.setAsyncExecutor(Runnable::run);

        GregorianCalendar newValue = new GregorianCalendar(2028, 1, 1);
        wizard.getPresentationModel().setNewValidFrom(newValue);
        wizard.getPresentationModel().setNewVersionId("2028-01");
        wizard.getPresentationModel().setChangeGenerationId(true);
        wizard.getPresentationModel().setChangeAttributes(false);

        wizard.performFinish();

        assertThat(productCmpt.getValidFrom(), is(newValue));
        assertThat(productCmpt.getRuntimeId(), is("TestProduct 2028-01"));

        assertThat(productCmptTarget.getValidFrom(), is(newValue));
        assertThat(productCmptTarget.getRuntimeId(), is("TestProductTarget 2028-01"));

        assertThat(productCmptTarget2.getValidFrom(), is(newValue));
        assertThat(productCmptTarget2.getRuntimeId(), is("TestProductTarget2 2028-01"));

        assertThat(productCmptSharedTarget.getValidFrom(), is(newValue));
        assertThat(productCmptSharedTarget.getRuntimeId(), is("TestProductSharedTarget 2028-01"));

    }

    @Test
    public void testPerform_withAttributeChange() {

        IpsUpdateValidfromWizard wizard = new IpsUpdateValidfromWizard(productCmpt);

        GregorianCalendar newValue = new GregorianCalendar(2028, 0, 1);
        wizard.getPresentationModel().setNewValidFrom(newValue);
        wizard.getPresentationModel().setNewVersionId("2028-01");
        wizard.getPresentationModel().setChangeGenerationId(false);
        wizard.getPresentationModel().setChangeAttributes(true);

        wizard.performFinish();

        assertThat(productCmptGen.getValidFrom(), is(newValue));

        String expectedDate = new SimpleDateFormat("yyyy-MM-dd").format(newValue.getTime());

        assertThat(productCmptGen.getAttributeValue("attr_LocalDate_UNRESTRICTED")
                .getValueHolder().getStringValue(), is(expectedDate));

        assertThat(productCmptGen.getAttributeValue("attr_GregorianCalendar_UNRESTRICTED")
                .getValueHolder().getStringValue(), is(expectedDate));

        assertThat(productCmptGen.getAttributeValue("attr_LocalDateTime_UNRESTRICTED")
                .getValueHolder().getStringValue(), startsWith(expectedDate));

        assertThat(productCmptGen.getAttributeValue("attr_LocalDate_UNRESTRICTED_MULTI")
                .getValueHolder().getValueList().get(0).getContentAsString(), is(expectedDate));

        assertThat(productCmptGen.getAttributeValue("attr_LocalDateTime_UNRESTRICTED_MULTI")
                .getValueHolder().getValueList().get(0).getContentAsString(), startsWith(expectedDate));

        assertThat(productCmptGen.getAttributeValue("attr_LocalDate_ENUM").getValueHolder().getStringValue(),
                is(expectedDate));

        assertThat(productCmptGen.getAttributeValue("attr_LocalDateTime_ENUM")
                .getValueHolder().getStringValue(), startsWith(expectedDate));

        assertThat(
                productCmptGen.getAttributeValue("attr_LocalDate_ENUM_MULTI").getValueHolder().getValueList().get(0)
                        .getContentAsString(),
                is(expectedDate));

        assertThat(productCmptGen.getAttributeValue("attr_LocalDateTime_ENUM_MULTI")
                .getValueHolder().getValueList().get(0).getContentAsString(), startsWith(expectedDate));

        assertThat(productCmptGen.getPropertyValue("attr_LocalDate_ENUM_POLICY", IConfiguredDefault.class).getValue(),
                is(expectedDate));

        assertThat(
                productCmptGen.getPropertyValue("attr_GregorianCalendar_ENUM_POLICY", IConfiguredDefault.class)
                        .getValue(),
                is(expectedDate));

        assertThat(
                productCmptGen.getPropertyValue("attr_LocalDateTime_ENUM_POLICY", IConfiguredDefault.class)
                        .getValue(),
                startsWith(expectedDate));

        assertThat(
                productCmptGen.getPropertyValue("attr_LocalDateTime_UNRESTRICTED_POLICY", IConfiguredDefault.class)
                        .getValue(),
                startsWith(expectedDate));

        assertThat(
                productCmptGen.getPropertyValue("attr_GregorianCalendar_UNRESTRICTED_POLICY", IConfiguredDefault.class)
                        .getValue(),
                startsWith(expectedDate));

        assertThat(
                productCmptGen.getPropertyValue("attr_LocalDate_UNRESTRICTED_POLICY", IConfiguredDefault.class)
                        .getValue(),
                startsWith(expectedDate));

        assertThat(
                productCmptGen.getPropertyValue("attr_LocalDate_UNRESTRICTED_POLICY_ENUM", IConfiguredDefault.class)
                        .getValue(),
                is(expectedDate));

    }

    private void createUnrestrictedAttributeProduct(IProductCmptType type,
            IProductCmptGeneration productCmptGen,
            String dataType,
            String value,
            boolean isMultiAttribute) {
        String suffix = isMultiAttribute ? "_MULTI" : "";
        String name = "attr_" + dataType + "_UNRESTRICTED" + suffix;

        IProductCmptTypeAttribute attr = type.newProductCmptTypeAttribute(name);
        attr.setDatatype(dataType);
        attr.setValueSetType(ValueSetType.UNRESTRICTED);
        attr.setMultiValueAttribute(isMultiAttribute);

        if (isMultiAttribute) {
            IAttributeValue attrVal = productCmptGen.newAttributeValue(attr);
            attrVal.setValueHolder(new MultiValueHolder(attrVal, List.of(new SingleValueHolder(attrVal, value))));
        } else {
            IAttributeValue attrVal = productCmptGen.newAttributeValue(attr);
            attrVal.setValueHolder(new SingleValueHolder(attrVal, value));
        }
    }

    private void createEnumAttributeProduct(IProductCmptType type,
            IProductCmptGeneration productCmptGen,
            String dataType,
            List<String> value,
            boolean isMultiAttribute) {

        String suffix = isMultiAttribute ? "_MULTI" : "";
        String name = "attr_" + dataType + "_ENUM" + suffix;

        IProductCmptTypeAttribute attr = type.newProductCmptTypeAttribute(name);
        attr.setDatatype(dataType);
        attr.setValueSetType(ValueSetType.ENUM);
        attr.setMultiValueAttribute(isMultiAttribute);

        EnumValueSet enumSet = (EnumValueSet)attr.getValueSet();

        enumSet.setContainsNull(false);
        enumSet.addValues(value);

        attr.setDefaultValue(value.get(0));

        if (isMultiAttribute) {
            IAttributeValue attrVal = productCmptGen.newAttributeValue(attr);
            attrVal.setValueHolder(new MultiValueHolder(attrVal, List.of(new SingleValueHolder(attrVal, value.get(0)),
                    new SingleValueHolder(attrVal, value.get(1)))));
        } else {
            IAttributeValue attrVal = productCmptGen.newAttributeValue(attr);
            attrVal.setValueHolder(new SingleValueHolder(attrVal, value.get(0)));
        }

    }

    private void createUnrestrictedAttributepPolicy(IPolicyCmptType type,
            String dataType,
            String value) {
        String name = "attr_" + dataType + "_UNRESTRICTED_POLICY";

        IPolicyCmptTypeAttribute attr = type.newPolicyCmptTypeAttribute(name);
        attr.setDatatype(dataType);
        attr.setValueSetType(ValueSetType.UNRESTRICTED);
        attr.setValueSetConfiguredByProduct(true);

        attr.setDefaultValue(value);

        IConfiguredValueSet configuredValueSet = productCmptGen.newPropertyValue(attr, IConfiguredValueSet.class);
        configuredValueSet.setValueSetType(ValueSetType.UNRESTRICTED);

        IConfiguredDefault configuredDefault = productCmptGen.newPropertyValue(attr, IConfiguredDefault.class);
        configuredDefault.setValue(value);

    }

    private void createEnumAttributePolicy(IPolicyCmptType type,
            String dataType,
            List<String> value) {
        String name = "attr_" + dataType + "_ENUM_POLICY";

        IPolicyCmptTypeAttribute attr = type.newPolicyCmptTypeAttribute(name);
        attr.setDatatype(dataType);
        attr.setValueSetType(ValueSetType.ENUM);
        attr.setValueSetConfiguredByProduct(true);
        attr.setChangingOverTime(true);

        EnumValueSet enumSet = (EnumValueSet)attr.getValueSet();

        enumSet.setContainsNull(false);
        enumSet.addValues(value);

        attr.setDefaultValue(value.get(0));

        IConfiguredValueSet configuredValueSet = productCmptGen.newPropertyValue(attr, IConfiguredValueSet.class);
        configuredValueSet.setValueSetType(ValueSetType.ENUM);
        EnumValueSet valueSet = (EnumValueSet)configuredValueSet.getValueSet();
        valueSet.addValues(value);

        IConfiguredDefault configuredDefault = productCmptGen.newPropertyValue(attr, IConfiguredDefault.class);
        configuredDefault.setValue(value.get(0));

    }

    private void createUnrestrictedAttributepPolicyConfiguredEnum(IPolicyCmptType type,
            String dataType,
            String value) {
        String name = "attr_" + dataType + "_UNRESTRICTED_POLICY_ENUM";

        IPolicyCmptTypeAttribute attr = type.newPolicyCmptTypeAttribute(name);
        attr.setDatatype(dataType);
        attr.setValueSetType(ValueSetType.UNRESTRICTED);
        attr.setValueSetConfiguredByProduct(true);

        attr.setDefaultValue(value);

        IConfiguredValueSet configuredValueSet = productCmptGen.newPropertyValue(attr, IConfiguredValueSet.class);
        configuredValueSet.setValueSetType(ValueSetType.ENUM);
        EnumValueSet valueSet = (EnumValueSet)configuredValueSet.getValueSet();
        valueSet.addValues(List.of(value));

        IConfiguredDefault configuredDefault = productCmptGen.newPropertyValue(attr, IConfiguredDefault.class);
        configuredDefault.setValue(value);

    }

}
