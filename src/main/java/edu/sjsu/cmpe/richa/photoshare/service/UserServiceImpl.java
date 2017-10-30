package edu.sjsu.cmpe.richa.photoshare.service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.apache.commons.collections4.CollectionUtils;

import edu.sjsu.cmpe.richa.photoshare.model.CreateAccountResponse;
import edu.sjsu.cmpe.richa.photoshare.model.User;

@Service
public class UserServiceImpl implements UserService {
	
	private static final Logger logger = LoggerFactory.getLogger(edu.sjsu.cmpe.richa.photoshare.service.UserServiceImpl.class);
	
	@Value( "${photoshare.webserver.base.url}" )
	private String baseUrl;
	
	@Value( "${photoshare.webservice.validateUser.resource}" )
	private String validateUserResource;
	
	@Value( "${photoshare.webservice.signUpUser.resource}" )
	private String signUpUserResource;
	
	@Value( "${photoshare.webservice.users.resource}" )
	private String usersResource;
	
	private static final ConcurrentMap<String, User> loggedInUsersCache = new ConcurrentHashMap<String, User>();

	public User validateAndGetUser(String username, String password) {
		
		RestTemplate restTemplate = new RestTemplate();
		MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
		map.add("username", username);
		map.add("password", password);
		
		String validateUserUrl = baseUrl + validateUserResource;
		logger.debug("Trying to get user from URL : " + validateUserUrl + " with request params : " + map);
		User user = null;
		try {
			user = restTemplate.postForObject(validateUserUrl, map, User.class);
			logger.debug("Got user : " + user);
		} catch (RestClientException rcex) {
			logger.error("Unable to POST request {} to server!", validateUserUrl);
		} catch (Exception ex) {
			logger.error("Exception when trying to POST request {} to server!", validateUserUrl);
		}
		

		return user;
	}

	public List<User> getUsers() {
		// TODO Auto-generated method stub
		return null;
	}

	public User getUser(String username) {
		RestTemplate restTemplate = new RestTemplate();
		//eg : http://localhost/users.php?username=username;
		String usersUrl = baseUrl + usersResource + "?username="+username;
		logger.debug("Trying to get user from URL : " + usersUrl);
		//User user = restTemplate.getForObject(usersUrl, User.class);
		List<User> users = (List<User>) restTemplate.getForObject(usersUrl, Object.class);
		logger.debug("Got user : " + users);
		return CollectionUtils.isEmpty(users) ? null : users.get(0);
	}
	
	public CreateAccountResponse createNewUser(User newUser) {
		RestTemplate restTemplate = new RestTemplate();
		MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
		map.add("username", newUser.getUsername());
		map.add("password", newUser.getPassword());
		map.add("first_name", newUser.getFirstName());
		map.add("last_name", newUser.getLastName());
		map.add("email_id", newUser.getEmailId());
		map.add("status", "active"); // set status as Active for new user.
		
		String createUserUrl = baseUrl + signUpUserResource;
		logger.debug("Trying to get user from URL : " + createUserUrl + " with request params : " + map);
		CreateAccountResponse respose = null;
		try {
			respose = restTemplate.postForObject(createUserUrl, map, CreateAccountResponse.class);
			logger.debug("Create User API response : " + respose);
		} catch (RestClientException rcex) {
			logger.error("Unable to POST request {} to server!", createUserUrl);
		} catch (Exception ex) {
			logger.error("Exception when trying to POST request {} to server!", createUserUrl);
		}
		
		return respose;
	}
	
	public void addLoggedInUserToCache(User loggedInUser) {
		loggedInUsersCache.putIfAbsent(loggedInUser.getUsername(), loggedInUser);
		logger.info("Added LoggedIn User {} to Cache", loggedInUser.getUsername());
	}
	
	public void removeLoggedOutUserFromCache(User loggedOutUser) {
		loggedInUsersCache.remove(loggedOutUser.getUsername());
		logger.info("Removed LoggedOut User {} from Cache", loggedOutUser.getUsername());
	}

}
