package da_ni_ni.backend.intimacy.domain;

import da_ni_ni.backend.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "IntimacyScores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder          // ← 이 한 줄을 추가
public class IntimacyScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer intimacyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 0~100 */
    private Integer score;

    /** 테스트 완료 날짜 */
    private LocalDate testDate;

    private LocalDateTime createdAt;
}