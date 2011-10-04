/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.runtime.internal.toc;


/**
 * A {@link TocEntryObject} representing a enum xml adapter element
 * 
 * @author dirmeier
 */
public class EnumXmlAdapterTocEntry extends TocEntryObject {

    public static final String XML_TAG = "EnumXmlAdapter";

    public EnumXmlAdapterTocEntry(String ipsObjectId, String qualifiedName, String implementationClassName) {
        super(ipsObjectId, qualifiedName, "", implementationClassName);
    }

    @Override
    protected String getXmlElementTag() {
        return XML_TAG;
    }

}
