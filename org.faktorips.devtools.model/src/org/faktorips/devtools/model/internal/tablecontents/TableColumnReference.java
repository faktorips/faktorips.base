/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.tablecontents;

import org.faktorips.devtools.model.IIpsMetaObject;
import org.faktorips.devtools.model.internal.ipsobject.PartReference;
import org.faktorips.util.ArgumentCheck;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class TableColumnReference extends PartReference {

    /** The XML tag for this IPS object part. */
    static final String XML_TAG = "ColumnTableReference"; //$NON-NLS-1$

    /**
     * Creates a new <code>TableColumnReference</code>.
     * 
     * @param parent The <code>ITableContents</code> this <code>TableColumnReference</code> belongs
     *            to.
     * @param id A unique ID for this <code>TableColumnReference</code>.
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
