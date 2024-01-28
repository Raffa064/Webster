package com.raffa064.webster;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.Toast;
import com.raffa064.webster.MainActivity;
import android.webkit.WebViewClient;
import android.content.SharedPreferences;

public class MainActivity extends AppCompatActivity {
    private WebView webView;
	private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		webView = new WebView(this);
		setupWebView();

		preferences = getSharedPreferences("data", MODE_PRIVATE);
		load();
    }

	@Override
	protected void onResume() {
		super.onResume();
		load();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (webView != null) {
			webView.saveState(outState);
		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		if (webView != null) {
			webView.restoreState(savedInstanceState);
		}
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN && event.isCtrlPressed()) {
			switch (event.getKeyCode()) {
				case KeyEvent.KEYCODE_U:
					openUrlDialog();
					break;
			}
		}

		return super.dispatchKeyEvent(event);
	}
	
	private void setupWebView() {
		webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		webView.addJavascriptInterface(this, "app");
		webView.setWebViewClient(new WebViewClient());

		WebSettings settings = webView.getSettings();
		settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
		settings.setJavaScriptEnabled(true);
		settings.setForceDark(WebSettings.FORCE_DARK_OFF);
		settings.setDomStorageEnabled(true);
		settings.setAllowUniversalAccessFromFileURLs(true);
		settings.setAllowFileAccessFromFileURLs(true);

		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
			settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW); // http/https access (used for debug)
		}

		setContentView(webView);
	}

	public void setTestUrl(String url) {
		preferences.edit().putString("test-url", url).commit();
	}
	
	public String getTestUrl() {
		return preferences.getString("test-url", "http://localhost:8000");
	}

	public void openUrlDialog() {
		final EditText editText = new EditText(this);
		editText.setText(getTestUrl());
		editText.setHint("URL or localhost port");

		new AlertDialog.Builder(this)
			.setTitle("Change test URL")
			.setView(editText)
			.setNegativeButton("Cancel", null)
			.setPositiveButton("Change", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String input = editText.getText().toString();
					
					try {
						int port = Integer.parseInt(input);
						setTestUrl("http://localhost:"+port);
					} catch(NumberFormatException e) {
						setTestUrl(input);
					}
					
					load();
				}
			})
			.create()
			.show();

	}

	private void load() {
		webView.clearCache(true);
		webView.clearHistory();
		System.gc();

		webView.loadUrl(getTestUrl());
	}

	private void showToast(final String message) {
		runOnUiThread(new Runnable() {
				@Override
				public void run() {
					int length = message.length() < 20 ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG;

					Toast.makeText(MainActivity.this, message, length).show();			
				}
			});
	}
}
