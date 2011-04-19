/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls.valuesets;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IRangeValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSetOwner;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.UIController;

/**
 * Factory to create controls to edit a given value set. As the value set is given to the factory it
 * is not possible to switch the type of value set. The returned control es suitable to edit the
 * given value set instance.
 * 
 * @author Jan Ortmann
 */
public class ValueSetEditControlFactory {

    /**
     * Creates a new control that allows to edit a value set of the given type where the values are
     * instances of the given value datatype. The returned instance is an {@link Composite} and an
     * {@link IValueSetEditControl}. It is safe to cast it to {@link Composite}.
     * 
     * @param valueSet The value set that needs to be edited.
     * @param valueDatatype The datatype the values in the set are instances of.
     * @param parent The parent composite.
     * @param toolkit The ui toolkit to use.
     * @param uiController The ui controller.
     * @param ipsProject the {@link IValueSetOwner}'s {@link IIpsProject}
     * 
     * @return The new composite.
     */
    public Control newControl(IValueSet valueSet,
            ValueDatatype valueDatatype,
            Composite parent,
            UIToolkit toolkit,
            UIController uiController,
            IIpsProject ipsProject) {

        if (valueSet.isRange() && !valueSet.isAbstract()) {
            return new RangeEditControl(parent, toolkit, valueDatatype, (IRangeValueSet)valueSet, uiController);
        }
        if (valueSet.canBeUsedAsSupersetForAnotherEnumValueSet()) {
            IEnumValueSet enumValueSet = (IEnumValueSet)valueSet;
            if (valueDatatype.isEnum()) {
                EnumSubsetChooser chooser = new EnumSubsetChooser(parent, toolkit, null, enumValueSet, valueDatatype,
                        uiController);
                return chooser;
            }
            EnumValueSetEditControl enumValueSetEditControl = new EnumValueSetEditControl(parent, valueDatatype,
                    ipsProject);
            enumValueSetEditControl.initialize(enumValueSet, null);
            return enumValueSetEditControl;
        }
        throw new RuntimeException("Can't create edit control for value set " + valueSet + " and datatype " //$NON-NLS-1$ //$NON-NLS-2$
                + valueDatatype);
    }

}
