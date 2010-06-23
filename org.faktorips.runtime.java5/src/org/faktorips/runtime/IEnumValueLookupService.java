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

package org.faktorips.runtime;

import java.util.List;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * A service that can look up the values for exactly one enumeration type in a data source that is
 * not the Faktor-IPS runtime repository. For example, a data source could be a database table, an
 * XML file or a web service.
 * <p>
 * A lookup service can be registered with the repository. The repository uses the service to look
 * up the values for the enumeration type specified by {@link #getEnumTypeClass()}. Clients of the
 * repository can access enumeration values without knowing whether they are stored in the
 * Faktor-IPS runtime repository or come from another data source.
 * 
 * @author Jan Ortmann
 * 
 * @see IRuntimeRepository#addEnumValueLookupService(IEnumValueLookupService)
 * @see IRuntimeRepository#getEnumValues(Class)
 * @see IRuntimeRepository#getEnumValue(Class, Object)
 */
public interface IEnumValueLookupService<T> {

    /**
     * Returns the enumeration class, e.g. org.foo.PaymentMode.
     */
    public Class<T> getEnumTypeClass();

    /**
     * Returns the enumeration values. So the return type is a list, it is expected that every value
     * is contained only once. {@link IRuntimeRepository#getEnumValues(Class)} will return the
     * values in the order defined by this list. The runtime repository does NOT cache the values,
     * this is the responsibility of the lookup service as it depends on the kind of data if caching
     * is ok, or when the data needs to be refreshed.
     */
    public List<T> getEnumValues();

    /**
     * Returns the value identified by the given id or <code>null</code> if no value exists with
     * that id.
     */
    public T getEnumValue(Object id);

    /**
     * Returns an XmlAdapter that should be used for marshaling/unmarshaling data with JAXB for the
     * enumeration class this is a lookup service for. Returns <code>null</code> if it is not
     * required to use a special XmlAdapter instance. In most cases you return an XmlAdapter here if
     * the default mechanism provided by Faktor-IPS does not meet your requirements.
     * <p>
     * IMPORT NOTE: It is not enough to just return the XmlAdapter instance here, you also have to
     * specify the XmlAdapter class in the generated code! To understand this, you have to
     * understand how JAXB determines the XMLAdapter to use. For the exact details, please look at
     * the JAXB specification or tutorials.
     * <p>
     * Basically it works like this. If you have enabled the option to generate JAXB annotations,
     * Faktor-IPS generates a default XmlAdapter for each enumeration type that does not contain the
     * values. The adapter is required as for those types no Java Enum is generated, but a Java
     * class implementing the typesafe enumeration pattern. This generated class has an annotation
     * that specifies that the generated XmlAdapter should be used by default for all attributes
     * using the enumeration type as data type. For example given the enumeration PaymentMode,
     * Faktor-IPS generates the adapter PaymentModeXmlAdapter and the JAXB annotation in the class
     * PaymentMode would be @XmlJavaTypeAdapter(PaymentModeXmlAdapter.class)
     * <p>
     * The easiest option to use your own adapter is to change the tag @generated to @generated NOT
     * and change the annotation, for example to @XmlJavaTypeAdapter(MyPaymentModeXmlAdapter.class)
     * Why isn't this enough? It could be, it depends on your requirements. By default JAXB creates
     * an instance of your XmlAdapter class (MyPaymentModeXmlAdapter) by using the default
     * constructor. When unmarshalling the XML, you basically get the identifier stored in the XML
     * and have to return an instance of your enumeration class. In most cases you need the (this)
     * enum value lookup service to do so. The easiest thing to get a reference to this service is
     * by returning an XmlAdapter that has a reference to it, right here. If you create a new
     * {@link javax.xml.bind.JAXBContext} with {@link IRuntimeRepository#newJAXBContext()} the
     * adapter returned by this method is added to the {@link javax.xml.bind.Marshaller} and
     * {@link javax.xml.bind.Unmarshaller} created by the <code>JAXBContext</code> via
     * {@link javax.xml.bind.JAXBContext#createMarshaller()} and
     * {@link javax.xml.bind.JAXBContext#createUnmarshaller()}
     * 
     * @see IRuntimeRepository#newJAXBContext()
     * @see javax.xml.bind.JAXBContext#createMarshaller()
     * @see javax.xml.bind.JAXBContext#createUnmarshaller()
     * @see javax.xml.bind.Marshaller#setAdapter(XmlAdapter)
     * @see javax.xml.bind.Unmarshaller#setAdapter(XmlAdapter)
     */
    public XmlAdapter<?, T> getXmlAdapter();

}
