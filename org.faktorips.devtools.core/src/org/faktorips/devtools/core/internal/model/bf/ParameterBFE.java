/***************************************************************************************************
 * Copyright (c) 2005-2008 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 * 
 **************************************************************************************************/

package org.faktorips.devtools.core.internal.model.bf;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.bf.BFElementType;
import org.faktorips.devtools.core.model.bf.IParameterBFE;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.util.ArgumentCheck;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ParameterBFE extends BFElement implements IParameterBFE {

    private String datatype = "";

    public ParameterBFE(IIpsObject parent, int id) {
        super(parent, id);
    }

    public String getDisplayString() {
        return getDatatype() + ":" + getName();
    }

    public String getDatatype() {
        return datatype;
    }

    public void setDatatype(String datatype) {
        ArgumentCheck.notNull(datatype, this);
        String old = this.datatype;
        this.datatype = datatype;
        valueChanged(old, datatype);
    }

    public Datatype findDatatype() throws CoreException {
        return getIpsProject().findDatatype(datatype);
    }

    /**
     * {@inheritDoc}
     */
    protected void initPropertiesFromXml(Element element, Integer id) {
        name = element.getAttribute(PROPERTY_NAME);
        setType(BFElementType.getType(element.getAttribute(PROPERTY_TYPE)));
        datatype = element.getAttribute(PROPERTY_DATATYPE);
    }

    /**
     * {@inheritDoc}
     */
    protected void propertiesToXml(Element element) {
        element.setAttribute(PROPERTY_NAME, name);
        element.setAttribute(PROPERTY_TYPE, getType().getId());
        element.setAttribute(PROPERTY_DATATYPE, datatype);
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(IParameterBFE.XML_TAG);
    }

}
