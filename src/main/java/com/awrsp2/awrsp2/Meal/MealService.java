package com.awrsp2.awrsp2.Meal;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MealService {

    private final MealRepository mealRepository;

    public MealService(MealRepository mealRepository) {
        this.mealRepository = mealRepository;
    }

    public List<Meal> getAllMeals() {
        return mealRepository.findAll();
    }

    public Meal getMealById(int id) {
        return mealRepository.findById(id).orElse(null);
    }

    public Meal createMeal(Meal meal) {
        return mealRepository.save(meal);
    }
    public void deleteMeal(int id) {
        mealRepository.deleteById(id);
    }
}