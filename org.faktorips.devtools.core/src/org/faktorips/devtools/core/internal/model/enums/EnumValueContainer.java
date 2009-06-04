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

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.internal.model.ipsobject.BaseIpsObject;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPartCollection;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.enums.IEnumValueContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;

/**
 * Implementation of <code>IEnumValueContainer</code>, see the corresponding interface for more
 * details.
 * 
 * @see org.faktorips.devtools.core.model.enums.IEnumValueContainer
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public abstract class EnumValueContainer extends BaseIpsObject implements IEnumValueContainer {

    /** Collection containing the enum values. */
    private IpsObjectPartCollection<IEnumValue> enumValues;

    /**
     * Creates a new <code>EnumValueContainer</code>.
     * 
     * @param file The ips source file in which this ips object will be stored in.
     */
    protected EnumValueContainer(IIpsSrcFile file) {
        super(file);

        this.enumValues = new IpsObjectPartCollection<IEnumValue>(this, EnumValue.class, IEnumValue.class, IEnumValue.XML_TAG);
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
    public IEnumValue findEnumValue(String literalNameAttributeValue, IIpsProject ipsProject) throws CoreException {
        if (literalNameAttributeValue == null) {
            return null;
        }
        for (IEnumValue enumValue : getEnumValues()) {
            IEnumType enumType = findEnumType(ipsProject);
            IEnumAttribute literalNameAttribute = enumType.findLiteralNameAttribute(ipsProject);
            IEnumAttributeValue value = enumValue.findEnumAttributeValue(ipsProject, literalNameAttribute);
            if (value == null) {
                continue;
            }
            if (literalNameAttributeValue.equals(value.getValue())) {
                return enumValue;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public IEnumValue newEnumValue() throws CoreException {
        IEnumType enumType = findEnumType(getIpsProject());

        // Creation not possible if enum type can't be found
        if (enumType == null) {
            return null;
        }

        NewEnumValueRunnable workspaceRunnable = new NewEnumValueRunnable(enumType);
        getIpsModel().runAndQueueChangeEvents(workspaceRunnable, null);

        return workspaceRunnable.newEnumValue;
    }

    /**
     * {@inheritDoc}
     */
    public int getEnumValuesCount() {
        return enumValues.size();
    }

    /**
     * {@inheritDoc}
     */
    public int moveEnumValue(IEnumValue enumValue, boolean up) throws CoreException {
        ArgumentCheck.notNull(enumValue);

        if (up) {
            // Can't move further up any more
            if (enumValue == enumValues.getPart(0)) {
                return getIndexOfEnumValue(enumValue);
            }
        } else {
            // Can't move further down any more
            if (enumValue == enumValues.getPart(enumValues.size() - 1)) {
                return getIndexOfEnumValue(enumValue);
            }
        }

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

    /**
     * {@inheritDoc}
     */
    public void clear() {
        enumValues.clear();
        objectHasChanged();
    }

    /**
     * Creates a new enum value with as many enum attribute values as enum attributes in the enum
     * type referenced by this enum value container.
     * <p>
     * These operations are atomic and therefore need to be batched into a runanble.
     */
    private class NewEnumValueRunnable implements IWorkspaceRunnable {

        /** The enum type referenced by this enum value container. */
        private IEnumType enumType;

        /** Handle to the enum value to be created. */
        private IEnumValue newEnumValue;

        /** Creates the <code>NewEnumAttributeRunnable</code>. */
        public NewEnumValueRunnable(IEnumType enumType) {
            this.enumType = enumType;
            this.newEnumValue = null;
        }

        /**
         * {@inheritDoc}
         */
        public void run(IProgressMonitor monitor) throws CoreException {
            // Create new enum value
            newEnumValue = (IEnumValue)newPart(IEnumValue.class);

            // Add as many enum attribute values as there are enum attributes in the enum type
            for (int i = 0; i < enumType.getEnumAttributesCount(true); i++) {
                newEnumValue.newEnumAttributeValue();
            }

            /*
             * Set the value of each enum attribute value to null if the datatype of the referenced
             * enum attribute is not String
             */
            for (IEnumAttributeValue currentEnumAttributeValue : newEnumValue.getEnumAttributeValues()) {
                IIpsProject ipsProject = currentEnumAttributeValue.getIpsProject();
                if (!(currentEnumAttributeValue.findEnumAttribute(ipsProject).getDatatype().equals(Datatype.STRING
                        .getQualifiedName()))) {
                    currentEnumAttributeValue.setValue(null);
                }
            }
        }

    }

}
