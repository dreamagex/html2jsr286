package com.celamanzi.liferay.portlets.rails286;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.NameValuePair;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.portlet.MockActionRequest;
import org.springframework.mock.web.portlet.MockActionResponse;
import org.springframework.mock.web.portlet.MockPortletConfig;
import org.springframework.mock.web.portlet.MockPortletContext;
import org.springframework.mock.web.portlet.MockPortletSession;
import org.springframework.mock.web.portlet.MockRenderRequest;
import org.springframework.mock.web.portlet.MockRenderResponse;
import org.springframework.mock.web.portlet.MockResourceRequest;
import org.springframework.mock.web.portlet.MockResourceResponse;

public class PortletTest {

	private Rails286Portlet portlet = new Rails286Portlet();
	private PortletConfig portletConfig = null;
	private PortletContext portletContext = new MockPortletContext();
	private PortletSession session = null;
	private String portletName = "__TEST__";

	protected final static String host    = "http://localhost:3000";
	protected final static String servlet = "";
	protected final static String route   = "/";

	protected final static String railsTestBenchRoute = "/caterpillar/test_bench";
	protected final static String railsJUnitRoute = railsTestBenchRoute+"/junit";
	protected final static String railsJUnitURL = host+servlet+railsJUnitRoute;

	protected final static String sessionKey    = "_example_session";
	protected final static String sessionSecret = 
		"cfb08929b6465bced2081c3387940d07bf7dceac8e38c95ea34bd1e518ca3bce"+
		"7244aafedcefc10b6d8c79b37a0b88f501f237361e45360ff688cad332222ccb";


	@Before
	public void setup()
	throws MalformedURLException
	{
		assertNotNull(portlet);
		assertNotNull(portletContext);
		MockPortletConfig _portletConfig = new MockPortletConfig(portletContext,portletName);
		assertNotNull(_portletConfig);
		portletConfig = (PortletConfig)_portletConfig;

		session = new MockPortletSession();
		assertNotNull(session);

		session.setAttribute("railsBaseUrl",
				new URL(host+"/"+servlet),
				PortletSession.PORTLET_SCOPE);

		session.setAttribute("servlet",
				servlet,
				PortletSession.PORTLET_SCOPE);

		session.setAttribute("railsRoute",
				route,
				PortletSession.PORTLET_SCOPE);

		session.setAttribute("requestMethod",
				null,
				PortletSession.PORTLET_SCOPE);

		session.setAttribute("httpReferer",
				null,
				PortletSession.PORTLET_SCOPE);
	}


	@Test
	public void test_init()
	throws PortletException
	{
		assertNotNull(portlet);
		portlet.init(portletConfig);
	}


	@Test
	/* Baffled?

    The test creates a mock request and response to the portlet.


	 */
	public void test_render()
	throws PortletException, IOException
	{
		portlet.init(portletConfig);

		MockRenderRequest _request = new MockRenderRequest(PortletMode.VIEW);
		_request.setSession(session);
		RenderRequest request = (RenderRequest)_request;
		assertNotNull(request);

		RenderResponse response = new MockRenderResponse();
		assertNotNull(response);

		portlet.render(request,response);

		URL _baseUrl = (URL)session.getAttribute("railsBaseUrl");
		assertNotNull(_baseUrl);
		assertEquals(new URL(host+"/"+servlet),_baseUrl);
		// TODO: test the thing with different combinations

		String _servlet = (String)session.getAttribute("servlet");
		assertNotNull(_servlet);
		assertEquals(servlet,_servlet);

		String _route = (String)session.getAttribute("railsRoute");
		assertNotNull(_route);
		assertEquals(route,_route);

		String _method = (String)session.getAttribute("requestMethod");
		assertNull(_method);

		assertNull(session.getAttribute("httpReferer"));
	}


	@Test
	public void test_processAction()
	throws PortletException, IOException
	{
		portlet.init(portletConfig);

		MockActionRequest _request = new MockActionRequest(PortletMode.VIEW);
		_request.setSession(session);
		ActionRequest request = (ActionRequest)_request;
		assertNotNull(request);

		ActionResponse response = new MockActionResponse();
		assertNotNull(response);

		//portletRequest.addParameter("param1", "value1");

		portlet.processAction(request,response);
		// TODO: re-design and test
	}

	@Test
	public void test_redirect()
	throws IOException, PortletException, MalformedURLException
	{
		portlet.init(portletConfig);

		session.setAttribute("railsBaseUrl",new URL(host));
		session.setAttribute("servlet",servlet);
		session.setAttribute("railsRoute",railsJUnitRoute+"/redirect");

		String targetURI = railsJUnitRoute+"/redirect_target";

		MockRenderRequest _request = new MockRenderRequest(PortletMode.VIEW);
		_request.setSession(session);
		RenderRequest request = (RenderRequest)_request;
		assertNotNull(request);

		RenderResponse response = new MockRenderResponse();
		assertNotNull(response);

		portlet.render(request,response);

		// re-cast to read response body;
		// the body contains the correct value to match with
		MockRenderResponse _response = (MockRenderResponse)response;
		String _targetURI = _response.getContentAsString().trim();

		assertEquals(targetURI,_targetURI);
	}

	@Test
	/** Test that Cookie[] can be stored to PortletSession correctly.
	 */
	public void test_CookiesInPortletSession()
	{
		PortletSession session = new MockPortletSession();
		assertNotNull(session);

		Cookie cookie1 = new Cookie("_domain","_name1","_value1"); 
		Cookie cookie2 = new Cookie("_domain","_name2","_value2"); 
		Cookie[] cookies = {cookie1,cookie2};
		assertEquals(2,cookies.length);

		session.setAttribute("cookies",
				cookies,
				PortletSession.PORTLET_SCOPE);

		Cookie[] _cookies = (Cookie[])session.getAttribute("cookies");
		assertEquals(2,_cookies.length);

		for (int i=0 ; i < cookies.length ; i++) {
			assertEquals(cookies[i],_cookies[i]);
		}
	}

	@Test
	public void test_serveResource_Ajax() throws Exception {
		portlet.init(portletConfig);

		session.setAttribute("railsBaseUrl",new URL(host));
		session.setAttribute("servlet",servlet);
		session.setAttribute("railsRoute",railsJUnitRoute+"/check_xhr");

		MockResourceRequest request = new MockResourceRequest();
		request.setSession(session);
		MockResourceResponse response = new MockResourceResponse();

		portlet.serveResource(request, response);
		assertEquals("true", response.getContentAsString());
	}

	@Test
	public void test_serveResource_download() throws Exception {
		File original = new File("test/resources/jake_sully.jpg");
		
		File directory = new File("../temp/");
		if (!directory.exists()){
			directory.mkdirs();
		}
		
		String tempFilename = "../temp/jake_sully.jpg";
		File download = new File(tempFilename);
		if (download.exists()){
			download.delete();
		}

		portlet.init(portletConfig);

		session.setAttribute("railsBaseUrl",new URL(host));
		session.setAttribute("servlet",servlet);
		session.setAttribute("railsRoute",railsJUnitRoute+"/download_image");

		MockResourceRequest request = new MockResourceRequest();
		request.setSession(session);

		MockResourceResponse response = new MockResourceResponse();

		portlet.serveResource(request, response);

		download = new File(tempFilename);
		InputStream isOriginal = new FileInputStream(original);
		InputStream isDownload = new FileInputStream(download);

		int read = 0;
		while((read = isOriginal.read()) != -1) {
			assertEquals(read, isDownload.read());
		}
		
		download.delete();
		directory.delete();
	}
	
	@Test
	public void test_renderInEditMode() throws Exception {
		portlet.init(portletConfig);
		session.setAttribute("railsRoute", railsJUnitRoute); //preferences
		
		MockRenderRequest _request = new MockRenderRequest(PortletMode.EDIT);
		_request.setSession(session);
		RenderRequest request = (RenderRequest)_request;
		assertNotNull(request);

		RenderResponse response = new MockRenderResponse();
		assertNotNull(response);

		portlet.render(request,response);

		URL _baseUrl = (URL) session.getAttribute("railsBaseUrl");
		assertNotNull(_baseUrl);
		assertEquals(new URL(host+"/"+servlet), _baseUrl);

		assertEquals(railsJUnitRoute+"/preferences", portlet.getRailsRoute());
		
		String _servlet = (String)session.getAttribute("servlet");
		assertNotNull(_servlet);
		assertEquals(servlet, _servlet);

		String _route = (String) session.getAttribute("railsRoute");
		assertNotNull(_route);
		assertEquals(railsJUnitRoute, _route);

		String _method = (String) session.getAttribute("requestMethod");
		assertNull(_method);

		assertNull(session.getAttribute("httpReferer"));
		assertEquals("Preferences view\n", ((MockRenderResponse)response).getContentAsString());
	}
	
	@Test
	public void test_processActionInEditMode() throws Exception {
		portlet.init(portletConfig);
		session.setAttribute("railsRoute", railsJUnitRoute); //preferences
		
		MockActionRequest request = new MockActionRequest(PortletMode.EDIT);
		ActionResponse response = new MockActionResponse();
		request.setSession(session);

		request.addParameter("originalActionUrl", railsJUnitRoute+"/preferences");
		request.addParameter("originalActionMethod", "post");
		
		portlet.processAction(request,response);
		
		MockRenderRequest renderRequest = new MockRenderRequest(PortletMode.EDIT);
		MockRenderResponse renderResponse = new MockRenderResponse();
		renderRequest.setSession(session);
		
		renderRequest.setAttribute("requestMethod", "post");
		renderRequest.setAttribute("railsRoute", railsJUnitRoute+"/preferences");
		
		portlet.render(renderRequest, renderResponse);
		assertEquals("Preferences view\n", renderResponse.getContentAsString());
	}
	
	@Test
	public void test_processActionWithPublicRenderParameters() throws Exception {
		portlet.init(portletConfig);
		session.setAttribute("railsRoute", railsJUnitRoute);
		
		MockActionRequest request = new MockActionRequest();
		
		// Public render parameters need to have the _prp sufix in their names
		request.addParameter("tag_prp", "public render parameter value");
		request.addParameter("param", "common value");
		
		MockActionResponse response = new MockActionResponse();
		request.setSession(session);

		request.addParameter("originalActionUrl", railsJUnitRoute+"/public_render_parameters");
		request.addParameter("originalActionMethod", "post");

		portlet.processAction(request, response);
		
		assertEquals(1, response.getRenderParameterMap().size());
		
		// The sufix _prp is removed when the value is treated
		assertEquals("public render parameter value", response.getRenderParameter("tag"));
		
		// The attributes remain in prp parametersBody
		NameValuePair[] parametersBody = (NameValuePair[]) request.getAttribute("parametersBody");
		assertEquals(2, parametersBody.length);
	}

	@Test
	public void test_unicode_parameters_processAction() throws PortletException, IOException
	{
		portlet.init(portletConfig);

		MockActionRequest request = new MockActionRequest();
		request.setSession(session);
		request.setContentType("application/x-www-form-urlencoded");

		request.setParameter("originalActionUrl", "/");
		request.setParameter("postcode", "è");

		ActionResponse response = new MockActionResponse();
		portlet.processAction(request, response);

		NameValuePair[] m = (NameValuePair[])request.getAttribute("parametersBody");
		assertEquals("name=postcode, value=è", m[0].toString());
	}

	@Test
	/** If document type is UTF-8, but the form accept-charset is ISO-8859-1,
	 * IE will happily send data encoded as "Windows-1252".
	 */
	public void test_processAction_IEhack() throws PortletException, IOException
	{
		portlet.init(portletConfig);

		MockActionRequest request = new MockActionRequest();
		request.setSession(session);
		request.setContentType("application/x-www-form-urlencoded");

		request.setParameter("originalActionUrl", "/");
		request.setParameter("postcode", new String("è".getBytes("UTF-8"), "windows-1252"));
		request.setParameter("_encoding_", "CP1252");

		ActionResponse response = new MockActionResponse();
		portlet.processAction(request, response);

		NameValuePair[] params = (NameValuePair[])request.getAttribute("parametersBody");
		for (NameValuePair p : params) {
			if (p.getName() == "postcode") {
				assertEquals("è", (String)p.getValue());
			}
		}
	}
}
















