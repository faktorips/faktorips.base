/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.testextensions;

import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.SimpleCustomValidationForProductCmptAttributeValue;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

public class TestExtensionCustomValidation extends SimpleCustomValidationForProductCmptAttributeValue {

    public TestExtensionCustomValidation() {
        super("TestExtension_CustomValidation");
    }

    @Override
    public ValidationResult validate(String value, IIpsProject ipsProject) throws IpsException {
        if (value != null && !value.isBlank()) {
            if (!value.contains("@")) {
                return SimpleCustomValidationForProductCmptAttributeValue.newError("not-valid-string",
                        "The string does not contain an @");
            }
        }
        return null;
    }
}
