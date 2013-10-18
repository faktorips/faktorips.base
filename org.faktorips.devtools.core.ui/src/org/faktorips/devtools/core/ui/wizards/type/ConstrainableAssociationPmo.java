/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.type;

import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.binding.PresentationModelObject;

public class ConstrainableAssociationPmo extends PresentationModelObject {

    public static final String PROPERTY_SELECTED_ASSOCIATION = "selectedAssociation";
    public static final String PROPERTY_SELECTED_TARGET_TEXT = "selectedTargetText";

    private IAssociation selectedAssociation;

    public ConstrainableAssociationPmo() {
        super();
    }

    public String getSelectedTargetText() {
        return "";
    }

    public void setSelectedTargetText(IType target) {

    }

    public IAssociation getSelectedAssociation() {
        return selectedAssociation;
    }

    public void setSelectedAssociation(IAssociation selectedAssociation) {
        this.selectedAssociation = selectedAssociation;
    }

}
