/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.productcmpt;

import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.XmlContentFileCopyBuilder;

/**
 * Copies the product cmpt xml file to the output location (=java source folder).
 */
public class ProductCmptXMLBuilder extends XmlContentFileCopyBuilder {

    public ProductCmptXMLBuilder(IpsObjectType type, StandardBuilderSet builderSet) {
        super(type, builderSet);
    }

    @Override
    public String getName() {
        return "ProductCmptXmlBuilder";
    }
}
