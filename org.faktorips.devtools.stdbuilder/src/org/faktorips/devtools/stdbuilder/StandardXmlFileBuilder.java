/***************************************************************************************************
 * Copyright (c) 2005-2008 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 * 
 **************************************************************************************************/

package org.faktorips.devtools.stdbuilder;

import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;

/**
 * Abstract XML file builder that belongs to the Faktor-IPS standard code generator and implements
 * the {@link IIpsStandardArtefactBuilder} interface.
 * 
 * @author Jan Ortmann
 */
public abstract class StandardXmlFileBuilder extends AbstractXmlFileBuilder implements IIpsStandardArtefactBuilder {

    private boolean buildsPublishedArtefacts = false;

    public StandardXmlFileBuilder(IpsObjectType type, IIpsArtefactBuilderSet builderSet) {
        this(type, builderSet, false);
    }

    public StandardXmlFileBuilder(IpsObjectType type, IIpsArtefactBuilderSet builderSet,
            boolean buildsPublishedArtefacts) {
        super(type, builderSet, "");
        this.buildsPublishedArtefacts = buildsPublishedArtefacts;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean buildsPublishedArtefacts() {
        return buildsPublishedArtefacts;
    }

}
