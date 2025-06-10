package com.pp_projekt.pp_projekt.UserProfile;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class UserProfileControllerTest {

    @Mock
    private UserProfileRepository repository;

    @Mock
    private UserProfileService service;

    @InjectMocks
    private UserProfileController controller;

    public UserProfileControllerTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateProfile_ValidInput() {
        UserProfile profile = new UserProfile();
        profile.setHeight(180);
        profile.setWeight(75);
        profile.setGender("male");
        profile.setActivityLevel(3);
        profile.setGoal("maintain");

        when(service.calculateCalories(75, 180, "male", 3, "maintain")).thenReturn(2500.0);
        when(repository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserProfile result = controller.createProfile(profile);

        assertEquals(2500.0, result.getDailyCalories());
        verify(repository).save(profile);
    }

    @Test
    void testCreateProfile_InvalidHeight() {
        UserProfile profile = new UserProfile();
        profile.setHeight(90);
        profile.setWeight(75);

        assertThrows(IllegalArgumentException.class, () -> controller.createProfile(profile));
    }
}
