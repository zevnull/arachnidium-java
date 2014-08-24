package web;

import junit.framework.Assert;

import org.arachnidium.web.google.AnyPage;
import org.arachnidium.web.google.Google;
import org.arachnidium.web.google.LinksAreFoundExceptionThrowing;
import org.arachnidium.web.google.SearchBarExceptionThrowing;
import org.arachnidium.web.google.TestExceptionHandler;
import org.testng.annotations.Test;

public class GoogleExceptionInterceptionTest {
	
	private void workWithGoogle(Google google, boolean toClickOnALinkWhichWasFound) throws Exception{
		TestExceptionHandler eh1 = new TestExceptionHandler();
		TestExceptionHandler eh2 = new TestExceptionHandler();
		
		SearchBarExceptionThrowing<?> sbet = google.getPart(SearchBarExceptionThrowing.class);
		sbet.checkInExceptionHandler(eh1);		
		sbet.performSearch("Hello world Wikipedia");
		Assert.assertEquals(true, eh1.isExceptionCatched);
		Assert.assertEquals(true, eh1.isExceptionHandled);
		eh1.isExceptionCatched = false;
		eh1.isExceptionHandled = false;
		
		LinksAreFoundExceptionThrowing<?> lafet = google.getPart(LinksAreFoundExceptionThrowing.class);
		lafet.checkInExceptionHandler(eh2);
		
		Assert.assertNotSame(0, lafet.getLinkCount());
		Assert.assertEquals(true, eh2.isExceptionCatched);
		Assert.assertEquals(true, eh2.isExceptionHandled);
		eh2.isExceptionCatched = false;
		eh2.isExceptionHandled = false;
		
		
		if (!toClickOnALinkWhichWasFound){
			lafet.openLinkByIndex(1);
		}
		else{
			lafet.clickOnLinkByIndex(1);
		}
		Assert.assertEquals(true, eh2.isExceptionCatched);
		Assert.assertEquals(true, eh2.isExceptionHandled);
		eh2.isExceptionCatched = false;
		eh2.isExceptionHandled = false;
		Assert.assertEquals(false, eh1.isExceptionCatched);
		Assert.assertEquals(false, eh1.isExceptionHandled);
		
		AnyPage anyPage = google.getPart(AnyPage.class, 1);
		anyPage.close();
		Assert.assertEquals(false, eh2.isExceptionCatched);
		Assert.assertEquals(false, eh2.isExceptionHandled);
		Assert.assertEquals(false, eh1.isExceptionCatched);
		Assert.assertEquals(false, eh1.isExceptionHandled);
	}
	
	private void test(Google google, boolean toClickOnALinkWhichWasFound) throws Exception {
		try {
			workWithGoogle(google, toClickOnALinkWhichWasFound);
		} finally {
			google.quit();
		}
	}

	@Test(description = "This test checks exception interception and handling")
	public void exceptionInterceptionTest() throws Exception{
		test(Google.getNew(), false);
	}
}
