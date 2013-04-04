/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.productcmpt;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.valueset.EnumValueSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.valueset.IUnrestrictedValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.ui.binding.IpsObjectPartPmo;

/**
 * Presentation Model Object which links a Boolean control (three check boxes: true, false, and
 * null) and a boolean value control (three radio buttons: true, false, and null) to their
 * properties. This Presentation Model Object can also be used to represent primitive boolean types,
 * in which case the null option is omitted.
 * 
 */
public class BooleanValueSetPMO extends IpsObjectPartPmo {

    public static final String PROPERTY_TRUE = "trueInValueSet"; //$NON-NLS-1$

    public static final String PROPERTY_FALSE = "falseInValueSet"; //$NON-NLS-1$

    public static final String PROPERTY_NULL = "nullInValueSet"; //$NON-NLS-1$

    /**
     * The corresponding {@link IIpsProject}
     */
    private final IIpsProject ipsProject;

    /**
     * The {@link IConfigElement} representing the property value.
     */
    private final IConfigElement propertyValue;

    public BooleanValueSetPMO(IConfigElement propertyValue) {
        super(propertyValue.getValueSet());
        this.ipsProject = propertyValue.getIpsProject();
        this.propertyValue = propertyValue;
    }

    public boolean isTrueInValueSet() {
        try {
            return propertyValue.getValueSet().containsValue(Boolean.TRUE.toString(), ipsProject);
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    public void setTrueInValueSet(boolean b) {
        modifyValue(Boolean.TRUE.toString(), b);
    }

    public boolean isFalseInValueSet() {
        try {
            return propertyValue.getValueSet().containsValue(Boolean.FALSE.toString(), ipsProject);
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    public void setFalseInValueSet(boolean b) {
        modifyValue(Boolean.FALSE.toString(), b);
    }

    public boolean isNullInValueSet() {
        try {
            return propertyValue.getValueSet().containsValue(null, ipsProject);
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    public void setNullInValueSet(boolean b) {
        modifyValue(null, b);
    }

    /**
     * Adds or removes the specified value from the value set. Note that this method also implicitly
     * converts the value set from type unrestricted to type enum, if necessary.
     * 
     * @param value the value to add or remove
     * @param add if true, the value is added, otherwise, the value is removed
     */
    private void modifyValue(String value, boolean add) {
        try {
            IValueSet valueSet = propertyValue.getValueSet();
            if (add && !valueSet.containsValue(value, ipsProject) || !add && valueSet.containsValue(value, ipsProject)) {
                if (valueSet instanceof IUnrestrictedValueSet) {
                    valueSet = propertyValue.convertValueSetToEnumType();
                }
                if (add) {
                    ((EnumValueSet)valueSet).addValue(value);
                } else {
                    ((EnumValueSet)valueSet).removeValue(value);
                }
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    @Override
    protected void partHasChanged() {
        String currentValue = propertyValue.getPropertyValue();
        boolean needsToBeModified = isTrueValueSetButNotAllowed(currentValue)
                || isFalseValueSetButNotAllowed(currentValue) || isNullValueSetButNotAllowed(currentValue);
        if (needsToBeModified) {
            if (isTrueInValueSet()) {
                propertyValue.setValue(Boolean.TRUE.toString());
            } else if (isFalseInValueSet()) {
                propertyValue.setValue(Boolean.FALSE.toString());
            } else if (isNullInValueSet()) {
                propertyValue.setValue(null);
            }
        }
    }

    private boolean isTrueValueSetButNotAllowed(String currentValue) {
        return Boolean.TRUE.toString().equals(currentValue) && !isTrueInValueSet();
    }

    private boolean isFalseValueSetButNotAllowed(String currentValue) {
        return Boolean.FALSE.toString().equals(currentValue) && !isFalseInValueSet();
    }

    private boolean isNullValueSetButNotAllowed(String currentValue) {
        return currentValue == null && !isNullInValueSet();
    }
}