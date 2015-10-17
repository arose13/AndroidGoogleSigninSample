package se.stephenro.customcloudtest.api;

import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.POST;

/**
 * Created by Anthony on 10/12/2015.
 *
 * This contains EVERYTHING required by Retrofit for backend communication.
 */
public class GSPService {

    /**
     * The top level domain for the backend
     */
    public static final String BASE_URL = "https://gsproject-api.herokuapp.com";

    /**
     * This is specific to the backend and THIS IS NOT THE ANDROID CLIENT CODE
     */
    public static final String SERVER_CLIENT_ID = "158330931359-n3q9bnt9vfi0i7ostmlk3rn5n99153jk.apps.googleusercontent.com";

    /**
     * The BackendApi is used to make request calls to the server
     * - The object inside the Call<> is what the data will be placed in
     */
    public interface BackendApi {

        @POST("/")
        Call<TestData> testResp();

        @POST("/")
        Call<TestData> testResp(@Body TokenPayload tokenPayload);

    }

    /**
     * For the TestData object to work properly with Jackson you need...
     * - The class to be static
     * - A protected dummy class
     * - Getters and Setters for all the fields
     */
    public static class TestData {

        private String title;
        private String content;

        protected TestData() {}

        public TestData(String title, String content) {
            this.title = title;
            this.content = content;
        }

        public String getTitle() {
            return title;
        }

        public String getContent() {
            return content;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    /**
     * This is the TokenPayload to be sent when you've got the Token!!!
     */
    public static class TokenPayload {
        private final String email;
        private final String token;

        public TokenPayload(String email, String token) {
            this.email = email;
            this.token = token;
        }

        public String getEmail() {
            return email;
        }

        public String getToken() {
            return token;
        }
    }
}
