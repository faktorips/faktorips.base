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

package org.faktorips.devtools.stdbuilder;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.dthelpers.AbstractDatatypeHelper;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.stdbuilder.enumtype.EnumTypeBuilder;
import org.faktorips.util.ArgumentCheck;

/**
 * A datatype helper for {@link IEnumType} which is an implementation of the Datatype interface.
 * This datatype helper uses the {@link EnumTypeBuilder} to get the java class name, the new
 * instance expression as well as the value of expression from it.
 * 
 * @author Peter Kuntz
 */
public class EnumTypeDatatypeHelper extends AbstractDatatypeHelper {

    private EnumTypeBuilder enumTypeBuilder;
    private IEnumType enumType;

    public EnumTypeDatatypeHelper(EnumTypeBuilder enumTypeBuilder, IEnumType enumType) {
        super(enumType);

        ArgumentCheck.notNull(enumTypeBuilder, this);
        ArgumentCheck.notNull(enumType, this);

        this.enumTypeBuilder = enumTypeBuilder;
        this.enumType = enumType;
    }

    /**
     * {@inheritDoc}
     */
    public JavaCodeFragment newInstance(String value) {
        try {

            if (!(enumType.isContainingValues())) {
                throw new IllegalStateException(
                        "This method cannot be called if the values of the enum type of this helper are not "
                                + "part of the composite but instead are contained in an enum content object.");
            }

            IEnumValue enumValue = enumType.findEnumValue(value, enumTypeBuilder.getIpsProject());
            return enumTypeBuilder.getNewInstanceCodeFragement(enumType, enumValue);

        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public JavaCodeFragment nullExpression() {
        return new JavaCodeFragment("null");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getJavaClassName() {
        try {
            return enumTypeBuilder.getQualifiedClassName(enumType.getIpsSrcFile());
        } catch (CoreException e) {
            throw new RuntimeException("An exception occurred while trying to determine the java class name "
                    + "of the enum type: " + enumType.getQualifiedName(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JavaCodeFragment valueOfExpression(String expression) {
        try {
            return enumTypeBuilder.getValueByXXXCodeFragment(enumType.findLiteralNameAttribute(enumTypeBuilder.getIpsProject()), expression);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

}
