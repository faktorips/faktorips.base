/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.ipsobject;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.ILabel;
import org.faktorips.devtools.core.model.ipsobject.ILabeledElement;
import org.faktorips.devtools.core.model.ipsproject.ISupportedLanguage;
import org.faktorips.util.ArgumentCheck;
import org.w3c.dom.Element;

/**
 * This helper class is used by the classes {@link LabeledIpsObject} and
 * {@link LabeledIpsObjectPart}. The code necessary to implement the support for {@link ILabel}s is
 * shared by these classes. However, it was not possible to put this code into the common base class
 * {@link IpsObjectPartContainer} because there are only some subclasses of
 * {@link IpsObjectPartContainer} that use labels while the other subclasses shall remain unaffected
 * by the changes.
 * 
 * @author Alexander Weickmann
 */
public class LabeledElementHelper {

    private final IpsObjectPartContainer ipsObjectPartContainer;

    /** Set containing all labels attached to the labeled object. */
    private final Set<ILabel> labels;

    public LabeledElementHelper(IpsObjectPartContainer ipsObjectPartContainer) {
        this.ipsObjectPartContainer = ipsObjectPartContainer;
        labels = new HashSet<ILabel>();
    }

    /**
     * @see IpsObjectPartContainer#getChildren()
     */
    public IIpsElement[] getChildren() {
        return labels.toArray(new IIpsElement[labels.size()]);
    }

    /**
     * @see IpsObjectPartContainer#reinitPartCollections()
     */
    protected void reinitPartCollections() {
        labels.clear();
    }

    /**
     * @see IpsObjectPartContainer#addPart(IIpsObjectPart)
     */
    protected void addPart(IIpsObjectPart part) {
        if (part instanceof ILabel) {
            labels.add((ILabel)part);
        }
    }

    /**
     * @see IpsObjectPartContainer#removePart(IIpsObjectPart)
     */
    protected void removePart(IIpsObjectPart part) {
        if (part instanceof ILabel) {
            labels.remove(part);
        }
    }

    /**
     * @see IpsObjectPartContainer#newPart(Element, String)
     */
    protected IIpsObjectPart newPart(Element xmlTag, String id) {
        if (xmlTag.getNodeName().equals(ILabel.XML_TAG_NAME)) {
            return newLabel(id);
        }
        return null;
    }

    private ILabel newLabel(String id) {
        ILabel newLabel = new Label(ipsObjectPartContainer, id);
        labels.add(newLabel);
        return newLabel;
    }

    /**
     * @see ILabeledElement#getLabel(Locale)
     */
    public ILabel getLabel(Locale locale) {
        ArgumentCheck.notNull(locale);
        for (ILabel label : labels) {
            Locale labelLocale = label.getLocale();
            if (labelLocale == null) {
                continue;
            }
            if (locale.getLanguage().equals(labelLocale.getLanguage())) {
                return label;
            }
        }
        return null;
    }

    /**
     * @see ILabeledElement#getLabels()
     */
    public Set<ILabel> getLabels() {
        return Collections.unmodifiableSet(labels);
    }

    /**
     * @see ILabeledElement#getLabelForCurrentLocale()
     */
    public ILabel getLabelForCurrentLocale() {
        Locale currentLocale = IpsPlugin.getDefault().getIpsModelLocale();
        return getLabel(currentLocale);
    }

    /**
     * @see ILabeledElement#getLabelForDefaultLocale()
     */
    public ILabel getLabelForDefaultLocale() {
        ILabel defaultLabel = null;
        ISupportedLanguage defaultLanguage = ipsObjectPartContainer.getIpsProject().getProperties()
                .getDefaultLanguage();
        if (defaultLanguage != null) {
            defaultLabel = getLabel(defaultLanguage.getLocale());
        }
        return defaultLabel;
    }

    /**
     * @see ILabeledElement#newLabel()
     */
    public ILabel newLabel() {
        return newLabel(ipsObjectPartContainer.getNextPartId());
    }

}
