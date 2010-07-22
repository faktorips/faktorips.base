/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.runtime.internal;

import java.util.Map;

import org.faktorips.runtime.IModelObject;

/**
 * Callback interface for XML initialization.
 * 
 * @author Joerg Ortmann
 */
public interface XmlCallback {
    /**
     * Callback method will be called for each node element inside the XML structure.
     * 
     * @param pathFromAggregateRoot The path starting on the aggregates root node element
     * @param modelObject The model object which is currently initialized via XML
     * @param propMap Map containing all node properties
     */
    public void initProperties(String pathFromAggregateRoot, IModelObject modelObject, Map<String, String> propMap);
}
