package com.alphawallet.app.ui;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alphawallet.app.R;
import com.alphawallet.app.repository.EthereumNetworkRepository;
import com.alphawallet.app.ui.widget.adapter.NodeRPCsAdapter;
import com.alphawallet.app.viewmodel.NodeRPCsViewModel;
import com.alphawallet.app.widget.StandardHeader;
import com.alphawallet.ethereum.NetworkInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class NodeRPCsActivity extends BaseActivity
{
    RecyclerView mainnetRecyclerView;
    RecyclerView testnetRecyclerView;
    StandardHeader mainnetHeader;
    StandardHeader testnetHeader;

    NodeRPCsViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_node_rpcs);
        toolbar();

        setTitle(getString(R.string.action_node_rpcs));

        viewModel = new ViewModelProvider(this)
                .get(NodeRPCsViewModel.class);

        initViews();
        setupList(Arrays.asList(viewModel.getNetworkList()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuItem item = menu.add(0, R.string.action_reset_rpc_fetch, 0, R.string.action_reset_rpc_fetch);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.string.action_reset_rpc_fetch)
        {
            viewModel.forceRefreshRPCs();
            Toast.makeText(this, R.string.rpc_fetch_reset_message, Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initViews()
    {
        mainnetHeader = findViewById(R.id.mainnet_header);
        testnetHeader = findViewById(R.id.testnet_header);

        mainnetRecyclerView = findViewById(R.id.main_list);
        testnetRecyclerView = findViewById(R.id.test_list);

        mainnetRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        testnetRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupList(List<NetworkInfo> networkInfoList)
    {
        ArrayList<NetworkInfo> mainNetList = new ArrayList<>();
        ArrayList<NetworkInfo> testNetList = new ArrayList<>();

        for (NetworkInfo info : networkInfoList)
        {
            if (EthereumNetworkRepository.hasRealValue(info.chainId))
            {
                mainNetList.add(info);
            }
            else
            {
                testNetList.add(info);
            }
        }

        NodeRPCsAdapter mainnetAdapter = new NodeRPCsAdapter(mainNetList);
        NodeRPCsAdapter testnetAdapter = new NodeRPCsAdapter(testNetList);

        mainnetRecyclerView.setAdapter(mainnetAdapter);
        testnetRecyclerView.setAdapter(testnetAdapter);
    }
}
