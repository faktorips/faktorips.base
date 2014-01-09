/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
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
