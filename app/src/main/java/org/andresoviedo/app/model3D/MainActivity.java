package org.andresoviedo.app.model3D;

import java.io.File;

import org.andresoviedo.app.model3D.view.MenuActivity;
import org.andresoviedo.app.model3D.view.ModelActivity;
import org.andresoviedo.app.util.Utils;
import org.andresoviedo.dddmodel2.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

public class MainActivity extends Activity {

	private static final String ASSETS_TARGET_DIRECTORY = Environment.getExternalStorageDirectory() + File.separator
			+ "3DModelViewerOS";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		MainActivity.this.startActivity(new Intent(MainActivity.this.getApplicationContext(), MenuActivity.class));
		MainActivity.this.finish();
	}

	private void init() {
		try {
			Thread tcopy = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						installExamples();
					} catch (Exception ex) {
						Toast.makeText(MainActivity.this.getApplicationContext(),
								"Unexpected error: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
						Log.e("init", "Unexpected error: " + ex.getMessage(), ex);
					}
				}
			});
			Thread tsplash = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(000);
						MainActivity.this.startActivity(
								new Intent(MainActivity.this.getApplicationContext(), ModelActivity.class));
						MainActivity.this.finish();
					} catch (InterruptedException ex) {
						Toast.makeText(MainActivity.this.getApplicationContext(),
								"Unexpected error: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
						Log.e("init", "Unexpected error: " + ex.getMessage(), ex);
					}
				}
			});

		} catch (Exception ex) {
			Toast.makeText(MainActivity.this.getApplicationContext(), "Unexpected error: " + ex.getMessage(),
					Toast.LENGTH_SHORT).show();
			Log.e("init", "Unexpected error: " + ex.getMessage(), ex);
		}
		MainActivity.this.startActivity(new Intent(MainActivity.this.getApplicationContext(), ModelActivity.class));
		MainActivity.this.finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void installExamples() {
		if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			Toast.makeText(MainActivity.this.getApplicationContext(), "Couldn't copy assets. Please install an sd-card",
					Toast.LENGTH_SHORT).show();
			return;
		}
		Utils.copyAssets(getApplicationContext(), "models", new File(ASSETS_TARGET_DIRECTORY, "models"));
	}
}
