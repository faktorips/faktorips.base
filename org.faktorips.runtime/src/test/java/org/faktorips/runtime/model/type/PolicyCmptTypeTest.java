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

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.Severity;
import org.faktorips.runtime.internal.AbstractModelObject;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.runtime.internal.ProductComponent;
import org.faktorips.runtime.model.IpsModel;
import org.faktorips.runtime.model.annotation.IpsAssociation;
import org.faktorips.runtime.model.annotation.IpsAssociations;
import org.faktorips.runtime.model.annotation.IpsAttribute;
import org.faktorips.runtime.model.annotation.IpsAttributeSetter;
import org.faktorips.runtime.model.annotation.IpsAttributes;
import org.faktorips.runtime.model.annotation.IpsConfiguredBy;
import org.faktorips.runtime.model.annotation.IpsConfiguredValidationRule;
import org.faktorips.runtime.model.annotation.IpsPolicyCmptType;
import org.faktorips.runtime.model.annotation.IpsProductCmptType;
import org.faktorips.runtime.model.annotation.IpsValidatedBy;
import org.faktorips.runtime.model.annotation.IpsValidationRule;
import org.faktorips.runtime.model.annotation.IpsValidationRules;
import org.junit.Test;

public class PolicyCmptTypeTest {

    private final PolicyCmptType policyCmptType = IpsModel.getPolicyCmptType(Policy.class);
    private final PolicyCmptType superPolicyCmptType = IpsModel.getPolicyCmptType(SuperPolicy.class);

    @Test
    public void testGetName() throws Exception {
        assertThat(policyCmptType.getName(), is("MyPolicy"));
        assertThat(superPolicyCmptType.getName(), is("MySuperPolicy"));
    }

    @Test
    public void testGetSuperType() throws Exception {
        assertThat(policyCmptType.getSuperType().getName(), is(superPolicyCmptType.getName()));
        assertThat(superPolicyCmptType.getSuperType(), is(nullValue()));
    }

    @Test
    public void testFindSuperType() throws Exception {
        assertThat(policyCmptType.findSuperType().map(Type::getName).get(), is(superPolicyCmptType.getName()));
        assertThat(superPolicyCmptType.findSuperType().isPresent(), is(false));
    }

    @Test
    public void testIsConfiguredByPolicyCmptType() throws Exception {
        assertThat(policyCmptType.isConfiguredByProductCmptType(), is(true));
        assertThat(superPolicyCmptType.isConfiguredByProductCmptType(), is(false));
    }

    @Test
    public void testGetProductCmptType() throws Exception {
        assertThat(policyCmptType.getProductCmptType().getName(), is("MyProduct"));
    }

    @Test(expected = NullPointerException.class)
    public void testGetProductCmptType_NPE_NotConfigured() throws Exception {
        assertThat(superPolicyCmptType.getProductCmptType().getName(), is(nullValue()));
    }

    @Test
    public void testGetDeclaredAttributes() {
        assertThat(policyCmptType.getDeclaredAttributes().size(), is(4));
        assertThat(policyCmptType.getDeclaredAttributes().get(2).getName(), is("const"));
    }

    @Test
    public void testGetDeclaredAttribute() {
        assertThat(policyCmptType.getDeclaredAttribute("attr"), is(notNullValue()));
        assertThat(policyCmptType.getDeclaredAttribute("CapitalAttr").getName(), is("CapitalAttr"));
        assertThat(policyCmptType.getDeclaredAttribute("capitalAttr").getName(), is("CapitalAttr"));
    }

    @Test
    public void testGetAttributes() {
        assertThat(policyCmptType.getAttributes().size(), is(5));
        assertThat(policyCmptType.getAttributes().get(0).getName(), is("attr"));
    }

    @Test
    public void testGetAttribute() {
        assertThat(policyCmptType.getAttribute("supAttr"), is(notNullValue()));
        assertThat(policyCmptType.getAttribute("capitalAttr").getName(), is("CapitalAttr"));
        assertThat(policyCmptType.getAttribute("CapitalAttr").getName(), is("CapitalAttr"));
    }

    @Test
    public void testGetDeclaredAssociations() {
        assertThat(policyCmptType.getDeclaredAssociations().size(), is(3));
        assertThat(policyCmptType.getDeclaredAssociations().get(0).getName(), is("asso"));
        assertNotNull(policyCmptType.getDeclaredAssociation("asso"));
        assertThat(policyCmptType.getDeclaredAssociations().get(1).getName(), is("Asso2"));
        assertNotNull(policyCmptType.getDeclaredAssociation("asso2"));
        assertThat(policyCmptType.getDeclaredAssociations().get(2).getName(), is("overwrittenAsso"));
        assertNotNull(policyCmptType.getDeclaredAssociation("overwrittenAsso"));
    }

    @Test
    public void testGetDeclaredAssociation() {
        PolicyAssociation association = policyCmptType.getDeclaredAssociation("asso");
        PolicyAssociation association2 = policyCmptType.getDeclaredAssociation("Asso2");
        PolicyAssociation association2lowerCase = policyCmptType.getDeclaredAssociation("asso2");
        PolicyAssociation superAssoInSuper = superPolicyCmptType.getDeclaredAssociation("supAsso");

        assertThat(association.getName(), is("asso"));
        assertThat(association.getNamePlural(), is(IpsStringUtils.EMPTY));
        assertThat(association2.getName(), is("Asso2"));
        assertThat(association2.getNamePlural(), is("Asso2s"));
        assertThat(association2, is(association2lowerCase));
        assertThat(superAssoInSuper.getName(), is("supAsso"));
        assertThat(superAssoInSuper.getNamePlural(), is("supAssos"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetDeclaredAssociation_thowsException() {
        policyCmptType.getDeclaredAssociation("supAsso");
    }

    @Test
    public void testGetDeclaredAssociation_Plural() {
        PolicyAssociation superAssoInSuper = superPolicyCmptType.getDeclaredAssociation("supAssos");

        assertThat(superAssoInSuper.getName(), is("supAsso"));
        assertThat(superAssoInSuper.getNamePlural(), is("supAssos"));
    }

    @Test
    public void testGetAssociation() {
        PolicyAssociation association = policyCmptType.getAssociation("asso");
        PolicyAssociation association2 = policyCmptType.getAssociation("asso2");
        PolicyAssociation superAsso = policyCmptType.getAssociation("supAsso");
        PolicyAssociation superAssoInSuper = superPolicyCmptType.getAssociation("supAsso");

        assertThat(association.getName(), is("asso"));
        assertThat(association.getNamePlural(), is(IpsStringUtils.EMPTY));
        assertThat(association2.getName(), is("Asso2"));
        assertThat(association2.getNamePlural(), is("Asso2s"));
        assertThat(superAsso.getName(), is("supAsso"));
        assertThat(superAsso.getNamePlural(), is("supAssos"));
        assertThat(superAssoInSuper.getName(), is("supAsso"));
        assertThat(superAssoInSuper.getNamePlural(), is("supAssos"));
    }

    @Test
    public void testGetAssociation_Plural() {
        PolicyAssociation superAsso = policyCmptType.getAssociation("supAssos");
        PolicyAssociation superAssoInSuper = superPolicyCmptType.getAssociation("supAssos");
        PolicyAssociation association2 = policyCmptType.getAssociation("asso2s");

        assertThat(superAsso.getName(), is("supAsso"));
        assertThat(superAsso.getNamePlural(), is("supAssos"));
        assertThat(superAssoInSuper.getName(), is("supAsso"));
        assertThat(superAssoInSuper.getNamePlural(), is("supAssos"));
        assertThat(association2.getName(), is("Asso2"));
        assertThat(association2.getNamePlural(), is("Asso2s"));
    }

    @Test
    public void testGetAssociations() {
        assertThat(policyCmptType.getAssociations().size(), is(4));
        assertThat(policyCmptType.getAssociations().get(0).getName(), is("asso"));
        assertThat(policyCmptType.getAssociations().get(1).getName(), is("Asso2"));
        assertThat(policyCmptType.getAssociations().get(2).getName(), is("overwrittenAsso"));
        assertThat(policyCmptType.getAssociations().get(3).getName(), is("supAsso"));
        assertNotNull(policyCmptType.getAssociation("supAsso"));
    }

    @Test
    public void testIsAttributePresent() {
        assertThat(policyCmptType.isAttributePresent("supAttr"), is(true));
        assertThat(policyCmptType.isAttributeDeclared("supAttr"), is(false));

        assertThat(policyCmptType.isAttributePresent("overwrittenAttr"), is(true));
        assertThat(policyCmptType.isAttributeDeclared("overwrittenAttr"), is(true));

        assertThat(policyCmptType.isAttributePresent("CapitalAttr"), is(true));
        assertThat(policyCmptType.isAttributeDeclared("CapitalAttr"), is(true));
    }

    @Test
    public void testIsAssociationPresent() {
        assertThat(policyCmptType.isAssociationPresent("supAsso"), is(true));
        assertThat(policyCmptType.isAssociationDeclared("supAsso"), is(false));

        assertThat(policyCmptType.isAssociationPresent("overwrittenAsso"), is(true));
        assertThat(policyCmptType.isAssociationDeclared("overwrittenAsso"), is(true));

        assertThat(policyCmptType.isAssociationPresent("Asso2"), is(true));
        assertThat(policyCmptType.isAssociationDeclared("Asso2"), is(true));
    }

    @Test
    public void testGetDeclaredValidationRules() {
        List<ValidationRule> declaredValidationRules = policyCmptType.getDeclaredValidationRules();

        assertThat(declaredValidationRules.size(), is(2));
        assertThat(declaredValidationRules.get(0).getName(), is("someRule"));
        assertThat(declaredValidationRules.get(1).getName(), is("anotherRule"));
        assertThat(getAllNames(declaredValidationRules), not(hasItem("superRule")));
    }

    @Test
    public void testGetDeclaredValidationRules_SeparateValidatorClass() {
        PolicyCmptType type = IpsModel.getPolicyCmptType(SeparatelyValidatedPolicy.class);

        List<ValidationRule> declaredValidationRules = type.getDeclaredValidationRules();

        assertThat(declaredValidationRules.size(), is(2));
        assertThat(declaredValidationRules.get(0).getName(), is("rule"));
        assertThat(declaredValidationRules.get(1).getName(), is("configuredRule"));
    }

    @Test
    public void testGetDeclaredValidationRule_SeparateValidatorClass() {
        PolicyCmptType type = IpsModel.getPolicyCmptType(SeparatelyValidatedPolicy.class);

        ValidationRule rule = type.getDeclaredValidationRule("rule");

        assertThat(rule.getName(), is("rule"));
    }

    @Test
    public void testGetDeclaredValidationRule_SeparateValidatorClass_ConfiguredRule() {
        PolicyCmptType type = IpsModel.getPolicyCmptType(SeparatelyValidatedPolicy.class);

        ValidationRule rule = type.getDeclaredValidationRule("configuredRule");

        assertThat(rule.getName(), is("configuredRule"));
        assertThat(rule.isChangingOverTime(), is(true));
    }

    @Test
    public void testGetValidationRules_SeparateValidatorClass() {
        PolicyCmptType type = IpsModel.getPolicyCmptType(SeparatelyValidatedPolicy.class);

        List<ValidationRule> rules = type.getValidationRules();

        assertThat(rules.size(), is(3));
        assertThat(rules.get(0).getName(), is("rule"));
        assertThat(rules.get(1).getName(), is("configuredRule"));
        assertThat(rules.get(2).getName(), is("superRule"));
    }

    private List<String> getAllNames(List<ValidationRule> declaredValidationRules) {
        ArrayList<String> names = new ArrayList<>();
        for (ValidationRule rule : declaredValidationRules) {
            names.add(rule.getName());
        }
        return names;
    }

    @Test
    public void testGetDeclaredValidationRule() {
        ValidationRule declaredValidationRule = policyCmptType.getDeclaredValidationRule("someRule");
        assertThat(declaredValidationRule, is(notNullValue()));
        assertThat(declaredValidationRule.getMsgCode(), is(Policy.MSG_CODE_RULE));
        assertThat(declaredValidationRule.getSeverity(), is(Severity.ERROR));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetDeclaredValidationRule_RuleFromSuperClassNotPresent() {
        policyCmptType.getDeclaredValidationRule("superRule");
    }

    @Test
    public void testGetValidationRules() {
        List<ValidationRule> validationRules = policyCmptType.getValidationRules();
        assertThat(validationRules.size(), is(3));
        assertThat(validationRules.get(0).getName(), is("someRule"));
        assertThat(validationRules.get(1).getName(), is("anotherRule"));
        assertThat(validationRules.get(2).getName(), is("superRule"));
    }

    @Test
    public void testGetValidationRule() {
        ValidationRule superValidationRule = policyCmptType.getValidationRule("superRule");
        assertThat(superValidationRule, is(notNullValue()));
        assertThat(superValidationRule.getName(), is("superRule"));
        assertThat(superValidationRule.getMsgCode(), is(SuperPolicy.MSG_CODE_SUPER_RULE));

        ValidationRule validationRule = policyCmptType.getValidationRule("anotherRule");
        assertThat(validationRule, is(notNullValue()));
        assertThat(validationRule.getName(), is("anotherRule"));
        assertThat(validationRule.getMsgCode(), is(Policy.MSG_CODE_RULE));
    }

    @Test
    public void testIsActivatedByDefault() {
        assertThat(policyCmptType.getValidationRule("someRule").isActivatedByDefault(), is(true));
        assertThat(policyCmptType.getValidationRule("anotherRule").isActivatedByDefault(), is(false));
        assertThat(policyCmptType.getValidationRule("superRule").isActivatedByDefault(), is(false));
    }

    @Test
    public void testIsChangingOverTime() {
        assertThat(policyCmptType.getValidationRule("someRule").isChangingOverTime(), is(true));
        assertThat(policyCmptType.getValidationRule("anotherRule").isChangingOverTime(), is(false));
        assertThat(policyCmptType.getValidationRule("superRule").isChangingOverTime(), is(false));
    }

    @Test
    public void testIsProductRelevant() {
        assertThat(policyCmptType.getValidationRule("someRule").isProductRelevant(), is(true));
        assertThat(policyCmptType.getValidationRule("anotherRule").isProductRelevant(), is(true));
        assertThat(policyCmptType.getValidationRule("superRule").isProductRelevant(), is(false));
    }

    @Test
    public void testToString() {
        assertThat(policyCmptType.getValidationRule("someRule").toString(), is(
                "@IpsValidationRule( name = 'someRule', msgCode = 'dummy message code', severity = 'ERROR')\n@IpsConfiguredValidationRule(changeOverTime = true, defaultActivated = true)"));
        assertThat(policyCmptType.getValidationRule("superRule").toString(),
                is("@IpsValidationRule( name = 'superRule', msgCode = 'message code', severity = 'ERROR')"));
    }

    @IpsPolicyCmptType(name = "MyPolicy")
    @IpsConfiguredBy(Product.class)
    @IpsAttributes({ "attr", "overwrittenAttr", "const", "CapitalAttr" })
    @IpsAssociations({ "asso", "Asso2", "overwrittenAsso" })
    @IpsValidationRules({ "someRule", "anotherRule" })
    private static abstract class Policy extends SuperPolicy {
        public static final String MSG_CODE_RULE = "dummy message code";

        @IpsAttribute(name = "const", kind = AttributeKind.CONSTANT, valueSetKind = ValueSetKind.AllValues)
        public static final int CONST = 2;

        @IpsAttribute(name = "attr", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.Range)
        public abstract String getAttr();

        @IpsAttributeSetter("attr")
        public abstract void setAttr(String attr);

        @Override
        @IpsAttribute(name = "overwrittenAttr", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.Range)
        public abstract String getOverwrittenAttr();

        @IpsAttribute(name = "CapitalAttr", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.Enum)
        public abstract String getCapitalAttr();

        @IpsAssociation(name = "asso", max = 0, min = 0, targetClass = Policy.class, kind = AssociationKind.Composition)
        public abstract Policy getAsso();

        @IpsAssociation(name = "Asso2", pluralName = "Asso2s", max = 5, min = 0, targetClass = Policy.class, kind = AssociationKind.Composition)
        public abstract List<Policy> getAsso2();

        @Override
        @IpsAssociation(name = "overwrittenAsso", pluralName = "overwrittenAssos", max = 0, min = 0, targetClass = SuperPolicy.class, kind = AssociationKind.Composition)
        public abstract SuperPolicy getOverwrittenAsso();

        @IpsValidationRule(name = "someRule", msgCode = MSG_CODE_RULE, severity = Severity.ERROR)
        @IpsConfiguredValidationRule(changingOverTime = true, defaultActivated = true)
        public abstract boolean someRule(MessageList ml, IValidationContext context);

        @IpsValidationRule(name = "anotherRule", msgCode = MSG_CODE_RULE, severity = Severity.ERROR)
        @IpsConfiguredValidationRule(changingOverTime = false, defaultActivated = false)
        public abstract boolean anotherRule(MessageList ml, IValidationContext context);
    }

    @IpsPolicyCmptType(name = "MySuperPolicy")
    @IpsAttributes({ "supAttr", "overwrittenAttr" })
    @IpsAssociations({ "supAsso", "overwrittenAsso" })
    @IpsValidationRules({ "superRule" })
    private static abstract class SuperPolicy extends AbstractModelObject {
        public static final String MSG_CODE_SUPER_RULE = "message code";

        @IpsAttribute(name = "supAttr", kind = AttributeKind.CONSTANT, valueSetKind = ValueSetKind.AllValues)
        public static final int supAttr = 5;

        @IpsAttribute(name = "overwrittenAttr", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.Range)
        public abstract String getOverwrittenAttr();

        @IpsAssociation(name = "supAsso", pluralName = "supAssos", max = 0, min = 0, targetClass = SuperPolicy.class, kind = AssociationKind.Composition)
        public abstract SuperPolicy getSupAsso();

        @IpsAssociation(name = "overwrittenAsso", pluralName = "overwrittenAssos", max = 0, min = 0, targetClass = SuperPolicy.class, kind = AssociationKind.Composition)
        public abstract SuperPolicy getOverwrittenAsso();

        @IpsValidationRule(name = "superRule", msgCode = MSG_CODE_SUPER_RULE, severity = Severity.ERROR)
        public abstract boolean superRule(MessageList ml, IValidationContext context);
    }

    @IpsProductCmptType(name = "MyProduct")
    private static abstract class Product extends ProductComponent {

        public Product(IRuntimeRepository repository, String id, String productKindId, String versionId) {
            super(repository, id, productKindId, versionId);
        }

    }

    @IpsPolicyCmptType(name = "SeparatelyValidatedSuperPolicy")
    @IpsValidatedBy(SeparatelyValidatedSuperPolicyValidator.class)
    private static class SeparatelyValidatedSuperPolicy extends AbstractModelObject {

    }

    @IpsValidationRules({ "superRule" })
    private static class SeparatelyValidatedSuperPolicyValidator {

        public static final String MSG_CODE_SUPER_RULE = "message code";

        @SuppressWarnings("unused")
        @IpsValidationRule(name = "superRule", msgCode = MSG_CODE_SUPER_RULE, severity = Severity.ERROR)
        protected boolean superRule(MessageList ml, IValidationContext context) {
            return false;
        }

    }

    @IpsPolicyCmptType(name = "SeparatelyValidatedPolicy")
    @IpsValidatedBy(SeparatelyValidatedPolicyValidator.class)
    private static class SeparatelyValidatedPolicy extends SeparatelyValidatedSuperPolicy {

    }

    @IpsValidationRules({ "rule", "configuredRule" })
    private static class SeparatelyValidatedPolicyValidator extends SeparatelyValidatedSuperPolicyValidator {

        public static final String MSG_CODE_RULE = "message code";

        @SuppressWarnings("unused")
        @IpsValidationRule(name = "rule", msgCode = MSG_CODE_RULE, severity = Severity.ERROR)
        protected boolean rule(MessageList ml, IValidationContext context) {
            return false;
        }

        @SuppressWarnings("unused")
        @IpsValidationRule(name = "configuredRule", msgCode = MSG_CODE_RULE, severity = Severity.ERROR)
        @IpsConfiguredValidationRule(changingOverTime = true, defaultActivated = false)
        protected boolean configuredRule(MessageList ml, IValidationContext context) {
            return false;
        }

    }

}