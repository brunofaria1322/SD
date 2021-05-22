/**
 * Raul Barbosa 2014-11-07
 */
package WebInterface.action;

import WebInterface.model.WebServer;
import com.github.scribejava.apis.FacebookApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;

import java.io.IOException;
import java.io.Serial;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class AddFacebookAction extends ActionSupport implements SessionAware {
	@Serial
	private static final long serialVersionUID = 4L;
	private Map<String, Object> session;
	private String username = null, password = null, code = null;
	private static final String NETWORK_NAME = "Facebook";
	private static final String PROTECTED_RESOURCE_URL = "https://graph.facebook.com/v3.2/me";
	@Override
	public String execute() throws IOException {
		try {
			if(code!=null){
				// Step 1: Create Facebook Account
				// Step 2: Create application (https://developers.facebook.com/ Log In -> Get Started)
				// Step 3: Replace below with your app key and secret
				final String appKey = "2913897935402035";
				final String appSecret = "192d0ba9d4d3565ff60c1c5eef150602";
				final String secretState = "secret" + new Random().nextInt(999_999);
				final OAuth20Service service = new ServiceBuilder(appKey)
						.apiSecret(appSecret)
						.callback("http://localhost:8080/eVoting/AddFacebookAction")
						.build(FacebookApi.instance());

				final OAuth2AccessToken accessToken = service.getAccessToken(code);

				final OAuthRequest request = new OAuthRequest(Verb.GET, PROTECTED_RESOURCE_URL);
				service.signRequest(accessToken, request);
				try (Response response = service.execute(request)) {
					Type type = new TypeToken<HashMap<String, String>>(){}.getType();
					HashMap<String, String> body = new Gson().fromJson(response.getBody(), type);
					getWebServer().addFacebook(username,body.get("id"));
				}
			}
			if(session.containsKey("admin") && (boolean)session.get("admin")){
				return "admin";
			}
			return "voter";
		}
		catch (Exception e){
			if(!getWebServer().connect()){
				System.out.println(e);
				session.put("error","The server is down. Sorry...");
				return ERROR;
			}
			return this.execute();
		}


	}
	
	public void setUsername(String username) {
		this.username = username; // will you sanitize this input? maybe use a prepared statement?
	}

	public void setPassword(String password) {
		this.password = password; // what about this input? 
	}
	
	public WebServer getWebServer() throws IOException {
		if(!session.containsKey("WebServer"))
			this.setWebServer(new WebServer());
		return (WebServer) session.get("WebServer");
	}

	public void setWebServer(WebServer WebServer) throws IOException {
		this.session.put("WebServer", WebServer);
		getWebServer().readConfig();
		getWebServer().connect();
	}

	@Override
	public void setSession(Map<String, Object> session) {
		this.session = session;
	}

	public void setCode(String code) {
		this.code = code;
	}
}
