/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.enumcontent;

import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.IpsObjectRefControl;
import org.faktorips.devtools.core.ui.wizards.fixcontent.AssignContentAttributesPage;
import org.faktorips.devtools.core.ui.wizards.fixcontent.DeltaFixWizardStrategy;
import org.faktorips.devtools.core.ui.wizards.fixcontent.Messages;
import org.faktorips.devtools.model.IPartReference;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumContent;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

public class FixEnumWizardStrategy implements DeltaFixWizardStrategy<IEnumType, IEnumAttribute> {

    private IEnumContent enumContent;
    private IEnumType selectedContentType;

    public FixEnumWizardStrategy(IEnumContent enumContent) {
        this.enumContent = enumContent;
    }

    @Override
    public IEnumContent getContent() {
        return enumContent;
    }

    @Override
    public IEnumType findContentType(IIpsProject ipsProject, String qName) {
        if (qName == null && selectedContentType == null) {
            selectedContentType = enumContent.findEnumType(ipsProject);
        } else if (qName != null) {
            selectedContentType = ipsProject.findEnumType(qName);
        }
        return selectedContentType;
    }

    @Override
    public IIpsProject getIpsProject() {
        return enumContent.getIpsProject();
    }

    @Override
    public List<IPartReference> getContentAttributeReferences() {
        return enumContent.getEnumAttributeReferences();
    }

    @Override
    public String getContentAttributeReferenceName(List<IPartReference> contentAttributeReferences, int i) {
        return contentAttributeReferences.get(i).getName();
    }

    @Override
    public int getContentAttributeReferencesCount() {
        return enumContent.getEnumAttributeReferencesCount();
    }

    @Override
    public String getContentTypeString() {
        return Messages.EnumTypeString;
    }

    @Override
    public IpsObjectRefControl createContentTypeRefControl(UIToolkit uitoolkit, Composite workArea) {
        return uitoolkit.createEnumTypeRefControl(getIpsProject(), workArea, false);
    }

    @Override
    public int getContentAttributesCountIncludeSupertypeCopies(IEnumType newContentType, boolean includeLiteralName) {

        return newContentType.getEnumAttributesCountIncludeSupertypeCopies(includeLiteralName);

    }

    @Override
    public List<IEnumAttribute> getContentAttributesIncludeSupertypeCopies(IEnumType newEnumType,
            boolean includeLiteralName) {
        return newEnumType.getEnumAttributesIncludeSupertypeCopies(includeLiteralName);
    }

    @Override
    public void createControl(IEnumType enumType, IpsObjectRefControl contentTypeRefControl) {
        if (enumType != null) {
            if (!(enumType.isAbstract()) && enumType.isExtensible()) {
                contentTypeRefControl.setText(enumType.getQualifiedName());
            }
        }

    }

    @Override
    public boolean checkForCorrectDataType(String currentComboText, int i) {
        if (selectedContentType != null) {
            String datatypeDestination = selectedContentType.findAllEnumAttributes(false, getIpsProject()).get(i)
                    .getDatatype();
            if ("String".equals(datatypeDestination)) { //$NON-NLS-1$
                // all datatypes can be written to a String
                return true;
            }
            // need to cut off the prefix of the combo text
            String modifiedComboText = currentComboText
                    .substring(AssignContentAttributesPage.AVAIABLECOLUMN_PREFIX.length());
            IEnumAttribute enumAttribute = selectedContentType.getEnumAttribute(modifiedComboText);
            if (enumAttribute != null) {
                String datatypeSource = enumAttribute.getDatatype();
                return datatypeDestination.equals(datatypeSource);
            } else {
                // deleted columns can not be validated since their datatype is unknown
                return true;
            }
        }
        return false;
    }

}
