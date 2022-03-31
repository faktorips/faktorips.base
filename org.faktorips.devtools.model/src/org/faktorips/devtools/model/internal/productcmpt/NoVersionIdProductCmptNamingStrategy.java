/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt;

import java.util.GregorianCalendar;

import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.runtime.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A naming strategy for product components that allows to use only the constant part, the version
 * is always the empty string. The next version is determined by appending a 1 to the given name.
 * 
 * @author Jan Ortmann
 */
public class NoVersionIdProductCmptNamingStrategy extends AbstractProductCmptNamingStrategy {

    public static final String EXTENSION_ID = "org.faktorips.devtools.model.NoVersionIdProductCmptNamingStrategy"; //$NON-NLS-1$

    @SuppressWarnings("hiding")
    public static final String XML_TAG_NAME = "NoVersionIdProductCmptNamingStrategy"; //$NON-NLS-1$

    public NoVersionIdProductCmptNamingStrategy() {
        super();
    }

    @Override
    public String getExtensionId() {
        return EXTENSION_ID;
    }

    @Override
    public boolean supportsVersionId() {
        return false;
    }

    @Override
    public String getKindId(String productCmptName) {
        return productCmptName;
    }

    @Override
    public String getVersionId(String productCmptName) {
        return ""; //$NON-NLS-1$
    }

    @Override
    public String getNextVersionId(IProductCmpt productCmpt, GregorianCalendar validFrom) {
        return ""; //$NON-NLS-1$
    }

    @Override
    public String getNextName(IProductCmpt productCmpt, GregorianCalendar validFrom) {
        return productCmpt.getName() + "1"; //$NON-NLS-1$
    }

    @Override
    public MessageList validateVersionId(String versionId) {
        return new MessageList();
    }

    @Override
    public void initSubclassFromXml(Element el) {
        setVersionIdSeparator(""); //$NON-NLS-1$
    }

    @Override
    public Element toXmlSubclass(Document doc) {
        return doc.createElement(XML_TAG_NAME);
    }

    @Override
    public String getUniqueRuntimeId(IIpsProject project, String productCmptName) {
        String id = project.getRuntimeIdPrefix() + productCmptName;
        String uniqueId = id;

        int i = 1;
        while (project.findProductCmptByRuntimeId(uniqueId) != null) {
            uniqueId = id + i;
            i++;
        }

        return uniqueId;
    }

}
