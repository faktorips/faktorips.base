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

package org.faktorips.devtools.core.ui.editors;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.util.ArgumentCheck;

/**
 * A section that shows parts in a single composite.
 */
public abstract class SimpleIpsPartsSection extends IpsSection {

    private IIpsObject ipsObject;

    private IpsPartsComposite partsComposite;

    /**
     * Creates a new <code>SimpleIpsPartsSection</code>.
     * 
     * @param pdObject
     * @param parent
     * @param title
     * @param toolkit
     */
    public SimpleIpsPartsSection(IIpsObject pdObject, Composite parent, String title, UIToolkit toolkit) {
        super(parent, ExpandableComposite.TITLE_BAR, GridData.FILL_BOTH, toolkit);

        ArgumentCheck.notNull(pdObject);

        ipsObject = pdObject;
        initControls();
        setText(title);
    }

    /**
     * Creates a new <code>SimpleIpsPartsSection</code>.
     * 
     * @param ipsObject
     * @param parent
     * @param style
     * @param title
     * @param toolkit
     */
    public SimpleIpsPartsSection(IIpsObject ipsObject, Composite parent, int style, String title, UIToolkit toolkit) {
        super(parent, style, GridData.FILL_BOTH, toolkit);

        ArgumentCheck.notNull(ipsObject);

        this.ipsObject = ipsObject;
        initControls();
        setText(title);
    }

    @Override
    protected final void initClientComposite(Composite client, UIToolkit toolkit) {
        partsComposite = createIpsPartsComposite(client, toolkit);
        addFocusControl(partsComposite);
    }

    /**
     * Subclasses must implement this method by creating the <code>IpsPartsComposite</code> to show
     * inside this <code>SimpleIpsPartsSection</code>.
     * 
     * @param parent The parent UI composite.
     * @param toolkit The UI toolkit to use for creating UI elements.
     * 
     * @return The <code>IpsPartsComposite</code> to show inside this
     *         <code>SimpleIpsPartsSection</code>.
     */
    protected abstract IpsPartsComposite createIpsPartsComposite(Composite parent, UIToolkit toolkit);

    /**
     * 
     */
    public IIpsObject getIpsObject() {
        return ipsObject;
    }

    @Override
    protected void performRefresh() {
        partsComposite.refresh();
    }

    /**
     * Adds the listener as one being notified when the selected part changes. On notification the
     * listener can query the selected part by calling <code>getSelectedPart()</code> on this
     * object.
     */
    public void addSelectionChangedListener(ISelectionChangedListener listener) {
        partsComposite.addSelectionChangedListener(listener);
    }

    /**
     * Removes the listener as one being notified when the selected part changes.
     */
    public void removeSelectionChangedListener(ISelectionChangedListener listener) {
        partsComposite.removeSelectionChangedListener(listener);
    }

    /**
     * Returns the part selected in the section or <code>null</code> if no part is selected.
     */
    public final IIpsObjectPart getSelectedPart() {
        return partsComposite.getSelectedPart();
    }

    protected IpsPartsComposite getPartsComposite() {
        return partsComposite;
    }

}
