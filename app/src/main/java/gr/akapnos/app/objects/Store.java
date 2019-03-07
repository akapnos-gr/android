package gr.akapnos.app.objects;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

import gr.akapnos.app.Helper;
import gr.akapnos.app.R;
import gr.akapnos.app.utilities.LocatorGoogle;

public class Store implements Serializable {
    public static final int typeNONE = 0;
    public static final int typeBAR = 1;
    public static final int typeCAFE = 2;
    public static final int typeRESTAURANT = 3;
    public static final int typeFAVORITE = 4;

    private String store_id;
    private String type;
    private String type_string;

    private String title;
    private String subtitle;
    private String desc;


    private String latitude;
    private String longitude;

    private String created;
    private String tripadvisor;

//private String address;

    private String website;
    private String phone;

    private String city;
    private String street;
    private String street_number;
    private String postcode;


    private ArrayList images;
    private ArrayList certificate_images;

    private String etableSlug;

    private boolean calculatedDistance = false;
    private float distanceToUser = -1;
    private double user_location_lat = 200;
    private double user_location_lon = 200;

    public Store(Map<String, String> map) {
        store_id = cleanString(map.get("ID"));
        title = cleanString(map.get("Title"));
        type_string = cleanString(map.get("Listing Κατηγοριών"));

        latitude = cleanString(map.get("geolocation_lat"));
        longitude = cleanString(map.get("geolocation_long"));

        tripadvisor = cleanString(map.get("link_tripadvisor"));

        phone = cleanString(map.get("_phone"));
        website = cleanString(map.get("_company_website"));

        street_number = cleanString(map.get("geolocation_street_number"));
        street = cleanString(map.get("geolocation_street"));
        city = cleanString(map.get("geolocation_city"));
        postcode = cleanString(map.get("geolocation_postcode"));

        etableSlug = cleanString(map.get("etableslug"));

        String image_featured = cleanString(map.get("Image Featured"));
        if(image_featured.length() > 0) {
            images = new ArrayList();
            images.add(image_featured);
        }
    }


    public String getEtableSlug() {
        return Helper.safeString(etableSlug);
    }

    public void setEtableSlug(String etableSlug) {
        this.etableSlug = etableSlug;
    }

    public String getStore_id() {
        return Helper.safeString(store_id);
    }

    public void setStore_id(String store_id) {
        this.store_id = store_id;
    }

    public String getType() {
        return Helper.safeString(type);
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType_string() {
        return Helper.safeString(type_string);
    }

    public void setType_string(String type_string) {
        this.type_string = type_string;
    }

    public String getTitle() {
        return Helper.safeString(title);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return Helper.safeString(subtitle);
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getDesc() {
        return Helper.safeString(desc);
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getLatitude() {
        return cleanString(Helper.safeString(latitude, "0"));
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return cleanString(Helper.safeString(longitude, "0"));
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getCreated() {
        return Helper.safeString(created);
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getTripadvisor() {
        return cleanString(Helper.safeString(tripadvisor));
    }

    public void setTripadvisor(String tripadvisor) {
        this.tripadvisor = tripadvisor;
    }

    public String getWebsite() {
        return Helper.safeString(website);
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getPhone() {
        return cleanString(Helper.safeString(phone));
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCity() {
        return cleanString(Helper.safeString(city));
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return cleanString(Helper.safeString(street));
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getStreet_number() {
        return cleanString(Helper.safeString(street_number));
    }

    public void setStreet_number(String street_number) {
        this.street_number = street_number;
    }

    public String getPostcode() {
        return cleanString(Helper.safeString(postcode));
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public ArrayList getImages() {
        return images;
    }


    public void setImages(ArrayList images) {
        this.images = images;
    }

    public ArrayList getCertificate_images() {
        return certificate_images;
    }

    public void setCertificate_images(ArrayList certificate_images) {
        this.certificate_images = certificate_images;
    }

    public boolean has_location() {
        return getLatitude().length() > 0 && getLongitude().length() > 0;
    }

    public float distance_to_user() {
        Location location = LocatorGoogle.getInstance().getUserLocation();
        if (location != null &&
                (!calculatedDistance ||
                        user_location_lat != location.getLatitude() ||
                        user_location_lon != location.getLongitude()
                )
        ) {

            user_location_lat = location.getLatitude();
            user_location_lon = location.getLongitude();

            Location loc1 = new Location("");
            loc1.setLatitude(lat());
            loc1.setLongitude(lon());

            distanceToUser = loc1.distanceTo(location);
            calculatedDistance = true;
        }
        return distanceToUser;
    }



    private double lat() {
        try { return Double.parseDouble(getLatitude()); } catch (Exception ignored){}
        return 0;
    }

    private double lon() {
        try { return Double.parseDouble(getLongitude()); } catch (Exception ignored){}
        return 0;
    }


    public LatLng getLatLng() {
        try {
            return new LatLng(lat(), lon());
        } catch (Exception ignored) {}
        return new LatLng(0, 0);
    }

    public String distanceToUserDesc() {
        if(distanceToUser == -1) {
            return "";
        }
        if(distanceToUser < 1000) {
            return String.format(Locale.getDefault(),"%d %s",(int)distanceToUser, Helper.appCtx().getResources().getString(R.string.DISTANCE_METERS));
        }
        return String.format(Locale.getDefault(), "%.1f %s", distanceToUser/1000, Helper.appCtx().getResources().getString(R.string.DISTANCE_KILOMETERS));
    }

    private String _address;
    public String getAddress() {
    /*
     "geolocation_city" = Kifisia;
     "geolocation_postcode" = "145 64";
     "geolocation_street" = Elaion;
     "geolocation_street_number" = 54;
     */
        if(_address == null) {
            String tmp = "";
            tmp += getStreet();
            if(tmp.length() > 0) { tmp += " "; } tmp += getStreet_number();
            if(tmp.length() > 0) { tmp += " "; } tmp += getCity();
            if(tmp.length() > 0) { tmp += " "; } tmp += getPostcode();
            _address = tmp;
        }
        return _address;
    }
    public static String cleanupString(String str) {
        if(str == null) { return ""; }
        String[] arr = str.split("\\|");
        if(arr.length > 0) { str = arr[0]; }
        return str;
    }
    public String cleanString(String str) {
        str = Store.cleanupString(str);
        return str;
    }



    public int getStoreType() {
        if(getType().length() > 0) {
            return Integer.parseInt(type);
        }
        else if(getType_string().length() > 0) {
            if(getType_string().contains("Μπαρ")) {
                return typeBAR;
            }
            else if(getType_string().contains("Καφέ")) {
                return typeCAFE;
            }
            else if(getType_string().contains("Εστιατόρια")) {
                return typeRESTAURANT;
            }
        }
        return typeNONE;
    }
    public int storeTypeColor() {
        return Store.colorForStoreType(getStoreType());
    }
    public String storeTypeTitle() {
        return Store.titleForStoreType(getStoreType());
    }
    public static int colorForStoreType(int store_type) {
        return Helper.appCtx().getResources().getColor(

//    store_type == typeFAVORITE ? [UIColor redColor] :
                store_type == typeNONE ? R.color.keyColor :
                        store_type == typeBAR ? R.color.keyColor :
                                store_type == typeCAFE ? R.color.cafe_color :
                                        store_type == typeRESTAURANT ? R.color.restaurants_color :
                                                android.R.color.white);
    }
    public static String titleForStoreType(int store_type) {
        return
                store_type == typeFAVORITE ? "Αγαπημένα" :
                        store_type == typeNONE ? "LBL_ALL_STORES" :
                                store_type == typeBAR ? "Μπαρ" : //LocalizedString(@"BAR") :
                                        store_type == typeCAFE ? "Καφέ" : //LocalizedString(@"CAFE") :
                                                store_type == typeRESTAURANT ? "Εστιατόρια" : //LocalizedString(@"RESTAURANT") :
                                                        "";
    }

    public String getFirstImage() {
        if(getImages() != null && getImages().size() > 0) {
            return (String)getImages().get(0);
        }
        return "";
    }

    @Override
    public String toString() {
        return "Store{" +
                "store_id='" + store_id + '\'' +
                ", type_string='" + type_string + '\'' +
                ", title='" + title + '\'' +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                '}';
    }
}
