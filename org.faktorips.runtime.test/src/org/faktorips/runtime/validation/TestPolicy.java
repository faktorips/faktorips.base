/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.runtime.validation;

import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.model.annotation.IpsAllowedValues;
import org.faktorips.runtime.model.annotation.IpsAttribute;
import org.faktorips.runtime.model.annotation.IpsAttributeSetter;
import org.faktorips.runtime.model.annotation.IpsAttributes;
import org.faktorips.runtime.model.annotation.IpsConfiguredAttribute;
import org.faktorips.runtime.model.annotation.IpsDocumented;
import org.faktorips.runtime.model.annotation.IpsPolicyCmptType;
import org.faktorips.runtime.model.type.AttributeKind;
import org.faktorips.runtime.model.type.ValueSetKind;
import org.faktorips.valueset.UnrestrictedValueSet;
import org.faktorips.valueset.ValueSet;

@IpsPolicyCmptType(name = "TestPolicy")
@IpsAttributes({ "IntegerAttribute" })
@IpsDocumented(bundleName = "org.faktorips.runtime.validation.TestPolicy", defaultLocale = "de")
public class TestPolicy implements IModelObject {

    public static final String PROPERTY_INTEGER_ATTRIBUTE = "IntegerAttribute";

    private Integer integerAttribute;
    private ValueSet<Integer> allowedValuesForIntegerAttribute = new UnrestrictedValueSet<>();

    @IpsAttribute(name = "IntegerAttribute", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.Range)
    @IpsConfiguredAttribute(changingOverTime = true)
    public Integer getIntegerAttribute() {
        return integerAttribute;
    }

    @IpsAttributeSetter("IntegerAttribute")
    public void setIntegerAttribute(Integer i) {
        integerAttribute = i;
    }

    @IpsAllowedValues("IntegerAttribute")
    public ValueSet<Integer> getAllowedValuesForIntegerAttribute() {
        return allowedValuesForIntegerAttribute;
    }

    public void setAllowedValuesForIntegerAttribute(ValueSet<Integer> allowedValuesForIntegerAttribute) {
        this.allowedValuesForIntegerAttribute = allowedValuesForIntegerAttribute;
    }

    @Override
    public MessageList validate(IValidationContext context) {
        return null;
    }
}