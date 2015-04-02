package com.fruko.materialcampus;

import android.os.Bundle;

public class AccountLoginActivity extends LoginActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        System.out.println("created accountloginactivity");
    }

    @Override
    protected void login( String district, String username, String password, boolean save )
    {
        showProgress(true);
        mAuthTask = new UserLoginTask( this, district, username, password, save, false );
        mAuthTask.execute((Void) null);
    }
}