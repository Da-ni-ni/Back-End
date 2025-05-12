package da_ni_ni.backend.daily.domain;

import da_ni_ni.backend.daily.dto.UpdateDailyData;
import da_ni_ni.backend.group.domain.FamilyGroup;
import da_ni_ni.backend.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Builder @Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Daily {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "daily_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "family_group_id")
    private FamilyGroup group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDate date;
    private String content;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private long commentCount;
    private long likeCount;

    @Builder.Default
    @OneToMany(mappedBy = "daily", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    public void updateDaily(UpdateDailyData data) {
        this.content = data.getContent();
    }

}
