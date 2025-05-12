package da_ni_ni.backend.user.service;

import da_ni_ni.backend.user.domain.User;
import da_ni_ni.backend.user.repository.UserRepository;
import da_ni_ni.backend.qna.exception.ForbiddenException;
import da_ni_ni.backend.qna.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepo;

    /** 현재 로그인된 User 엔티티를 찾아 반환 */
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // 인증 정보가 없거나 익명 사용자라면 BadRequestException
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new BadRequestException("로그인 사용자 정보를 찾을 수 없습니다.");
        }

        String email = auth.getName();
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("로그인 사용자 정보를 찾을 수 없습니다."));
    }

    /** 가입 승인된 그룹에 속한 사용자만 반환 */
    public User getApprovedUser() {
        User me = getCurrentUser();
        if (me.getFamilyGroup().getId() == null) {
            throw new ForbiddenException("가입 승인된 그룹이 없습니다.");
        }
        return me;
    }

    /** 가입 승인된 그룹의 모든 멤버 조회 */
    public List<User> getFamilyMembers() {
        User me = getApprovedUser();
        return userRepo.findAllByFamilyGroup(me.getFamilyGroup());
    }
}
