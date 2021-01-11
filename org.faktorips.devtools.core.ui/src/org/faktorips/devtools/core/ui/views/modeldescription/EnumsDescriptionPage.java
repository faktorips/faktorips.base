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

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumType;

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
    protected List<DescriptionItem> createDescriptions() throws CoreException {
        List<DescriptionItem> descriptions = new ArrayList<DescriptionItem>();
        if (getIpsObject() != null) {
            // use of
            // org.faktorips.devtools.core.model.enums.IEnumType.findAllEnumAttributes(boolean,
            // IIpsProject) would lead to displaying the overwritten description as desired in
            // https://jira.faktorzehn.de/browse/FIPS-4372, but to an empty description in all other
            // cases, as descriptions are not automatically inherited.
            @SuppressWarnings("deprecation")
            List<IEnumAttribute> enumAttributtes = getIpsObject().findAllEnumAttributesIncludeSupertypeOriginals(true,
                    getIpsObject().getIpsProject());
            for (IEnumAttribute enumAttributte : enumAttributtes) {
                createDescriptionItem(enumAttributte, descriptions);
            }
        }
        return descriptions;
    }

    @Override
    public IEnumType getIpsObject() {
        return (IEnumType)super.getIpsObject();

    }

}
