package org.faktorips.devtools.core.ui.editors;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.util.ArgumentCheck;


/**
 * A section that shows parts in a single composite.
 */
public abstract class SimpleIpsPartsSection extends IpsSection {
    
    private IIpsObject pdObject;
    private IpsPartsComposite pdPartsComposite;

    public SimpleIpsPartsSection(
            IIpsObject pdObject, 
            Composite parent,
            String title,
            UIToolkit toolkit) {
        super(parent, Section.TITLE_BAR, GridData.FILL_BOTH, toolkit);
        ArgumentCheck.notNull(pdObject);
        this.pdObject = pdObject;
        initControls();
        setText(title);
    }
    
    public SimpleIpsPartsSection(
            IIpsObject pdObject, 
            Composite parent,
            int style,
            String title,
            UIToolkit toolkit) {
        super(parent, style, GridData.FILL_BOTH, toolkit);
        ArgumentCheck.notNull(pdObject);
        this.pdObject = pdObject;
        initControls();
        setText(title);
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.forms.IpsSection#initClientComposite(org.eclipse.swt.widgets.Composite, org.faktorips.devtools.core.ui.UIToolkit)
     */
    protected final void initClientComposite(Composite client, UIToolkit toolkit) {
		pdPartsComposite = createIpsPartsComposite(client, toolkit);
    }
    
    protected abstract IpsPartsComposite createIpsPartsComposite(Composite parent, UIToolkit toolkit);
    
    public IIpsObject getIpsObject() {
        return pdObject;
    }
    
    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.forms.IpsSection#performRefresh()
     */
    protected void performRefresh() {
        pdPartsComposite.refresh();
    }
    
    /**
     * Adds the listener as one being notified when the selected part changes.
     * On notification the listener can query the selected part by calling
     * <code>getSelectedPart</code> on this object.
     */
    public void addSelectionChangedListener(ISelectionChangedListener listener) {
        pdPartsComposite.addSelectionChangedListener(listener);
    }
    
    /**
     * Removes the listener as one being notified when the selected part changes.
     */
    public void removeSelectionChangedListener(ISelectionChangedListener listener) {
        pdPartsComposite.removeSelectionChangedListener(listener);
    }

    /**
     * Returns the part selected in the section or null if no part is selected.
     */
    public final IIpsObjectPart getSelectedPart() {
        return pdPartsComposite.getSelectedPart();
    }
    
    
    
}
