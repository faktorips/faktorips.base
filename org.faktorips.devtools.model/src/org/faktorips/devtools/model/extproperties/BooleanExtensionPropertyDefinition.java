/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.extproperties;

import org.faktorips.devtools.model.util.XmlUtil;
import org.w3c.dom.Element;

/**
 * Implementation of <code>IExtensionPropertyDefinition</code> for extension properties of type
 * Boolean.
 * 
 * @author Jan Ortmann
 */
public class BooleanExtensionPropertyDefinition extends ExtensionPropertyDefinition {

    /**
     * Empty constructor needed because of Eclipse extension point mechanism.
     */
    public BooleanExtensionPropertyDefinition() {
        super();
    }

    @Override
    public Object getValueFromXml(Element valueElement) {
        String content = XmlUtil.getCDATAorTextContent(valueElement);
        return content == null ? Boolean.FALSE : Boolean.valueOf(content);
    }

    @Override
    public Object getValueFromString(String value) {
        return value;
    }

    @Override
    public void setDefaultValue(String s) {
        setDefaultValue(Boolean.valueOf(s));
    }

}
