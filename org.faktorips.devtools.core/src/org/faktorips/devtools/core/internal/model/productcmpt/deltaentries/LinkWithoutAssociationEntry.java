/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.productcmpt.deltaentries;

import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.productcmpt.DeltaType;
import org.faktorips.devtools.core.model.productcmpt.IDeltaEntry;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;

/**
 * 
 * @author Jan Ortmann
 */
public class LinkWithoutAssociationEntry implements IDeltaEntry {

    private final IProductCmptLink link;

    public LinkWithoutAssociationEntry(IProductCmptLink link) {
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
        return getDeltaType() + ": " + IpsPlugin.getMultiLanguageSupport().getLocalizedCaption(link); //$NON-NLS-1$
    }

}
