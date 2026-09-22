/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors;

import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.enums.IEnumValue;
import org.faktorips.devtools.model.internal.value.StringValue;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * Shared fixture for the pctype and productcmpttype {@code AttributeEditDialog} test suites, both of
 * which reproduce FIPS-8526/FIPS-15317's exact scenario against an Enum type with the same name and
 * values: an Integer-typed attribute whose Enum value set values match a real Enum datatype's IDs
 * one-for-one.
 * <p>
 * {@code newDefaultEnumType} is a protected member of {@link AbstractIpsPluginTest} in a different
 * package, so this fixture has to live in the class hierarchy rather than as a standalone utility.
 * <p>
 * {@code createDialogWhileEditingDatatype()} and the tests that exercise it directly are still
 * duplicated between the two subclasses rather than hoisted here: each subclass's
 * {@code AttributeEditDialog} is a distinct, unrelated production class (one per package) with no
 * shared test-friendly interface, so unifying that code would mean either introducing generics keyed
 * on the dialog type or adding a production-code interface purely for test convenience — both a larger
 * change than this fixture extraction, which only needed to touch test code.
 */
public abstract class AbstractAttributeEditDialogTest extends AbstractIpsPluginTest {

    protected static final List<String> ZAHLWEISE_VALUES = Arrays.asList("1", "2", "4", "12");

    protected Shell shell;

    protected void openShell() {
        shell = new Shell(Display.getCurrent());
    }

    protected void disposeShell() {
        shell.dispose();
    }

    protected IEnumType newZahlweiseEnumType(IIpsProject ipsProject) {
        IEnumType enumType = newDefaultEnumType(ipsProject, "Zahlweise");
        enumType.getEnumAttribute("id").setDatatype(Datatype.INTEGER.getQualifiedName());
        newZahlweiseValue(enumType, "JAEHRLICH", "1", "jährlich");
        newZahlweiseValue(enumType, "HALBJAEHRLICH", "2", "halbjährlich");
        newZahlweiseValue(enumType, "QUARTALSWEISE", "4", "quartalsweise");
        newZahlweiseValue(enumType, "MONATLICH", "12", "monatlich");
        return enumType;
    }

    private void newZahlweiseValue(IEnumType enumType, String literalName, String id, String name) {
        IEnumValue enumValue = enumType.newEnumValue();
        List<IEnumAttributeValue> attributeValues = enumValue.getEnumAttributeValues();
        attributeValues.get(0).setValue(new StringValue(literalName));
        attributeValues.get(1).setValue(new StringValue(id));
        attributeValues.get(2).setValue(new StringValue(name));
    }

    /**
     * Leaving the datatype field completes its work with {@code asyncExec}, so the queued runnable
     * has to be dispatched before the effect can be asserted.
     */
    protected void drainDisplayEvents() {
        while (shell.getDisplay().readAndDispatch()) {
            // dispatch until the queue is empty
        }
    }
}
