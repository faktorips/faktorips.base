/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpttype;

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
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.valueset.IEnumValueSet;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.devtools.model.valueset.ValueSetType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The product component type counterpart to the policy attribute dialog: an attribute whose
 * datatype cannot be resolved must keep its {@link ValueSetType} and its configured values, both
 * while the datatype is being changed and when the dialog is opened on that state, while a datatype
 * that does resolve must still correct a value set type it does not allow.
 * <p>
 * These tests drive the model directly through {@code setDatatype}, which is what the datatype field
 * does per keystroke, but they do not exercise the field, its binding or its content proposals.
 * <p>
 * Building the value set page recomputes the allowed types right after creating the control, so
 * opening the dialog was the path that replaced the enum value set with a derived one before the
 * user had touched anything.
 */
public class AttributeEditDialogTest extends AbstractAttributeEditDialogTest {

    private IIpsProject ipsProject;
    private IProductCmptTypeAttribute attribute;
    private AttributeEditDialog dialog;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        IProductCmptType productCmptType = newProductCmptType(ipsProject, "ProductType");
        attribute = productCmptType.newProductCmptTypeAttribute("zahlweise");
        attribute.setDatatype(Datatype.INTEGER.getQualifiedName());
        attribute.setValueSetType(ValueSetType.ENUM);
        ((IEnumValueSet)attribute.getValueSet()).addValues(ZAHLWEISE_VALUES);

        newZahlweiseEnumType(ipsProject);

        openShell();
        dialog = new AttributeEditDialog(attribute, shell);
        dialog.create();
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

    private ValueSetType valueSetType() {
        return attribute.getValueSet().getValueSetType();
    }

    private List<String> configuredValues() {
        return ((IEnumValueSet)attribute.getValueSet()).getValuesAsList();
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
    public void testOpeningDialogWithUnresolvableDatatype_preservesValueSetAndValues() {
        attribute.setDatatype("DoesNotExist");
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
    public void testOpeningDialogWithUnresolvableDatatype_stillOffersTheCurrentValueSetType() {
        attribute.setDatatype("DoesNotExist");

        dialog.close();
        dialog = null;
        AttributeEditDialog reopenedDialog = new AttributeEditDialog(attribute, shell);
        try {
            reopenedDialog.create();

            assertThat(reopenedDialog.getValueSetEditControl().getAllowedValueSetTypes(), hasItem(ValueSetType.ENUM));
        } finally {
            reopenedDialog.close();
        }
    }

    @Test
    public void testSettingUnresolvableDatatype_preservesValueSetAndValues() {
        attribute.setDatatype("DoesNotExist");

        assertThat(attribute.findDatatype(ipsProject), is(nullValue()));
        assertThat(valueSetType(), is(ValueSetType.ENUM));
        assertThat(configuredValues(), is(ZAHLWEISE_VALUES));
        assertThat(dialog.getValueSetEditControl().getAllowedValueSetTypes(), hasItem(ValueSetType.ENUM));
    }

    @Test
    public void testSuccessiveDatatypeChangesEndingOnString_preservesValueSetAndValues() {
        attribute.setDatatype("S");
        attribute.setDatatype("St");
        attribute.setDatatype("String");

        assertThat(attribute.findDatatype(ipsProject).getQualifiedName(), is(Datatype.STRING.getQualifiedName()));
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
    public void testResolvableDatatypeWithoutEnumSupport_fallsBackToAllowedValueSetType() {
        attribute.setDatatype(Datatype.INTEGER.getQualifiedName() + "[]");

        assertThat(attribute.findDatatype(ipsProject), is(notNullValue()));
        assertThat(valueSetType(), is(not(ValueSetType.ENUM)));
    }
}
