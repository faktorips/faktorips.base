/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.model.type;

import java.util.Optional;

import org.faktorips.runtime.model.annotation.IpsExtensionProperties;
import org.faktorips.runtime.util.MessagesHelper;

/**
 * Abstract class for any part of a {@link Type}.
 */
public abstract class TypePart extends ModelElement {

    private final Type type;

    public TypePart(String name, Type parent, IpsExtensionProperties extensionProperties,
            Optional<Deprecation> deprecation) {
        super(name, extensionProperties, deprecation);
        type = parent;
    }

    /**
     * Returns the parent {@link Type}
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns the parent {@link Type}
     * 
     * @deprecated Use {@link #getType()}
     */
    @Deprecated
    public Type getModelType() {
        return getType();
    }

    /**
     * Returns the name of the parent model type.
     */
    protected String getTypeName() {
        return getModelType().getName();
    }

    @Override
    protected String getMessageKey(DocumentationKind messageType) {
        return messageType.getKey(getTypeName(), getType().getKindName(), getName());
    }

    @Override
    protected MessagesHelper getMessageHelper() {
        return getType().getMessageHelper();
    }

}
