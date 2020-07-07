package com.cardio;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;

import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;

public class RNCardIOModule extends ReactContextBaseJavaModule implements ActivityEventListener {

  public static final int CARD_IO_SCAN = 1;

  private Promise promise;

  public RNCardIOModule(ReactApplicationContext reactContext) {
    super(reactContext);
    reactContext.addActivityEventListener(this);
  }

  @Override
  public String getName() {
    return "RCTCardIOModule";
  }

  @ReactMethod
  public void scanCard(ReadableMap config, Promise promise) {
    this.promise = promise;
    Activity activity = getCurrentActivity();
    Intent scanIntent = new Intent(activity, CardIOActivity.class);
    scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, true);
    scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, false);
    scanIntent.putExtra(CardIOActivity.EXTRA_SCAN_EXPIRY, true);
    scanIntent.putExtra(CardIOActivity.EXTRA_SUPPRESS_MANUAL_ENTRY, true);
    scanIntent.putExtra(CardIOActivity.EXTRA_SUPPRESS_CONFIRMATION, true);
    if (activity != null) {
      activity.startActivityForResult(scanIntent, CARD_IO_SCAN);
    }
  }

  @Override
  public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
    if (promise != null) {
      if (requestCode != CARD_IO_SCAN) {
        return;
      }
      if (data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
        CreditCard scanResult = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);
        WritableMap res = Arguments.createMap();
        res.putString("cardNumber", scanResult.cardNumber);
        res.putString("redactedCardNumber", scanResult.getRedactedCardNumber());
        res.putInt("expiryMonth", scanResult.expiryMonth);
        res.putInt("expiryYear", scanResult.expiryYear);
        promise.resolve(res);
      } else {
        promise.reject("user_cancelled", "The user cancelled");
      }
    }
  }

  @Override
  public void onNewIntent(Intent intent) {}
}
