package da_ni_ni.backend.emotion.exception;

public class EmotionNotFoundException extends RuntimeException{

    public EmotionNotFoundException() {
        super("Emotion을 찾을 수 없습니다.");
    }
}
