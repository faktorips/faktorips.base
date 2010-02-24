package org.faktorips.devtools.htmlexport.pages.elements.core;

import org.eclipse.swt.graphics.ImageData;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.htmlexport.generators.ILayouter;

public class ImagePageElement extends AbstractPageElement {

	protected ImageData imageData;
	protected String title;
	protected String path;

	public ImagePageElement(IIpsElement element, String title, String path) {
		imageData = createImageDataByIpsElement(element);
		this.title = title;
		this.path = path;
	}

	public ImagePageElement(IIpsElement element) {
		imageData = createImageDataByIpsElement(element);
		title = element.getName();
		path = getIpsElementImageName(element);
	}

	private ImageData createImageDataByIpsElement(IIpsElement element) {
		return IpsUIPlugin.getImageHandling().getImage(element, true).getImageData();
	}

	private String getIpsElementImageName(IIpsElement element) {
		if (element instanceof IIpsPackageFragment) {
			return "ipspackage";
		}
		if (element instanceof IIpsObject) {
			IIpsObject object = (IIpsObject) element;
			return object.getIpsObjectType().getFileExtension();
		}

		return element.getName();
	}

	@Override
	public void acceptLayouter(ILayouter layouter) {
		layouter.layoutImagePageElement(this);
	}

	public ImageData getImageData() {
		return imageData;
	}

	public String getTitle() {
		return title;
	}

	public String getPath() {
		return path;
	}
}
