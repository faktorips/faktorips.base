/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.runtime.modeltype.internal;

import org.faktorips.runtime.model.annotation.IpsExtensionProperties;
import org.faktorips.runtime.modeltype.IModelType;
import org.faktorips.runtime.util.MessagesHelper;

/**
 * Abstract class for any part of a {@link IModelType}.
 */
public abstract class ModelPart extends AbstractModelElement {

    private final ModelType modelType;

    public ModelPart(String name, ModelType parent, IpsExtensionProperties extensionProperties) {
        super(name, extensionProperties);
        this.modelType = parent;
    }

    /**
     * Returns the parent {@link ModelType}
     */
    public ModelType getModelType() {
        return modelType;
    }

    /**
     * Returns the name of the parent model type.
     */
    protected String getTypeName() {
        return getModelType().getName();
    }

    @Override
    protected String getMessageKey(DocumentationType messageType) {
        return messageType.getKey(getTypeName(), getName());
    }

    @Override
    protected MessagesHelper getMessageHelper() {
        return getModelType().getMessageHelper();
    }

}
