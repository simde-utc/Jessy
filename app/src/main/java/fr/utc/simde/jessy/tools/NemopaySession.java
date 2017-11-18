package fr.utc.simde.jessy.tools;

import android.app.Activity;
import android.util.Log;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.utc.simde.jessy.R;

/**
 * Created by Samy on 24/10/2017.
 */

public class NemopaySession {
    private static final String LOG_TAG = "_NemopaySession";
    private static final String url = "https://api.nemopay.net/services/";
    private static Map<String, String> allRights = new HashMap<String, String>();
    private String name;
    private String key;
    private String session;
    private String username;
    private int foundationId;
    private String foundationName;
    private int locationId;

    private HTTPRequest request;

    private String sessionNull;
    private String notLogged;
    private String noRight;
    private String noRights;
    private String noSuperRight;
    private String noSuperRights;
    private String allRightsNeeded;
    private String serviceText;
    private String notFound;
    private String badRequest;
    private String internalError;
    private String errorRequest;

    private Map<String, String> cookies = new HashMap<String, String>();
    private final Map<String, String> getArgs = new HashMap<String, String>() {{
        put("system_id", "payutc");
    }};

    public NemopaySession(final Activity activity) {
        this.name = "";
        this.key = "";
        this.session = "";
        this.username = "";
        this.foundationId = -1;
        this.foundationName = "";

        String[] keys = activity.getResources().getStringArray(R.array.rights_keys);
        String[] values = activity.getResources().getStringArray(R.array.rights_values);
        for (int i = 0; i < Math.min(keys.length, values.length); ++i)
            this.allRights.put(keys[i], values[i]);

        this.sessionNull = activity.getString(R.string.session_null);
        this.notLogged = activity.getString(R.string.no_longer_connected);
        this.allRightsNeeded = activity.getString(R.string.all_rights_needed);
        this.noRight = activity.getString(R.string.no_right);
        this.noRights = activity.getString(R.string.no_rights);
        this.noSuperRight = activity.getString(R.string.no_super_right);
        this.noSuperRights = activity.getString(R.string.no_super_rights);
        this.serviceText = activity.getString(R.string.service);
        this.notFound = activity.getString(R.string.not_found);
        this.badRequest = activity.getString(R.string.bad_request);
        this.internalError = activity.getString(R.string.internal_error);
        this.errorRequest = activity.getString(R.string.error_request);
    }

    public Boolean isConnected() { return !this.session.isEmpty() && !this.username.isEmpty(); }
    public Boolean isRegistered() { return !this.name.isEmpty() && !this.key.isEmpty() && !this.session.isEmpty(); }

    public void disconnect() {
        this.username = "";

        if (!isRegistered())
            this.session = "";
    }

    public void unregister() {
        this.name = "";
        this.key = "";

        disconnect();
    }

    public void setFoundation(final int foundationId, final String foundationName, final int locationId) {
        this.foundationId = foundationId;
        this.foundationName = foundationName;
        this.locationId = locationId;
    }

    public String getName() { return this.name; }
    public String getKey() { return this.key; }
    public String getUsername() { return username; }
    public HTTPRequest getRequest() { return this.request; }
    public int getFoundationId() { return foundationId; }
    public int getLocationId() { return locationId; }
    public String getFoundationName() { return foundationName; }

    public int cancelTransaction(final int foundationId, final int purchaseId) throws Exception {
        if (!isConnected())
            throw new Exception(this.notLogged);

        return request(
            "POSS3",
            "cancel",
            new HashMap<String, String>() {{
                put("fun_id", Integer.toString(foundationId));
                put("pur_id", Integer.toString(purchaseId));
            }},
            new String[]{
                "POSS3"
            }
        );
    }

    public int setTransaction(final String badgeId, final List<Integer> articleList) throws Exception {
        if (!isConnected())
            throw new Exception(this.notLogged);

        if (this.foundationId == -1)
            throw new Exception("No foundation set");

        String articles = "";
        for (Integer article : articleList)
            articles += Integer.toString(article) + " ";

        final String obj_ids = articles;
        return request(
            "POSS3",
            "transaction",
            new HashMap<String, String>() {{
                put("fun_id", Integer.toString(foundationId));
                put("badge_id", badgeId);
                put("obj_ids", obj_ids);

                if (locationId != -1)
                    put("location_id", Integer.toString(locationId));
            }},
            new String[]{
                "POSS3"
            }
        );
    }

    public int getAllMyRights() throws Exception {
        if (!isConnected())
            throw new Exception(this.notLogged);

        return request(
                "USERRIGHT",
                "getAllMyRights"
        );
    }

    public int getUser(final int id) throws Exception {
        if (!isConnected())
            throw new Exception(this.notLogged);

        return request(
            "GESUSERS",
            "getUser",
            new HashMap<String, String>() {{
                put("id", String.valueOf(id));
            }},
            new String[]{
                "GESUSERS"
            }
        );
    }

    public int foundUser(final String username) throws Exception {
        if (!isConnected())
            throw new Exception(this.notLogged);

        return request(
            "GESUSERS",
            "userAutocompleteUsername",
            new HashMap<String, String>() {{
                put("queryString", username);
            }},
            new String[]{
                "GESUSERS"
            }
        );
    }

    public int getBuyerInfoByLogin(final String login) throws Exception {
        if (!isConnected())
            throw new Exception(this.notLogged);

        return request(
            "POSS3",
            "getBuyerInfoByLogin",
            new HashMap<String, String>() {{
                put("login", login);
            }},
            new String[]{
                "POSS3"
            }
        );
    }

    public int getBuyerInfo(final String badgeId) throws Exception {
        if (!isConnected())
            throw new Exception(this.notLogged);

        return request(
            "POSS3",
            "getBuyerInfo",
            new HashMap<String, String>() {{
                if (foundationId != -1)
                    put("fun_id", Integer.toString(foundationId));
                put("badge_id", badgeId);
            }},
            new String[]{
                "POSS3"
            }
        );
    }

    public int getArticles() throws Exception {
        if (!isConnected())
            throw new Exception(this.notLogged);

        return request(
            "POSS3",
            "getProducts",
            new HashMap<String, String>() {{
                if (foundationId != -1)
                    put("fun_id", Integer.toString(foundationId));
            }},
            new String[]{
                "POSS3"
            }
        );
    }

    public int getLocations() throws Exception {
        if (!isConnected())
            throw new Exception(this.notLogged);

        return request(
            "POSS3",
            "getSalesLocations",
            new HashMap<String, String>() {{
                if (foundationId != -1)
                    put("fun_id", Integer.toString(foundationId));
            }},
            new String[]{
                "POSS3"
            }
        );
    }

    public int getKeyboards() throws Exception {
        if (!isConnected())
            throw new Exception(this.notLogged);

        return request(
            "POSS3",
            "getKeyboards",
            new HashMap<String, String>() {{
                if (foundationId != -1)
                    put("fun_id", Integer.toString(foundationId));
            }},
            new String[]{
                "POSS3"
            }
        );
    }

    public int getCategories() throws Exception {
        if (!isConnected())
            throw new Exception(this.notLogged);

        if (this.foundationId == -1)
            throw new Exception("No foundation set");

        return request(
            "POSS3",
            "getCategories",
            new HashMap<String, String>() {{
                put("fun_id", Integer.toString(foundationId));
            }},
            new String[]{
                "POSS3"
            }
        );
    }

    public int getFoundations() throws Exception {
        if (!isConnected())
            throw new Exception(this.notLogged);

        return request(
            "POSS3",
            "getFundations",
        new String[]{
                "POSS3"
        }
        );
    }

    public int getCASUrl() throws Exception {
        return request(
                "POSS3",
                "getCasUrl"
        );
    }

    public int registerApp(final String name, final String description, final String service) throws Exception {
        if (!isConnected())
            throw new Exception(this.notLogged);

        int reponseCode = request(
            "KEY",
            "registerApplication",
            new HashMap<String, String>() {{
                put("app_url", service);
                put("app_name", name);
                put("app_desc", description);
            }}
        );

        if (reponseCode != 200 || !this.request.isJSONResponse())
            throw new Exception("Not created");

        JsonNode response = this.request.getJSONResponse();
        if (response.has("app_key"))
            this.key = response.get("app_key").textValue();
        else
            throw new Exception("Unexpected JSON");

        return reponseCode;
    }

    public int loginApp(final String key, CASConnexion casConnexion) throws Exception {
        int reponseCode = loginApp(key);

        JsonNode response = this.request.getJSONResponse();
        if (response.has("config") && response.get("config").has("cas_url"))
            casConnexion.setUrl(response.get("config").get("cas_url").textValue());
        else
            throw new Exception("Unexpected JSON");

        return reponseCode;
    }
    public int loginApp(final String key) throws Exception {
        int reponseCode = request(
            "POSS3",
            "loginApp",
            new HashMap<String, String>() {{
                put("key", key);
            }}
        );

        JsonNode response;
        if (reponseCode == 200 && this.request.isJSONResponse())
            response = this.request.getJSONResponse();
        else
            throw new Exception(this.notLogged);

        if (response.has("sessionid") && response.has("name")) {
            if (response.get("sessionid").textValue() == null)
                throw new Exception(this.sessionNull);

            this.session = response.get("sessionid").textValue();
            this.name = response.get("name").textValue();
            this.key = key;
        }
        else
            throw new Exception("Unexpected JSON");

        return reponseCode;
    }

    public int loginBadge(final String badgeId, final String pin) throws Exception {
        if (!isRegistered())
            throw new Exception(this.notLogged);

        int reponseCode = request(
            "POSS3",
            "loginBadge2",
            new HashMap<String, String>() {{
                put("badge_id", badgeId);
                put("pin", pin);
            }},
            new String[]{
                "POSS3"
            }
        );

        JsonNode response;
        if (reponseCode == 200 && this.request.isJSONResponse())
            response = this.request.getJSONResponse();
        else
            throw new Exception(this.notLogged);

        if (response.has("sessionid") && response.has("username")) {
            if (response.get("sessionid").textValue() == null)
                throw new Exception(this.sessionNull);

            this.session = response.get("sessionid").textValue();
            this.username = response.get("username").textValue();
        }
        else
            throw new Exception("Unexpected JSON");

        return reponseCode;
    }

    public int loginCas(final String ticket, final String service) throws Exception {
        int reponseCode = request(
            "POSS3",
            "loginCas2",
            new HashMap<String, String>() {{
                put("ticket", ticket);
                put("service", service);
            }}
        );

        JsonNode response;
        if (reponseCode == 200 && this.request.isJSONResponse())
            response = this.request.getJSONResponse();
        else
            throw new Exception(this.notLogged);

        if (response.has("sessionid") && response.has("username")) {
            if (response.get("sessionid").textValue() == null)
                throw new Exception(this.sessionNull);

            this.session = response.get("sessionid").textValue();
            this.username = response.get("username").textValue();
        }
        else
            throw new Exception("Unexpected JSON");

        return reponseCode;
    }

    public String forbidden(final String[] rightsNeeded, final boolean needToBeSuper) {
        String result;
        if (rightsNeeded.length == 0)
            return this.allRightsNeeded;
        else if (rightsNeeded.length == 1)
            result = needToBeSuper ? this.noSuperRight : this.noRight;
        else
            result = needToBeSuper ? this.noSuperRights : this.noRights;

        for (String right : rightsNeeded) {
            if (allRights.containsKey(right))
                result += " " + allRights.get(right) + ",";
            else {
                result += " " + right + ",";
                Log.e(LOG_TAG, "\"" + right + "\" does not exist");
            }
        }

        return result.substring(0, result.length() - 1) + ".";
    }

    protected int request(final String method, final String service) throws Exception { return request(method, service, new HashMap<String, String>(), new String[]{}); }
    protected int request(final String method, final String service, final String[] rightsNeeded) throws Exception { return request(method, service, new HashMap<String, String>(), rightsNeeded); }
    protected int request(final String method, final String service, final Map<String, String> postArgs) throws Exception { return request(method, service, postArgs, new String[]{}); }
    protected int request(final String method, final String service, final Map<String, String> postArgs, final String[] rightsNeeded) throws Exception {
        this.request = new HTTPRequest(url + method + "/" + service);
        this.request.setGet(getArgs);
        this.request.setPost(postArgs);
        this.request.setCookies(this.cookies);

        int responseCode = this.request.post();
        this.cookies = request.getCookies();

        if (responseCode == 200)
            return 200;
        else if (responseCode == 403) {
            if (this.request.isJSONResponse()) {
                if (this.request.getJSONResponse().get("error").get("message").textValue().contains("must be logged"))
                    throw new Exception(this.notLogged);
            }

            throw new Exception(forbidden(rightsNeeded, false));
        }
        else if (responseCode == 404)
            throw new Exception(this.serviceText + " " + service + " " + this.notFound);
        else if (responseCode == 400) {
            if (this.request.isJSONResponse()) {
                if (this.request.getJSONResponse().has("error") && this.request.getJSONResponse().get("error").has("message"))
                    throw new Exception(this.request.getJSONResponse().get("error").get("message").textValue());
            }

            throw new Exception(this.serviceText + " " + service + " " + this.badRequest);
        }
        else if (responseCode == 500 || responseCode == 503) {
            throw new Exception(this.serviceText + " " + service + " " + this.internalError);
        }
        else
            throw new Exception(this.serviceText + " " + service + " " + this.errorRequest + " " + responseCode);
    }
}
