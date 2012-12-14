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

public class BooleanValueSetPMO extends IpsObjectPartPmo {
    public static final String PROPERTY_TRUE_ENABLED = "trueEnabled"; //$NON-NLS-1$
    public static final String PROPERTY_FALSE_ENABLED = "falseEnabled"; //$NON-NLS-1$
    public static final String PROPERTY_NULL_ENABLED = "nullEnabled"; //$NON-NLS-1$
    public static final String PROPERTY_TRUE = "true"; //$NON-NLS-1$
    public static final String PROPERTY_FALSE = "false"; //$NON-NLS-1$
    public static final String PROPERTY_NULL = "null"; //$NON-NLS-1$

    private IValueSet valueSet;
    private final IIpsProject ipsProject;
    private final IConfigElement propertyValue;
    private boolean propTrue;
    private boolean propFalse;
    private boolean propNull;

    public BooleanValueSetPMO(IConfigElement propertyValue) {
        super(propertyValue.getValueSet());
        this.valueSet = propertyValue.getValueSet();
        this.ipsProject = propertyValue.getIpsProject();
        this.propertyValue = propertyValue;

        try {
            propTrue = valueSet.containsValue(Boolean.TRUE.toString(), ipsProject);
            propFalse = valueSet.containsValue(Boolean.FALSE.toString(), ipsProject);
            propNull = valueSet.containsValue(null, ipsProject);
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    public boolean isTrueEnabled() {
        try {
            return valueSet.containsValue(Boolean.TRUE.toString(), ipsProject);
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    public boolean isFalseEnabled() {
        try {
            return valueSet.containsValue(Boolean.FALSE.toString(), ipsProject);
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    public boolean isNullEnabled() {
        try {
            boolean result = valueSet.containsValue(null, ipsProject);
            return result;
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    public boolean isTrue() {
        return propTrue;
    }

    public void setTrue(boolean b) {
        propTrue = b;
        modifyValue(Boolean.TRUE.toString(), b);
    }

    public boolean isFalse() {
        return propFalse;
    }

    public void setFalse(boolean b) {
        propFalse = b;
        modifyValue(Boolean.FALSE.toString(), b);
    }

    public boolean isNull() {
        return propNull;
    }

    public void setNull(boolean b) {
        propNull = b;
        modifyValue(null, b);
    }

    private void modifyValue(String value, boolean add) {
        try {
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
        boolean needsToBeModified = (Boolean.TRUE.toString().equals(currentValue) && !isTrueEnabled())
                || (Boolean.FALSE.toString().equals(currentValue) && !isFalseEnabled())
                || (currentValue == null && !isNullEnabled());
        if (needsToBeModified) {
            if (isTrueEnabled()) {
                propertyValue.setValue(Boolean.TRUE.toString());
            } else if (isFalseEnabled()) {
                propertyValue.setValue(Boolean.FALSE.toString());
            } else if (isNullEnabled()) {
                propertyValue.setValue(null);
            }
        }
    }
}