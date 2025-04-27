package program.chatus.Chatmustdelete.Connection;

public class Tokens {
    private static String Token;
    private static String RefreshToken;

    public static String getToken() {
        return Token;
    }

    public static void setToken(String token) {
        Token = token;
    }

    public static String getRefreshToken() {
        return RefreshToken;
    }

    public static void setRefreshToken(String refreshToken) {
        RefreshToken = refreshToken;
    }
}
