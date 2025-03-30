package ru.spacewar;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface SpaceAPI {
    @GET("/spacewar.php")
    Call<List<DataFromDB>> sendQuery(@Query("q") String s);

    @GET("/spacewar.php")
    Call<List<DataFromDB>> sendQuery(@Query("name") String n, @Query("score") int s, @Query("kills") int k);
}
