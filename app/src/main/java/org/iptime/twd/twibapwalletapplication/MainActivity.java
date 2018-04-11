package org.iptime.twd.twibapwalletapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

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
                byte[] rootSeeds = getEntropy(128);

                String text = bytesToHex(rootSeeds);
                Snackbar.make(view, text, Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });

        // 최초 실행 시 종자키 생성화면으로 이동
        SharedPreferences sf = getSharedPreferences("PrimitiveData",MODE_PRIVATE);
        Log.e(TAG, sf.getString("rootSeed", "Empty"));
        if (!sf.contains("rootSeed")) {
            Intent intent = new Intent(this, MnemonicActivity.class);
            startActivityForResult(intent, INIT_WALLET);
        }
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

    /**
     * 무작위 숫자열을 생성한다.
     *
     * @param bits
     * @return
     */
    public byte[] getEntropy(int bits){
        byte[] result = new byte[ bits/8 ];

        SecureRandom random = null;
        try {
            random = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        assert random != null;
        random.nextBytes(result);

        return result;
    }

    /**
     * Byte 배열을 Hex 표현으로 전환한다.
     *
     * https://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java
     */
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

}
