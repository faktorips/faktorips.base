package org.faktorips.devtools.htmlexport.pages.elements.core;

import org.eclipse.swt.graphics.ImageData;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.htmlexport.generators.ILayouter;

/**
 * A {@link PageElement} representing an image for an {@link IIpsElement}.
 * @author dicker
 *
 */
public class ImagePageElement extends AbstractPageElement {

	protected ImageData imageData;
	protected String title;
	protected String fileName;

	/**
	 * @param element
	 * @param title
	 * @param path
	 */
	public ImagePageElement(IIpsElement element, String title, String path) {
		imageData = createImageDataByIpsElement(element);
		this.title = title;
		this.fileName = path;
	}

	/**
	 * @param element
	 */
	public ImagePageElement(IIpsElement element) {
		this(element, element.getName(), getIpsElementImageName(element));
	}

	private ImageData createImageDataByIpsElement(IIpsElement element) {
		return IpsUIPlugin.getImageHandling().getImage(element, true).getImageData();
	}

	private static String getIpsElementImageName(IIpsElement element) {
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

	/**
	 * @return {@link ImageData} of the image
	 */
	public ImageData getImageData() {
		return imageData;
	}

	/**
	 * @return title of the image
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @return fileName of the image
	 */
	public String getFileName() {
		return fileName;
	}
}
