package com.example.nongkibib

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nongkibib.R
import com.example.nongkibib.model.SpotItem

class SpotAdapter(
    private val spots: List<SpotItem>,
    private val onItemClick: (SpotItem) -> Unit
) : RecyclerView.Adapter<SpotAdapter.SpotViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpotViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_spot, parent, false)
        return SpotViewHolder(view)
    }

    override fun onBindViewHolder(holder: SpotViewHolder, position: Int) {
        val spot = spots[position]
        holder.bind(spot, onItemClick)
    }

    override fun getItemCount(): Int = spots.size

    class SpotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvSpotName: TextView = itemView.findViewById(R.id.tvSpotName)
        private val tvSpotTypeAddress: TextView = itemView.findViewById(R.id.tvSpotTypeAddress)
        private val tvSpotRating: TextView = itemView.findViewById(R.id.tvSpotRating)
        private val ivSpotWifi: ImageView = itemView.findViewById(R.id.ivSpotWifi)

        fun bind(spot: SpotItem, onItemClick: (SpotItem) -> Unit) {
            tvSpotName.text = spot.name
            tvSpotTypeAddress.text = "${spot.type} • ${spot.address}"
            tvSpotRating.text = "⭐ ${spot.rating}"

            ivSpotWifi.visibility = if (spot.wifi) View.VISIBLE else View.GONE

            itemView.setOnClickListener {
                onItemClick(spot)
            }
        }
    }
}
