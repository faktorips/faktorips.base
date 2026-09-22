/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.pctype;

import static org.faktorips.testsupport.IpsMatchers.lacksMessageCode;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.ui.editors.AbstractAttributeEditDialogTest;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.valueset.IEnumValueSet;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.devtools.model.valueset.ValueSetType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * A not yet resolvable datatype for a policy attribute must neither reset the attribute's
 * {@link ValueSetType} nor discard the values already configured for it, while a datatype that does
 * resolve must still correct a value set type it does not allow.
 * <p>
 * These tests drive the model directly through {@code setDatatype}, which is what the datatype field
 * does per keystroke, but they do not exercise the field, its binding or its content proposals. The
 * deferral until the field is left is therefore simulated rather than produced by real focus, see
 * {@link #createDialogWhileEditingDatatype()}.
 * <p>
 * Three things about this fixture are load bearing:
 * <ul>
 * <li>The dialog is fully created rather than only built, because it reacts to datatype changes
 * through a content change listener that first refreshes the title and the message area. The IPS
 * model swallows every exception such a listener throws, so a half built dialog would fail silently
 * and every "value set is unchanged" assertion would hold for the wrong reason.
 * <li>The enum identifiers are the integers already configured in the value set, so preserving the
 * value set leaves the attribute valid rather than merely unchanged.
 * <li>The attribute is not configured by product unless a test says so, which is what makes an
 * extensible enum datatype incompatible with an enum value set.
 * </ul>
 */
public class AttributeEditDialogTest extends AbstractAttributeEditDialogTest {

    private IIpsProject ipsProject;
    private IPolicyCmptTypeAttribute attribute;
    private AttributeEditDialog dialog;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        PolicyCmptType policyCmptType = newPolicyAndProductCmptType(ipsProject, "Policy", "PolicyType");
        attribute = policyCmptType.newPolicyCmptTypeAttribute("zahlweise");
        attribute.setDatatype(Datatype.INTEGER.getQualifiedName());
        attribute.setValueSetType(ValueSetType.ENUM);
        ((IEnumValueSet)attribute.getValueSet()).addValues(ZAHLWEISE_VALUES);

        newZahlweiseEnumType(ipsProject);

        openShell();
        dialog = createDialog();
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        try {
            if (dialog != null) {
                dialog.close();
            }
        } finally {
            disposeShell();
            super.tearDown();
        }
    }

    private AttributeEditDialog createDialog() {
        AttributeEditDialog newDialog = new AttributeEditDialog(attribute, shell);
        newDialog.create();
        return newDialog;
    }

    /**
     * SWT grants no focus to a shell that was never opened, so the "user is editing the datatype
     * field" state cannot be produced by asking the widget. It is simulated instead, which keeps the
     * deferral itself testable while the focus query stays a one liner over
     * {@link org.eclipse.swt.widgets.Control#isFocusControl()}.
     * <p>
     * Closes the {@code dialog} field first: {@code IpsPartEditDialog2} registers itself as a model
     * wide {@link org.faktorips.devtools.model.ContentsChangeListener} for as long as it is open, so
     * leaving it open would make it react to the very same {@code setDatatype} calls the returned
     * dialog is being tested against, independently switching the shared attribute's value set
     * through its own, non-deferred control.
     */
    private AttributeEditDialog createDialogWhileEditingDatatype() {
        dialog.close();
        dialog = null;
        AttributeEditDialog editingDialog = new AttributeEditDialog(attribute, shell) {
            @Override
            boolean isEditingDatatype() {
                return true;
            }
        };
        editingDialog.create();
        return editingDialog;
    }

    private ValueSetType valueSetType() {
        return attribute.getValueSet().getValueSetType();
    }

    private List<String> configuredValues() {
        return ((IEnumValueSet)attribute.getValueSet()).getValuesAsList();
    }

    @Test
    public void testSettingUnresolvableDatatype_doesNotChangeValueSetType() {
        attribute.setDatatype("Zahlweis");

        assertThat(attribute.findDatatype(ipsProject), is(nullValue()));
        assertThat(valueSetType(), is(ValueSetType.ENUM));
        assertThat(configuredValues(), is(ZAHLWEISE_VALUES));
    }

    @Test
    public void testSuccessiveDatatypeChangesEndingOnEnum_preservesValueSetAndValues() {
        attribute.setDatatype("Z");
        attribute.setDatatype("Za");
        attribute.setDatatype("Zahlweis");
        attribute.setDatatype("Zahlweise");

        assertThat(attribute.findDatatype(ipsProject).getQualifiedName(), is("Zahlweise"));
        assertThat(valueSetType(), is(ValueSetType.ENUM));
        assertThat(configuredValues(), is(ZAHLWEISE_VALUES));
    }

    @Test
    public void testSuccessiveDatatypeChangesBackToInteger_preservesValueSetAndValues() {
        attribute.setDatatype("Z");
        attribute.setDatatype("Za");
        attribute.setDatatype("Zahlweis");
        attribute.setDatatype("Zahlweise");

        attribute.setDatatype("I");
        attribute.setDatatype("Int");
        attribute.setDatatype("Integer");

        assertThat(attribute.findDatatype(ipsProject).getQualifiedName(), is(Datatype.INTEGER.getQualifiedName()));
        assertThat(valueSetType(), is(ValueSetType.ENUM));
        assertThat(configuredValues(), is(ZAHLWEISE_VALUES));
    }

    @Test
    public void testChangingDatatypeInOneStep_preservesValueSetAndValues() {
        attribute.setDatatype("Zahlweise");

        assertThat(valueSetType(), is(ValueSetType.ENUM));
        assertThat(configuredValues(), is(ZAHLWEISE_VALUES));
    }

    @Test
    public void testChangingToExtensibleEnumWhenNotProductRelevant_removesEnumValueSetType() {
        IEnumType extensibleEnumType = newDefaultEnumType(ipsProject, "ExtensibleEnum");
        extensibleEnumType.setExtensible(true);
        attribute.setValueSetConfiguredByProduct(false);

        attribute.setDatatype(extensibleEnumType.getQualifiedName());

        assertThat(attribute.findDatatype(ipsProject), is(notNullValue()));
        assertThat(valueSetType(), is(not(ValueSetType.ENUM)));
    }

    @Test
    public void testResolvableDatatypeWithoutEnumSupport_fallsBackToAllowedValueSetType() {
        attribute.setDatatype(Datatype.INTEGER.getQualifiedName() + "[]");

        assertThat(attribute.findDatatype(ipsProject), is(notNullValue()));
        assertThat(valueSetType(), is(not(ValueSetType.ENUM)));
    }

    @Test
    public void testReopeningDialogWithUnresolvableDatatype_preservesEnumValueSetAndValues() {
        attribute.setDatatype("Zahlweis");
        assertThat(valueSetType(), is(ValueSetType.ENUM));

        dialog.close();
        dialog = null;
        AttributeEditDialog reopenedDialog = new AttributeEditDialog(attribute, shell);
        try {
            reopenedDialog.create();

            assertThat(attribute.findDatatype(ipsProject), is(nullValue()));
            assertThat(valueSetType(), is(ValueSetType.ENUM));
            assertThat(configuredValues(), is(ZAHLWEISE_VALUES));
        } finally {
            reopenedDialog.close();
        }
    }

    @Test
    public void testSettingUnresolvableDatatype_disablesControlWithoutReplacingOfferedTypes() {
        assertThat(dialog.getValueSetSpecificationControl().isDataChangeable(), is(true));

        attribute.setDatatype("Zahlweis");

        assertThat(dialog.getValueSetSpecificationControl().isDataChangeable(), is(false));
        assertThat(dialog.getValueSetSpecificationControl().getAllowedValueSetTypes(), hasItem(ValueSetType.ENUM));
    }

    @Test
    public void testSettingEnumDatatypeWithMatchingIds_leavesValueSetValid() {
        attribute.setDatatype("Zahlweise");

        assertThat(valueSetType(), is(ValueSetType.ENUM));
        assertThat(configuredValues(), is(ZAHLWEISE_VALUES));
        assertThat(attribute.getValueSet().validate(ipsProject),
                lacksMessageCode(IValueSet.MSGCODE_VALUE_NOT_PARSABLE));
        assertThat(attribute.getValueSet().validate(ipsProject),
                lacksMessageCode(IValueSet.MSGCODE_UNKNOWN_DATATYPE));
    }

    @Test
    public void testProductConfigurableAttribute_settingUnresolvableDatatype_preservesValueSetAndValues() {
        attribute.setValueSetConfiguredByProduct(true);

        attribute.setDatatype("Zahlweis");

        assertThat(attribute.isProductRelevant(), is(true));
        assertThat(attribute.findDatatype(ipsProject), is(nullValue()));
        assertThat(valueSetType(), is(ValueSetType.ENUM));
        assertThat(configuredValues(), is(ZAHLWEISE_VALUES));
    }

    @Test
    public void testProductConfigurableAttribute_successiveDatatypeChangesEndingOnEnum_preservesValueSetAndValues() {
        attribute.setValueSetConfiguredByProduct(true);

        attribute.setDatatype("Z");
        attribute.setDatatype("Za");
        attribute.setDatatype("Zahlweis");
        attribute.setDatatype("Zahlweise");

        assertThat(attribute.findDatatype(ipsProject).getQualifiedName(), is("Zahlweise"));
        assertThat(valueSetType(), is(ValueSetType.ENUM));
        assertThat(configuredValues(), is(ZAHLWEISE_VALUES));
    }

    @Test
    public void testProductConfigurableAttribute_successiveDatatypeChangesBackToInteger_preservesValueSetAndValues() {
        attribute.setValueSetConfiguredByProduct(true);
        attribute.setDatatype("Zahlweise");

        attribute.setDatatype("I");
        attribute.setDatatype("Int");
        attribute.setDatatype("Integer");

        assertThat(attribute.findDatatype(ipsProject).getQualifiedName(), is(Datatype.INTEGER.getQualifiedName()));
        assertThat(valueSetType(), is(ValueSetType.ENUM));
        assertThat(configuredValues(), is(ZAHLWEISE_VALUES));
    }

    @Test
    public void testProductConfigurableAttribute_reopeningDialogWithUnresolvableDatatype_preservesValueSetAndValues() {
        attribute.setValueSetConfiguredByProduct(true);
        attribute.setDatatype("Zahlweis");

        dialog.close();
        dialog = null;
        AttributeEditDialog reopenedDialog = new AttributeEditDialog(attribute, shell);
        try {
            reopenedDialog.create();

            assertThat(valueSetType(), is(ValueSetType.ENUM));
            assertThat(configuredValues(), is(ZAHLWEISE_VALUES));
        } finally {
            reopenedDialog.close();
        }
    }

    @Test
    public void testEditingDatatypeField_defersValueSetAdjustment() {
        AttributeEditDialog editingDialog = createDialogWhileEditingDatatype();
        try {
            attribute.setDatatype(Datatype.INTEGER.getQualifiedName() + "[]");

            assertThat(attribute.findDatatype(ipsProject), is(notNullValue()));
            assertThat(valueSetType(), is(ValueSetType.ENUM));
            assertThat(configuredValues(), is(ZAHLWEISE_VALUES));
        } finally {
            editingDialog.close();
        }
    }

    @Test
    public void testLeavingDatatypeField_appliesTheDeferredValueSetAdjustment() {
        AttributeEditDialog editingDialog = createDialogWhileEditingDatatype();
        try {
            attribute.setDatatype(Datatype.INTEGER.getQualifiedName() + "[]");
            assertThat(valueSetType(), is(ValueSetType.ENUM));

            editingDialog.getDatatypeControl().getTextControl().notifyListeners(SWT.FocusOut, new Event());
            drainDisplayEvents();

            assertThat(valueSetType(), is(not(ValueSetType.ENUM)));
        } finally {
            editingDialog.close();
        }
    }

    @Test
    public void testConfirmingDialogWhileEditingDatatypeField_appliesTheDeferredValueSetAdjustment() {
        AttributeEditDialog editingDialog = createDialogWhileEditingDatatype();
        try {
            attribute.setDatatype(Datatype.INTEGER.getQualifiedName() + "[]");
            assertThat(valueSetType(), is(ValueSetType.ENUM));

            editingDialog.okPressed();

            assertThat(valueSetType(), is(not(ValueSetType.ENUM)));
        } finally {
            editingDialog.close();
        }
    }

    /**
     * The {@code ModifyListener} added alongside the real {@code FocusListener} only sets the
     * editing flag when {@code isFocusControl()} is true, which a shell that was never opened never
     * reports (see the class Javadoc), so only the focus half of the real listener wiring is
     * reachable here. It is exercised through a real {@code SWT.FocusIn} event rather than the
     * hardcoded {@link #createDialogWhileEditingDatatype()} override, so a broken listener
     * registration (e.g. never attached, or an inverted condition) would fail this test.
     */
    @Test
    public void testRealFocusGainedEvent_setsDatatypeFieldActive() {
        assertThat(dialog.isEditingDatatype(), is(false));

        dialog.getDatatypeControl().getTextControl().notifyListeners(SWT.FocusIn, new Event());

        assertThat(dialog.isEditingDatatype(), is(true));
    }

    @Test
    public void testTogglingProductRelevance_refreshesOfferedValueSetTypes() {
        IEnumType extensibleEnumType = newDefaultEnumType(ipsProject, "ExtensibleEnum");
        extensibleEnumType.setExtensible(true);
        attribute.setValueSetConfiguredByProduct(true);
        attribute.setDatatype(extensibleEnumType.getQualifiedName());
        assertThat(dialog.getValueSetSpecificationControl().getAllowedValueSetTypes(), hasItem(ValueSetType.ENUM));

        attribute.setValueSetConfiguredByProduct(false);

        assertThat(dialog.getValueSetSpecificationControl().getAllowedValueSetTypes(), not(hasItem(ValueSetType.ENUM)));
    }

    @Test
    public void testProductConfigurableAttribute_changingToExtensibleEnum_keepsEnumValueSetType() {
        IEnumType extensibleEnumType = newDefaultEnumType(ipsProject, "ExtensibleEnum");
        extensibleEnumType.setExtensible(true);
        attribute.setValueSetConfiguredByProduct(true);

        attribute.setDatatype(extensibleEnumType.getQualifiedName());

        assertThat(attribute.findDatatype(ipsProject), is(notNullValue()));
        assertThat(valueSetType(), is(ValueSetType.ENUM));
    }
}
