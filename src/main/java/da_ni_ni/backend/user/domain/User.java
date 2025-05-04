package da_ni_ni.backend.user.domain;

import jakarta.persistence.*;
import lombok.*;
//import da_ni_ni.backend.group.domain.Groups;
import java.time.LocalDateTime;

@Entity
@Table(name = "users") // DB 테이블명은 소문자 복수형 추천
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;  // 회원 고유 식별자

    private String name;

    @Column(unique = true, nullable = false)
    private String email;  // 로그인용 이메일

    @Column(nullable = false)
    private String passwordHash;  // 암호화된 비밀번호

    private String nickname;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "group_id")
//    private Groups group;  // 가입된 가족 그룹 (없을 수도 있음)
    @Column(name = "group_id")
    private Long groupId;    // 그룹생성전

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
