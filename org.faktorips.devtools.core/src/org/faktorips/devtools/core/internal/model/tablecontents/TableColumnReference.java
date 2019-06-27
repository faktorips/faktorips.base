/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.tablecontents;

import org.faktorips.devtools.core.internal.model.PartReference;
import org.faktorips.devtools.core.model.IIpsMetaObject;
import org.faktorips.util.ArgumentCheck;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class TableColumnReference extends PartReference {

    /** The XML tag for this IPS object part. */
    static final String XML_TAG = "ColumnTableReference"; //$NON-NLS-1$

    /**
     * Creates a new <tt>TableColumnReference</tt>.
     * 
     * @param parent The <tt>ITableContents</tt> this <tt>TableColumnReference</tt> belongs to.
     * @param id A unique ID for this <tt>TableColumnReference</tt>.
     */
    public TableColumnReference(IIpsMetaObject parent, String id) {
        super(parent, id);
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(XML_TAG);
    }

    @Override
    public void setName(String name) {
        ArgumentCheck.notNull(name);
        this.name = name;
    }

}
