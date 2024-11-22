/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.model.type;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.faktorips.runtime.CardinalityRange;
import org.faktorips.runtime.FormulaExecutionException;
import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IModifiableRuntimeRepository;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.IProductComponentLink;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.ITimedConfigurableModelObject;
import org.faktorips.runtime.InMemoryRuntimeRepository;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.ValidationContext;
import org.faktorips.runtime.internal.AbstractModelObject;
import org.faktorips.runtime.internal.DateTime;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.runtime.internal.ProductComponent;
import org.faktorips.runtime.internal.ProductComponentGeneration;
import org.faktorips.runtime.internal.ProductComponentLink;
import org.faktorips.runtime.internal.ProductConfiguration;
import org.faktorips.runtime.model.IpsModel;
import org.faktorips.runtime.model.annotation.IpsAllowedValues;
import org.faktorips.runtime.model.annotation.IpsAllowedValuesSetter;
import org.faktorips.runtime.model.annotation.IpsAssociation;
import org.faktorips.runtime.model.annotation.IpsAssociationAdder;
import org.faktorips.runtime.model.annotation.IpsAssociationLinks;
import org.faktorips.runtime.model.annotation.IpsAssociationRemover;
import org.faktorips.runtime.model.annotation.IpsAssociations;
import org.faktorips.runtime.model.annotation.IpsAttribute;
import org.faktorips.runtime.model.annotation.IpsAttributeSetter;
import org.faktorips.runtime.model.annotation.IpsAttributes;
import org.faktorips.runtime.model.annotation.IpsChangingOverTime;
import org.faktorips.runtime.model.annotation.IpsConfiguredAttribute;
import org.faktorips.runtime.model.annotation.IpsConfiguredBy;
import org.faktorips.runtime.model.annotation.IpsConfigures;
import org.faktorips.runtime.model.annotation.IpsDefaultValue;
import org.faktorips.runtime.model.annotation.IpsDefaultValueSetter;
import org.faktorips.runtime.model.annotation.IpsDocumented;
import org.faktorips.runtime.model.annotation.IpsEnumAttribute;
import org.faktorips.runtime.model.annotation.IpsEnumType;
import org.faktorips.runtime.model.annotation.IpsFormula;
import org.faktorips.runtime.model.annotation.IpsFormulas;
import org.faktorips.runtime.model.annotation.IpsMatchingAssociation;
import org.faktorips.runtime.model.annotation.IpsPolicyCmptType;
import org.faktorips.runtime.model.annotation.IpsProductCmptType;
import org.faktorips.runtime.model.type.DefaultPolicyAttributeTest.Produkt;
import org.faktorips.runtime.util.ProductComponentLinks;
import org.faktorips.valueset.IntegerRange;
import org.faktorips.valueset.OrderedValueSet;
import org.faktorips.valueset.StringLengthValueSet;
import org.faktorips.valueset.ValueSet;
import org.junit.Test;

public class ProductCmptTypeTest {

    private final ProductCmptType productCmptType = IpsModel.getProductCmptType(Product.class);
    private final ProductCmptType superProductModel = IpsModel.getProductCmptType(SuperProduct.class);

    @Test
    public void testGetName() throws Exception {
        assertThat(productCmptType.getName(), is("MyProduct"));
        assertThat(superProductModel.getName(), is("MySuperProduct"));
    }

    @Test
    public void testGetSuperType() throws Exception {
        assertThat(productCmptType.getSuperType().getName(), is(superProductModel.getName()));
        assertThat(superProductModel.getSuperType(), is(nullValue()));
    }

    @Test
    public void testFindSuperType() throws Exception {
        assertThat(productCmptType.findSuperType().map(Type::getName).get(), is(superProductModel.getName()));
        assertThat(superProductModel.findSuperType().isPresent(), is(false));
    }

    @Test
    public void testIsConfiguredByProductCmptType() throws Exception {
        assertThat(productCmptType.isConfigurationForPolicyCmptType(), is(true));
        assertThat(superProductModel.isConfigurationForPolicyCmptType(), is(false));
    }

    @Test
    public void testGetPolicyCmptType() throws Exception {
        assertThat(productCmptType.getPolicyCmptType().getName(), is("MyPolicy"));
    }

    @Test(expected = NullPointerException.class)
    public void testGetPolicyCmptType_NPE_NotConfigured() throws Exception {
        superProductModel.getPolicyCmptType().getName();
    }

    @Test
    public void testIsChangingOverTime() throws Exception {
        assertThat(productCmptType.isChangingOverTime(), is(true));
        assertThat(superProductModel.isChangingOverTime(), is(false));
    }

    @Test
    public void testGetFormulas() {
        List<Formula> formulas = productCmptType.getFormulas();
        assertThat(formulas.size(), is(4));
        assertThat(formulas.get(0).getName(), is("formula1"));
        assertThat(formulas.get(1).getName(), is("formula2"));
        assertThat(formulas.get(2).getName(), is("formulaGen"));
        assertThat(formulas.get(3).getName(), is("supFormula"));
    }

    @Test
    public void testGetFormula() {
        assertThat(productCmptType.getFormula("formula1").getName(), is("formula1"));
        assertThat(productCmptType.getFormula("formula2").getName(), is("formula2"));
        assertThat(productCmptType.getFormula("formulaGen").getName(), is("formulaGen"));
        assertThat(productCmptType.getFormula("supFormula").getName(), is("supFormula"));
    }

    @Test
    public void testGetDeclaredFormulas() {
        assertThat(productCmptType.getDeclaredFormulas().size(), is(3));
        assertThat(productCmptType.getDeclaredFormulas().get(0).getName(), is("formula1"));
        assertThat(productCmptType.getDeclaredFormulas().get(1).getName(), is("formula2"));
        assertThat(productCmptType.getDeclaredFormulas().get(2).getName(), is("formulaGen"));
    }

    @Test
    public void testGetDeclaredFormula() {
        assertThat(productCmptType.getDeclaredFormula("formula1").getName(), is("formula1"));
        assertThat(productCmptType.getDeclaredFormula("formula2").getName(), is("formula2"));
        assertThat(productCmptType.getDeclaredFormula("formulaGen").getName(), is("formulaGen"));
    }

    @Test
    public void testGetDeclaredAttributes() {
        assertThat(productCmptType.getDeclaredAttributes().size(), is(5));
        assertThat(productCmptType.getDeclaredAttributes().get(0).getName(), is("attr"));
        assertThat(productCmptType.getDeclaredAttributes().get(1).getName(), is("overwrittenAttr"));
        assertThat(productCmptType.getDeclaredAttributes().get(2).getName(), is("BigAttr"));
        assertThat(productCmptType.getDeclaredAttributes().get(3).getName(), is("attr_changing"));
        assertThat(productCmptType.getDeclaredAttributes().get(4).getName(), is("abstractSuper"));
    }

    @Test
    public void testGetDeclaredAttribute_FindsCorrectCovariantReturnType() {
        assertThat(superProductModel.getDeclaredAttribute("abstractSuper").getDatatype().getName(),
                is(AbstractEnum.class.getName()));
        assertThat(productCmptType.getDeclaredAttribute("abstractSuper").getDatatype().getName(),
                is(SubOfAbstractEnum.class.getName()));
    }

    @Test
    public void testGetDeclaredAttribute() {
        assertThat(productCmptType.getDeclaredAttribute("attr").getName(), is("attr"));
        assertThat(productCmptType.getDeclaredAttribute("Attr").getName(), is("attr"));
        assertThat(productCmptType.getDeclaredAttribute("BigAttr").getName(), is("BigAttr"));
        assertThat(productCmptType.getDeclaredAttribute("bigAttr").getName(), is("BigAttr"));
    }

    @Test
    public void testGetAttributes() {
        assertThat(productCmptType.getAttributes().size(), is(6));
        assertThat(productCmptType.getAttributes().get(0).getName(), is("attr"));
        assertThat(productCmptType.getAttributes().get(1).getName(), is("overwrittenAttr"));
        assertThat(productCmptType.getAttributes().get(2).getName(), is("BigAttr"));
        assertThat(productCmptType.getAttributes().get(3).getName(), is("attr_changing"));
        assertThat(productCmptType.getAttributes().get(4).getName(), is("abstractSuper"));
        assertThat(productCmptType.getAttributes().get(5).getName(), is("supAttr"));
    }

    @Test
    public void testGetAttribute() {
        assertThat(productCmptType.getAttribute("attr").getName(), is("attr"));
        assertThat(productCmptType.getAttribute("Attr").getName(), is("attr"));
        assertThat(productCmptType.getAttribute("BigAttr").getName(), is("BigAttr"));
        assertThat(productCmptType.getAttribute("bigAttr").getName(), is("BigAttr"));
        assertThat(productCmptType.getAttribute("supAttr").getName(), is("supAttr"));
    }

    @Test
    public void testGetDeclaredAssociations() {
        assertThat(productCmptType.getDeclaredAssociations().size(), is(2));
        assertThat(productCmptType.getDeclaredAssociations().get(0).getName(), is("asso"));
        assertThat(productCmptType.getDeclaredAssociations().get(1).getName(), is("asso_changing"));
        assertThat(superProductModel.getDeclaredAssociations().size(), is(1));
        assertThat(superProductModel.getDeclaredAssociations().get(0).getName(), is("SupAsso"));
    }

    @Test
    public void testGetAssociations() {
        assertThat(productCmptType.getAssociations().size(), is(3));
        assertThat(productCmptType.getAssociations().get(0).getName(), is("asso"));
        assertThat(productCmptType.getAssociations().get(1).getName(), is("asso_changing"));
        assertThat(productCmptType.getAssociations().get(2).getName(), is("SupAsso"));
    }

    @Test
    public void testGetDeclaredAssociation() {
        ProductAssociation association = productCmptType.getDeclaredAssociation("asso");
        ProductAssociation superAssoInSuper = superProductModel.getDeclaredAssociation("SupAsso");
        ProductAssociation superAssoInSuperLowerCase = superProductModel.getDeclaredAssociation("supAsso");

        assertThat(association.getName(), is("asso"));
        assertThat(association.getNamePlural(), is(IpsStringUtils.EMPTY));
        assertThat(superAssoInSuper.getName(), is("SupAsso"));
        assertThat(superAssoInSuper.getNamePlural(), is("SupAssos"));
        assertThat(superAssoInSuperLowerCase, is(sameInstance(superAssoInSuper)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetDeclaredAssociation_withexception() {
        productCmptType.getDeclaredAssociation("SupAsso");
    }

    @Test
    public void testGetDeclaredAssociation_Plural() {
        ProductAssociation superAssoInSuper = superProductModel.getDeclaredAssociation("SupAssos");
        ProductAssociation superAssoInSuperLowerCase = superProductModel.getDeclaredAssociation("supAssos");

        assertThat(superAssoInSuper.getName(), is("SupAsso"));
        assertThat(superAssoInSuper.getNamePlural(), is("SupAssos"));
        assertThat(superAssoInSuperLowerCase, is(sameInstance(superAssoInSuper)));
    }

    @Test
    public void testGetAssociation() {
        ProductAssociation association = productCmptType.getAssociation("asso");
        ProductAssociation superAsso = productCmptType.getAssociation("SupAsso");
        ProductAssociation superAssoInSuper = superProductModel.getAssociation("SupAsso");
        ProductAssociation superAssoInSuperLowerCase = superProductModel.getAssociation("supAsso");

        assertThat(association.getName(), is("asso"));
        assertThat(association.getNamePlural(), is(IpsStringUtils.EMPTY));
        assertThat(superAsso.getName(), is("SupAsso"));
        assertThat(superAsso.getNamePlural(), is("SupAssos"));
        assertThat(superAssoInSuper.getName(), is("SupAsso"));
        assertThat(superAssoInSuper.getNamePlural(), is("SupAssos"));
        assertThat(superAssoInSuperLowerCase, is(sameInstance(superAssoInSuper)));
    }

    @Test
    public void testGetAssociation_Plural() {
        ProductAssociation superAsso = productCmptType.getAssociation("SupAssos");
        ProductAssociation superAssoInSuper = superProductModel.getAssociation("SupAssos");
        ProductAssociation superAssoInSuperLowerCase = superProductModel.getAssociation("supAssos");

        assertThat(superAsso.getName(), is("SupAsso"));
        assertThat(superAsso.getNamePlural(), is("SupAssos"));
        assertThat(superAssoInSuper.getName(), is("SupAsso"));
        assertThat(superAssoInSuper.getNamePlural(), is("SupAssos"));
        assertThat(superAssoInSuperLowerCase, is(sameInstance(superAssoInSuper)));
    }

    @Test
    public void testIsAttributePresent() {
        assertThat(productCmptType.isAttributePresent("supAttr"), is(true));
        assertThat(productCmptType.isAttributeDeclared("supAttr"), is(false));

        assertThat(productCmptType.isAttributePresent("overwrittenAttr"), is(true));
        assertThat(productCmptType.isAttributeDeclared("overwrittenAttr"), is(true));

        assertThat(productCmptType.isAttributePresent("BigAttr"), is(true));
        assertThat(productCmptType.isAttributeDeclared("BigAttr"), is(true));
    }

    @Test
    public void isSameOrSub_Same() {
        assertThat(IpsModel.getProductCmptType(Product.class).isSameOrSub(productCmptType), is(true));
    }

    @Test
    public void isSameOrSub_Sub() {
        assertThat(productCmptType.isSameOrSub(IpsModel.getProductCmptType(SuperProduct.class)), is(true));
    }

    @Test
    public void isSameOrSub_NoSubOrSame() {
        assertThat(IpsModel.getProductCmptType(SuperProduct.class).isSameOrSub(productCmptType), is(false));
    }

    @Test
        public void testValidatePart_WrongType() {
            IRuntimeRepository repository = new InMemoryRuntimeRepository();
            Produkt productComponent = new Produkt(repository);
            ProductCmptType productCmptType = IpsModel.getProductCmptType(ConfiguringProduct.class);
    
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> productCmptType.validate(productComponent, new MessageList(), new ValidationContext()));
            assertThat(exception.getMessage(), containsString("id"));
            assertThat(exception.getMessage(), containsString("ConfiguringProduct"));
        }

    @Test
        public void testValidatePart_Attributes() {
            IModifiableRuntimeRepository repository = new InMemoryRuntimeRepository();
            ProductCmptType productCmptType = IpsModel.getProductCmptType(ConfiguringProduct.class);
            ConfiguringProduct productComponent = new ConfiguringProduct(repository, "P 1", "P", "1");
            productComponent.setAllowedValuesForStaticPolicyAttribute(new StringLengthValueSet(42));
            productComponent.setStaticProductAttribute("foo");
            repository.putProductComponent(productComponent);
            TargetProduct targetProduct = new TargetProduct(repository, "T 1", "T", "1");
            repository.putProductComponent(targetProduct);
            ConfiguringProductAdj configuringProductAdj1 = new ConfiguringProductAdj(productComponent);
            configuringProductAdj1.setValidFrom(new DateTime(2024, 1, 1));
            configuringProductAdj1.setProductAttribute(-1);
            configuringProductAdj1.setDefaultValuePolicyAttribute(99);
            configuringProductAdj1.addProductAssociation(targetProduct, CardinalityRange.MANDATORY);
            repository.putProductCmptGeneration(configuringProductAdj1);
            MessageList messages = new MessageList();
    
            productCmptType.validate(productComponent, messages, new ValidationContext());
    
            assertThat(messages.containsErrorMsg(), is(true));
            assertThat(messages.size(), is(4));
            assertThat(messages.toString(), containsString("foo"));
            assertThat(messages.toString(), containsString("42"));
            assertThat(messages.toString(), containsString("-1"));
            assertThat(messages.toString(), containsString("99"));
            assertThat(
                    messages.getMessageByCode(DefaultPolicyAttribute.MSGCODE_VALUE_SET_NOT_IN_VALUE_SET)
                            .getInvalidObjectProperties().get(0).getProperty(),
                    is(DefaultPolicyAttribute.PROPERTY_VALUE_SET));
            assertThat(
                    messages.getMessageByCode(ProductAttribute.MSGCODE_VALUE_NOT_IN_VALUE_SET)
                            .getInvalidObjectProperties().get(0).getProperty(),
                    is(ProductAttribute.PROPERTY_VALUE));
        }

    @Test
        public void testValidatePart_Associations() {
            IModifiableRuntimeRepository repository = new InMemoryRuntimeRepository();
            ProductCmptType productCmptType = IpsModel.getProductCmptType(ConfiguringProduct.class);
            TargetProduct targetProduct = new TargetProduct(repository, "T 1", "T", "1");
            repository.putProductComponent(targetProduct);
            ConfiguringProduct productComponent = new ConfiguringProduct(repository, "P 1", "P", "1");
            productComponent.addStaticProductAssociation(targetProduct, new CardinalityRange(100, 200, 150));
            repository.putProductComponent(productComponent);
            ConfiguringProductAdj configuringProductAdj1 = new ConfiguringProductAdj(productComponent);
            configuringProductAdj1.setValidFrom(new DateTime(2024, 1, 1));
            repository.putProductCmptGeneration(configuringProductAdj1);
            MessageList messages = new MessageList();
    
            productCmptType.validate(productComponent, messages, new ValidationContext());
    
            assertThat(messages.containsErrorMsg(), is(true));
            assertThat(messages.size(), is(2));
            assertThat(messages.toString(), containsString("200"));
            assertThat(messages.toString(), containsString("1"));
        }

    @IpsProductCmptType(name = "MyProduct")
    @IpsConfigures(Policy.class)
    @IpsChangingOverTime(ProductGen.class)
    @IpsAttributes({ "attr", "overwrittenAttr", "BigAttr", "attr_changing", "abstractSuper" })
    @IpsFormulas({ "formula1", "formula2", "formulaGen" })
    @IpsAssociations({ "asso", "asso_changing" })
    private abstract static class Product extends SuperProduct {

        public Product(IRuntimeRepository repository, String id, String PolicyKindId, String versionId) {
            super(repository, id, PolicyKindId, versionId);
        }

        @IpsAttribute(name = "attr", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.AllValues)
        public abstract String getAttr();

        @IpsAttributeSetter("attr")
        public abstract void setAttr(String attr);

        @Override
        @IpsAttribute(name = "overwrittenAttr", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.AllValues)
        public abstract String getOverwrittenAttr();

        @Override
        @IpsAttribute(name = "abstractSuper", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.AllValues)
        public abstract SubOfAbstractEnum getAbstractSuper();

        @IpsAttribute(name = "BigAttr", kind = AttributeKind.CONSTANT, valueSetKind = ValueSetKind.AllValues)
        public abstract String getBigAttr();

        @IpsAssociation(name = "asso", min = 1, max = 2, targetClass = Product.class, kind = AssociationKind.Association)
        public abstract Product getAsso();

        @IpsFormula(name = "formula1")
        public Integer computeFormula1(String param) throws FormulaExecutionException {
            return (Integer)getFormulaEvaluator().evaluate("computeFormula1", param);
        }

        @IpsFormula(name = "formula2")
        public String computeFormula2() throws FormulaExecutionException {
            return (String)getFormulaEvaluator().evaluate("computeFormula2");
        }
    }

    private abstract static class ProductGen extends ProductComponentGeneration {

        public ProductGen(ProductComponent productCmpt) {
            super(productCmpt);
        }

        @IpsAttribute(name = "attr_changing", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.AllValues)
        public abstract String getAttr();

        @IpsAttributeSetter("attr_changing")
        public abstract void setAttr(String attr);

        @IpsAssociation(name = "asso_changing", min = 1, max = 2, targetClass = Product.class, kind = AssociationKind.Association)
        public abstract ProductGen getAsso_changing();

        @IpsFormula(name = "formulaGen")
        public String computeFormulaGen() {
            return (String)getFormulaEvaluator().evaluate("computeFormulaGen");
        }
    }

    @IpsProductCmptType(name = "MySuperProduct")
    @IpsAttributes({ "supAttr", "overwrittenAttr", "abstractSuper" })
    @IpsAssociations({ "SupAsso" })
    @IpsFormulas({ "supFormula" })
    private abstract static class SuperProduct extends ProductComponent {

        @IpsAttribute(name = "supAttr", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.AllValues)
        public int getSup() {
            return 1;
        }

        @IpsAttribute(name = "overwrittenAttr", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.AllValues)
        public abstract String getOverwrittenAttr();

        @IpsAttribute(name = "abstractSuper", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.AllValues)
        public abstract AbstractEnum getAbstractSuper();

        @IpsAssociation(name = "SupAsso", pluralName = "SupAssos", max = 5, min = 1, targetClass = SuperProduct.class, kind = AssociationKind.Association)
        public abstract SuperProduct getSupAsso();

        @IpsFormula(name = "supFormula")
        public Integer computeSupFormula(String param) throws FormulaExecutionException {
            return (Integer)getFormulaEvaluator().evaluate("computeSupFormula", param);
        }

        public SuperProduct(IRuntimeRepository repository, String id, String PolicyKindId, String versionId) {
            super(repository, id, PolicyKindId, versionId);
        }

    }

    @IpsPolicyCmptType(name = "MyPolicy")
    private abstract static class Policy extends AbstractModelObject implements IConfigurableModelObject {
        // a policy
    }

    @IpsEnumType(name = "enums.AnnotatedAbstractEnum", attributeNames = { "id", "name" })
    private interface AbstractEnum {

        @IpsEnumAttribute(name = "id", identifier = true, unique = true)
        String getId();

        @IpsEnumAttribute(name = "name", unique = true, displayName = true)
        String getName();
    }

    @IpsEnumType(name = "enums.SubOfAnnotatedAbstractEnum", attributeNames = { "id", "name" })
    private enum SubOfAbstractEnum implements AbstractEnum {

        VALUE("1", "1");

        private final String id;
        private final String name;

        SubOfAbstractEnum(String id, String name) {
            this.id = id;
            this.name = name;
        }

        @IpsEnumAttribute(name = "id", identifier = true, unique = true)
        @Override
        public String getId() {
            return id;
        }

        @IpsEnumAttribute(name = "name", unique = true, displayName = true)
        @Override
        public String getName() {
            return name;
        }

    }

    @IpsPolicyCmptType(name = "ConfiguredPolicy")
    @IpsAttributes({ "staticPolicyAttribute", "policyAttribute" })
    @IpsAssociations({ "staticPolicyAssociation", "policyAssociation" })
    @IpsConfiguredBy(ConfiguringProduct.class)
    @IpsDocumented(bundleName = "org.faktorips.sample.model.model-label-and-descriptions", defaultLocale = "en")
    public static class ConfiguredPolicy extends AbstractModelObject implements ITimedConfigurableModelObject {

        public static final IntegerRange MAX_MULTIPLICITY_OF_STATIC_POLICY_ASSOCIATION = IntegerRange.valueOf(0, 1);

        public static final String ASSOCIATION_STATIC_POLICY_ASSOCIATION = "staticPolicyAssociation";

        public static final IntegerRange MAX_MULTIPLICITY_OF_POLICY_ASSOCIATION = IntegerRange.valueOf(1, 2147483647);

        public static final String ASSOCIATION_POLICY_ASSOCIATIONS = "policyAssociations";

        public static final String PROPERTY_STATICPOLICYATTRIBUTE = "staticPolicyAttribute";
        @IpsAllowedValues("staticPolicyAttribute")
        public static final ValueSet<String> MAX_ALLOWED_STRING_LENGTH_FOR_STATIC_POLICY_ATTRIBUTE = new StringLengthValueSet(
                5, false);
        @IpsDefaultValue("staticPolicyAttribute")
        public static final String DEFAULT_VALUE_FOR_STATIC_POLICY_ATTRIBUTE = null;
        public static final String PROPERTY_POLICYATTRIBUTE = "policyAttribute";
        @IpsAllowedValues("policyAttribute")
        public static final OrderedValueSet<Integer> MAX_ALLOWED_VALUES_FOR_POLICY_ATTRIBUTE = new OrderedValueSet<>(
                false,
                null, Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3));
        @IpsDefaultValue("policyAttribute")
        public static final Integer DEFAULT_VALUE_FOR_POLICY_ATTRIBUTE = null;

        private String staticPolicyAttribute = DEFAULT_VALUE_FOR_STATIC_POLICY_ATTRIBUTE;
        private Integer policyAttribute = DEFAULT_VALUE_FOR_POLICY_ATTRIBUTE;

        private ProductConfiguration productConfiguration;

        private TargetPolicy staticPolicyAssociation = null;

        private List<TargetPolicy> policyAssociations = new ArrayList<>();

        public ConfiguredPolicy() {
            super();
            productConfiguration = new ProductConfiguration();
        }

        public ConfiguredPolicy(ConfiguringProduct productCmpt) {
            super();
            productConfiguration = new ProductConfiguration(productCmpt);
        }

        @IpsAllowedValues("staticPolicyAttribute")
        public ValueSet<String> getAllowedValuesForStaticPolicyAttribute() {
            return getConfiguringProduct().getAllowedValuesForStaticPolicyAttribute();
        }

        @IpsAttribute(name = "staticPolicyAttribute", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.StringLength)
        @IpsConfiguredAttribute(changingOverTime = false)
        public String getStaticPolicyAttribute() {
            return staticPolicyAttribute;
        }

        @IpsAttributeSetter("staticPolicyAttribute")
        public void setStaticPolicyAttribute(String newValue) {
            staticPolicyAttribute = newValue;
        }

        @IpsAllowedValues("policyAttribute")
        public ValueSet<Integer> getAllowedValuesForPolicyAttribute() {
            return getConfiguringProductAdj().getAllowedValuesForPolicyAttribute();
        }

        @IpsAttribute(name = "policyAttribute", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.Enum)
        @IpsConfiguredAttribute(changingOverTime = true)
        public Integer getPolicyAttribute() {
            return policyAttribute;
        }

        @IpsAttributeSetter("policyAttribute")
        public void setPolicyAttribute(Integer newValue) {
            policyAttribute = newValue;
        }

        @IpsAssociation(name = "staticPolicyAssociation", pluralName = "", kind = AssociationKind.Composition, targetClass = TargetPolicy.class, min = 0, max = 1)
        @IpsMatchingAssociation(source = ConfiguringProduct.class, name = "staticProductAssociation")
        public TargetPolicy getStaticPolicyAssociation() {
            return staticPolicyAssociation;
        }

        @IpsAssociationAdder(association = "staticPolicyAssociation")
        public void setStaticPolicyAssociation(TargetPolicy newObject) {
            staticPolicyAssociation = newObject;
        }

        public TargetPolicy newStaticPolicyAssociation() {
            TargetPolicy newStaticPolicyAssociation = new TargetPolicy();
            setStaticPolicyAssociation(newStaticPolicyAssociation);
            newStaticPolicyAssociation.initialize();
            return newStaticPolicyAssociation;
        }

        public TargetPolicy newStaticPolicyAssociation(TargetProduct targetProduct) {
            if (targetProduct == null) {
                return newStaticPolicyAssociation();
            }
            TargetPolicy newStaticPolicyAssociation = targetProduct.createTargetPolicy();
            setStaticPolicyAssociation(newStaticPolicyAssociation);
            newStaticPolicyAssociation.initialize();
            return newStaticPolicyAssociation;
        }

        public int getNumOfPolicyAssociations() {
            return policyAssociations.size();
        }

        public boolean containsPolicyAssociation(TargetPolicy objectToTest) {
            return policyAssociations.contains(objectToTest);
        }

        @IpsAssociation(name = "policyAssociation", pluralName = "policyAssociations", kind = AssociationKind.Composition, targetClass = TargetPolicy.class, min = 0, max = Integer.MAX_VALUE)
        @IpsMatchingAssociation(source = ConfiguringProduct.class, name = "ProductAssociation")
        public List<? extends TargetPolicy> getPolicyAssociations() {
            return Collections.unmodifiableList(policyAssociations);
        }

        public TargetPolicy getPolicyAssociation(int index) {
            return policyAssociations.get(index);
        }

        @IpsAssociationAdder(association = "policyAssociation")
        public void addPolicyAssociation(TargetPolicy objectToAdd) {
            if (objectToAdd == null) {
                throw new NullPointerException("Can't add null to association policyAssociation of " + this);
            }
            if (policyAssociations.contains(objectToAdd)) {
                return;
            }
            policyAssociations.add(objectToAdd);
        }

        public TargetPolicy newPolicyAssociation() {
            TargetPolicy newPolicyAssociation = new TargetPolicy();
            addPolicyAssociation(newPolicyAssociation);
            newPolicyAssociation.initialize();
            return newPolicyAssociation;
        }

        public TargetPolicy newPolicyAssociation(TargetProduct targetProduct) {
            if (targetProduct == null) {
                return newPolicyAssociation();
            }
            TargetPolicy newPolicyAssociation = targetProduct.createTargetPolicy();
            addPolicyAssociation(newPolicyAssociation);
            newPolicyAssociation.initialize();
            return newPolicyAssociation;
        }

        @IpsAssociationRemover(association = "policyAssociation")
        public void removePolicyAssociation(TargetPolicy objectToRemove) {
            if (objectToRemove == null) {
                return;
            }
            policyAssociations.remove(objectToRemove);
        }

        @Override
        public void initialize() {
            if (getConfiguringProduct() != null) {
                setStaticPolicyAttribute(getConfiguringProduct().getDefaultValueStaticPolicyAttribute());
            }
        }

        public ConfiguringProduct getConfiguringProduct() {
            return (ConfiguringProduct)getProductComponent();
        }

        @Override
        public IProductComponent getProductComponent() {
            return productConfiguration.getProductComponent();
        }

        @Override
        public void setProductComponent(IProductComponent productComponent) {
            productConfiguration.setProductComponent(productComponent);
        }

        public ConfiguringProductAdj getConfiguringProductAdj() {
            return (ConfiguringProductAdj)getProductCmptGeneration();
        }

        @Override
        public IProductComponentGeneration getProductCmptGeneration() {
            return productConfiguration.getProductCmptGeneration(getEffectiveFromAsCalendar());
        }

        public void setProductCmptGeneration(IProductComponentGeneration productComponentGeneration) {
            productConfiguration.setProductCmptGeneration(productComponentGeneration);
        }

        @Override
        public Calendar getEffectiveFromAsCalendar() {
            return null;
        }

    }

    @IpsProductCmptType(name = "ConfiguringProduct")
    @IpsAttributes({ "staticProductAttribute", "productAttribute" })
    @IpsAssociations({ "staticProductAssociation", "ProductAssociation" })
    @IpsConfigures(ConfiguredPolicy.class)
    @IpsChangingOverTime(ConfiguringProductAdj.class)
    @IpsDocumented(bundleName = "org.faktorips.sample.model.model-label-and-descriptions", defaultLocale = "en")
    public static class ConfiguringProduct extends ProductComponent {

        public static final String PROPERTY_STATICPRODUCTATTRIBUTE = "staticProductAttribute";
        @IpsAllowedValues("staticProductAttribute")
        public static final OrderedValueSet<String> MAX_ALLOWED_VALUES_FOR_STATIC_PRODUCT_ATTRIBUTE = new OrderedValueSet<>(
                false, null, "a", "b", "c");
        @IpsDefaultValue("staticProductAttribute")
        public static final String DEFAULT_VALUE_FOR_STATIC_PRODUCT_ATTRIBUTE = null;

        private String staticProductAttribute = DEFAULT_VALUE_FOR_STATIC_PRODUCT_ATTRIBUTE;

        private String defaultValueStaticPolicyAttribute = null;
        private ValueSet<String> maximumLengthStaticPolicyAttribute = ConfiguredPolicy.MAX_ALLOWED_STRING_LENGTH_FOR_STATIC_POLICY_ATTRIBUTE;

        private Map<String, IProductComponentLink<TargetProduct>> staticProductAssociations = new LinkedHashMap<>(0);

        public ConfiguringProduct(IRuntimeRepository repository, String id, String kindId, String versionId) {
            super(repository, id, kindId, versionId);
        }

        public ConfiguringProductAdj getConfiguringProductAdj(Calendar effectiveDate) {
            return (ConfiguringProductAdj)getRepository().getProductComponentGeneration(getId(), effectiveDate);
        }

        @Override
        public boolean isChangingOverTime() {
            return true;
        }

        @IpsAttribute(name = "staticProductAttribute", kind = AttributeKind.CONSTANT, valueSetKind = ValueSetKind.Enum)
        public String getStaticProductAttribute() {
            return staticProductAttribute;
        }

        @IpsAttributeSetter("staticProductAttribute")
        public void setStaticProductAttribute(String newValue) {
            setStaticProductAttributeInternal(newValue);
        }

        protected final void setStaticProductAttributeInternal(String newValue) {
            staticProductAttribute = newValue;
        }

        @IpsDefaultValue("staticPolicyAttribute")
        public String getDefaultValueStaticPolicyAttribute() {
            return defaultValueStaticPolicyAttribute;
        }

        @IpsDefaultValueSetter("staticPolicyAttribute")
        public void setDefaultValueStaticPolicyAttribute(String defaultValueStaticPolicyAttribute) {
            this.defaultValueStaticPolicyAttribute = defaultValueStaticPolicyAttribute;
        }

        @IpsAllowedValues("staticPolicyAttribute")
        public ValueSet<String> getAllowedValuesForStaticPolicyAttribute() {
            return maximumLengthStaticPolicyAttribute;
        }

        @IpsAllowedValuesSetter("staticPolicyAttribute")
        public void setAllowedValuesForStaticPolicyAttribute(ValueSet<String> maximumLengthStaticPolicyAttribute) {
            this.maximumLengthStaticPolicyAttribute = maximumLengthStaticPolicyAttribute;
        }

        @IpsAssociation(name = "staticProductAssociation", pluralName = "staticProductAssociations", kind = AssociationKind.Composition, targetClass = TargetProduct.class, min = 0, max = Integer.MAX_VALUE)
        @IpsMatchingAssociation(source = ConfiguredPolicy.class, name = "staticPolicyAssociation")
        public List<? extends TargetProduct> getStaticProductAssociations() {
            List<TargetProduct> result = new ArrayList<>(staticProductAssociations.size());
            for (IProductComponentLink<TargetProduct> staticProductAssociation : staticProductAssociations.values()) {
                if (!staticProductAssociation.getCardinality().isEmpty()) {
                    result.add(staticProductAssociation.getTarget());
                }
            }
            return result;
        }

        public TargetProduct getStaticProductAssociation(int index) {
            return ProductComponentLinks.getTarget(index, staticProductAssociations);
        }

        @IpsAssociationAdder(association = "staticProductAssociation")
        public void addStaticProductAssociation(TargetProduct target) {
            staticProductAssociations.put(target.getId(),
                    new ProductComponentLink<>(this, target, "staticProductAssociation"));
        }

        @IpsAssociationAdder(association = "staticProductAssociation", withCardinality = true)
        public void addStaticProductAssociation(TargetProduct target, CardinalityRange cardinality) {
            staticProductAssociations.put(target.getId(),
                    new ProductComponentLink<>(this, target, cardinality, "staticProductAssociation"));
        }

        @IpsAssociationRemover(association = "staticProductAssociation")
        public void removeStaticProductAssociation(TargetProduct target) {
            staticProductAssociations.remove(target.getId());
        }

        @IpsAssociationLinks(association = "staticProductAssociation")
        public Collection<IProductComponentLink<TargetProduct>> getLinksForStaticProductAssociations() {
            return Collections.unmodifiableCollection(staticProductAssociations.values());
        }

        public IProductComponentLink<TargetProduct> getLinkForStaticProductAssociation(TargetProduct productComponent) {
            return staticProductAssociations.get(productComponent.getId());
        }

        public CardinalityRange getCardinalityForStaticPolicyAssociation(TargetProduct productCmpt) {
            if (productCmpt != null) {
                return staticProductAssociations.containsKey(productCmpt.getId())
                        ? staticProductAssociations.get(productCmpt.getId())
                                .getCardinality()
                        : null;
            }
            return null;
        }

        public int getNumOfstaticProductAssociations() {
            return staticProductAssociations.size();
        }

        public ConfiguredPolicy createConfiguredPolicy() {
            ConfiguredPolicy policy = new ConfiguredPolicy(this);
            policy.initialize();
            return policy;
        }

        @Override
        public ConfiguredPolicy createPolicyComponent() {
            return createConfiguredPolicy();
        }

        @Override
        public IProductComponentLink<? extends IProductComponent> getLink(String linkName, IProductComponent target) {
            if ("staticProductAssociation".equals(linkName)) {
                return getLinkForStaticProductAssociation((TargetProduct)target);
            }
            return null;
        }

        @Override
        public List<IProductComponentLink<? extends IProductComponent>> getLinks() {
            List<IProductComponentLink<? extends IProductComponent>> list = new ArrayList<>();
            list.addAll(getLinksForStaticProductAssociations());
            return list;
        }
    }

    public static class ConfiguringProductAdj extends ProductComponentGeneration {

        public static final String PROPERTY_PRODUCTATTRIBUTE = "productAttribute";
        @IpsAllowedValues("productAttribute")
        public static final IntegerRange MAX_ALLOWED_RANGE_FOR_PRODUCT_ATTRIBUTE = IntegerRange
                .valueOf(Integer.valueOf("0"), Integer.valueOf(100), Integer.valueOf(10), false);
        @IpsDefaultValue("productAttribute")
        public static final Integer DEFAULT_VALUE_FOR_PRODUCT_ATTRIBUTE = null;

        private Integer productAttribute = DEFAULT_VALUE_FOR_PRODUCT_ATTRIBUTE;

        private Integer defaultValuePolicyAttribute = null;
        private OrderedValueSet<Integer> allowedValuesForPolicyAttribute = ConfiguredPolicy.MAX_ALLOWED_VALUES_FOR_POLICY_ATTRIBUTE;
        private Map<String, IProductComponentLink<TargetProduct>> productAssociations = new LinkedHashMap<>(0);

        public ConfiguringProductAdj(ConfiguringProduct productCmpt) {
            super(productCmpt);
        }

        @IpsAttribute(name = "productAttribute", kind = AttributeKind.CONSTANT, valueSetKind = ValueSetKind.Range)
        public Integer getProductAttribute() {
            return productAttribute;
        }

        @IpsAttributeSetter("productAttribute")
        public void setProductAttribute(Integer newValue) {
            setProductAttributeInternal(newValue);
        }

        protected final void setProductAttributeInternal(Integer newValue) {
            productAttribute = newValue;
        }

        @IpsDefaultValue("policyAttribute")
        public Integer getDefaultValuePolicyAttribute() {
            return defaultValuePolicyAttribute;
        }

        @IpsDefaultValueSetter("policyAttribute")
        public void setDefaultValuePolicyAttribute(Integer defaultValuePolicyAttribute) {
            this.defaultValuePolicyAttribute = defaultValuePolicyAttribute;
        }

        @IpsAllowedValues("policyAttribute")
        public ValueSet<Integer> getAllowedValuesForPolicyAttribute() {
            return allowedValuesForPolicyAttribute;
        }

        @IpsAllowedValuesSetter("policyAttribute")
        public void setAllowedValuesForPolicyAttribute(ValueSet<Integer> allowedValuesForPolicyAttribute) {
            this.allowedValuesForPolicyAttribute = (OrderedValueSet<Integer>)allowedValuesForPolicyAttribute;
        }

        @IpsAssociation(name = "ProductAssociation", pluralName = "ProductAssociations", kind = AssociationKind.Composition, targetClass = TargetProduct.class, min = 1, max = Integer.MAX_VALUE)
        @IpsMatchingAssociation(source = ConfiguredPolicy.class, name = "policyAssociation")
        public List<? extends TargetProduct> getProductAssociations() {
            List<TargetProduct> result = new ArrayList<>(productAssociations.size());
            for (IProductComponentLink<TargetProduct> productAssociation : productAssociations.values()) {
                if (!productAssociation.getCardinality().isEmpty()) {
                    result.add(productAssociation.getTarget());
                }
            }
            return result;
        }

        public TargetProduct getProductAssociation(int index) {
            return ProductComponentLinks.getTarget(index, productAssociations);
        }

        @IpsAssociationAdder(association = "ProductAssociation")
        public void addProductAssociation(TargetProduct target) {
            productAssociations.put(target.getId(), new ProductComponentLink<>(this, target, "ProductAssociation"));
        }

        @IpsAssociationAdder(association = "ProductAssociation", withCardinality = true)
        public void addProductAssociation(TargetProduct target, CardinalityRange cardinality) {
            productAssociations.put(target.getId(),
                    new ProductComponentLink<>(this, target, cardinality, "ProductAssociation"));
        }

        @IpsAssociationRemover(association = "ProductAssociation")
        public void removeProductAssociation(TargetProduct target) {
            productAssociations.remove(target.getId());
        }

        @IpsAssociationLinks(association = "ProductAssociation")
        public Collection<IProductComponentLink<TargetProduct>> getLinksForProductAssociations() {
            return Collections.unmodifiableCollection(productAssociations.values());
        }

        public IProductComponentLink<TargetProduct> getLinkForProductAssociation(TargetProduct productComponent) {
            return productAssociations.get(productComponent.getId());
        }

        public CardinalityRange getCardinalityForPolicyAssociation(TargetProduct productCmpt) {
            if (productCmpt != null) {
                return productAssociations.containsKey(productCmpt.getId())
                        ? productAssociations.get(productCmpt.getId())
                                .getCardinality()
                        : null;
            }
            return null;
        }

        public int getNumOfProductAssociations() {
            return productAssociations.size();
        }

        public ConfiguringProduct getConfiguringProduct() {
            return (ConfiguringProduct)getProductComponent();
        }

        public ConfiguredPolicy createConfiguredPolicy() {
            ConfiguredPolicy policy = new ConfiguredPolicy(getConfiguringProduct());
            policy.setProductCmptGeneration(this);
            policy.initialize();
            return policy;
        }

        @Override
        public ConfiguredPolicy createPolicyComponent() {
            return createConfiguredPolicy();
        }

        @Override
        public IProductComponentLink<? extends IProductComponent> getLink(String linkName, IProductComponent target) {
            if ("ProductAssociation".equals(linkName)) {
                return getLinkForProductAssociation((TargetProduct)target);
            }
            return null;
        }

        @Override
        public List<IProductComponentLink<? extends IProductComponent>> getLinks() {
            List<IProductComponentLink<? extends IProductComponent>> list = new ArrayList<>();
            list.addAll(getLinksForProductAssociations());
            return list;
        }

    }

    @IpsPolicyCmptType(name = "TargetPolicy")
    @IpsConfiguredBy(TargetProduct.class)
    @IpsDocumented(bundleName = "org.faktorips.sample.model.model-label-and-descriptions", defaultLocale = "en")
    public static class TargetPolicy extends AbstractModelObject implements IConfigurableModelObject {

        private ProductConfiguration productConfiguration;

        public TargetPolicy() {
            super();
            productConfiguration = new ProductConfiguration();
        }

        public TargetPolicy(TargetProduct productCmpt) {
            super();
            productConfiguration = new ProductConfiguration(productCmpt);
        }

        @Override
        public void initialize() {
            // nothing to do
        }

        public TargetProduct getTargetProduct() {
            return (TargetProduct)getProductComponent();
        }

        public void setTargetProduct(TargetProduct targetProduct, boolean initPropertiesWithConfiguratedDefaults) {
            setProductComponent(targetProduct);
            if (initPropertiesWithConfiguratedDefaults) {
                initialize();
            }
        }

        @Override
        public IProductComponent getProductComponent() {
            return productConfiguration.getProductComponent();
        }

        @Override
        public void setProductComponent(IProductComponent productComponent) {
            productConfiguration.setProductComponent(productComponent);
        }

        protected void resetProductCmptGenerationAfterEffectiveFromHasChanged() {
            productConfiguration.resetProductCmptGeneration();
        }

        @Override
        public Calendar getEffectiveFromAsCalendar() {
            return null;
        }

    }

    @IpsProductCmptType(name = "TargetProduct")
    @IpsConfigures(TargetPolicy.class)
    @IpsDocumented(bundleName = "org.faktorips.sample.model.model-label-and-descriptions", defaultLocale = "en")
    public static class TargetProduct extends ProductComponent {

        public TargetProduct(IRuntimeRepository repository, String id, String kindId, String versionId) {
            super(repository, id, kindId, versionId);
        }

        @Override
        public boolean isChangingOverTime() {
            return false;
        }

        public TargetPolicy createTargetPolicy() {
            TargetPolicy policy = new TargetPolicy(this);
            policy.initialize();
            return policy;
        }

        @Override
        public TargetPolicy createPolicyComponent() {
            return createTargetPolicy();
        }

    }
}
