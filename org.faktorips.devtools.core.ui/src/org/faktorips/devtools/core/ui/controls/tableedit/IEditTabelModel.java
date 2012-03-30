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

/**
 * The model for an {@link EditTableControlViewer}. Provides methods for adding, removing and
 * swapping elements. Those methods are called to add, remove or reorder the table model's elements,
 * e.g. when the user clicks the {@link EditTableControlViewer}'s buttons.
 * 
 * @see EditTableControlViewer
 * @since 3.7
 * 
 * @author Stefan Widmaier
 */
public interface IEditTabelModel {

    public void swapElements(int index1, int index2);

    public Object addElement();

    public void removeElement(int index);

}
