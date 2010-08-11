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
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.ILabel;
import org.faktorips.devtools.core.model.ipsobject.ILabeledElement;
import org.w3c.dom.Element;

/**
 * A <tt>LabeledIpsObjectPart</tt> is an {@link IpsObjectPart} that contains {@link ILabel}s.
 * <p>
 * If an IPS object part wants to support labels, it has to inherit from this class. This class
 * implements the following methods of <tt>IpsObjectPartContainer</tt>:
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
public abstract class LabeledIpsObjectPart extends IpsObjectPart implements ILabeledElement {

    private final LabeledElementHelper labelHelper = new LabeledElementHelper(this);

    protected LabeledIpsObjectPart(IIpsObjectPartContainer parent, String id) {
        super(parent, id);
    }

    public LabeledIpsObjectPart() {
        // Constructor for testing purposes
    }

    @Override
    public IIpsElement[] getChildren() {
        return labelHelper.getChildren();
    }

    @Override
    protected void reinitPartCollections() {
        labelHelper.reinitPartCollections();
    }

    @Override
    protected void addPart(IIpsObjectPart part) {
        labelHelper.addPart(part);
    }

    @Override
    protected void removePart(IIpsObjectPart part) {
        labelHelper.removePart(part);
    }

    @Override
    protected IIpsObjectPart newPart(Element xmlTag, String id) {
        return labelHelper.newPart(xmlTag, id);
    }

    @Override
    public ILabel getLabel(Locale locale) {
        return labelHelper.getLabel(locale);
    }

    @Override
    public Set<ILabel> getLabels() {
        return labelHelper.getLabels();
    }

    @Override
    public ILabel getLabelForCurrentLocale() {
        return labelHelper.getLabelForCurrentLocale();
    }

    @Override
    public ILabel getLabelForDefaultLocale() {
        return labelHelper.getLabelForDefaultLocale();
    }

    @Override
    public boolean isPluralLabelSupported() {
        return false;
    }

    @Override
    public ILabel newLabel() {
        return labelHelper.newLabel();
    }

}
