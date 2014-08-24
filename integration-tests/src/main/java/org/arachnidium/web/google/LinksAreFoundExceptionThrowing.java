package org.arachnidium.web.google;

import org.arachnidium.core.Handle;

public class LinksAreFoundExceptionThrowing<T extends Handle> extends
		LinksAreFound<T> {
	private boolean isMethodInvokedTwice = false;

	protected LinksAreFoundExceptionThrowing(T handle) {
		super(handle);
	}

	private void onInvokeTwice() {
		if (!isMethodInvokedTwice) {
			isMethodInvokedTwice = true;
			throw new TestException();
		}
		isMethodInvokedTwice = false;
	}

	@InteractiveMethod
	public void openLinkByIndex(int index) {
		onInvokeTwice();
		super.openLinkByIndex(index);
	}

	@InteractiveMethod
	public int getLinkCount() {
		onInvokeTwice();
		return super.getLinkCount();
	}

	@InteractiveMethod
	public void clickOnLinkByIndex(int index) {
		onInvokeTwice();
		super.clickOnLinkByIndex(index);
	}

}
