/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsobject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.core.runtime.PlatformObject;
import org.faktorips.devtools.model.ipsobject.IFixDifferencesComposite;

/**
 * An abstract implementation of {@link IFixDifferencesComposite} handling the list of possible
 * children.
 * 
 * @author dirmeier
 */
public abstract class AbstractFixDifferencesComposite extends PlatformObject implements IFixDifferencesComposite {

    private final List<IFixDifferencesComposite> children = new ArrayList<>();

    @Override
    public final boolean isEmpty() {
        if (!isEmptyThis()) {
            return false;
        }
        for (IFixDifferencesComposite child : getChildren()) {
            if (!child.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * This method should return true if this element is empty no matter whether the chilren are
     * empty or not
     * 
     * @return true if this element does not contains any fixes
     */
    protected abstract boolean isEmptyThis();

    @Override
    public final void fixAllDifferencesToModel() {
        fix();
        for (IFixDifferencesComposite child : getChildren()) {
            child.fixAllDifferencesToModel();
        }
    }

    /**
     * Fix all differences in this element. Do not call the fix method of any children because they
     * are fixed by {@link #fixAllDifferencesToModel()}
     */
    protected abstract void fix();

    @Override
    public List<IFixDifferencesComposite> getChildren() {
        return new CopyOnWriteArrayList<>(children);
    }

    /**
     * Adding a {@link IFixDifferencesComposite} to the list of children. Should be final because it
     * may be called in constructor of subclasses.
     * 
     * @param child The new child
     * @return true if the list add returns true
     */
    public final boolean addChild(IFixDifferencesComposite child) {
        return children.add(child);
    }

    /**
     * Removing a {@link IFixDifferencesComposite} to the list of children. Should be final because
     * it may be called in constructor of subclasses.
     * 
     * @param child The child to be removed
     * @return true if the list remove returns true
     */
    public final boolean removeChild(IFixDifferencesComposite child) {
        return children.remove(child);
    }

}
