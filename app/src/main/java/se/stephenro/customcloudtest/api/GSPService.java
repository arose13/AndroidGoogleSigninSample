package se.stephenro.customcloudtest.api;

import retrofit.Call;
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
     * The BackendApi is used to make request calls to the server
     * - The object inside the Call<> is what the data will be placed in
     */
    public interface BackendApi {

        @POST("/")
        Call<TestData> testResp();

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
}
