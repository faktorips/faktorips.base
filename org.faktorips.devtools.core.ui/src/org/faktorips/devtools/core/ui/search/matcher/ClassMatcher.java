/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search.matcher;

import java.util.Set;

import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;

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
