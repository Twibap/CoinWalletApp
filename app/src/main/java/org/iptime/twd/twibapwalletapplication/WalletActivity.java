package org.iptime.twd.twibapwalletapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.Wallet;

public class WalletActivity extends AppCompatActivity {

    private static final String TAG = "WalletActivity";

    private static final int INIT_WALLET = 1000;

    private DeterministicSeed mRawSeed;

    NetworkParameters mTestNetParams;
    DeterministicKey mKeyMasterPrivate;
    byte[] mKeyMasterChainCode;
    byte[] mKeyMasterPublic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        // 최초 실행 시 종자키 생성화면으로 이동
        SharedPreferences sf = getSharedPreferences("PrimitiveData",MODE_PRIVATE);
        Log.e(TAG, sf.getString("rootSeed", "Empty"));
        if (!sf.contains("rootSeed")) {
            goMnemonicActivity();
        } else {
            mRawSeed = new Gson().fromJson(sf.getString("rootSeed", null), DeterministicSeed.class);
            mTestNetParams = TestNet3Params.get();
            Wallet wallet = new Wallet(mTestNetParams);

            mKeyMasterPrivate = HDKeyDerivation.createMasterPrivateKey(mRawSeed.getSeedBytes());
            mKeyMasterChainCode = mKeyMasterPrivate.getChainCode();
            mKeyMasterPublic = mKeyMasterPrivate.getPubKey();

        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(WalletActivity.this, "코인 전송", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_wallet, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case INIT_WALLET:
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_backup:
                goMnemonicActivity();
                return true;
            case R.id.menu_show_address:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Bitcoin 수신 주소")
                        .setMessage(mKeyMasterPrivate.toAddress(mTestNetParams).toBase58())
                        .setPositiveButton("확인", null)
                        .setCancelable(false)
                        .create()
                        .show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * RawSeed의 연상기호를 백업할 수 있는 화면으로 이동한다.
     */
    private void goMnemonicActivity(){
        Intent intent = new Intent(this, MnemonicActivity.class);
        startActivityForResult(intent, INIT_WALLET);
    }
}
