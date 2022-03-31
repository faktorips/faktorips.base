/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.type;

import static org.faktorips.abstracttest.matcher.Matchers.allOf;
import static org.faktorips.testsupport.IpsMatchers.containsText;
import static org.faktorips.testsupport.IpsMatchers.hasInvalidObject;
import static org.faktorips.testsupport.IpsMatchers.hasMessageCode;
import static org.faktorips.testsupport.IpsMatchers.hasMessages;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.internal.enums.EnumContent;
import org.faktorips.devtools.model.internal.enums.EnumType;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptType;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmpt;
import org.faktorips.devtools.model.internal.tablecontents.TableContents;
import org.faktorips.devtools.model.internal.tablestructure.TableStructure;
import org.faktorips.devtools.model.internal.testcase.TestCase;
import org.faktorips.devtools.model.internal.testcasetype.TestCaseType;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.type.IType;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;

public class TypeValidationsTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private PolicyCmptType policyCmptType;
    private IProductCmptType productCmptType;
    private ProductCmpt productCmpt;
    private EnumType enumType;
    private EnumContent enumContent;
    private TableStructure tableStructure;
    private TableContents tableContents;
    private TestCaseType testCaseType;
    private TestCase testCase;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject("p");
        policyCmptType = newPolicyAndProductCmptType(ipsProject, "foo", "foo");
        productCmptType = policyCmptType.findProductCmptType(ipsProject);
        productCmpt = newProductCmpt(productCmptType, "foo");
        enumType = newEnumType(ipsProject, "foo");
        enumContent = newEnumContent(enumType, "foo");
        tableStructure = newTableStructure(ipsProject, "foo");
        tableContents = newTableContents(tableStructure, "foo");
        testCaseType = newTestCaseType(ipsProject, "foo");
        testCase = newTestCase(ipsProject, "foo");
    }

    @Test
    public void testValidateUniqueQualifiedName() {
        assertThat(policyCmptType,
                conflictsWith(/* policyCmptType, */ productCmptType, productCmpt, enumType, enumContent, tableStructure,
                        tableContents, testCaseType, testCase));
        assertThat(productCmptType,
                conflictsWith(policyCmptType, /* productCmptType, */ productCmpt, enumType, enumContent, tableStructure,
                        tableContents, testCaseType, testCase));
        assertThat(productCmpt,
                conflictsWith(policyCmptType, productCmptType, /* productCmpt, */ enumType, enumContent, tableStructure,
                        tableContents, testCaseType, testCase));
        assertThat(enumType,
                conflictsWith(policyCmptType, productCmptType, productCmpt, /*
                                                                             * enumType,
                                                                             * enumContent,
                                                                             */ tableStructure,
                        tableContents, testCaseType, testCase));
        assertThat(enumContent,
                conflictsWith(policyCmptType, productCmptType, productCmpt, /*
                                                                             * enumType,
                                                                             * enumContent,
                                                                             */ tableStructure,
                        tableContents, testCaseType, testCase));
        assertThat(tableStructure,
                conflictsWith(policyCmptType, productCmptType, productCmpt, enumType, enumContent,
                        /*
                         * tableStructure, tableContents,
                         */ testCaseType, testCase));
        assertThat(tableContents,
                conflictsWith(policyCmptType, productCmptType, productCmpt, enumType, enumContent,
                        /*
                         * tableStructure, tableContents,
                         */ testCaseType, testCase));
        assertThat(testCaseType,
                conflictsWith(policyCmptType, productCmptType, productCmpt, enumType, enumContent, tableStructure,
                        tableContents/* , testCaseType, testCase */));
        assertThat(testCase,
                conflictsWith(policyCmptType, productCmptType, productCmpt, enumType, enumContent, tableStructure,
                        tableContents/* , testCaseType , testCase */));
    }

    @Test
    public void testValidateUniqueQualifiedName_DependantProject() {
        IIpsProject dependantIpsProject = newIpsProject("p2");
        IIpsObjectPath ipsObjectPath = dependantIpsProject.getIpsObjectPath();
        ipsObjectPath.newIpsProjectRefEntry(ipsProject);
        dependantIpsProject.setIpsObjectPath(ipsObjectPath);

        PolicyCmptType policyCmptType2 = newPolicyCmptType(dependantIpsProject, "foo");

        MessageList messages = TypeValidations.validateUniqueQualifiedName(policyCmptType2);

        assertThat(messages, hasMessageCode(IType.MSGCODE_OTHER_TYPE_WITH_SAME_NAME_IN_DEPENDENT_PROJECT_EXISTS));
        assertThat(messages, not(hasMessageCode(IType.MSGCODE_OTHER_TYPE_WITH_SAME_NAME_EXISTS)));

    }

    private Matcher<IIpsObject> conflictsWith(IIpsObject... conflictingIpsObjects) {
        return new TypeSafeMatcher<>() {

            @Override
            public void describeTo(Description description) {
                description.appendText(
                        "TypeValidations.validateUniqueQualifiedName should return messages for conflicts with ");
                description.appendText(Arrays.stream(conflictingIpsObjects).map(IIpsObject::getIpsObjectType)
                        .map(IpsObjectType::toString).collect(Collectors.joining(", ")));
            }

            @Override
            protected boolean matchesSafely(IIpsObject ipsObject) {
                @SuppressWarnings("unchecked")
                Matcher<Message>[] matchers = Arrays.stream(conflictingIpsObjects)
                        .map(t -> allOf(hasInvalidObject(ipsObject),
                                containsText(t.getIpsObjectType().getDisplayName())))
                        .toArray(Matcher[]::new);
                try {
                    return hasMessages(matchers).matches(TypeValidations.validateUniqueQualifiedName(ipsObject)
                            .getMessagesByCode(IType.MSGCODE_OTHER_TYPE_WITH_SAME_NAME_EXISTS));
                } catch (IpsException e) {
                    return false;
                }
            }

            @Override
            protected void describeMismatchSafely(IIpsObject ipsObject, Description mismatchDescription) {
                mismatchDescription.appendValue(TypeValidations.validateUniqueQualifiedName(ipsObject)
                        .getMessagesByCode(IType.MSGCODE_OTHER_TYPE_WITH_SAME_NAME_EXISTS));
            }
        };
    }

}
