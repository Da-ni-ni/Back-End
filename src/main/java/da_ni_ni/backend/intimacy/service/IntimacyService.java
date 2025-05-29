package da_ni_ni.backend.intimacy.service;

import da_ni_ni.backend.group.domain.FamilyGroup;
import da_ni_ni.backend.group.repository.GroupRepository;
import da_ni_ni.backend.intimacy.domain.IntimacyScore;
import da_ni_ni.backend.intimacy.domain.IntimacyTestResponse;
import da_ni_ni.backend.intimacy.dto.AnswerDto;
import da_ni_ni.backend.intimacy.dto.FamilyScoreResponse;
import da_ni_ni.backend.intimacy.dto.PersonalScoreResponse;
import da_ni_ni.backend.intimacy.exception.GroupNotFoundException;
import da_ni_ni.backend.intimacy.exception.IntimacyRecordNotFoundException;
import da_ni_ni.backend.intimacy.exception.InvalidAnswerCountException;
import da_ni_ni.backend.intimacy.repository.IntimacyScoreRepository;
import da_ni_ni.backend.intimacy.repository.IntimacyTestResponseRepository;
import da_ni_ni.backend.user.domain.User;
import da_ni_ni.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IntimacyService {
    private final IntimacyScoreRepository scoreRepo;
    private final IntimacyTestResponseRepository respRepo;
    private final UserRepository userRepo;
    private final GroupRepository groupRepo;

    @Transactional
    public void submitAnswers(User me, List<AnswerDto> answers) {
        if (answers == null || answers.size() != 10) {
            throw new InvalidAnswerCountException();
        }
        int sum = answers.stream().mapToInt(AnswerDto::getAnswer).sum();
        int totalScore = sum * 2;

        // Score 저장
        IntimacyScore score = new IntimacyScore();
        score.setUser(me);
        score.setScore(totalScore);
        score.setTestDate(LocalDate.now());
        score.setCreatedAt(LocalDateTime.now());
        scoreRepo.save(score);

        // Response 저장
        IntimacyTestResponse resp = new IntimacyTestResponse();
        resp.setIntimacyScore(score);
        resp.setAnswer1(answers.get(0).getAnswer().byteValue());
        resp.setAnswer2(answers.get(1).getAnswer().byteValue());
        resp.setAnswer3(answers.get(2).getAnswer().byteValue());
        resp.setAnswer4(answers.get(3).getAnswer().byteValue());
        resp.setAnswer5(answers.get(4).getAnswer().byteValue());
        resp.setAnswer6(answers.get(5).getAnswer().byteValue());
        resp.setAnswer7(answers.get(6).getAnswer().byteValue());
        resp.setAnswer8(answers.get(7).getAnswer().byteValue());
        resp.setAnswer9(answers.get(8).getAnswer().byteValue());
        resp.setAnswer10(answers.get(9).getAnswer().byteValue());
        resp.setCreatedAt(LocalDateTime.now());
        respRepo.save(resp);
    }

    @Transactional(readOnly = true)
    public PersonalScoreResponse getPersonalScore(User me) {
        IntimacyScore latest = scoreRepo.findFirstByUserOrderByTestDateDescCreatedAtDesc(me)
                .orElseThrow(IntimacyRecordNotFoundException::new);
        return new PersonalScoreResponse("나", latest.getScore());
    }

    @Transactional(readOnly = true)
    public FamilyScoreResponse getFamilyScore(User me) {
        FamilyGroup group = me.getFamilyGroup();
        if (group == null) {
            throw new GroupNotFoundException();
        }

        List<User> members = userRepo.findAllByFamilyGroup(group);

        double avg = members.stream()
                .mapToInt(u -> scoreRepo.findFirstByUserOrderByTestDateDescCreatedAtDesc(u)
                        .map(IntimacyScore::getScore)
                        .orElse(0))
                .average()
                .orElse(0.0);

        return new FamilyScoreResponse(group.getName(), avg);
    }

}