/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.generators;

import java.util.Objects;

/**
 * A LayoutResource is referenced by a page, but not included in the page. <br>
 * If the documentation is layouted in html, then e.g. images and stylesheets are external
 * resources, which are not part of the html-file. <br>
 * A LayoutResource consists of an identifying and describing name and the content of this resource
 * as an byte[].
 * 
 * @author dicker
 * 
 */
public class LayoutResource {
    private String name;
    private byte[] content;

    public LayoutResource(String name, byte[] data) {
        super();
        this.name = name;
        content = data;
    }

    public String getName() {
        return name;
    }

    public byte[] getContent() {
        return content;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        LayoutResource other = (LayoutResource)obj;
        return Objects.equals(name, other.name);
    }
}
