/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.modeldescription;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
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
        IpsPlugin.getDefault().getIpsModel().addChangeListener(this);
        setDescriptionData();
    }

    @Override
    protected List<DescriptionItem> createDescriptions() throws CoreException {
        List<DescriptionItem> descriptions = new ArrayList<DescriptionItem>();
        if (getIpsObject() != null) {
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
