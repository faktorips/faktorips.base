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

import org.w3c.dom.Element;

/**
 * A special kind of {@link TestCaseTocEntry} for formula test entries
 * 
 * @author dirmeier
 */
public class FormulaTestTocEntry extends TestCaseTocEntry {

    public static final String FORMULA_TEST_XML_TAG = "FormulaTest";

    private final String kindId;
    private final String versionId;

    public static FormulaTestTocEntry createFromXml(Element entryElement) {
        String ipsObjectId = entryElement.getAttribute(PROPERTY_IPS_OBJECT_ID);
        String implementationClassName = entryElement.getAttribute(PROPERTY_IMPLEMENTATION_CLASS);

        String ipsObjectName = entryElement.getAttribute(PROPERTY_IPS_OBJECT_QNAME);
        String kindId = entryElement.getAttribute(ProductCmptTocEntry.PROPERTY_KIND_ID);
        String versionId = entryElement.getAttribute(ProductCmptTocEntry.PROPERTY_VERSION_ID);

        return new FormulaTestTocEntry(ipsObjectId, ipsObjectName, kindId, versionId, implementationClassName);
    }

    public FormulaTestTocEntry(String ipsObjectId, String ipsObjectQualifiedName, String kindId, String versionId,
            String implementationClassName) {
        super(ipsObjectId, ipsObjectQualifiedName, "", implementationClassName);
        this.kindId = kindId;
        this.versionId = versionId;
    }

    /**
     * @return Returns the kindId.
     */
    public String getKindId() {
        return kindId;
    }

    /**
     * @return Returns the versionId.
     */
    public String getVersionId() {
        return versionId;
    }

    @Override
    protected void addToXml(Element entryElement) {
        super.addToXml(entryElement);
        entryElement.setAttribute(ProductCmptTocEntry.PROPERTY_KIND_ID, kindId);
        entryElement.setAttribute(ProductCmptTocEntry.PROPERTY_VERSION_ID, versionId);
    }

    @Override
    protected String getXmlElementTag() {
        return FORMULA_TEST_XML_TAG;
    }

}