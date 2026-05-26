package com.alphawallet.app.service;

import com.alphawallet.app.repository.EthereumNetworkBase;
import com.alphawallet.app.repository.RpcConfigRepository;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages RPC config refresh and runtime RPC selection.
 */
public class RPCRankingManager
{
    private final RpcConfigRepository rpcConfigRepository;

    // In-memory round-robin pointer per chain
    private final ConcurrentHashMap<Long, AtomicInteger> roundRobinPointers = new ConcurrentHashMap<>();

    public RPCRankingManager(RpcConfigRepository rpcConfigRepository)
    {
        this.rpcConfigRepository = rpcConfigRepository;
    }

    public void checkAndUpdateRPCs()
    {
        rpcConfigRepository.checkAndUpdateRPCs();
    }

    public void forceRefreshRPCs()
    {
        rpcConfigRepository.forceRefreshRPCs();
    }

    public String[] getRPCsForChain(long chainId)
    {
        return EthereumNetworkBase.getConfiguredRPCUrls(chainId);
    }

    public int getNextRPCIndex(long chainId, int rpcCount)
    {
        if (rpcCount <= 0) return 0;

        AtomicInteger pointer = roundRobinPointers.computeIfAbsent(chainId, k -> new AtomicInteger(0));
        return Math.abs(pointer.getAndIncrement() % rpcCount);
    }

    public void recordCallResult(long chainId, String url, long responseMs)
    {
        // No-op for file-only RPC config model.
    }

    public boolean isCalibrating(long chainId)
    {
        return false;
    }

}
