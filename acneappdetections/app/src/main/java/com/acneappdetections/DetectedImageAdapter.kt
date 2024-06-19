package com.acneappdetections

import android.content.ClipData
import android.content.ClipDescription
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class DetectedImageAdapter(private val imageUris: List<Uri>) :
    RecyclerView.Adapter<DetectedImageAdapter.DetectedImageViewHolder>() {

    class DetectedImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageViewDetectedImage)

        init {
            // Set long click listener for drag-and-drop functionality
            itemView.setOnLongClickListener { view ->
                val item = ClipData.Item(view.tag as CharSequence)
                val dragData = ClipData(
                    view.tag as CharSequence,
                    arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
                    item
                )

                val dragShadow = View.DragShadowBuilder(view)
                view.startDragAndDrop(dragData, dragShadow, view, 0)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetectedImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_detected_image, parent, false)
        return DetectedImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: DetectedImageViewHolder, position: Int) {
        val imageUri = imageUris[position]
        holder.imageView.tag = imageUri.toString()
        Glide.with(holder.itemView.context)
            .load(imageUri)
            .into(holder.imageView)
    }

    override fun getItemCount(): Int {
        return imageUris.size
    }
}
