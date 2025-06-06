package com.awrsp2.awrsp2.UserProfile;
import org.springframework.stereotype.Service;

@Service
public class UserProfileService {

    public double calculateBMI(double weight, int height) {
        double heightInMeters = height / 100.0;
        return weight / (heightInMeters * heightInMeters);
    }

    public double calculateCalories(double weight, int height, String gender, int activityLevel, String goal) {
        double bmr;
        if (gender.equalsIgnoreCase("male")) {
            bmr = 10 * weight + 6.25 * height - 5 * 25 + 5;
        } else {
            bmr = 10 * weight + 6.25 * height - 5 * 25 - 161;
        }

        double activityFactor = switch (activityLevel) {
            case 1 -> 1.2;
            case 2 -> 1.375;
            case 3 -> 1.55;
            case 4 -> 1.725;
            case 5 -> 1.9;
            default -> 1.2;
        };

        double calories = bmr * activityFactor;

        return switch (goal) {
            case "lose" -> calories - 500;
            case "gain" -> calories + 500;
            default -> calories;
        };
    }
}
