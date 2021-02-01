/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.businessfct;

import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.businessfct.BusinessFunction;
import org.faktorips.devtools.model.internal.ipsobject.IpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.w3c.dom.Element;

// TODO AW: Is this class obsolete?
public class BusinessFunctionImpl extends IpsObject implements BusinessFunction {

    public BusinessFunctionImpl(IIpsSrcFile file) {
        super(file);
    }

    @Override
    public IpsObjectType getIpsObjectType() {
        return IpsObjectType.BUSINESS_FUNCTION;
    }

    @Override
    protected IIpsElement[] getChildrenThis() {
        return new IIpsElement[0];
    }

    @Override
    protected void reinitPartCollectionsThis() {
        // Nothing to do
    }

    @Override
    protected boolean addPartThis(IIpsObjectPart part) {
        return false;
    }

    @Override
    protected boolean removePartThis(IIpsObjectPart part) {
        return false;
    }

    @Override
    protected IIpsObjectPart newPartThis(Element xmlTag, String id) {
        return null;
    }

    @Override
    protected IIpsObjectPart newPartThis(Class<? extends IIpsObjectPart> partType) {
        return null;
    }

}
