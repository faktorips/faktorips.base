/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.builder.flidentifier.ast;

import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.AbstractDatatype;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.MultiLanguageSupport;
import org.faktorips.devtools.core.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.core.util.TextRegion;

/**
 * This node represents the first part of a enum reference, the part that identifies the enum type.
 * It uses a special datatype implementation {@link EnumClass} of {@link Datatype} to store the
 * found enum type. The successor of a {@link EnumClassNode} have to be always of type
 * {@link EnumValueNode}
 * 
 * @author dirmeier
 */
public class EnumClassNode extends IdentifierNode {

    EnumClassNode(EnumClass datatype, TextRegion textRegion) {
        super(datatype, textRegion);
    }

    @Override
    public EnumClass getDatatype() {
        return (EnumClass)super.getDatatype();
    }

    @Override
    public void setSuccessor(IdentifierNode successor) {
        if (successor instanceof EnumValueNode) {
            super.setSuccessor(successor);
        } else {
            throw new RuntimeException("Invalid successor type in enum class node: " + successor); //$NON-NLS-1$
        }
    }

    @Override
    public EnumValueNode getSuccessor() {
        return (EnumValueNode)super.getSuccessor();
    }

    @Override
    public String getText() {
        EnumDatatype enumDatatype = getDatatype().getEnumDatatype();
        return enumDatatype.getName();
    }

    @Override
    public String getDescription(MultiLanguageSupport multiLanguageSupport) {
        if (getDatatype().getEnumDatatype() instanceof EnumTypeDatatypeAdapter) {
            EnumTypeDatatypeAdapter enumTypeDatatypeAdapter = (EnumTypeDatatypeAdapter)getDatatype().getEnumDatatype();
            return getNameAndDescription(enumTypeDatatypeAdapter.getEnumType(), multiLanguageSupport);
        }
        return NLS.bind(Messages.EnumClassNode_description, getText());
    }

    public static class EnumClass extends AbstractDatatype {

        private static final String CLASS_NAME = Class.class.getSimpleName();

        private final EnumDatatype enumDatatype;

        public EnumClass(EnumDatatype enumDatatype) {
            this.enumDatatype = enumDatatype;
        }

        @Override
        public String getName() {
            return CLASS_NAME + "<" + getEnumDatatype().getName() + ">"; //$NON-NLS-1$ //$NON-NLS-2$
        }

        @Override
        public String getQualifiedName() {
            return getName();
        }

        @Override
        public boolean isPrimitive() {
            return false;
        }

        @Override
        public boolean isAbstract() {
            return false;
        }

        @Override
        public boolean isValueDatatype() {
            return false;
        }

        @Override
        public String getJavaClassName() {
            return Class.class.getName();
        }

        public EnumDatatype getEnumDatatype() {
            return enumDatatype;
        }

    }
}
