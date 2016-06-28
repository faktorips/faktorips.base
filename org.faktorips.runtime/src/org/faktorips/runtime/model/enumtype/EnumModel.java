/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.runtime.model.enumtype;

import org.faktorips.runtime.model.annotation.IpsExtensionProperties;
import org.faktorips.runtime.modeltype.internal.AbstractModelElement;
import org.faktorips.runtime.modeltype.internal.DocumentationType;
import org.faktorips.runtime.util.MessagesHelper;

public class EnumModel extends AbstractModelElement {

    public static final String KIND_NAME = "EnumType";

    public EnumModel(String name, IpsExtensionProperties extensionProperties) {
        super(name, extensionProperties);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected String getMessageKey(DocumentationType messageType) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected MessagesHelper getMessageHelper() {
        // TODO Auto-generated method stub
        return null;
    }

}
