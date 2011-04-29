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

package org.faktorips.devtools.core.ui.search.model.finder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.search.ui.text.Match;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.type.IType;

public abstract class AbstractIpsObjectPartFinder implements IpsObjectPartFinder {

    private final StringMatcher stringMatcher = new StringMatcher();

    @Override
    public List<Match> findMatchingIpsObjectParts(Set<IType> searchedTypes, String searchTerm) {
        List<Match> matches = new ArrayList<Match>();
        for (IType type : searchedTypes) {
            List<? extends IIpsObjectPart> ipsObjectParts = getIpsObjectParts(type);
            for (IIpsObjectPart ipsObjectPart : ipsObjectParts) {
                if (stringMatcher.isMatching(searchTerm, ipsObjectPart.getName())) {
                    matches.add(new Match(ipsObjectPart, 0, 0));
                }
            }
        }
        return matches;
    }

    protected abstract List<? extends IIpsObjectPart> getIpsObjectParts(IType type);

}
