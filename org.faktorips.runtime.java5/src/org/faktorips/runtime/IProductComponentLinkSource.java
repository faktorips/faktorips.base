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

package org.faktorips.runtime;

/**
 * The source of an {@link IProductComponentLink}. This may be a {@link IProductComponent} or a
 * {@link IProductComponentGeneration}.
 * 
 */
public interface IProductComponentLinkSource {

    /**
     * Getting the runtime repository of this link source.
     * 
     * @return The {@link IRuntimeRepository} that was used to create this
     *         {@link IProductComponentLinkSource}
     */
    public IRuntimeRepository getRepository();

}
