/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.xmodel.productcmpt;

import org.faktorips.devtools.model.builder.xmodel.GeneratorModelContext;
import org.faktorips.devtools.model.builder.xmodel.ModelService;
import org.faktorips.devtools.model.builder.xmodel.XAttribute;
import org.faktorips.devtools.model.type.IAttribute;

/**
 * This class provides a model node for multi value attribute that does not overwrite the data type.
 * Hence this attribute node handles the data type just like for single value attributes.
 * <p>
 * Use this model node if you need the single value data type of a multi value attribute.
 * 
 * @author dirmeier
 */
public class XSingleValueOfMultiValueAttribute extends XAttribute {

    public XSingleValueOfMultiValueAttribute(IAttribute attribute, GeneratorModelContext context,
            ModelService modelService) {
        super(attribute, context, modelService);
    }

}
