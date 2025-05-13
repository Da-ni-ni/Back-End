package da_ni_ni.backend.emotion.domain;

import da_ni_ni.backend.common.BaseTime;
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

public class Emotion extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "emotion_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private FamilyGroup group;

    @OneToOne(mappedBy = "emotion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private User user;

    @Enumerated(EnumType.STRING)
    private EmotionType emotionType;


    public static Emotion create(User user, EmotionType emotionType, LocalDateTime createdAt) {
        return Emotion.builder()
                .user(user)
                .emotionType(emotionType)
                .build();
    }

    public void updateType (UpdateEmotionData data) {

        this.emotionType = data.getEmotionType();
    }

}
