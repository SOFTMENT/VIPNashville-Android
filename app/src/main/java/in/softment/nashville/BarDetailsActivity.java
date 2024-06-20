package in.softment.nashville;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Locale;

import in.softment.nashville.Model.BarModel;

public class BarDetailsActivity extends AppCompatActivity {
    private GoogleMap mMap;
    private BarModel barModel;
    private TextView barName,description, address, rating;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bar_details);

        barModel = (BarModel) getIntent().getSerializableExtra("barmodel");
        if (barModel == null) {
            finish();
            return;
        }

        barName = findViewById(R.id.barName);
        description = findViewById(R.id.description);
        address = findViewById(R.id.address);
        rating = findViewById(R.id.rating);
        imageView = findViewById(R.id.barImage);

        Glide.with(this).load(barModel.image).placeholder(R.drawable.placeholder).into(imageView);
        barName.setText(barModel.name);
        description.setText(barModel.about);
        rating.setText(String.format("%.1f", barModel.rating));
        address.setText(barModel.address);

        //Back
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //OpenWebsite
        findViewById(R.id.openWebsiteBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BarDetailsActivity.this, OpenWebsiteActivity.class);
                intent.putExtra("name",barModel.name);
                intent.putExtra("url",barModel.webUrl);
                startActivity(intent);
            }
        });

        //More
        CardView more = findViewById(R.id.more);
        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Initializing the popup menu and giving the reference as current context
                PopupMenu popupMenu = new PopupMenu(BarDetailsActivity.this, more);

                // Inflating popup menu from popup_menu.xml file
                popupMenu.getMenuInflater().inflate(R.menu.more_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if (menuItem.getItemId() == R.id.map) {
                            String uri = String.format(Locale.ENGLISH, "geo:%f,%f", barModel.latitude, barModel.longitude);
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                            startActivity(intent);
                        }
//                        else if (menuItem.getItemId() == R.id.share) {
//                            try {
//                                Intent shareIntent = new Intent(Intent.ACTION_SEND);
//                                shareIntent.setType("text/plain");
//                                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "V.I.P Nashville");
//                                String shareMessage= "\nLet me recommend you this application\n\n";
//                                shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID +"\n\n";
//                                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
//                                startActivity(Intent.createChooser(shareIntent, "choose one"));
//                            } catch(Exception e) {
//                                //e.toString();
//                            }
//                        }

                        return true;
                    }
                });
                // Showing the popup menu
                popupMenu.show();
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {
                LatLng latLng = new LatLng(barModel.latitude, barModel.longitude);
                mMap = googleMap;
                mMap.addMarker(new
                        MarkerOptions().position(latLng).title("Raised by Wolves"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(13.0f));
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

            }
        });
    }
}
