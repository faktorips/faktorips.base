/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.modeldescription;

import org.faktorips.devtools.core.internal.model.pctype.Attribute;
import org.faktorips.devtools.core.model.pctype.IAttribute;

public class DescriptionViewItem {
    private IAttribute attribute;
    
    public String getAttributeName() {
        return attribute.getName();
    }

    public void setAttributeName(String attributeName) {
        attribute.setName(attributeName);
    }

    public String getDescription() {
        return attribute.getDescription();
    }

    public void setDescription(String description) {
        attribute.setDescription(description);
    }

    /**
     * @param name
     * @param description
     */
    public DescriptionViewItem(String attributeName, String description) {
        attribute = new Attribute();
        attribute.setDescription(description);
        attribute.setName(attributeName);
    }
    
    public DescriptionViewItem(IAttribute attr) {
        attribute = attr;
    }

}
