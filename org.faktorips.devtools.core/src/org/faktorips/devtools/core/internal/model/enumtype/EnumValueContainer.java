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

package org.faktorips.devtools.core.internal.model.enumtype;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.core.internal.model.ipsobject.BaseIpsObject;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPartCollection;
import org.faktorips.devtools.core.model.enumtype.IEnumValue;
import org.faktorips.devtools.core.model.enumtype.IEnumValueContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;

/**
 * Implementation of IEnumValueContainer, see the corresponding interface for more details.
 * 
 * @see org.faktorips.devtools.core.model.enumtype.IEnumValueContainer
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public abstract class EnumValueContainer extends BaseIpsObject implements IEnumValueContainer {

    // Collection containing the enum values
    private IpsObjectPartCollection enumValues;

    /**
     * Creates a new enum value container.
     * 
     * @param file The ips source file in which this ips object will be stored in.
     */
    protected EnumValueContainer(IIpsSrcFile file) {
        super(file);

        this.enumValues = new IpsObjectPartCollection(this, EnumValue.class, IEnumValue.class, IEnumValue.XML_TAG);
    }

    /**
     * {@inheritDoc}
     */
    public List<IEnumValue> getEnumValues() {
        List<IEnumValue> valuesList = new ArrayList<IEnumValue>();
        IIpsObjectPart[] parts = enumValues.getParts();
        for (IIpsObjectPart currentObjectPart : parts) {
            valuesList.add((IEnumValue)currentObjectPart);
        }

        return valuesList;
    }

    /**
     * {@inheritDoc}
     */
    public IEnumValue newEnumValue() {
        return (IEnumValue)newPart(IEnumValue.class);
    }

    /**
     * {@inheritDoc}
     */
    public IEnumValue getEnumValue(int id) {
        return (IEnumValue)enumValues.getPartById(id);
    }

}
