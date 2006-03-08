/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.productstructureexplorer;

import org.faktorips.devtools.core.model.product.IProductCmpt;

/**
 * Primitive helper-class to avoid problems with the TreeViewer if an object is given as root which can be displayed directly.
 * If the ContentProvider returns this object directly, an endless recursion of this item as its own child is displayed. So these 
 * objects are encapsulated with these class to force different objects.
 * 
 * @author Thorsten Guenther
 */
public class DummyRoot {
    public IProductCmpt data;

}
