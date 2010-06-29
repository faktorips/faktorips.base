/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.productcmpt.deltaentries;

import org.faktorips.devtools.core.internal.model.productcmpt.GenerationToTypeDelta;
import org.faktorips.devtools.core.model.productcmpt.DeltaType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;

/**
 * 
 * @author Jan Ortmann
 */
public class LinkWithoutAssociationEntry extends AbstractDeltaEntry {

    private IProductCmptLink link;

    public LinkWithoutAssociationEntry(GenerationToTypeDelta delta, IProductCmptLink link) {
        super(delta);
        this.link = link;
    }

    /**
     * Returns the link that is missing the association.
     */
    public IProductCmptLink getLink() {
        return link;
    }

    @Override
    public void fix() {
        link.delete();
    }

    @Override
    public DeltaType getDeltaType() {
        return DeltaType.LINK_WITHOUT_ASSOCIATION;
    }

    @Override
    public String getDescription() {
        return getDeltaType() + ": " + link.getAssociation(); //$NON-NLS-1$
    }

}
