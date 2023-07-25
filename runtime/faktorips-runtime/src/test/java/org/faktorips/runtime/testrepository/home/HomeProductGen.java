/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.testrepository.home;

import org.faktorips.runtime.testrepository.PnCProductGen;
import org.w3c.dom.Element;

public class HomeProductGen extends PnCProductGen {

    public HomeProductGen(HomeProduct productCmpt) {
        super(productCmpt);
    }

    @Override
    protected void writePropertiesToXml(Element generationElement) {
        // no properties to write
    }

}
