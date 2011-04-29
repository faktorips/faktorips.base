/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.search;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.search.ui.text.Match;
import org.faktorips.devtools.core.model.IIpsElement;

public class IpsElementMatch extends Match {

    public List<IIpsElement> pathToRoot;

    public IpsElementMatch(IIpsElement element) {
        super(element, 0, 0);

        initialize();
    }

    private void initialize() {
        pathToRoot = new ArrayList<IIpsElement>();

        pathToRoot.add(getMatchedIpsElement());

    }

    public IIpsElement getMatchedIpsElement() {
        return (IIpsElement)getElement();
    }

}
