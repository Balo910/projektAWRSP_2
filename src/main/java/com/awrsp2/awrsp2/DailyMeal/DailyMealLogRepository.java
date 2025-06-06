package com.awrsp2.awrsp2.DailyMeal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyMealLogRepository extends JpaRepository<DailyMealLog, Integer> {
    DailyMealLog findByUserId(int userId);
}
