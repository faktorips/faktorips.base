/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.enumcontent;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.ExtensionPropertyControlFactory;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.devtools.model.enums.IEnumContent;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.runtime.MessageList;
import org.faktorips.util.ArgumentCheck;

/**
 * The general info section for the <code>EnumContentEditor</code>. It shows the
 * <code>IEnumType</code> the <code>IEnumContent</code> to edit is built upon and provides
 * navigation to this <code>IEnumType</code>.
 * 
 * @see EnumContentEditor
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumContentGeneralInfoSection extends IpsSection {

    /** The label showing the base <code>IEnumType</code>. */
    Label enumTypeLabel;

    /** The <code>IEnumContent</code> the editor is currently editing. */
    private IEnumContent enumContent;

    /** The wizard page of the <code>EnumContentEditor</code>. */
    private EnumContentEditorPage enumContentEditorPage;

    /** The extension property control factory that may extend the controls. */
    private ExtensionPropertyControlFactory extFactory;

    /**
     * Creates a new <code>EnumContentGeneralInfoSection</code> using the specified parameters.
     * 
     * @param enumContentEditorPage The wizard page of the <code>EnumContentEditor</code>.
     * @param enumContent The <code>IEnumContent</code> the <code>EnumContentEditor</code> is
     *            currently editing.
     * @param parent The parent UI composite to attach this info section to.
     * @param toolkit The UI toolkit to be used to create new UI elements.
     * 
     * @throws NullPointerException If <code>enumContent</code> is <code>null</code>.
     */
    public EnumContentGeneralInfoSection(EnumContentEditorPage enumContentEditorPage, IEnumContent enumContent,
            Composite parent, UIToolkit toolkit) {

        super(parent, ExpandableComposite.TITLE_BAR, GridData.FILL_HORIZONTAL, toolkit);
        ArgumentCheck.notNull(enumContent);

        this.enumContentEditorPage = enumContentEditorPage;
        this.enumContent = enumContent;

        extFactory = new ExtensionPropertyControlFactory(enumContent);
        initControls();
        setText(Messages.EnumContentGeneralInfoSection_title);
    }

    @Override
    protected void initClientComposite(Composite client, UIToolkit toolkit) {
        client.setLayout(new GridLayout(1, false));
        Composite composite = toolkit.createLabelEditColumnComposite(client);

        if (IpsPlugin.getDefault().getIpsPreferences().canNavigateToModelOrSourceCode()) {
            Hyperlink link = toolkit.createHyperlink(composite, Messages.EnumContentGeneralInfoSection_linkEnumType);
            link.addHyperlinkListener(new HyperlinkAdapter() {
                @Override
                public void linkActivated(HyperlinkEvent event) {
                    // If the setting has been changed while the editor was opened.
                    if (!(IpsPlugin.getDefault().getIpsPreferences().canNavigateToModelOrSourceCode())) {
                        return;
                    }
                    IEnumType enumType;
                    enumType = enumContent.findEnumType(enumContent.getIpsProject());
                    if (enumType != null) {
                        IpsUIPlugin.getDefault().openEditor(enumType);
                    }
                }
            });
        } else {
            toolkit.createLabel(composite, Messages.EnumContentGeneralInfoSection_linkEnumType);
        }

        enumTypeLabel = toolkit.createLabel(composite, enumContent.getEnumType());
        getBindingContext().bindContent(enumTypeLabel, enumContent, IEnumContent.PROPERTY_ENUM_TYPE);

        createExtensionProperty(toolkit, composite);
    }

    private void createExtensionProperty(UIToolkit toolkit, Composite composite) {
        extFactory.createControls(composite, toolkit, enumContent);
        extFactory.bind(getBindingContext());
    }

    @Override
    protected void performRefresh() {
        IIpsProject ipsProject = enumContent.getIpsProject();
        MessageList validationMessages = enumContent.validate(ipsProject);
        if (validationMessages.getMessageByCode(IEnumContent.MSGCODE_ENUM_CONTENT_ENUM_TYPE_DOES_NOT_EXIST) != null
                || validationMessages
                        .getMessageByCode(
                                IEnumContent.MSGCODE_ENUM_CONTENT_REFERENCED_ENUM_ATTRIBUTES_COUNT_INVALID) != null) {
            enumContentEditorPage.getEnumValuesSection().reinit();
        }
        enumTypeLabel.pack();
    }

}
