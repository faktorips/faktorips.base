/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.modeldescription;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumContent;
import org.faktorips.devtools.model.enums.IEnumType;

/**
 * A page for presenting the properties of a {@link IEnumType} or {@link IEnumContent} . This page
 * is connected to a Editor similar to the outline view.
 * 
 * @author Quirin Stoll
 */
public class EnumsDescriptionPage extends DefaultModelDescriptionPage {

    public EnumsDescriptionPage(IEnumType enumType) {
        super();
        setIpsObject(enumType);
        setDescriptionData();
    }

    @Override
    protected List<DescriptionItem> createDescriptions() {
        List<DescriptionItem> descriptions = new ArrayList<>();
        IEnumType enumType = getIpsObject();
        if (enumType != null) {
            descriptions.add(createStructureDescriptionItem());
            List<IEnumAttribute> enumAttributes = enumType.findAllEnumAttributes(true, enumType.getIpsProject());
            for (IEnumAttribute enumAttribute : enumAttributes) {
                createDescriptionItem(enumAttribute, descriptions);
            }
        }
        return descriptions;
    }

    @Override
    public IEnumType getIpsObject() {
        return (IEnumType)super.getIpsObject();

    }

}
