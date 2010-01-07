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

package org.faktorips.devtools.core.internal.model.enums;

import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.internal.model.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.core.model.enums.IEnumAttributeReference;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.util.ArgumentCheck;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of <tt>IEnumAttributeReference</tt>, see the corresponding interface for more
 * details.
 * 
 * @see org.faktorips.devtools.core.model.enums.IEnumAttributeReference
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.4
 */
public class EnumAttributeReference extends AtomicIpsObjectPart implements IEnumAttributeReference {

    /**
     * Creates a new <tt>IEnumAttributeReference</tt>.
     * 
     * @param parent The <tt>IEnumContent</tt> this <tt>IEnumAttributeReference</tt> belongs to.
     * @param id A unique ID for this <tt>IEnumAttributeReference</tt>.
     */
    public EnumAttributeReference(IEnumContent parent, int id) {
        super(parent, id);
    }

    @Override
    public void setName(String name) {
        ArgumentCheck.notNull(name);

        String oldName = this.name;
        this.name = name;
        valueChanged(oldName, name);
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(XML_TAG);
    }

    @Override
    public void initFromXml(Element element) {
        name = element.getAttribute(PROPERTY_NAME);

        super.initFromXml(element);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);

        element.setAttribute(PROPERTY_NAME, name);
    }

    @Override
    public boolean isDescriptionChangable() {
        return false;
    }

}
