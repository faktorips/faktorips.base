/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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
import org.faktorips.devtools.core.model.value.ValueFactory;
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
    private static final String IMAGE_NAME = "Refresh.gif"; //$NON-NLS-1$

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

        setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(IMAGE_NAME));
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
        int indexDefaultProvider = (defaultProviderAttribute == null) ? -1 : enumType.getIndexOfEnumAttribute(
                defaultProviderAttribute, true);
        int indexLiteralName = enumType.getIndexOfEnumAttribute(literalNameAttribute, true);
        for (IEnumValue currentEnumValue : enumType.getEnumValues()) {
            List<IEnumAttributeValue> attributeValues = currentEnumValue.getEnumAttributeValues();
            String nullPresentation = IpsPlugin.getDefault().getIpsPreferences().getNullPresentation();
            IEnumAttributeValue defaultProviderAttributeValue = attributeValues.get(indexDefaultProvider);
            String defaultProviderValue = indexDefaultProvider == -1 ? nullPresentation : defaultProviderAttributeValue
                    .getValue().getDefaultLocalizedContent(defaultProviderAttributeValue.getIpsProject());
            String literalNameValue = defaultProviderValue.equals(nullPresentation) ? null : enumType.getIpsProject()
                    .getJavaNamingConvention().getEnumLiteral(defaultProviderValue);
            attributeValues.get(indexLiteralName).setValue(ValueFactory.createStringValue(literalNameValue));
        }

        enumValuesTableViewer.refresh();
    }

}
