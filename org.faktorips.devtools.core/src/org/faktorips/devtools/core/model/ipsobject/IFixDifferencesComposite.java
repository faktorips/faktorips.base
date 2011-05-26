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

package org.faktorips.devtools.core.model.ipsobject;

import java.util.List;

import org.faktorips.devtools.core.model.IIpsMetaClass;
import org.faktorips.devtools.core.model.IIpsMetaObject;

/**
 * This interface is used for a composite of fix difference elements. The fix difference framework
 * is used to indicate and fix the differences between {@link IIpsMetaObject} and the corresponding
 * {@link IIpsMetaClass}. Differences may happen accrue if the model changes while there are
 * existing product definition objects. In such cases we want to give as much help to the user as
 * possible to get the product definition object in synch with the model.
 * 
 * @author dirmeier
 */
public interface IFixDifferencesComposite {

    /**
     * Returns true if this element as well as every child element is empty. That means there is no
     * work to do, the method {@link #fixAllDifferencesToModel()} would do nothing.
     * 
     * @return true if this element and every child element is empty
     */

    public abstract boolean isEmpty();

    /**
     * Fixes the difference between the type and the product component. This method should fix the
     * differences in this element and call the {@link #fixAllDifferencesToModel()} method in every
     * child element.
     * <p>
     * For example if the type contains a new attribute but the product component generation. has
     * not matching attribute value, this method creates the attribute value.
     */
    public abstract void fixAllDifferencesToModel();

    /**
     * An {@link IFixDifferencesComposite} would have children that also contains fixes. This method
     * getting the containing fix differences elements
     * 
     * @return a list of {@link IFixDifferencesComposite}s that are children of this element
     */
    public List<IFixDifferencesComposite> getChildren();

}