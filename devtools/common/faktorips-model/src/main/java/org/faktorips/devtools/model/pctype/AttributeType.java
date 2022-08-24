/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.pctype;

/**
 * Describes the kind of attribute.
 */
public enum AttributeType {

    /**
     * Defines an attribute as being changeable per policy component instance.
     */
    CHANGEABLE("changeable", Messages.AttributeType_changeable), //$NON-NLS-1$

    /**
     * Defines an attribute as being computed. In contrast to a derived attribute a computed
     * attribute is computed by an explicit method call. E.g. a method calculatePremium() might
     * calculate several computed attributes like netPremium and grossPremium. The computed
     * attributes keep their computed value until their are recalculated by another method call.
     * <p>
     * If a computed attribute is also product relevant the computation formula can be defined by
     * the product developer. The IT developer defines the parameters that the product developer can
     * use.
     */
    DERIVED_BY_EXPLICIT_METHOD_CALL("computed", Messages.AttributeType_derived_by_explicit_method_call), //$NON-NLS-1$

    /**
     * Defines an attribute as being derived, that means the attributes value can be derived from
     * other attribute values. In contrast to computed attributes the value of derived attributes
     * are always calculated on the fly. E.g. the gross premium could be derived on the fly from the
     * net premium and the tax.
     * <p>
     * If a derived attribute is product relevant the computation formula can be defined by the
     * product developer. The IT developer defines the parameters that the product developer can
     * use.
     */
    DERIVED_ON_THE_FLY("derived", Messages.AttributeType_derived_on_the_fly), //$NON-NLS-1$

    /**
     * Defines an attribute as being constant for all policy components that are based on the same
     * product.
     */
    CONSTANT("constant", Messages.AttributeType_constant); //$NON-NLS-1$

    private final String id;
    private final String name;

    AttributeType(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public static final AttributeType getAttributeType(String id) {
        if ("changeable".equals(id)) { //$NON-NLS-1$
            // migration of old files
            return CHANGEABLE;
        }
        for (AttributeType at : values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }

    /**
     * Returns <code>true</code> if this is one of the derived type, otherwise <code>false</code>.
     */
    public boolean isDerived() {
        return this == DERIVED_BY_EXPLICIT_METHOD_CALL || this == DERIVED_ON_THE_FLY;
    }

    /**
     * @return Returns the id.
     */
    public String getId() {
        return id;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }

}
