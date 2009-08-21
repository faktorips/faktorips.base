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

package org.faktorips.devtools.core.ui.editors.enums;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TableViewer;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumLiteralNameAttribute;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.util.ArgumentCheck;

/**
 * This action is used in the context menu of the <tt>EnumValuesSection</tt>. It enables the user to
 * reset all literal names to their default values.
 * <p>
 * The action is only applicable for <tt>IEnumType</tt>s
 * 
 * @see EnumValuesSection
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.4
 */
public class ResetLiteralNamesAction extends Action {

    /** The name of the image for the action. */
    private final String IMAGE_NAME = "Refresh.gif";

    /** The table viewer linking the enumeration values UI table widget with the model data. */
    private TableViewer enumValuesTableViewer;

    /** The <tt>IEnumType</tt> being edited. */
    private IEnumType enumType;

    /**
     * Creates a new <tt>ResetLiteralNamesAction</tt>.
     * 
     * @param enumValuesTableViewer The table viewer linking the table widget with the model data.
     * @param enumType The <tt>IEnumType</tt> being edited.
     * 
     * @throws NullPointerException If <tt>enumValuesTableViewer</tt> or <tt>enumType</tt> is
     *             <tt>null</tt>.
     */
    public ResetLiteralNamesAction(TableViewer enumValuesTableViewer, IEnumType enumType) {
        super();
        ArgumentCheck.notNull(enumValuesTableViewer);

        this.enumValuesTableViewer = enumValuesTableViewer;
        this.enumType = enumType;

        setImageDescriptor(IpsUIPlugin.getDefault().getImageDescriptor(IMAGE_NAME));
        setText(Messages.EnumValuesSection_labelResetLiteralNames);
        setToolTipText(Messages.EnumValuesSection_tooltipResetLiteralNames);
    }

    @Override
    public void run() {
        IEnumLiteralNameAttribute literalNameAttribute = enumType.getEnumLiteralNameAttribute();
        if (literalNameAttribute == null) {
            return;
        }

        IEnumAttribute defaultProviderAttribute = enumType.getEnumAttributeIncludeSupertypeCopies(literalNameAttribute
                .getDefaultValueProviderAttribute());
        int indexDefaultProvider = (defaultProviderAttribute == null) ? -1 : enumType
                .getIndexOfEnumAttribute(defaultProviderAttribute);
        int indexLiteralName = enumType.getIndexOfEnumAttribute(literalNameAttribute);
        for (IEnumValue currentEnumValue : enumType.getEnumValues()) {
            List<IEnumAttributeValue> attributeValues = currentEnumValue.getEnumAttributeValues();
            String defaultValue = (indexDefaultProvider == -1) ? IpsPlugin.getDefault().getIpsPreferences()
                    .getNullPresentation() : attributeValues.get(indexDefaultProvider).getValueAsLiteralName();
            IEnumAttributeValue literalNameAttributeValue = attributeValues.get(indexLiteralName);
            literalNameAttributeValue.setValue(defaultValue);
        }

        enumValuesTableViewer.refresh();
    }

}
