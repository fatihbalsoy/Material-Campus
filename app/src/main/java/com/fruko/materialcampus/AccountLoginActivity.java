package com.fruko.materialcampus;

import android.os.Bundle;
import android.view.WindowManager;

public class AccountLoginActivity extends LoginActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    protected void login( String district, String username, String password, boolean save )
    {
        showProgress(true);
        mAuthTask = new UserLoginTask( this, district, username, password, save, false );
        mAuthTask.execute((Void) null);
    }
}