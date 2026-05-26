package com.alphawallet.app.viewmodel;

import com.alphawallet.app.repository.PreferenceRepositoryType;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class CrashReportSettingsViewModel extends BaseViewModel
{
    private final PreferenceRepositoryType preferenceRepository;

    @Inject
    CrashReportSettingsViewModel(PreferenceRepositoryType preferenceRepository)
    {
        this.preferenceRepository = preferenceRepository;
    }

    public boolean isCrashReportingEnabled()
    {
        return preferenceRepository.isCrashReportingEnabled();
    }

    public void toggleCrashReporting(boolean isEnabled)
    {
        preferenceRepository.setCrashReportingEnabled(isEnabled);
    }
}
