package se.stephenro.customcloudtest.api;

import retrofit.Call;
import retrofit.http.POST;

/**
 * Created by Anthony on 10/12/2015.
 */
public class GSPService {

    public static final String BASE_URL = "https://gsproject-api.herokuapp.com";

    public interface BackendApi {

        @POST("/")
        Call<TestData> testResp();

    }

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
