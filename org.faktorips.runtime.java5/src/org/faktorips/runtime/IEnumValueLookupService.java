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

/**
 * A service that can look up the values for exactly one enumeration type in a data source
 * that is not the Faktor-IPS runtime repository. For example, a data source could be a database table,
 * an xml file or a webservice.
 * <p>
 * A lookup service can be registered with the repository. The repository uses the service
 * to look up the values for the enumeration type specified by {@link #getEnumTypeClass()}.
 * Clients of the repository can access enum values without knowing whether they are stored in the
 * Faktor-IPS runtime repository or come from another data source.
 * 
 * @author Jan Ortmann
 * 
 * @see IRuntimeRepository#addEnumValueLookupService(IEnumValueLookupService)
 * @see IRuntimeRepository#getEnumValues(Class)
 * @see IRuntimeRepository#getEnumValue(Class, Object) 
 */
public interface IEnumValueLookupService<T extends IEnumValue> {

    /**
     * Returns the enumeration class, e.g. org.foo.PaymentMode. 
     * @return
     */
    public Class<T> getEnumTypeClass();

    /**
     * Returns the enumeration values. So the return type is a list, it is expected that every value
     * is contained only once. {@link IRuntimeRepository#getEnumValues(Class) will return the values in
     * the order defined by this list. The runtime repository does NOT cache the values, this is the 
     * responsibility of the lookup service as it depends on the kind of data if caching is ok, or when
     * the data needs to be refreshed.
     */
    public List<T> getEnumValues();

    /**
     * Returns the value identified by the given id or <code>null</code> if no value exists with that id.
     */
    public T getEnumValue(Object id);

}
