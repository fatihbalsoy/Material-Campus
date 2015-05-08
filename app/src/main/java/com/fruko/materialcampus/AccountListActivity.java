package com.fruko.materialcampus;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.view.View.OnClickListener;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

import nu.xom.ParsingException;
import us.plxhack.InfiniteCampus.api.InfiniteCampusApi;

public class AccountListActivity extends ActionBarActivity
{
    public final static String NO_AUTO_LOGIN = "com.fruko.materialcampus.NO_AUTO_LOGIN";

    private UserLoginTask loginTask;

    private ListView accountsList;
    private ProgressBar progressView;
    private Button addAccountButton;
    private LinearLayout baseView;

    private AccountController accounts;

    private boolean loggingIn = false;
    private boolean isVisible = true;
    private boolean changeActivityWhenVisible = false;

    private void startActivity( Class otherActivity )
    {
        Intent intent = new Intent( this, otherActivity );
        startActivity( intent );
    }

    private void setupAccountsList()
    {
        AccountController.AccountDataContainer[] arrayTemp = accounts.getAccounts();
        final ArrayList<String[]> accountsArray = new ArrayList<>();

        for (int i=0;i < arrayTemp.length;++i)
        {
            AccountController.AccountDataContainer d = arrayTemp[i];
            String[] temp = { d.username };
            accountsArray.add( temp );
        }

        accountsList.setAdapter(new ArrayAdapter<String[]>(this, R.layout.user_list_item, R.id.username, accountsArray) {
            public View getView(final int position, View convertView, ViewGroup parent) {
                View view;
                if (convertView == null) {
                    LayoutInflater infl = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                    convertView = infl.inflate(R.layout.user_list_item, parent, false);
                }
                view = super.getView(position, convertView, parent);

                TextView name = (TextView) view.findViewById(R.id.username);
                name.setText(accountsArray.get(position)[0]);
                return view;
            }
        });

        accountsList.setOnItemClickListener( new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if (loggingIn)
                    return;

                login( accounts.getAccount( position ) );
            }
        });
    }

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accountslist);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        baseView = (LinearLayout)findViewById( R.id.accountslist_base );

        progressView = (ProgressBar)findViewById( R.id.login_progress_alist );
        accountsList = (ListView)findViewById( R.id.accounts_list );
        addAccountButton = (Button)findViewById( R.id.add_account );

        addAccountButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if (loggingIn == false)
                    startActivity( LoginActivity.class );
            }
        });

        accounts = new AccountController( this );
        if (accounts.getNumAccounts() == 0)
            startActivity( LoginActivity.class );
        else
        {
            setupAccountsList();

            if (accounts.getNumAccounts() == 1 && !getIntent().getBooleanExtra(NO_AUTO_LOGIN, false))
                login( accounts.getFirstAccount() );
            else
                baseView.setVisibility( View.VISIBLE );
        }
    }

    @Override
    protected void onResume( )
    {
        super.onResume();

        isVisible = true;

        setupAccountsList();

        if (!loggingIn)
            baseView.setVisibility( View.VISIBLE );

        if (changeActivityWhenVisible)
            startActivity(ClassesActivity.class);
    }

    protected void onStart()
    {
        super.onStart();
    }

    protected void onRestart()
    {
        super.onRestart();
    }

    protected void onPause()
    {
        super.onPause();
        isVisible = false;
    }

    protected void onStop()
    {
        super.onStop();
    }

    protected void onDestroy()
    {
        super.onDestroy();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            progressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    void login( AccountController.AccountDataContainer data )
    {
        showProgress(true);
        loginTask = new UserLoginTask( this, data.district, data.username, data.password );
        loginTask.execute((Void) null);
    }

    public class UserLoginTask extends AsyncTask<Void, Void, Boolean>
    {

        private final String mUser;
        private final String mPassword;
        private final String mDistrict;
        private final Activity parentActivity;

        UserLoginTask( Activity a, String district, String user, String password )
        {
            parentActivity = a;

            mDistrict = district;
            mUser = user;
            mPassword = password;
        }

        @Override
        protected void onPreExecute()
        {
            baseView.setVisibility( View.GONE );
            loggingIn = true;
        }

        @Override
        protected Boolean doInBackground(Void... params)
        {
            Boolean result = false;
            try
            {
                result = InfiniteCampusApi.getInstance().login(mDistrict, mUser, mPassword, getApplicationContext(), false);
            } catch (ParsingException e)
            {
                e.printStackTrace();
            } catch (IOException e)
            {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(final Boolean success)
        {
            loggingIn = false;
            loginTask = null;

            showProgress(false);

            if (success)
            {
                if (isVisible)
                    startActivity(ClassesActivity.class);
                else
                    changeActivityWhenVisible = true;
            }
            else
                baseView.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();

        if (loginTask != null)
            loginTask.cancel(true);
    }

}
