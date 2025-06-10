package com.pp_projekt.pp_projekt.UserProfile;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserProfileServiceTest {

    private final UserProfileService service = new UserProfileService();

    @Test
    public void testCalculateBMI() {
        double bmi = service.calculateBMI(70, 175);
        assertEquals(22.86, bmi, 0.1);
    }

    @Test
    public void testCalculateCalories_Male_LoseWeight() {
        double calories = service.calculateCalories(80, 180, "male", 3, "lose");
        assertTrue(calories > 1800 && calories < 2800);
    }

    @Test
    public void testCalculateCalories_Female_GainWeight() {
        double calories = service.calculateCalories(60, 165, "female", 2, "gain");
        assertTrue(calories > 1700 && calories < 2500);
    }
}