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

import java.util.Locale;
import java.util.Set;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.core.model.ipsobject.IDescription;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.ILabel;
import org.faktorips.devtools.core.model.ipsobject.ILabeledElement;
import org.w3c.dom.Element;

/**
 * A <tt>DescribedAndLabeledIpsObject</tt> is an {@link IpsObject} that contains
 * {@link IDescription}s and {@link ILabel}s.
 * <p>
 * If an IPS object wants to support descriptions and labels, it has to inherit from this class.
 * This class implements the following methods of <tt>IpsObjectPartContainer</tt>:
 * <ul>
 * <li>getChildren()
 * <li>reinitPartCollections()
 * <li>addPart(IIpsObjectPart part)
 * <li>removePart(IIpsObjectPart part)
 * <li>newPart(Element xmlTag, String id)
 * </ul>
 * <p>
 * Subclasses must make sure to call the super implementation when overriding one of these
 * operations.
 * 
 * @author Alexander Weickmann
 * 
 * @since 3.1
 */
public abstract class DescribedAndLabeledIpsObject extends IpsObject implements ILabeledElement, IDescribedElement {

    private final DescribedAndLabeledHelper helper;

    protected DescribedAndLabeledIpsObject(IIpsSrcFile file) {
        super(file);
        helper = new DescribedAndLabeledHelper(this);
    }

    @Override
    public IIpsElement[] getChildren() {
        return helper.getChildren();
    }

    @Override
    protected void reinitPartCollections() {
        helper.reinitPartCollections();
    }

    @Override
    protected void addPart(IIpsObjectPart part) {
        helper.addPart(part);
    }

    @Override
    protected void removePart(IIpsObjectPart part) {
        helper.removePart(part);
    }

    @Override
    protected IIpsObjectPart newPart(Element xmlTag, String id) {
        return helper.newPart(xmlTag, id);
    }

    @Override
    public ILabel getLabel(Locale locale) {
        return helper.getLabel(locale);
    }

    @Override
    public Set<ILabel> getLabels() {
        return helper.getLabels();
    }

    @Override
    public ILabel getLabelForIpsModelLocale() {
        return helper.getLabelForIpsModelLocale();
    }

    @Override
    public ILabel getLabelForDefaultLocale() {
        return helper.getLabelForDefaultLocale();
    }

    @Override
    public boolean isPluralLabelSupported() {
        return false;
    }

    @Override
    public ILabel newLabel() {
        return helper.newLabel();
    }

    @Override
    public IDescription getDescription(Locale locale) {
        return helper.getDescription(locale);
    }

    @Override
    public Set<IDescription> getDescriptions() {
        return helper.getDescriptions();
    }

    @Override
    public IDescription getDescriptionForIpsModelLocale() {
        return helper.getDescriptionForIpsModelLocale();
    }

    @Override
    public IDescription getDescriptionForDefaultLocale() {
        return helper.getDescriptionForDefaultLocale();
    }

    @Override
    public IDescription newDescription() {
        return helper.newDescription();
    }

}
