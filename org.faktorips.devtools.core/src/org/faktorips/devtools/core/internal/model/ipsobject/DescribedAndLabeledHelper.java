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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.core.model.ipsobject.IDescription;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.ILabel;
import org.faktorips.devtools.core.model.ipsobject.ILabeledElement;
import org.faktorips.devtools.core.model.ipsproject.ISupportedLanguage;
import org.faktorips.util.ArgumentCheck;
import org.w3c.dom.Element;

/**
 * This helper class is used by the classes {@link DescribedAndLabeledIpsObject} and
 * {@link DescribedAndLabeledIpsObjectPart}. The code necessary to implement the support for
 * {@link ILabel}s and {@link IDescription}s is shared by these classes. However, it was not
 * possible to put this code into the common base class {@link IpsObjectPartContainer} because there
 * are only some subclasses of {@link IpsObjectPartContainer} that use descriptions and labels while
 * the other subclasses shall remain unaffected.
 * 
 * @author Alexander Weickmann
 * 
 * @since 3.1
 */
public class DescribedAndLabeledHelper {

    private final IIpsObjectPartContainer ipsObjectPartContainer;

    /** Set containing all labels attached to the object part container. */
    private final Set<ILabel> labels;

    /** Set containing all descriptions attached to the object part container. */
    private final Set<IDescription> descriptions;

    public DescribedAndLabeledHelper(IIpsObjectPartContainer ipsObjectPartContainer) {
        this.ipsObjectPartContainer = ipsObjectPartContainer;
        labels = new HashSet<ILabel>();
        descriptions = new HashSet<IDescription>();
    }

    /**
     * @see IpsObjectPartContainer#getChildren()
     */
    public IIpsElement[] getChildren() {
        List<IIpsElement> children = new ArrayList<IIpsElement>(labels.size() + descriptions.size());
        children.addAll(labels);
        children.addAll(descriptions);
        return children.toArray(new IIpsElement[children.size()]);
    }

    /**
     * @see IpsObjectPartContainer#reinitPartCollections()
     */
    protected void reinitPartCollections() {
        labels.clear();
        descriptions.clear();
    }

    /**
     * @see IpsObjectPartContainer#addPart(IIpsObjectPart)
     */
    protected void addPart(IIpsObjectPart part) {
        if (part instanceof ILabel) {
            labels.add((ILabel)part);

        } else if (part instanceof IDescription) {
            descriptions.add((IDescription)part);
        }
    }

    /**
     * @see IpsObjectPartContainer#removePart(IIpsObjectPart)
     */
    protected void removePart(IIpsObjectPart part) {
        if (part instanceof ILabel) {
            labels.remove(part);

        } else if (part instanceof IDescription) {
            descriptions.remove(part);
        }
    }

    /**
     * @see IpsObjectPartContainer#newPart(Element, String)
     */
    protected IIpsObjectPart newPart(Element xmlTag, String id) {
        if (xmlTag.getNodeName().equals(ILabel.XML_TAG_NAME)) {
            return newLabel(id);

        } else if (xmlTag.getNodeName().equals(IDescription.XML_TAG_NAME)) {
            return newDescription(id);
        }

        return null;
    }

    private ILabel newLabel(String id) {
        ILabel newLabel = new Label(ipsObjectPartContainer, id);
        labels.add(newLabel);
        return newLabel;
    }

    private IDescription newDescription(String id) {
        IDescription newDescription = new Description(ipsObjectPartContainer, id);
        descriptions.add(newDescription);
        return newDescription;
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
     * @see ILabeledElement#getLabelForIpsModelLocale()
     */
    public ILabel getLabelForIpsModelLocale() {
        Locale ipsModelLocale = IpsPlugin.getDefault().getIpsModelLocale();
        return getLabel(ipsModelLocale);
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
        return newLabel(((IpsObjectPartContainer)ipsObjectPartContainer).getNextPartId());
    }

    /**
     * @see IDescribedElement#getDescription(Locale)
     */
    public IDescription getDescription(Locale locale) {
        ArgumentCheck.notNull(locale);
        for (IDescription description : descriptions) {
            Locale descriptionLocale = description.getLocale();
            if (descriptionLocale == null) {
                continue;
            }
            if (locale.getLanguage().equals(descriptionLocale.getLanguage())) {
                return description;
            }
        }
        return null;
    }

    /**
     * @see IDescribedElement#getDescriptions()
     */
    public Set<IDescription> getDescriptions() {
        return Collections.unmodifiableSet(descriptions);
    }

    /**
     * @see IDescribedElement#getDescriptionForIpsModelLocale()
     */
    public IDescription getDescriptionForIpsModelLocale() {
        Locale ipsModelLocale = IpsPlugin.getDefault().getIpsModelLocale();
        return getDescription(ipsModelLocale);
    }

    /**
     * @see IDescribedElement#getDescriptionForDefaultLocale()
     */
    public IDescription getDescriptionForDefaultLocale() {
        IDescription defaultDescription = null;
        ISupportedLanguage defaultLanguage = ipsObjectPartContainer.getIpsProject().getProperties()
                .getDefaultLanguage();
        if (defaultLanguage != null) {
            defaultDescription = getDescription(defaultLanguage.getLocale());
        }
        return defaultDescription;
    }

    /**
     * @see IDescribedElement#newDescription()
     */
    public IDescription newDescription() {
        return newDescription(((IpsObjectPartContainer)ipsObjectPartContainer).getNextPartId());
    }

}
