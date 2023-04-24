package org.faktorips.runtime.testrepository.testenum.produkt;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.List;

import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.IRuntimeRepositoryLookup;
import org.faktorips.runtime.IpsEnumToXmlWriter;
import org.faktorips.runtime.annotation.IpsGenerated;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.faktorips.runtime.model.annotation.IpsDocumented;
import org.faktorips.runtime.model.annotation.IpsEnumAttribute;
import org.faktorips.runtime.model.annotation.IpsEnumType;
import org.faktorips.runtime.model.annotation.IpsExtensibleEnum;
import org.faktorips.runtime.xml.IToXmlSupport;
import org.faktorips.values.ListUtil;
import org.w3c.dom.Element;

/**
 *
 * @generated
 */
@IpsDocumented(bundleName = "org.faktorips.runtime.testrepository.testenum.testEnum-label-and-descriptions", defaultLocale = "en")
@IpsEnumType(name = "produkt.SuperExtensibleEnumWithModell", attributeNames = { "id", "name" })
@IpsExtensibleEnum(enumContentName = "produkt.SuperExtensibleEnumWithModell")
public final class SuperExtensibleEnumWithModell
implements AbstractExtensibleEnum, Serializable, Comparable<SuperExtensibleEnumWithModell>, IToXmlSupport {

    /**
     * Die SerialVersionUID.
     *
     * @generated
     */
    public static final long serialVersionUID = 2L;
    /**
     * @generated
     */
    public static final SuperExtensibleEnumWithModell SUPER_EXTENSIBLE_ENUM_1 = new SuperExtensibleEnumWithModell(0,
            "1", "super:extensible_enum_1");
    /**
     * Konstante für alle Werte die im Aufzählungstyp definiert wurden. Die Werte könnten durch
     * weitere Werte aus dem Runtime Repository ergänzt werden.
     *
     * @generated
     */
    public static final List<SuperExtensibleEnumWithModell> VALUES = ListUtil.unmodifiableList(SUPER_EXTENSIBLE_ENUM_1);
    /**
     * @generated
     */
    private final int index;
    /**
     * @generated
     */
    private final String id;
    /**
     * @generated
     */
    private final String name;
    /**
     * @generated
     */
    private final IRuntimeRepository productRepository;

    /**
     * Erzeugt eine neue Instanz von SuperExtensibleEnumWithModell.
     *
     * @param productRepository Das Runtime Repository wird zum laden anderer Aufzaehlungsinhalte
     *            und bei der De-/Serialisierung verwendet.
     *
     * @generated
     */
    @IpsGenerated
    protected SuperExtensibleEnumWithModell(int index, String idString, String nameString,
            IRuntimeRepository productRepository) {
        this.index = index;
        id = idString;
        name = nameString;
        this.productRepository = productRepository;
    }

    /**
     * Erzeugt eine neue Instanz von SuperExtensibleEnumWithModell.
     *
     * @generated
     */
    @IpsGenerated
    public SuperExtensibleEnumWithModell(int index, String id, String name) {
        this.index = index;
        this.id = id;
        this.name = name;
        productRepository = null;
    }

    /**
     * Gibt den Wert des Attributs id zurück.
     *
     * @generated
     */
    @IpsEnumAttribute(name = "id", identifier = true, unique = true)
    @Override
    @IpsGenerated
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
    @IpsGenerated
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     *
     * @generated
     */
    @Override
    @IpsGenerated
    public String toString() {
        return "SuperExtensibleEnumWithModell: " + id + '(' + name + ')';
    }

    /**
     * {@inheritDoc}
     *
     * @generated
     */
    @Override
    @IpsGenerated
    public boolean equals(Object obj) {
        if (obj instanceof SuperExtensibleEnumWithModell) {
            return getId().equals(((SuperExtensibleEnumWithModell)obj).getId());
        }
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @generated
     */
    @Override
    @IpsGenerated
    public int hashCode() {
        return getId().hashCode();
    }

    /**
     * Diese Methode darf nicht entfernt werden. Sie wird vom Runtime Repository (ueber Java
     * Reflection) aufgerufen, um einen Aufzaehlungstyp zu identifizieren.
     *
     * @generated
     */
    @IpsGenerated
    Object getEnumValueId() {
        return id;
    }

    /**
     * @generated
     */
    @Override
    @IpsGenerated
    public int compareTo(SuperExtensibleEnumWithModell o) {
        return index - o.index;
    }

    /**
     * {@inheritDoc}
     *
     * @generated
     */
    @Override
    @IpsGenerated
    public void writePropertiesToXml(Element element) {
        ValueToXmlHelper.addValueToElement(id, element, IpsEnumToXmlWriter.XML_ELEMENT_ENUMATTRIBUTEVALUE);
        ValueToXmlHelper.addValueToElement(name, element, IpsEnumToXmlWriter.XML_ELEMENT_ENUMATTRIBUTEVALUE);
    }

    /**
     * @generated
     */
    @IpsGenerated
    private Object writeReplace() {
        return new SerializationProxy(id, getRepositoryLookup());
    }

    /**
     * @generated
     */
    @IpsGenerated
    private IRuntimeRepositoryLookup getRepositoryLookup() {
        if (productRepository != null) {
            IRuntimeRepositoryLookup runtimeRepositoryLookup = productRepository.getRuntimeRepositoryLookup();
            if (runtimeRepositoryLookup == null) {
                throw new IllegalStateException(
                        "For serialization of SuperExtensibleEnumWithModell instances you need to set an IRuntimeRepositoryLookup in your runtime repository.");
            }
            return runtimeRepositoryLookup;
        } else {
            return null;
        }
    }

    /**
     * @generated
     */
    @IpsGenerated
    private void readObject(@SuppressWarnings("unused") ObjectInputStream s) throws IOException {
        throw new InvalidObjectException("SerializationProxy required");
    }

    /**
     * @generated
     */
    private static class SerializationProxy implements Serializable {
        private static final long serialVersionUID = 1L;

        private final String id;
        private final IRuntimeRepositoryLookup runtimeRepositoryLookup;

        /**
         * @generated
         */
        @IpsGenerated
        SerializationProxy(String id, IRuntimeRepositoryLookup runtimeRepositoryLookup) {
            this.id = id;
            this.runtimeRepositoryLookup = runtimeRepositoryLookup;
        }

        /**
         * @generated
         */
        @IpsGenerated
        private Object readResolve() {
            return runtimeRepositoryLookup.getRuntimeRepository().getEnumValue(SuperExtensibleEnumWithModell.class, id);
        }
    }
}
