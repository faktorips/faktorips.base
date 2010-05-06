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

package org.faktorips.runtime.internal.toc;

import java.util.TimeZone;

import org.faktorips.runtime.internal.DateTime;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A toc entry that refers to an ips object generation.
 * 
 * @author Jan Ortmann
 */
public class TocEntryGeneration extends TocEntry {

    protected final static String GENERATION_ENTRY_XML_ELEMENT = "Generation";

    private final static TimeZone defaultTimeZone = TimeZone.getDefault();

    public final static TocEntryGeneration createFromXml(IProductCmptTocEntry parent, Element element) {
        DateTime validFrom = DateTime.parseIso(element.getAttribute("validFrom"));
        String className = element.getAttribute("implementationClass");
        String xmlResourceName = element.getAttribute("xmlResource");
        return new TocEntryGeneration(parent, validFrom, className, xmlResourceName);
    }

    private IProductCmptTocEntry parent;
    private DateTime validFrom;
    private long validFromAsLongInDefaultTimeZone;

    public TocEntryGeneration(IProductCmptTocEntry parent, DateTime validFrom, String className, String xmlResourceName) {
        super(className, xmlResourceName);
        this.parent = parent;
        this.validFrom = validFrom;
        validFromAsLongInDefaultTimeZone = validFrom.toDate(defaultTimeZone).getTime();
    }

    @Override
    public String getXmlResourceName() {
        if (super.getXmlResourceName().equals("")) {
            return parent.getXmlResourceName();
        } else {
            return super.getXmlResourceName();
        }
    }

    /**
     * @return Returns the parent entry.
     */
    public IProductCmptTocEntry getParent() {
        return parent;
    }

    /**
     * @return Returns the validFrom.
     */
    public DateTime getValidFrom() {
        return validFrom;
    }

    /**
     * Returns the point in time this generation is valid from in the given time zone. This method
     * never returns <code>null</code>.
     * 
     * @throws NullPointerException if zone is <code>null</code>.
     */
    public final long getValidFromInMillisec(TimeZone zone) {
        if (zone.equals(defaultTimeZone)) {
            return validFromAsLongInDefaultTimeZone;
        }
        return validFrom.toDate(zone).getTime();
    }

    /**
     * Transforms the toc entry to xml.
     * 
     * @param doc The document used as factory for new element.
     */
    public Element toXml(Document doc) {
        Element entryElement = doc.createElement(GENERATION_ENTRY_XML_ELEMENT);
        super.addToXml(entryElement);
        entryElement.setAttribute("validFrom", validFrom.toIsoFormat());
        return entryElement;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return parent.toString() + " " + validFrom;
    }

}
