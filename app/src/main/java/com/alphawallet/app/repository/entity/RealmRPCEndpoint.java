package com.alphawallet.app.repository.entity;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Stores RPC endpoint data fetched from chainlist.org with ranking information.
 *
 * Ranking values:
 *   -1 = untested (freshly fetched or reset after weekly update)
 *    0 = non-responsive (failed during calibration)
 *   >0 = average response time in milliseconds (lower is better)
 *
 * A singleton record with key "__rpc_meta__" stores the lastRPCCheck timestamp.
 */
public class RealmRPCEndpoint extends RealmObject
{
    public static final String META_KEY = "__rpc_meta__";

    @PrimaryKey
    private String key; // composite: "chainId-url" or META_KEY for singleton metadata

    private long chainId;
    private String url;
    private long rank; // -1 = untested, 0 = dead, >0 = avg response ms
    private long lastRPCCheck; // only used in the META_KEY record

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public long getChainId()
    {
        return chainId;
    }

    public void setChainId(long chainId)
    {
        this.chainId = chainId;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public long getRank()
    {
        return rank;
    }

    public void setRank(long rank)
    {
        this.rank = rank;
    }

    public long getLastRPCCheck()
    {
        return lastRPCCheck;
    }

    public void setLastRPCCheck(long lastRPCCheck)
    {
        this.lastRPCCheck = lastRPCCheck;
    }

    public static String makeKey(long chainId, String url)
    {
        return chainId + "-" + url;
    }
}
