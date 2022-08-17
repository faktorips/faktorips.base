/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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
    void initProperties(String pathFromAggregateRoot, IModelObject modelObject, Map<String, String> propMap);
}
