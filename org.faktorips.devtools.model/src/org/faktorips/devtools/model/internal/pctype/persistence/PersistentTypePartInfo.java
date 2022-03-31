/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.pctype.persistence;

import java.text.MessageFormat;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.model.internal.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.model.internal.pctype.Messages;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.persistence.IPersistentTypePartInfo;
import org.faktorips.devtools.model.util.PersistenceUtil;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Informations about the persistence properties that could be configured for any type part
 * (attributes and associations).
 */
public abstract class PersistentTypePartInfo extends AtomicIpsObjectPart implements IPersistentTypePartInfo {

    private boolean transientPart = false;

    private String indexName = StringUtils.EMPTY;

    public PersistentTypePartInfo(IIpsObjectPart parent, String id) {
        super(parent, id);
    }

    @Override
    public boolean isTransient() {
        return transientPart;
    }

    @Override
    public void setTransient(boolean transientPart) {
        boolean oldValue = this.transientPart;
        this.transientPart = transientPart;
        valueChanged(oldValue, transientPart);
    }

    @Override
    public String getIndexName() {
        return indexName;
    }

    @Override
    public void setIndexName(String newIndexName) {
        String oldIndexName = indexName;
        indexName = newIndexName;
        valueChanged(oldIndexName, indexName);
    }

    @Override
    public boolean isIndexNameDefined() {
        return StringUtils.isNotEmpty(indexName);
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(getXmlTag());
    }

    protected abstract String getXmlTag();

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        transientPart = Boolean.valueOf(element.getAttribute(PROPERTY_TRANSIENT));
        indexName = element.getAttribute(PROPERTY_INDEX_NAME);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_TRANSIENT, Boolean.toString(transientPart));
        element.setAttribute(PROPERTY_INDEX_NAME, indexName);
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) {
        super.validateThis(list, ipsProject);
        validateIndexName(list);
    }

    private void validateIndexName(MessageList msgList) {
        if (isIndexNameDefined()) {
            if (!PersistenceUtil.isValidDatabaseIdentifier(indexName)) {
                String text = MessageFormat.format(Messages.PersistentInfo_msgIndexNameIsInvalid, indexName);
                msgList.add(Message.newError(MSGCODE_INDEX_NAME_INVALID, text, this, PROPERTY_INDEX_NAME));
            }
        }
    }

}
