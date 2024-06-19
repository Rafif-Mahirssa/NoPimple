package com.acneappdetections

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class DetectedAcneAdapter(private val acneList: List<DetectedAcne>) :
    RecyclerView.Adapter<DetectedAcneAdapter.DetectedAcneViewHolder>() {

    class DetectedAcneViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageViewDetectedAcne)
        val nameTextView: TextView = itemView.findViewById(R.id.textViewAcneName)
        val typeTextView: TextView = itemView.findViewById(R.id.textViewAcneType)
        val descriptionTextView: TextView = itemView.findViewById(R.id.textViewAcneDescription)
        val treatmentTextView: TextView = itemView.findViewById(R.id.textViewAcneTreatment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetectedAcneViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_detected_acne, parent, false)
        return DetectedAcneViewHolder(view)
    }

    override fun onBindViewHolder(holder: DetectedAcneViewHolder, position: Int) {
        val detectedAcne = acneList[position]
        holder.nameTextView.text = detectedAcne.name
        holder.typeTextView.text = detectedAcne.type
        holder.descriptionTextView.text = detectedAcne.description
        holder.treatmentTextView.text = detectedAcne.treatment

        // Load image using Glide
        Glide.with(holder.itemView.context)
            .load(Uri.parse(detectedAcne.imageUri))
            .into(holder.imageView)
    }

    override fun getItemCount(): Int {
        return acneList.size
    }
}
    