/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls.valuesets;

import org.eclipse.swt.widgets.Composite;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IRangeValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSetOwner;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controls.chooser.EnumValueSubsetChooserModel;
import org.faktorips.devtools.core.ui.controls.chooser.SubsetChooserEditControl;
import org.faktorips.devtools.core.ui.controls.chooser.SubsetChooserViewer;

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
     * @param uiController The bindingContext.
     * @param ipsProject the {@link IValueSetOwner}'s {@link IIpsProject}
     * 
     * @return The new composite.
     */
    public IValueSetEditControl newControl(IValueSet valueSet,
            ValueDatatype valueDatatype,
            Composite parent,
            UIToolkit toolkit,
            BindingContext uiController,
            IIpsProject ipsProject) {

        if (valueSet.isRange() && !valueSet.isAbstract()) {
            return new RangeEditControl(parent, toolkit, valueDatatype, (IRangeValueSet)valueSet, uiController);
        }
        if (valueSet.canBeUsedAsSupersetForAnotherEnumValueSet()) {
            IEnumValueSet enumValueSet = (IEnumValueSet)valueSet;
            if (valueDatatype.isEnum()) {
                SubsetChooserViewer subsetChooserViewer = new SubsetChooserViewer(parent, toolkit);
                EnumValueSubsetChooserModel model = new EnumValueSubsetChooserModel((EnumDatatype)valueDatatype,
                        enumValueSet);
                subsetChooserViewer.init(model);
                return new SubsetChooserEditControl(subsetChooserViewer.getChooserComposite(), model);
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
