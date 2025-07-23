package com.app.smart

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.smart.adapters.EmergencyContactsAdapter
import com.app.smart.data.EmergencyContact
import com.app.smart.databinding.ActivityEmergencyContactsBinding
import com.app.smart.databinding.DialogAddContactBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class EmergencyContactsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEmergencyContactsBinding
    private lateinit var adapter: EmergencyContactsAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var addContactDialog: AlertDialog? = null
    private var dialogBinding: DialogAddContactBinding? = null

    private val pickContact = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { contactUri ->
                retrieveContactInfo(contactUri)
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchContactPicker()
        } else {
            Toast.makeText(this, "Permission required to access contacts", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmergencyContactsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.emergency_contacts)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupRecyclerView()
        setupFab()
        loadContacts()
    }

    private fun setupRecyclerView() {
        adapter = EmergencyContactsAdapter(
            onDelete = { contact -> showDeleteDialog(contact) }
        )
        binding.rvContacts.apply {
            layoutManager = LinearLayoutManager(this@EmergencyContactsActivity)
            adapter = this@EmergencyContactsActivity.adapter
        }
    }

    private fun setupFab() {
        binding.fabAddContact.setOnClickListener {
            showAddContactDialog()
        }
    }

    private fun showAddContactDialog() {
        dialogBinding = DialogAddContactBinding.inflate(LayoutInflater.from(this))
        dialogBinding?.let { dialog ->
            dialog.btnPickContact.setOnClickListener {
                checkContactPermission()
            }

            addContactDialog = AlertDialog.Builder(this)
                .setTitle(R.string.add_contact)
                .setView(dialog.root)
                .setPositiveButton("Add") { _, _ ->
                    val name = dialog.etName.text.toString().trim()
                    val phone = dialog.etPhone.text.toString().trim()
                    val relationship = dialog.etRelationship.text.toString().trim()
                    
                    if (validateContactInput(name, phone)) {
                        val contact = EmergencyContact(
                            id = UUID.randomUUID().toString(),
                            name = name,
                            phone = phone,
                            relationship = relationship
                        )
                        addContact(contact)
                    }
                }
                .setNegativeButton("Cancel", null)
                .create()

            addContactDialog?.show()
        }
    }

    private fun checkContactPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED -> {
                launchContactPicker()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS) -> {
                showPermissionRationaleDialog()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            }
        }
    }

    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("We need access to your contacts to help you select emergency contacts.")
            .setPositiveButton("Grant") { _, _ ->
                requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun launchContactPicker() {
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        pickContact.launch(intent)
    }

    private fun retrieveContactInfo(contactUri: Uri) {
        val projection = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.Contacts.HAS_PHONE_NUMBER
        )

        contentResolver.query(contactUri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID)
                val nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                val hasPhoneIndex = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)

                val contactId = cursor.getString(idIndex)
                val contactName = cursor.getString(nameIndex)
                val hasPhone = cursor.getInt(hasPhoneIndex) > 0

                if (hasPhone) {
                    getContactPhone(contactId)?.let { phoneNumber ->
                        dialogBinding?.apply {
                            etName.setText(contactName)
                            etPhone.setText(phoneNumber)
                        }
                    }
                }
            }
        }
    }

    private fun getContactPhone(contactId: String): String? {
        val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
        val selection = "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?"
        val selectionArgs = arrayOf(contactId)

        contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                return cursor.getString(phoneIndex)
            }
        }
        return null
    }

    private fun validateContactInput(name: String, phone: String): Boolean {
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show()
            return false
        }
        if (phone.isEmpty()) {
            Toast.makeText(this, "Please enter a phone number", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun loadContacts() {
        auth.currentUser?.let { user ->
            db.collection("users")
                .document(user.uid)
                .collection("emergency_contacts")
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Toast.makeText(this, "Error loading contacts", Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }

                    snapshot?.let {
                        val contacts = it.documents.mapNotNull { doc ->
                            doc.toObject(EmergencyContact::class.java)
                        }
                        adapter.submitList(contacts)
                        binding.tvNoContacts.visibility = if (contacts.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
        }
    }

    private fun addContact(contact: EmergencyContact) {
        auth.currentUser?.let { user ->
            db.collection("users")
                .document(user.uid)
                .collection("emergency_contacts")
                .document(contact.id)
                .set(contact)
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to add contact", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun showDeleteDialog(contact: EmergencyContact) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_contact_title)
            .setMessage(R.string.delete_contact_message)
            .setPositiveButton("Delete") { _, _ ->
                deleteContact(contact)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteContact(contact: EmergencyContact) {
        auth.currentUser?.let { user ->
            db.collection("users")
                .document(user.uid)
                .collection("emergency_contacts")
                .document(contact.id)
                .delete()
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to delete contact", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        addContactDialog?.dismiss()
        addContactDialog = null
        dialogBinding = null
    }
} 