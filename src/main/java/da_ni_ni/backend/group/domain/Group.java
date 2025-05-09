package da_ni_ni.backend.group.domain;

import da_ni_ni.backend.global.domain.BaseTime;
import da_ni_ni.backend.group.dto.UpdateGroupNameData;
import da_ni_ni.backend.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Entity
@Getter @Builder @Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Group extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Long id;

    private String name;
    private String inviteCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_user_id")
    private User adminUser;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    private List<User> users = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void updateName(UpdateGroupNameData data) {
        this.name = data.getNewName();
    }

    public void addUser(User user) {
        users.add(user);
        user.setGroup(this);
    }

    public static String generateRandomCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUYVWXYZ0123456789";
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }

    public static Group create(String name, User adminUser) {
        return Group.builder()
                .name(name)
                .adminUser(adminUser)
                .inviteCode(generateRandomCode())
                .build();
    }

}

