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

package org.faktorips.devtools.htmlexport.helper.filter;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * {@link IpsElementFilter}, which accepts all {@link IIpsElement}s within the given
 * {@link IIpsProject}
 * 
 * @author dicker
 */
public class IpsObjectInProjectFilter implements IpsElementFilter {

    private final IIpsProject ipsProject;

    public IpsObjectInProjectFilter(IIpsProject ipsProject) {
        super();
        this.ipsProject = ipsProject;
    }

    @Override
    public boolean accept(IIpsElement element) {
        return ipsProject.equals(element.getIpsProject());
    }

}
