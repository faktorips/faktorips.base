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

package org.faktorips.devtools.core.ui.search.product.conditions.types;

import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.valueset.IValueSet;

public abstract class AbstractAttributeCondition extends AbstractCondition {

    @Override
    public ValueDatatype getValueDatatype(IIpsElement elementPart) {
        IAttribute attribute = (IAttribute)elementPart;
        try {
            return attribute.findDatatype(attribute.getIpsProject());
        } catch (CoreException e) {
            // TODO Exception Handling
            throw new RuntimeException(e);
        }
    }

    @Override
    public IValueSet getValueSet(IIpsElement elementPart) {
        IAttribute attribute = (IAttribute)elementPart;

        try {
            return attribute.getValueSet();
        } catch (CoreException e) {
            // TODO Exception Handling
            throw new RuntimeException(e);
        }
    }

    @Override
    public Collection<?> getAllowedValues(IIpsElement elementPart) {
        throw new IllegalStateException("This Condition doesn't allow calling getAllowedValues");
    }

    @Override
    public boolean hasValueSet() {
        return true;
    }
}