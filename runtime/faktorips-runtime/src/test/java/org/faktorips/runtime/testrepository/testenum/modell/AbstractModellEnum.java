package org.faktorips.runtime.testrepository.testenum.modell;

import org.faktorips.runtime.annotation.IpsGenerated;
import org.faktorips.runtime.model.annotation.IpsDocumented;
import org.faktorips.runtime.model.annotation.IpsEnumAttribute;
import org.faktorips.runtime.model.annotation.IpsEnumType;

/**
 *
 * @generated
 */
@IpsDocumented(bundleName = "org.faktorips.runtime.testrepository.testenum.testEnum-label-and-descriptions", defaultLocale = "en")
@IpsEnumType(name = "modell.AbstractModellEnum", attributeNames = { "id", "name" })
public interface AbstractModellEnum {
    /**
     * Gibt den Wert des Attributs id zurück.
     *
     * @generated
     */
    @IpsEnumAttribute(name = "id", identifier = true, unique = true)
    @IpsGenerated
    String getId();

    /**
     * Gibt den Wert des Attributs name zurück.
     *
     * @generated
     */
    @IpsEnumAttribute(name = "name", unique = true, displayName = true)
    @IpsGenerated
    String getName();
}
