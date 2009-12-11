package org.faktorips.devtools.htmlexport.generators;

import org.faktorips.devtools.htmlexport.pages.PageElement;

public interface ILayouter extends IGenerator {
    public void layoutPageElement(PageElement pageElement);

    public void layoutPageElement(PageElement pageElement, LayouterVisitingMode mode);

    public void layoutPageElement(PageElement pageElement, LayouterWrapperType wrapper, LayouterVisitingMode mode);

    public void reset();
}
