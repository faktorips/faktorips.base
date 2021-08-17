/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.sample.model;

import java.io.Serializable;
import java.util.List;

import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.model.annotation.IpsDocumented;
import org.faktorips.runtime.model.annotation.IpsEnumAttribute;
import org.faktorips.runtime.model.annotation.IpsEnumType;
import org.faktorips.runtime.model.annotation.IpsExtensibleEnum;
import org.faktorips.values.ListUtil;

/**
 *
 * @generated
 */
@IpsDocumented(bundleName = "org.faktorips.sample.model.model-label-and-descriptions", defaultLocale = "en")
@IpsEnumType(name = "TestConcreteExtensibleEnum", attributeNames = { "id", "name" })
@IpsExtensibleEnum(enumContentName = "TestConcreteExtensibleEnum")
public final class TestConcreteExtensibleEnum
        implements TestAbstractEnum, Serializable, Comparable<TestConcreteExtensibleEnum> {

    /**
     * Die SerialVersionUID.
     *
     * @generated
     */
    public static final long serialVersionUID = 1L;
    /**
     * @generated
     */
    public static final TestConcreteExtensibleEnum CLASS_VALUE_1 = new TestConcreteExtensibleEnum(0, "C1",
            "Class Value 1");
    /**
     * @generated
     */
    public static final TestConcreteExtensibleEnum CLASS_VALUE_2 = new TestConcreteExtensibleEnum(1, "C2",
            "Class Value 2");
    /**
     * Konstante für alle Werte die im Aufzählungstyp definiert wurden. Die Werte könnten durch
     * weitere Werte aus dem Runtime Repository ergänzt werden.
     *
     * @generated
     */
    public static final List<TestConcreteExtensibleEnum> VALUES = ListUtil.unmodifiableList(CLASS_VALUE_1,
            CLASS_VALUE_2);
    /**
     * @generated
     */
    private final int index;
    /**
     *
     * @generated
     */
    private final String id;
    /**
     *
     * @generated
     */
    private final String name;

    /**
     * Erzeugt eine neue Instanz von TestConcreteExtensibleEnum.
     * 
     * @param productRepository Das Runtime Repository wird zum laden anderer Aufzählungsinhalte
     *            verwendet.
     *
     * @generated
     */
    protected TestConcreteExtensibleEnum(int index, String idString, String nameString,
            IRuntimeRepository productRepository) {
        this.index = index;
        this.id = idString;
        this.name = nameString;
    }

    /**
     * Erzeugt eine neue Instanz von TestConcreteExtensibleEnum.
     *
     * @generated
     */
    public TestConcreteExtensibleEnum(int index, String id, String name) {
        this.index = index;
        this.id = id;
        this.name = name;
    }

    /**
     * Gibt den Wert des Attributs id zurück.
     *
     * @generated
     */
    @IpsEnumAttribute(name = "id", identifier = true, unique = true)
    @Override
    public String getId() {
        return id;
    }

    /**
     * Gibt den Wert des Attributs name zurück.
     *
     * @generated
     */
    @IpsEnumAttribute(name = "name", unique = true, displayName = true)
    @Override
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     *
     * @generated
     */
    @Override
    public String toString() {
        return "TestConcreteExtensibleEnum: " + id + '(' + name + ')';
    }

    /**
     * {@inheritDoc}
     *
     * @generated
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TestConcreteExtensibleEnum) {
            return this.getId().equals(((TestConcreteExtensibleEnum)obj).getId());
        }
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @generated
     */
    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    /**
     * Diese Methode darf nicht entfernt werden. Sie wird vom Runtime Repository (ueber Java
     * Reflection) aufgerufen, um einen Aufzaehlungstyp zu identifizieren.
     *
     * @generated
     */
    Object getEnumValueId() {
        return id;
    }

    /**
     * @generated
     */
    @Override
    public int compareTo(TestConcreteExtensibleEnum o) {
        return index - o.index;
    }
}
