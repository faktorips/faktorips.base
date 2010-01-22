package org.faktorips.devtools.htmlexport.pages.elements.core;

import org.faktorips.devtools.htmlexport.generators.ILayouter;

public abstract class AbstractRootPageElement extends AbstractCompositePageElement {

	@Override
    public void build() {
        reset();
    }

    @Override
    public void acceptLayouter(ILayouter layouter) {
        layouter.layoutRootPageElement(this);
    }
    
    /**
     * 
     * @return Pfad zum Root-Verzeichnis der Dokumentation
     */
    public abstract String getPathToRoot();
}
