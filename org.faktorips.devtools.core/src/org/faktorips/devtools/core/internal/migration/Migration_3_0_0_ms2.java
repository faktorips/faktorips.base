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

package org.faktorips.devtools.core.internal.migration;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumLiteralNameAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * Migration from version 3.0.0.ms2 to version 3.0.0.ms3. The XML structure of
 * {@link org.faktorips.devtools.core.model.enums.IEnumType} has changed a little bit if the
 * enumeration type contains values directly. In this case, literal names are stored in the
 * enumeration values. For the storage of these values, a new XML tag is used.
 * 
 * @author Alexander Weickmann
 */
public class Migration_3_0_0_ms2 extends DefaultMigration {

    public Migration_3_0_0_ms2(IIpsProject projectToMigrate, String featureId) {
        super(projectToMigrate, featureId);
    }

    @Override
    protected boolean migrate(IFile file) throws CoreException {
        return false;
    }

    @Override
    protected void migrate(IIpsSrcFile srcFile) throws CoreException {
        if (srcFile.getIpsObjectType().equals(IpsObjectType.ENUM_TYPE)) {
            IEnumType enumType = (IEnumType)srcFile.getIpsObject();
            if (enumType.isContainingValues()) {
                int literalIndex = enumType.getIndexOfEnumLiteralNameAttribute();
                if (literalIndex == -1) {
                    return;
                }

                /*
                 * In every enumeration value we delete the old attribute value for the literal name
                 * and replace it with a new one.
                 */
                for (IEnumValue enumValue : enumType.getEnumValues()) {
                    IEnumAttributeValue literalNameAttributeValue = enumValue.getEnumAttributeValues()
                            .get(literalIndex);
                    String literal = literalNameAttributeValue.getValue();
                    literalNameAttributeValue.delete();
                    IEnumLiteralNameAttributeValue newLiteralNameAttributeValue = enumValue
                            .newEnumLiteralNameAttributeValue();
                    newLiteralNameAttributeValue.setValue(literal);

                    // Move the new attribute value to it's correct position.
                    int newIndex = enumValue.getIndexOfEnumAttributeValue(newLiteralNameAttributeValue);
                    if (newIndex != literalIndex) {
                        boolean up = (newIndex > literalIndex) ? true : false;
                        int difference = Math.abs(newIndex - literalIndex);
                        for (int i = 0; i < difference; i++) {
                            enumValue.moveEnumAttributeValue(newLiteralNameAttributeValue, up);
                        }
                    }
                }

                enumType.clearUniqueIdentifierValidationCache();
            }
        }
    }

    @Override
    public String getDescription() {
        return "The XML structure of Enumeration Types containing values directly has changed."; //$NON-NLS-1$
    }

    @Override
    public String getTargetVersion() {
        return "3.0.0.ms3"; //$NON-NLS-1$
    }

}
