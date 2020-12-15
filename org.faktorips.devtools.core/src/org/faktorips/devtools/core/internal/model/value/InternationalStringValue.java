/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.value;

import java.util.Locale;
import java.util.Observer;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.internal.model.InternationalString;
import org.faktorips.devtools.core.internal.model.InternationalStringXmlHelper;
import org.faktorips.devtools.core.internal.model.productcmpt.AttributeValue;
import org.faktorips.devtools.core.model.IInternationalString;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.ISupportedLanguage;
import org.faktorips.devtools.core.model.value.IValue;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;
import org.faktorips.values.LocalizedString;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Value-Representation of the {@link InternationalString}.
 * 
 * @author frank
 * @since 3.9
 */
public class InternationalStringValue extends AbstractValue<IInternationalString> {

    public static final String CONTENT_STRING_SEPERATOR = "|"; //$NON-NLS-1$
    private final InternationalString content;

    /**
     * New InternationalStringValue with empty {@link InternationalString}
     */
    public InternationalStringValue() {
        this.content = new InternationalString();
    }

    /**
     * Create a new InternationalStringValue for the XML-Element-Node
     * 
     * @param element XML-Element-Node
     * @return InternationalStringValue
     */
    public static InternationalStringValue createFromXml(Element element) {
        InternationalStringValue value = new InternationalStringValue();
        IInternationalString internationalString = value.getContent();
        InternationalStringXmlHelper.initFromXml(internationalString, element);
        return value;
    }

    @Override
    public IInternationalString getContent() {
        return content;
    }

    @Override
    public Node toXml(Document doc) {
        return getContent().toXml(doc);
    }

    @Override
    public String getContentAsString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (LocalizedString localizedString : getContent().values()) {
            if (!StringUtils.isEmpty(stringBuilder.toString())) {
                stringBuilder.append(CONTENT_STRING_SEPERATOR);
            }
            stringBuilder.append(localizedString.getLocale()).append("=").append(localizedString.getValue()); //$NON-NLS-1$
        }
        return stringBuilder.toString();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("InternationalStringValue ["); //$NON-NLS-1$
        builder.append("content="); //$NON-NLS-1$
        builder.append(getContent());
        builder.append("]"); //$NON-NLS-1$
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + getContent().hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof InternationalStringValue)) {
            return false;
        }
        InternationalStringValue other = (InternationalStringValue)obj;
        if (!getContent().equals(other.getContent())) {
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Creates a new warning message for every {@link ISupportedLanguage} that is null or empty. if
     * all saved languages null or empty, then no warning message will be return.
     */
    @Override
    public void validate(ValueDatatype datatype,
            String datatypeName,
            IIpsProject ipsproject,
            MessageList list,
            ObjectProperty... objectProperty) {
        if (getContent() != null) {
            MessageList newList = new MessageList();
            Set<ISupportedLanguage> supportedLanguages = ipsproject.getReadOnlyProperties().getSupportedLanguages();
            int languagesCount = supportedLanguages.size();
            for (ISupportedLanguage supportedLanguage : supportedLanguages) {
                LocalizedString iLocalizedString = getContent().get(supportedLanguage.getLocale());
                if (StringUtils.isEmpty(iLocalizedString.getValue())) {
                    newList.add(new Message(AttributeValue.MSGCODE_MULTILINGUAL_NOT_SET, NLS.bind(
                            Messages.AttributeValue_MultiLingual_NotSet, supportedLanguage.getLocale()
                                    .getDisplayLanguage()),
                            Message.WARNING, objectProperty));
                }
            }
            if (languagesCount > newList.size()) {
                list.add(newList);
            }
        }
    }

    @Override
    public String getLocalizedContent(Locale locale) {
        LocalizedString localizedString = getContent().get(locale);
        return localizedString.getValue();
    }

    @Override
    public String getLocalizedContent() {
        for (LocalizedString localizedString : getContent().values()) {
            String value = localizedString.getValue();
            if (!StringUtils.isEmpty(value)) {
                return value;
            }
        }
        return StringUtils.EMPTY;
    }

    @Override
    public void addObserver(Observer observer) {
        this.content.addObserver(observer);

    }

    @Override
    public void deleteObserver(Observer observer) {
        this.content.deleteObserver(observer);
    }

    @Override
    public int compare(IValue<?> other, ValueDatatype valueDatatype) {
        if (IInternationalString.class.isAssignableFrom(other.getContent().getClass())) {
            return getContent().compareTo((IInternationalString)other.getContent());
        } else {
            return ObjectUtils.compare(getContentAsString(), other.getContentAsString());
        }
    }

}
