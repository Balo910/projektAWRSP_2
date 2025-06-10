package com.pp_projekt.pp_projekt.DailyMeal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface DailyMealLogRepository extends JpaRepository<DailyMealLog, Integer> {
    DailyMealLog findByUserIdAndDate(int userId, LocalDate date);
    List<DailyMealLog> findByUserIdOrderByDateDesc(int userId);
}
