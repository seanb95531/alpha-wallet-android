package com.alphawallet.app.entity.opensea;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Trait
{
    @SerializedName("trait_type")
    @Expose
    private String traitType;

    @SerializedName("value")
    @Expose
    private String value;

    @SerializedName("trait_count")
    @Expose
    private long traitCount;

    private float traitRarity;

    private boolean isUnique;

    public float getTraitRarity()
    {
        return traitRarity;
    }

    public void updateTraitRarity(long totalSupply)
    {
        traitRarity =  ((float) traitCount * 100) / totalSupply;
    }

    public long getTraitCount()
    {
        return traitCount;
    }

    public void setTraitCount(long traitCount)
    {
        this.traitCount = traitCount;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public String getTraitType()
    {
        return traitType;
    }

    public void setTraitType(String traitType)
    {
        this.traitType = traitType;
    }

    public void setUnique(boolean isUnique)
    {
        this.isUnique = isUnique;
    }

    public boolean isUnique()
    {
        return this.isUnique;
    }
}