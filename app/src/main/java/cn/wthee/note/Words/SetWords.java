package cn.wthee.note.Words;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SetWords {

    public void setMyWords(final GetText getText) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(2,TimeUnit.SECONDS)
                .readTimeout(2,TimeUnit.SECONDS)
                .callTimeout(2,TimeUnit.SECONDS)
                .build();
        Request request = new Request.Builder()
                .url("http://open.iciba.com/dsapi/")
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getText.onFailure();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final Word word = new Gson().fromJson(response.body().string(), Word.class);
                getText.onSuccess(word);
            }
        });
    }

    public interface GetText{
        void onSuccess(Word word);
        void onFailure();
    }
}
