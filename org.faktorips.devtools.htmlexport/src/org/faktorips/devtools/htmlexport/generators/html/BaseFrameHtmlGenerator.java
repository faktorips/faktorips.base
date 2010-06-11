package org.faktorips.devtools.htmlexport.generators.html;

import org.faktorips.devtools.htmlexport.generators.AbstractTextGenerator;
import org.faktorips.devtools.htmlexport.helper.html.HtmlUtil;

public class BaseFrameHtmlGenerator extends AbstractTextGenerator {
    private String title;
    private String colDefinition;
    private String rowsDefinition;

    public BaseFrameHtmlGenerator(String title, String colDefinition, String rowsDefinition) {
        super();
        this.title = title;
        this.colDefinition = colDefinition;
        this.rowsDefinition = rowsDefinition;
    }

    @Override
    public String generateText() {
        return HtmlUtil.createDocFrame(title, colDefinition, rowsDefinition);
    }

}
