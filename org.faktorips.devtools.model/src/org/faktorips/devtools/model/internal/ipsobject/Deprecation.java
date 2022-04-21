/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.model.internal.ipsobject;

import org.apache.commons.lang.StringUtils;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.IVersion;
import org.faktorips.devtools.model.IVersionProvider;
import org.faktorips.devtools.model.ipsobject.IDeprecation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Deprecation extends AtomicIpsObjectPart implements IDeprecation {

    /** Whether the part is marked for removal. */
    private boolean forRemoval;

    protected Deprecation(IpsObjectPartContainer parent, String id) {
        super(parent, id);
        setSinceVersionStringInternal(parent.getDefaultVersion());
    }

    @Override
    public boolean isForRemoval() {
        return forRemoval;
    }

    @Override
    public void setForRemoval(boolean forRemoval) {
        boolean oldValue = this.forRemoval;
        this.forRemoval = forRemoval;
        valueChanged(oldValue, forRemoval, IDeprecation.PROPERTY_FOR_REMOVAL);
    }

    @Override
    public IVersion<?> getSinceVersion() {
        if (StringUtils.isBlank(getSinceVersionString())) {
            return null;
        }
        IVersionProvider<?> versionProvider = getIpsProject().getVersionProvider();
        return versionProvider.getVersion(getSinceVersionString());
    }

    @Override
    public boolean isValidSinceVersion() {
        if (StringUtils.isNotBlank(getSinceVersionString())) {
            IVersionProvider<?> versionProvider = getIpsProject().getVersionProvider();
            return versionProvider.isCorrectVersionFormat(getSinceVersionString());
        } else {
            return false;
        }
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(XML_TAG);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(XML_ATTRIBUTE_DEPRECATION_VERSION, getSinceVersionString());
        element.setAttribute(XML_ATTRIBUTE_FOR_REMOVAL, String.valueOf(forRemoval));
    }

    @Override
    protected void initPropertiesFromXml(Element deprecationNode, String id) {
        super.initPropertiesFromXml(deprecationNode, id);
        setSinceVersionStringInternal(deprecationNode.getAttribute(XML_ATTRIBUTE_DEPRECATION_VERSION));
        forRemoval = Boolean.valueOf(deprecationNode.getAttribute(XML_ATTRIBUTE_FOR_REMOVAL));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Deprecated"); //$NON-NLS-1$
        if (isForRemoval()) {
            sb.append(Messages.Deprecation_forRemoval);
        }
        if (isValidSinceVersion()) {
            sb.append(" "); //$NON-NLS-1$
            sb.append(NLS.bind(Messages.Deprecation_since, getSinceVersionString()));
        }
        sb.append(". "); //$NON-NLS-1$
        sb.append(IIpsModel.get().getMultiLanguageSupport().getLocalizedDescription(this));
        return sb.toString();
    }
}
