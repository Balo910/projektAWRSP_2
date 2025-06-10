package com.pp_projekt.pp_projekt.DailyMeal;

import com.pp_projekt.pp_projekt.Meal.Meal;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class DailyMealLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int userId;

    private LocalDate date;

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    @ManyToMany
    private List<Meal> meals = new ArrayList<>();

    public DailyMealLog(int id, int userId, List<Meal> meals) {
        this.id = id;
        this.userId = userId;
        this.meals = meals;
    }

    public DailyMealLog() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public List<Meal> getMeals() {
        return meals;
    }

    public void setMeals(List<Meal> meals) {
        this.meals = meals;
    }
}
