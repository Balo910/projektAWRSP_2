package com.awrsp2.awrsp2;

import com.awrsp2.awrsp2.Meal.Meal;
import com.awrsp2.awrsp2.Meal.MealRepository;
import com.awrsp2.awrsp2.UserProfile.UserProfile;
import com.awrsp2.awrsp2.UserProfile.UserProfileRepository;
import com.awrsp2.awrsp2.UserProfile.UserProfileService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Component
public class ConsoleAppRunner implements CommandLineRunner {

    private final UserProfileRepository userRepo;
    private final MealRepository mealRepo;
    private final UserProfileService userService;

    public ConsoleAppRunner(UserProfileRepository userRepo, MealRepository mealRepo, UserProfileService userService) {
        this.userRepo = userRepo;
        this.mealRepo = mealRepo;
        this.userService = userService;
    }

    @Override
    public void run(String... args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("\n\n====================");
        System.out.println("Witaj w aplikacji liczącej kalorie!");
        System.out.println("====================\n");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        int height;
        while (true) {
            System.out.print("Podaj swój wzrost (cm): ");
            if (scanner.hasNextInt()) {
                height = scanner.nextInt();
                if (height >= 100 && height <= 250) break;
            }
            System.out.println("Nieprawidłowy wzrost. Spróbuj ponownie.");
            scanner.nextLine();
        }

        double weight;
        while (true) {
            System.out.print("Podaj swoją wagę (kg): ");
            if (scanner.hasNextDouble()) {
                weight = scanner.nextDouble();
                if (weight >= 30 && weight <= 300) break;
            }
            System.out.println("Nieprawidłowa waga. Spróbuj ponownie.");
            scanner.nextLine();
        }

        scanner.nextLine();
        String gender = "";
        while (true) {
            System.out.print("Podaj płeć (M - mężczyzna / K - kobieta): ");
            String genderInput = scanner.nextLine().toUpperCase();
            if (genderInput.equals("M")) {
                gender = "male";
                break;
            } else if (genderInput.equals("K")) {
                gender = "female";
                break;
            } else {
                System.out.println("Nieprawidłowa wartość. Spróbuj ponownie.");
            }
        }

        int activity;
        while (true) {
            System.out.println("Podaj poziom aktywności (1-5):");
            System.out.println("1 - brak aktywności");
            System.out.println("2 - lekka aktywność");
            System.out.println("3 - umiarkowana aktywność");
            System.out.println("4 - duża aktywność");
            System.out.println("5 - bardzo duża aktywność");

            if (scanner.hasNextInt()) {
                activity = scanner.nextInt();
                if (activity >= 1 && activity <= 5) break;
            }
            System.out.println("Nieprawidłowy poziom aktywności. Spróbuj ponownie.");
            scanner.nextLine();
        }

        scanner.nextLine();
        String goal = "";
        while (true) {
            System.out.print("Podaj cel (S - schudnąć / U - utrzymać wagę / P - przytyć): ");
            String goalInput = scanner.nextLine().toUpperCase();
            switch (goalInput) {
                case "S":
                    goal = "lose";
                    break;
                case "U":
                    goal = "maintain";
                    break;
                case "P":
                    goal = "gain";
                    break;
                default:
                    System.out.println("Nieprawidłowy cel. Spróbuj ponownie.");
                    continue;
            }
            break;
        }

        double bmi = userService.calculateBMI(weight, height);
        double calories = userService.calculateCalories(weight, height, gender, activity, goal);

        UserProfile user = new UserProfile();
        user.setWeight(weight);
        user.setHeight(height);
        user.setGender(gender);
        user.setActivityLevel(activity);
        user.setGoal(goal);
        user.setDailyCalories(calories);
        user = userRepo.save(user);

        System.out.printf("Twoje BMI wynosi: %.2f%n", bmi);
        System.out.println("Interpretacja BMI: " + interpretBMI(bmi));
        System.out.printf("Twoje zapotrzebowanie kaloryczne to: %.0f kcal%n", calories);

        double total = 0;
        List<Meal> eatenMeals = new ArrayList<>();

        while (true) {
            System.out.println("\nLista posiłków:");
            List<Meal> meals = mealRepo.findAll();
            for (Meal m : meals) {
                System.out.printf("%d. %s (%.0f kcal)%n", m.getId(), m.getName(), m.getCalories());
            }

            System.out.print("Wybierz ID posiłku do dodania, -3 aby dodać własny, -2 aby usunąć posiłek, -1 aby zakończyć: ");
            if (!scanner.hasNextInt()) {
                System.out.println("Nieprawidłowe ID.");
                scanner.nextLine();
                continue;
            }

            int id = scanner.nextInt();
            scanner.nextLine();

            if (id == -1) break;

            if (id == -2) {
                if (eatenMeals.isEmpty()) {
                    System.out.println("Nie masz żadnych dodanych posiłków do usunięcia.");
                    continue;
                }
                System.out.println("Zjedzone posiłki:");
                for (int i = 0; i < eatenMeals.size(); i++) {
                    Meal m = eatenMeals.get(i);
                    System.out.printf("%d. %s (%.0f kcal)%n", i + 1, m.getName(), m.getCalories());
                }
                System.out.print("Podaj numer posiłku do usunięcia: ");
                if (scanner.hasNextInt()) {
                    int removeIndex = scanner.nextInt() - 1;
                    scanner.nextLine();
                    if (removeIndex >= 0 && removeIndex < eatenMeals.size()) {
                        Meal removed = eatenMeals.remove(removeIndex);
                        total -= removed.getCalories();
                        System.out.printf("Usunięto: %s (%.0f kcal). Nowy bilans: %.0f / %.0f kcal%n",
                                removed.getName(), removed.getCalories(), total, calories);
                        printGoalStatus(total, calories);
                    } else {
                        System.out.println("Nieprawidłowy numer.");
                    }
                }
                continue;
            }

            if (id == -3) {
                System.out.print("Podaj nazwę nowego posiłku: ");
                String newName = scanner.nextLine();

                double newCalories;
                while (true) {
                    System.out.print("Podaj kaloryczność nowego posiłku: ");
                    if (scanner.hasNextDouble()) {
                        newCalories = scanner.nextDouble();
                        scanner.nextLine();
                        if (newCalories > 0) break;
                    } else {
                        scanner.nextLine();
                    }
                    System.out.println("Nieprawidłowa wartość. Spróbuj ponownie.");
                }

                Meal newMeal = new Meal();
                newMeal.setName(newName);
                newMeal.setCalories(newCalories);
                mealRepo.save(newMeal);
                System.out.printf("✅ Dodano nowy posiłek: %s (%.0f kcal)%n", newName, newCalories);
                continue;
            }

            Meal meal = mealRepo.findById(id).orElse(null);
            if (meal != null) {
                eatenMeals.add(meal);
                total += meal.getCalories();
                System.out.printf("Dodano: %s (%.0f kcal). Łącznie zjedzono: %.0f / %.0f kcal%n",
                        meal.getName(), meal.getCalories(), total, calories);
                printGoalStatus(total, calories);
            } else {
                System.out.println("Nie znaleziono posiłku.");
            }
        }

        System.out.println("\nPodsumowanie - zjedzone posiłki:");
        if (eatenMeals.isEmpty()) {
            System.out.println("Nie zjedzono żadnych posiłków.");
        } else {
            for (Meal m : eatenMeals) {
                System.out.printf("- %s (%.0f kcal)%n", m.getName(), m.getCalories());
            }
            System.out.printf("Łącznie: %.0f / %.0f kcal%n", total, calories);
            printGoalStatus(total, calories);
        }

        try {
            String logResult = exportLogToFile(user.getId(), eatenMeals, total, calories);
            System.out.println(logResult);
        } catch (IOException e) {
            System.out.println("❌ Nie udało się zapisać dziennika posiłków: " + e.getMessage());
        }
    }

    private void printGoalStatus(double total, double calories) {
        if (total >= calories * 1.2) {
            System.out.printf("❗ Przekroczono cel kaloryczny o %.0f kcal!%n", total - calories);
        } else if (total >= calories) {
            System.out.println("✅ Udało się osiągnąć cel kaloryczny!");
        } else {
            System.out.printf("❌ Brakuje %.0f kcal.%n", calories - total);
        }
    }

    private String interpretBMI(double bmi) {
        if (bmi < 16.0) return "wygłodzenie";
        if (bmi < 17.0) return "wychudzenie";
        if (bmi < 18.5) return "niedowaga";
        if (bmi < 25.0) return "waga prawidłowa";
        if (bmi < 30.0) return "nadwaga";
        if (bmi < 35.0) return "otyłość I stopnia";
        if (bmi < 40.0) return "otyłość II stopnia";
        return "otyłość III stopnia";
    }

    private String exportLogToFile(int userId, List<Meal> meals, double totalCalories, double goal) throws IOException {
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
