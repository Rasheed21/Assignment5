package edu.montclair.mobilecomputing.m_alrajab.week11firebasechat;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import edu.montclair.mobilecomputing.m_alrajab.week11firebasechat.model.ChatMessage;
import edu.montclair.mobilecomputing.m_alrajab.week11firebasechat.model.ChatMsgAdapter;

import static edu.montclair.mobilecomputing.m_alrajab.week11firebasechat.R.id.button;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener, TextToSpeech.OnUtteranceCompletedListener, MainActivity2 {

    FirebaseStorage storage;
    StorageReference storageRef;
    private String senderName = "User";
    private ListView listView;
    private Button sendBtn;
    private Button button;
    EditText editText;
    ArrayAdapter<String> adapter;
    ChatMsgAdapter mChatMsgAdapter;

    TextToSpeech textToSpeech;

    final int RC_SIGN_IN = 11, RC_PHOTO_PICKER = 12;

    DatabaseReference myRef;
    ChildEventListener mChildEventListener;
    FirebaseAuth mFirebaseAuth;
    FirebaseAuth.AuthStateListener mAuthStateListener;


    ArrayList<String> lstOfMsgs = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        List<ChatMessage> lstOfChatMsgs = new ArrayList<>();
        mFirebaseAuth = FirebaseAuth.getInstance();
        myRef = FirebaseDatabase.getInstance().getReference("msgs");
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference().child("images");
        ;


        sendBtn = (Button) findViewById(R.id.send_msg);
        editText = (EditText) findViewById(R.id.input_msg);
        lstOfMsgs.add("MSG1");
        lstOfMsgs.add("MSG2");

        listView = (ListView) findViewById(R.id.lstview);
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, lstOfMsgs);

        mChatMsgAdapter = new ChatMsgAdapter(this, R.layout.chat_msg_item, lstOfChatMsgs);
        //listView.setAdapter(adapter);
        listView.setAdapter(mChatMsgAdapter);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str = editText.getText().toString();
                //mChatMsgAdapter.add(new ChatMessage(str,senderName,getCurrentTime()));

                myRef.push().setValue(new ChatMessage(str, senderName, getCurrentTime(), null));

                editText.setText("");
            }
        });


        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                ChatMessage msg = dataSnapshot.getValue(ChatMessage.class);
                mChatMsgAdapter.add(msg);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };


        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null) {
                    Toast.makeText(MainActivity.this, "You are in", Toast.LENGTH_LONG).show();
                } else {

                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setProviders(Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                            new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                                    .build(),
                            RC_SIGN_IN);

                }
            }
        };

        myRef.addChildEventListener(mChildEventListener);
    }

    public String getCurrentTime() {
        SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm:ss");
        return format.format(Calendar.getInstance().getTime());
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
    }

    public void sendPic(View view) {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/jpeg");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == RC_PHOTO_PICKER) {
            Uri uri = data.getData();
            StorageReference lRefer = storageRef.child(uri.getLastPathSegment());
            lRefer.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                    ChatMessage cm = new ChatMessage(null, senderName, getCurrentTime(), taskSnapshot.getDownloadUrl().toString());
                    myRef.push().setValue(cm);
                }
            });
        }
    }

    @Override
    public void onCreate2(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

        while (cursor.moveToNext()) {
            //Displays Contact Name and ID
            String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

            // Use ID of contact as query parameter
            Cursor phoneCursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
            //Dumps output to logger
            Log.i("MY INFO", id + " = " + name);

            while (phoneCursor.moveToNext()) {
                String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                Log.i("MY INFO", phoneNumber);
            }
            Cursor emailCursor = resolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Email.CONTACT_ID + " =? ", new String[]{id}, null);

            while (emailCursor.moveToNext()) {
                String email = emailCursor.getString(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));

                Log.i("MY INFO", email);

                textToSpeech = new TextToSpeech(MainActivity.this, MainActivity.this);
                final Button SpkButton = (Button) findViewById(R.id.button);
                final TextView textView = (TextView) findViewById(R.id.textView);

                SpkButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!textToSpeech.isSpeaking()) {
                            HashMap<String, String> stringStringHashMap = new HashMap<String, String>();
                            stringStringHashMap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "Hello");
                            textToSpeech.speak(textView.getText().toString(), TextToSpeech.QUEUE_ADD, stringStringHashMap);
                            SpkButton.setVisibility(Button.GONE);
                        } else {
                            textToSpeech.stop();
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onInit(int i) {
        textToSpeech.setOnUtteranceCompletedListener(this);
    }

    @Override
    public void onUtteranceCompleted(String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "Utterance completed", Toast.LENGTH_LONG).show();
                Button button = (Button) findViewById(R.id.button);
                button.setVisibility(Button.VISIBLE);
            }
        });
    }

    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }
        super.onDestroy();
    }
}

