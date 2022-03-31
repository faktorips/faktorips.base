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
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;

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
        if (getIpsObject() != null) {
            // use of
            // org.faktorips.devtools.core.model.enums.IEnumType.findAllEnumAttributes(boolean,
            // IIpsProject) would lead to displaying the overwritten description as desired in
            // https://jira.faktorzehn.de/browse/FIPS-4372, but to an empty description in all other
            // cases, as descriptions are not automatically inherited.
            List<IEnumAttribute> enumAttributtes = findAllEnumAttributesIncludeSupertypeOriginals(getIpsObject());
            for (IEnumAttribute enumAttributte : enumAttributtes) {
                createDescriptionItem(enumAttributte, descriptions);
            }
        }
        return descriptions;
    }

    private List<IEnumAttribute> findAllEnumAttributesIncludeSupertypeOriginals(IEnumType enumType) {
        IIpsProject ipsProject = enumType.getIpsProject();
        ArgumentCheck.notNull(ipsProject);

        List<IEnumAttribute> attributesList = new ArrayList<>(enumType.getEnumAttributes(true));
        for (IEnumType superEnumType : enumType.findAllSuperEnumTypes(ipsProject)) {
            attributesList.addAll(superEnumType.getEnumAttributes(true));
        }
        return attributesList;
    }

    @Override
    public IEnumType getIpsObject() {
        return (IEnumType)super.getIpsObject();

    }

}
