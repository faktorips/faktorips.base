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

package org.faktorips.devtools.core.ui.search.matcher;

import java.util.Set;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;

/**
 * The ClassMatcher tests, whether an IIpsElement matches given classes.
 * <p>
 * The Constructor {@link #ClassMatcher(Set)} gets a Set of {@link IIpsObjectPart}. The class of a
 * matching IIpsElements or a superclass must be a member of this Set.
 * 
 * 
 * @author dicker
 */
public class ClassMatcher implements IMatcher<IIpsElement> {

    private final Set<Class<? extends IIpsObjectPart>> clazzes;

    public ClassMatcher(Set<Class<? extends IIpsObjectPart>> clazzes) {
        this.clazzes = clazzes;
    }

    @Override
    public boolean isMatching(IIpsElement element) {
        for (Class<? extends IIpsObjectPart> clazz : clazzes) {
            if (clazz.isAssignableFrom(element.getClass())) {
                return true;
            }
        }
        return false;
    }
}
