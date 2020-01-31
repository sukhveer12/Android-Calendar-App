package com.ics4u.nac;

import android.util.Log;

import com.nac.entity.AccountList_RTO;
import com.nac.entity.Account_RTO;
import com.nac.entity.ActivityList_RTO;
import com.nac.entity.Activity_RTO;
import com.nac.entity.CompoundExpr_RTO;
import com.nac.entity.ParticipationList_RTO;
import com.nac.entity.Participation_RTO;

import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.client.authentication.RequestAuthenticationException;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * This is the Network_IO_Controller class for the NAC app. It falls under the Controller
 * category in the Model-View-Controller paradigm. It is used to execute Network I/O requests
 * to the server using the RESTful architecture.
 *
 * @author Sukhveer Sahota
 * @version 1.0
 * @since June 3rd, 2018
 */
public class Network_IO_Controller {

    // Jersey client object used for the HTTP requests.
    private Client client;

    private String baseURLString;

    // Used for debugging
    private static final String TAG = "Network_IO_Controller";

    private static Network_IO_Controller instance = new Network_IO_Controller();

    /**
     * Default constructor
     */
    private Network_IO_Controller() {
        baseURLString = "";
        initClient("", "");
    }

    /**
     * Getter method for the Singleton Instance
     * @return Network_IO_Controller
     */
    static Network_IO_Controller getInstance() {
        return instance;
    }

    void setBaseURLString(String baseURLString) {
        this.baseURLString = baseURLString;
    }

    /**
     * Initialize the credentials of the HTTP client
     * @param username String: username
     * @param password String: password
     */
    void initClient(String username, String password) {
        client = ClientBuilder.newClient();
        HttpAuthenticationFeature basicHTTPAuth = HttpAuthenticationFeature.basic(username, password);
        client.register(basicHTTPAuth);
        client.property(ClientProperties.CONNECT_TIMEOUT, 1000);
        client.property(ClientProperties.READ_TIMEOUT, 1000);
    }

    /**
     * Get the account for which the userID matches the specified userID
     * @param userID The userID
     * @return Account_RTO
     * @throws ProcessingException Thrown by library
     */
    Account_RTO getAccountFromUserID(String userID) throws ProcessingException {
        String urlString = baseURLString;
        urlString = urlString.concat("/account/" + userID);
        // set up an HTTP GET request which uses JSON
        Invocation.Builder builder = client.target(urlString).request(MediaType.APPLICATION_JSON);
        return builder.get(Account_RTO.class);
    }

    /**
     * Get all the accounts from the server
     * @return AccountList_RTO
     * @throws ProcessingException Thrown by library
     */
    AccountList_RTO getAllAccounts() throws ProcessingException {
        String urlString = baseURLString;
        urlString = urlString.concat("/account/all");
        // set up an HTTP GET request which uses JSON
        Invocation.Builder builder = client.target(urlString).request(MediaType.APPLICATION_JSON);
        return builder.get(AccountList_RTO.class);
    }

    /**
     * Add the passed in account onto the server
     * @param account The account to put on the server
     * @throws WebApplicationException Thrown by library
     * @throws ProcessingException Thrown by library
     */
    void putAccountOnServer(Account_RTO account) throws WebApplicationException, ProcessingException {
        String urlString = baseURLString;
        urlString = urlString.concat("/account/" + Integer.toString(account.getEid()));
        // set up an HTTP GET request which uses JSON
        Invocation.Builder builder = client.target(urlString).request(MediaType.APPLICATION_JSON);
        Response response = builder.put(Entity.entity(account, MediaType.APPLICATION_JSON));
        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            throw new WebApplicationException("HTTP: " + response.getStatus() + " " + response.getStatusInfo().getReasonPhrase());
        }
    }

    /**
     * Find all accounts that match the passed-in search parameters
     * @param searchExpression Search Params
     * @return Account_ListRTO
     * @throws WebApplicationException Thrown by library
     * @throws ProcessingException Thrown by library
     */
    AccountList_RTO findAccounts(CompoundExpr_RTO searchExpression) throws WebApplicationException, ProcessingException {
        String urlString = baseURLString;
        urlString = urlString.concat("/account/find");
        // set up an HTTP POST request which uses JSON
        Invocation.Builder builder = client.target(urlString).request(MediaType.APPLICATION_JSON);
        Response response = builder.post(Entity.entity(searchExpression, MediaType.APPLICATION_JSON));
        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            throw new WebApplicationException("HTTP: " + response.getStatus() + " " + response.getStatusInfo().getReasonPhrase());
        }
        AccountList_RTO fetchedList = response.readEntity(AccountList_RTO.class);
        if (fetchedList == null) {
            fetchedList = new AccountList_RTO();
        }
        return fetchedList;
    }

    /**
     * Get the activity for which the EID matches the specified EID
     * @param EID The activity EID
     * @return Activity_RTO
     * @throws ProcessingException Thrown by library
     */
    Activity_RTO getActivityFromEID(int EID) throws ProcessingException {
        String urlString = baseURLString;
        urlString = urlString.concat("/activity/" + Integer.toString(EID));
        // set up an HTTP GET request which uses JSON
        Invocation.Builder builder = client.target(urlString).request(MediaType.APPLICATION_JSON);
        return builder.get(Activity_RTO.class);
    }

    /**
     * Get all the accounts from the server
     * @return ActivityList_RTO
     * @throws ProcessingException Thrown by library
     */
    ActivityList_RTO getAllActivities() throws ProcessingException {
        String urlString = baseURLString;
        urlString = urlString.concat("/activity/all");
        // set up an HTTP GET request which uses JSON
        Invocation.Builder builder = client.target(urlString).request(MediaType.APPLICATION_JSON);
        return builder.get(ActivityList_RTO.class);
    }

    /**
     * Add the passed in activity onto the server
     * @param activity The activity to put on the server
     * @throws WebApplicationException Thrown by library
     * @throws ProcessingException Thrown by library
     */
    void putActivityOnServer(Activity_RTO activity) throws WebApplicationException, ProcessingException {
        String urlString = baseURLString;
        urlString = urlString.concat("/activity/" + Integer.toString(activity.getEid()));
        // set up an HTTP GET request which uses JSON
        Invocation.Builder builder = client.target(urlString).request(MediaType.APPLICATION_JSON);
        Response response = builder.put(Entity.entity(activity, MediaType.APPLICATION_JSON));
        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            throw new WebApplicationException("HTTP: " + response.getStatus() + " " + response.getStatusInfo().getReasonPhrase());
        }
    }

    /**
     * Find all accounts that match the passed-in search parameters
     * @param searchExpression Search Params
     * @return Account_ListRTO
     * @throws WebApplicationException Thrown by library
     * @throws ProcessingException Thrown by library
     */
    ActivityList_RTO findActivities(CompoundExpr_RTO searchExpression) throws WebApplicationException, ProcessingException {
        String urlString = baseURLString;
        urlString = urlString.concat("/activity/find");
        // set up an HTTP POST request which uses JSON
        Invocation.Builder builder = client.target(urlString).request(MediaType.APPLICATION_JSON);
        Response response = builder.post(Entity.entity(searchExpression, MediaType.APPLICATION_JSON));
        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            throw new WebApplicationException("HTTP: " + response.getStatus() + " " + response.getStatusInfo().getReasonPhrase());
        }
        ActivityList_RTO fetchedList = response.readEntity(ActivityList_RTO.class);
        if (fetchedList == null) {
            fetchedList = new ActivityList_RTO();
        }
        return fetchedList;
    }

    /**
     * Get the participation entity for which the EID matches the specified EID
     * @param ID The activity EID
     * @return Participation_RTO
     * @throws ProcessingException Thrown by library
     */
    Participation_RTO getParticipationFromID(int ID) throws ProcessingException {
        String urlString = baseURLString;
        urlString = urlString.concat("/participation/" + Integer.toString(ID));
        // set up an HTTP GET request which uses JSON
        Invocation.Builder builder = client.target(urlString).request(MediaType.APPLICATION_JSON);
        return builder.get(Participation_RTO.class);
    }

    /**
     * Get all the participations from the server
     * @return ParticipationList_RTO
     * @throws ProcessingException Thrown by library
     */
    ParticipationList_RTO getAllParticipationEntities() throws ProcessingException {
        String urlString = baseURLString;
        urlString = urlString.concat("/participation/all");
        // set up an HTTP GET request which uses JSON
        Invocation.Builder builder = client.target(urlString).request(MediaType.APPLICATION_JSON);
        return builder.get(ParticipationList_RTO.class);
    }

    /**
     * Add the passed in account onto the server
     * @param participation The account to put on the server
     * @throws WebApplicationException Thrown by library
     * @throws ProcessingException Thrown by library
     */
    void putParticipationEntityOnServer(Participation_RTO participation) throws WebApplicationException, ProcessingException {
        String urlString = baseURLString;
        urlString = urlString.concat("/participation/" + Integer.toString(participation.getEid()));
        // set up an HTTP GET request which uses JSON
        Invocation.Builder builder = client.target(urlString).request(MediaType.APPLICATION_JSON);
        Response response = builder.put(Entity.entity(participation, MediaType.APPLICATION_JSON));
        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            throw new WebApplicationException("HTTP: " + response.getStatus() + " " + response.getStatusInfo().getReasonPhrase());
        }
    }

    /**
     * Find all accounts that match the passed-in search parameters
     * @param searchExpression Search Params
     * @return Account_ListRTO
     * @throws WebApplicationException Thrown by library
     * @throws ProcessingException Thrown by library
     */
    ParticipationList_RTO findParticipationEntities(CompoundExpr_RTO searchExpression) throws WebApplicationException, ProcessingException {
        String urlString = baseURLString;
        urlString = urlString.concat("/participation/find");
        // set up an HTTP POST request which uses JSON
        Invocation.Builder builder = client.target(urlString).request(MediaType.APPLICATION_JSON);
        Response response = builder.post(Entity.entity(searchExpression, MediaType.APPLICATION_JSON));
        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            throw new WebApplicationException("HTTP: " + response.getStatus() + " " + response.getStatusInfo().getReasonPhrase());
        }
        ParticipationList_RTO fetchedList = response.readEntity(ParticipationList_RTO.class);
        if (fetchedList == null) {
            fetchedList = new ParticipationList_RTO();
        }
        return fetchedList;
    }

}
