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

import java.util.HashMap;
import java.util.Map;

import org.faktorips.runtime.model.annotation.IpsDocumented;
import org.faktorips.runtime.model.annotation.IpsEnumAttribute;
import org.faktorips.runtime.model.annotation.IpsEnumType;

/**
 *
 * @generated
 */
@IpsDocumented(bundleName = "org.faktorips.sample.model.model-label-and-descriptions", defaultLocale = "en")
@IpsEnumType(name = "TestConcreteJavaEnum", attributeNames = { "id", "name" })
public enum TestConcreteJavaEnum implements TestAbstractEnum {
    /**
     * @generated
     */
    JAVA_VALUE_1("J1", "Java Value 1"),
    /**
     * @generated
     */
    JAVA_VALUE_2("J2", "Java Value 2")

    ;

    /**
     * Eine Map um schnell zu einer ID auf den passenden Enum-Wert zu bekommen.
     *
     * @generated
     */
    private static final Map<String, TestConcreteJavaEnum> ID_MAP;
    /**
     * In diesem static Block wird die ID-Map mit allen Enum-Werten initialisiert.
     *
     * @generated
     */
    static {
        ID_MAP = new HashMap<>();
        for (TestConcreteJavaEnum value : values()) {
            ID_MAP.put(value.id, value);
        }
    }

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
     * Erzeugt eine neue Instanz von TestConcreteJavaEnum.
     *
     * @generated
     */
    private TestConcreteJavaEnum(String id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Gibt den Aufzählungswert für den angegebenen Parameter <code>id</code> zurueck. Gibt
     * <code>null</code> zurück, wenn kein entsprechender Aufzählungswert gefunden wird, oder der
     * Parameter <code>id</code> <code>null</code> ist.
     *
     * @generated
     */
    public static final TestConcreteJavaEnum getValueById(String id) {
        return ID_MAP.get(id);
    }

    /**
     * Gibt den Aufzählungswert für den angegebenen Parameter <code>name</code> zurueck. Gibt
     * <code>null</code> zurück, wenn kein entsprechender Aufzählungswert gefunden wird, oder der
     * Parameter <code>name</code> <code>null</code> ist.
     *
     * @generated
     */
    public static final TestConcreteJavaEnum getValueByName(String name) {
        for (TestConcreteJavaEnum currentValue : values()) {
            if (currentValue.name.equals(name)) {
                return currentValue;
            }
        }
        return null;
    }

    /**
     * Gibt den Aufzählungswert für den angegebenen Parameter <code>id</code> zurück. Falls für den
     * gegebenen Parameter kein entsprechender Aufzählungswert gefunden wird, wird eine
     * {@link IllegalArgumentException} geworfen.
     *
     * @throws IllegalArgumentException falls kein passender Aufzählungswert existiert
     *
     * @generated
     */
    public static final TestConcreteJavaEnum getExistingValueById(String id) {
        if (ID_MAP.containsKey(id)) {
            return ID_MAP.get(id);
        } else {
            throw new IllegalArgumentException("No enum value with id " + id);
        }
    }

    /**
     * Gibt den Aufzählungswert für den angegebenen Parameter <code>name</code> zurück. Falls für
     * den gegebenen Parameter kein entsprechender Aufzählungswert gefunden wird, wird eine
     * {@link IllegalArgumentException} geworfen.
     *
     * @throws IllegalArgumentException falls kein passender Aufzählungswert existiert
     *
     * @generated
     */
    public static final TestConcreteJavaEnum getExistingValueByName(String name) {
        for (TestConcreteJavaEnum currentValue : values()) {
            if (currentValue.name.equals(name)) {
                return currentValue;
            }
        }
        throw new IllegalArgumentException("No enum value with name " + name);
    }

    /**
     * Gibt <code>true</code> zurueck, falls der Parameterwert einen Wert dieser Aufzählung
     * identifiziert.
     *
     * @generated
     */
    public static final boolean isValueById(String id) {
        return getValueById(id) != null;
    }

    /**
     * Gibt <code>true</code> zurueck, falls der Parameterwert einen Wert dieser Aufzählung
     * identifiziert.
     *
     * @generated
     */
    public static final boolean isValueByName(String name) {
        return getValueByName(name) != null;
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
        return "TestConcreteJavaEnum: " + id + '(' + name + ')';
    }
}
