/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsobject.extensionpropertyrepresentation;

import org.faktorips.devtools.core.model.extproperties.ExtensionPropertyDefinition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class ExtensionPropertyRepresentation extends ExtensionPropertyDefinition {

    public abstract void saveElementInXML(Document doc, Element extPropertiesEl);

    @Override
    public void setDefaultValue(String s) {
        setDefaultValue((Object)s);
    }

    @Override
    public Object getValueFromString(String value) {
        return getDefaultValue().toString();
    }
}
