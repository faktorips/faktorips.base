/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPartSite;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.util.ArgumentCheck;

/**
 * A section that shows parts in a single composite.
 */
public abstract class SimpleIpsPartsSection extends IpsObjectPartContainerSection {

    private final IIpsObject ipsObject;

    private final IWorkbenchPartSite site;

    private IpsPartsComposite partsComposite;

    /**
     * This constructor is intended to be used if it is desired that the <em>expanded</em> state of
     * the section is stored in the Faktor-IPS preference store.
     * <p>
     * <strong>Subclassing:</strong><br>
     * This constructor first calls {@link #initControls()} to create the section UI elements, and
     * then {@link #setText(String)} to set the provided section title.
     * <p>
     * <strong>Important:</strong> Subclass-specific constructor code is called after
     * {@link #initControls()} has been invoked.
     * 
     * @param id a string that identifies this section, used as key to store whether the section is
     *            currently expanded
     * @param ipsObject the {@link IIpsObject} the parts to show belong to
     */
    protected SimpleIpsPartsSection(String id, IIpsObject ipsObject, Composite parent, IWorkbenchPartSite site,
            String title, UIToolkit toolkit) {

        super(id, ipsObject, parent, GridData.FILL_BOTH, toolkit);

        ArgumentCheck.notNull(ipsObject);

        this.ipsObject = ipsObject;
        this.site = site;
        initControls();
        setText(title);
    }

    protected SimpleIpsPartsSection(IIpsObject ipsObject, Composite parent, IWorkbenchPartSite site, int style,
            String title, UIToolkit toolkit) {

        super(ipsObject, parent, style, GridData.FILL_BOTH, toolkit);

        ArgumentCheck.notNull(ipsObject);

        this.ipsObject = ipsObject;
        this.site = site;
        initControls();
        setText(title);
    }

    @Override
    protected final void initClientComposite(Composite client, UIToolkit toolkit) {
        partsComposite = createIpsPartsComposite(client, toolkit);
        addFocusControl(partsComposite);
        partsComposite.createContextMenu();
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

    public IIpsObject getIpsObject() {
        return ipsObject;
    }

    @Override
    protected void performRefresh() {
        super.performRefresh();
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

    protected IWorkbenchPartSite getSite() {
        return site;
    }

}
