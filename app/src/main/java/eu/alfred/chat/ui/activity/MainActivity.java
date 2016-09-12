package eu.alfred.chat.ui.activity;

import android.app.ActionBar;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.ButterKnife;
import butterknife.InjectView;
import eu.alfred.chat.R;
import eu.alfred.chat.model.Contact;
import eu.alfred.chat.util.Constants;
import eu.alfred.chat.util.DebugUtils;
import eu.alfred.chat.util.PhoneCommunicationUtils;
import eu.alfred.chat.util.Prefs;
import eu.alfred.chat.util.StringUtils;
import eu.alfred.ui.BackToPAButton;
import eu.alfred.ui.CircleButton;

public class MainActivity extends BaseActivity {

    private static final String TAG = "ChatMainActivity";
    private final Map<String, Contact> identifiedContacts = new HashMap<>();

    @InjectView(R.id.imageButtonLocalSettings)
    ImageButton imageButtonLocalSettings;
    @InjectView(R.id.editTextSetAddress)
    EditText editTextSetAddress;
    @InjectView(R.id.buttonSetAddress)
    ImageButton buttonSetAddress;
    @InjectView(R.id.buttonCallCaregiver)
    Button buttonCallCaregiver;
    private int recogMessage = 0;
    private long contactsLastUpdated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        init();
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            cade.StopListening(this.getPackageName());
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        editTextSetAddress.setText(Prefs.getString(Constants.KEY_CADE_URL, Constants.LOCAL_CADE_URL));
        updateContacts();
    }

    private void updateContacts() {
        if (System.currentTimeMillis() - contactsLastUpdated > 60 * 1000) {
            updateContactCache();
        }
        else {
            Log.d(TAG, "updateContacts: Using cached contacts");
        }
    }

    private void updateContactCache() {
        contactsLastUpdated = System.currentTimeMillis();
        identifiedContacts.clear();
        List<Contact> cntacts = retrievePhoneContacts();
        if (cntacts != null) {
            for (Contact contact : cntacts) {
                identifiedContacts.put(contact.getIdentifier(), contact);
            }
        }
        Log.d(TAG, "Updated contacts: " + identifiedContacts.keySet());
    }

    private void init() {
        initViews();
        setListeners();
    }

    private void initViews() {
        initActionBar();
        editTextSetAddress.setText(Prefs.getString(Constants.KEY_CADE_URL, Constants.LOCAL_CADE_URL));
    }

    private void initActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    private void setListeners() {
        circleButton = (CircleButton) findViewById(R.id.voiceControlBtn);
        backToPAButton = (BackToPAButton) findViewById(R.id.backControlBtn);

        circleButton.setOnTouchListener(new MicrophoneTouchListener());
        backToPAButton.setOnTouchListener(new BackTouchListener());
        imageButtonLocalSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editTextSetAddress.getVisibility() == View.GONE) {
                    editTextSetAddress.setVisibility(View.VISIBLE);
                    buttonSetAddress.setVisibility(View.VISIBLE);
                } else {
                    editTextSetAddress.setVisibility(View.GONE);
                    buttonSetAddress.setVisibility(View.GONE);
                }
            }
        });
        editTextSetAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Prefs.setString(Constants.KEY_CADE_URL, editTextSetAddress.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        buttonSetAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cade.SetCadeBackendUrl(editTextSetAddress.getText().toString());
            }
        });
        buttonCallCaregiver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DebugUtils.d(MainActivity.this, TAG, getString(R.string.calling_caregiver), true, true);
                String caregiverPhone = getCaregivePhoneNumber();
                PhoneCommunicationUtils.call(MainActivity.this, caregiverPhone);
            }
        });
    }

    @Override
    public void performAction(String command, Map<String, String> map) {
        Log.d(TAG, "performAction command: #" + command + "#, map: #" + StringUtils.getReadableString(map) + "#");
        if (TextUtils.equals(command, Constants.CADE_ACTION_REQUEST_MESSAGE_RECOG)) {
            RequestMessageRecog();
            cade.sendActionResult(true);
            return;
        }else if (TextUtils.equals(command, Constants.CADE_ACTION_REQUEST_MESSAGE_RECOG)) {
            OnRequestMessageRecog();
            cade.sendActionResult(true);
            return;
        } else if (TextUtils.equals(command, Constants.CADE_ACTION_CALL)) {
            DebugUtils.d(this, TAG, "" + Constants.CADE_ACTION_CALL, true, true);

            String contactName = map.get(Constants.CADE_WH_QUERY_SELECTED_CONTACT);
            findAndCall(contactName);

            cade.sendActionResult(true);
            return;
        } else if (TextUtils.equals(command, Constants.CADE_ACTION_SEND_MESSAGE)) {
            DebugUtils.d(this, TAG, "" + Constants.CADE_ACTION_SEND_MESSAGE, true, true);

            String contactName = map.get(Constants.CADE_WH_QUERY_SELECTED_CONTACT);
            String message = map.get(Constants.CADE_WH_QUERY_SELECTED_MESSAGE);
            findAndSendMessage(contactName, message);

            cade.sendActionResult(true);
            return;
        } else if (TextUtils.equals(command, Constants.CADE_ACTION_VIDEOCALL)) {
            DebugUtils.d(this, TAG, "" + Constants.CADE_ACTION_VIDEOCALL, true, true);

            String contactName = map.get(Constants.CADE_WH_QUERY_SELECTED_CONTACT);
            findAndVideoCall(contactName);

            cade.sendActionResult(true);
            return;
        }
        cade.sendActionResult(true);
    }

    @Override
    public void performWhQuery(String command, Map<String, String> map) {
    }

    @Override
    public void performValidity(String command, Map<String, String> map) {
    }

    @Override
    public void performEntityRecognizer(String command, Map<String, String> map) {
        Log.d(TAG, "performEntityRecognizer 0 command: #" + command + "#, map: #" + StringUtils.getReadableString(map) + "#" + recogMessage);
        String searchString = map.get("search_string");
        if (searchString == null) {
            Log.e(TAG, "performEntityRecognizer got no search string");
            return;
        }
        ArrayList<Map<String, String>> entities = new ArrayList<>();
        if (TextUtils.equals(command, Constants.CADE_RECOGNIZER_RECOGNIZER)) {
            if (recogMessage > 0) {
                Map<String, String> temp = new HashMap<>();
                temp.put("sort", "message");
                temp.put("grammar_entry", searchString);
                OnRequestMessageRecog();
                entities.add(temp);
                Log.d(TAG, "performEntityRecognizer recognized message: " + searchString);
            } else {
                updateContacts();
                Collection<Contact> contacts = identifiedContacts.values();

                for (Contact contact : contacts) {
                    List<Map<String, String>> contactEntities = findEntitiesOfContact(searchString, contact);
                    entities.addAll(contactEntities);
                }
            }
        }
        Log.d(TAG, "performEntityRecognizer return list:" + StringUtils.getReadableString(entities) + "#");
        cade.sendEntityRecognizerResult(entities);
    }

    private List<Map<String, String>> findEntitiesOfContact(String searchString, Contact contact) {
        List<Map<String, String>> entities = new ArrayList<>();
        String firstName = contact.getName();
        String lastName = contact.getLastName();
        String fullName = firstName + " " + lastName;
        String id = contact.getIdentifier();

        recognizeAndPotentiallyAddEntityOfName(entities, searchString, id, fullName);
        recognizeAndPotentiallyAddEntityOfName(entities, searchString, id, firstName);
        recognizeAndPotentiallyAddEntityOfName(entities, searchString, id, lastName);

        return entities;
    }

    private void recognizeAndPotentiallyAddEntityOfName(
            List<Map<String, String>> entities, String searchString, String id, String name) {
        if (isNameRecognized(searchString, name)) {
            Log.d(TAG, "recognizeAndPotentiallyAddEntityOfName: recognized '" + name + "' for '" + id + "' in '" + searchString + "'");
            Map<String, String> entity = createContactEntity(id, name);
            entities.add(entity);
        }
    }

    public static boolean isNameRecognized(String searchString, String name) {
        boolean isNameValid = isContactNameValid(name);
        boolean isNameMatched = isContactNameMatched(searchString.toLowerCase(), name.toLowerCase());
        return isNameValid && isNameMatched;
    }

    private static boolean isContactNameValid(String name) {
        Pattern pattern = Pattern.compile(".*[a-z]{2,}.*");
        Matcher matcher = pattern.matcher(name);
        return matcher.matches();
    }

    public static boolean isContactNameMatched(String searchString, String name) {
        String pattern = ".*\\b" + name + "\\b.*";
        boolean matches = Pattern.matches(pattern, searchString);
        return matches;
    }

    private Map<String, String> createContactEntity(String id, String grammarEntry) {
        Map<String, String> entity = new HashMap<>();
        entity.put("name", id);
        entity.put("sort", "contact");
        entity.put("grammar_entry", grammarEntry);
        return entity;
    }

    private void findAndCall(String contactName) {
        if (!TextUtils.isEmpty(contactName)) {
            Contact contact = identifiedContacts.get(contactName);
            String phoneNumber = contact.getPhoneNumber();
            if (!TextUtils.isEmpty(phoneNumber)) {
                PhoneCommunicationUtils.call(this, phoneNumber);
            }
            else {
                Log.e(TAG, "findAndCall: Expected phoneNumber for contact '" + contactName + "' but it was empty: " + phoneNumber);
            }
        }
        else {
            Log.e(TAG, "findAndCall: Expected name but it was empty: " + contactName);
        }
    }

    private void findAndSendMessage(String contactName, String message) {
        if (!TextUtils.isEmpty(contactName) && !TextUtils.isEmpty(message)) {
            Contact contact = identifiedContacts.get(contactName);
            String phoneNumber = contact.getPhoneNumber();

            if (!TextUtils.isEmpty(phoneNumber)) {
                PhoneCommunicationUtils.sendMessage(this, phoneNumber, message);
            }
            else {
                Log.e(TAG, "findAndSendMessage: Expected phoneNumber for contact '" + contactName + "' but it was empty: " + phoneNumber);
            }
        }
        else {
            Log.e(TAG, "findAndSendMessage: Expected name and message but one was empty: name=" + contactName + ", message=" + message);
        }
    }

    private void findAndVideoCall(String contactName) {
        if (!TextUtils.isEmpty(contactName)) {
            Contact contact = identifiedContacts.get(contactName);
            String phoneNumber = contact.getPhoneNumber();
            if (!TextUtils.isEmpty(phoneNumber)) {
                PhoneCommunicationUtils.videoCall(this, contactName, phoneNumber);
            }
            else {
                Log.e(TAG, "findAndVideoCall: Expected phoneNumber for contact '" + contactName + "' but it was empty: " + phoneNumber);
            }
        }
        else {
            Log.e(TAG, "findAndVideoCall: Expected name but it was empty: " + contactName);
        }
    }

    public void RequestMessageRecog() {
        this.recogMessage = 1;
    }

    public void OnRequestMessageRecog() {
        this.recogMessage = 0;
    }

    private String getCaregivePhoneNumber() {
        String phoneNumber = "+34900000000";
        return phoneNumber;
    }

    public List<Contact> retrievePhoneContacts() {
        List<Contact> alContacts = new ArrayList<Contact>();
        Cursor cursor = null;
        try {
            ContentResolver cr = getContentResolver(); //Activity/Application android.content.Context
            cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                        Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
                        Contact contact;
                        while (pCur.moveToNext()) {
                            String hasNumber = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER));
                            if (hasNumber != null && hasNumber.equals("1")) {
                                String name = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                                String lastName = "";
                                if (name.contains(" ")) {
                                    String[] names = name.split(" ");
                                    if (names.length > 1) {
                                        name = names[0];
                                        for (int i = 1; i < names.length; i++) {
                                            if (i == 1) {
                                                lastName = lastName + "" + names[i];
                                            } else {
                                                lastName = lastName + " " + names[i];
                                            }
                                        }
                                    } else {
                                        name = name.trim();
                                    }
                                }
                                String contactNumber = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                contactNumber = contactNumber.replaceAll("\\s", "");
                                contact = new Contact(name.toLowerCase(), lastName.toLowerCase(), contactNumber);
                                alContacts.add(contact);
                            }
                            break;
                        }
                        pCur.close();
                    }

                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                cursor.close();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        for (Contact alContact : alContacts) {
            if (alContact.getName().equals("john")) {
                Log.d(TAG, "alContact: " + alContact);
            }
        }
        return alContacts;
    }
}
