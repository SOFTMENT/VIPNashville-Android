package in.softment.nashville;

import static com.android.billingclient.api.BillingClient.SkuType.SUBS;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import in.softment.nashville.Adapter.BarAdapter;
import in.softment.nashville.Model.BarModel;
import in.softment.nashville.Util.Constants;
import in.softment.nashville.Util.ProgressHud;
import in.softment.nashville.Util.Security;
import in.softment.nashville.Util.Services;

public class MainActivity extends AppCompatActivity implements PurchasesUpdatedListener {

    private BarAdapter barAdapter;
    private FusedLocationProviderClient fusedLocationClient;
    private LinearLayout locationLL;
    private TextView locationTV;
    private TextView userName;
    Geocoder geocoder;
    List<Address> addresses;
    String fullName = "";
    AppCompatButton broadWayBtn, midtownBtn, printersAlleyBtn;
    String sku = "product_nashville_pro";
    private BillingClient billingClient;
    public static final String PREF_FILE= "MyPref";
    public static final String SUBSCRIBE_KEY= "subscribe";
    private ArrayList<BarModel> barItems = new ArrayList<>();
    private ArrayList<BarModel> cityBarItems = new ArrayList<>();
    private ArrayList<BarModel> cityBarFinalItems = new ArrayList<>();
    public LinearLayout no_bars_ll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        no_bars_ll = findViewById(R.id.no_bar_available_LL);

        billingClient = BillingClient.newBuilder(this).enablePendingPurchases().setListener(this).build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if(billingResult.getResponseCode()==BillingClient.BillingResponseCode.OK){
                    Purchase.PurchasesResult queryPurchase = billingClient.queryPurchases(SUBS);
                    List<Purchase> queryPurchases = queryPurchase.getPurchasesList();
                    if(queryPurchases!=null && queryPurchases.size()>0){
                        handlePurchases(queryPurchases);
                    }
                    //if no item in purchase list means subscription is not subscribed
                    //Or subscription is cancelled and not renewed for next month
                    // so update pref in both cases
                    // so next time on app launch our premium content will be locked
                    else{
                        saveSubscribeValueToPref(false);
                    }
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                Toast.makeText(MainActivity.this,"Service Disconnected",Toast.LENGTH_SHORT).show();
            }
        });

        BottomSheetDialog membershipDialog = new BottomSheetDialog(MainActivity.this, R.style.BottomSheetStyle);
        View view3 = LayoutInflater.from(MainActivity.this).inflate(R.layout.membership_sheet_dialog,findViewById(R.id.membership_sheet));
        view3.findViewById(R.id.activateBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                membershipDialog.dismiss();
                if (billingClient.isReady()) {
                    initiatePurchase(sku);
                }
                //else reconnect service
                else{
                    billingClient = BillingClient.newBuilder(MainActivity.this).enablePendingPurchases().setListener(MainActivity.this).build();
                    String finalSku = sku;
                    billingClient.startConnection(new BillingClientStateListener() {
                        @Override
                        public void onBillingSetupFinished(BillingResult billingResult) {
                            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                                initiatePurchase(finalSku);
                            } else {
                                Toast.makeText(MainActivity.this,"Error "+billingResult.getDebugMessage(),Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onBillingServiceDisconnected() {
                            Toast.makeText(MainActivity.this,"Service Disconnected ",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });


        view3.findViewById(R.id.termsAndConditions).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://softment.in/terms-of-service/"));
                startActivity(browserIntent);
                membershipDialog.dismiss();
            }
        });

        view3.findViewById(R.id.privacyPolicy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://softment.in/privacy-policy/"));
                startActivity(browserIntent);
                membershipDialog.dismiss();
            }
        });

        geocoder = new Geocoder(this,Locale.getDefault());
        barAdapter = new BarAdapter(this,cityBarItems);
        locationLL = findViewById(R.id.locationLL);
        locationTV = findViewById(R.id.location);
        userName = findViewById(R.id.hiname);


        //Cities
        broadWayBtn = findViewById(R.id.broadwayBtn);
        midtownBtn = findViewById(R.id.midtownBtn);
        printersAlleyBtn = findViewById(R.id.printersAlleyBtn);

        broadWayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getCityFor(2);
                broadWayBtn.setBackgroundResource(R.drawable.cityback);
                midtownBtn.setBackgroundResource(R.drawable.citybackwhite);
                printersAlleyBtn.setBackgroundResource(R.drawable.citybackwhite);

                broadWayBtn.setTextColor(getResources().getColor(R.color.white));
                midtownBtn.setTextColor(getResources().getColor(R.color.black));
                printersAlleyBtn.setTextColor(getResources().getColor(R.color.black));



            }
        });


        midtownBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!hasMembership()) {

                    membershipDialog.setContentView(view3);
                    membershipDialog.show();

                    return;
                }
                getCityFor(1);
                broadWayBtn.setBackgroundResource(R.drawable.citybackwhite);
                midtownBtn.setBackgroundResource(R.drawable.cityback);
                printersAlleyBtn.setBackgroundResource(R.drawable.citybackwhite);

                broadWayBtn.setTextColor(getResources().getColor(R.color.black));
                midtownBtn.setTextColor(getResources().getColor(R.color.white));
                printersAlleyBtn.setTextColor(getResources().getColor(R.color.black));

            }
        });

        printersAlleyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!hasMembership()) {

                    membershipDialog.setContentView(view3);
                    membershipDialog.show();

                    return;
                }
                getCityFor(3);
                broadWayBtn.setBackgroundResource(R.drawable.citybackwhite);
                midtownBtn.setBackgroundResource(R.drawable.citybackwhite);
                printersAlleyBtn.setBackgroundResource(R.drawable.cityback);

                broadWayBtn.setTextColor(getResources().getColor(R.color.black));
                midtownBtn.setTextColor(getResources().getColor(R.color.black));
                printersAlleyBtn.setTextColor(getResources().getColor(R.color.white));
            }
        });



        //Filter
        ImageView filter = findViewById(R.id.filter);
        filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Initializing the popup menu and giving the reference as current context
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, filter);

                // Inflating popup menu from popup_menu.xml file
                popupMenu.getMenuInflater().inflate(R.menu.filter_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if (menuItem.getItemId() == R.id.rating) {
                            getAllBar("Rating");
                        }
                        else if (menuItem.getItemId() == R.id.atoz){
                            getAllBar("AtoZ");
                        }
                        return true;
                    }
                });
                // Showing the popup menu
                popupMenu.show();
            }
        });
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(barAdapter);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            getLocation();
        } else {

            requestPermissionLauncher.launch(
                    Manifest.permission.ACCESS_FINE_LOCATION);
        }

        findViewById(R.id.profile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BottomSheetDialog profileDialog = new BottomSheetDialog(MainActivity.this, R.style.BottomSheetStyle);
                View view2 = LayoutInflater.from(MainActivity.this).inflate(R.layout.profile_sheet_dialog,findViewById(R.id.sheet));


                view2.findViewById(R.id.shareApp).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("text/plain");
                            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "V.I.P Nashville");
                            String shareMessage= "\nLet me recommend you this application\n\n";
                            shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID +"\n\n";
                            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                            startActivity(Intent.createChooser(shareIntent, "choose one"));
                        } catch(Exception e) {
                            //e.toString();
                        }
                    }
                });

                view2.findViewById(R.id.rateApp).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Uri uri = Uri.parse("market://details?id=" + getPackageName());
                        Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
                        try {
                            startActivity(myAppLinkToMarket);
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(MainActivity.this, " unable to find market app", Toast.LENGTH_LONG).show();
                        }
                    }
                });

                view2.findViewById(R.id.privacyPolicy).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://softment.in/privacy-policy"));
                        startActivity(browserIntent);
                    }
                });

                view2.findViewById(R.id.termsAndConditions).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://softment.in/terms-of-service"));
                        startActivity(browserIntent);
                    }

                });

//                view2.findViewById(R.id.logout).setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this,R.style.AlertDialogTheme);
//                        builder.setTitle("Logout");
//                        builder.setMessage("Are you sure you want to logout?");
//                        builder.setCancelable(false);
//                        builder.setNegativeButton("Logout", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                                     Services.logout(MainActivity.this);
//                            }
//                        });
//
//                        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                                dialogInterface.cancel();
//                            }
//                        });
//
//                        builder.show();
//                    }
//                });

//
//                view2.findViewById(R.id.delete_account).setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this,R.style.AlertDialogTheme);
//                        builder.setTitle("Delete Account");
//                        builder.setMessage("Are you sure you want to delete your account?");
//                        builder.setCancelable(false);
//                        builder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                                ProgressHud.show(MainActivity.this,"Deleting ...");
//                                FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().getUid()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<Void> task) {
//                                            ProgressHud.dialog.dismiss();
//                                        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
//                                        final FirebaseUser currentUser = firebaseAuth.getCurrentUser();
//                                        currentUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
//                                            @Override
//                                            public void onComplete(@NonNull Task<Void> task) {
//                                                if (task.isSuccessful()) {
//
//                                                    startActivity(new Intent(MainActivity.this, SignInActivity.class));
//                                                    finish();
//                                                } else {
//                                                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this,R.style.AlertDialogTheme);
//                                                    builder.setTitle("LOGIN AGAIN");
//                                                    builder.setMessage("Please login and try again delete account.");
//                                                    builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
//                                                        @Override
//                                                        public void onClick(DialogInterface dialogInterface, int i) {
//                                                            startActivity(new Intent(MainActivity.this, SignInActivity.class));
//                                                            finish();
//                                                        }
//                                                    });
//
//                                                    builder.show();
//
//                                                }
//                                            }
//                                        });
//                                    }
//                                });
//                            }
//                        });
//
//                        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                                dialogInterface.cancel();
//                            }
//                        });
//
//                        builder.show();
//                    }
//                });

                profileDialog.setContentView(view2);
                profileDialog.show();
            }
        });



        StringRequest stringRequest = new StringRequest(Request.Method.GET, "http://worldtimeapi.org/api/ip/",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        JSONObject parentObject = null;
                        try {
                            parentObject = new JSONObject(response);
                        } catch (JSONException e) {

                            e.printStackTrace();
                        }
                        try {
                            Constants.currentDate = new Date(Long.parseLong(parentObject.getString("unixtime")) * 1000);

                        } catch (JSONException e) {

                            e.printStackTrace();


                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {


                    }
                }){

        };

        EditText searchEditText = findViewById(R.id.searchEditText);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
               cityBarItems.clear();
               if (s.toString().isEmpty()) {
                   cityBarItems.addAll(cityBarFinalItems);
                   barAdapter.notifyDataSetChanged();
                   return;
               }
               for (BarModel barModel : cityBarFinalItems) {
                   if (barModel.name.toLowerCase().contains(s.toString().toLowerCase()) || barModel.address.toLowerCase().contains(s.toString().toLowerCase())) {
                       cityBarItems.add(barModel);
                   }
               }
               barAdapter.notifyDataSetChanged();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {

                // TODO Auto-generated method stub
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

        getAllBar("AtoZ");
    }


    public void getAllBar(String filter) {
        ProgressHud.show(MainActivity.this,"Loading...");
        Query query =  FirebaseFirestore.getInstance().collection("Bars").orderBy("name");
        if (filter.equalsIgnoreCase("Rating")) {
            query = FirebaseFirestore.getInstance().collection("Bars").orderBy("rating", Query.Direction.DESCENDING);
        }
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                ProgressHud.dialog.dismiss();
                if (task.isSuccessful()) {
                    barItems.clear();
                    if (task.getResult() != null && !task.getResult().isEmpty()) {
                        for (DocumentSnapshot documentSnapshot : task.getResult().getDocuments()) {
                            BarModel barModel = documentSnapshot.toObject(BarModel.class);
                            barItems.add(barModel);
                        }
                    }
                    getCityFor(2);
                }
                else {
                    Services.showDialog(MainActivity.this,"ERROR",task.getException().getLocalizedMessage());
                }
            }
        });
    }

    public void getCityFor(int cityId) {
        cityBarItems.clear();
        cityBarFinalItems.clear();
        for (BarModel barModel : barItems) {
            if (barModel.cityId == cityId) {
                cityBarItems.add(barModel);
            }
        }
        cityBarFinalItems = new ArrayList<>(cityBarItems);
        barAdapter.notifyDataSetChanged();
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    getLocation();
                } else {
                    Services.showDialog(MainActivity.this, "Permission Required", "We need access of location permission so we can show nearest bar to you.");
                }
            });

    public void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            try {
                                addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                                String city = addresses.get(0).getLocality();
                                String[] addresses = address.split(",");

                                StringBuilder add = new StringBuilder();
                                int i = 0;
                                for (String value : addresses) {

                                    if (i > 1) {
                                        break;
                                    }
                                    add.append(value).append(", ");
                                    i++;
                                }

                                String myAdd =  add.toString().substring(0, add.length() - 2);
                                locationTV.setText(myAdd);
                                locationLL.setVisibility(View.VISIBLE);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }


                        }
                    }
                });
    }


    private void initiatePurchase(String item_sku) {
        List<String> skuList = new ArrayList<>();
        skuList.add(item_sku);
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(SUBS);
        BillingResult billingResult = billingClient.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS);
        if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
            billingClient.querySkuDetailsAsync(params.build(),
                    new SkuDetailsResponseListener() {
                        @Override
                        public void onSkuDetailsResponse(BillingResult billingResult,
                                                         List<SkuDetails> skuDetailsList) {
                            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                                if (skuDetailsList != null && skuDetailsList.size() > 0) {
                                    BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                                            .setSkuDetails(skuDetailsList.get(0))
                                            .build();
                                    billingClient.launchBillingFlow(MainActivity.this, flowParams);
                                } else {
                                    //try to add subscription item "sub_example" in google play console
                                    Toast.makeText(MainActivity.this, "Item not Found", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(MainActivity.this,
                                        " Error " + billingResult.getDebugMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }else{
            Toast.makeText(MainActivity.this,
                    "Sorry Subscription not Supported. Please Update Play Store", Toast.LENGTH_SHORT).show();
        }
    }

    private SharedPreferences getPreferenceObject() {
        return getSharedPreferences(PREF_FILE, 0);
    }
    private SharedPreferences.Editor getPreferenceEditObject() {
        SharedPreferences pref = getSharedPreferences(PREF_FILE, 0);
        return pref.edit();
    }
    private boolean getSubscribeValueFromPref(){
        return getPreferenceObject().getBoolean( SUBSCRIBE_KEY,false);
    }
    private void saveSubscribeValueToPref(boolean value){
        getPreferenceEditObject().putBoolean(SUBSCRIBE_KEY,value).commit();
    }

    void handlePurchases(List<Purchase>  purchases) {
        for(Purchase purchase:purchases) {
            //if item is purchased

            if ((purchase.getSkus().contains(sku)) && purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED)
            {


                if (!verifyValidSignature(purchase.getOriginalJson(), purchase.getSignature())) {
                    // Invalid purchase
                    // show error to user
                    Toast.makeText(MainActivity.this, "Error : invalid Purchase", Toast.LENGTH_SHORT).show();
                    return;
                }
                // else purchase is valid
                //if item is purchased and not acknowledged
                if (!purchase.isAcknowledged()) {
                    AcknowledgePurchaseParams acknowledgePurchaseParams =
                            AcknowledgePurchaseParams.newBuilder()
                                    .setPurchaseToken(purchase.getPurchaseToken())
                                    .build();
                    billingClient.acknowledgePurchase(acknowledgePurchaseParams, ackPurchase);
                }
                //else item is purchased and also acknowledged
                else {
                    // Grant entitlement to the user on item purchase
                    // restart activity
                    if(!getSubscribeValueFromPref()){
                        saveSubscribeValueToPref(true);
                        Calendar c = Calendar.getInstance();
                        c.setTime(Constants.currentDate);
                        c.add(Calendar.DATE, 30);
                        Constants.expireDate = c.getTime();
                        Map<String , Object> map = new HashMap<>();
                        map.put("expireDate",c.getTime());
                    //    FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).set(map, SetOptions.merge());
                        Toast.makeText(MainActivity.this, "Item Purchased", Toast.LENGTH_SHORT).show();
                        (MainActivity.this).recreate();
                    }
                }
            }
            //if purchase is pending
            else if((purchase.getSkus().contains(sku) && purchase.getPurchaseState() == Purchase.PurchaseState.PENDING))
            {
                Toast.makeText(MainActivity.this,
                        "Purchase is Pending. Please complete Transaction", Toast.LENGTH_SHORT).show();
            }
            //if purchase is unknown mark false
            else if((purchase.getSkus().contains(sku) && purchase.getPurchaseState() == Purchase.PurchaseState.UNSPECIFIED_STATE))
            {
                saveSubscribeValueToPref(false);
                // premiumContent.setVisibility(View.GONE);
                // subscribe.setVisibility(View.VISIBLE);
                // subscriptionStatus.setText("Subscription Status : Not Subscribed");
                Toast.makeText(MainActivity.this, "Purchase Status Unknown", Toast.LENGTH_SHORT).show();
            }
        }
    }
    AcknowledgePurchaseResponseListener ackPurchase = new AcknowledgePurchaseResponseListener() {
        @Override
        public void onAcknowledgePurchaseResponse(BillingResult billingResult) {
            if(billingResult.getResponseCode()==BillingClient.BillingResponseCode.OK){
                //if purchase is acknowledged
                // Grant entitlement to the user. and restart activity
                saveSubscribeValueToPref(true);
                (MainActivity.this).recreate();
            }
        }
    };

    /**
     * Verifies that the purchase was signed correctly for this developer's public key.
     * <p>Note: It's strongly recommended to perform such check on your backend since hackers can
     * replace this method with "constant true" if they decompile/rebuild your app.
     * </p>
     */
    private boolean verifyValidSignature(String signedData, String signature) {
        try {
            // To get key go to Developer Console > Select your app > Development Tools > Services & APIs.
            String base64Key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAm+gW7QLzIdyDbaVwQ6jr8MkcTpHeNUdDJwVU+Ata6gygYNR+6NTseW7rRl4JweVury862BmN7cKmGECaC84nUp++dM01cDebd942q2QpvjWSUSz1gRXaJkW31oYbPuCzal1Im1cokul8KVX18tgDXgrGXyLkdmzZf3Vq+RYrPUiDaXLP/COISikTIdYbROzI0HAqTTxtnFr9m/82xTaEKM4VVxF6is8gc/MI6xG36wYNjuODE7EG5BRL6yNNmib6EBc93OfQfIklslnTpU90N+v80Z+DeXqRvt0uZbFP9SaaJPG4XS6nPmDmAwP9Nf9SP6WCATYIUkOzvxUlKx3cswIDAQAB";
            return Security.verifyPurchase(base64Key, signedData, signature);
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(billingClient!=null){
            billingClient.endConnection();
        }
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
        //if item subscribed
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            handlePurchases(purchases);
        }
        //if item already subscribed then check and reflect changes
        else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            Purchase.PurchasesResult queryAlreadyPurchasesResult = billingClient.queryPurchases(SUBS);
            List<Purchase> alreadyPurchases = queryAlreadyPurchasesResult.getPurchasesList();
            if(alreadyPurchases!=null){

                handlePurchases(alreadyPurchases);
                if (Constants.currentDate.compareTo(Constants.expireDate) > 0) {
                    Calendar c = Calendar.getInstance();
                    c.setTime(Constants.currentDate);
                    c.add(Calendar.DATE, 30);
                     Constants.expireDate = c.getTime();
                    Map<String, Object> map = new HashMap<>();
                    map.put("expireDate", c.getTime());
                   // FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).set(map, SetOptions.merge());
                }
            }
        }
        //if Purchase canceled
        else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            //Toast.makeText(getContext(),"Purchase Canceled",Toast.LENGTH_SHORT).show();
        }
        // Handle any other error msgs
        else {
            Toast.makeText(MainActivity.this,"Error "+billingResult.getDebugMessage(),Toast.LENGTH_SHORT).show();
        }
    }

    public boolean hasMembership(){
        return  getSubscribeValueFromPref() || (Constants.expireDate.compareTo(Constants.currentDate) > 0);
    }
}
