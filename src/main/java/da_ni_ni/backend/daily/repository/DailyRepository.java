package da_ni_ni.backend.daily.repository;

import da_ni_ni.backend.daily.domain.Daily;
import da_ni_ni.backend.group.domain.FamilyGroup;
import da_ni_ni.backend.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DailyRepository extends JpaRepository<Daily, Long> {
    List<Daily> findDailyByUser(User user);
    List<Daily> findAllByFamilyGroupIdAndDateBetween(Long familyGroupId, LocalDate startDate, LocalDate endDate);
}


