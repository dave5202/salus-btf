package com.salus.widget.config

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.salus.widget.R
import com.salus.widget.data.Contact

/**
 * Adapter for displaying contacts in a RecyclerView with selection capability.
 */
class ContactsAdapter(
    private val onSelectionChanged: (Set<Contact>) -> Unit
) : ListAdapter<Contact, ContactsAdapter.ContactViewHolder>(ContactDiffCallback()) {

    private val selectedContacts = mutableSetOf<Contact>()

    /**
     * Sets the initially selected contacts.
     */
    fun setSelectedContacts(contacts: Set<Contact>) {
        selectedContacts.clear()
        selectedContacts.addAll(contacts)
        notifyDataSetChanged()
        onSelectionChanged(selectedContacts.toSet())
    }

    /**
     * Returns the currently selected contacts.
     */
    fun getSelectedContacts(): Set<Contact> = selectedContacts.toSet()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = getItem(position)
        holder.bind(contact, contact in selectedContacts)
    }

    inner class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val checkbox: CheckBox = itemView.findViewById(R.id.contact_checkbox)
        private val initialView: TextView = itemView.findViewById(R.id.contact_initial)
        private val nameView: TextView = itemView.findViewById(R.id.contact_name)
        private val phoneView: TextView = itemView.findViewById(R.id.contact_phone)

        fun bind(contact: Contact, isSelected: Boolean) {
            initialView.text = contact.getInitial()
            nameView.text = contact.name
            phoneView.text = contact.phoneNumber
            checkbox.isChecked = isSelected

            // Handle click on the entire row
            itemView.setOnClickListener {
                toggleSelection(contact)
            }

            checkbox.setOnClickListener {
                toggleSelection(contact)
            }
        }

        private fun toggleSelection(contact: Contact) {
            if (contact in selectedContacts) {
                selectedContacts.remove(contact)
            } else {
                selectedContacts.add(contact)
            }
            notifyItemChanged(adapterPosition)
            onSelectionChanged(selectedContacts.toSet())
        }
    }

    /**
     * DiffUtil callback for efficient list updates.
     */
    class ContactDiffCallback : DiffUtil.ItemCallback<Contact>() {
        override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem == newItem
        }
    }
}
