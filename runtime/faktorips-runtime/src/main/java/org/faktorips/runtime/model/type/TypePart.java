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

import java.util.Calendar;
import java.util.Locale;
import java.util.Optional;

import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
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

    /**
     * Validates this part's configuration in the given product against the model.
     *
     * @param list a {@link MessageList}, to which validation messages may be added
     * @param context the {@link IValidationContext}, needed to determine the {@link Locale} in
     *            which to create {@link Message Messages}
     * @param product the {@link IProductComponent} to validate
     * @param effectiveDate the date that determines which {@link IProductComponentGeneration} is to
     *            be validated, if the {@link IProductComponent} has any and this part
     *            {@link #isChangingOverTime() is changing over time}
     *
     * @since 25.1
     */
    public void validate(MessageList list,
            IValidationContext context,
            IProductComponent product,
            Calendar effectiveDate) {
        // nothing to validate here.
    }

    /**
     * Checks whether this part is changing over time (resides in the generation) or not (resides in
     * the product component).
     *
     * @return whether this association is changing over time
     *
     * @since 25.1
     */
    public abstract boolean isChangingOverTime();

}
