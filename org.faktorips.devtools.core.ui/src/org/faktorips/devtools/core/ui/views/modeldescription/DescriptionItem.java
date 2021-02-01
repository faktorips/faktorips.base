/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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
