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

package org.faktorips.devtools.core.internal.model.valueset;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.valueset.IUnrestrictedValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of IUnrestrictedValueSet
 * 
 * @author Thorsten Guenther
 */
public class UnrestrictedValueSet extends ValueSet implements IUnrestrictedValueSet {

    public final static String XML_TAG = "AllValues"; //$NON-NLS-1$

    /**
     * Creates a new value set representing all values of the datatype provided by the parent. The
     * parent therefore has to implement IValueDatatypeProvider.
     * 
     * @param parent The parent this valueset belongs to.
     * @param partId The id this part is knwon by by the parent.
     * @throws IllegalArgumentException if the parent does not implement the interface
     *             <code>IValueDatatypeProvider</code>.
     */
    public UnrestrictedValueSet(IIpsObjectPart parent, int partId) {
        super(ValueSetType.UNRESTRICTED, parent, partId);
    }

    /**
     * {@inheritDoc}
     */
    public String toShortString() {
        return "[" + org.faktorips.devtools.core.model.valueset.Messages.ValueSetType__allValues + "]"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return super.toString() + ":" + "UnrestrictedValueSet"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsValue(String value,
            MessageList list,
            Object invalidObject,
            String invalidProperty,
            IIpsProject ipsProject) throws CoreException {

        if (list == null) {
            throw new NullPointerException("MessageList required"); //$NON-NLS-1$
        }

        ValueDatatype datatype = findValueDatatype(ipsProject);
        if (datatype == null) {
            addMsg(list, Message.WARNING, MSGCODE_UNKNOWN_DATATYPE, Messages.AllValuesValueSet_msgUnknownDatatype,
                    invalidObject, invalidProperty);
            return false;
        }

        if (!datatype.isParsable(value)) {
            String msg = NLS.bind(
                    org.faktorips.devtools.core.internal.model.valueset.Messages.AllValuesValueSet_msgValueNotParsable,
                    value, datatype.getName());
            addMsg(list, MSGCODE_VALUE_NOT_PARSABLE, msg, invalidObject, invalidProperty);

            // the value can not be parsed - so it is not contained, too...
            msg = NLS.bind(Messages.AllValuesValueSet_msgValueNotContained, datatype.getName());
            addMsg(list, MSGCODE_VALUE_NOT_CONTAINED, msg, invalidObject, invalidProperty);
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsValueSet(IValueSet subset, MessageList list, Object invalidObject, String invalidProperty) {
        if (list == null) {
            throw new NullPointerException("MessageList required"); //$NON-NLS-1$
        }

        ValueDatatype datatype = getValueDatatype();
        ValueDatatype subDatatype = ((ValueSet)subset).getValueDatatype();

        if (datatype == null || subDatatype == null) {
            addMsg(list, Message.WARNING, MSGCODE_UNKNOWN_DATATYPE, Messages.AllValuesValueSet_msgUnknowndDatatype,
                    invalidObject, invalidProperty);
            return false;
        }

        if (datatype.getQualifiedName().equals(subDatatype.getQualifiedName())) {
            return true;
        }

        String msg = NLS.bind(Messages.AllValuesValueSet_msgNoSubset, subset.toShortString(), toShortString());
        addMsg(list, MSGCODE_NOT_SUBSET, msg, invalidObject, invalidProperty);
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsValueSet(IValueSet subset) {
        return containsValueSet(subset, new MessageList(), null, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
        // nothing more to do...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        Document doc = element.getOwnerDocument();
        Element tagElement = doc.createElement(XML_TAG);
        element.appendChild(tagElement);
    }

    /**
     * {@inheritDoc}
     */
    public IValueSet copy(IIpsObjectPart parent, int id) {
        return new UnrestrictedValueSet(parent, id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void copyPropertiesFrom(IValueSet target) {
        // nothing to do.
    }

    /**
     * {@inheritDoc}
     */
    public boolean getContainsNull() {
        ValueDatatype type = getValueDatatype();
        return type == null || !type.isPrimitive();
    }

    /**
     * Because this is an <strong>All</strong>Values valueset, this method throws an
     * UnsupportedOperationException if the underlying datatype is non-primitive and this method is
     * called with <code>false</code> for containsNull, too.
     * 
     * {@inheritDoc}
     */
    public void setContainsNull(boolean containsNull) {
        if (getValueDatatype().isPrimitive() && containsNull) {
            throw new UnsupportedOperationException(
                    "Datatype is primitive, therefore this all-values valueset must not contain null"); //$NON-NLS-1$
        }
        if (!getValueDatatype().isPrimitive() && !containsNull) {
            throw new UnsupportedOperationException(
                    "Datatype is nonPrimitive, therefore this all-values valueset must contain null"); //$NON-NLS-1$
        }
    }

}
