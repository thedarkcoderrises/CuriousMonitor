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
		setSocatSubscription();
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


	private void setSocatSubscription() {
		ImageDetails imgDtl = new ImageDetails("e617a56c238ed06a0215366a122d19fab0b94b28c1413e2171bbe2f883686e6b",
				true,thresholdErrCnt,"LOCAL_DD","529facf5325a5bc85f928f094354b3c677841e638eb3b5b059cea2907bceee40");
		imageRepository.save(imgDtl);

		/*ImageDetails imgDtl2 = new ImageDetails("2760d6ae57c103b15b4c886eeb5080d969a85dfb144f0dd7fd7e5fc01ce3fee8",
				true,thresholdErrCnt,"LOCAL_DD",0);
		imageRepository.save(imgDtl2);*/
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
