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

package org.faktorips.devtools.core.ui.search.model;

import java.util.Set;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;

/**
 * ClassMatcher tests, whether an IIpsElement matches one of the given classes.
 * 
 * @author dicker
 */
public class ClassMatcher {
    private final Set<Class<? extends IIpsObjectPart>> clazzes;

    public ClassMatcher(Set<Class<? extends IIpsObjectPart>> clazzes) {
        this.clazzes = clazzes;
    }

    public boolean isMatchingClass(IIpsElement element) {
        for (Class<? extends IIpsObjectPart> clazz : clazzes) {
            if (clazz.isAssignableFrom(element.getClass())) {
                return true;
            }
        }
        return false;
    }
}
