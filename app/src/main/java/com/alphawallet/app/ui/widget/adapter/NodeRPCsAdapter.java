package com.alphawallet.app.ui.widget.adapter;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alphawallet.app.R;
import com.alphawallet.app.repository.EthereumNetworkBase;
import com.alphawallet.app.widget.TokenIcon;
import com.alphawallet.ethereum.NetworkInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Adapter for displaying RPC endpoints grouped by chain.
 * Uses a flat list with two view types: chain header and RPC entry.
 */
public class NodeRPCsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    private static final int VIEW_TYPE_CHAIN_HEADER = 0;
    private static final int VIEW_TYPE_RPC_ENTRY = 1;

    private final List<Object> items = new ArrayList<>();
    private final Set<Long> expandedChains = new HashSet<>();

    public NodeRPCsAdapter(List<NetworkInfo> networks)
    {
        buildItems(networks);
    }

    private void buildItems(List<NetworkInfo> networks)
    {
        items.clear();

        for (NetworkInfo network : networks)
        {
            List<RPCData> rpcList = new ArrayList<>();
            String[] configured = EthereumNetworkBase.getConfiguredRPCUrls(network.chainId);
            if (configured != null)
            {
                for (String url : configured)
                {
                    if (url != null && !url.trim().isEmpty())
                    {
                        rpcList.add(new RPCData(url, -1));
                    }
                }
            }

            if (rpcList.isEmpty())
            {
                continue;
            }

            ChainHeader header = new ChainHeader(network, rpcList.size());
            header.rpcDataList.addAll(rpcList);
            items.add(header);
        }
    }

    @Override
    public int getItemViewType(int position)
    {
        return items.get(position) instanceof ChainHeader ? VIEW_TYPE_CHAIN_HEADER : VIEW_TYPE_RPC_ENTRY;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        if (viewType == VIEW_TYPE_CHAIN_HEADER)
        {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_rpc_chain_header, parent, false);
            return new ChainHeaderViewHolder(view);
        }
        else
        {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_rpc_entry, parent, false);
            return new RPCEntryViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position)
    {
        if (holder instanceof ChainHeaderViewHolder)
        {
            ChainHeader header = (ChainHeader) items.get(position);
            ((ChainHeaderViewHolder) holder).bind(header, expandedChains.contains(header.network.chainId));
            holder.itemView.setOnClickListener(v -> toggleChain(header.network.chainId, holder.getAdapterPosition()));
        }
        else if (holder instanceof RPCEntryViewHolder)
        {
            RPCItem rpcItem = (RPCItem) items.get(position);
            ((RPCEntryViewHolder) holder).bind(rpcItem.rpcData);
        }
    }

    @Override
    public int getItemCount()
    {
        return items.size();
    }

    private void toggleChain(long chainId, int headerPosition)
    {
        if (expandedChains.contains(chainId))
        {
            // Collapse: remove RPC entries after this header
            expandedChains.remove(chainId);
            int removeStart = headerPosition + 1;
            int removeCount = 0;
            while (removeStart + removeCount < items.size()
                    && items.get(removeStart + removeCount) instanceof RPCItem)
            {
                removeCount++;
            }
            if (removeCount > 0)
            {
                items.subList(removeStart, removeStart + removeCount).clear();
                notifyItemRangeRemoved(removeStart, removeCount);
            }
            notifyItemChanged(headerPosition); // update arrow/count
        }
        else
        {
            // Expand: insert RPC entries after this header
            expandedChains.add(chainId);
            ChainHeader header = (ChainHeader) items.get(headerPosition);

            // Re-fetch from the header's stored data count - we need to find the RPCs
            // They were stored during construction. Re-read from the items list won't work
            // since they were removed. We store RPCs in the header.
            List<RPCItem> rpcItems = new ArrayList<>();
            for (RPCData rpc : header.rpcDataList)
            {
                rpcItems.add(new RPCItem(chainId, rpc));
            }

            items.addAll(headerPosition + 1, rpcItems);
            notifyItemRangeInserted(headerPosition + 1, rpcItems.size());
            notifyItemChanged(headerPosition); // update arrow/count
        }
    }

    // ---- View Holders ----

    static class ChainHeaderViewHolder extends RecyclerView.ViewHolder
    {
        final TokenIcon tokenIcon;
        final TextView chainName;
        final TextView chainId;
        final TextView rpcCount;

        ChainHeaderViewHolder(View view)
        {
            super(view);
            tokenIcon = view.findViewById(R.id.token_icon);
            chainName = view.findViewById(R.id.chain_name);
            chainId = view.findViewById(R.id.chain_id);
            rpcCount = view.findViewById(R.id.rpc_count);
        }

        void bind(ChainHeader header, boolean expanded)
        {
            tokenIcon.bindData(header.network.chainId);
            chainName.setText(header.network.name);
            chainId.setText(itemView.getContext().getString(R.string.chain_id, header.network.chainId));
            String countText = header.rpcCount + " RPCs";
            if (!expanded) countText += " \u25B6"; // right arrow
            else countText += " \u25BC"; // down arrow
            rpcCount.setText(countText);
        }
    }

    static class RPCEntryViewHolder extends RecyclerView.ViewHolder
    {
        final View rankIndicator;
        final TextView rpcUrl;
        final TextView rpcRank;

        RPCEntryViewHolder(View view)
        {
            super(view);
            rankIndicator = view.findViewById(R.id.rank_indicator);
            rpcUrl = view.findViewById(R.id.rpc_url);
            rpcRank = view.findViewById(R.id.rpc_rank);
        }

        void bind(RPCData data)
        {
            rpcUrl.setText(data.url);

            if (data.rank < 0)
            {
                // Untested
                rpcRank.setText(R.string.rpc_rank_untested);
                setIndicatorColor(0xFFBDBDBD); // grey
            }
            else
            {
                // Ranked by response time
                rpcRank.setText(itemView.getContext().getString(R.string.rpc_rank_ms, data.rank));
                if (data.rank < 500)
                {
                    setIndicatorColor(0xFF75B943); // green - fast
                }
                else if (data.rank < 1500)
                {
                    setIndicatorColor(0xFFFD9426); // orange - medium
                }
                else
                {
                    setIndicatorColor(0xFFFF3B30); // red - slow
                }
            }
        }

        private void setIndicatorColor(int color)
        {
            if (rankIndicator.getBackground() instanceof GradientDrawable)
            {
                ((GradientDrawable) rankIndicator.getBackground().mutate()).setColor(color);
            }
        }
    }

    // ---- Data classes ----

    static class ChainHeader
    {
        final NetworkInfo network;
        final int rpcCount;
        final List<RPCData> rpcDataList;

        ChainHeader(NetworkInfo network, int rpcCount)
        {
            this.network = network;
            this.rpcCount = rpcCount;
            this.rpcDataList = new ArrayList<>();
        }
    }

    static class RPCData
    {
        final String url;
        final long rank;

        RPCData(String url, long rank)
        {
            this.url = url;
            this.rank = rank;
        }
    }

    static class RPCItem
    {
        final long chainId;
        final RPCData rpcData;

        RPCItem(long chainId, RPCData rpcData)
        {
            this.chainId = chainId;
            this.rpcData = rpcData;
        }
    }
}
