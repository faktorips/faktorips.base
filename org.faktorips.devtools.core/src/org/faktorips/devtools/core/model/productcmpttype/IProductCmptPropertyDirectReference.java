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

package org.faktorips.devtools.core.model.productcmpttype;

import org.faktorips.devtools.core.model.type.IProductCmptProperty;

/**
 * An {@link IProductCmptPropertyReference} to {@link IProductCmptProperty}s that are stored
 * directly in the {@link IProductCmptType}.
 * <p>
 * Direct references directly work on the property object and it's XML is directly written from the
 * in-memory object. This way, the references are automatically updated when the property is renamed
 * (no rename refactoring necessary).
 * 
 * @since 3.6
 * 
 * @author Alexander Weickmann
 */
public interface IProductCmptPropertyDirectReference extends IProductCmptPropertyReference {

    public final static String XML_TAG_NAME = "ProductCmptPropertyDirectReference"; //$NON-NLS-1$

    /**
     * Returns the referenced {@link IProductCmptProperty}.
     */
    public IProductCmptProperty getProductCmptProperty();

}
