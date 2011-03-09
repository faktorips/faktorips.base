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
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IAttribute;

/**
 * A page for presenting the properties of a {@link IPolicyCmptType}. This page is connected to a
 * Editor similar to the outline view.
 * 
 * @author Quirin Stoll
 */
public class PolicyCmtTypeDescriptionPage extends DefaultModelDescriptionPage {

    public PolicyCmtTypeDescriptionPage(IPolicyCmptType policyCmptType) {
        super();
        setIpsObject(policyCmptType);
        setDescriptionData();
    }

    @Override
    protected List<DescriptionItem> createDescriptions() throws CoreException {
        List<DescriptionItem> descriptions = new ArrayList<DescriptionItem>();
        List<IAttribute> attributes = getIpsObject().findAllAttributes(getIpsObject().getIpsProject());
        for (IAttribute attribute : attributes) {
            createDescriptionItem(attribute, descriptions);
        }
        List<IAssociation> associations = getIpsObject().findAllAssociations(getIpsObject().getIpsProject());
        for (IAssociation association : associations) {
            createDescriptionItem(association, descriptions);
        }
        return descriptions;
    }

    @Override
    public IPolicyCmptType getIpsObject() {
        return (IPolicyCmptType)super.getIpsObject();
    }

}
