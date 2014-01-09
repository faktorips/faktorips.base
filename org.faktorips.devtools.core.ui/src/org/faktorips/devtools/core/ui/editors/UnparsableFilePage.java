/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.util.StringUtil;

/**
 * A page to show the contents of an unparsable file.
 * 
 * @author Jan Ortmann
 */
public class UnparsableFilePage extends IpsObjectEditorPage {

    public final static String PAGE_ID = "UnparsableFile"; //$NON-NLS-1$

    public UnparsableFilePage(IpsObjectEditor editor) {
        super(editor, PAGE_ID, "UnparsableFileContents"); //$NON-NLS-1$
    }

    @Override
    protected void createPageContent(Composite formBody, UIToolkit toolkit) {
        formBody.setLayout(createPageLayout(1, false));
        toolkit.createLabel(formBody, Messages.UnparsableFilePage_fileContentIsNotParsable);
        Text xmlText = toolkit.createMultilineText(formBody);
        try {
            String charSet = ((IpsObjectEditor)getEditor()).getIpsProject().getXmlFileCharset();
            String text = StringUtil.readFromInputStream(getIpsObjectEditor().getIpsSrcFile()
                    .getContentFromEnclosingResource(), charSet);
            xmlText.setText(text);
        } catch (Exception e) {
            IpsPlugin.log(e);
        }
    }

}
