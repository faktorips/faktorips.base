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

    public FormulaTestTocEntry(String ipsObjectId, String ipsObjectQualifiedName, String kindId, String versionId,
            String implementationClassName) {
        super(ipsObjectId, ipsObjectQualifiedName, "", implementationClassName);
        this.kindId = kindId;
        this.versionId = versionId;
    }

    public String getKindId() {
        return kindId;
    }

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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((kindId == null) ? 0 : kindId.hashCode());
        result = prime * result + ((versionId == null) ? 0 : versionId.hashCode());
        return result;
    }

    // CSOFF: CyclomaticComplexity
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof FormulaTestTocEntry)) {
            return false;
        }
        FormulaTestTocEntry other = (FormulaTestTocEntry)obj;
        if (kindId == null) {
            if (other.kindId != null) {
                return false;
            }
        } else if (!kindId.equals(other.kindId)) {
            return false;
        }
        if (versionId == null) {
            if (other.versionId != null) {
                return false;
            }
        } else if (!versionId.equals(other.versionId)) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        return true;
    }
    // CSON: CyclomaticComplexity

}
