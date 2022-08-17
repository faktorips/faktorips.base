/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller;

import java.beans.PropertyDescriptor;

import org.faktorips.devtools.core.ui.binding.PresentationModelObject;

/**
 * This mapping does not update neither the control nor the object's property but does show problem
 * markers of the object's property at the control. This is useful if you want to bind the content
 * by using an {@link PresentationModelObject} but still wants to show the problem markers of the
 * validation messages of the original object.
 * 
 * @author dirmeier
 */
public class ProblemMarkerPropertyMapping<T> extends FieldPropertyMappingByPropertyDescriptor<T> {

    public ProblemMarkerPropertyMapping(EditField<T> edit, Object object, PropertyDescriptor property) {
        super(edit, object, property);
    }

    @Override
    public void setPropertyValue() {
        // do not update the property - this mapping should only show error markers
    }

    @Override
    public void setControlValue(boolean force) {
        // do not update the control - this mapping should only show error markers
    }

}
