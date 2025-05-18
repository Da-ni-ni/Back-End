package da_ni_ni.backend.intimacy.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "IntimacyTestResponses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder          // ← 이 한 줄을 추가
public class IntimacyTestResponse {
    /** PK이자 FK */
    @Id
    private Integer intimacyId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "intimacy_id")
    private IntimacyScore intimacyScore;

    private Byte answer1;
    private Byte answer2;
    private Byte answer3;
    private Byte answer4;
    private Byte answer5;
    private Byte answer6;
    private Byte answer7;
    private Byte answer8;
    private Byte answer9;
    private Byte answer10;

    private LocalDateTime createdAt;
}
