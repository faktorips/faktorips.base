/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.bf;

import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.internal.bf.BusinessFunction;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;

/**
 * The IPS object type for a business function. A reference to this class is made in an extension of
 * the extension-point <code>org.faktorips.devtools.core.ipsobjecttype</code>
 * 
 * @author Peter Erzberger
 */
public class BusinessFunctionIpsObjectType extends IpsObjectType {

    public static final String ID = "org.faktorips.devtools.bf.model.BusinessFunction"; //$NON-NLS-1$

    public BusinessFunctionIpsObjectType() {
        super(ID, "BusinessFunction", // XML element name $NON-NLS-1$ //$NON-NLS-1$
                Messages.BusinessFunctionIpsObjectType_displayName, // display name
                Messages.BusinessFunctionIpsObjectType_displayNamePlural, // display name plural
                "ipsbusinessfunction", false, false, BusinessFunction.class); //$NON-NLS-1$
    }

    /**
     * Returns the unique instance of this class.
     */
    public static final BusinessFunctionIpsObjectType getInstance() {
        return (BusinessFunctionIpsObjectType)IIpsModel.get().getIpsObjectType(ID);
    }

    @Override
    public IIpsObject newObject(IIpsSrcFile file) {
        return new BusinessFunction(file);
    }

}
