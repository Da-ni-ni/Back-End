package da_ni_ni.backend.emotion.domain;

import da_ni_ni.backend.emotion.dto.UpdateEmotionData;
import da_ni_ni.backend.group.domain.FamilyGroup;
import da_ni_ni.backend.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)

public class Emotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "emotion_id")
    private Long id;

    private LocalDate date;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private FamilyGroup group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private EmotionType type;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    public static Emotion create(User user, EmotionType type, LocalDateTime createdAt) {
        return Emotion.builder()
                .user(user)
                .type(type)
                .createdAt(createdAt)
                .build();
    }

    public void updateType (UpdateEmotionData data) {
        this.type = data.getEmotion();
    }

}
