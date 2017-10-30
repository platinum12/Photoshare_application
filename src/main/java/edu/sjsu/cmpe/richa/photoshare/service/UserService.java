package edu.sjsu.cmpe.richa.photoshare.service;

import java.util.List;

import edu.sjsu.cmpe.richa.photoshare.model.CreateAccountResponse;
import edu.sjsu.cmpe.richa.photoshare.model.User;

public interface UserService {
	
	/**
	 * Determine if User identified by specified username and password is valid on Server.
	 * If Valid, return the User model.
	 * @param username
	 * @param password
	 * @return User model is user is valid. Else NULL.
	 */
	User validateAndGetUser(String username, String password);
	
	/**
	 * Create new User Account on Server.
	 * @param newUser
	 * @return CreateAccountResponse containing the response from Server.
	 */
	CreateAccountResponse createNewUser(User newUser);
	
	/**
	 * Return List of all Users registered on Server.
	 * @return
	 */
	List<User> getUsers();
	
	/**
	 * Get the Registered User from Server, identifier by given username.
	 * @param username
	 * @return
	 */
	User getUser(String username);
	
	/**
	 * Add logged-in User to Cache.
	 * User will be removed from Cache once he Logs out.
	 *
	 * @param loggedInUser
	 */
	void addLoggedInUserToCache(User loggedInUser);
	
	/**
	 * Removed loggedOut User from Cache. 
	 * @param loggedInUser
	 */
	void removeLoggedOutUserFromCache(User loggedOut);

}
