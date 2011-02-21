/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.MessageCueController;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * A section that shows parts in a single composite.
 */
public abstract class SimpleIpsPartsSection extends IpsSection {

    /**
     * Set containing all validation message codes that are monitored by this section meaning that
     * they will be indicated at the section level with an appropriate marker.
     */
    private final Set<String> monitoredValidationMessageCodes = new HashSet<String>();

    private IIpsObject ipsObject;

    private IpsPartsComposite partsComposite;

    public SimpleIpsPartsSection(IIpsObject pdObject, Composite parent, String title, UIToolkit toolkit) {
        super(parent, ExpandableComposite.TITLE_BAR, GridData.FILL_BOTH, toolkit);

        ArgumentCheck.notNull(pdObject);

        ipsObject = pdObject;
        initControls();
        setText(title);
    }

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

    public IIpsObject getIpsObject() {
        return ipsObject;
    }

    @Override
    protected void performRefresh() {
        partsComposite.refresh();
        refreshSectionMessageIndicator();
    }

    /**
     * Refreshes the message indicator that is attached to the section.
     */
    private void refreshSectionMessageIndicator() {
        try {
            MessageList filteredMessageList = new MessageList();
            if (!(monitoredValidationMessageCodes.isEmpty())) {
                MessageList validationMessageList = getIpsObject().validate(getIpsObject().getIpsProject());
                for (String messageCode : monitoredValidationMessageCodes) {
                    Message searchedErrorMessage = validationMessageList.getMessageByCode(messageCode);
                    if (searchedErrorMessage != null) {
                        filteredMessageList.add(searchedErrorMessage);
                    }
                }
            }
            MessageCueController.setMessageCue(getPartsComposite(), filteredMessageList);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
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

    /**
     * Adds the given validation message code to this section's monitored validation message codes.
     * <p>
     * Should the section encounter a monitored validation error it will render an appropriate
     * indicator image at the section level.
     * <p>
     * Returns true if the validation message code was successfully added or false if the code is
     * already monitored.
     * 
     * @param validationMessageCode The validation message code that should be monitored from now on
     *            by this section
     */
    protected final boolean addMonitoredValidationMessageCode(String validationMessageCode) {
        return monitoredValidationMessageCodes.add(validationMessageCode);
    }

    /**
     * Removes the given validation message code from this section's monitored validation message
     * codes.
     * <p>
     * The section therefore will no longer render an indicator image at the section level if the
     * validation error is encountered.
     * <p>
     * Returns true if the validation message code was successfully removed or false if the code
     * wasn't monitored in the first place.
     * 
     * @param validationMessageCode The validation message code that will no longer be monitored by
     *            this section
     */
    protected final boolean removeMonitoredValidationMessageCode(String validationMessageCode) {
        return monitoredValidationMessageCodes.remove(validationMessageCode);
    }

    /**
     * Returns an unmodifiable view on this section's set of monitored validation message codes.
     */
    protected final Set<String> getMonitoredValidationMessageCodes() {
        return Collections.unmodifiableSet(monitoredValidationMessageCodes);
    }

}
