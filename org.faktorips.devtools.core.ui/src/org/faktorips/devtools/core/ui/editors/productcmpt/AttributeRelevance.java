/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.internal.valueset.RangeValueSet;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpt.IConfiguredValueSet;
import org.faktorips.devtools.model.valueset.IEnumValueSet;
import org.faktorips.devtools.model.valueset.IRangeValueSet;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.devtools.model.valueset.ValueSetType;

/**
 * The relevance of an attribute can be expressed through its {@linkplain IValueSet value set}. An
 * {@linkplain IValueSet#isEmpty() empty set} is considered {@link #Irrelevant}, as no value is
 * allowed. If the value set contains values, {@link #Mandatory} means a value must be set, so
 * {@link IValueSet#isContainsNull()} must be {@code false}, otherwise the attribute is considered
 * {@link #Optional}.
 * <p>
 * This is a helper class modifying a given {@link IConfiguredValueSet} to match a desired relevance
 * setting.
 */
public enum AttributeRelevance {
    Optional {
        @Override
        public void set(IConfiguredValueSet configuredValueSet) {
            nonEmpty(configuredValueSet).setContainsNull(true);
        }
    },
    Mandatory {
        @Override
        public void set(IConfiguredValueSet configuredValueSet) {
            nonEmpty(configuredValueSet).setContainsNull(false);
        }
    },
    Irrelevant {
        @Override
        public void set(IConfiguredValueSet configuredValueSet) {
            IValueSet valueSet = configuredValueSet.getValueSet();
            if (valueSet.isRange()) {
                ((IRangeValueSet)valueSet).setEmpty(true);
            } else {
                IEnumValueSet newValueSet = (IEnumValueSet)configuredValueSet.changeValueSetType(ValueSetType.ENUM);
                newValueSet.removeValues(newValueSet.getValuesAsList());
                newValueSet.setContainsNull(false);
            }
        }
    };

    public abstract void set(IConfiguredValueSet configuredValueSet);

    protected IValueSet nonEmpty(IConfiguredValueSet configuredValueSet) {
        try {
            IPolicyCmptTypeAttribute attribute = configuredValueSet
                    .findPcTypeAttribute(configuredValueSet.getIpsProject());
            IValueSet parentValueSet = attribute.getValueSet();
            ValueDatatype valueSetDatatype = attribute.findDatatype(configuredValueSet.getIpsProject());

            if (configuredValueSet.getValueSet().isEmpty()) {
                if (parentValueSet.isEnum() || Datatype.BOOLEAN.equals(valueSetDatatype)
                        || Datatype.PRIMITIVE_BOOLEAN.equals(valueSetDatatype)) {
                    // force creation of a new EnumValueSet with values from the model
                    configuredValueSet.setValueSetType(ValueSetType.UNRESTRICTED);
                    configuredValueSet.convertValueSetToEnumType();
                } else {
                    if (configuredValueSet.getValueSet().isRange()) {
                        ((IRangeValueSet)configuredValueSet.getValueSet()).setEmpty(false);
                    } else {
                        configuredValueSet.changeValueSetType(parentValueSet.getValueSetType());
                    }
                    if (parentValueSet.isRange()) {
                        RangeValueSet range = (RangeValueSet)configuredValueSet.getValueSet();
                        range.copyPropertiesFrom(parentValueSet);
                    }
                }

            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
        return configuredValueSet.getValueSet();
    }

    public static AttributeRelevance of(IValueSet valueSet) {
        if (valueSet.isEmpty()) {
            return AttributeRelevance.Irrelevant;
        } else {
            return valueSet.isContainsNull() ? AttributeRelevance.Optional : AttributeRelevance.Mandatory;
        }
    }
}