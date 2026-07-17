/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.java;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.eclipse.emf.codegen.merge.java.GeneratedMemberMarkerInjector;
import org.eclipse.jdt.core.JavaCore;
import org.junit.Test;

/**
 * Tests for {@link GeneratedMemberMarkerInjector}.
 */
public class GeneratedMemberMarkerInjectorTest {

    private static final String START = "//@START@"; //$NON-NLS-1$
    private static final String END = "//@END@"; //$NON-NLS-1$

    private GeneratedMemberMarkerInjector injector(String start, String end) {
        return new GeneratedMemberMarkerInjector(start, end, JavaCore.getOptions());
    }

    @Test
    public void testMethod_generatedAnnotation_tagsInserted() {
        String source = """
                package p;
                class A {
                    /**
                     * @generated
                     */
                    public void foo() {}
                }
                """;
        String expected = """
                package p;
                class A {
                    //@START@
                    /**
                     * @generated
                     */
                    public void foo() {}
                    //@END@
                }
                """;

        assertThat(injector(START, END).inject(source), is(expected));
    }

    @Test
    public void testMethod_generatedNot_noTagsInserted() {
        String source = """
                package p;
                class A {
                    /**
                     * @generated NOT
                     */
                    public void foo() {}
                }
                """;

        assertThat(injector(START, END).inject(source), is(source));
    }

    @Test
    public void testMethod_generatedNot_TagsRemoved() {
        String source = """
                package p;
                class A {
                    //@START@
                    /**
                     * @generated NOT
                     */
                    public void foo() {}
                    //@END@
                }
                """;
        String expected = """
                package p;
                class A {
                    /**
                     * @generated NOT
                     */
                    public void foo() {}
                }
                """;

        assertThat(injector(START, END).inject(source), is(expected));
    }

    @Test
    public void testField_generatedNot_TagsRemoved() {
        String source = """
                package p;
                class A {
                    //@START@
                    /**
                     * @generated NOT
                     */
                    private int x = 42;
                    //@END@
                }
                """;
        String expected = """
                package p;
                class A {
                    /**
                     * @generated NOT
                     */
                    private int x = 42;
                }
                """;

        assertThat(injector(START, END).inject(source), is(expected));
    }

    @Test
    public void testEnumConstant_generatedNot_TagsRemoved() {
        String source = """
                package p;
                enum E {
                    //@START@
                    /**
                     * @generated NOT
                     */
                    FOO,
                    //@END@
                    BAR;
                }
                """;
        String expected = """
                package p;
                enum E {
                    /**
                     * @generated NOT
                     */
                    FOO,
                    BAR;
                }
                """;

        assertThat(injector(START, END).inject(source), is(expected));
    }

    @Test
    public void testMethod_generatedRedirect_TagsRemoved() {
        String source = """
                package p;
                class A {
                    //@START@
                    /**
                     * @generated REDIRECT
                     */
                    public void foo() {}
                    //@END@
                }
                """;
        String expected = """
                package p;
                class A {
                    /**
                     * @generated REDIRECT
                     */
                    public void foo() {}
                }
                """;

        assertThat(injector(START, END).inject(source), is(expected));
    }

    @Test
    public void testMixed_removesTagsFromNonGenerated_keepsTagsOnGenerated() {
        String source = """
                package p;
                class A {
                    //@START@
                    /**
                     * @generated
                     */
                    public void generated() {}
                    //@END@

                    //@START@
                    /**
                     * @generated NOT
                     */
                    public void manual() {}
                    //@END@
                }
                """;
        String expected = """
                package p;
                class A {
                    //@START@
                    /**
                     * @generated
                     */
                    public void generated() {}
                    //@END@

                    /**
                     * @generated NOT
                     */
                    public void manual() {}
                }
                """;

        assertThat(injector(START, END).inject(source), is(expected));
    }

    @Test
    public void testMethod_generatedNot_onlyStartTag_startTagRemoved() {
        String source = """
                package p;
                class A {
                    //@START@
                    /**
                     * @generated NOT
                     */
                    public void foo() {}
                }
                """;
        String expected = """
                package p;
                class A {
                    /**
                     * @generated NOT
                     */
                    public void foo() {}
                }
                """;

        assertThat(injector(START, END).inject(source), is(expected));
    }

    @Test
    public void testMethod_generatedNot_onlyEndTag_endTagRemoved() {
        String source = """
                package p;
                class A {
                    /**
                     * @generated NOT
                     */
                    public void foo() {}
                    //@END@
                }
                """;
        String expected = """
                package p;
                class A {
                    /**
                     * @generated NOT
                     */
                    public void foo() {}
                }
                """;

        assertThat(injector(START, END).inject(source), is(expected));
    }

    @Test
    public void testIdempotency_formatterAddsBlankLineAfterStartTag_notInsertedAgain() {
        String source = """
                package p;
                class A {
                    //@START@

                    /**
                     * @generated
                     */
                    public void foo() {}
                    //@END@
                }
                """;

        assertThat(injector(START, END).inject(source), is(source));
    }

    @Test
    public void testIdempotency_formatterAddsBlankLineBeforeEndTag_notInsertedAgain() {
        String source = """
                package p;
                class A {
                    //@START@
                    /**
                     * @generated
                     */
                    public void foo() {}

                    //@END@
                }
                """;

        assertThat(injector(START, END).inject(source), is(source));
    }

    @Test
    public void testMethod_generatedNot_blankLineAfterStartTag_startTagRemoved() {
        String source = """
                package p;
                class A {
                    //@START@

                    /**
                     * @generated NOT
                     */
                    public void foo() {}
                    //@END@
                }
                """;
        String expected = """
                package p;
                class A {

                    /**
                     * @generated NOT
                     */
                    public void foo() {}
                }
                """;

        assertThat(injector(START, END).inject(source), is(expected));
    }

    @Test
    public void testMethod_generatedNot_blankLineBeforeEndTag_endTagRemoved() {
        String source = """
                package p;
                class A {
                    //@START@
                    /**
                     * @generated NOT
                     */
                    public void foo() {}

                    //@END@
                }
                """;
        String expected = """
                package p;
                class A {
                    /**
                     * @generated NOT
                     */
                    public void foo() {}

                }
                """;

        assertThat(injector(START, END).inject(source), is(expected));
    }

    @Test
    public void testField_generatedNot_noTagsInserted() {
        String source = """
                package p;
                class A {
                    /**
                     * @generated NOT
                     */
                    private int x = 42;
                }
                """;

        assertThat(injector(START, END).inject(source), is(source));
    }

    @Test
    public void testEnumConstant_generatedNot_noTagsInserted() {
        String source = """
                package p;
                enum E {
                    /**
                     * @generated NOT
                     */
                    FOO,
                    BAR;
                }
                """;

        assertThat(injector(START, END).inject(source), is(source));
    }

    @Test
    public void testMixed_generatedNotMembersNotTagged_generatedMembersTagged() {
        String source = """
                package p;
                class A {
                    /**
                     * @generated
                     */
                    public void generated() {}

                    /**
                     * @generated NOT
                     */
                    public void manual() {}
                }
                """;
        String expected = """
                package p;
                class A {
                    //@START@
                    /**
                     * @generated
                     */
                    public void generated() {}
                    //@END@

                    /**
                     * @generated NOT
                     */
                    public void manual() {}
                }
                """;

        assertThat(injector(START, END).inject(source), is(expected));
    }

    @Test
    public void testMethod_noJavadoc_noTagsInserted() {
        String source = """
                package p;
                class A {
                    public void foo() {}
                }
                """;

        assertThat(injector(START, END).inject(source), is(source));
    }

    @Test
    public void testMethod_noGeneratedJavadoc_noTagsInserted() {
        String source = """
                package p;
                class A {
                    /**
                     * This is a javadoc
                     *
                     * @param bar a string
                     * @return a string
                     */
                    public String foo(String bar) {return "";}
                }
                """;

        assertThat(injector(START, END).inject(source), is(source));
    }

    @Test
    public void testMethod_noGeneratedShortJavadoc_noTagsInserted() {
        String source = """
                package p;
                class A {
                    /**
                     * {@return a string}
                     */
                    public String foo() {return "";}
                }
                """;

        assertThat(injector(START, END).inject(source), is(source));
    }

    @Test
    public void testMethod_noGeneratedBlockComment_noTagsInserted() {
        String source = """
                package p;
                class A {
                    /*
                     * A Comment
                     */
                    public void foo() {}
                }
                """;

        assertThat(injector(START, END).inject(source), is(source));
    }

    @Test
    public void testMethod_noGeneratedLineComment_noTagsInserted() {
        String source = """
                package p;
                class A {
                    // A Comment
                    public void foo() {}
                }
                """;

        assertThat(injector(START, END).inject(source), is(source));
    }

    @Test
    public void testField_generatedAnnotation_tagsInserted() {
        String source = """
                package p;
                class A {
                    /**
                     * @generated
                     */
                    private int x = 42;
                }
                """;
        String expected = """
                package p;
                class A {
                    //@START@
                    /**
                     * @generated
                     */
                    private int x = 42;
                    //@END@
                }
                """;

        assertThat(injector(START, END).inject(source), is(expected));
    }

    @Test
    public void testEnumConstants_multipleGenerated_allTaggedIndividually() {
        String source = """
                package p;
                enum E {
                    /**
                     * @generated
                     */
                    FOO,
                    /**
                     * @generated
                     */
                    BAR,
                    NOT_ME;
                }
                """;
        String expected = """
                package p;
                enum E {
                    //@START@
                    /**
                     * @generated
                     */
                    FOO,
                    //@END@
                    //@START@
                    /**
                     * @generated
                     */
                    BAR,
                    //@END@
                    NOT_ME;
                }
                """;

        assertThat(injector(START, END).inject(source), is(expected));
    }

    @Test
    public void testIdempotency_tagsAlreadyPresent_notInsertedAgain() {
        String source = """
                package p;
                class A {
                    //@START@
                    /**
                     * @generated
                     */
                    public void foo() {}
                    //@END@
                }
                """;
        assertThat(injector(START, END).inject(source), is(source));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyStartTag_throwsIllegalArgumentException() {
        injector("", END); //$NON-NLS-1$
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyEndTag_throwsIllegalArgumentException() {
        injector(START, ""); //$NON-NLS-1$
    }

    @Test
    public void testNestedClass_memberTagged() {
        String source = """
                package p;
                class A {
                    class B {
                        /**
                         * @generated
                         */
                        public void bar() {}
                    }
                }
                """;
        String expected = """
                package p;
                class A {
                    class B {
                        //@START@
                        /**
                         * @generated
                         */
                        public void bar() {}
                        //@END@
                    }
                }
                """;

        assertThat(injector(START, END).inject(source), is(expected));
    }

    @Test
    public void testMultipleMembers_onlyGeneratedTagged() {
        String source = """
                package p;
                class A {
                    /**
                     * @generated
                     */
                    public void generated() {}

                    public void manual() {}
                }
                """;
        String expected = """
                package p;
                class A {
                    //@START@
                    /**
                     * @generated
                     */
                    public void generated() {}
                    //@END@

                    public void manual() {}
                }
                """;

        assertThat(injector(START, END).inject(source), is(expected));
    }

    @Test
    public void testTagWithoutSlashes_slashesAutomaticallyPrepended() {
        String source = """
                package p;
                class A {
                    /**
                     * @generated
                     */
                    public void foo() {}
                }
                """;
        String expected = """
                package p;
                class A {
                    //@START@
                    /**
                     * @generated
                     */
                    public void foo() {}
                    //@END@
                }
                """;

        assertThat(injector("@START@", "@END@").inject(source), is(expected)); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    // GeneratedTagPropertyDef rejects tags starting with "//" via UI validation, but the injector
    // itself must handle them defensively so it does not double the slashes if called directly.
    public void testTagWithSlashesAlready_notDoubled() {
        String source = """
                package p;
                class A {
                    /**
                     * @generated
                     */
                    public void foo() {}
                }
                """;
        String expected = """
                package p;
                class A {
                    //@START@
                    /**
                     * @generated
                     */
                    public void foo() {}
                    //@END@
                }
                """;

        assertThat(injector("//@START@", "//@END@").inject(source), is(expected)); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void testIdempotency_formatterAddsSpace_notInsertedAgain() {
        // Simulates the Eclipse formatter converting //@START@ to // @START@
        String source = """
                package p;
                class A {
                    // @START@
                    /**
                     * @generated
                     */
                    public void foo() {}
                    // @END@
                }
                """;

        assertThat(injector("//@START@", "//@END@").inject(source), is(source)); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void testAdjacentGeneratedMethods_bothTagged() {
        String source = """
                package p;
                class A {
                    /**
                     * @generated
                     */
                    public void first() {}
                    /**
                     * @generated
                     */
                    public void second() {}
                }
                """;
        String expected = """
                package p;
                class A {
                    //@START@
                    /**
                     * @generated
                     */
                    public void first() {}
                    //@END@
                    //@START@
                    /**
                     * @generated
                     */
                    public void second() {}
                    //@END@
                }
                """;

        assertThat(injector(START, END).inject(source), is(expected));
    }

    @Test
    public void testNestedEnum_constantTagged() {
        String source = """
                package p;
                class A {
                    enum B {
                        /**
                         * @generated
                         */
                        FOO,
                        BAR;
                    }
                }
                """;
        String expected = """
                package p;
                class A {
                    enum B {
                        //@START@
                        /**
                         * @generated
                         */
                        FOO,
                        //@END@
                        BAR;
                    }
                }
                """;

        assertThat(injector(START, END).inject(source), is(expected));
    }

    @Test
    public void testMethod_generatedRedirect_noTagsInserted() {
        String source = """
                package p;
                class A {
                    /**
                     * @generated REDIRECT
                     */
                    public void foo() {}
                }
                """;

        assertThat(injector(START, END).inject(source), is(source));
    }

    @Test
    public void testMethod_generatedRedirectPair_onlyRedirectionMethodTagged() {
        String source = """
                package p;
                class A {
                    /**
                     * @generated REDIRECT
                     */
                    public void foo() {}

                    /**
                     * @generated
                     */
                    public void fooGeneratedRedirection() {}
                }
                """;
        String expected = """
                package p;
                class A {
                    /**
                     * @generated REDIRECT
                     */
                    public void foo() {}

                    //@START@
                    /**
                     * @generated
                     */
                    public void fooGeneratedRedirection() {}
                    //@END@
                }
                """;

        assertThat(injector(START, END).inject(source), is(expected));
    }

    @Test
    public void testMethod_generatedWithArbitraryText_noTagsInserted() {
        String source = """
                package p;
                class A {
                    /**
                     * @generated some text
                     */
                    public void foo() {}
                }
                """;

        assertThat(injector(START, END).inject(source), is(source));
    }

    @Test
    public void testMethod_generatedWithThisFieldMethod_tagsInserted() {
        String source = """
                package p;
                class A {
                    /**
                     * @generated This field/method was generated by ModelElement 'foo'
                     */
                    public void foo() {}
                }
                """;
        String expected = """
                package p;
                class A {
                    //@START@
                    /**
                     * @generated This field/method was generated by ModelElement 'foo'
                     */
                    public void foo() {}
                    //@END@
                }
                """;

        assertThat(injector(START, END).inject(source), is(expected));
    }

    // --- line-ending variants --------------------------------------------------

    private static String withLineSeparator(String source, String sep) {
        return source.replace("\n", sep); //$NON-NLS-1$
    }

    @Test
    public void testMethod_crlfLineEndings_tagsInserted() {
        String sourceLf = """
                package p;
                class A {
                    /**
                     * @generated
                     */
                    public void foo() {}
                }
                """;
        String expectedLf = """
                package p;
                class A {
                    //@START@
                    /**
                     * @generated
                     */
                    public void foo() {}
                    //@END@
                }
                """;

        String source = withLineSeparator(sourceLf, "\r\n"); //$NON-NLS-1$
        String expected = withLineSeparator(expectedLf, "\r\n"); //$NON-NLS-1$
        assertThat(injector(START, END).inject(source), is(expected));
    }

    @Test
    public void testMethod_crLineEndings_tagsInserted() {
        String sourceLf = """
                package p;
                class A {
                    /**
                     * @generated
                     */
                    public void foo() {}
                }
                """;
        String expectedLf = """
                package p;
                class A {
                    //@START@
                    /**
                     * @generated
                     */
                    public void foo() {}
                    //@END@
                }
                """;

        // CR-only: classic Mac OS line endings (pre-OS X)
        String source = withLineSeparator(sourceLf, "\r"); //$NON-NLS-1$
        String expected = withLineSeparator(expectedLf, "\r"); //$NON-NLS-1$
        assertThat(injector(START, END).inject(source), is(expected));
    }

    @Test
    public void testIdempotency_crlf_notInsertedAgain() {
        String sourceLf = """
                package p;
                class A {
                    //@START@
                    /**
                     * @generated
                     */
                    public void foo() {}
                    //@END@
                }
                """;

        String source = withLineSeparator(sourceLf, "\r\n"); //$NON-NLS-1$
        assertThat(injector(START, END).inject(source), is(source));
    }

    @Test
    public void testEnumConstant_separatorOnNextLine_endTagAfterSeparator() {
        String source = """
                package p;
                enum E {
                    /**
                     * @generated
                     */
                    FOO
                    ,
                    BAR;
                }
                """;
        String expected = """
                package p;
                enum E {
                    //@START@
                    /**
                     * @generated
                     */
                    FOO
                    ,
                    //@END@
                    BAR;
                }
                """;

        assertThat(injector(START, END).inject(source), is(expected));
    }

    // --- @\s*generated variants (mirrors merge.java5.xml which uses @\s*generated) ---------------

    @Test
    public void testMethod_generatedWithSpaceBetweenAtAndKeyword_tagsInserted() {
        // merge.java5.xml matches "@ generated" the same as "@generated" — the injector mirrors
        // this
        String source = """
                package p;
                class A {
                    /**
                     * @ generated
                     */
                    public void foo() {}
                }
                """;
        String expected = """
                package p;
                class A {
                    //@START@
                    /**
                     * @ generated
                     */
                    public void foo() {}
                    //@END@
                }
                """;

        assertThat(injector(START, END).inject(source), is(expected));
    }

    @Test
    public void testMethod_generatedWithMultipleSpaces_tagsInserted() {
        String source = """
                package p;
                class A {
                    /**
                     * @  generated
                     */
                    public void foo() {}
                }
                """;
        String expected = """
                package p;
                class A {
                    //@START@
                    /**
                     * @  generated
                     */
                    public void foo() {}
                    //@END@
                }
                """;

        assertThat(injector(START, END).inject(source), is(expected));
    }

    @Test
    public void testMethod_generatedWithSpaceAndThisFieldMethod_tagsInserted() {
        String source = """
                package p;
                class A {
                    /**
                     * @ generated This field/method was generated by ModelElement 'foo'
                     */
                    public void foo() {}
                }
                """;
        String expected = """
                package p;
                class A {
                    //@START@
                    /**
                     * @ generated This field/method was generated by ModelElement 'foo'
                     */
                    public void foo() {}
                    //@END@
                }
                """;

        assertThat(injector(START, END).inject(source), is(expected));
    }

    @Test
    public void testMethod_generatedWithSpaceAndNot_noTagsInserted() {
        // "@ generated NOT" must not match — only plain @generated or @generated This
        // field/method...
        String source = """
                package p;
                class A {
                    /**
                     * @ generated NOT
                     */
                    public void foo() {}
                }
                """;

        assertThat(injector(START, END).inject(source), is(source));
    }

    @Test
    public void testMethod_generatedWithTabBetweenAtAndKeyword_tagsInserted() {
        // \s also matches \t
        String source = "package p;\nclass A {\n    /**\n     * @\tgenerated\n     */\n    public void foo() {}\n}\n";
        String expected = "package p;\nclass A {\n    //@START@\n    /**\n     * @\tgenerated\n     */\n    public void foo() {}\n    //@END@\n}\n";

        assertThat(injector(START, END).inject(source), is(expected));
    }

    @Test
    public void testMethod_generatedWithFormFeedBetweenAtAndKeyword_tagsInserted() {
        // \s also matches \f (form feed, U+000C)
        String source = "package p;\nclass A {\n    /**\n     * @\fgenerated\n     */\n    public void foo() {}\n}\n";
        String expected = "package p;\nclass A {\n    //@START@\n    /**\n     * @\fgenerated\n     */\n    public void foo() {}\n    //@END@\n}\n";

        assertThat(injector(START, END).inject(source), is(expected));
    }

    @Test
    public void testMethod_generatedWithVerticalTabBetweenAtAndKeyword_tagsInserted() {
        // \s also matches \u000B (vertical tab)
        String source = "package p;\nclass A {\n    /**\n     * @\u000Bgenerated\n     */\n    public void foo() {}\n}\n";
        String expected = "package p;\nclass A {\n    //@START@\n    /**\n     * @\u000Bgenerated\n     */\n    public void foo() {}\n    //@END@\n}\n";

        assertThat(injector(START, END).inject(source), is(expected));
    }
}
