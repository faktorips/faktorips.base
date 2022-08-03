/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.adapter;

import org.eclipse.core.runtime.IAdapterFactory;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.type.IType;

/**
 * An abstract class for IPS adapter factories, providing standard adaptation methods.
 */
public abstract class AbstractIpsAdapterFactory implements IAdapterFactory {

    /**
     * Getting the {@link IIpsObject} of an {@link IIpsSrcFile} if the file is of this type.
     */
    protected IIpsObject adaptToIpsObject(IIpsSrcFile file) {
        if (file == null) {
            return null;
        }
        try {
            return file.getIpsObject();
        } catch (IpsException e) {
            IpsPlugin.log(e);
            return null;
        }
    }

    /**
     * Getting the {@link IType} of an {@link IIpsSrcFile} if the file is of this type.
     */
    protected IType adaptToType(IIpsSrcFile file) {
        if (file == null) {
            return null;
        }

        IpsObjectType type = file.getIpsObjectType();
        if (IpsObjectType.PRODUCT_CMPT_TYPE.equals(type) || IpsObjectType.POLICY_CMPT_TYPE.equals(type)) {
            try {
                return (IType)file.getIpsObject();
            } catch (IpsException e) {
                IpsPlugin.log(e);
            }
        }
        return null;
    }

    /**
     * Getting the IProductCmpt of an {@link IIpsSrcFile} if the file is of type
     * {@link IpsObjectType#PRODUCT_CMPT} or {@link IpsObjectType#PRODUCT_TEMPLATE}.
     */
    protected IProductCmpt adaptToProductCmpt(IIpsSrcFile file) {
        if (file == null) {
            return null;
        }
        IpsObjectType fileObjectType = file.getIpsObjectType();
        if (IpsObjectType.PRODUCT_CMPT.equals(fileObjectType)
                || IpsObjectType.PRODUCT_TEMPLATE.equals(fileObjectType)) {
            try {
                return (IProductCmpt)file.getIpsObject();
            } catch (IpsException e) {
                IpsPlugin.log(e);
            }
        }
        return null;
    }

}
