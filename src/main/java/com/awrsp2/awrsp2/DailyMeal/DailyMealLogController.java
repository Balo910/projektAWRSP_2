package com.awrsp2.awrsp2.DailyMeal;
import com.awrsp2.awrsp2.Meal.Meal;
import com.awrsp2.awrsp2.Meal.MealRepository;
import com.awrsp2.awrsp2.UserProfile.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/log")
public class DailyMealLogController {

    @Autowired
    private DailyMealLogRepository logRepository;

    @Autowired
    private MealRepository mealRepository;

    @Autowired
    private UserProfileRepository userRepo;

    @PostMapping("/{userId}/add/{mealId}")
    public String addMeal(@PathVariable int userId, @PathVariable int mealId) {
        DailyMealLog log = logRepository.findByUserId(userId);
        if (log == null) {
            log = new DailyMealLog();
            log.setUserId(userId);
        }

        Meal meal = mealRepository.findById(mealId).orElseThrow();
        log.getMeals().add(meal);
        logRepository.save(log);

        double totalCalories = log.getMeals().stream().mapToDouble(Meal::getCalories).sum();
        double goal = userRepo.findById(userId).get().getDailyCalories();

        if (totalCalories >= goal) {
            return "Zjedzono " + totalCalories + " kcal. Udało się spełnić cel!";
        }

        return "Zjedzono " + totalCalories + " / " + goal + " kcal. Zostało: " + (goal - totalCalories) + " kcal.";
    }

    @PostMapping("/{userId}/remove/{mealId}")
    public String removeMeal(@PathVariable int userId, @PathVariable int mealId) {
        DailyMealLog log = logRepository.findByUserId(userId);
        if (log == null) return "Nie znaleziono loga posiłków.";

        log.getMeals().removeIf(meal -> meal.getId() == mealId);
        logRepository.save(log);

        double totalCalories = log.getMeals().stream().mapToDouble(Meal::getCalories).sum();
        double goal = userRepo.findById(userId).get().getDailyCalories();

        return "Po usunięciu: " + totalCalories + " / " + goal + " kcal. Zostało: " + (goal - totalCalories) + " kcal.";
    }

    @GetMapping("/{userId}/summary")
    public String getSummary(@PathVariable int userId) {
        DailyMealLog log = logRepository.findByUserId(userId);
        if (log == null) return "Brak zjedzonych posiłków.";

        double totalCalories = log.getMeals().stream().mapToDouble(Meal::getCalories).sum();
        double goal = userRepo.findById(userId).get().getDailyCalories();

        return "Zjedzono " + totalCalories + " / " + goal + " kcal. Zostało: " + (goal - totalCalories) + " kcal.";
    }
}
