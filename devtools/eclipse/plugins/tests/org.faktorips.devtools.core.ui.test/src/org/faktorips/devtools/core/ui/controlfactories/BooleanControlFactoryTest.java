/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controlfactories;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashMap;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptType;
import org.faktorips.devtools.model.internal.valueset.EnumValueSet;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.valueset.IEnumValueSet;
import org.faktorips.devtools.model.valueset.ValueSetType;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class BooleanControlFactoryTest extends AbstractIpsPluginTest {

    @Mock
    private IEnumValueSet enumValueSet;

    @Mock
    private BooleanControlFactory booleanControlFactory;

    private Shell shell;

    @Override
    @Before
    public void setUp() throws Exception {
        booleanControlFactory = new BooleanControlFactory();
        shell = new Shell(Display.getCurrent());
    }

    @Override
    @After
    public void tearDown() {
        shell.dispose();
    }

    @Test
    public void testRadioOptions_EnumValueSetWithoutNull() throws Exception {
        LinkedHashMap<String, String> options = booleanControlFactory.initOptions(enumValueSet,
                Datatype.PRIMITIVE_BOOLEAN);

        assertThat(options.keySet(), hasItem("true"));
        assertThat(options.keySet(), hasItem("false"));
    }

    @Test
    public void testRadioOptions_EnumValueSetNullIncluded() throws Exception {
        LinkedHashMap<String, String> options = booleanControlFactory.initOptions(enumValueSet, Datatype.BOOLEAN);

        assertThat(options.keySet(), hasItem("true"));
        assertThat(options.keySet(), hasItem("false"));
        assertTrue(options.containsKey(null));
    }

    @Test
    public void testDefaultValueButtonEnablement_TrueFalseNull() {
        IIpsProject ipsProject = newIpsProject();
        EnumValueSet valueSet = createBooleanAttribute(ipsProject, Boolean.TRUE, Boolean.FALSE, null);

        EditField<String> editField = booleanControlFactory.createEditField(new UIToolkit(null), shell,
                Datatype.BOOLEAN, valueSet, ipsProject);

        Composite composite = (Composite)editField.getControl();
        Control[] buttons = composite.getChildren();
        assertThat(buttons[0], is(enabled()));
        assertThat(buttons[1], is(enabled()));
        assertThat(buttons[2], is(enabled()));
    }

    @Test
    public void testDefaultValueButtonEnablement_TrueFalse() {
        IIpsProject ipsProject = newIpsProject();
        EnumValueSet valueSet = createBooleanAttribute(ipsProject, Boolean.TRUE, Boolean.FALSE);

        EditField<String> editField = booleanControlFactory.createEditField(new UIToolkit(null), shell,
                Datatype.BOOLEAN, valueSet, ipsProject);

        Composite composite = (Composite)editField.getControl();
        Control[] buttons = composite.getChildren();
        assertThat(buttons[0], is(enabled()));
        assertThat(buttons[1], is(enabled()));
        assertThat(buttons[2], is(enabled()));
    }

    @Test
    public void testDefaultValueButtonEnablement_TrueNull() {
        IIpsProject ipsProject = newIpsProject();
        EnumValueSet valueSet = createBooleanAttribute(ipsProject, Boolean.TRUE, null);

        EditField<String> editField = booleanControlFactory.createEditField(new UIToolkit(null), shell,
                Datatype.BOOLEAN, valueSet, ipsProject);

        Composite composite = (Composite)editField.getControl();
        Control[] buttons = composite.getChildren();
        assertThat(buttons[0], is(enabled()));
        assertThat(buttons[1], is(not(enabled())));
        assertThat(buttons[2], is(enabled()));
    }

    @Test
    public void testDefaultValueButtonEnablement_False() {
        IIpsProject ipsProject = newIpsProject();
        EnumValueSet valueSet = createBooleanAttribute(ipsProject, Boolean.FALSE, null);

        EditField<String> editField = booleanControlFactory.createEditField(new UIToolkit(null), shell,
                Datatype.BOOLEAN, valueSet, ipsProject);

        Composite composite = (Composite)editField.getControl();
        Control[] buttons = composite.getChildren();
        assertThat(buttons[0], is(not(enabled())));
        assertThat(buttons[1], is(enabled()));
        assertThat(buttons[2], is(enabled()));
    }

    private static Matcher<Control> enabled() {
        return new TypeSafeMatcher<>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("enabled");
            }

            @Override
            protected boolean matchesSafely(Control control) {
                return control.isEnabled();
            }
        };
    }

    public EnumValueSet createBooleanAttribute(IIpsProject ipsProject, Boolean... allowedValues) {
        PolicyCmptType policyCmptType = newPolicyCmptType(ipsProject, "V");
        IPolicyCmptTypeAttribute attribute = policyCmptType.newPolicyCmptTypeAttribute();
        attribute.setDatatype(Datatype.BOOLEAN.getQualifiedName());
        EnumValueSet valueSet = (EnumValueSet)attribute.changeValueSetType(ValueSetType.ENUM);
        for (Boolean value : allowedValues) {
            valueSet.addValue(value == null ? null : value.toString());
        }
        return valueSet;
    }
}
