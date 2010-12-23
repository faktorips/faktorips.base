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

package org.faktorips.devtools.core.ui.views.modeldescription;

import java.util.ArrayList;
import java.util.List;

public class DescriptionItem {

    private String name;

    private String description;

    private List<DescriptionItem> children = new ArrayList<DescriptionItem>(0);

    public DescriptionItem() {
        // Empty default constructor
    }

    public DescriptionItem(String name, String description) {
        this.description = description;
        this.name = name;
    }

    public DescriptionItem(String name, List<DescriptionItem> children) {
        this.description = ""; //$NON-NLS-1$
        this.name = name;
        this.children = children;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setChildren(List<DescriptionItem> children) {
        if (children == null) {
            this.children = new ArrayList<DescriptionItem>(0);
        }
        this.children = children;
    }

    public List<DescriptionItem> getChildren() {
        return children;
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

}
