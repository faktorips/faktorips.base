/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal.toc;


/**
 * A {@link TocEntryObject} for test cases
 * 
 * @author dirmeier
 */
public class TestCaseTocEntry extends TocEntryObject {

    public static final String TEST_XML_TAG = "TestCase";

    /**
     * Creates an entry that references to a test case.
     */
    public TestCaseTocEntry(String ipsObjectId, String ipsObjectQualifiedName, String xmlResourceName,
            String implementationClassName) {
        super(ipsObjectId, ipsObjectQualifiedName, xmlResourceName, implementationClassName);
    }

    @Override
    protected String getXmlElementTag() {
        return TEST_XML_TAG;
    }

}
