package com.alphawallet.app.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

public class RpcConfigRepository
{
    private static final String LAST_FETCH_TIME_KEY = "rpc_rankings_last_fetch_time";
    private static final long ONE_DAY_MS = TimeUnit.DAYS.toMillis(1);
    private static final String RPC_RANKINGS_FILE_NAME = "rpc-rankings.json";

    private final Context context;
    private final OkHttpClient httpClient;
    private final SharedPreferences sharedPreferences;

    public RpcConfigRepository(Context context, OkHttpClient httpClient)
    {
        this.context = context.getApplicationContext();
        this.httpClient = httpClient;
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.context);
    }

    public synchronized void initialiseFromDisk()
    {
        EthereumNetworkBase.populateRPCList();
        String activeJson = loadStoredRpcConfigJson();
        if (!TextUtils.isEmpty(activeJson))
        {
            EthereumNetworkBase.applyRpcConfigJson(activeJson);
        }
    }

    public void checkAndUpdateRPCs()
    {
        new Thread(() ->
        {
            try
            {
                if (needsUpdate())
                {
                    fetchStoreAndReinitialise();
                }
            }
            catch (Exception e)
            {
                Timber.e(e, "Failed to refresh RPC config");
            }
        }, "RpcConfigUpdate").start();
    }

    public void forceRefreshRPCs()
    {
        new Thread(() ->
        {
            try
            {
                fetchStoreAndReinitialise();
            }
            catch (Exception e)
            {
                Timber.e(e, "Failed to force refresh RPC config");
            }
        }, "RpcConfigForceRefresh").start();
    }

    private boolean needsUpdate()
    {
        long lastFetchTimeMs = sharedPreferences.getLong(LAST_FETCH_TIME_KEY, 0L);
        if (lastFetchTimeMs <= 0)
        {
            return true;
        }

        long elapsed = System.currentTimeMillis() - lastFetchTimeMs;
        return elapsed >= ONE_DAY_MS;
    }

    private void fetchStoreAndReinitialise() throws Exception
    {
        String remoteUrl = EthereumNetworkBase.getRpcRankingsRemoteUrl();
        Request request = new Request.Builder().url(remoteUrl).build();

        try (Response response = httpClient.newCall(request).execute())
        {
            if (!response.isSuccessful() || response.body() == null)
            {
                Timber.w("RPC config fetch failed: %s", response.code());
                return;
            }

            String json = response.body().string();
            if (TextUtils.isEmpty(json) || !isValidPayload(json))
            {
                Timber.w("RPC config fetch returned invalid payload");
                return;
            }

            try (FileOutputStream outputStream = new FileOutputStream(getRpcRankingsFile(), false))
            {
                outputStream.write(json.getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
            }

            sharedPreferences.edit().putLong(LAST_FETCH_TIME_KEY, System.currentTimeMillis()).apply();
            EthereumNetworkBase.applyRpcConfigJson(json);
        }
    }

    private boolean isValidPayload(String json)
    {
        try
        {
            Type payloadType = new TypeToken<CuratedRpcPayload>() {}.getType();
            CuratedRpcPayload payload = new Gson().fromJson(json, payloadType);
            return payload != null && payload.chains != null;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    private String loadStoredRpcConfigJson()
    {
        try
        {
            File file = getRpcRankingsFile();
            if (!file.exists())
            {
                return null;
            }

            StringBuilder content = new StringBuilder();
            try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))
            {
                char[] buffer = new char[4096];
                int readCount;
                while ((readCount = reader.read(buffer)) != -1)
                {
                    content.append(buffer, 0, readCount);
                }
            }
            return content.toString();
        }
        catch (Exception e)
        {
            return null;
        }
    }

    private File getRpcRankingsFile()
    {
        return new File(context.getFilesDir(), RPC_RANKINGS_FILE_NAME);
    }

    private static class CuratedRpcPayload
    {
        List<CuratedChainEntry> chains;
    }

    private static class CuratedChainEntry
    {
        long chainId;
        List<CuratedRpcEntry> rpcs;
    }

    private static class CuratedRpcEntry
    {
        String url;
    }
}
