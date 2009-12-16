/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.tablestructure;

import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.internal.model.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.ITableAccessFunction;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class TableAccessFunction extends AtomicIpsObjectPart implements ITableAccessFunction {

    private String accessedColumn;
    private String type;
    private String[] argTypes = new String[0];

    // hides field in supertype. Done to avoid update-events fired on description changes.
    private String description;

    public TableAccessFunction(IIpsObject parent, int id) {
        super(parent, id);
    }

    public TableAccessFunction(IIpsObjectPart parent, int id) {
        super(parent, id);
    }

    public TableAccessFunction() {
        super();
    }

    public ITableStructure getTableStructure() {
        return (ITableStructure)getParent();
    }

    @Override
    protected Element createElement(Document doc) {
        return null;
    }

    @Override
    public String getName() {
        return getTableStructure().getName() + '.' + getAccessedColumn();
    }

    public String getAccessedColumn() {
        return accessedColumn;
    }

    public void setAccessedColumn(String columnName) {
        accessedColumn = columnName;
    }

    public IColumn findAccessedColumn() {
        return getTableStructure().getColumn(accessedColumn);
    }

    public String getType() {
        return type;
    }

    public void setType(String newType) {
        type = newType;
    }

    public void setArgTypes(String[] types) {
        // make a defensive copy.
        argTypes = new String[types.length];
        System.arraycopy(types, 0, argTypes, 0, types.length);
    }

    public String[] getArgTypes() {
        String[] types = new String[argTypes.length];
        System.arraycopy(argTypes, 0, types, 0, argTypes.length);
        return types; // return defensive copy
    }

    public Image getImage() {
        return null;
    }

    /**
     * Overridden to avoid change-notifications on description changes. This method is necessary,
     * because we hide the field holding the description in supertype. {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * Overridden to avoid change-notifications on description changes. {@inheritDoc}
     */
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

}
