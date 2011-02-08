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
import org.faktorips.devtools.core.ui.editors.pctype.Messages;

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
        IAttribute[] attributes = getIpsObject().findAllAttributes(getIpsObject().getIpsProject());
        List<DescriptionItem> attributeDescriptions = new ArrayList<DescriptionItem>();
        for (IAttribute attribute : attributes) {
            createDescriptionItem(attribute, attributeDescriptions);
        }
        descriptions.add(new DescriptionItem(Messages.AttributesSection_title, attributeDescriptions));
        IAssociation[] associations = getIpsObject().findAllAssociations(getIpsObject().getIpsProject());
        List<DescriptionItem> aDescriptions = new ArrayList<DescriptionItem>();
        for (IAssociation association : associations) {
            createDescriptionItem(association, aDescriptions);
        }
        descriptions.add(new DescriptionItem(Messages.AssociationsSection_title, aDescriptions));
        return descriptions;
    }

    @Override
    public IPolicyCmptType getIpsObject() {
        return (IPolicyCmptType)super.getIpsObject();
    }

}
