/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.editors.productcmpt;

import static org.faktorips.devtools.core.ui.editors.productcmpt.AttributeRelevanceTest.ValueSetMatchers.contains;
import static org.faktorips.devtools.core.ui.editors.productcmpt.AttributeRelevanceTest.ValueSetMatchers.containsNull;
import static org.faktorips.devtools.core.ui.editors.productcmpt.AttributeRelevanceTest.ValueSetMatchers.empty;
import static org.faktorips.devtools.core.ui.editors.productcmpt.AttributeRelevanceTest.ValueSetMatchers.isRange;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.util.Arrays;

import com.google.common.base.Objects;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmpt;
import org.faktorips.devtools.core.internal.model.valueset.EnumValueSet;
import org.faktorips.devtools.core.internal.model.valueset.RangeValueSet;
import org.faktorips.devtools.core.internal.model.valueset.UnrestrictedValueSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IConfiguredValueSet;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IRangeValueSet;
import org.faktorips.devtools.core.model.valueset.IUnrestrictedValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;

public class AttributeRelevanceTest extends AbstractIpsPluginTest {

    private IConfiguredValueSet configuredValueSet;
    private IPolicyCmptTypeAttribute attribute;

    @Override
    @Before
    public void setUp() throws CoreException {
        IIpsProject ipsProject = newIpsProject();
        PolicyCmptType policyCmptType = newPolicyAndProductCmptType(ipsProject, "Pol", "ProdType");
        attribute = policyCmptType.newPolicyCmptTypeAttribute("attr");
        attribute.setDatatype(Datatype.INTEGER.getQualifiedName());
        attribute.setRelevanceConfiguredByProduct(true);
        ProductCmpt productCmpt = newProductCmpt(ipsProject.findProductCmptType("ProdType"), "Prod");
        configuredValueSet = productCmpt.newPropertyValue(attribute, IConfiguredValueSet.class);
    }

    @Test
    public void testOf_UnrestrictedWithoutNull() {
        assertThat(AttributeRelevance.of(new UnrestrictedValueSet(configuredValueSet, "1", false)),
                is(AttributeRelevance.Mandatory));
    }

    @Test
    public void testOf_UnrestrictedWithNull() {
        assertThat(AttributeRelevance.of(new UnrestrictedValueSet(configuredValueSet, "1", true)),
                is(AttributeRelevance.Optional));
    }

    @Test
    public void testOf_EnumWithoutNull() {
        EnumValueSet enumValueSet = new EnumValueSet(configuredValueSet, "1");
        enumValueSet.addValue("Foo");
        enumValueSet.addValue("Bar");
        enumValueSet.setContainsNull(false);

        assertThat(AttributeRelevance.of(enumValueSet),
                is(AttributeRelevance.Mandatory));
    }

    @Test
    public void testOf_EnumWithNull() {
        EnumValueSet enumValueSet = new EnumValueSet(configuredValueSet, "1");
        enumValueSet.addValue("Foo");
        enumValueSet.addValue("Bar");
        enumValueSet.setContainsNull(true);

        assertThat(AttributeRelevance.of(enumValueSet),
                is(AttributeRelevance.Optional));
    }

    @Test
    public void testOf_EmptyEnum() {
        assertThat(AttributeRelevance.of(new EnumValueSet(configuredValueSet, "1")),
                is(AttributeRelevance.Irrelevant));
    }

    @Test
    public void testOf_RangeWithoutNull() {
        assertThat(AttributeRelevance.of(new RangeValueSet(configuredValueSet, "1", "0", "100", "10", false)),
                is(AttributeRelevance.Mandatory));
    }

    @Test
    public void testOf_RangeWithNull() {
        assertThat(AttributeRelevance.of(new RangeValueSet(configuredValueSet, "1", "0", "100", "10", true)),
                is(AttributeRelevance.Optional));
    }

    @Test
    public void testOf_EmptyRange() {
        assertThat(AttributeRelevance.of(RangeValueSet.empty(configuredValueSet, "1")),
                is(AttributeRelevance.Irrelevant));
    }

    @Test
    public void testSet_Irrelevant_From_UnrestrictedWithNull() {
        configuredValueSet.changeValueSetType(ValueSetType.UNRESTRICTED);
        configuredValueSet.getValueSet().setContainsNull(true);

        AttributeRelevance.Irrelevant.set(configuredValueSet);

        assertThat(configuredValueSet.getValueSet(), is(empty()));
        assertThat(configuredValueSet.getValueSet(), is(instanceOf(IEnumValueSet.class)));
    }

    @Test
    public void testSet_Irrelevant_From_UnrestrictedWithoutNull() {
        configuredValueSet.changeValueSetType(ValueSetType.UNRESTRICTED);
        configuredValueSet.getValueSet().setContainsNull(false);

        AttributeRelevance.Irrelevant.set(configuredValueSet);

        assertThat(configuredValueSet.getValueSet(), is(empty()));
        assertThat(configuredValueSet.getValueSet(), is(instanceOf(IEnumValueSet.class)));
    }

    @Test
    public void testSet_Irrelevant_From_EnumWithNull() {
        configuredValueSet.changeValueSetType(ValueSetType.ENUM);
        configuredValueSet.getValueSet().setContainsNull(true);
        ((IEnumValueSet)configuredValueSet.getValueSet()).addValue("1");
        ((IEnumValueSet)configuredValueSet.getValueSet()).addValue("3");

        AttributeRelevance.Irrelevant.set(configuredValueSet);

        assertThat(configuredValueSet.getValueSet(), is(empty()));
        assertThat(configuredValueSet.getValueSet(), is(instanceOf(IEnumValueSet.class)));
    }

    @Test
    public void testSet_Irrelevant_From_EnumWithoutNull() {
        configuredValueSet.changeValueSetType(ValueSetType.ENUM);
        configuredValueSet.getValueSet().setContainsNull(false);
        ((IEnumValueSet)configuredValueSet.getValueSet()).addValue("1");
        ((IEnumValueSet)configuredValueSet.getValueSet()).addValue("3");

        AttributeRelevance.Irrelevant.set(configuredValueSet);

        assertThat(configuredValueSet.getValueSet(), is(empty()));
        assertThat(configuredValueSet.getValueSet(), is(instanceOf(IEnumValueSet.class)));
    }

    @Test
    public void testSet_Irrelevant_From_EmptyEnum_WithEnumInModel() {
        attribute.changeValueSetType(ValueSetType.ENUM);
        ((IEnumValueSet)attribute.getValueSet()).addValue("1");
        ((IEnumValueSet)attribute.getValueSet()).addValue("3");
        configuredValueSet.changeValueSetType(ValueSetType.ENUM);
        configuredValueSet.getValueSet().setContainsNull(false);

        AttributeRelevance.Irrelevant.set(configuredValueSet);

        assertThat(configuredValueSet.getValueSet(), is(empty()));
        assertThat(configuredValueSet.getValueSet(), is(instanceOf(IEnumValueSet.class)));
    }

    @Test
    public void testSet_Irrelevant_From_RangeWithNull() {
        configuredValueSet.changeValueSetType(ValueSetType.RANGE);
        configuredValueSet.getValueSet().setContainsNull(true);
        ((IRangeValueSet)configuredValueSet.getValueSet()).setLowerBound("0");
        ((IRangeValueSet)configuredValueSet.getValueSet()).setUpperBound("100");

        AttributeRelevance.Irrelevant.set(configuredValueSet);

        assertThat(configuredValueSet.getValueSet(), is(empty()));
        assertThat(configuredValueSet.getValueSet(), is(instanceOf(IRangeValueSet.class)));
    }

    @Test
    public void testSet_Irrelevant_From_RangeWithoutNull() {
        configuredValueSet.changeValueSetType(ValueSetType.RANGE);
        configuredValueSet.getValueSet().setContainsNull(false);
        ((IRangeValueSet)configuredValueSet.getValueSet()).setLowerBound("0");
        ((IRangeValueSet)configuredValueSet.getValueSet()).setUpperBound("100");

        AttributeRelevance.Irrelevant.set(configuredValueSet);

        assertThat(configuredValueSet.getValueSet(), is(empty()));
        assertThat(configuredValueSet.getValueSet(), is(instanceOf(IRangeValueSet.class)));
    }

    @Test
    public void testSet_Mandatory_From_UnrestrictedWithNull() {
        configuredValueSet.changeValueSetType(ValueSetType.UNRESTRICTED);
        configuredValueSet.getValueSet().setContainsNull(true);

        AttributeRelevance.Mandatory.set(configuredValueSet);

        assertThat(configuredValueSet.getValueSet(), is(instanceOf(IUnrestrictedValueSet.class)));
        assertThat(configuredValueSet.getValueSet(), not(containsNull()));
    }

    @Test
    public void testSet_Mandatory_From_UnrestrictedWithoutNull() {
        configuredValueSet.changeValueSetType(ValueSetType.UNRESTRICTED);
        configuredValueSet.getValueSet().setContainsNull(false);

        AttributeRelevance.Mandatory.set(configuredValueSet);

        assertThat(configuredValueSet.getValueSet(), is(instanceOf(IUnrestrictedValueSet.class)));
        assertThat(configuredValueSet.getValueSet(), not(containsNull()));
    }

    @Test
    public void testSet_Mandatory_From_EnumWithNull() {
        configuredValueSet.changeValueSetType(ValueSetType.ENUM);
        configuredValueSet.getValueSet().setContainsNull(true);
        ((IEnumValueSet)configuredValueSet.getValueSet()).addValue("1");
        ((IEnumValueSet)configuredValueSet.getValueSet()).addValue("3");

        AttributeRelevance.Mandatory.set(configuredValueSet);

        assertThat(configuredValueSet.getValueSet(), is(instanceOf(IEnumValueSet.class)));
        assertThat(configuredValueSet.getValueSet(), not(containsNull()));
        assertThat(configuredValueSet.getValueSet(), contains("1", "3"));
    }

    @Test
    public void testSet_Mandatory_From_EnumWithoutNull() {
        configuredValueSet.changeValueSetType(ValueSetType.ENUM);
        configuredValueSet.getValueSet().setContainsNull(false);
        ((IEnumValueSet)configuredValueSet.getValueSet()).addValue("1");
        ((IEnumValueSet)configuredValueSet.getValueSet()).addValue("3");

        AttributeRelevance.Mandatory.set(configuredValueSet);

        assertThat(configuredValueSet.getValueSet(), is(instanceOf(IEnumValueSet.class)));
        assertThat(configuredValueSet.getValueSet(), not(containsNull()));
        assertThat(configuredValueSet.getValueSet(), contains("1", "3"));
    }

    @Test
    public void testSet_Mandatory_From_EmptyEnum() {
        configuredValueSet.changeValueSetType(ValueSetType.ENUM);
        configuredValueSet.getValueSet().setContainsNull(false);

        AttributeRelevance.Mandatory.set(configuredValueSet);

        assertThat(configuredValueSet.getValueSet(), is(instanceOf(IUnrestrictedValueSet.class)));
        assertThat(configuredValueSet.getValueSet(), not(containsNull()));
    }

    @Test
    public void testSet_Mandatory_From_EmptyEnum_WithEnumInModel() {
        attribute.changeValueSetType(ValueSetType.ENUM);
        ((IEnumValueSet)attribute.getValueSet()).addValue("1");
        ((IEnumValueSet)attribute.getValueSet()).addValue("3");
        configuredValueSet.changeValueSetType(ValueSetType.ENUM);
        configuredValueSet.getValueSet().setContainsNull(false);

        AttributeRelevance.Mandatory.set(configuredValueSet);

        assertThat(configuredValueSet.getValueSet(), is(instanceOf(IEnumValueSet.class)));
        assertThat(configuredValueSet.getValueSet(), not(containsNull()));
        assertThat(configuredValueSet.getValueSet(), contains("1", "3"));
    }

    @Test
    public void testSet_Mandatory_From_RangeWithNull() {
        configuredValueSet.changeValueSetType(ValueSetType.RANGE);
        configuredValueSet.getValueSet().setContainsNull(true);
        ((IRangeValueSet)configuredValueSet.getValueSet()).setLowerBound("0");
        ((IRangeValueSet)configuredValueSet.getValueSet()).setUpperBound("100");

        AttributeRelevance.Mandatory.set(configuredValueSet);

        assertThat(configuredValueSet.getValueSet(), is(isRange("0", "100")));
        assertThat(configuredValueSet.getValueSet(), not(containsNull()));
    }

    @Test
    public void testSet_Mandatory_From_RangeWithoutNull() {
        configuredValueSet.changeValueSetType(ValueSetType.RANGE);
        configuredValueSet.getValueSet().setContainsNull(false);
        ((IRangeValueSet)configuredValueSet.getValueSet()).setLowerBound("0");
        ((IRangeValueSet)configuredValueSet.getValueSet()).setUpperBound("100");

        AttributeRelevance.Mandatory.set(configuredValueSet);

        assertThat(configuredValueSet.getValueSet(), is(isRange("0", "100")));
        assertThat(configuredValueSet.getValueSet(), not(containsNull()));
    }

    @Test
    public void testSet_Mandatory_From_EmptyRange() {
        configuredValueSet.changeValueSetType(ValueSetType.RANGE);
        ((IRangeValueSet)configuredValueSet.getValueSet()).setEmpty(true);

        AttributeRelevance.Mandatory.set(configuredValueSet);

        assertThat(configuredValueSet.getValueSet(), is(isRange(null, null)));
        assertThat(configuredValueSet.getValueSet(), is(not(empty())));
        assertThat(configuredValueSet.getValueSet(), not(containsNull()));
    }

    @Test
    public void testSet_Mandatory_From_EmptyRange_WithRangeInModel() {
        attribute.changeValueSetType(ValueSetType.RANGE);
        ((IRangeValueSet)attribute.getValueSet()).setLowerBound("0");
        ((IRangeValueSet)attribute.getValueSet()).setUpperBound("100");
        configuredValueSet.changeValueSetType(ValueSetType.RANGE);
        ((IRangeValueSet)configuredValueSet.getValueSet()).setEmpty(true);

        AttributeRelevance.Mandatory.set(configuredValueSet);

        assertThat(configuredValueSet.getValueSet(), is(isRange("0", "100")));
        assertThat(configuredValueSet.getValueSet(), is(not(empty())));
        assertThat(configuredValueSet.getValueSet(), not(containsNull()));
    }

    @Test
    public void testSet_Optional_From_UnrestrictedWithNull() {
        configuredValueSet.changeValueSetType(ValueSetType.UNRESTRICTED);
        configuredValueSet.getValueSet().setContainsNull(true);

        AttributeRelevance.Optional.set(configuredValueSet);

        assertThat(configuredValueSet.getValueSet(), is(instanceOf(IUnrestrictedValueSet.class)));
        assertThat(configuredValueSet.getValueSet(), containsNull());
    }

    @Test
    public void testSet_Optional_From_UnrestrictedWithoutNull() {
        configuredValueSet.changeValueSetType(ValueSetType.UNRESTRICTED);
        configuredValueSet.getValueSet().setContainsNull(false);

        AttributeRelevance.Optional.set(configuredValueSet);

        assertThat(configuredValueSet.getValueSet(), is(instanceOf(IUnrestrictedValueSet.class)));
        assertThat(configuredValueSet.getValueSet(), containsNull());
    }

    @Test
    public void testSet_Optional_From_EnumWithNull() {
        configuredValueSet.changeValueSetType(ValueSetType.ENUM);
        configuredValueSet.getValueSet().setContainsNull(true);
        ((IEnumValueSet)configuredValueSet.getValueSet()).addValue("1");
        ((IEnumValueSet)configuredValueSet.getValueSet()).addValue("3");

        AttributeRelevance.Optional.set(configuredValueSet);

        assertThat(configuredValueSet.getValueSet(), is(instanceOf(IEnumValueSet.class)));
        assertThat(configuredValueSet.getValueSet(), containsNull());
        assertThat(configuredValueSet.getValueSet(), contains("1", "3"));
    }

    @Test
    public void testSet_Optional_From_EnumWithoutNull() {
        configuredValueSet.changeValueSetType(ValueSetType.ENUM);
        configuredValueSet.getValueSet().setContainsNull(false);
        ((IEnumValueSet)configuredValueSet.getValueSet()).addValue("1");
        ((IEnumValueSet)configuredValueSet.getValueSet()).addValue("3");

        AttributeRelevance.Optional.set(configuredValueSet);

        assertThat(configuredValueSet.getValueSet(), is(instanceOf(IEnumValueSet.class)));
        assertThat(configuredValueSet.getValueSet(), containsNull());
        assertThat(configuredValueSet.getValueSet(), contains("1", "3"));
    }

    @Test
    public void testSet_Optional_From_EmptyEnum() {
        configuredValueSet.changeValueSetType(ValueSetType.ENUM);
        configuredValueSet.getValueSet().setContainsNull(false);

        AttributeRelevance.Optional.set(configuredValueSet);

        assertThat(configuredValueSet.getValueSet(), is(instanceOf(IUnrestrictedValueSet.class)));
        assertThat(configuredValueSet.getValueSet(), containsNull());
    }

    @Test
    public void testSet_Optional_From_EmptyEnum_WithEnumInModel() {
        attribute.changeValueSetType(ValueSetType.ENUM);
        ((IEnumValueSet)attribute.getValueSet()).addValue("1");
        ((IEnumValueSet)attribute.getValueSet()).addValue("3");
        configuredValueSet.changeValueSetType(ValueSetType.ENUM);
        configuredValueSet.getValueSet().setContainsNull(false);

        AttributeRelevance.Optional.set(configuredValueSet);

        assertThat(configuredValueSet.getValueSet(), is(instanceOf(IEnumValueSet.class)));
        assertThat(configuredValueSet.getValueSet(), containsNull());
        assertThat(configuredValueSet.getValueSet(), contains("1", "3"));
    }

    @Test
    public void testSet_Optional_From_RangeWithNull() {
        configuredValueSet.changeValueSetType(ValueSetType.RANGE);
        configuredValueSet.getValueSet().setContainsNull(true);
        ((IRangeValueSet)configuredValueSet.getValueSet()).setLowerBound("0");
        ((IRangeValueSet)configuredValueSet.getValueSet()).setUpperBound("100");

        AttributeRelevance.Optional.set(configuredValueSet);

        assertThat(configuredValueSet.getValueSet(), is(isRange("0", "100")));
        assertThat(configuredValueSet.getValueSet(), containsNull());
    }

    @Test
    public void testSet_Optional_From_RangeWithoutNull() {
        configuredValueSet.changeValueSetType(ValueSetType.RANGE);
        configuredValueSet.getValueSet().setContainsNull(false);
        ((IRangeValueSet)configuredValueSet.getValueSet()).setLowerBound("0");
        ((IRangeValueSet)configuredValueSet.getValueSet()).setUpperBound("100");

        AttributeRelevance.Optional.set(configuredValueSet);

        assertThat(configuredValueSet.getValueSet(), is(isRange("0", "100")));
        assertThat(configuredValueSet.getValueSet(), containsNull());
    }

    @Test
    public void testSet_Optional_From_EmptyRange() {
        configuredValueSet.changeValueSetType(ValueSetType.RANGE);
        ((IRangeValueSet)configuredValueSet.getValueSet()).setEmpty(true);

        AttributeRelevance.Optional.set(configuredValueSet);

        assertThat(configuredValueSet.getValueSet(), is(isRange(null, null)));
        assertThat(configuredValueSet.getValueSet(), is(not(empty())));
        assertThat(configuredValueSet.getValueSet(), containsNull());
    }

    @Test
    public void testSet_Optional_From_EmptyRange_WithRangeInModel() {
        attribute.changeValueSetType(ValueSetType.RANGE);
        ((IRangeValueSet)attribute.getValueSet()).setLowerBound("0");
        ((IRangeValueSet)attribute.getValueSet()).setUpperBound("100");
        configuredValueSet.changeValueSetType(ValueSetType.RANGE);
        ((IRangeValueSet)configuredValueSet.getValueSet()).setEmpty(true);

        AttributeRelevance.Optional.set(configuredValueSet);

        assertThat(configuredValueSet.getValueSet(), is(isRange("0", "100")));
        assertThat(configuredValueSet.getValueSet(), is(not(empty())));
        assertThat(configuredValueSet.getValueSet(), containsNull());
    }

    public static class ValueSetMatchers {

        public static Matcher<IValueSet> empty() {
            return new TypeSafeMatcher<IValueSet>() {

                @Override
                public void describeTo(Description description) {
                    description.appendText("an empty value set");
                }

                @Override
                protected boolean matchesSafely(IValueSet valueSet) {
                    return valueSet.isEmpty();
                }
            };
        }

        public static Matcher<IValueSet> containsNull() {
            return new TypeSafeMatcher<IValueSet>() {

                @Override
                public void describeTo(Description description) {
                    description.appendText("a value set containing null");
                }

                @Override
                protected boolean matchesSafely(IValueSet valueSet) {
                    return valueSet.isContainsNull();
                }
            };
        }

        public static Matcher<IValueSet> isRange(String lower, String upper) {
            return new TypeSafeMatcher<IValueSet>() {

                @Override
                public void describeTo(Description description) {
                    description.appendText("a range value set from " + lower + " to " + upper);
                }

                @Override
                protected boolean matchesSafely(IValueSet valueSet) {
                    return valueSet.isRange()
                            && Objects.equal(((IRangeValueSet)valueSet).getLowerBound(), lower)
                            && Objects.equal(((IRangeValueSet)valueSet).getUpperBound(), upper);
                }
            };
        }

        public static Matcher<IValueSet> contains(String... values) {
            return new TypeSafeMatcher<IValueSet>() {

                @Override
                public void describeTo(Description description) {
                    description.appendText("a value set containing " + String.join(", ", values));
                }

                @Override
                protected boolean matchesSafely(IValueSet valueSet) {
                    return Arrays.stream(values).allMatch(v -> {
                        try {
                            return valueSet.containsValue(v, valueSet.getIpsProject());
                        } catch (CoreException e) {
                            fail(e.getMessage());
                            return false;
                        }
                    });
                }
            };
        }
    }

}
