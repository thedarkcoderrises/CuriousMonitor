package com.tdcr.docker.app;

import com.tdcr.docker.backend.data.entity.ImageDetails;
import com.tdcr.docker.backend.repositories.ImageRepository;
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
	private ImageRepository imageRepository;

	@Autowired
	public DataGenerator(UserRepository userRepository,
                         PasswordEncoder passwordEncoder, ImageRepository imageRepository) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.imageRepository = imageRepository;
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
		ImageDetails imgDtl = new ImageDetails("e617a56c238ed06a0215366a122d19fab0b94b28c1413e2171bbe2f883686e6b",
				true);
		imgDtl.setTotalCloseIncidents(5);
		imgDtl.setTotalOpenIncidents(2);
		imageRepository.save(imgDtl
				);
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
