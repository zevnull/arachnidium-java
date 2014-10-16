package com.github.arachnidium.core;

import java.util.Set;

import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.UnreachableBrowserException;
import org.openqa.selenium.support.ui.ExpectedCondition;

import com.github.arachnidium.core.bean.MainBeanConfiguration;
import com.github.arachnidium.core.fluenthandle.FluentWindowWaiting;
import com.github.arachnidium.core.settings.WindowIsClosedTimeOut;

public final class WindowManager extends Manager<HowToGetBrowserWindow> {

	private static long TIME_OUT_TO_SWITCH_ON = 2; //two seconds
	
	public WindowManager(WebDriverEncapsulation initialDriverEncapsulation) {
		super(initialDriverEncapsulation);
		handleWaiting = new FluentWindowWaiting();
	}

	/**
	 * Changes active window
	 * @see com.github.arachnidium.core.Manager#changeActive(java.lang.String)
	 */
	@Override
	void changeActive(String handle) throws NoSuchWindowException,
			UnhandledAlertException {
		Set<String> handles = getHandles();
		if (!handles.contains(handle))
			throw new NoSuchWindowException("There is no window with handle "
					+ handle + "!");
		try {
			awaiting.awaitCondition(TIME_OUT_TO_SWITCH_ON, isSwithedOn(handle));
		} catch (UnhandledAlertException | NoSuchWindowException e) {
			throw e;
		}
		catch (TimeoutException e) {
			throw new WebDriverException("Can't to switch on window handle " + handle, e);
		}
	}

	/**
	 * Closes browser window
	 * 
	 * @param handle
	 *            Given string wibdow handle
	 * 
	 * @throws UnclosedWindowException
	 *             If window is not closed
	 * @throws NoSuchWindowException
	 *             If window has been already closed
	 * @throws UnhandledAlertException
	 * @throws UnreachableBrowserException
	 */
	synchronized void close(String handle) throws UnclosedWindowException,
			NoSuchWindowException, UnhandledAlertException,
			UnreachableBrowserException {
		long timeOut = getTimeOut(getWebDriverEncapsulation().getWrappedConfiguration()
				.getSection(WindowIsClosedTimeOut.class)
				.getWindowIsClosedTimeOutTimeOut());

		try {
			changeActive(handle);
			WebDriver driver = getWrappedDriver();
			driver.switchTo().window(handle).close();
		} catch (UnhandledAlertException | NoSuchWindowException e) {
			throw e;
		}

		try {
			awaiting.awaitCondition(timeOut, isClosed(handle));
		} catch (TimeoutException e) {
			throw new UnclosedWindowException("Window hasn't been closed!", e);
		}

		int actualWinCount = 0;
		try {
			actualWinCount = getHandles().size();
		} catch (WebDriverException e) { // if all windows are closed
			actualWinCount = 0;
		} finally {
			if (actualWinCount == 0) {
				destroy();
				getWebDriverEncapsulation().destroy();
			}
		}
	}

	/**
	 * @see com.github.arachnidium.core.Manager#getHandle(int)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public BrowserWindow getHandle(int windowIndex)
			throws NoSuchWindowException {		
		Long time = getTimeOut(getHandleWaitingTimeOut()
				.getHandleWaitingTimeOut());
		return getHandle(windowIndex, time);
	}

	/**
	 * @see com.github.arachnidium.core.Manager#getHandles()
	 */
	@Override
	Set<String> getHandles() {
		return getWrappedDriver().getWindowHandles();
	}

	/**
	 * @see com.github.arachnidium.core.Manager#getHandle(com.github.arachnidium.core.fluenthandle.IHowToGetHandle)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public BrowserWindow getHandle(HowToGetBrowserWindow howToGet)
			throws NoSuchWindowException {
		Long time = getTimeOut(getHandleWaitingTimeOut()
				.getHandleWaitingTimeOut());
		return getHandle(time,howToGet);
	}

	/**
	 *  Actual strategy is {@link HowToGetBrowserWindow}
	 * 
	 * @see com.github.arachnidium.core.Manager#getHandle(long,
	 *      com.github.arachnidium.core.fluenthandle.IHowToGetHandle)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public BrowserWindow getHandle(long timeOut,
			HowToGetBrowserWindow howToGet)
			throws NoSuchWindowException {
		String handle = this.getStringHandle(timeOut,
				howToGet);
		BrowserWindow initedWindow = (BrowserWindow) Handle.isInitiated(
				handle, this);
		if (initedWindow != null) {
			return initedWindow;
		}
		BrowserWindow window = new BrowserWindow(getStringHandle(timeOut,
				howToGet), this);
		return returnNewCreatedListenableHandle(window,
				MainBeanConfiguration.WINDOW_BEAN);
	}

	/**
	 * Actual strategy is {@link HowToGetBrowserWindow}
	 * 
	 * @see com.github.arachnidium.core.Manager#getStringHandle(long,
	 *      com.github.arachnidium.core.fluenthandle.IHowToGetHandle)
	 */
	@Override
	String getStringHandle(long timeOut,
			HowToGetBrowserWindow howToGet)
			throws NoSuchWindowException {
		HowToGetBrowserWindow clone = howToGet.cloneThis();
		try {
			return awaiting.awaitCondition(timeOut,
					clone.getExpectedCondition(handleWaiting));
		} catch (TimeoutException e) {
			throw new NoSuchWindowException("Can't find window! Condition is "
					+ clone.toString(), e);
		}
	}

	// is browser window closed?
	private static Boolean isClosed(final WebDriver from, String handle) {
		Set<String> handles;
		try {
			handles = from.getWindowHandles();
		} catch (WebDriverException e) { // if all windows are closed
			return true;
		}

		if (!handles.contains(handle))
			return true;
		else
			return null;
	}
	
	//is window switched on?
	private static Boolean isSwithedOn(final WebDriver from, String handle) {
		from.switchTo().window(handle);
		if (from.getWindowHandle().equals(handle))
			return true;
		else
			return null;
	}

	// fluent waiting for the result. See above
	private static ExpectedCondition<Boolean> isClosed(final String closingHandle) {
		return from -> isClosed(from, closingHandle);
	}
	
	private static ExpectedCondition<Boolean> isSwithedOn(final String handle) {
		return from -> isSwithedOn(from, handle);
	}	

	/**
	 * @see com.github.arachnidium.core.Manager#getHandle(int, long)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public BrowserWindow getHandle(int windowIndex, long timeOut) {
		String handle = this.getStringHandle(windowIndex, timeOut);
		BrowserWindow initedWindow = (BrowserWindow) Handle.isInitiated(handle,
				this);
		if (initedWindow != null)
			return initedWindow;
		BrowserWindow window = new BrowserWindow(handle, this);
		return returnNewCreatedListenableHandle(window,
				MainBeanConfiguration.WINDOW_BEAN);
	}

	/**
	 * @see com.github.arachnidium.core.Manager#getStringHandle(int, long)
	 */
	@Override
	String getStringHandle(int windowIndex, long timeOut) {
		HowToGetBrowserWindow f = new HowToGetBrowserWindow();
		f.setExpected(windowIndex);
		return getStringHandle(timeOut, f);
	}
}