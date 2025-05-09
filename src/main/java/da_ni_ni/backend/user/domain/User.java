package da_ni_ni.backend.user.domain;

import da_ni_ni.backend.global.domain.BaseTime;
import da_ni_ni.backend.group.domain.Group;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
//import da_ni_ni.backend.group.domain.Groups;
import java.time.LocalDateTime;

@Entity
@Table(name = "users") // DB 테이블명은 소문자 복수형 추천
@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class User extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;  // 회원 고유 식별자

    private String name;

    @Column(unique = true, nullable = false)
    private String email;  // 로그인용 이메일

    @Column(nullable = false)
    private String passwordHash;  // 암호화된 비밀번호

    private String nickName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    @NotEmpty
    private String role;

}