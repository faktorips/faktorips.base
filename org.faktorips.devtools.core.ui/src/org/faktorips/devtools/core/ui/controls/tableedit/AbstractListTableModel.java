/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.controls.tableedit;

import java.util.List;

/**
 * Base class for list based table models. The {@link #swapElements(int, int)} method is implemented
 * by this class.
 * <p>
 * Provides the method {@link #validate(Object)} for validating elements of this model. The validate
 * ensures each value is unique in this model.
 * 
 * @author Stefan Widmaier
 */
public abstract class AbstractListTableModel<T> implements IEditTabelModel {

    private final List<T> list;

    public AbstractListTableModel(List<T> list) {
        this.list = list;
    }

    @Override
    public void swapElements(int index1, int index2) {
        T element1 = list.get(index1);
        list.set(index1, list.get(index2));
        list.set(index2, element1);
    }

    @Override
    public List<T> getElements() {
        return list;
    }

}
