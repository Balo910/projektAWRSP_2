package com.pp_projekt.pp_projekt.UserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserProfileController {

    @Autowired
    private UserProfileRepository repository;

    @Autowired
    private UserProfileService service;

    @PostMapping("/create")
    public UserProfile createProfile(@RequestBody UserProfile profile) {
        if (profile.getHeight() < 100 || profile.getHeight() > 250) {
            throw new IllegalArgumentException("Wzrost poza zakresem 100–250 cm.");
        }
        if (profile.getWeight() < 30 || profile.getWeight() > 300) {
            throw new IllegalArgumentException("Waga poza zakresem 30–300 kg.");
        }

        double calories = service.calculateCalories(
                profile.getWeight(),
                profile.getHeight(),
                profile.getGender(),
                profile.getActivityLevel(),
                profile.getGoal()
        );
        profile.setDailyCalories(calories);
        return repository.save(profile);
    }
}
