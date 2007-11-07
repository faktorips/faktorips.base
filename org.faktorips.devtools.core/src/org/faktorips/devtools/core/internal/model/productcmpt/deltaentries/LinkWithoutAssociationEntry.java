/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpt.deltaentries;

import org.faktorips.devtools.core.internal.model.productcmpt.GenerationToTypeDelta;
import org.faktorips.devtools.core.model.productcmpt.DeltaType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpttype.ProdDefPropertyType;

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

    /**
     * {@inheritDoc}
     */
    public void fix() {
        link.delete();
    }

    /**
     * {@inheritDoc}
     */
    public DeltaType getDeltaType() {
        return DeltaType.LINK_WITHOUT_ASSOCIATION;
    }

    /**
     * {@inheritDoc}
     */
    public String getPropertyName() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public ProdDefPropertyType getPropertyType() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String getDescription() {
        return getDeltaType() + ": " + link.getAssociation(); //$NON-NLS-1$
    }

}
