package da_ni_ni.backend.emotion.domain;

import lombok.Getter;

@Getter
public enum EmotionType {
    ANGRY("분노"),
    SAD("슬픔"),
    HAPPY("기쁨"),
    RELAXED("편안함"),
    TIRED("피곤함"),
    MISSING("보고픔");

    private final String emoji;

    EmotionType(String emoji) {
        this.emoji = emoji;
    }

    public String getEmoji() {
        return emoji;
    }
}
