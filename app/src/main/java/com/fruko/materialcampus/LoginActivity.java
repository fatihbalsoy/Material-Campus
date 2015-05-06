package com.fruko.materialcampus;

import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import us.plxhack.InfiniteCampus.api.InfiniteCampusApi;
import us.plxhack.InfiniteCampus.api.Student;


public class LoginActivity extends AccountAuthenticatorActivity
{
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    protected UserLoginTask mAuthTask = null;

    // UI references.
    protected EditText mUsernameView;
    protected EditText mDistrictView;
    protected EditText mPasswordView;
    protected CheckBox mSavingInfo;
    protected View mProgressView;
    protected View mLoginFormView;

    protected AccountController accounts;

    protected boolean isVisible = true;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set up the login form.
        mUsernameView = (EditText) findViewById(R.id.username);
        mDistrictView = (EditText) findViewById(R.id.district_code);
        mPasswordView = (EditText) findViewById(R.id.password);
        mSavingInfo = (CheckBox) findViewById(R.id.remember_info);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        accounts = new AccountController( this );

        /*
        AccountController.AccountDataContainer d = accounts.getFirstAccount();

        if (d != null)
            login( d.district, d.username, d.password, false );
            */
    }

    @Override
    protected void onResume( )
    {
        super.onResume();
        isVisible = true;
    }

    @Override
    protected void onPause( )
    {
        super.onPause();
        isVisible = false;
    }

    public void attemptLogin()
    {

        if (mAuthTask != null)
            return;

        // Reset errors.
        mDistrictView.setError(null);
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String district = mDistrictView.getText().toString();
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password))
        {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid username
        if (TextUtils.isEmpty(username))
        {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        }

        // Check for a valid district code
        if (TextUtils.isEmpty(district) || district.length() < 6)
        {
            mDistrictView.setError(getString(R.string.error_field_required));
            focusView = mDistrictView;
            cancel = true;
        }

        if (cancel)
        {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        }
        else
        {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            login( district, username, password, mSavingInfo.isChecked() );
        }
    }

    protected void login( String district, String username, String password, boolean save )
    {
        showProgress(true);
        mAuthTask = new UserLoginTask( this, district, username, password, save, true );
        mAuthTask.execute((Void) null);
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean>
    {

        private final String mUser;
        private final String mPassword;
        private final String mDistrict;
        private final boolean saving;
        private final boolean goBack;
        private final Activity parentActivity;

        private boolean exceptionThrown = false;

        UserLoginTask( Activity a, String district, String user, String password, boolean save, boolean back )
        {
            parentActivity = a;

            mDistrict = district;
            mUser = user;
            mPassword = password;
            saving = save;
            goBack = back;
        }

        @Override
        protected Boolean doInBackground(Void... params)
        {
            try
            {
                return InfiniteCampusApi.login(mDistrict, mUser, mPassword, getApplicationContext(), false);
            }
            catch (Exception e)
            {
                exceptionThrown = true;
                e.printStackTrace();

                return false;
            }
        }

        @Override
        protected void onPreExecute() {
            mLoginFormView.setVisibility( View.INVISIBLE );
        }

        @Override
        protected void onPostExecute(final Boolean success)
        {
            mAuthTask = null;
            showProgress(false);

            if (success && isVisible)
            {
                if (saving && !accounts.saveAccount(mUser, mPassword, mDistrict))
                    System.out.println("failed to save account");

                if (goBack)
                {
                    finish();
                    Intent intent = new Intent(parentActivity, AccountListActivity.class);
                    startActivity(intent);
                }
                else
                {
                    final Intent intent = new Intent();
                    intent.putExtra( AccountManager.KEY_ACCOUNT_NAME, mUser );
                    intent.putExtra( AccountManager.KEY_ACCOUNT_TYPE, AccountController.accountType );

                    setAccountAuthenticatorResult( intent.getExtras() );
                    setResult( RESULT_OK, intent );

                    finish();
                }
            }
            else if (!success)
            {
                mLoginFormView.setVisibility( View.VISIBLE );

                if (exceptionThrown)
                    mPasswordView.setError(getString(R.string.error_exception));
                else
                    mPasswordView.setError(getString(R.string.error_incorrect_password));

                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled()
        {
            mAuthTask = null;
            showProgress(false);
        }
    }
}



