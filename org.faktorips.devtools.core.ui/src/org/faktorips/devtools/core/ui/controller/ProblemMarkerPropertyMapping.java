/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
    public void setControlValue() {
        // do not update the control - this mapping should only show error markers
    }

}
