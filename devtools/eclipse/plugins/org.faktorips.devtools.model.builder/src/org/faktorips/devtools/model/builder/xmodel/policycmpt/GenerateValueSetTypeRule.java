/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.model.builder.xmodel.policycmpt;

import org.faktorips.devtools.model.builder.xmodel.policycmpt.XPolicyAttribute.GenerateValueSetType;

public class GenerateValueSetTypeRule {

    private final GenerateValueSetType fromMethod;

    private final boolean fromOverride;
    private final boolean fromDeprecated;

    private final boolean delegate;

    public GenerateValueSetTypeRule(GenerateValueSetType fromMethod,
            boolean fromOverride, boolean fromDeprecated, boolean delegate) {
        super();
        this.fromMethod = fromMethod;
        this.fromOverride = fromOverride;
        this.fromDeprecated = fromDeprecated;
        this.delegate = delegate;
    }

    public GenerateValueSetType getFromMethod() {
        return fromMethod;
    }

    public boolean isFromOverride() {
        return fromOverride;
    }

    public boolean isFromDeprecated() {
        return fromDeprecated;
    }

    public boolean isDelegate() {
        return delegate;
    }
}
