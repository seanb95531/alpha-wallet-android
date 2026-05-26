package com.alphawallet.app.viewmodel;

import com.alphawallet.app.entity.NetworkInfo;
import com.alphawallet.app.repository.EthereumNetworkRepositoryType;
import com.alphawallet.app.service.RPCRankingManager;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class NodeRPCsViewModel extends BaseViewModel
{
    private final EthereumNetworkRepositoryType networkRepository;
    private final RPCRankingManager rpcRankingManager;

    @Inject
    public NodeRPCsViewModel(EthereumNetworkRepositoryType ethereumNetworkRepositoryType,
                             RPCRankingManager rpcRankingManager)
    {
        this.networkRepository = ethereumNetworkRepositoryType;
        this.rpcRankingManager = rpcRankingManager;
    }

    public NetworkInfo[] getNetworkList()
    {
        return networkRepository.getAvailableNetworkList();
    }

    public void forceRefreshRPCs()
    {
        rpcRankingManager.forceRefreshRPCs();
    }
}
