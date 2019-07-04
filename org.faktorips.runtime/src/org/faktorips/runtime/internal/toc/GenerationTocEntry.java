/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal.toc;

import java.util.TimeZone;

import org.faktorips.runtime.internal.DateTime;
import org.w3c.dom.Element;

/**
 * A toc entry that refers to an ips object generation.
 * 
 * @author Jan Ortmann
 */
public class GenerationTocEntry extends TocEntry {

    public static final String XML_TAG = "Generation";

    public static final String PROPERTY_VALID_FROM = "validFrom";

    private static final TimeZone DEFAULT_TIME_ZONE = TimeZone.getDefault();

    private ProductCmptTocEntry parent;
    private DateTime validFrom;
    private long validFromAsLongInDefaultTimeZone;

    public GenerationTocEntry(ProductCmptTocEntry parent, DateTime validFrom, String className,
            String xmlResourceName) {
        super(className, xmlResourceName);
        this.parent = parent;
        this.validFrom = validFrom;
        validFromAsLongInDefaultTimeZone = validFrom.toDate(DEFAULT_TIME_ZONE).getTime();
    }

    public static final GenerationTocEntry createFromXml(ProductCmptTocEntry parent, Element element) {
        DateTime validFrom = DateTime.parseIso(element.getAttribute(PROPERTY_VALID_FROM));
        String className = element.getAttribute(PROPERTY_IMPLEMENTATION_CLASS);
        String xmlResourceName = element.getAttribute(PROPERTY_XML_RESOURCE);
        return new GenerationTocEntry(parent, validFrom, className, xmlResourceName);
    }

    @Override
    public String getXmlResourceName() {
        if (super.getXmlResourceName().equals("")) {
            return parent.getXmlResourceName();
        } else {
            return super.getXmlResourceName();
        }
    }

    public ProductCmptTocEntry getParent() {
        return parent;
    }

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
        if (zone.equals(DEFAULT_TIME_ZONE)) {
            return validFromAsLongInDefaultTimeZone;
        }
        return validFrom.toDate(zone).getTime();
    }

    @Override
    protected void addToXml(Element element) {
        super.addToXml(element);
        element.setAttribute(PROPERTY_VALID_FROM, validFrom.toIsoFormat());
    }

    @Override
    public String toString() {
        return parent.toString() + " " + validFrom;
    }

    @Override
    protected String getXmlElementTag() {
        return XML_TAG;
    }

}
