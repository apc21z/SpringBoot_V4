package com.apc21z.proyectv4;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;

import com.apc21z.proyectv4.rest.model.Person;
import com.apc21z.proyectv4.rest.model.Skill;
import com.apc21z.proyectv4.rest.repository.PersonRepository;
import com.apc21z.proyectv4.rest.service.UserAccountService;
import com.apc21z.proyectv4.web.GlobalViewsExceptions;

@SpringBootTest(properties = "security.jwt.secret=test-secret-for-v4-integration-tests-only-123456")
@AutoConfigureMockMvc(addFilters = false)
class ProyectV4ApplicationTests {

	@Autowired
	private PersonRepository personRepository;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserAccountService userAccountService;

	@Test
	void contextLoads() {
	}

	@Test
	@Transactional
	void findPageBySkillPaginatesFilteredPeople() {
		String skillName = "page-filter-test-skill";
		List<Person> people = java.util.stream.IntStream.range(0, 6)
				.mapToObj(index -> new Person("Page", "Person" + index, "Test", List.of(new Skill(skillName))))
				.toList();
		personRepository.saveAll(people);

		Page<Person> firstPage = personRepository.findPageBySkill(skillName.toUpperCase(),
				PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "id")));
		Page<Person> secondPage = personRepository.findPageBySkill(skillName,
				PageRequest.of(1, 5, Sort.by(Sort.Direction.ASC, "id")));

		assertEquals(6, firstPage.getTotalElements());
		assertEquals(2, firstPage.getTotalPages());
		assertEquals(5, firstPage.getNumberOfElements());
		assertEquals(1, secondPage.getNumberOfElements());
	}

	@Test
	void booksPageRenders() throws Exception {
		mockMvc.perform(get("/books").param("page", "0"))
				.andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString("ISBN")));
	}

	@Test
	void peoplePageRenders() throws Exception {
		mockMvc.perform(get("/people").param("page", "0"))
				.andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString("Directorio")));
	}

	@Test
	void unhandledWebExceptionUses500ErrorPage() {
		var errorPage = new GlobalViewsExceptions().handleException(new IllegalStateException("internal detail"));

		assertEquals("error/500", errorPage.getViewName());
		assertEquals(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, errorPage.getStatus());
	}

	@Test
	void invalidRegistrationShowsFieldErrorsInsteadOf500() throws Exception {
		mockMvc.perform(post("/register").param("email", "no-es-correo").param("password", "short"))
				.andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString("Introduce un correo válido")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("La contraseña debe tener entre 8 y 72 caracteres")));
	}

	@Test
	void emptyRegistrationFieldsShowRequiredErrors() throws Exception {
		mockMvc.perform(post("/register").param("email", "").param("password", ""))
				.andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString("El correo es obligatorio")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("La contraseña es obligatoria")));
	}

	@Test
	@Transactional
	void registrationAssignsDefaultUserRole() {
		String email = "registration-" + java.util.UUID.randomUUID() + "@example.test";
		var account = userAccountService.register(email, "TestPassword123!");

		assertEquals(List.of("USER"), account.roles());
	}

}