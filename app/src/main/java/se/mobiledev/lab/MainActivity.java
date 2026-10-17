package se.mobiledev.lab;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView status = findViewById(R.id.statusText);
        Button button = findViewById(R.id.testButton);

        button.setOnClickListener(v -> {
            status.setText(R.string.status_ok);
            Toast.makeText(
                this,
                R.string.toast_test_passed,
                Toast.LENGTH_SHORT
            ).show();
        });
    }
}
