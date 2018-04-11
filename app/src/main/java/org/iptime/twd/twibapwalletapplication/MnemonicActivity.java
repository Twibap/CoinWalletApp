package org.iptime.twd.twibapwalletapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.wallet.DeterministicSeed;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;

/**
 * 종자키를 백업 및 복원할 수 있는 화면이다.
 * 앱 최초실행 또는 MainActivity에서 백업/복원 버튼을 누를 때 보이는 화면이다.
 *
 * 종자키를 백업할 수 있는 연상단어들이 화면에 나타난다.
 *
 * 최초실행 시 화면에 나타난 연상 단어들을 바꾸거나 이미 알고있는 연상 단어를 입력해 지갑을 복원한다.
 *
 * 최초실행이 아닌 경우 백업을 위해서 접속하는 경우 연상 단어를 바꿀 수 없다.
 */
public class MnemonicActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "MnemonicActivity";

    DeterministicSeed       mRawSeed;

    final String            mPassparse = "";
    final int               mSeedLength = 128;
    int                     mMnemonicAmount = (mSeedLength+(mSeedLength/32))/11;

    GridView                mViewMnemonic;
    Button                  mBtRestore;
    Button                  mBtRefresh;

    ArrayAdapter<String>    mAdapterMnemonic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mnemonic);

        // View 초기화
        initView();

        SharedPreferences sf = getSharedPreferences("PrimitiveData", MODE_PRIVATE);
        Log.e(TAG, sf.getString("rootSeed", "Empty"));
        if (!sf.contains("rootSeed")) {
            // Data 초기화
            initData();
        } else {
            // 이미 사용중인 지갑이 있는 경우 새로고침 버튼을 비활성화 한다.
            mBtRefresh.setEnabled(false);

            // 저장되어있는 연상 기호를 출력한다.
            mRawSeed = new Gson().fromJson(sf.getString("rootSeed", null), DeterministicSeed.class);
            mAdapterMnemonic.addAll(mRawSeed.getMnemonicCode());


        }

    }

    /**
     * 연상 기호를 입력하기 위해 보이는 값들을 비운다.
     */
    private void setRestore(){
        // TODO: 2018. 4. 11. "완료" 클릭 시 Adapter의 값이 확인되지 않고 있음
        mAdapterMnemonic.clear();
        for (int i = 0; i < mMnemonicAmount; i++)
            mAdapterMnemonic.add("");
        mAdapterMnemonic.notifyDataSetChanged();

        // 키보드의 ime 버튼 클릭 시 값 확인 후 adapter에 add 하려 했지만 이벤트 발생 안함
        EditText editMnemonic = (EditText) findViewById(R.id.item_edit_mnemonic);
        editMnemonic.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                switch (i){
                    case 100:   // item_edit_mnemonic imeActionId = 100
                        Toast.makeText(MnemonicActivity.this, textView.getText() + " 다음", Toast.LENGTH_SHORT).show();
                        mAdapterMnemonic.add(textView.getText().toString());
                        break;
                }
                return false;
            }
        });
    }

    /**
     * 지갑에 필요한 Seed와 연상기호를 생성한다.
     */
    private void initData(){
        mRawSeed = new DeterministicSeed(new SecureRandom(), mSeedLength, mPassparse, new Date().getTime());
        Log.e(TAG, new GsonBuilder().setPrettyPrinting().create().toJson(mRawSeed));

        if (mRawSeed.getMnemonicCode() != null) {
            mAdapterMnemonic.addAll(mRawSeed.getMnemonicCode());
            mAdapterMnemonic.notifyDataSetChanged();
        } else {
            Log.e(TAG, "Error - MnemonicCode is null");
            Toast.makeText(this, "Error : MnemonicCode is null", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 화면에 보이는 View 객체를 생성한다.
     */
    private void initView(){
        mViewMnemonic = (GridView) findViewById(R.id.viewMnemonic);
        mBtRefresh      = (Button) findViewById(R.id.btRefrash);
        mBtRestore      = (Button) findViewById(R.id.btRestore);

        mBtRefresh.setOnClickListener(this);
        mBtRestore.setOnClickListener(this);

        mAdapterMnemonic = new ArrayAdapter<>(this, R.layout.item_edit_mnemonic, new ArrayList<String>());
        mViewMnemonic.setAdapter(mAdapterMnemonic);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_mnemonic, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_ok:
                boolean isCorrect = false;

                String[] userInputMnemonic = new String[mMnemonicAmount];
                for (int i = 0; i < mAdapterMnemonic.getCount() ; i++)
                    userInputMnemonic[i] = mAdapterMnemonic.getItem(i);

                // 입력된 단어 BIP 39의 단어인지 확인한다.
                ArrayList<String> wordList = (ArrayList<String>) MnemonicCode.INSTANCE.getWordList();
                for (String userData : userInputMnemonic){
                    for (String word : wordList){
                        if (userData.equalsIgnoreCase(word))
                            isCorrect = true;
                    }
                }

                if (isCorrect) {
                    // 초기설정 완료 저장
                    SharedPreferences sf = getSharedPreferences("PrimitiveData", MODE_PRIVATE);
                    sf.edit().putString("rootSeed", new Gson().toJson(mRawSeed)).apply();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(this, "올바른 단어를 입력하세요", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btRefrash:
                mAdapterMnemonic.clear();
                initData();
                break;
            case R.id.btRestore:
                setRestore();
                Toast.makeText(this, "기능 구현 필요", Toast.LENGTH_SHORT).show();
                break;
        }
    }

}
