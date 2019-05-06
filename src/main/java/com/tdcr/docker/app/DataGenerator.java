package com.tdcr.docker.app;

import com.tdcr.docker.backend.data.entity.Subscription;
import com.tdcr.docker.backend.repositories.SubscriptionRepository;
import com.tdcr.docker.backend.repositories.UserRepository;
import com.tdcr.docker.backend.data.Role;
import com.tdcr.docker.backend.data.entity.User;
import com.vaadin.flow.spring.annotation.SpringComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.PostConstruct;
import java.util.Random;

@SpringComponent
public class DataGenerator implements HasLogger {


	private final Random random = new Random(1L);

	private UserRepository userRepository;
	private PasswordEncoder passwordEncoder;
	private SubscriptionRepository subscriptionRepository;

	@Autowired
	public DataGenerator( UserRepository userRepository,
                         PasswordEncoder passwordEncoder,SubscriptionRepository subscriptionRepository) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.subscriptionRepository = subscriptionRepository;
		//529facf5325a5bc85f928f094354b3c677841e638eb3b5b059cea2907bceee40
	}

	@PostConstruct
	public void loadData() {
		if (userRepository.count() != 0L) {
			getLogger().info("Using existing database");
			return;
		}
		getLogger().info("Generating demo data");
		getLogger().info("... generating users");
		createAdmin(userRepository, passwordEncoder);
		createChecker(userRepository,passwordEncoder);
		setSocatSubscription();
		getLogger().info("Generated demo data");
	}

	private void setSocatSubscription() {
		subscriptionRepository.save(
				new Subscription(
						"529facf5325a5bc85f928f094354b3c677841e638eb3b5b059cea2907bceee40",
						true));
	}


	private User createAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return userRepository.save(
				createUser("curious@tdcr.com", "Curious", "Monitor",
						passwordEncoder.encode("admin"), Role.ADMIN, true));
	}

	private User createChecker(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return userRepository.save(
				createUser("checker@tdcr.com", "Curious", "Checker",
						passwordEncoder.encode("checker"), Role.CHECKER, true));
	}
	private User createUser(String email, String firstName, String lastName, String passwordHash, String role,
							boolean locked) {
		User user = new User();
		user.setEmail(email);
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setPasswordHash(passwordHash);
		user.setRole(role);
		user.setLocked(locked);
		return user;
	}
}
