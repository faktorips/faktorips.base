/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.Section;
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

    public SimpleIpsPartsSection(
            IIpsObject pdObject, 
            Composite parent,
            String title,
            UIToolkit toolkit) {
        super(parent, Section.TITLE_BAR, GridData.FILL_BOTH, toolkit);
        ArgumentCheck.notNull(pdObject);
        this.ipsObject = pdObject;
        initControls();
        setText(title);
    }
    
    public SimpleIpsPartsSection(
            IIpsObject ipsObject, 
            Composite parent,
            int style,
            String title,
            UIToolkit toolkit) {
        super(parent, style, GridData.FILL_BOTH, toolkit);
        ArgumentCheck.notNull(ipsObject);
        this.ipsObject = ipsObject;
        initControls();
        setText(title);
    }
    
    /** 
     * {@inheritDoc}
     */
    protected final void initClientComposite(Composite client, UIToolkit toolkit) {
		partsComposite = createIpsPartsComposite(client, toolkit);
		addFocusControl(partsComposite);
    }
    
    protected abstract IpsPartsComposite createIpsPartsComposite(Composite parent, UIToolkit toolkit);
    
    public IIpsObject getIpsObject() {
        return ipsObject;
    }
    
    /**
     * {@inheritDoc}
     */
    protected void performRefresh() {
        partsComposite.refresh();
    }
    
    /**
     * Adds the listener as one being notified when the selected part changes.
     * On notification the listener can query the selected part by calling
     * <code>getSelectedPart</code> on this object.
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
     * Returns the part selected in the section or null if no part is selected.
     */
    public final IIpsObjectPart getSelectedPart() {
        return partsComposite.getSelectedPart();
    }
}
