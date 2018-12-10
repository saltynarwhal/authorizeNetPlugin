/**
 */
package com.saltynarwhal.cordova.plugin;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaActivity;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import android.util.Log;
import android.content.Context;
import android.content.Intent;

import net.authorize.Environment;
import net.authorize.Merchant;
import net.authorize.TransactionType;
import net.authorize.aim.cardpresent.DeviceType;
import net.authorize.aim.cardpresent.MarketType;
import net.authorize.aim.emv.EMVErrorCode;
import net.authorize.aim.emv.EMVTransaction;
import net.authorize.aim.emv.EMVTransactionManager;
import net.authorize.aim.emv.EMVTransactionManager.EMVTransactionListener;
import net.authorize.aim.emv.EMVTransactionType;
import net.authorize.auth.PasswordAuthentication;
import net.authorize.auth.SessionTokenAuthentication;
import net.authorize.data.Order;
import net.authorize.data.swiperdata.*;
import net.authorize.data.OrderItem;
import net.authorize.data.creditcard.CreditCard;
import net.authorize.data.creditcard.CreditCardPresenceType;
import net.authorize.data.mobile.MobileDevice;
import net.authorize.mobile.Transaction;

import java.math.BigDecimal;

public class authorizeNetPlugin extends CordovaPlugin {
  private static final String TAG = "authorizeNetPlugin";
  private static Merchant merchant;

  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);
    Log.d(TAG, "Initializing authozizeNetPlugin");
  }

  public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {

    if(action.equals("initMerchant")){
      initMerchant(args, callbackContext);
      return true;
    } else if (action.equals("createEMVTransaction")) {
      createEMVTransaction(args, callbackContext);
      return true;
    } else if (action.equals("createNonEMVTransaction")) {
      createNonEMVTransaction(args, callbackContext);
      return true;
    }

    callbackContext.error("No method found.");
    return true;
  }


  private Transaction createLoginTransaction(){
    return this.merchant.createMobileTransaction(net.authorize.mobile.TransactionType.MOBILE_DEVICE_LOGIN);
  }

  private void setEnvironment(String environment, PasswordAuthentication passAuth){
      if(environment.equals("sandbox")) {
        this.merchant = Merchant.createMerchant(Environment.SANDBOX, passAuth);
      } else if (environment.equals("production")){
        this.merchant = Merchant.createMerchant(Environment.PRODUCTION, passAuth);
      }
  }


  public void initMerchant(JSONArray args, CallbackContext callbackContext) throws JSONException {
    net.authorize.mobile.Result result;

    String deviceID = args.getString(0);
    String deviceDescription = args.getString(1);
    String deviceNumber = args.getString(2);
    String username = args.getString(3);
    String password = args.getString(4);
    String environment = args.getString(5);


    PasswordAuthentication passAuth = PasswordAuthentication
      .createMerchantAuthentication(username, password, deviceID);

    setEnvironment(environment, passAuth);

    Transaction transaction = createLoginTransaction();

    MobileDevice mobileDevice = MobileDevice
            .createMobileDevice(deviceID, deviceDescription, deviceNumber, "Android");
    transaction.setMobileDevice(mobileDevice);

    result = (net.authorize.mobile.Result) this.merchant.postTransaction(transaction);

    if(result.isOk()){
      try {
          SessionTokenAuthentication sessionTokenAuthentication = SessionTokenAuthentication
                  .createMerchantAuthentication(this.merchant
                          .getMerchantAuthentication().getName(), result
                          .getSessionToken(), deviceID);

          if ((result.getSessionToken() != null) && (sessionTokenAuthentication != null)) {
              this.merchant.setMerchantAuthentication(sessionTokenAuthentication);
              callbackContext.success("Login success.");
          }
      } catch (Exception ex) {
         callbackContext.error(ex.toString());
      }
    } else {
      callbackContext.error(result.getXmlResponse().toString());
    }
  }

  /* Create and Submit an EMV Transaction */
  public void createEMVTransaction(JSONArray args,final CallbackContext callbackContext){
    if (this.merchant == null) {
      callbackContext.error("Merchant does not exist. Please login in.");
      return;
    }

    Order order = Order.createOrder();
    BigDecimal amount;
    String solutionID;
    try {
      JSONObject params = args.getJSONObject(0);
      JSONArray items = params.getJSONArray("items");
      solutionID = params.getString("solution_id");
      amount = new BigDecimal(params.getString("amount"));
      order.setTotalAmount(amount);

      for (int i = 0; i < items.length(); i++) {
        JSONObject row = items.getJSONObject(i);

        OrderItem item = OrderItem.createOrderItem();
        item.setItemId(row.getString("id"));
        item.setItemName(row.getString("name"));
        item.setItemQuantity(row.getString("quantity"));
        item.setItemTaxable(row.getBoolean("taxable"));
        item.setItemDescription(row.getString("description"));
        item.setItemPrice(new BigDecimal(row.getString("price")));
        order.addOrderItem(item);
      }

    } catch(JSONException e){
      callbackContext.error("Problems with JSON args.");
      return;
    }


    EMVTransactionListener iemvTransaction = new EMVTransactionListener() {
      @Override
      public void onEMVTransactionSuccessful(net.authorize.aim.emv.Result result) {
        callbackContext.success("onEMVTransactionSuccessful: " + result.toString());
      }

      @Override
      public void onEMVReadError(EMVErrorCode emvError) {
        callbackContext.error("onEMVReadError: " + emvError.toString());
      }

      @Override
      public void onEMVTransactionError(net.authorize.aim.emv.Result result, EMVErrorCode emvError) {
        callbackContext.error("onEMVTransactionError: " + emvError.toString());
      }
    };

    EMVTransaction emvTransaction = EMVTransactionManager.createEMVTransaction(this.merchant, amount);
    emvTransaction.setEmvTransactionType(EMVTransactionType.SERVICES);
    emvTransaction.setOrder(order);
    emvTransaction.setSolutionID(solutionID);

    EMVTransactionManager.startEMVTransaction(emvTransaction, iemvTransaction, cordova.getActivity());
  }

  /* Create Non-EMV transaction Using Encrypted Swiper Data */
  public void createNonEMVTransaction(JSONArray args, final CallbackContext callbackContext){
    if (this.merchant == null) {
      callbackContext.error("Not exists Merchant. Please login in.");
      return;
    }

    String IDtechBlob;
    Order order = Order.createOrder();
    BigDecimal amount;
    String DeviceInfo;

//items = itens
    try {
      JSONObject params = args.getJSONObject(0);
      JSONArray items = params.getJSONArray("itens");
      IDtechBlob = params.getString("id_tech_blob");
      DeviceInfo = params.getString("device_info");
      amount = new BigDecimal(params.getString("amount"));
      order.setTotalAmount(amount);

      for (int i = 0; i < items.length(); i++) {
        JSONObject row = items.getJSONObject(i);

        OrderItem item = OrderItem.createOrderItem();
        item.setItemId(row.getString("id"));
        item.setItemName(row.getString("name"));
        item.setItemQuantity(row.getString("quantity"));
        item.setItemTaxable(row.getBoolean("taxable"));
        item.setItemDescription(row.getString("description"));
        item.setItemPrice(new BigDecimal(row.getString("price")));
        order.addOrderItem(item);
      }

    } catch(JSONException e){
      callbackContext.error("Problems with JSON args.");
      return;
    }


    CreditCard creditCard = CreditCard.createCreditCard();
    creditCard.setCardPresenseType(net.authorize.data.creditcard.CreditCardPresenceType.CARD_PRESENT_ENCRYPTED);
    creditCard.getSwipperData().setMode(SwiperModeType.DATA);

    creditCard.getSwipperData().setEncryptedData(IDtechBlob);
    creditCard.getSwipperData().setDeviceInfo(DeviceInfo);
    creditCard.getSwipperData().setEncryptionAlgorithm(SwiperEncryptionAlgorithmType.TDES);

    net.authorize.aim.Transaction authCaptureTransaction = net.authorize.aim.
            Transaction.createTransaction(merchant, TransactionType.AUTH_CAPTURE, amount);

    authCaptureTransaction.setCreditCard(creditCard);
    authCaptureTransaction.setOrder(order);

    net.authorize.aim.Result authCaptureResult = (net.authorize.aim.Result) merchant.postTransaction(authCaptureTransaction);


    if(authCaptureResult.isOk()) {
      callbackContext.success(authCaptureResult.toString());
    } else {
      callbackContext.error("Transaction Failed.");
    }
  }
}
