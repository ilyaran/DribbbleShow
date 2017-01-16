package mysite.com.dribbbleshow.model.api;

import java.util.List;

import mysite.com.dribbbleshow.model.dto.ShotDTO;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiInterface {

    @GET("shots")
    Call<List<ShotDTO>> getShots(
            @Query("per_page") int per_page,
            @Query("page") int page);

}