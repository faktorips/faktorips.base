/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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
            // TODO catch Exception needs to be documented properly or specialized
            IpsPlugin.log(e);
        }
    }

}
