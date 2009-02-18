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
import java.util.NoSuchElementException;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.ipsobject.BaseIpsObject;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPartCollection;
import org.faktorips.devtools.core.model.enumtype.IEnumValue;
import org.faktorips.devtools.core.model.enumtype.IEnumValueContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.util.ArgumentCheck;

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

    /**
     * {@inheritDoc}
     */
    // TODO rename to count
    public int getEnumValuesCount() {
        return enumValues.size();
    }

    /**
     * {@inheritDoc}
     */
    public int moveEnumValueUp(IEnumValue enumValue) throws CoreException {
        ArgumentCheck.notNull(enumValue);

        // Can't move further up any more
        if (enumValue == enumValues.getPart(0)) {
            return getIndexOfEnumValue(enumValue);
        }

        return moveEnumValue(enumValue, true);
    }

    /**
     * {@inheritDoc}
     */
    public int moveEnumValueDown(IEnumValue enumValue) throws CoreException {
        ArgumentCheck.notNull(enumValue);

        // Can't move further down any more
        if (enumValue == enumValues.getPart(enumValues.size() - 1)) {
            return getIndexOfEnumValue(enumValue);
        }

        return moveEnumValue(enumValue, false);
    }

    /*
     * Moves the given enum value up or down in the collection order by 1 and returns the new index
     */
    @SuppressWarnings("unchecked")
    private int moveEnumValue(IEnumValue enumValue, boolean up) throws CoreException {
        List<IEnumValue> enumValuesList = enumValues.getBackingList();
        for (int i = 0; i < enumValuesList.size(); i++) {
            IEnumValue currentEnumValue = enumValuesList.get(i);
            if (currentEnumValue == enumValue) {
                int[] newIndex = enumValues.moveParts(new int[] { i }, up);
                return newIndex[0];
            }
        }

        throw new NoSuchElementException();
    }

    /**
     * {@inheritDoc}
     */
    public int getIndexOfEnumValue(IEnumValue enumValue) {
        ArgumentCheck.notNull(enumValue);

        for (int i = 0; i < enumValues.size(); i++) {
            if (enumValues.getBackingList().get(i) == enumValue) {
                return i;
            }
        }

        throw new NoSuchElementException();
    }

}
