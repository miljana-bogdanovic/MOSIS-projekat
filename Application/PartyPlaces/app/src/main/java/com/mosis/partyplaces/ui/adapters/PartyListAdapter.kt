package com.mosis.partyplaces.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.mosis.partyplaces.R
import com.mosis.partyplaces.data.Party
import java.text.SimpleDateFormat

class PartyListAdapter(context: Context, private val parties: List<Party>)
    : ArrayAdapter<Party>(context, R.layout.party_list_item, parties) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val party = parties[position]

        val inflater = LayoutInflater.from(context)
        val rowView = inflater.inflate(R.layout.party_list_item, null)

        rowView.findViewById<TextView>(R.id.name).text = party.name
        rowView.findViewById<TextView>(R.id.location).text = party.address
        rowView.findViewById<TextView>(R.id.theme).text = party.theme
        rowView.findViewById<TextView>(R.id.guestNo).text = party.guestNo.toString()
        rowView.findViewById<TextView>(R.id.date).text = SimpleDateFormat("dd.MM.yyyy").format(party.day).toString()
        rowView.findViewById<TextView>(R.id.score).text = party.score.toString()

        return rowView
    }
}