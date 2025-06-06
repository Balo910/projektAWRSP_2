package com.awrsp2.awrsp2.DailyMeal;
import com.awrsp2.awrsp2.Meal.Meal;
import com.awrsp2.awrsp2.Meal.MealRepository;
import com.awrsp2.awrsp2.UserProfile.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
        LocalDate today = LocalDate.now();
        DailyMealLog log = logRepository.findByUserIdAndDate(userId, today);
        if (log == null) {
            log = new DailyMealLog();
            log.setUserId(userId);
            log.setDate(today);
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
        LocalDate today = LocalDate.now();
        DailyMealLog log = logRepository.findByUserIdAndDate(userId, today);
        if (log == null) return "Nie znaleziono loga posiłków.";

        log.getMeals().removeIf(meal -> meal.getId() == mealId);
        logRepository.save(log);

        double totalCalories = log.getMeals().stream().mapToDouble(Meal::getCalories).sum();
        double goal = userRepo.findById(userId).get().getDailyCalories();

        return "Po usunięciu: " + totalCalories + " / " + goal + " kcal. Zostało: " + (goal - totalCalories) + " kcal.";
    }

    @GetMapping("/{userId}/summary")
    public String getSummary(@PathVariable int userId) {
        LocalDate today = LocalDate.now();
        DailyMealLog log = logRepository.findByUserIdAndDate(userId, today);
        if (log == null) return "Brak zjedzonych posiłków.";

        double totalCalories = log.getMeals().stream().mapToDouble(Meal::getCalories).sum();
        double goal = userRepo.findById(userId).get().getDailyCalories();

        return "Zjedzono " + totalCalories + " / " + goal + " kcal. Zostało: " + (goal - totalCalories) + " kcal.";
    }

    @GetMapping("/{userId}/history")
    public String getHistory(@PathVariable int userId) {
        List<DailyMealLog> logs = logRepository.findByUserIdOrderByDateDesc(userId);
        if (logs.isEmpty()) return "Brak historii logów.";

        StringBuilder sb = new StringBuilder("📅 Historia dzienników:\n");

        for (DailyMealLog log : logs) {
            double total = log.getMeals().stream().mapToDouble(Meal::getCalories).sum();
            sb.append(String.format("- %s: %.0f kcal (%d posiłków)\n", log.getDate(), total, log.getMeals().size()));

            for (Meal meal : log.getMeals()) {
                sb.append(String.format("   • %s (%.0f kcal)\n", meal.getName(), meal.getCalories()));
            }
        }

        return sb.toString();
    }

    @GetMapping("/{userId}/export")
    public String exportLogToFile(@PathVariable int userId) throws IOException {
        LocalDate today = LocalDate.now();
        DailyMealLog log = logRepository.findByUserIdAndDate(userId, today);
        if (log == null || log.getMeals().isEmpty()) {
            return "Brak danych do zapisania.";
        }

        List<Meal> meals = log.getMeals();
        double totalCalories = meals.stream().mapToDouble(Meal::getCalories).sum();
        double goal = userRepo.findById(userId).get().getDailyCalories();

        StringBuilder content = new StringBuilder();
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter fileFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm");
        DateTimeFormatter displayFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        content.append("Dziennik posiłków\n");
        content.append("Data wygenerowania: ").append(displayFormat.format(now)).append("\n\n");

        for (Meal m : meals) {
            content.append("- ").append(m.getName()).append(" (").append(m.getCalories()).append(" kcal)\n");
        }

        content.append(String.format("\nŁącznie: %.0f / %.0f kcal\n", totalCalories, goal));

        if (totalCalories < goal) {
            content.append(String.format("❌ Nie osiągnięto celu. Brakuje %.0f kcal.\n", goal - totalCalories));
        } else if (totalCalories <= goal * 1.2) {
            content.append("✅ Udało się osiągnąć cel kaloryczny.\n");
        } else {
            content.append(String.format("❗ Przekroczono cel o %.0f kcal!\n", totalCalories - goal));
        }

        String filename = "log_" + fileFormat.format(now) + ".txt";
        Path path = Paths.get("logs", filename);
        Files.createDirectories(path.getParent());
        Files.write(path, content.toString().getBytes());

        return "✅ Zapisano dziennik do pliku: " + path.toAbsolutePath();
    }



}
