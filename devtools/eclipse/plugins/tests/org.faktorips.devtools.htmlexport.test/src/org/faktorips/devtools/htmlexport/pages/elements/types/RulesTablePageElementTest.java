/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.elements.types;

import java.util.List;

import org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement;
import org.faktorips.devtools.htmlexport.pages.standard.AbstractXmlUnitHtmlExportTest;
import org.faktorips.devtools.htmlexport.pages.standard.ContentPageUtil;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.devtools.model.pctype.MessageSeverity;
import org.faktorips.values.LocalizedString;
import org.junit.Before;
import org.junit.Test;

public class RulesTablePageElementTest extends AbstractXmlUnitHtmlExportTest {

    private IPolicyCmptType policy;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        policy = newPolicyCmptType(ipsProject, "Vertrag");
    }

    private void assertXPathFromTable(IPageElement objectContentPage, String subXPath) throws Exception {
        assertXPathExists(objectContentPage, getXPathMethodTable() + subXPath);
    }

    private String getXPathMethodTable() {
        return "//table[@id= '" + policy.getName() + "_validationrules" + "']";
    }

    @Test
    public void testMethodsTableVorhanden() throws Exception {

        IValidationRule methodString = createRuleWithAttributes();
        IValidationRule methodInteger = createRuleWithoutAttributes();

        IPageElement objectContentPage = ContentPageUtil
                .createObjectContentPageElement(policy.getIpsSrcFile(), context);

        assertXPathExists(objectContentPage, getXPathMethodTable());

        assertXPathFromTable(objectContentPage, "[count(.//tr)=3]");

        assertXPathFromTable(objectContentPage, "//tr[2][td='" + methodString.getName() + "']");

        assertXPathFromTable(objectContentPage, "//tr[3][td='" + methodInteger.getName() + "']");
    }

    @Test
    public void testMethodsTableNichtVorhandenOhneAttribute() throws Exception {

        IPageElement objectContentPage = ContentPageUtil
                .createObjectContentPageElement(policy.getIpsSrcFile(), context);

        assertXPathNotExists(objectContentPage, getXPathMethodTable());
    }

    @Test
    public void testMethodsTableAufbau() throws Exception {
        createRuleWithoutAttributes();
        createRuleWithAttributes();
        IPageElement objectContentPage = ContentPageUtil
                .createObjectContentPageElement(policy.getIpsSrcFile(), context);

        int row = 2;

        List<IValidationRule> rules = policy.getValidationRules();
        for (IValidationRule rule : rules) {
            assertXPathFromTable(objectContentPage, "//tr[" + row + "][td='" + rule.getName() + "']");
            assertXPathFromTable(objectContentPage, "//tr[" + row + "][td='" + rule.getMessageCode() + "']");
            assertXPathFromTable(objectContentPage, "//tr[" + row + "][td='" + rule.getMessageSeverity().getId()
                    + "']");
            assertXPathFromTable(objectContentPage,
                    "//tr[" + row + "][td='" + rule.getMessageText().get(context.getDocumentationLocale()).getValue()
                            + "']");

            String[] validatedAttributes = rule.getValidatedAttributes();
            for (String validatedAttribute : validatedAttributes) {
                assertXPathFromTable(objectContentPage, "//tr[" + row + "]/td[contains(., '" + validatedAttribute
                        + "')]");
            }

            row++;
        }
    }

    private IValidationRule createRuleWithoutAttributes() {
        IValidationRule rule = policy.newRule();
        rule.setName("RuleWithoutAttributes");
        rule.setMessageCode("CODE_WITHOUT_ATTRIBUTES");
        rule.setMessageSeverity(MessageSeverity.ERROR);
        String msgTxt = "blubber";
        rule.getMessageText().add(new LocalizedString(context.getDocumentationLocale(), msgTxt));
        rule.addValidatedAttribute("Attribut_1");
        rule.addValidatedAttribute("Attribut_2");
        return rule;
    }

    private IValidationRule createRuleWithAttributes() {
        IValidationRule rule = policy.newRule();
        rule.setName("RuleWithAttributes");
        rule.setMessageCode("CODE_WITH_ATTRIBUTES");
        rule.setMessageSeverity(MessageSeverity.WARNING);
        String msgTxt = "blabla";
        rule.getMessageText().add(new LocalizedString(context.getDocumentationLocale(), msgTxt));
        return rule;
    }
}
