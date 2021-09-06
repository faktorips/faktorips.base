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

import java.util.Locale;

import org.faktorips.devtools.model.IPreSaveProcessor;
import org.faktorips.devtools.model.ipsobject.IDescription;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;

public class TestPreSaveProcessor implements IPreSaveProcessor {

    @Override
    public void process(IIpsObject ipsObject) {
        IDescription description = ipsObject.getDescription(Locale.GERMAN);
        description.setText(description.getText().toUpperCase());
    }

    @Override
    public IpsObjectType getIpsObjectType() {
        return IpsObjectType.PRODUCT_CMPT;
    }

}
