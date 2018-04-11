package org.iptime.twd.twibapwalletapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

/**
 * 저장된 사용자의 지갑 목록을 보여주는 화면이다.
 * 지갑 생성 버튼을 누르면 지갑이 생성된다.
 * 지갑을 선택하면 해당 지갑을 관리하는 화면으로 전환된다.
 */
public class MainActivity extends AppCompatActivity {

    private static final int INIT_WALLET = 1000;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "토스트 메시지", Toast.LENGTH_SHORT).show();
            }
        });

        // 최초 실행 시 종자키 생성화면으로 이동
        SharedPreferences sf = getSharedPreferences("PrimitiveData",MODE_PRIVATE);
        Log.e(TAG, sf.getString("rootSeed", "Empty"));
        if (!sf.contains("rootSeed")) {
            goMnemonicActivity();
        }
    }

    /**
     * RawSeed의 연상기호를 백업할 수 있는 화면으로 이동한다.
     */
    private void goMnemonicActivity(){
        Intent intent = new Intent(this, MnemonicActivity.class);
        startActivityForResult(intent, INIT_WALLET);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_backup:
                goMnemonicActivity();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
