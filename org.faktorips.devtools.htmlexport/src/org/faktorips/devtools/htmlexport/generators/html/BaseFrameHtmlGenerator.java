/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.generators.html;

import org.faktorips.devtools.htmlexport.generators.AbstractTextGenerator;
import org.faktorips.devtools.htmlexport.helper.html.HtmlUtil;

/**
 * Generator for Html-Frames
 * 
 * @author dicker
 */
public class BaseFrameHtmlGenerator extends AbstractTextGenerator {
    private String title;
    private String colDefinition;
    private String rowsDefinition;
    private HtmlUtil htmlUtil = new HtmlUtil();

    public BaseFrameHtmlGenerator(String title, String colDefinition, String rowsDefinition) {
        super();
        this.title = title;
        this.colDefinition = colDefinition;
        this.rowsDefinition = rowsDefinition;
    }

    @Override
    public String generateText() {
        return htmlUtil.createDocFrame(title, colDefinition, rowsDefinition);
    }

}
