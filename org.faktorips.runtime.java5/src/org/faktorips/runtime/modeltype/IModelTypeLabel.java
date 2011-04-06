/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.runtime.modeltype;

import java.util.Locale;

/**
 * @author Alexander Weickmann
 */
public interface IModelTypeLabel extends IModelElement {

    /** The XML tag for this model element. */
    public static final String XML_TAG = "Label";

    /** The XML tag in which tags of this model element are wrapped. */
    public static final String WRAPPER_XML_TAG = "Labels";

    /**
     * Returns the model element this label belongs to.
     */
    public IModelElement getModelElement();

    /**
     * Returns the locale of this label.
     */
    public Locale getLocale();

    /**
     * Returns the value of the label.
     */
    public String getValue();

    /**
     * Returns the plural value of the label.
     */
    public String getPluralValue();

}
