package com.raffa064.webster;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.raffa064.webster.MainActivity;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.EditorInfo;

public class MainActivity extends AppCompatActivity {
  private WebView webView;
  private SharedPreferences preferences;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    webView = new WebView(this) {
      @Override
      public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        MyCustomInputConnection myCustomInputConnection = new MyCustomInputConnection(webView, false);
        return myCustomInputConnection;
      }
    };
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
  
  public void toggleStatusBar() {
    View decorView = getWindow().getDecorView();
    int uiOptions = decorView.getSystemUiVisibility();

    if ((uiOptions & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
      // Esconde a status bar
      decorView.setSystemUiVisibility(uiOptions | View.SYSTEM_UI_FLAG_FULLSCREEN);
    } else {
      // Mostra a status bar
      decorView.setSystemUiVisibility(uiOptions & ~View.SYSTEM_UI_FLAG_FULLSCREEN);
    }
  }


  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
    if (event.getAction() == KeyEvent.ACTION_DOWN && event.isCtrlPressed()) {
      switch (event.getKeyCode()) {
        case KeyEvent.KEYCODE_F:
  toggleStatusBar();
          break;
        case KeyEvent.KEYCODE_U:
          openUrlDialog();
          break;
        case KeyEvent.KEYCODE_R:
          clearCache();
          webView.reload();
          break;
        case KeyEvent.KEYCODE_D:
          String code = (
            "if (window.eruda) {" +
            "    if (eruda._isInit) {" +
            "        eruda.destroy();" +
            "    } else {" +
            "        eruda.init();" +
            "    }" +
            "} else {" +
            "    var erudaScript = document.createElement('script');" +
            "    erudaScript.src = 'https://cdn.jsdelivr.net/npm/eruda';" +
            "    erudaScript.onload = () => { eruda.init() };" +
            "    document.body.appendChild(erudaScript);" +
            "}"
            );

          webView.evaluateJavascript(code, null);
          break;
      }
    }

    return super.dispatchKeyEvent(event);
  }

  private void setupWebView() {
    webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
    webView.addJavascriptInterface(this, "app");
    webView.setWebViewClient(new WebViewClient());
    //webView.setBackgroundColor(Color.TRANSPARENT);

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
    LinearLayout layout = new LinearLayout(this);
    layout.setOrientation(LinearLayout.VERTICAL);

    final EditText editText = new EditText(this);
    editText.setText(getTestUrl());
    editText.setHint("URL or localhost port");
    editText.requestFocus();

    Button button = new Button(this);
    button.setText("Use current page");
    button.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          editText.setText(webView.getUrl());
        }
      });

    layout.addView(editText);
    layout.addView(button);

    new AlertDialog.Builder(this)
      .setTitle("Change test URL")
      .setView(layout)
      .setNegativeButton("Cancel", null)
      .setPositiveButton("Change", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          String input = editText.getText().toString();

          try {
            int port = Integer.parseInt(input);
            setTestUrl("http://localhost:" + port);
          } catch (NumberFormatException e) {
            setTestUrl(input);
          }

          load();
        }
      })
      .create()
      .show();

  }

  private void load() {
    clearCache();

    webView.loadUrl(getTestUrl());
  }

  private void clearCache() {
    webView.clearCache(true);
    webView.clearHistory();
    System.gc();
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
