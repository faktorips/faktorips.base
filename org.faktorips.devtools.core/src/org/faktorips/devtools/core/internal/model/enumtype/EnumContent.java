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

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.enumtype.EnumContentValidations;
import org.faktorips.devtools.core.model.enumtype.IEnumContent;
import org.faktorips.devtools.core.model.enumtype.IEnumType;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;

/**
 * Implementation of IEnumContent, see the corresponding interface for more details.
 * 
 * @see org.faktorips.devtools.core.model.enumtype.IEnumContent
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumContent extends EnumValueContainer implements IEnumContent {

    // The enum type this enum values is build upon
    private String enumType;

    /**
     * Creates a new enum content.
     * 
     * @param file The ips source file in which this enum content will be stored in.
     */
    public EnumContent(IIpsSrcFile file) {
        super(file);

        this.enumType = null;
    }

    /**
     * {@inheritDoc}
     */
    public IpsObjectType getIpsObjectType() {
        return IpsObjectType.ENUM_CONTENT;
    }

    /**
     * {@inheritDoc}
     */
    public IEnumType findEnumType() throws CoreException {
        IIpsSrcFile[] enumTypeSrcFiles = getIpsProject().findIpsSrcFiles(IpsObjectType.ENUM_TYPE);
        for (IIpsSrcFile currentIpsSrcFile : enumTypeSrcFiles) {
            if (currentIpsSrcFile.getIpsObject().getQualifiedName().equals(enumType)) {
                return (IEnumType)currentIpsSrcFile.getIpsObject();
            }
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void setEnumType(String enumType) {
        ArgumentCheck.notNull(enumType);

        String oldEnumType = this.enumType;
        this.enumType = enumType;
        valueChanged(oldEnumType, enumType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initFromXml(Element element, Integer id) {
        enumType = element.getAttribute(PROPERTY_ENUM_TYPE);

        super.initFromXml(element, id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);

        element.setAttribute(PROPERTY_ENUM_TYPE, enumType);
    }

    /**
     * {@inheritDoc}
     */
    public String getEnumType() {
        return enumType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);

        list.add(EnumContentValidations.validateEnumType(this, enumType, ipsProject));
    }

}
