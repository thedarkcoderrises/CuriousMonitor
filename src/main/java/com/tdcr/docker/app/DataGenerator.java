package com.tdcr.docker.app;

import com.tdcr.docker.backend.data.EventState;
import com.tdcr.docker.backend.data.Role;
import com.tdcr.docker.backend.data.entity.Event;
import com.tdcr.docker.backend.data.entity.ImageDetails;
import com.tdcr.docker.backend.data.entity.User;
import com.tdcr.docker.backend.repositories.EventsRepository;
import com.tdcr.docker.backend.repositories.ImageRepository;
import com.tdcr.docker.backend.repositories.UserRepository;
import com.vaadin.flow.spring.annotation.SpringComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Random;

@SpringComponent
public class DataGenerator implements HasLogger {


	private final Random random = new Random(1L);

	private UserRepository userRepository;
	private PasswordEncoder passwordEncoder;
	private ImageRepository imageRepository;
	private EventsRepository eventsRepository;

	@Value("${thresholdErrCnt:4}")
	int thresholdErrCnt;

	@Autowired
	public DataGenerator(UserRepository userRepository,
                         PasswordEncoder passwordEncoder, ImageRepository imageRepository, EventsRepository eventsRepository) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.imageRepository = imageRepository;
		this.eventsRepository = eventsRepository;
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
		createEvents();
		getLogger().info("Generated demo data");
	}

	private void createEvents() {
		LocalDate now = LocalDate.now();
		Event event =new Event();
			event.setState(EventState.CREATED);
			event.setDueDate(now);
			event.setDueTime(LocalTime.now());
			event.setContainerName("Curious");
			event.setShortDesc("Curious Monitor is up!");
			event.setImageName("Curious");
		eventsRepository.save(event);
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
