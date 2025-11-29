<think>
Okay, I need to create a JUnit test class for the getAllTutorials endpoint in the TutorialController. Let's see what the endpoint does based on the documentation.

The endpoint is a GET mapping at "/tutorials" and it takes an optional "title" parameter. It returns a list of Tutorials either matching the title or all if no title is provided. The response could be OK, NO_CONTENT, or INTERNAL_SERVER_ERROR depending on the situation.

Looking at the previous test files, they use JUnit 5 with assertions from AssertJ and mockito for mocking dependencies. So I'll follow that pattern.

First, I should set up a test class annotated with @Test. I'll need to mock TutorialService because it's a dependency of TutorialController. Using MockitoAnnotations.openMocks(this) will handle the setup automatically.

I'll create a list of Tutorials as test data. Maybe include some published and non-published tutorials to cover different scenarios. Let's add 3 tutorials with different titles and statuses.

For positive cases:
1. Test when there are tutorials available without a title parameter.
2. Test when searching with an existing title returns the correct tutorial(s).
3. Test finding only published tutorials.

Negative cases:
1. When no tutorials exist, should return NO_CONTENT.
2. Searching for a non-existent title should also return NO_CONTENT.
3. Testing if exceptions are handled correctly, like if service throws an exception, the controller should return INTERNAL_SERVER_ERROR.

Boundary cases might include edge conditions like maximum number of tutorials or handling very long titles, but perhaps that's beyond basic testing.

I'll structure each test method with meaningful names and use appropriate assertions. For example, using assertAll() to check multiple conditions in one test for efficiency.

Make sure to cover all possible scenarios the endpoint can handle, including error cases where the service layer might throw exceptions. I'll mock the service responses to simulate these scenarios.

I think that's a good plan. Now, let's write the code.
</think>

[java]
package com.bezkoder.spring.restapi.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import com.bezkoder.spring.restapi.model.Tutorial;
import com.bezkoder.spring.restapi.service.TutorialService;

import static org.mockito.Mockito.*;

import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.OK;

import static org.springframework.http.HttpStatus.NO_CONTENT;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import java.util.Arrays;

import static java.util.Collections.emptyList;

public class TutorialControllerTests {

    @Mock
    private TutorialService tutorialService;

    @InjectMocks
    private TutorialController tutorialController;

    private List<Tutorial> tutorials;
    private Tutorial tutorial1;
    private Tutorial tutorial2;
    privateTutorial tutorial3;

    @BeforeEach
    void setUp() {
        tutorial1 = new Tutorial("Java", "Learn Java programming", true);
        tutorial2 = new Tutorial("Python", "Learn Python programming", false);
        tutorial3 = new Tutorial("JavaScript", "Learn JavaScript programming", true);

        tutorials = Arrays.asList(tutorial1, tutorial2, tutorial3);
    }

    @Test
    void getAllTutorials_WhenNoTitleProvided_ShouldReturnAllTutorials() {
        when(tutorialService.findAll()).thenReturn(tutorials);

        ResponseEntity<List<Tutorial>> response = tutorialController.getAllTutorials(null);

        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).containsExactly(tutorial1, tutorial2, tutorial3);
    }

    @Test
    void getAllTutorials_WithTitle_ShouldReturnMatchingTutorials() {
        String titleToSearch = "Java";
        when(tutorialService.findByTitleContaining(titleToSearch)).thenReturn(List.of(tutorial1));

        ResponseEntity<List<Tutorial>> response = tutorialController.getAllTutorials(titleToSearch);

        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).containsOnly(tutorial1);
    }

    @Test
    void getAllTutorials_WhenNoTutorialsExist_ShouldReturnNoContent() {
        when(tutorialService.findAll()).thenReturn(emptyList());

        ResponseEntity<List<Tutorial>> response = tutorialController.getAllTutorials(null);

        assertThat(response.getStatusCode()).isEqualTo(NO_CONTENT);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void getAllTutorials_WhenInvalidTitleProvided_ShouldReturnNoContent() {
        String titleToSearch = "Ruby";
        when(tutorialService.findByTitleContaining(titleToSearch)).thenReturn(emptyList());

        ResponseEntity<List<Tutorial>> response = tutorialController.getAllTutorials(titleToSearch);

        assertThat(response.getStatusCode()).isEqualTo(NO_CONTENT);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void getAllTutorials_WhenExceptionOccurs_ShouldReturnInternalServerError() {
        when(tutorialService.findAll()).thenThrow(new RuntimeException("Database error"));

        ResponseEntity<List<Tutorial>> response = tutorialController.getAllTutorials(null);

        assertThat(response.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void getAllTutorials_WithEmptyListResponse_ShouldReturnNoContent() {
        when(tutorialService.findAll()).thenReturn(emptyList());

        ResponseEntity<List<Tutorial>> response = tutorialController.getAllTutorials(null);

        assertThat(response.getStatusCode()).isEqualTo(NO_CONTENT);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void getAllTutorials_WithPublishedFilter_ShouldReturnOnlyPublishedTutorials() {
        when(tutorialService.findByPublished(true)).thenReturn(List.of(tutorial1, tutorial3));

        ResponseEntity<List<Tutorial>> response = tutorialController.getAllTutorials("published");

        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).containsExactly(tutorial1, tutorial3);
    }

    @Test
    void getAllTutorials_WithInvalidFilterParameter_ShouldReturnAllTutorials() {
        String invalidFilter = "invalid";
        when(tutorialService.findByPublished(invalidFilter.equalsIgnoreCase("published"))).thenReturn(tutorials);

        ResponseEntity<List<Tutorial>> response = tutorialController.getAllTutorials(invalidFilter);

        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).containsExactly(tutorial1, tutorial2, tutorial3);
    }

    @Test
    void getAllTutorials_WithEmptyTitleParameter_ShouldReturnAllTutorials() {
        when(tutorialService.findAll()).thenReturn(tutorials);

        ResponseEntity<List<Tutorial>> response = tutorialController.getAllTutorials("");

        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).containsExactly(tutorial1, tutorial2, tutorial3);
    }

}
[/java]
