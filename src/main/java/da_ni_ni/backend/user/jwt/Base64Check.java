package da_ni_ni.backend.user.jwt;

import java.util.Base64;


// jwt 시크릿 디코딩 확인용
public class Base64Check {
    public static void main(String[] args) {
        String secret = "PAP41RubR_a82mgTWzSSgd84RcR3Sg_QQSbsvmd4Zxo";

        try {
            // URL-safe Base64 디코딩
            byte[] decoded = Base64.getUrlDecoder().decode(secret);
            System.out.println("Decoded Bytes: ");
            for (byte b : decoded) {
                System.out.print(b + " ");
            }
            System.out.println("\n디코딩이 성공했습니다!");
        } catch (IllegalArgumentException e) {
            System.out.println("디코딩 실패: " + e.getMessage());
        }
    }
}


