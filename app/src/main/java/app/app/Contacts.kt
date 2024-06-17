package app.app

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.NavHostFragment

class Contacts : Fragment() {

    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = DBHelper(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_contacts, container, false)

        view.findViewById<Button>(R.id.go_to_main_page_button).setOnClickListener {
            NavHostFragment.findNavController(this@Contacts).popBackStack(R.id.mainpage, false)
        }

        view.findViewById<Button>(R.id.add_contact_button).setOnClickListener {
            showEditContactDialog(null)
        }

        loadContacts(view)

        return view
    }

    private fun loadContacts(view: View) {
        val contactsListLayout = view.findViewById<LinearLayout>(R.id.contactsListLayout)
        contactsListLayout.removeAllViews()

        val contacts = dbHelper.getAllContacts()
        for (contact in contacts) {
            val contactView = layoutInflater.inflate(R.layout.contact_item, contactsListLayout, false)

            contactView.findViewById<TextView>(R.id.contact_name).text = "${contact.name} ${contact.surname}"
            contactView.findViewById<TextView>(R.id.contact_number).text = contact.phoneNumber

            contactView.findViewById<Button>(R.id.edit_contact_button).setOnClickListener {
                showEditContactDialog(contact)
            }

            contactView.findViewById<Button>(R.id.delete_contact_button).setOnClickListener {
                dbHelper.deleteContact(contact)
                loadContacts(view)
            }

            contactsListLayout.addView(contactView)
        }
    }

    private fun showEditContactDialog(contact: Contact?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_contact, null)
        val editTextName = dialogView.findViewById<EditText>(R.id.editTextName)
        val editTextSurname = dialogView.findViewById<EditText>(R.id.editTextSurname)
        val editTextPhoneNumber = dialogView.findViewById<EditText>(R.id.editTextPhoneNumber)
        val buttonSave = dialogView.findViewById<Button>(R.id.buttonSave)
        val buttonDelete = dialogView.findViewById<Button>(R.id.buttonDelete)

        if (contact != null) {
            editTextName.setText(contact.name)
            editTextSurname.setText(contact.surname)
            editTextPhoneNumber.setText(contact.phoneNumber)
            buttonDelete.visibility = View.VISIBLE
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        buttonSave.setOnClickListener {
            val name = editTextName.text.toString()
            val surname = editTextSurname.text.toString()
            val phoneNumber = editTextPhoneNumber.text.toString()

            if (contact == null) {
                // Add new contact
                dbHelper.addContact(Contact(0, name, surname, phoneNumber))
            } else {
                // Update existing contact
                dbHelper.updateContact(Contact(contact.id, name, surname, phoneNumber))
            }

            dialog.dismiss()
            loadContacts(requireView())
        }

        buttonDelete.setOnClickListener {
            dbHelper.deleteContact(contact!!)
            dialog.dismiss()
            loadContacts(requireView())
        }

        dialog.show()
    }
}
