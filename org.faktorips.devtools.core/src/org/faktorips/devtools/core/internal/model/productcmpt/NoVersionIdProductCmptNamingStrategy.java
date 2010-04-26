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

package org.faktorips.devtools.core.internal.model.productcmpt;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A naming strategy for product components that allows to use only the constant part, the version
 * is always the empty string. The next version is determined by appending a 1 to the given name.
 * 
 * @author Jan Ortmann
 */
public class NoVersionIdProductCmptNamingStrategy extends AbstractProductCmptNamingStrategy {

    public final static String EXTENSION_ID = "org.faktorips.devtools.core.NoVersionIdProductCmptNamingStrategy"; //$NON-NLS-1$

    public final static String XML_TAG_NAME = "NoVersionIdProductCmptNamingStrategy"; //$NON-NLS-1$

    public NoVersionIdProductCmptNamingStrategy() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    public String getExtensionId() {
        return EXTENSION_ID;
    }

    /**
     * {@inheritDoc}
     */
    public boolean supportsVersionId() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKindId(String productCmptName) {
        return productCmptName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getVersionId(String productCmptName) {
        return ""; //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public String getNextVersionId(IProductCmpt productCmpt) {
        return ""; //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNextName(IProductCmpt productCmpt) {
        return productCmpt.getName() + "1"; //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public MessageList validateVersionId(String versionId) {
        return new MessageList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initSubclassFromXml(Element el) {
        setVersionIdSeparator(""); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Element toXmlSubclass(Document doc) {
        return doc.createElement(XML_TAG_NAME);
    }

    /**
     * {@inheritDoc}
     */
    public String getUniqueRuntimeId(IIpsProject project, String productCmptName) throws CoreException {
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
