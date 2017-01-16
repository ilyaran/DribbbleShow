package mysite.com.dribbbleshow.model.api;

import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class ApiClient {

    public final static String API_KEY = "Bearer bc3b4c9cc1dedb6598585d845b417541464ea447f4bd66a486dc1f32566c9c0e";
    public final static String BASE_URL = "https://api.dribbble.com/v1/";
    static Retrofit retrofitDribbble;


    public static Retrofit getClientDribbble() {

        if (retrofitDribbble == null) {
            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request newRequest = chain.request().newBuilder()
                            .addHeader("Authorization", API_KEY)
                            .build();
                    return chain.proceed(newRequest);
                }
            }).build();

            retrofitDribbble = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitDribbble;
    }

}