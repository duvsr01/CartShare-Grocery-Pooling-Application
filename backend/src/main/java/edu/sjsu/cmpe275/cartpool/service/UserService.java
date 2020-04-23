package edu.sjsu.cmpe275.cartpool.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import edu.sjsu.cmpe275.cartpool.repository.UserRepository;
import edu.sjsu.cmpe275.cartpool.dto.User;
import edu.sjsu.cmpe275.cartpool.Constants;

@Service
public class UserService {

	@Autowired
	private UserRepository userRepository;

	@Transactional
	public User createUser(User user) {
		if (userRepository.existsUserByUidOrEmail(user.getUid(), user.getEmail())) {
			User existingUser = userRepository.findUserByEmail(user.getEmail());
			return existingUser;
		}

		String email = user.getEmail();
		String domain = email.substring(email .indexOf("@") + 1);
		if(domain.equals(Constants.DOMAIN)) {
			user.setRole(Constants.ADMIN_ROLE);
		} else {
			user.setRole(Constants.POOLER_ROLE);
		}
		return userRepository.save(user);
	}
	
	public User getUserByEmail(String email) {
		System.out.println(email);
		User user = userRepository.findByEmail(email);
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
		}
		return user;
	}
}
